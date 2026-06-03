package memorygame.test;

import memorygame.model.Card;
import memorygame.model.CardState;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CardTest {

    @Test
    void testCardInitialization() {
        Card card = new Card(1, "A");

        assertEquals(1, card.getId());
        assertEquals("A", card.getValue());
        assertEquals(CardState.FACE_DOWN, card.getState());
    }

    @Test
    void testSetState() {
        Card card = new Card(1, "A");

        card.setState(CardState.FACE_UP);
        assertEquals(CardState.FACE_UP, card.getState());

        card.setState(CardState.MATCHED);
        assertEquals(CardState.MATCHED, card.getState());
    }

    @Test
    void testIsMatched() {
        Card card = new Card(1, "A");

        assertFalse(card.isMatched());

        card.setState(CardState.MATCHED);
        assertTrue(card.isMatched());
    }

    @Test
    void testIsFaceDown() {
        Card card = new Card(1, "A");

        assertTrue(card.isFaceDown());

        card.setState(CardState.FACE_UP);
        assertFalse(card.isFaceDown());
    }

    @Test
    void testMultipleCardsWithSameValue() {
        Card card1 = new Card(1, "A");
        Card card2 = new Card(2, "A");

        assertEquals(card1.getValue(), card2.getValue());
        assertNotEquals(card1.getId(), card2.getId());
    }
}
