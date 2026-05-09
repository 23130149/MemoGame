package memorygame.controller;

import memorygame.model.*;
import memorygame.view.GameFrame;
import javax.swing.*;
import java.io.*;

public class GameController {
    private GameModel model;
    private GameFrame view;
    private Timer gameTimer;

    public GameController(GameModel model, GameFrame view) {
        this.model = model;
        this.view = view;

        // BƯỚC QUAN TRỌNG: Phải gọi hàm khởi tạo Timer ở đây
        initTimer();

        // UC6: Đăng ký sự kiện Test cộng điểm
        this.view.addTestMatchListener(e -> {
            model.updateScore(10);
            this.view.updateUI(model.getScore(), model.getTimer());
        });

        // UC9: Đăng ký sự kiện Lưu game
        this.view.addSaveListener(e -> saveGame());
    }

    // Tách hàm initTimer ra ngoài Constructor
    private void initTimer() {
        // Bước 1 trong Sequence: Timer gửi tín hiệu tick mỗi 1000ms
        gameTimer = new Timer(1000, e -> {

            // Bước 2: Yêu cầu Model giảm giá trị (timer - 1)
            int newTime = model.getTimer() - 1;
            model.setTimer(newTime);

            // Bước 3 & 4: Cập nhật UI dựa trên dữ liệu mới nhất từ Model
            view.updateUI(model.getScore(), model.getTimer());

            // Bước 5 - 9: Kiểm tra kết thúc
            if (model.getTimer() <= 10 && model.getTimer() > 0) {
                System.out.println("Cảnh báo: Sắp hết giờ!");
            } else if (model.getTimer() <= 0) {
                gameTimer.stop();
                JOptionPane.showMessageDialog(view, "Hết giờ!");
            }
        });
    }

    public void startGame() {
        if (gameTimer != null) {
            gameTimer.start();
        }
    }

    private void saveGame() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("savegame.dat"))) {
            gameTimer.stop(); // Tạm dừng Timer theo đúng Sequence UC9
            oos.writeObject(model.getScore());
            oos.writeObject(model.getTimer());
            oos.writeObject(model.getCards());
            JOptionPane.showMessageDialog(view, "UC9: Đã lưu tiến trình!");
            gameTimer.start(); // Chạy lại sau khi lưu xong
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(view, "Lỗi khi lưu game!");
        }
    }
}