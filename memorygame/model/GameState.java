package memorygame.model;

public class GameState {
    private Card firstCard;
    private Card secondCard;
    private boolean boardLocked;
    private int score;
    private int movesCount;
    private int remainingPairs;

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
}
