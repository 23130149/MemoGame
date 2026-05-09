package memorygame.model;

import java.io.Serializable;
import java.util.List;

public class GameSaveData implements Serializable {
    private static final long serialVersionUID = 1L;

    public final int score;
    public final int timer;
    public final List<Card> cards;

    public GameSaveData(int score, int timer, List<Card> cards) {
        this.score = score;
        this.timer = timer;
        this.cards = cards;
    }
}