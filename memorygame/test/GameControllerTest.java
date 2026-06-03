package memorygame.controller;

import memorygame.model.Card;
import memorygame.model.CardState;
import memorygame.model.GameState;
import memorygame.model.GameEngine;
import memorygame.view.GameBoardPanel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class GameControllerTest {

    private GameState gameState;
    private GameEngine engine;
    private GameBoardPanel boardPanel;
    private GameController controller;

    @BeforeEach
    void setUp() {
        gameState = new GameState(2);
        engine = new GameEngine();
        // restore engine với gameState test và danh sách card rỗng (tests thao tác trực
        // tiếp trên Card objects)
        engine.restore(null, gameState, new ArrayList<>());

        // GameBoardPanel hiện tại yêu cầu 2 tham số gridRows, gridCols
        boardPanel = new GameBoardPanel(1, 2);
        controller = new GameController(engine, boardPanel);
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

        // Sau khi click lần 2, board sẽ bị lock (trong thời gian chờ)
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
        assertEquals(card, gameState.getFirstCard()); // firstCard vẫn là card
        assertFalse(gameState.isLocked()); // board vẫn unlock
    }

    @Test
    void testGameEndWhenLastPairMatched() throws InterruptedException {
        // UC-05: Ghép cặp cuối cùng (remainingPairs = 1)
        Card a1 = new Card(1, "A");
        a1.setState(CardState.FACE_DOWN);

        Card a2 = new Card(2, "A");
        a2.setState(CardState.FACE_DOWN);

        // Setup: game chỉ còn 1 cặp thẻ
        gameState = new GameState(1); // remainingPairs = 1
        engine.restore(null, gameState, new ArrayList<>());
        controller = new GameController(engine, boardPanel);

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

    // UC-07: Yêu cầu gợi ý (Hint) — Test cases

    /**
     * Helper: Tạo GameBoardPanel stub không hiển thị JOptionPane.
     * Override showNotify() và showGameOver() để tránh block test bởi dialog.
     * Lưu lại message cuối cùng để assert nếu cần.
     */
    private static class StubBoardPanel extends GameBoardPanel {
        String lastNotifyMessage = null;
        int lastHintDisplayValue = -1;

        StubBoardPanel() {
            super(1, 2);
        }

        @Override
        public void showNotify(String message) {
            // Không hiện JOptionPane — chỉ ghi nhận message
            lastNotifyMessage = message;
        }

        @Override
        public void showGameOver(int score, int moves) {
            // Không hiện JOptionPane
        }

        @Override
        public void updateHintDisplay(int remainingHints) {
            lastHintDisplayValue = remainingHints;
        }

        @Override
        public void repaintCard(Card card) {
            // No-op trong test — tránh NullPointerException vì cardButtons chưa init
        }

        @Override
        public void showHintEffect(Card cardX, Card cardY) {
            // No-op trong test
        }

        @Override
        public void hideHintEffect(Card cardX, Card cardY) {
            // No-op trong test
        }
    }

    /**
     * Helper: Setup hint test với danh sách thẻ cụ thể.
     * Trả về controller mới sử dụng StubBoardPanel.
     */
    private HintTestContext setUpHintTest(int totalPairs, int hintCount, List<Card> cards) {
        GameState state = new GameState(totalPairs, hintCount);
        state.setCards(cards);

        GameEngine eng = new GameEngine();
        eng.restore(null, state, cards);

        StubBoardPanel stub = new StubBoardPanel();
        GameController ctrl = new GameController(eng, stub);

        return new HintTestContext(state, eng, stub, ctrl);
    }

    private static class HintTestContext {
        final GameState gameState;
        final GameEngine engine;
        final StubBoardPanel boardPanel;
        final GameController controller;

        HintTestContext(GameState gameState, GameEngine engine,
                StubBoardPanel boardPanel, GameController controller) {
            this.gameState = gameState;
            this.engine = engine;
            this.boardPanel = boardPanel;
            this.controller = controller;
        }
    }

    // UC-07 Test 1: Hết lượt gợi ý

    @Test
    void testHintWhenNoHintsRemaining() {
        // hintCount = 0, có cặp thẻ hợp lệ
        Card c1 = new Card(1, "A");
        Card c2 = new Card(2, "A");
        List<Card> cards = Arrays.asList(c1, c2);

        HintTestContext ctx = setUpHintTest(1, 0, cards);
        assertEquals(0, ctx.gameState.getHintCount());

        ctx.controller.onHintClick();

        // hint không bị trừ, board không bị lock
        assertEquals(0, ctx.gameState.getHintCount());
        assertFalse(ctx.gameState.isLocked());
        assertNotNull(ctx.boardPanel.lastNotifyMessage); // Thông báo "hết lượt"
    }

    // UC-07 Test 2: Board đang bị khóa

    @Test
    void testHintWhenBoardLocked() {
        // hintCount = 3, board đang locked (đang xử lý match)
        Card c1 = new Card(1, "A");
        Card c2 = new Card(2, "A");
        List<Card> cards = Arrays.asList(c1, c2);

        HintTestContext ctx = setUpHintTest(1, 3, cards);
        ctx.gameState.lockBoard(true); // Giả lập board đang locked

        int hintBefore = ctx.gameState.getHintCount();

        ctx.controller.onHintClick();

        // hint giữ nguyên, board vẫn locked (không đổi)
        assertEquals(hintBefore, ctx.gameState.getHintCount());
        assertTrue(ctx.gameState.isLocked()); // Vẫn locked như ban đầu
    }

    // UC-07 Test 3: Đang có thẻ lật dở (firstCard != null)

    @Test
    void testHintWhenCardFlipInProgress() {
        // hintCount = 3, đang có firstCard (lượt chưa hoàn thành)
        Card c1 = new Card(1, "A");
        Card c2 = new Card(2, "A");
        Card c3 = new Card(3, "B");
        List<Card> cards = Arrays.asList(c1, c2, c3);

        HintTestContext ctx = setUpHintTest(1, 3, cards);
        ctx.gameState.setFirstCard(c3); // Giả lập: đang lật thẻ c3

        int hintBefore = ctx.gameState.getHintCount();

        ctx.controller.onHintClick();

        // hint giữ nguyên, board không bị lock sai
        assertEquals(hintBefore, ctx.gameState.getHintCount());
        assertFalse(ctx.gameState.isLocked());
        assertNotNull(ctx.boardPanel.lastNotifyMessage); // Thông báo "hoàn thành lượt"
    }

    // UC-07 Test 4: Không tìm thấy cặp thẻ hợp lệ

    @Test
    void testHintDoesNotDecrementWhenNoPairFound() {
        // hintCount = 3, tất cả thẻ đã MATCHED → không có cặp
        Card c1 = new Card(1, "A");
        c1.setState(CardState.MATCHED);
        Card c2 = new Card(2, "A");
        c2.setState(CardState.MATCHED);
        List<Card> cards = Arrays.asList(c1, c2);

        HintTestContext ctx = setUpHintTest(0, 3, cards);

        int hintBefore = ctx.gameState.getHintCount();

        ctx.controller.onHintClick();

        // hintCount giữ nguyên, board KHÔNG bị khóa vĩnh viễn
        assertEquals(hintBefore, ctx.gameState.getHintCount());
        assertFalse(ctx.gameState.isLocked());
        assertNotNull(ctx.boardPanel.lastNotifyMessage); // "Không tìm thấy cặp"
    }

    // UC-07 Test 5: Hint hợp lệ — hintCount giảm đúng 1

    @Test
    void testHintDecrementsWhenPairFound() {
        // hintCount = 3, có cặp "A" hợp lệ (FACE_DOWN)
        Card c1 = new Card(1, "A");
        c1.setState(CardState.FACE_DOWN);
        Card c2 = new Card(2, "A");
        c2.setState(CardState.FACE_DOWN);
        Card c3 = new Card(3, "B");
        c3.setState(CardState.FACE_DOWN);
        Card c4 = new Card(4, "B");
        c4.setState(CardState.FACE_DOWN);
        List<Card> cards = Arrays.asList(c1, c2, c3, c4);

        HintTestContext ctx = setUpHintTest(2, 3, cards);

        int hintBefore = ctx.gameState.getHintCount();
        assertEquals(3, hintBefore);

        ctx.controller.onHintClick();

        // hintCount giảm đúng 1
        assertEquals(hintBefore - 1, ctx.gameState.getHintCount());
        assertEquals(2, ctx.gameState.getHintCount());

        // Board bị lock ngay sau khi hint bắt đầu (Timer đang chạy)
        assertTrue(ctx.gameState.isLocked());

        // updateHintDisplay đã được gọi với giá trị mới
        assertEquals(2, ctx.boardPanel.lastHintDisplayValue);
    }

}
