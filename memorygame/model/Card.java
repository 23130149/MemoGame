package memorygame.model;

import java.io.Serializable;

/**
 * Lớp đại diện cho một thẻ bài.
 * Cần implements Serializable để có thể lưu vào file (UC-09).
 */
public class Card implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int id;
    private final String value;
    private CardState state;

    public Card(int id, String value) {
        this.id = id;
        this.value = value;
        this.state = CardState.FACE_DOWN; // Mặc định ban đầu thẻ úp
    }

    public int getId() { return id; }
    public String getValue() { return value; }
    public CardState getState() { return state; }
    public void setState(CardState state) { this.state = state; }

    public boolean isMatched() { return state == CardState.MATCHED; }
    public boolean isFaceDown() { return state == CardState.FACE_DOWN; }
}