package memorygame.model;

import java.util.List;

public class GameState {
    private Card firstCard;
    private Card secondCard;
    private boolean boardLocked;
    private int score;
    private int movesCount;
    private int remainingPairs;

    private int hintCount = 3;
    private List<Card> cards;

    public GameState(int totalPairs) {
        this.remainingPairs = totalPairs;
    }

    public Card getFirstCard() { return firstCard; }
    public void setFirstCard(Card card) { this.firstCard = card; }

    public Card getSecondCard() { return secondCard; }
    public void setSecondCard(Card card) { this.secondCard = card; }

    public boolean isLocked() { return boardLocked; }
    public void lockBoard(boolean locked) { this.boardLocked = locked; }

    public int getScore() { return score; }
    public void updateScore(int points) { this.score += points; }

    public int getMovesCount() { return movesCount; }
    public void incrementMoves() { this.movesCount++; }

    public int getRemainingPairs() { return remainingPairs; }
    public void decrementRemainingPairs() { this.remainingPairs--; }

    public void resetTurnState() {
        firstCard = null;
        secondCard = null;
        boardLocked = false;
    }

    public int getHintCount() { return hintCount; }
    public void setHintCount(int count) { this.hintCount = count; }

    public void decrementHint() {
        if (hintCount > 0) hintCount--;
    }

    public List<Card> getCards() { return cards; }
    public void setCards(List<Card> cards) { this.cards = cards; }

    public Card[] findMatchPair() {
        if (cards == null || cards.isEmpty()) return null;

        for (int i = 0; i < cards.size(); i++) {
            Card a = cards.get(i);
            // Bỏ qua thẻ đã match hoặc đang FACE_UP
            if (a.isMatched() || a.getState() != CardState.FACE_DOWN) continue;

            for (int j = i + 1; j < cards.size(); j++) {
                Card b = cards.get(j);
                // Bỏ qua thẻ đã match hoặc đang FACE_UP
                if (b.isMatched() || b.getState() != CardState.FACE_DOWN) continue;

                if (a.getValue().equals(b.getValue())) {
                    return new Card[] { a, b };
                }
            }
        }
        return null;
    }
}
