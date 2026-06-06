package memorygame.controller;

import memorygame.model.Card;
import memorygame.model.CardState;
import memorygame.model.GameEngine;
import memorygame.model.GameState;
import memorygame.view.GameBoardPanel;

import javax.swing.Timer;

public class GameController {
    private static final int MATCH_POINTS = 10;
    private static final int FLIP_DELAY_MS = 1000;
    private static final int HINT_DELAY_MS = 1500;
    private static final int MISMATCH_PENALTY = 5;

    private final GameEngine engine;
    private final GameBoardPanel boardPanel;
    private final memorygame.controller.CardFlipController cardFlipController;

    public GameController(GameEngine engine, GameBoardPanel boardPanel) {
        this.engine = engine;
        this.boardPanel = boardPanel;
        this.cardFlipController = new memorygame.controller.CardFlipController();

        boardPanel.setOnCardClicked(this::handleCardClick);
        boardPanel.initializeBoard(engine.getCards());
    }

    private void handleCardClick(Card clickedCard) {
        // Không cho click nếu board locked hoặc thẻ đã match
        if (engine.getGameState().isLocked() || clickedCard.isMatched()) {
            return;
        }

        if (clickedCard.getState() == CardState.FACE_UP) {
            return;
        }

        GameState gameState = engine.getGameState();

        // Lật thẻ lên
        cardFlipController.flipCardFaceUp(clickedCard);
        boardPanel.repaintCard(clickedCard);

        // Nếu chưa có thẻ đầu tiên
        if (gameState.getFirstCard() == null) {
            gameState.setFirstCard(clickedCard);
            return;
        }

        // Nếu click lại thẻ đầu tiên
        if (gameState.getFirstCard() == clickedCard) {
            return;
        }

        // Có thẻ thứ hai - lock board và kiểm tra match
        gameState.setSecondCard(clickedCard);
        gameState.incrementMoves();
        gameState.lockBoard(true);
        boardPanel.setBoardLocked(true);

        // Delay trước khi kiểm tra match
        new Timer(FLIP_DELAY_MS, e -> {
            checkMatch();
            ((Timer) e.getSource()).stop();
        }).start();
    }

    public void onCardClick(Card card) {
        handleCardClick(card);
    }

    private void checkMatch() {
        GameState gameState = engine.getGameState();
        Card first = gameState.getFirstCard();
        Card second = gameState.getSecondCard();

        if (first == null || second == null) {
            resetTurn();
            return;
        }

        if (first.getValue().equals(second.getValue())) {
            // ✓ Match thành công
            handleMatchSuccess(first, second, gameState);
        } else {
            // ✗ Không match
            handleMatchFailed(first, second, gameState);
        }

        resetTurn();
    }

    private void handleMatchSuccess(Card first, Card second, GameState gameState) {
        // Đánh dấu thẻ đã match
        first.setState(CardState.MATCHED);
        second.setState(CardState.MATCHED);
        gameState.decrementRemainingPairs();

        // Cộng điểm theo hệ số difficulty
        if (engine.getSession() != null && engine.getSession().getLevel() != null) {
            int points = (int) (100 * engine.getSession().getLevel().getScoreMultiplier());
            gameState.updateScore(points);
        } else {
            gameState.updateScore(MATCH_POINTS);
        }
        boardPanel.showMatchEffect(first, second);

        if (gameState.getRemainingPairs() == 0) {
            boardPanel.showGameOver(gameState.getScore(), gameState.getMovesCount());
        }
    }

    private void handleMatchFailed(Card first, Card second, GameState gameState) {
        boardPanel.showNoMatchEffect(first, second);
        cardFlipController.flipCardFaceDown(first);
        cardFlipController.flipCardFaceDown(second);
        gameState.updateScore(-MISMATCH_PENALTY);
    }

    private void resetTurn() {
        GameState gameState = engine.getGameState();
        gameState.resetTurnState();
        gameState.lockBoard(false);
        boardPanel.setBoardLocked(false);
    }

