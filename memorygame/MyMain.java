package memorygame;

import memorygame.controller.GameFlowController;
import memorygame.model.GameEngine;
import memorygame.view.LevelSelectionPanel;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Entry point - man hinh chon cap do + nut tiep tuc.
 */
public class MyMain {

    private static final Path SAVE_FILE = Paths.get("memorygame_save.dat");

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MyMain::showLevelSelection);
    }

    public static void showLevelSelection() {
        JFrame frame = new JFrame("Memory Game - Chon Cap Do");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(760, 520);
        frame.setMinimumSize(new Dimension(600, 420));
        frame.setLocationRelativeTo(null);

        int playerId = 1;

        LevelSelectionPanel panel = new LevelSelectionPanel(playerId, session -> {
            GameEngine engine = new GameEngine();
            if (!engine.initBoard(session)) {
                JOptionPane.showMessageDialog(
                        frame,
                        "Khong the tao van choi moi.",
                        "Loi",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            frame.dispose();
            GameFlowController.openGameWindow(engine, SAVE_FILE);
        });

        JButton continueBtn = new JButton("Tiếp tục");
        continueBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        continueBtn.setPreferredSize(new Dimension(140, 40));
        continueBtn.addActionListener(e ->
                GameFlowController.continueGame(frame, SAVE_FILE, engine -> {
                    frame.dispose();
                    GameFlowController.openGameWindow(engine, SAVE_FILE);
                })
        );

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        bottom.add(continueBtn);

        JPanel root = new JPanel(new BorderLayout());
        root.add(panel, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        frame.setContentPane(root);
        frame.setVisible(true);
    }
}