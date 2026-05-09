package memorygame.controller;

import memorygame.model.GameEngine;
import memorygame.persistence.SaveGameService;

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
                    "Khong co du lieu",
                    "Thong bao",
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
                    "Tai tien trinh that bai: " + ex.getMessage(),
                    "Loi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public static void openGameWindow(GameEngine engine, Path saveFile) {
        JFrame gameFrame = new JFrame("Memory Game");
        gameFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        gameFrame.setSize(700, 500);
        gameFrame.setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        JLabel info = new JLabel(
                "Cards: " + engine.getCards().size()
                        + " | Score: " + engine.getGameState().getScore()
                        + " | Moves: " + engine.getGameState().getMovesCount(),
                SwingConstants.CENTER
        );

        JButton saveExitBtn = new JButton("Luu va thoat");
        saveExitBtn.addActionListener(e -> {
            try {
                engine.saveProgress(saveFile);
                JOptionPane.showMessageDialog(gameFrame, "Da luu tien trinh.");
                gameFrame.dispose();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(
                        gameFrame,
                        "Luu that bai: " + ex.getMessage(),
                        "Loi",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        root.add(info, BorderLayout.CENTER);
        root.add(saveExitBtn, BorderLayout.SOUTH);

        gameFrame.setContentPane(root);
        gameFrame.setVisible(true);
    }
}