    /**
     * UC-07 / UC-08: Xử lý sự kiện khi người chơi nhấn nút "Gợi ý".
     * [Thắng - UC07/UC08]
     * @return true nếu hint đã bắt đầu hiển thị, false nếu bị reject
     */
    public boolean onHintClick() {
        // Bước 1: UI triggers onHintClick()
        // → Người chơi nhấn nút "Gợi ý" trên giao diện, sự kiện
        // được chuyển đến Controller thông qua callback.
        GameState gameState = engine.getGameState();

        // Bước 2: Validate state (checkGameState)
        // → Kiểm tra: còn lượt hint? Board có đang khóa? Có thẻ
        // đang được lật dở? Nếu vi phạm → trả về ngay, không
        // thực hiện gợi ý.
        if (!checkGameState(gameState)) {
            return false;
        }

        // Bước 3: lockBoard(true)
        // → Khóa toàn bộ board để ngăn người chơi tương tác trong
        // khi hiệu ứng gợi ý đang hiển thị.
        gameState.lockBoard(true);
        boardPanel.setBoardLocked(true);

        // Bước 4: findMatchPair() TRƯỚC khi trừ hint
        // [Sửa lỗi UC07 - Thắng]: Tìm cặp thẻ hợp lệ trước.
        // Nếu không tìm thấy → mở khóa board, thông báo, KHÔNG trừ hint.
        Card[] pair = gameState.findMatchPair();
        if (pair == null) {
            gameState.lockBoard(false);
            boardPanel.setBoardLocked(false);
            boardPanel.showNotify("Không tìm thấy cặp để gợi ý.");
            return false; // Không decrementHint() → hintCount không bị trừ oan
        }

        // Bước 5: decrementHint() chỉ khi đã tìm thấy cặp hợp lệ
        // [Sửa lỗi UC07 - Thắng]: Đảm bảo hint chỉ bị trừ khi
        // thực sự hiển thị gợi ý cho người chơi.
        gameState.decrementHint();
        boardPanel.updateHintDisplay(gameState.getHintCount());

        Card cardX = pair[0];
        Card cardY = pair[1];

        // Bước 6: Set cards FACE_UP and show UI Hint Effect
        // → Lật 2 thẻ lên (FACE_UP), repaint để hiển thị mặt
        // trước, đồng thời kích hoạt hiệu ứng viền sáng
        // (highlight) trên giao diện.
        cardX.setState(CardState.FACE_UP);
        cardY.setState(CardState.FACE_UP);
        boardPanel.repaintCard(cardX);
        boardPanel.repaintCard(cardY);
        boardPanel.showHintEffect(cardX, cardY);

        // Bước 7: Start Timer (1500ms delay)
        // → Khởi tạo Timer với độ trễ HINT_DELAY_MS (1500ms) để
        // người chơi có thời gian ghi nhớ vị trí cặp thẻ.
        // Timer chỉ chạy 1 lần (setRepeats = false).
        Timer hintTimer = new Timer(HINT_DELAY_MS, e -> {

            // Bước 8: Timer ends -> Revert cards to FACE_DOWN if not
            // matched, hide effect
            // → Sau 1500ms, kiểm tra từng thẻ: nếu chưa được
            // match bởi người chơi trong thời gian chờ → lật
            // úp lại (FACE_DOWN). Tắt hiệu ứng gợi ý.
            if (!cardX.isMatched()) {
                cardX.setState(CardState.FACE_DOWN);
                boardPanel.repaintCard(cardX);
            }
            if (!cardY.isMatched()) {
                cardY.setState(CardState.FACE_DOWN);
                boardPanel.repaintCard(cardY);
            }

            boardPanel.hideHintEffect(cardX, cardY);

            // Bước 9: lockBoard(false)
            // → Mở khóa board, cho phép người chơi tiếp tục
            // tương tác bình thường với các thẻ.
            gameState.lockBoard(false);
            boardPanel.setBoardLocked(false);

            // Bước 10: Thông báo hint animation kết thúc để UI re-enable nút
            boardPanel.notifyHintAnimationDone();
        });
        hintTimer.setRepeats(false);
        hintTimer.start();
        return true;
    }

    /**
     * UC-07 / UC-08 — Bước 2: Validate trạng thái game trước khi
     * thực hiện gợi ý.
     * Điều kiện hợp lệ:
     * Còn lượt gợi ý ({@code hintCount > 0})
     * Board không bị khóa ({@code !isLocked()})
     * Không có thẻ đang trong lượt lật dở
     */
    private boolean checkGameState(GameState gameState) {
        if (gameState.getHintCount() <= 0) {
            boardPanel.showNotify("Bạn đã hết lượt gợi ý.");
            return false;
        }

        if (gameState.isLocked()) {
            boardPanel.showNotify("Board đang bị khóa. Vui lòng chờ.");
            return false;
        }

        if (gameState.getFirstCard() != null || gameState.getSecondCard() != null) {
            boardPanel.showNotify("Hoàn thành lượt hiện tại trước khi dùng gợi ý.");
            return false;
        }

        return true;
    }

    // ===== GETTERS =====
    public GameEngine getEngine() {
        return engine;
    }
}
