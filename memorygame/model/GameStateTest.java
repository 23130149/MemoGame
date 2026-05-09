package memorygame.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameStateTest {

    @Test
    void testInitialState() {
        GameState state = new GameState(5);

        assertNull(state.getFirstCard());
        assertNull(state.getSecondCard());
        assertFalse(state.isLocked());
        assertEquals(0, state.getScore());
        assertEquals(0, state.getMovesCount());
        assertEquals(5, state.getRemainingPairs());
    }

    @Test
    void testSetAndGetFirstCard() {
        GameState state = new GameState(1);
        Card card = new Card(1, "A");

        state.setFirstCard(card);

        assertSame(card, state.getFirstCard());
    }

    @Test
    void testSetAndGetSecondCard() {
        GameState state = new GameState(1);
        Card card = new Card(2, "A");

        state.setSecondCard(card);

        assertSame(card, state.getSecondCard());
    }

    @Test
    void testBoardLock() {
        GameState state = new GameState(1);

        assertFalse(state.isLocked());
        state.lockBoard(true);
        assertTrue(state.isLocked());
        state.lockBoard(false);
        assertFalse(state.isLocked());
    }

    @Test
    void testScoreUpdate() {
        GameState state = new GameState(1);

        assertEquals(0, state.getScore());
        state.updateScore(10);
        assertEquals(10, state.getScore());
        state.updateScore(5);
        assertEquals(15, state.getScore());
    }

    @Test
    void testMovesIncrement() {
        GameState state = new GameState(1);

        assertEquals(0, state.getMovesCount());
        state.incrementMoves();
        assertEquals(1, state.getMovesCount());
        state.incrementMoves();
        assertEquals(2, state.getMovesCount());
    }

    @Test
    void testRemainingPairsDecrement() {
        GameState state = new GameState(5);

        assertEquals(5, state.getRemainingPairs());
        state.decrementRemainingPairs();
        assertEquals(4, state.getRemainingPairs());
        state.decrementRemainingPairs();
        assertEquals(3, state.getRemainingPairs());
    }

    @Test
    void testResetTurnState() {
        GameState state = new GameState(1);
        Card card1 = new Card(1, "A");
        Card card2 = new Card(2, "A");

        state.setFirstCard(card1);
        state.setSecondCard(card2);
        state.lockBoard(true);

        state.resetTurnState();

        assertNull(state.getFirstCard());
        assertNull(state.getSecondCard());
        assertFalse(state.isLocked());
    }
}
