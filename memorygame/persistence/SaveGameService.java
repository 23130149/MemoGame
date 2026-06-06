package memorygame.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import memorygame.model.Card;
import memorygame.model.DifficultyLevel;
import memorygame.model.GameSession;
import memorygame.model.GameState;
import memorygame.model.PlayerProfile;

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

    // ==================== CHECK ====================

    /**
     * Kiểm tra file save tồn tại VÀ có thể đọc được (không bị corrupt)
     */
    public static boolean hasSave(Path savePath) {
        if (!Files.exists(savePath) || !Files.isRegularFile(savePath)) return false;
        try {
            String json = Files.readString(savePath, StandardCharsets.UTF_8);
            GameSaveData data = GSON.fromJson(json, GameSaveData.class);
            return data != null && data.getCards() != null && !data.getCards().isEmpty() && data.getLevelId() > 0;
        } catch (Exception e) {
            return false; // file corrupt → coi như không có save
        }
    }
    /**
     * Xóa file save
     */
    public static void deleteSave(Path savePath) throws IOException {
        Files.deleteIfExists(savePath);
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

        // Serialize danh sách thẻ
        List<CardSaveData> cardData = new ArrayList<>(cards.size());
        for (Card c : cards) {
            cardData.add(new CardSaveData(c.getId(), c.getValue(), c.getState()));
        }

        // Lấy ID của 2 thẻ đang chọn (nếu có)
        Integer firstId = state.getFirstCard() == null ? null : state.getFirstCard().getId();
        Integer secondId = state.getSecondCard() == null ? null : state.getSecondCard().getId();

        long playerGold = 0;
        String selectedBackSkinId = "default_back";
        String selectedFaceThemeId = "default_face";
        Set<String> ownedBackSkinIds = new HashSet<>(Collections.singleton("default_back"));
        Set<String> ownedFaceThemeIds = new HashSet<>(Collections.singleton("default_face"));

        if (playerProfile != null) {
            playerGold = playerProfile.getGold();
            selectedBackSkinId = playerProfile.getSelectedBackSkinId();
            selectedFaceThemeId = playerProfile.getSelectedFaceThemeId();
            ownedBackSkinIds = new HashSet<>(playerProfile.getOwnedBackSkinIds());
            ownedFaceThemeIds = new HashSet<>(playerProfile.getOwnedFaceThemeIds());
        }

        // Tạo GameSaveData với tất cả thông tin game
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
                selectedBackSkinId,
                selectedFaceThemeId,
                ownedBackSkinIds,
                ownedFaceThemeIds
        );

        // Tạo thư mục nếu chưa có
        Path parent = savePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        // Backup file cũ trước khi ghi đè
        backupIfExists(savePath);

        // Ghi vào file tạm, sau đó rename (tránh mất dữ liệu nếu crash giữa chừng)
        Path tempPath = savePath.resolveSibling(savePath.getFileName() + ".tmp");

        // Serialize vào file
        try {
            String json = GSON.toJson(saveData);
            Files.writeString(tempPath, json, StandardCharsets.UTF_8);
            Files.move(tempPath, savePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            // Xóa file tạm nếu lỗi
            Files.deleteIfExists(tempPath);
            throw e;
        }
    }
    // ==================== BACKUP ====================

    /**
     * Backup file save cũ thành .bak trước khi ghi đè
     */
    private static void backupIfExists(Path savePath) {
        if (!Files.exists(savePath)) return;
        Path backupPath = savePath.resolveSibling(savePath.getFileName() + BACKUP_SUFFIX);
        try {
            Files.copy(savePath, backupPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            // Không throw — backup thất bại không nên chặn việc lưu
            System.err.println("[SaveGameService] Cảnh báo: không thể tạo backup: " + e.getMessage());
        }
    }
    /**
     * Khôi phục từ file backup (dùng khi file chính bị hỏng)
     */
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

        // Deserialize từ file JSON đã ghi bằng Gson.
        GameSaveData data;
        String json = Files.readString(savePath, StandardCharsets.UTF_8);
        data = GSON.fromJson(json, GameSaveData.class);
        if (data == null) {
            throw new IOException("File save không hợp lệ hoặc bị rỗng.");
        }

        // Kiểm tra level ID hợp lệ
        DifficultyLevel.Level[] levels = DifficultyLevel.Level.values();
        int idx = data.getLevelId() - 1;
        if (idx < 0 || idx >= levels.length) {
            throw new IllegalStateException("levelId trong save không hợp lệ: " + data.getLevelId());
        }

        // Tạo level từ levelId
        DifficultyLevel level = new DifficultyLevel(levels[idx]);

        // Tạo session từ dữ liệu đã lưu
        GameSession session = new GameSession(data.getPlayerId(), level);
        session.save();
        session.confirm();

        // Reconstruct danh sách thẻ từ CardSaveData
        List<Card> cards = new ArrayList<>(data.getCards().size());
        Map<Integer, Card> byId = new HashMap<>();

        for (CardSaveData c : data.getCards()) {
            Card card = new Card(c.getId(), c.getValue());
            card.setState(c.getState());
            cards.add(card);
            byId.put(card.getId(), card);
        }

        // Lấy 2 thẻ đang chọn (nếu có)
        Card first = data.getFirstCardId() == null ? null : byId.get(data.getFirstCardId());
        Card second = data.getSecondCardId() == null ? null : byId.get(data.getSecondCardId());

        // Tạo GameState mới
        GameState state = new GameState(data.getRemainingPairs(), data.getHintCount());

        // Set thời gian từ dữ liệu đã lưu
        state.setTimeLeftSec(data.getTimeLeftSec());  //

        // Restore toàn bộ trạng thái
        state.restoreState(data.getScore(), data.getMovesCount(), data.getRemainingPairs(), data.isBoardLocked(), first, second);

        PlayerProfile playerProfile = new PlayerProfile();
        playerProfile.restoreFromSave(
                data.getPlayerGold(),
                data.getSelectedBackSkinId(),
                data.getSelectedFaceThemeId(),
                data.getOwnedBackSkinIds(),
                data.getOwnedFaceThemeIds()
        );

        return new LoadedGame(session, state, cards, playerProfile);
    }
}