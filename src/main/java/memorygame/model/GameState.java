package memorygame.model;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class GameState {
    // Game state fields
    private Card firstCard;
    private Card secondCard;
    private boolean boardLocked;
    private int score;
    private int movesCount;
    private int remainingPairs;
    private int hintCount;
    private int timeLeftSec;
    private List<Card> cards;

    // Constructor 1: Legacy support - default 3 hints
    public GameState(int totalPairs) {
        this(totalPairs, 3);
    }

    // Constructor 2: Full initialization with hint count
    public GameState(int totalPairs, int hintCount) {
        this.remainingPairs = Math.max(0, totalPairs);
        this.hintCount = Math.max(0, hintCount);
        this.timeLeftSec = 0; // Sẽ được set từ GameEngine
    }

    // ===== CARD SELECTION =====
    public Card getFirstCard() {
        return firstCard;
    }

    public void setFirstCard(Card card) {
        this.firstCard = card;
    }

    public Card getSecondCard() {
        return secondCard;
    }

    public void setSecondCard(Card card) {
        this.secondCard = card;
    }

    // ===== BOARD STATE =====
    public boolean isLocked() {
        return boardLocked;
    }

    public void lockBoard(boolean locked) {
        this.boardLocked = locked;
    }

    // ===== SCORE MANAGEMENT =====
    public int getScore() {
        return score;
    }

    public void updateScore(int points) {
        this.score = Math.max(0, this.score + points);
    }

    public void resetScore() {
        this.score = 0;
    }

    // ===== MOVES TRACKING =====
    public int getMovesCount() {
        return movesCount;
    }

    public void incrementMoves() {
        this.movesCount++;
    }

    public void resetMoves() {
        this.movesCount = 0;
    }

    // ===== PAIRS MANAGEMENT =====
    public int getRemainingPairs() {
        return remainingPairs;
    }

    public void decrementRemainingPairs() {
        if (remainingPairs > 0) {
            remainingPairs--;
        }
    }

    // ===== HINT MANAGEMENT =====
    public int getHintCount() {
        return hintCount;
    }

    public void setHintCount(int count) {
        this.hintCount = Math.max(0, count);
    }

    public void decrementHint() {
        if (hintCount > 0) {
            hintCount--;
        }
    }

    // ===== TIME MANAGEMENT =====
    public int getTimeLeftSec() {
        return timeLeftSec;
    }

    public void setTimeLeftSec(int timeLeftSec) {
        this.timeLeftSec = Math.max(0, timeLeftSec);
    }

    public void decrementTimeLeft() {
        if (timeLeftSec > 0) {
            timeLeftSec--;
        }
    }

    public boolean isTimeUp() {
        return timeLeftSec <= 0;
    }

    // ===== CARDS MANAGEMENT =====
    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    // ===== TURN MANAGEMENT =====
    public void resetTurnState() {
        firstCard = null;
        secondCard = null;
        boardLocked = false;
    }

    // ===== PHẦN PHÁT TRIỂN BỞI NGUYỄN VĂN THẮNG - UC07 =====
    // Tối ưu bước hệ thống tìm cặp thẻ hợp lệ cho chức năng Yêu cầu gợi ý.
    // Tương ứng Use Case/Sequence Diagram UC07: chỉ xét các thẻ chưa matched,
    // đang ở trạng thái FACE_DOWN và tìm cặp cùng giá trị bằng HashMap để giảm độ phức tạp.
    // ===== HINT PAIR FINDING =====
    public Card[] findMatchPair() {
        if (cards == null || cards.size() < 2) {
            return null;
        }

        Map<String, Card> unseenMatches = new HashMap<>();
        for (Card card : cards) {
            if (canUseForHint(card)) {
                String value = card.getValue();
                if (unseenMatches.containsKey(value)) {
                    return new Card[] { unseenMatches.get(value), card };
                }
                unseenMatches.put(value, card);
            }
        }

        return null;
    }

    private boolean canUseForHint(Card card) {
        return card != null
                && !card.isMatched()
                && card.getState() == CardState.FACE_DOWN;
    }

    // ===== STATE RESTORATION (For Continue Game) =====
    public void restoreState(int score, int movesCount, int remainingPairs,
            boolean boardLocked, Card firstCard, Card secondCard) {
        this.score = Math.max(0, score);
        this.movesCount = Math.max(0, movesCount);
        this.remainingPairs = Math.max(0, remainingPairs);
        this.boardLocked = boardLocked;
        this.firstCard = firstCard;
        this.secondCard = secondCard;
    }

    // ===== RESET ALL (For New Game) =====
    public void resetAllGameState() {
        this.firstCard = null;
        this.secondCard = null;
        this.boardLocked = false;
        this.score = 0;
        this.movesCount = 0;
        this.timeLeftSec = 0;
    }
}
