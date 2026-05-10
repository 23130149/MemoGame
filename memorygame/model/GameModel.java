package memorygame.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý toàn bộ dữ liệu và trạng thái của ván chơi
 */
public class GameModel {
    private List<Card> cards;
    private int score;
    private int timer; // Đếm ngược thời gian (giây)
    private int movesCount; // Đếm số lượt lật thẻ
    private int remainingPairs; // Số cặp thẻ còn lại chưa tìm thấy
    private Card firstSelectedCard;
    private Card secondSelectedCard;

    public GameModel() {
        this.cards = new ArrayList<>();
        this.score = 0;
        this.timer = 60; // Thiết lập mặc định 60 giây cho mỗi ván (UC-06).
        this.movesCount = 0;
    }

    // Logic UC-06: Cập nhật điểm số và đảm bảo điểm không bao giờ âm.
    public void updateScore(int points) {
        this.score = Math.max(0, this.score + points);
    }

    // Logic UC-06: Tăng số lượt đi sau mỗi lần hoàn thành 1 lượt nhấn (UC-04).
    public void incrementMoves() { this.movesCount++; }

    // Getters & Setters
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getTimer() { return timer; }
    public void setTimer(int timer) { this.timer = timer; } // Dùng để giảm thời gian mỗi giây (UC-06).

    public List<Card> getCards() { return cards; }
    public void setCards(List<Card> cards) {
        this.cards = cards;
        this.remainingPairs = cards.size() / 2; // Tự động tính số cặp dựa trên tổng số thẻ
    }

    public int getRemainingPairs() { return remainingPairs; }
    public void decrementRemainingPairs() { this.remainingPairs--; }

    public int getMovesCount() { return movesCount; }
    public void setMovesCount(int moves) { this.movesCount = moves; }

    public Card getFirstSelected() { return firstSelectedCard; }
    public void setFirstSelected(Card card) { this.firstSelectedCard = card; }
    public Card getSecondSelected() { return secondSelectedCard; }
    public void setSecondSelected(Card card) { this.secondSelectedCard = card; }

    // Reset trạng thái sau mỗi lượt lật 2 thẻ (Dù đúng hay sai)
    public void resetTurn() {
        this.firstSelectedCard = null;
        this.secondSelectedCard = null;
    }
}