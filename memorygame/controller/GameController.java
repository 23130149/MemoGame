package memorygame.controller;

import memorygame.model.Card;
import memorygame.model.CardState;
import memorygame.model.GameEngine;
import memorygame.view.GameBoardPanel;

import javax.swing.Timer;

public class GameController {
    private static final int MATCH_POINTS = 10;
    private static final int NO_MATCH_DELAY_MS = 1000;
    private static final int HINT_DISPLAY_MS = 2000;  // 2 giây

    private final GameEngine engine;
    private final GameBoardPanel boardPanel;
    private final CardFlipController cardFlipController;
    private static final int FLIP_DELAY_MS = 1000;

    public GameController(GameEngine engine, GameBoardPanel boardPanel) {
        this.engine = engine;
        this.boardPanel = boardPanel;
        this.cardFlipController = new CardFlipController();

        boardPanel.setOnCardClicked(this::handleCardClick);
        boardPanel.initializeBoard(engine.getCards());
    }

    private void handleCardClick(Card clickedCard) {
        // Không cho click nếu board locked hoặc thẻ đã match
        if (engine.getGameState().isLocked() || clickedCard.isMatched()) {
            return;
        }

        // Flip thẻ lên
        cardFlipController.flipCardFaceUp(clickedCard);
        boardPanel.repaintCard(clickedCard);

        var gameState = engine.getGameState();

        // Nếu chưa có thẻ đầu tiên
        if (gameState.getFirstCard() == null) {
            gameState.setFirstCard(clickedCard);
            return;
        }

        // Nếu click lại thẻ đầu tiên
        if (gameState.getFirstCard() == clickedCard) {
            return;
        }

        // Có thẻ thứ hai
        gameState.setSecondCard(clickedCard);
        gameState.incrementMoves();
        gameState.lockBoard(true);
        boardPanel.setBoardLocked(true);

        // Kiểm tra match sau delay
        new Timer(FLIP_DELAY_MS, e -> {
            checkMatch();
            ((Timer)e.getSource()).stop();
        }).start();
    }

    // public wrapper để tests / các lớp khác có thể gọi
    public void onCardClick(Card card) {
        handleCardClick(card);
    }

    private void checkMatch() {
        var gameState = engine.getGameState();
        Card first = gameState.getFirstCard();
        Card second = gameState.getSecondCard();

        if (first == null || second == null) {
            // không đủ dữ kiện
            gameState.resetTurnState();
            gameState.lockBoard(false);
            boardPanel.setBoardLocked(false);
            return;
        }

        if (first.getValue().equals(second.getValue())) {
            // ✓ Match thành công
            first.setState(CardState.MATCHED);
            second.setState(CardState.MATCHED);
            gameState.decrementRemainingPairs();
            // dùng hệ số điểm từ level (nếu có)
            if (engine.getSession() != null && engine.getSession().getLevel() != null) {
                gameState.updateScore((int)(100 * engine.getSession().getLevel().getScoreMultiplier()));
            } else {
                gameState.updateScore(MATCH_POINTS);
            }
            boardPanel.showMatchEffect(first, second);

            if (gameState.getRemainingPairs() == 0) {
                boardPanel.showGameOver(gameState.getScore(), gameState.getMovesCount());
            }
        } else {
            // ✗ Không match
            cardFlipController.flipCardFaceDown(first);
            cardFlipController.flipCardFaceDown(second);
            boardPanel.showNoMatchEffect(first, second);
            boardPanel.repaintCard(first);
            boardPanel.repaintCard(second);
        }

        gameState.resetTurnState();
        gameState.lockBoard(false);
        boardPanel.setBoardLocked(false);
    }

    private boolean isValidSelection(Card card) {
        if (card.isMatched()) return false;
        return card.getState() != CardState.FACE_UP;
    }

    public void onHintClick() {
        var gameState = engine.getGameState();

        if (!checkGameState()) {
            return;
        }

        // khóa bàn, trừ lượt hint
        gameState.lockBoard(true);
        gameState.decrementHint();
        boardPanel.updateHintDisplay(gameState.getHintCount());

        Card[] pair = gameState.findMatchPair();
        if (pair == null) {
            gameState.lockBoard(false);
            boardPanel.showNotify("Không tìm thấy cặp để gợi ý.");
            return;
        }

        Card cardX = pair[0];
        Card cardY = pair[1];

        cardX.setState(CardState.FACE_UP);
        cardY.setState(CardState.FACE_UP);
        boardPanel.repaintCard(cardX);
        boardPanel.repaintCard(cardY);

        // [Animation] Lật tạm 2 thẻ lên
        boardPanel.showHintEffect(cardX, cardY);

        // Hiển thị và ẩn gợi ý sau HINT_DISPLAY_MS
        Timer hintTimer = new Timer(HINT_DISPLAY_MS, e -> {
            if (!cardX.isMatched()) {
                cardX.setState(CardState.FACE_DOWN);
                boardPanel.repaintCard(cardX);
            }
            if (!cardY.isMatched()) {
                cardY.setState(CardState.FACE_DOWN);
                boardPanel.repaintCard(cardY);
            }

            boardPanel.hideHintEffect(cardX, cardY);
            gameState.lockBoard(false);
            boardPanel.setBoardLocked(false);
            ((Timer)e.getSource()).stop();
        });
        hintTimer.setRepeats(false);
        hintTimer.start();
    }

    private boolean checkGameState() {
        var gameState = engine.getGameState();
        // Điều kiện 1: hintCount > 0
        if (gameState.getHintCount() <= 0) {
            boardPanel.showNotify("Bạn đã hết lượt gợi ý.");
            return false;
        }

        // Điều kiện 2: boardLocked = false
        if (gameState.isLocked()) {
            boardPanel.showNotify("Board đang bị khóa. Vui lòng chờ.");
            return false;
        }

        // Điều kiện 3: không có thẻ đang mở
        if (gameState.getFirstCard() != null || gameState.getSecondCard() != null) {
            boardPanel.showNotify("Hoàn thành lượt hiện tại trước khi dùng gợi ý.");
            return false;
        }

        return true;
    }

    public GameEngine getEngine() {
        return engine;
    }
}
