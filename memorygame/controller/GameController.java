package memorygame.controller;

import memorygame.model.Card;
import memorygame.model.CardState;
import memorygame.model.GameEngine;
import memorygame.view.GameBoardPanel;

import javax.swing.Timer;

public class GameController {

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

    private void checkMatch() {
        var gameState = engine.getGameState();
        Card first = gameState.getFirstCard();
        Card second = gameState.getSecondCard();

        if (first == null || second == null) {
            return;
        }

        if (first.getValue().equals(second.getValue())) {
            // ✓ Match thành công
            first.setState(CardState.MATCHED);
            second.setState(CardState.MATCHED);
            gameState.decrementRemainingPairs();
            gameState.updateScore((int)(100 * engine.getSession().getLevel().getScoreMultiplier()));
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

    public GameEngine getEngine() {
        return engine;
    }
}
