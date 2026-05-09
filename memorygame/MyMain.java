package memorygame;

import memorygame.view.LevelSelectionPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Entry point – mở màn hình chọn cấp độ (UC-01).
 */
public class MyMain {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MyMain::showLevelSelection);
    }

    public static void showLevelSelection() {
        JFrame frame = new JFrame("Memory Game – Chọn Cấp Độ");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(760, 480);
        frame.setMinimumSize(new Dimension(600, 400));
        frame.setLocationRelativeTo(null);

        int playerId = 1;

        LevelSelectionPanel panel = new LevelSelectionPanel(playerId, () -> {
            JOptionPane.showMessageDialog(frame,
                    "Bắt đầu trò chơi!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            frame.dispose();
        });

        frame.setContentPane(panel);
        frame.setVisible(true);
    }
}