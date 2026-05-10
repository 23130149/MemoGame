package memorygame.model;

import java.io.Serializable;
import java.util.List;

/**
 * Đối tượng DTO dùng để đóng gói dữ liệu phục vụ UC-09 (Lưu tiến trình).
 */
public class GameSaveData implements Serializable {
    private static final long serialVersionUID = 1L;

    // Các thuộc tính public final để đảm bảo dữ liệu không bị thay đổi khi đang ghi file
    public final int score;
    public final int timer;
    public final List<Card> cards;

    public GameSaveData(int score, int timer, List<Card> cards) {
        this.score = score;
        this.timer = timer;
        this.cards = cards;
    }
}