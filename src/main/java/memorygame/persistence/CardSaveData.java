package memorygame.persistence;

import memorygame.model.CardState;

import java.io.Serializable;

public class CardSaveData implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String value;
    private final CardState state;

    public CardSaveData(int id, String value, CardState state) {
        this.id = id;
        this.value = value;
        this.state = state;
    }

    public int getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public CardState getState() {
        return state;
    }
}
