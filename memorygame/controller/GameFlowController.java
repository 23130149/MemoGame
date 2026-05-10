package memorygame.controller;

import memorygame.model.GameEngine;
import memorygame.persistence.SaveGameService;
import memorygame.view.GameBoardPanel;

import javax.swing.*;
import java.awt.*;
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

        // Top panel: Thông tin
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(0x1A1A2E));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel infoLabel = new JLabel();
        infoLabel.setForeground(Color.WHITE);
        infoLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        updateInfoLabel(infoLabel, engine);
        topPanel.add(infoLabel, BorderLayout.WEST);

        // Board panel
        int rows = engine.getSession().getLevel().getGridRows();
        int cols = engine.getSession().getLevel().getGridCols();
        GameBoardPanel boardPanel = new GameBoardPanel(rows, cols);

        // Game controller
        GameController gameController = new GameController(engine, boardPanel);

        // Bottom panel: Buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottomPanel.setBackground(new Color(0x1A1A2E));

        JButton saveExitBtn = new JButton("Lưu và thoát");
        saveExitBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        saveExitBtn.setPreferredSize(new Dimension(130, 40));
        saveExitBtn.addActionListener(e -> {
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
            }
        });

        JButton resetBtn = new JButton("Chơi lại");
        resetBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        resetBtn.setPreferredSize(new Dimension(130, 40));
        resetBtn.addActionListener(e -> {
            // Reset game
            engine.initBoard(engine.getSession());
            boardPanel.initializeBoard(engine.getCards());
            updateInfoLabel(infoLabel, engine);
        });

        bottomPanel.add(resetBtn);
        bottomPanel.add(saveExitBtn);

        // Root panel
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
}
