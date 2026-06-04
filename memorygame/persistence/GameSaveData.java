package memorygame.persistence;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private final int hintCount;
    private final int timeLeftSec;
    private final long playerGold;
    private final String selectedBackSkinId;
    private final String selectedFaceThemeId;
    private final Set<String> ownedBackSkinIds;
    private final Set<String> ownedFaceThemeIds;

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
            String selectedBackSkinId,
            String selectedFaceThemeId,
            Set<String> ownedBackSkinIds,
            Set<String> ownedFaceThemeIds
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
        this.selectedBackSkinId = selectedBackSkinId;
        this.selectedFaceThemeId = selectedFaceThemeId;
        this.ownedBackSkinIds = new HashSet<>(ownedBackSkinIds);
        this.ownedFaceThemeIds = new HashSet<>(ownedFaceThemeIds);
    }

    // ===== GETTERS =====
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

    public String getSelectedBackSkinId() {
        return selectedBackSkinId;
    }

    public String getSelectedFaceThemeId() {
        return selectedFaceThemeId;
    }

    public Set<String> getOwnedBackSkinIds() {
        if (ownedBackSkinIds == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(ownedBackSkinIds);
    }

    public Set<String> getOwnedFaceThemeIds() {
        if (ownedFaceThemeIds == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(ownedFaceThemeIds);
    }
}
