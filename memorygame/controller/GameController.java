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

    private final GameEngine engine;
    private final GameBoardPanel boardPanel;
    private final CardFlipController cardFlipController;

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
        // Lật lại thẻ
        cardFlipController.flipCardFaceDown(first);
        cardFlipController.flipCardFaceDown(second);
        boardPanel.showNoMatchEffect(first, second);
        boardPanel.repaintCard(first);
        boardPanel.repaintCard(second);
    }

    private void resetTurn() {
        GameState gameState = engine.getGameState();
        gameState.resetTurnState();
        gameState.lockBoard(false);
        boardPanel.setBoardLocked(false);
    }

    public void onHintClick() {
        GameState gameState = engine.getGameState();

        // Kiểm tra điều kiện sử dụng gợi ý
        if (!checkGameState(gameState)) {
            return;
        }

        // Khóa board và trừ lượt hint
        gameState.lockBoard(true);
        gameState.decrementHint();
        boardPanel.updateHintDisplay(gameState.getHintCount());

        // Tìm cặp thẻ để gợi ý
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
        boardPanel.showHintEffect(cardX, cardY);

        // Lật lại sau HINT_DISPLAY_MS
        Timer hintTimer = new Timer(HINT_DELAY_MS, e -> {
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
        });
        hintTimer.setRepeats(false);
        hintTimer.start();
    }

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
