package memorygame.controller;

import memorygame.model.Card;
import memorygame.model.CardState;
import memorygame.model.GameModel;
import memorygame.model.LevelConfig;
import memorygame.view.GameBoardPanel;

import javax.swing.JOptionPane;
import javax.swing.Timer;

public class GameController {

    private final GameModel model;
    private final GameBoardPanel view;
    private Timer gameTimer;
    private boolean boardLocked = false;

    public GameController(GameModel model, GameBoardPanel view) {
        this.model = model;
        this.view  = view;
    }

    // UC-06: Khởi động game theo cấp độ
    public void startGame(LevelConfig level) {
        model.setLevel(level);
        model.resetScore();
        model.resetTimer();
        boardLocked = false;

        // Setup bàn chơi theo số hàng/cột của cấp độ
        view.setupBoard(level.getRows(), level.getCols());
        view.updateTimerDisplay(model.getTimer());
        view.updateScoreDisplay(model.getScore());

        initTimer();
        gameTimer.start();
    }

    // UC-06: Khởi tạo bộ đếm thời gian
    private void initTimer() {
        if (gameTimer != null && gameTimer.isRunning()) {
            gameTimer.stop();
        }
        gameTimer = new Timer(1000, e -> {
            int currentTime = model.getTimer();
            if (currentTime > 0) {
                model.setTimer(currentTime - 1);
                view.updateTimerDisplay(model.getTimer());

                // A3: Cảnh báo khi còn <= 10 giây
                if (model.getTimer() <= 10) {
                    view.showTimerWarning();
                }
            } else {
                // A4: Hết thời gian
                stopGame("Hết thời gian!");
            }
        });
    }

    // UC-04: Xử lý khi nhấn lật thẻ
    public void onCardClick(Card card) {
        if (boardLocked || card.isMatched() || card == model.getFirstSelected()) return;

        card.setState(CardState.FACE_UP);
        view.repaintCard(card);

        if (model.getFirstSelected() == null) {
            model.setFirstSelected(card);
        } else {
            model.setSecondSelected(card);
            boardLocked = true;
            checkMatch();
        }
    }

    // UC-05: Kiểm tra cặp thẻ trùng
    private void checkMatch() {
        Card c1 = model.getFirstSelected();
        Card c2 = model.getSecondSelected();
        model.incrementMoves();

        if (c1.getValue().equals(c2.getValue())) {
            // A1: Ghép đúng -> cộng điểm × multiplier
            c1.setState(CardState.MATCHED);
            c2.setState(CardState.MATCHED);
            model.updateScore(10);
            model.decrementRemainingPairs();
            view.showMatchEffect(c1, c2);
            view.updateScoreDisplay(model.getScore());
            model.resetTurn();
            boardLocked = false;
            checkEndCondition();
        } else {
            // A2: Ghép sai -> trừ điểm × multiplier, úp lại sau 1 giây
            model.updateScore(-2);
            view.updateScoreDisplay(model.getScore());
            Timer delay = new Timer(1000, e -> {
                c1.setState(CardState.FACE_DOWN);
                c2.setState(CardState.FACE_DOWN);
                view.repaintCard(c1);
                view.repaintCard(c2);
                model.resetTurn();
                boardLocked = false;
            });
            delay.setRepeats(false);
            delay.start();
        }
    }

    // UC-06: Kiểm tra điều kiện kết thúc
    private void checkEndCondition() {
        if (model.getRemainingPairs() == 0) {
            // A5: Hoàn thành sớm -> thưởng điểm thời gian còn lại × multiplier
            int bonusBase = model.getTimer() * 2;
            model.updateScore(bonusBase);
            view.updateScoreDisplay(model.getScore());
            stopGame("Chúc mừng! Bạn đã hoàn thành!");
        }
    }

    // UC-06: Dừng game
    private void stopGame(String message) {
        gameTimer.stop();
        boardLocked = true;
        view.lockBoard();
        view.showGameOver(model.getScore(), model.getMovesCount());
    }

    // Điều hướng về Menu
    public void backToMenu() {
        if (gameTimer != null && gameTimer.isRunning()) {
            gameTimer.stop();
        }
        JOptionPane.showMessageDialog(view, "Quay về Menu!");
    }
}