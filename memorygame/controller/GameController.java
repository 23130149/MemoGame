package memorygame.controller;

import memorygame.model.*;
import memorygame.service.PersistenceManager;
import memorygame.view.GameBoardPanel;
import javax.swing.*;

public class GameController {
    private final GameModel model; // Dùng GameModel làm dữ liệu chính
    private final GameBoardPanel view;
    private Timer gameTimer;
    private boolean boardLocked = false;

    // Constructor: Kết nối Model và View
    public GameController(GameModel model, GameBoardPanel view) {
        this.model = model;
        this.view = view;
        initTimer(); // Khởi tạo bộ đếm thời gian
    }

    // ==========================================================
    // UC-06: TÍNH ĐIỂM & ĐẾM THỜI GIAN
    // ==========================================================
    private void initTimer() {
        gameTimer = new Timer(1000, e -> {
            int currentTime = model.getTimer();
            if (currentTime > 0) {
                model.setTimer(currentTime - 1); // Logic UC6: Trừ 1 giây mỗi lần tick
                view.updateTimerDisplay(model.getTimer());
            } else {
                stopGame("Hết thời gian!");
            }
        });
    }

    public void startGame() { gameTimer.start(); }

    // ==========================================================
    // UC-09: LƯU TIẾN TRÌNH (Sửa lỗi trùng hàm và sai biến ở đây)
    // ==========================================================
    public void saveGame() {
        gameTimer.stop(); // Tạm dừng Timer để dữ liệu đứng yên khi lưu

        // Đóng gói dữ liệu hiện tại vào đối tượng SaveData
        GameSaveData data = new GameSaveData(
                model.getScore(),
                model.getTimer(),
                model.getCards()
        );

        // Gọi PersistenceManager (hàm static) để ghi file
        boolean success = PersistenceManager.saveGame("savegame.dat", data);

        if (success) {
            JOptionPane.showMessageDialog(view, "Đã lưu ván chơi thành công!");
        } else {
            JOptionPane.showMessageDialog(view, "Lỗi: Không thể lưu file!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

        gameTimer.start(); // Chạy tiếp Timer sau khi lưu xong
    }

    // ==========================================================
    // UC-12: TẢI TIẾN TRÌNH (Load Game)
    // ==========================================================
    public void loadGame() {
        GameSaveData data = PersistenceManager.loadGame("savegame.dat");
        if (data != null) {
            // Nạp lại dữ liệu cũ vào Model hiện tại
            model.setScore(data.score);
            model.setTimer(data.timer);
            model.setCards(data.cards);

            // Cập nhật lại toàn bộ giao diện (View)
            view.setupBoard(model.getCards(), 4, 4, this);
            view.updateScoreDisplay(model.getScore());
            view.updateTimerDisplay(model.getTimer());

            JOptionPane.showMessageDialog(view, "Đã khôi phục ván chơi cũ!");
        }
    }

    // UC-04: Xử lý khi nhấn lật thẻ
    public void onCardClick(Card card) {
        if (boardLocked || card.isMatched() || card == model.getFirstSelected()) return;

        if (model.getFirstSelected() == null) {
            card.setState(CardState.FACE_UP);
            model.setFirstSelected(card);
            view.repaintCard(card);
        } else {
            card.setState(CardState.FACE_UP);
            model.setSecondSelected(card);
            view.repaintCard(card);
            boardLocked = true; // Khóa bàn chơi để kiểm tra cặp thẻ (UC-05)
            checkMatch();
        }
    }

    // UC-05: Kiểm tra cặp thẻ trùng
    private void checkMatch() {
        Card c1 = model.getFirstSelected();
        Card c2 = model.getSecondSelected();

        if (c1.getValue().equals(c2.getValue())) {
            c1.setState(CardState.MATCHED);
            c2.setState(CardState.MATCHED);
            model.updateScore(10); // Logic UC6: Cộng 10 điểm
            model.incrementMoves();
            view.updateScoreDisplay(model.getScore());
            model.resetTurn();
            boardLocked = false;
            // Kiểm tra thắng game ở đây...
        } else {
            Timer delay = new Timer(1000, e -> {
                c1.setState(CardState.FACE_DOWN);
                c2.setState(CardState.FACE_DOWN);
                view.repaintCard(c1);
                view.repaintCard(c2);
                model.incrementMoves();
                model.resetTurn();
                boardLocked = false;
            });
            delay.setRepeats(false);
            delay.start();
        }
    }

    private void stopGame(String msg) {
        gameTimer.stop();
        view.showGameOver(model.getScore(), model.getMovesCount());
    }
}