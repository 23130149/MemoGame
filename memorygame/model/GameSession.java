package memorygame.model;

import java.time.LocalDateTime;
public class GameSession {

    public enum Status {
        LEVEL_SELECTED,   // Đã chọn cấp độ, chưa xác nhận
        CONFIRMED,        // Đã xác nhận, sẵn sàng chơi
        IN_PROGRESS,      // Đang chơi
        CANCELLED         // Người chơi hủy (luồng 7B)
    }

    private final int sessionId;
    private final int playerId;
    private final DifficultyLevel level;
    private final LocalDateTime startedAt;
    private LocalDateTime confirmedAt;
    private Status status;

    private static int idCounter = 1;

    public GameSession(int playerId, DifficultyLevel level) {
        this.sessionId = idCounter++;
        this.playerId  = playerId;
        this.level     = level;
        this.startedAt = LocalDateTime.now();
        this.status    = Status.LEVEL_SELECTED;
    }

    public boolean save() {
        if (level == null || !level.validate()) {
            return false;
        }
        return true;
    }

    public boolean confirm() {
        if (status != Status.LEVEL_SELECTED) return false;
        this.confirmedAt = LocalDateTime.now();
        this.status      = Status.CONFIRMED;
        return true;
    }

    public void cancel() {
        this.status = Status.CANCELLED;
    }

    public int              getSessionId()   { return sessionId; }
    public int              getPlayerId()    { return playerId; }
    public DifficultyLevel  getLevel()       { return level; }
    public LocalDateTime    getStartedAt()   { return startedAt; }
    public LocalDateTime    getConfirmedAt() { return confirmedAt; }
    public Status           getStatus()      { return status; }

    public String getStatusText() {
        switch (status) {
            case LEVEL_SELECTED: return "Đã chọn";
            case CONFIRMED:      return "Đã xác nhận";
            case IN_PROGRESS:    return "Đang chơi";
            case CANCELLED:      return "Đã hủy";
            default:             return "Không xác định";
        }
    }
}