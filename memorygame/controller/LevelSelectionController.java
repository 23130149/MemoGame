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

    public boolean confirmLevel(GameSession session) {
        if (selectedLevel == null || session == null) {
            if (listener != null) {
                listener.onError("EX-01", "Vui lòng chọn cấp độ trước khi xác nhận.");
            }
            return false;
        }

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

        if (listener != null) {
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