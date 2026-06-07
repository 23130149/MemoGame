package memorygame.persistence;

import java.util.List;
import java.util.Set;

public class GameSaveData {

    private int playerId;
    private int levelId;
    private int score;
    private int movesCount;
    private int remainingPairs;
    private boolean boardLocked;
    private Integer firstCardId;
    private Integer secondCardId;
    private List<CardSaveData> cards;
    private int hintCount;
    private int timeLeftSec;

    private long playerGold;
    private String selectedThemeId;
    private Set<String> ownedThemeIds;

    public GameSaveData(
            int playerId,
            int levelId,
            int score,
            int movesCount,
            int remainingPairs,
            boolean boardLocked,
            Integer firstCardId,
            Integer secondCardId,
            List<CardSaveData> cards,
            int hintCount,
            int timeLeftSec,
            long playerGold,
            String selectedThemeId,
            Set<String> ownedThemeIds
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
        this.hintCount = hintCount;
        this.timeLeftSec = timeLeftSec;
        this.playerGold = playerGold;
        this.selectedThemeId = selectedThemeId;
        this.ownedThemeIds = ownedThemeIds;
    }

    public int getPlayerId() {
        return playerId;
    }

    public int getLevelId() {
        return levelId;
    }

    public int getScore() {
        return score;
    }

    public int getMovesCount() {
        return movesCount;
    }

    public int getRemainingPairs() {
        return remainingPairs;
    }

    public boolean isBoardLocked() {
        return boardLocked;
    }

    public Integer getFirstCardId() {
        return firstCardId;
    }

    public Integer getSecondCardId() {
        return secondCardId;
    }

    public List<CardSaveData> getCards() {
        return cards;
    }

    public int getHintCount() {
        return hintCount;
    }

    public int getTimeLeftSec() {
        return timeLeftSec;
    }

    public long getPlayerGold() {
        return playerGold;
    }

    public String getSelectedThemeId() {
        return selectedThemeId;
    }

    public Set<String> getOwnedThemeIds() {
        return ownedThemeIds;
    }
}