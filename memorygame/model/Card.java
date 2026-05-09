package memorygame.model;

public class Card {
    private final int id;
    private final String value;
    private CardState state;

    public Card(int id, String value) {
        this.id = id;
        this.value = value;
        this.state = CardState.FACE_DOWN;
    }

    public int getId() { return id; }
    public String getValue() { return value; }
    public CardState getState() { return state; }
    public void setState(CardState state) { this.state = state; }
    public boolean isMatched() { return state == CardState.MATCHED; }
    public boolean isFaceDown() { return state == CardState.FACE_DOWN; }
}
