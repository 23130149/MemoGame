package memorygame.controller;

import memorygame.model.GameEngine;
import memorygame.model.PlayerProfile;
import memorygame.model.RewardCalculator;
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
        continueGame(parentFrame, saveFile, null, onLoaded);
    }

    public static void continueGame(
            JFrame parentFrame,
            Path saveFile,
            PlayerProfile runtimeProfile,
            Consumer<GameEngine> onLoaded
    ) {
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

            if (runtimeProfile != null && engine.getPlayerProfile() != null) {
                runtimeProfile.copyFrom(engine.getPlayerProfile());
            }

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
        openGameWindow(engine, saveFile, null, null);
    }

    public static void openGameWindow(GameEngine engine, Path saveFile, PlayerProfile playerProfile) {
        openGameWindow(engine, saveFile, playerProfile, null);
    }

    public static void openGameWindow(
            GameEngine engine,
            Path saveFile,
            PlayerProfile playerProfile,
            Runnable onBackToMenu
    ) {
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

        // Góc phải: vàng + gợi ý
        JPanel rightInfoPanel = new JPanel();
        rightInfoPanel.setOpaque(false);
        rightInfoPanel.setLayout(new BoxLayout(rightInfoPanel, BoxLayout.Y_AXIS));

        JLabel goldLabel = new JLabel();
        goldLabel.setForeground(new Color(0xFFD54F));
        goldLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        updateGoldLabel(goldLabel, playerProfile);

        JLabel hintLabel = new JLabel();
        hintLabel.setForeground(Color.WHITE);
        hintLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        updateHintLabel(hintLabel, engine);

        rightInfoPanel.add(goldLabel);
        rightInfoPanel.add(Box.createVerticalStrut(4));
        rightInfoPanel.add(hintLabel);
        topPanel.add(rightInfoPanel, BorderLayout.EAST);

        // ===== BOARD PANEL =====
        int rows = engine.getSession().getLevel().getGridRows();
        int cols = engine.getSession().getLevel().getGridCols();
        GameBoardPanel boardPanel = new GameBoardPanel(rows, cols);

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

        // ===== COUNTDOWN TIMER =====
        Timer[] countdownTimer = new Timer[1]; // Mảng để có thể thay đổi reference

        // ===== GAME CONTROLLER =====
        GameController gameController = new GameController(
                engine,
                boardPanel,
                playerProfile,
                (score, moves, rewardGold, totalGold) -> {
                    if (countdownTimer[0] != null) {
                        countdownTimer[0].stop();
                    }
                    boardPanel.setBoardLocked(true);
                    updateGoldLabel(goldLabel, playerProfile);

                    String message = "Chúc mừng! Bạn đã hoàn thành!\n"
                            + "Điểm: " + score + " | Số lượt: " + moves + "\n"
                            + "Bạn nhận được: " + rewardGold + " vàng (điểm/10).\n"
                            + "Tổng vàng: " + totalGold;

                    if (onBackToMenu != null) {
                        Object[] options = {"Quay lại Menu", "Đóng"};
                        int choice = JOptionPane.showOptionDialog(
                                gameFrame,
                                message,
                                "Kết thúc trò chơi",
                                JOptionPane.DEFAULT_OPTION,
                                JOptionPane.INFORMATION_MESSAGE,
                                null,
                                options,
                                options[0]
                        );

                        if (choice == 0) {
                            gameFrame.dispose();
                            onBackToMenu.run();
                        }
                    } else {
                        JOptionPane.showMessageDialog(
                                gameFrame,
                                message,
                                "Kết thúc trò chơi",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                }
        );

        // Nút Gợi ý
        JButton hintBtn = new JButton("💡 Gợi ý (" + engine.getGameState().getHintCount() + ")");
        hintBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        hintBtn.setPreferredSize(new Dimension(130, 40));
        hintBtn.addActionListener(e -> {
            gameController.onHintClick();
            updateHintLabel(hintLabel, engine);
            hintBtn.setText("💡 Gợi ý (" + engine.getGameState().getHintCount() + ")");
        });

        bottomPanel.add(hintBtn);
        bottomPanel.add(resetBtn);
        bottomPanel.add(saveExitBtn);

        countdownTimer[0] = new Timer(1000, e -> {
            engine.getGameState().decrementTimeLeft();
            updateTimeLabel(timeLabel, engine);
            updateInfoLabel(infoLabel, engine);

            // Kiểm tra hết giờ
            if (engine.getGameState().isTimeUp()) {
                countdownTimer[0].stop();
                boardPanel.setBoardLocked(true);
                showTimeUpDialog(gameFrame, engine, playerProfile, goldLabel, onBackToMenu);
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
                    showTimeUpDialog(gameFrame, engine, playerProfile, goldLabel, onBackToMenu);
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
                engine.saveProgress(saveFile, playerProfile);
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

    private static void updateGoldLabel(JLabel label, PlayerProfile profile) {
        long gold = (profile == null) ? 0 : profile.getGold();
        label.setText("Vàng: " + gold);
    }

    private static void showTimeUpDialog(
            JFrame gameFrame,
            GameEngine engine,
            PlayerProfile playerProfile,
            JLabel goldLabel,
            Runnable onBackToMenu
    ) {
        int score = engine.getGameState().getScore();
        int moves = engine.getGameState().getMovesCount();
        long rewardGold = RewardCalculator.calculateGoldFromScore(score);
        long totalGold = rewardGold;

        if (playerProfile != null) {
            playerProfile.creditGold(rewardGold);
            totalGold = playerProfile.getGold();
            updateGoldLabel(goldLabel, playerProfile);
        }

        String message = "⏰ Hết giờ! Bạn thua.\n"
                + "Điểm: " + score + " | Lượt: " + moves + "\n"
                + "Bạn nhận được: " + rewardGold + " vàng (điểm/10).\n"
                + "Tổng vàng: " + totalGold;

        if (onBackToMenu != null) {
            Object[] options = {"Quay lại Menu", "Đóng"};
            int choice = JOptionPane.showOptionDialog(
                    gameFrame,
                    message,
                    "Game Over",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            if (choice == 0) {
                gameFrame.dispose();
                onBackToMenu.run();
            }
            return;
        }

        JOptionPane.showMessageDialog(
                gameFrame,
                message,
                "Game Over",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
