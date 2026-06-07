package memorygame.controller;

import memorygame.model.DifficultyLevel;
import memorygame.model.GameSession;

import java.util.ArrayList;
import java.util.List;

public class LevelSelectionController {
    public interface LevelSelectionListener {
        void onLevelListLoaded(List<DifficultyLevel> levels);
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

            // UC-02 - Le VietKhanh: kiểm tra dữ liệu cấp độ trước khi đưa lên giao diện.
            // Tác dụng: bỏ qua cấp độ không hợp lệ để tránh lỗi khi người chơi xác nhận.
            if (dl.validate()) {
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

    public boolean selectLevel(DifficultyLevel level) {
        // UC-02 - Le VietKhanh: phần nâng cấp chọn cấp độ.
        // Tác dụng: chỉ lưu cấp độ đang chọn, không hiện popup và không tạo GameSession quá sớm.
        if (level == null || !level.validate()) {
            if (listener != null) {
                listener.onError("EX-03", "Dữ liệu cấp độ không hợp lệ.");
            }
            return false;
        }

        this.selectedLevel = level;
        this.currentSession = null;
        return true;
    }

    public boolean confirmLevel() {
        // UC-02 - Le VietKhanh: phần nâng cấp xác nhận cấp độ.
        // Tác dụng: chỉ khi người chơi bấm Xác nhận thì mới tạo GameSession,
        // lưu cấu hình, chuyển trạng thái CONFIRMED và cho phép mở màn hình chơi.
        if (selectedLevel == null) {
            if (listener != null) {
                listener.onError("EX-01", "Vui lòng chọn cấp độ trước khi xác nhận.");
            }
            return false;
        }

        GameSession session = new GameSession(playerId, selectedLevel);

        if (!session.save()) {
            if (listener != null) {
                listener.onError("EX-02", "Không thể lưu cấp độ. Vui lòng thử lại.");
            }
            return false;
        }

        if (!session.confirm()) {
            if (listener != null) {
                listener.onError("EX-02", "Không thể xác nhận cấp độ. Vui lòng thử lại.");
            }
            return false;
        }

        this.currentSession = session;

        if (listener != null) {
            listener.onLevelConfirmed(session);
        }
        return true;
    }

    public boolean confirmLevel(GameSession session) {
        return confirmLevel();
    }

    public void cancelSelection() {
        // UC-02 - Le VietKhanh: hủy lựa chọn cấp độ.
        // Tác dụng: nếu đã có phiên chơi thì chuyển sang CANCELLED, sau đó xóa lựa chọn hiện tại.
        if (currentSession != null) {
            currentSession.cancel();
        }
        selectedLevel = null;
        currentSession = null;

        if (listener != null) {
            listener.onSelectionCancelled();
        }
    }

    public void cancelSelection(GameSession session) {
        if (session != null) {
            session.cancel();
        }
        cancelSelection();
    }

    public DifficultyLevel getSelectedLevel() { return selectedLevel; }
    public GameSession getCurrentSession() { return currentSession; }
}
