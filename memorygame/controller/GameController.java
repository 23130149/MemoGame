package memorygame.controller;

import memorygame.model.Card;
import memorygame.model.CardState;
import memorygame.model.GameState;
import memorygame.view.GameBoardPanel;
import javax.swing.*;

public class GameController {
    private static final int MATCH_POINTS = 10;
    private static final int NO_MATCH_DELAY_MS = 1000;
    private static final int HINT_DISPLAY_MS = 2000;  // 2 giây

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
        return card.getState() != CardState.FACE_UP;
    }

    public void onHintClick() {
        if (!checkGameState()) {
            return;
        }

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

        // [Animation] Lật tam 2 thẻ lên
        boardPanel.showHintEffect(cardX, cardY);

        // ===== BUỘC 2 - Hiển thị và Ẩn gợi ý =====
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


        });
        hintTimer.setRepeats(false);
        hintTimer.start();
    }


    private boolean checkGameState() {
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
}
