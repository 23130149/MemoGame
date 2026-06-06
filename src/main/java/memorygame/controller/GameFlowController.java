package memorygame.controller;

import memorygame.model.GameEngine;
import memorygame.persistence.SaveGameService;
import memorygame.view.GameBoardPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

public final class GameFlowController {

    private GameFlowController() {
    }

    public static void continueGame(JFrame parentFrame, Path saveFile, Consumer<GameEngine> onLoaded) {
        if (!SaveGameService.hasSave(saveFile)) {
            JOptionPane.showMessageDialog(
                    parentFrame,
                    "Không có dữ liệu",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        GameEngine engine = new GameEngine();
        try {
            engine.loadProgress(saveFile);
            if (onLoaded != null) {
                onLoaded.accept(engine);
            }
        } catch (IOException | ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(
                    parentFrame,
                    "Tải tiến trình thất bại: " + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public static void openGameWindow(GameEngine engine, Path saveFile) {
        JFrame gameFrame = new JFrame("Memory Game - " + engine.getSession().getLevel().getLevelName());
        gameFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        gameFrame.setSize(900, 700);
        gameFrame.setLocationRelativeTo(null);

        // ===== TOP PANEL: THÔNG TIN GAME =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(0x1A1A2E));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Label điểm, lượt, cặp
        JLabel infoLabel = new JLabel();
        infoLabel.setForeground(Color.WHITE);
        infoLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        updateInfoLabel(infoLabel, engine);
        topPanel.add(infoLabel, BorderLayout.WEST);

        // Label thời gian (CENTER)
        JLabel timeLabel = new JLabel();
        timeLabel.setForeground(Color.WHITE);
        timeLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        updateTimeLabel(timeLabel, engine);
        topPanel.add(timeLabel, BorderLayout.CENTER);

        // Label gợi ý
        JLabel hintLabel = new JLabel();
        hintLabel.setForeground(Color.WHITE);
        hintLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        updateHintLabel(hintLabel, engine);
        topPanel.add(hintLabel, BorderLayout.EAST);

        // ===== BOARD PANEL =====
        int rows = engine.getSession().getLevel().getGridRows();
        int cols = engine.getSession().getLevel().getGridCols();
        GameBoardPanel boardPanel = new GameBoardPanel(rows, cols);

        // ===== GAME CONTROLLER =====
        GameController gameController = new GameController(engine, boardPanel);

        // ===== BOTTOM PANEL: NÚT ĐIỀU KHIỂN =====
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottomPanel.setBackground(new Color(0x1A1A2E));

        // Nút Gợi ý
        int initialHints = engine.getGameState().getHintCount();
        JButton hintBtn = new JButton("💡 Gợi ý (" + initialHints + ")");
        hintBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        hintBtn.setPreferredSize(new Dimension(130, 40));
        // Disable ngay nếu ban đầu đã hết hint (ví dụ: load save đã dùng hết)
        if (initialHints <= 0) {
            hintBtn.setEnabled(false);
        }

        // Callback: khi GameController gọi updateHintDisplay() → cập nhật text nút
        boardPanel.setOnHintCountChanged(remaining -> {
            hintBtn.setText("💡 Gợi ý (" + remaining + ")");
            updateHintLabel(hintLabel, engine);
            if (remaining <= 0) {
                hintBtn.setEnabled(false);
            }
        });

        // Callback: khi hint animation kết thúc → re-enable nút (nếu còn hint)
        boardPanel.setOnHintAnimationDone(() -> {
            if (engine.getGameState().getHintCount() > 0) {
                hintBtn.setEnabled(true);
            }
        });

        hintBtn.addActionListener(e -> {
            // Disable ngay để chặn spam click trong khi hint đang hiển thị
            hintBtn.setEnabled(false);
            boolean hintStarted = gameController.onHintClick();
            if (!hintStarted) {
                // Hint bị reject (board locked, hết hint, hoặc không tìm thấy cặp)
                // → re-enable nếu còn hint
                if (engine.getGameState().getHintCount() > 0) {
                    hintBtn.setEnabled(true);
                }
            }
            // Nếu hintStarted = true → nút giữ disabled cho đến onHintAnimationDone
        });

        // Nút Chơi lại
        JButton resetBtn = new JButton("Chơi lại");
        resetBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        resetBtn.setPreferredSize(new Dimension(130, 40));

        // Nút Lưu và thoát
        JButton saveExitBtn = new JButton("Lưu và thoát");
        saveExitBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        saveExitBtn.setPreferredSize(new Dimension(130, 40));

        bottomPanel.add(hintBtn);
        bottomPanel.add(resetBtn);
        bottomPanel.add(saveExitBtn);

        // ===== COUNTDOWN TIMER =====
        Timer[] countdownTimer = new Timer[1]; // Mảng để có thể thay đổi reference

        countdownTimer[0] = new Timer(1000, e -> {
            engine.getGameState().decrementTimeLeft();
            updateTimeLabel(timeLabel, engine);
            updateInfoLabel(infoLabel, engine);

            // Kiểm tra hết giờ
            if (engine.getGameState().isTimeUp()) {
                countdownTimer[0].stop();
                boardPanel.setBoardLocked(true);

                JOptionPane.showMessageDialog(gameFrame,
                        "⏰ Hết giờ! Bạn thua.\nĐiểm: " + engine.getGameState().getScore() +
                                " | Lượt: " + engine.getGameState().getMovesCount(),
                        "Game Over",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
        countdownTimer[0].setRepeats(true);
        countdownTimer[0].start();

        // ===== STOP TIMER KHI ĐÓNG CỬA SỔ (nút X) =====
        gameFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (countdownTimer[0] != null) {
                    countdownTimer[0].stop();
                    countdownTimer[0] = null;
                }
            }
        });

        // ===== NÚT CHƠI LẠI (với logic reset timer) =====
        resetBtn.addActionListener(e -> {
            if (countdownTimer[0] != null) {
                countdownTimer[0].stop();  // Stop timer cũ
            }

            engine.initBoard(engine.getSession());
            boardPanel.initializeBoard(engine.getCards());
            updateInfoLabel(infoLabel, engine);
            updateTimeLabel(timeLabel, engine);
            updateHintLabel(hintLabel, engine);
            hintBtn.setText("💡 Gợi ý (" + engine.getGameState().getHintCount() + ")");
            hintBtn.setEnabled(engine.getGameState().getHintCount() > 0);

            // Start timer mới
            countdownTimer[0] = new Timer(1000, evt -> {
                engine.getGameState().decrementTimeLeft();
                updateTimeLabel(timeLabel, engine);
                updateInfoLabel(infoLabel, engine);

                if (engine.getGameState().isTimeUp()) {
                    ((Timer) evt.getSource()).stop();
                    boardPanel.setBoardLocked(true);

                    JOptionPane.showMessageDialog(gameFrame,
                            "⏰ Hết giờ! Bạn thua.\nĐiểm: " + engine.getGameState().getScore() +
                                    " | Lượt: " + engine.getGameState().getMovesCount(),
                            "Game Over",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            });
            countdownTimer[0].setRepeats(true);
            countdownTimer[0].start();
        });

        // ===== NÚT LƯU VÀ THOÁT (với logic stop timer) =====
        saveExitBtn.addActionListener(e -> {
            if (countdownTimer[0] != null) {
                countdownTimer[0].stop();  // Stop timer khi lưu
            }

            try {
                engine.saveProgress(saveFile);
                JOptionPane.showMessageDialog(gameFrame, "Đã lưu tiến trình.");
                gameFrame.dispose();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(
                        gameFrame,
                        "Lưu thất bại: " + ex.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
                if (countdownTimer[0] != null) {
                    countdownTimer[0].start();  // Tiếp tục timer nếu lưu thất bại
                }
            }
        });

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(0x1A1A2E));
        root.add(topPanel, BorderLayout.NORTH);
        root.add(boardPanel, BorderLayout.CENTER);
        root.add(bottomPanel, BorderLayout.SOUTH);

        gameFrame.setContentPane(root);
        gameFrame.setVisible(true);
    }

    private static void updateInfoLabel(JLabel label, GameEngine engine) {
        label.setText(String.format(
                "Điểm: %d | Lượt: %d | Cặp còn lại: %d",
                engine.getGameState().getScore(),
                engine.getGameState().getMovesCount(),
                engine.getGameState().getRemainingPairs()
        ));
    }

    private static void updateTimeLabel(JLabel label, GameEngine engine) {
        int timeLeft = engine.getGameState().getTimeLeftSec();

        if (timeLeft <= 10) {
            // Đỏ khi dưới 10 giây
            label.setText(String.format("<html><span style='color: #FF5252'>⏱ Thời gian: %d giây</span></html>",
                    timeLeft));
        } else {
            // Trắng bình thường
            label.setText(String.format("⏱ Thời gian: %d giây", timeLeft));
        }
    }

    private static void updateHintLabel(JLabel label, GameEngine engine) {
        label.setText("Gợi ý còn lại: " + engine.getGameState().getHintCount());
    }
}
