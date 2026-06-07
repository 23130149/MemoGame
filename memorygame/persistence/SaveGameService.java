package memorygame.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import memorygame.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * SaveGameService - Nâng cấp:
 * 1. Dùng JSON (Gson) thay vì Java Serialization
 * 2. Backup file cũ trước khi ghi đè
 * 3. Validate file trước khi load
 * 4. hasSave() kiểm tra file có đọc được không
 */
public final class SaveGameService {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String BACKUP_SUFFIX = ".bak";

    private SaveGameService() {
    }

    public static boolean hasSave(Path savePath) {
        if (!Files.exists(savePath) || !Files.isRegularFile(savePath)) return false;
        try {
            String json = Files.readString(savePath, StandardCharsets.UTF_8);
            GameSaveData data = GSON.fromJson(json, GameSaveData.class);
            return data != null && data.getCards() != null && !data.getCards().isEmpty() && data.getLevelId() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static void deleteSave(Path savePath) throws IOException {
        Files.deleteIfExists(savePath);
        Files.deleteIfExists(savePath.resolveSibling(savePath.getFileName() + BACKUP_SUFFIX));
    }

    public static void save(Path savePath, GameSession session, GameState state, List<Card> cards) throws IOException {
        save(savePath, session, state, cards, null);
    }

    public static void save(Path savePath, GameSession session, GameState state, List<Card> cards,
                            PlayerProfile playerProfile) throws IOException {
        if (session == null || state == null || cards == null || cards.isEmpty()) {
            throw new IllegalArgumentException("Dữ liệu game để lưu không hợp lệ.");
        }
        if (state.isLocked()) {
            throw new IllegalStateException("Không thể lưu khi đang chờ lật lại thẻ.");
        }

        List<CardSaveData> cardData = new ArrayList<>(cards.size());
        for (Card c : cards) {
            cardData.add(new CardSaveData(c.getId(), c.getValue(), c.getState()));
        }

        Integer firstId = state.getFirstCard() == null ? null : state.getFirstCard().getId();
        Integer secondId = state.getSecondCard() == null ? null : state.getSecondCard().getId();

        long playerGold = 0;
        String selectedThemeId = ShopCatalog.DEFAULT_THEME_ID;
        Set<String> ownedThemeIds = new HashSet<>(Collections.singleton(ShopCatalog.DEFAULT_THEME_ID));

        if (playerProfile != null) {
            playerGold = playerProfile.getGold();
            selectedThemeId = playerProfile.getSelectedThemeId();
            ownedThemeIds = new HashSet<>(playerProfile.getOwnedThemeIds());
        }

        GameSaveData saveData = new GameSaveData(
                session.getPlayerId(),
                session.getLevel().getLevelId(),
                state.getScore(),
                state.getMovesCount(),
                state.getRemainingPairs(),
                state.isLocked(),
                firstId,
                secondId,
                cardData,
                state.getHintCount(),
                state.getTimeLeftSec(),
                playerGold,
                selectedThemeId,
                ownedThemeIds
        );

        Path parent = savePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        backupIfExists(savePath);

        Path tempPath = savePath.resolveSibling(savePath.getFileName() + ".tmp");

        try {
            String json = GSON.toJson(saveData);
            Files.writeString(tempPath, json, StandardCharsets.UTF_8);
            Files.move(tempPath, savePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            Files.deleteIfExists(tempPath);
            throw e;
        }
    }

    private static void backupIfExists(Path savePath) {
        if (!Files.exists(savePath)) return;

        Path backupPath = savePath.resolveSibling(savePath.getFileName() + BACKUP_SUFFIX);
        try {
            Files.copy(savePath, backupPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("[SaveGameService] Cảnh báo: không thể tạo backup: " + e.getMessage());
        }
    }

    public static LoadedGame loadFromBackup(Path savePath) throws IOException, ClassNotFoundException {
        Path backupPath = savePath.resolveSibling(savePath.getFileName() + BACKUP_SUFFIX);
        if (!Files.exists(backupPath)) {
            throw new FileNotFoundException("Không có file backup để khôi phục");
        }
        return load(backupPath);
    }

    public static LoadedGame load(Path savePath) throws IOException, ClassNotFoundException {
        if (!hasSave(savePath)) {
            throw new FileNotFoundException("Không tìm thấy file save: " + savePath);
        }

        String json;
        try {
            json = Files.readString(savePath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IOException("Không thể đọc file save", e);
        }

        GameSaveData data;
        try {
            data = GSON.fromJson(json, GameSaveData.class);
        } catch (JsonSyntaxException e) {
            throw new IOException("File save bị hỏng hoặc sai định dạng", e);
        }

        if (data == null) {
            throw new IOException("File save rỗng hoặc không hợp lệ");
        }

        DifficultyLevel.Level[] levels = DifficultyLevel.Level.values();
        int idx = data.getLevelId() - 1;
        if (idx < 0 || idx >= levels.length) {
            throw new IllegalStateException("levelId trong save không hợp lệ: " + data.getLevelId());
        }

        DifficultyLevel level = new DifficultyLevel(levels[idx]);

        GameSession session = new GameSession(data.getPlayerId(), level);
        session.save();
        session.confirm();

        List<Card> cards = new ArrayList<>(data.getCards().size());
        Map<Integer, Card> byId = new HashMap<>();

        for (CardSaveData c : data.getCards()) {
            Card card = new Card(c.getId(), c.getValue());
            card.setState(c.getState());
            cards.add(card);
            byId.put(card.getId(), card);
        }

        Card first = data.getFirstCardId() == null ? null : byId.get(data.getFirstCardId());
        Card second = data.getSecondCardId() == null ? null : byId.get(data.getSecondCardId());

        GameState state = new GameState(data.getRemainingPairs(), data.getHintCount());
        state.setTimeLeftSec(data.getTimeLeftSec());

        state.restoreState(
                data.getScore(),
                data.getMovesCount(),
                data.getRemainingPairs(),
                data.isBoardLocked(),
                first,
                second
        );

        PlayerProfile playerProfile = new PlayerProfile();
        playerProfile.restoreFromSave(
                data.getPlayerGold(),
                data.getSelectedThemeId(),
                data.getOwnedThemeIds()
        );

        return new LoadedGame(session, state, cards, playerProfile);
    }
}