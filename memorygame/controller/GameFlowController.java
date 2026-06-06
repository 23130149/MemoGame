package memorygame.controller;

import memorygame.MyMain;
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

    private static final String FONT_FAMILY = "Segoe UI";
    private static final Color BG_COLOR = new Color(0x1A1A2E);
    private static final Color BG_CARD = new Color(0x16213E);

    public static void openGameWindow(GameEngine engine, Path saveFile) {
        JFrame gameFrame = new JFrame("Memory Game - " + engine.getSession().getLevel().getLevelName());
        gameFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        gameFrame.setSize(1100, 750);
        gameFrame.setMinimumSize(new Dimension(1000, 700));
        gameFrame.setLocationRelativeTo(null);

        // ===== TOP PANEL: THONG TIN GAME =====
        JPanel topPanel = new JPanel(new GridLayout(1, 3, 12, 0));
        topPanel.setBackground(BG_COLOR);
        topPanel.setBorder(BorderFactory.createEmptyBorder(12, 20, 8, 20));

        // Label diem, luot, cap
        JLabel infoLabel = new JLabel();
        infoLabel.setForeground(Color.WHITE);
        infoLabel.setFont(new Font(FONT_FAMILY, Font.BOLD, 14));
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infoLabel.setOpaque(true);
        infoLabel.setBackground(BG_CARD);
        infoLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x0F3460), 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        updateInfoLabel(infoLabel, engine);
        topPanel.add(infoLabel);

        // Label thoi gian (CENTER)
        JLabel timeLabel = new JLabel();
        timeLabel.setForeground(Color.WHITE);
        timeLabel.setFont(new Font(FONT_FAMILY, Font.BOLD, 15));
        timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timeLabel.setOpaque(true);
        timeLabel.setBackground(BG_CARD);
        timeLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x0F3460), 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        updateTimeLabel(timeLabel, engine);
        topPanel.add(timeLabel);

        // Label goi y
        JLabel hintLabel = new JLabel();
        hintLabel.setForeground(Color.WHITE);
        hintLabel.setFont(new Font(FONT_FAMILY, Font.BOLD, 14));
        hintLabel.setHorizontalAlignment(SwingConstants.CENTER);
        hintLabel.setOpaque(true);
        hintLabel.setBackground(BG_CARD);
        hintLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x0F3460), 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        updateHintLabel(hintLabel, engine);
        topPanel.add(hintLabel);

        // ===== BOARD PANEL =====
        int rows = engine.getSession().getLevel().getGridRows();
        int cols = engine.getSession().getLevel().getGridCols();
        GameBoardPanel boardPanel = new GameBoardPanel(rows, cols);

        // ===== GAME CONTROLLER =====
        GameController gameController = new GameController(engine, boardPanel);

        // ===== BOTTOM PANEL: NUT DIEU KHIEN =====
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        bottomPanel.setBackground(BG_COLOR);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 14, 20));

        // Nut Goi y
        int initialHints = engine.getGameState().getHintCount();
        JButton hintBtn = createGameButton("Gợi ý (" + initialHints + ")", new Color(0xFF9800));
        // Disable ngay neu ban dau da het hint (vi du: load save da dung het)
        if (initialHints <= 0) {
            hintBtn.setEnabled(false);
        }

        // Callback: khi GameController goi updateHintDisplay() -> cap nhat text nut
        boardPanel.setOnHintCountChanged(remaining -> {
            hintBtn.setText("Gợi ý (" + remaining + ")");
            updateHintLabel(hintLabel, engine);
            if (remaining <= 0) {
                hintBtn.setEnabled(false);
            }
        });

        // Callback: khi hint animation ket thuc -> re-enable nut (neu con hint)
        boardPanel.setOnHintAnimationDone(() -> {
            if (engine.getGameState().getHintCount() > 0) {
                hintBtn.setEnabled(true);
            }
        });

        hintBtn.addActionListener(e -> {
            // Disable ngay de chan spam click trong khi hint dang hien thi
            hintBtn.setEnabled(false);
            boolean hintStarted = gameController.onHintClick();
            if (!hintStarted) {
                // Hint bi reject (board locked, het hint, hoac khong tim thay cap)
                // -> re-enable neu con hint
                if (engine.getGameState().getHintCount() > 0) {
                    hintBtn.setEnabled(true);
                }
            }
            // Neu hintStarted = true -> nut giu disabled cho den onHintAnimationDone
        });

        // Nut Choi lai
        JButton resetBtn = createGameButton("Chơi lại", new Color(0x2196F3));

        // Nut Luu va thoat
        JButton saveExitBtn = createGameButton("Lưu và thoát", new Color(0x4CAF50));

        // Nut Ve menu
        JButton menuBtn = createGameButton("Về menu", new Color(0x607D8B));

        bottomPanel.add(hintBtn);
        bottomPanel.add(resetBtn);
        bottomPanel.add(saveExitBtn);
        bottomPanel.add(menuBtn);

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
                        "Hết giờ! Bạn thua.\nĐiểm: " + engine.getGameState().getScore() +
                                " | Lượt: " + engine.getGameState().getMovesCount(),
                        "Kết thúc trò chơi",
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
            hintBtn.setText("Gợi ý (" + engine.getGameState().getHintCount() + ")");
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
                            "Hết giờ! Bạn thua.\nĐiểm: " + engine.getGameState().getScore() +
                                    " | Lượt: " + engine.getGameState().getMovesCount(),
                            "Kết thúc trò chơi",
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

        // ===== NUT VE MENU (voi logic stop timer) =====
        menuBtn.addActionListener(e -> {
            if (countdownTimer[0] != null) {
                countdownTimer[0].stop();
                countdownTimer[0] = null;
            }
            gameFrame.dispose();
            MyMain.showMainMenu();
        });

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_COLOR);
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
            label.setText(String.format("<html><span style='color: #FF5252'>Thời gian: %d giây</span></html>",
                    timeLeft));
        } else {
            label.setText(String.format("Thời gian: %d giây", timeLeft));
        }
    }

    private static void updateHintLabel(JLabel label, GameEngine engine) {
        label.setText("Gợi ý còn lại: " + engine.getGameState().getHintCount());
    }

    private static JButton createGameButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font(FONT_FAMILY, Font.BOLD, 13));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(150, 42));
        return btn;
    }
}
