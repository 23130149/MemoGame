package memorygame;

import memorygame.controller.GameFlowController;
import memorygame.model.GameEngine;
import memorygame.model.PlayerProfile;
import memorygame.persistence.PlayerProfileStore;
import memorygame.view.LevelSelectionPanel;
import memorygame.view.MainMenuPanel;
import memorygame.view.ShopPanel;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MyMain {

    private static final Path SAVE_FILE = Paths.get("memorygame_save.dat");
    private static final PlayerProfile PLAYER_PROFILE = new PlayerProfile();
    private static final String APP_TITLE = "Memory Card Game";

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        // UC-15 - Le VietKhanh: Tải vàng/vật phẩm đã mua trước khi mở menu.
        PlayerProfileStore.loadDefault(PLAYER_PROFILE);
        SwingUtilities.invokeLater(MyMain::showMainMenu);
    }

    public static void showMainMenu() {
        JFrame frame = createAppFrame(APP_TITLE, 1100, 750);

        MainMenuPanel menuPanel = new MainMenuPanel();

        menuPanel.setOnStartGame(() -> {
            frame.dispose();
            showLevelSelection();
        });

        menuPanel.setOnContinueGame(() ->
                GameFlowController.continueGame(frame, SAVE_FILE, PLAYER_PROFILE, engine -> {
                    frame.dispose();
                    GameFlowController.openGameWindow(engine, SAVE_FILE, PLAYER_PROFILE, MyMain::showMainMenu);
                })
        );

        menuPanel.setOnOpenShop(() -> {
            frame.dispose();
            showShop();
        });

        menuPanel.setOnExitGame(() -> {
            PlayerProfileStore.saveDefault(PLAYER_PROFILE);
            frame.dispose();
            System.exit(0);
        });

        frame.setContentPane(menuPanel);
        frame.setVisible(true);
    }

    public static void showLevelSelection() {
        JFrame frame = createAppFrame(APP_TITLE + " - Chọn Cấp Độ", 1100, 750);

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
            GameFlowController.openGameWindow(engine, SAVE_FILE, PLAYER_PROFILE, MyMain::showLevelSelection);
        }, PLAYER_PROFILE.getGold());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottom.setBackground(new Color(0x1A1A2E));

        JButton continueBtn = createStyledButton("Tiếp tục", new Color(0x4CAF50));
        continueBtn.addActionListener(e ->
                GameFlowController.continueGame(frame, SAVE_FILE, PLAYER_PROFILE, engine -> {
                    frame.dispose();
                    GameFlowController.openGameWindow(engine, SAVE_FILE, PLAYER_PROFILE, MyMain::showLevelSelection);
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


    public static void showShop() {
        JFrame frame = createAppFrame(APP_TITLE + " - Cửa hàng", 760, 620);

        ShopPanel shopPanel = new ShopPanel(PLAYER_PROFILE, () -> {
            frame.dispose();
            showMainMenu();
        });

        frame.setContentPane(shopPanel);
        frame.setVisible(true);
    }

    private static JFrame createAppFrame(String title, int width, int height) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(width, height);
        frame.setMinimumSize(new Dimension(1000, 700));
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
        btn.setPreferredSize(new Dimension(160, 44));
        return btn;
    }
}