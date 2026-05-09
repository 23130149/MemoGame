package memorygame.controller;

import memorygame.model.Card;
import memorygame.model.CardState;
import memorygame.model.GameState;
import memorygame.view.GameBoardPanel;
import javax.swing.*;

public class GameController {
    private static final int MATCH_POINTS = 10;
    private static final int NO_MATCH_DELAY_MS = 1000;

    private final GameState gameState;
    private final GameBoardPanel boardPanel;

    public GameController(GameState gameState, GameBoardPanel boardPanel) {
        this.gameState = gameState;
        this.boardPanel = boardPanel;
    }

    // UC-04: Xử lý khi người chơi nhấn thẻ
    public void onCardClick(Card card) {
        if (gameState.isLocked()) return;
        if (!isValidSelection(card)) return;

        if (gameState.getFirstCard() == null) {
            // Lật thẻ thứ nhất
            card.setState(CardState.FACE_UP);
            gameState.setFirstCard(card);
            boardPanel.repaintCard(card);
        } else {
            // Lật thẻ thứ hai
            card.setState(CardState.FACE_UP);
            gameState.setSecondCard(card);
            gameState.lockBoard(true);
            boardPanel.repaintCard(card);

            // Kích hoạt UC-05
            checkMatch();
        }
    }

    // UC-05: Kiểm tra cặp thẻ trùng
    private void checkMatch() {
        Card first = gameState.getFirstCard();
        Card second = gameState.getSecondCard();

        if (first.getValue().equals(second.getValue())) {
            handleMatch(first, second);
        } else {
            handleNoMatch(first, second);
        }
    }

    private void handleMatch(Card first, Card second) {
        first.setState(CardState.MATCHED);
        second.setState(CardState.MATCHED);
        gameState.updateScore(MATCH_POINTS);
        gameState.decrementRemainingPairs();
        gameState.incrementMoves();
        boardPanel.showMatchEffect(first, second);
        boardPanel.updateScoreDisplay(gameState.getScore());
        gameState.resetTurnState();

        if (gameState.getRemainingPairs() == 0) {
            boardPanel.showGameOver(gameState.getScore(), gameState.getMovesCount());
        }
    }

    private void handleNoMatch(Card first, Card second) {
        boardPanel.showNoMatchEffect(first, second);
        Timer timer = new Timer(NO_MATCH_DELAY_MS, e -> {
            first.setState(CardState.FACE_DOWN);
            second.setState(CardState.FACE_DOWN);
            boardPanel.repaintCard(first);
            boardPanel.repaintCard(second);
            gameState.incrementMoves();
            gameState.resetTurnState();
        });
        timer.setRepeats(false);
        timer.start();
    }

    private boolean isValidSelection(Card card) {
        if (card.isMatched()) return false;
        if (card.getState() == CardState.FACE_UP) return false;
        return true;
    }
}
