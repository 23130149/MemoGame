package memorygame;

import memorygame.controller.GameFlowController;
import memorygame.model.GameEngine;
import memorygame.view.LevelSelectionPanel;
import memorygame.view.MainMenuPanel;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MyMain {

    private static final Path SAVE_FILE = Paths.get("memorygame_save.dat");
    private static final String APP_TITLE = "Memory Card Game";

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(MyMain::showMainMenu);
    }

    public static void showMainMenu() {
        JFrame frame = createAppFrame(APP_TITLE, 760, 520);

        MainMenuPanel menuPanel = new MainMenuPanel();

        menuPanel.setOnStartGame(() -> {
            frame.dispose();
            showLevelSelection();
        });

        menuPanel.setOnContinueGame(() ->
                GameFlowController.continueGame(frame, SAVE_FILE, engine -> {
                    frame.dispose();
                    GameFlowController.openGameWindow(engine, SAVE_FILE);
                })
        );

        menuPanel.setOnExitGame(() -> {
            frame.dispose();
            System.exit(0);
        });

        frame.setContentPane(menuPanel);
        frame.setVisible(true);
    }

    public static void showLevelSelection() {
        JFrame frame = createAppFrame(APP_TITLE + " - Chọn Cấp Độ", 760, 520);

        int playerId = 1;

        LevelSelectionPanel panel = new LevelSelectionPanel(playerId, session -> {
            GameEngine engine = new GameEngine();
            if (!engine.initBoard(session)) {
                JOptionPane.showMessageDialog(
                        frame,
                        "Không thể tạo ván chơi mới.",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            frame.dispose();
            GameFlowController.openGameWindow(engine, SAVE_FILE);
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottom.setBackground(new Color(0x1A1A2E));

        JButton continueBtn = createStyledButton("Tiếp tục", new Color(0x4CAF50));
        continueBtn.addActionListener(e ->
                GameFlowController.continueGame(frame, SAVE_FILE, engine -> {
                    frame.dispose();
                    GameFlowController.openGameWindow(engine, SAVE_FILE);
                })
        );

        JButton backBtn = createStyledButton("Menu chính", new Color(0x607D8B));
        backBtn.addActionListener(e -> {
            frame.dispose();
            showMainMenu();
        });

        bottom.add(continueBtn);
        bottom.add(backBtn);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(0x1A1A2E));
        root.add(panel, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        frame.setContentPane(root);
        frame.setVisible(true);
    }

    private static JFrame createAppFrame(String title, int width, int height) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(width, height);
        frame.setMinimumSize(new Dimension(600, 420));
        frame.setLocationRelativeTo(null);
        return frame;
    }

    private static JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 40));
        return btn;
    }
}