package memorygame.persistence;

import java.io.Serializable;
import java.util.List;

public class GameSaveData implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int playerId;
    private final int levelId;

    private final int score;
    private final int movesCount;
    private final int remainingPairs;
    private final boolean boardLocked;

    private final Integer firstCardId;
    private final Integer secondCardId;

    private final List<CardSaveData> cards;

    public GameSaveData(
            int playerId,
            int levelId,
            int score,
            int movesCount,
            int remainingPairs,
            boolean boardLocked,
            Integer firstCardId,
            Integer secondCardId,
            List<CardSaveData> cards
    ) {
        this.playerId = playerId;
        this.levelId = levelId;
        this.score = score;
        this.movesCount = movesCount;
        this.remainingPairs = remainingPairs;
        this.boardLocked = boardLocked;
        this.firstCardId = firstCardId;
        this.secondCardId = secondCardId;
        this.cards = cards;
    }

    public int getPlayerId() { return playerId; }
    public int getLevelId() { return levelId; }
    public int getScore() { return score; }
    public int getMovesCount() { return movesCount; }
    public int getRemainingPairs() { return remainingPairs; }
    public boolean isBoardLocked() { return boardLocked; }
    public Integer getFirstCardId() { return firstCardId; }
    public Integer getSecondCardId() { return secondCardId; }
    public List<CardSaveData> getCards() { return cards; }
}
