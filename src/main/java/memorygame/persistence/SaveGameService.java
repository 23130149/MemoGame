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
 * SaveGameService
 *
 * [BẢN GỐC - Tạ Gia Thuận - 23130321]
 * UC09: Lưu tiến trình
 * UC10: Ghi vào bộ nhớ (dùng Java Serialization / ObjectOutputStream)
 * UC11: Tiếp tục trò chơi
 * UC12: Tải tiến trình đã lưu (dùng ObjectInputStream)
 *
 * [PHÁT TRIỂN TIẾP - Phạm Quang Thiện - 23130312]
 * Nâng cấp 1: Dùng Gson/JSON thay Java Serialization
 *   → File lưu dạng JSON đọc được bằng tay, không hỏng khi thêm field mới vào class
 * Nâng cấp 2: Backup tự động trước khi ghi đè (.bak)
 *   → Có thể phục hồi bằng loadFromBackup() nếu file mới bị lỗi
 * Nâng cấp 3: Ghi an toàn qua file tạm (.tmp) + ATOMIC_MOVE
 *   → Crash giữa chừng không mất dữ liệu
 * Nâng cấp 4: hasSave() validate JSON thay vì chỉ check file tồn tại
 *   → Phát hiện file corrupt trước khi load
 */
public final class SaveGameService {
    // [PHÁT TRIỂN TIẾP - Phạm Quang Thiện]
    // Dùng Gson thay ObjectOutputStream/ObjectInputStream
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String BACKUP_SUFFIX = ".bak";

    private SaveGameService() {
    }

    // ==================== CHECK ====================

    /**
     * [PHÁT TRIỂN TIẾP - Phạm Quang Thiện]
     * UC11 - Bước 2: hasSave() kiểm tra file tồn tại VÀ parse được JSON VÀ dữ liệu hợp lệ
     * Bản gốc chỉ check Files.exists() — không phát hiện được file corrupt.
     * Bản nâng cấp: parse JSON và validate các field bắt buộc (cards, levelId).
     */
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
    // ==================== DELETE ====================

    /**
     * [PHÁT TRIỂN TIẾP - Phạm Quang Thiện]
     * Xóa file save VÀ file backup (.bak) đi kèm
     * Bản gốc chỉ xóa file chính, để lại .bak rác trên disk.
     */
    public static void deleteSave(Path savePath) throws IOException {
        Files.deleteIfExists(savePath);
        // Xóa luôn file backup nếu có
        Files.deleteIfExists(savePath.resolveSibling(savePath.getFileName() + BACKUP_SUFFIX));
    }
    public static void save(Path savePath, GameSession session, GameState state, List<Card> cards) throws IOException {
        save(savePath, session, state, cards, null);
    }
    /**
     * [PHÁT TRIỂN TIẾP - Phạm Quang Thiện]
     * UC09/UC10: Lưu tiến trình với các nâng cấp:
     *   - Bước 1: Validate dữ liệu đầu vào (giữ nguyên từ bản gốc)
     *   - Bước 2: Backup file cũ → .bak (THÊM MỚI)
     *   - Bước 3: Ghi JSON vào file .tmp (thay ObjectOutputStream)
     *   - Bước 4: Rename .tmp → .json bằng ATOMIC_MOVE (THÊM MỚI)
     */
    public static void save(Path savePath, GameSession session, GameState state, List<Card> cards,
                            PlayerProfile playerProfile) throws IOException {
        // Bước 1: Validate — giữ nguyên từ bản gốc
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
        // [PHÁT TRIỂN TIẾP - Phạm Quang Thiện]
        // Bước 2 (UC10): Backup file cũ thành .bak trước khi ghi đè
        // → Nếu ghi mới thất bại, file .bak vẫn còn để phục hồi

        backupIfExists(savePath);

        // [PHÁT TRIỂN TIẾP - Phạm Quang Thiện]
        // Bước 3 (UC10): Ghi JSON vào file tạm .tmp thay vì ObjectOutputStream
        // Bước 4 (UC10): Rename .tmp → .json bằng ATOMIC_MOVE
        // → Crash giữa chừng không làm hỏng file save cũ
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
    /**
     * [PHÁT TRIỂN TIẾP - Phạm Quang Thiện]
     * UC10 - Bước 2: Copy file save hiện tại thành .bak trước khi ghi đè.
     * Không throw exception — backup thất bại không nên chặn việc lưu.
     */
    private static void backupIfExists(Path savePath) {
        if (!Files.exists(savePath)) return;

        Path backupPath = savePath.resolveSibling(savePath.getFileName() + BACKUP_SUFFIX);
        try {
            Files.copy(savePath, backupPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("[SaveGameService] Cảnh báo: không thể tạo backup: " + e.getMessage());
        }
    }
    /**
     * [PHÁT TRIỂN TIẾP - Phạm Quang Thiện]
     * UC11 - Luồng rẽ nhánh A2: Phục hồi từ file backup khi file chính bị hỏng.
     * Bản gốc không có cơ chế phục hồi này.
     */
    public static LoadedGame loadFromBackup(Path savePath) throws IOException, ClassNotFoundException {
        Path backupPath = savePath.resolveSibling(savePath.getFileName() + BACKUP_SUFFIX);
        if (!Files.exists(backupPath)) {
            throw new FileNotFoundException("Không có file backup để khôi phục");
        }
        return load(backupPath);
    }
    // ==================== LOAD ====================

    /**
     * [PHÁT TRIỂN TIẾP - Phạm Quang Thiện]
     * UC11/UC12: Tải tiến trình với các nâng cấp:
     *   - Đọc file JSON thay ObjectInputStream
     *   - Bắt JsonSyntaxException riêng biệt (phát hiện file corrupt rõ ràng hơn)
     *   - Validate data != null sau khi parse
     */
    public static LoadedGame load(Path savePath) throws IOException, ClassNotFoundException {
        if (!hasSave(savePath)) {
            throw new FileNotFoundException("Không tìm thấy file save: " + savePath);
        }
        // [PHÁT TRIỂN TIẾP - Phạm Quang Thiện]
        // UC12 - Bước 1: Đọc file JSON thay vì ObjectInputStream
        String json;
        try {
            json = Files.readString(savePath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IOException("Không thể đọc file save", e);
        }
        // [PHÁT TRIỂN TIẾP - Phạm Quang Thiện]
        // UC12 - Bước 2: Parse JSON → GameSaveData, bắt lỗi corrupt rõ ràng
        GameSaveData data;
        try {
            data = GSON.fromJson(json, GameSaveData.class);
        } catch (JsonSyntaxException e) {
            throw new IOException("File save bị hỏng hoặc sai định dạng", e);
        }

        if (data == null) {
            throw new IOException("File save rỗng hoặc không hợp lệ");
        }
        // UC12 - Bước 3: Validate levelId — giữ nguyên từ bản gốc
        DifficultyLevel.Level[] levels = DifficultyLevel.Level.values();
        int idx = data.getLevelId() - 1;
        if (idx < 0 || idx >= levels.length) {
            throw new IllegalStateException("levelId trong save không hợp lệ: " + data.getLevelId());
        }
        // UC12 - Bước 4: Khôi phục GameSession — giữ nguyên từ bản gốc
        DifficultyLevel level = new DifficultyLevel(levels[idx]);

        GameSession session = new GameSession(data.getPlayerId(), level);
        session.save();
        session.confirm();
        // UC12 - Bước 5: Khôi phục danh sách Card — giữ nguyên từ bản gốc
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
        // UC12 - Bước 6: Khôi phục GameState — giữ nguyên từ bản gốc
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