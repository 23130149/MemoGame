package memorygame.controller;

import memorygame.model.DifficultyLevel;
import memorygame.model.GameSession;

import java.util.ArrayList;
import java.util.List;

public class LevelSelectionController {
    public interface LevelSelectionListener {
        void onLevelListLoaded(List<DifficultyLevel> levels);
        void onLevelDetailShown(DifficultyLevel level);
        void onLevelConfirmed(GameSession session);
        void onSelectionCancelled();
        void onError(String errorCode, String message);
    }

    private final int playerId;
    private final LevelSelectionListener listener;

    private List<DifficultyLevel> availableLevels;
    private DifficultyLevel selectedLevel;
    private GameSession currentSession;

    public LevelSelectionController(int playerId, LevelSelectionListener listener) {
        this.playerId = playerId;
        this.listener = listener;
    }

    public List<DifficultyLevel> loadLevelList() {
        availableLevels = new ArrayList<>();
        for (DifficultyLevel.Level l : DifficultyLevel.Level.values()) {
            DifficultyLevel dl = new DifficultyLevel(l);
            if (dl.validate()) {             // EX-03: bỏ qua nếu data lỗi
                availableLevels.add(dl);
            }
        }

        if (availableLevels.isEmpty()) {
            if (listener != null) {
                listener.onError("EX-03", "Không thể tải danh sách cấp độ. Vui lòng khởi động lại.");
            }
            return availableLevels;
        }

        if (listener != null) {
            listener.onLevelListLoaded(availableLevels);
        }
        return availableLevels;
    }

    public GameSession selectLevel(DifficultyLevel level) {
        if (level == null || !level.validate()) {
            if (listener != null) {
                listener.onError("EX-03", "Dữ liệu cấp độ không hợp lệ.");
            }
            return null;
        }

        this.selectedLevel  = level;
        this.currentSession = new GameSession(playerId, level);

        if (listener != null) {
            listener.onLevelDetailShown(level);
        }
        return currentSession;
    }

    public void showLevelDetail(DifficultyLevel level) {
        if (listener != null) {
            listener.onLevelDetailShown(level);
        }
    }

    /*
     * UC-02 - Le VietKhanh: Phần nâng cấp xác nhận cấp độ.
     * Tác dụng: kiểm tra người chơi đã chọn cấp độ hợp lệ, lưu cấu hình phiên chơi,
     * chuyển GameSession sang trạng thái CONFIRMED, sau đó mới cho phép mở màn hình chơi.
     */
    public boolean confirmLevel(GameSession session) {
        // UC-02 - Le VietKhanh: Kiểm tra bắt buộc phải có cấp độ được chọn và phiên chơi tạm trước khi xác nhận.
        if (selectedLevel == null || session == null) {
            if (listener != null) {
                // UC-02 - Le VietKhanh: Gửi lỗi về UI khi người chơi bấm Xác nhận nhưng chưa chọn cấp độ.
                listener.onError("EX-01", "Vui lòng chọn cấp độ trước khi xác nhận.");
            }
            return false;
        }

        // UC-02 - Le VietKhanh: Lưu thông tin cấp độ vào GameSession; nếu dữ liệu không hợp lệ thì dừng luồng xác nhận.
        if (!session.save()) {
            if (listener != null) {
                listener.onError("EX-02", "Không thể lưu cấp độ. Vui lòng thử lại.");
            }
            return false;
        }

        // UC-02 - Le VietKhanh: Chuyển trạng thái phiên chơi từ LEVEL_SELECTED sang CONFIRMED.
        if (!session.confirm()) {
            if (listener != null) {
                listener.onError("EX-02", "Không thể xác nhận cấp độ. Vui lòng thử lại.");
            }
            return false;
        }

        if (listener != null) {
            // UC-02 - Le VietKhanh: Thông báo cho giao diện rằng cấp độ đã xác nhận thành công để chuyển sang màn hình game.
            listener.onLevelConfirmed(session);
        }
        return true;
    }

    public void cancelSelection(GameSession session) {
        if (session != null) {
            session.cancel();
        }
        selectedLevel  = null;
        currentSession = null;

        if (listener != null) {
            listener.onSelectionCancelled();
        }
    }

    public DifficultyLevel getSelectedLevel() { return selectedLevel; }
    public GameSession getCurrentSession()    { return currentSession; }
}