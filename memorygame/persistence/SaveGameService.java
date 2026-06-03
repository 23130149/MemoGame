package memorygame.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import memorygame.model.Card;
import memorygame.model.DifficultyLevel;
import memorygame.model.GameSession;
import memorygame.model.GameState;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class SaveGameService {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
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
            return data != null
                    && data.getCards() != null
                    && !data.getCards().isEmpty()
                    && data.getLevelId() > 0;
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
                state.getTimeLeftSec()
        );

        // Tạo thư mục nếu chưa có
        Path parent = savePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        // Serialize vào file
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(savePath.toFile()))) {
            out.writeObject(saveData);
        }
    }

    public static LoadedGame load(Path savePath) throws IOException, ClassNotFoundException {
        if (!hasSave(savePath)) {
            throw new FileNotFoundException("Không tìm thấy file save: " + savePath);
        }

        // Deserialize từ file
        GameSaveData data;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(savePath.toFile()))) {
            data = (GameSaveData) in.readObject();
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
        state.restoreState(
                data.getScore(),
                data.getMovesCount(),
                data.getRemainingPairs(),
                data.isBoardLocked(),
                first,
                second
        );

        return new LoadedGame(session, state, cards);
    }
}