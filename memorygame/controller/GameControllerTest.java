package memorygame.controller;

import memorygame.model.Card;
import memorygame.model.CardState;
import memorygame.model.GameState;
import memorygame.view.GameBoardPanel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class GameControllerTest {

    private GameState gameState;
    private GameBoardPanel boardPanel;
    private GameController controller;

    @BeforeEach
    void setUp() {
        gameState = new GameState(2);
        boardPanel = new GameBoardPanel();
        controller = new GameController(gameState, boardPanel);
    }

    @Test
    void testClickFirstCard() {
        Card card = new Card(1, "A");
        card.setState(CardState.FACE_DOWN);

        controller.onCardClick(card);

        assertEquals(CardState.FACE_UP, card.getState());
        assertSame(card, gameState.getFirstCard());
        assertNull(gameState.getSecondCard());
        assertFalse(gameState.isLocked());
    }

    @Test
    void testClickSecondCardAndMatch() {
        Card a1 = new Card(1, "A");
        a1.setState(CardState.FACE_DOWN);

        Card a2 = new Card(2, "A");
        a2.setState(CardState.FACE_DOWN);

        controller.onCardClick(a1);
        assertEquals(CardState.FACE_UP, a1.getState());

        controller.onCardClick(a2);

        // Sau khi click lần 2, board sẽ bị lock
        assertTrue(gameState.isLocked());
        assertEquals(CardState.MATCHED, a1.getState());
        assertEquals(CardState.MATCHED, a2.getState());
        assertEquals(10, gameState.getScore());
        assertEquals(1, gameState.getMovesCount());
        assertEquals(1, gameState.getRemainingPairs());
    }

    @Test
    void testClickSecondCardAndNoMatch() throws InterruptedException {
        Card a = new Card(1, "A");
        a.setState(CardState.FACE_DOWN);

        Card b = new Card(2, "B");
        b.setState(CardState.FACE_DOWN);

        controller.onCardClick(a);
        controller.onCardClick(b);

        // Sau click thứ 2, board lock và chờ timer 1 giây
        assertTrue(gameState.isLocked());
        assertEquals(CardState.FACE_UP, a.getState());
        assertEquals(CardState.FACE_UP, b.getState());

        // Chờ timer kết thúc + 200ms buffer
        Thread.sleep(1300);

        // Sau timer: thẻ quay lại FACE_DOWN, board unlock
        assertEquals(CardState.FACE_DOWN, a.getState());
        assertEquals(CardState.FACE_DOWN, b.getState());
        assertFalse(gameState.isLocked());
        assertEquals(1, gameState.getMovesCount());
    }

    @Test
    void testCannotClickWhenBoardLocked() {
        Card a1 = new Card(1, "A");
        Card a2 = new Card(2, "A");

        gameState.lockBoard(true);

        controller.onCardClick(a1);

        assertNull(gameState.getFirstCard());
        assertEquals(CardState.FACE_DOWN, a1.getState());
    }

    @Test
    void testCannotClickMatchedCard() {
        Card card = new Card(1, "A");
        card.setState(CardState.MATCHED);

        controller.onCardClick(card);

        assertNull(gameState.getFirstCard());
    }

    @Test
    void testCannotClickFaceUpCard() {
        Card card1 = new Card(1, "A");
        Card card2 = new Card(2, "B");

        card1.setState(CardState.FACE_DOWN);
        card2.setState(CardState.FACE_UP);

        controller.onCardClick(card1);
        assertEquals(card1, gameState.getFirstCard());

        controller.onCardClick(card2);

        // card2 là FACE_UP, nên không thể click
        assertNotSame(card2, gameState.getSecondCard());
    }

    @Test
    void testGameComplete() {
        Card a1 = new Card(1, "A");
        Card a2 = new Card(2, "A");

        gameState.setFirstCard(a1);
        gameState.setSecondCard(a2);
        gameState.lockBoard(true);

        controller.onCardClick(a1);
        controller.onCardClick(a2);

        // Đây là lần click thứ nhất sau reset
        // Sau khi match, remainingPairs giảm từ 2 -> 1
        // Nếu là lần cuối cùng (remainingPairs == 0), sẽ show GameOver
        assertEquals(CardState.MATCHED, a1.getState());
        assertEquals(CardState.MATCHED, a2.getState());
    }

    @Test
    void testScoreNotIncrementOnNoMatch() throws InterruptedException {
        Card a = new Card(1, "A");
        Card b = new Card(2, "B");

        controller.onCardClick(a);
        controller.onCardClick(b);

        int scoreBeforeWait = gameState.getScore();
        Thread.sleep(1300);
        int scoreAfterWait = gameState.getScore();

        assertEquals(scoreBeforeWait, scoreAfterWait);
        assertEquals(0, scoreAfterWait);
    }
    @Test
    void testCannotClickSameCardTwice() {
        // UC-04: Nhấn lại thẻ thứ nhất (thẻ vừa lật lên)
        Card card = new Card(1, "A");
        card.setState(CardState.FACE_DOWN);

        // Lần 1: Nhấn thẻ A
        controller.onCardClick(card);
        assertEquals(CardState.FACE_UP, card.getState());
        assertEquals(card, gameState.getFirstCard());

        // Lần 2: Nhấn lại chính thẻ A (đang FACE_UP)
        controller.onCardClick(card);

        // Kỳ vọng: secondCard vẫn null, không có phản hồi
        assertNull(gameState.getSecondCard());
        assertEquals(card, gameState.getFirstCard());  // firstCard vẫn là card
        assertFalse(gameState.isLocked());  // board vẫn unlock
    }
    @Test
    void testGameEndWhenLastPairMatched() throws InterruptedException {
        // UC-05: Ghép cặp cuối cùng (remainingPairs = 1)
        Card a1 = new Card(1, "A");
        a1.setState(CardState.FACE_DOWN);

        Card a2 = new Card(2, "A");
        a2.setState(CardState.FACE_DOWN);

        // Setup: game chỉ còn 1 cặp thẻ
        gameState = new GameState(1);  // remainingPairs = 1
        controller = new GameController(gameState, boardPanel);

        // Click thẻ đầu tiên
        controller.onCardClick(a1);
        assertEquals(CardState.FACE_UP, a1.getState());
        assertEquals(a1, gameState.getFirstCard());

        // Click thẻ thứ hai (cùng value)
        controller.onCardClick(a2);

        // Ngay sau click: cả 2 MATCHED, remainingPairs = 0
        assertEquals(CardState.MATCHED, a1.getState());
        assertEquals(CardState.MATCHED, a2.getState());
        assertEquals(10, gameState.getScore());
        assertEquals(0, gameState.getRemainingPairs());

        // Game kết thúc (showGameOver được gọi)
        // Note: không thể test UI trực tiếp, nhưng state phải đúng
        assertEquals(1, gameState.getMovesCount());
    }
    @Test
    void testMovesCountIncrementsAfterMatch() {
        // UC-05: movesCount tăng sau mỗi lượt
        Card a1 = new Card(1, "A");
        a1.setState(CardState.FACE_DOWN);

        Card a2 = new Card(2, "A");
        a2.setState(CardState.FACE_DOWN);

        assertEquals(0, gameState.getMovesCount());

        controller.onCardClick(a1);
        controller.onCardClick(a2);

        // ✅ Sau match: movesCount tăng 1
        assertEquals(1, gameState.getMovesCount());
    }

    @Test
    void testMovesCountIncrementsAfterNoMatch() throws InterruptedException {
        // UC-05: movesCount tăng sau mỗi lượt (no-match)
        Card a = new Card(1, "A");
        a.setState(CardState.FACE_DOWN);

        Card b = new Card(2, "B");
        b.setState(CardState.FACE_DOWN);

        assertEquals(0, gameState.getMovesCount());

        controller.onCardClick(a);
        controller.onCardClick(b);

        Thread.sleep(1300);

        // ✅ Sau no-match: movesCount tăng 1
        assertEquals(1, gameState.getMovesCount());
    }

    @Test
    void testGameStateResetAfterMatch() {
        // Reset GameState: Sau khi xử lý lý xong
        Card a1 = new Card(1, "A");
        a1.setState(CardState.FACE_DOWN);

        Card a2 = new Card(2, "A");
        a2.setState(CardState.FACE_DOWN);

        controller.onCardClick(a1);
        controller.onCardClick(a2);

        // ✅ Sau match: reset state
        assertNull(gameState.getFirstCard());
        assertNull(gameState.getSecondCard());
        assertFalse(gameState.isLocked());
    }

    @Test
    void testGameStateResetAfterNoMatch() throws InterruptedException {
        // Reset GameState: Sau khi xử lý lý xong (no-match)
        Card a = new Card(1, "A");
        a.setState(CardState.FACE_DOWN);

        Card b = new Card(2, "B");
        b.setState(CardState.FACE_DOWN);

        controller.onCardClick(a);
        controller.onCardClick(b);

        Thread.sleep(1300);

        // ✅ Sau no-match + delay: reset state
        assertNull(gameState.getFirstCard());
        assertNull(gameState.getSecondCard());
        assertFalse(gameState.isLocked());
    }

}
