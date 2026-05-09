package memorygame.view;

import memorygame.model.Card;

import javax.swing.*;

public class GameBoardPanel extends JPanel {

    public void repaintCard(Card card) {
        repaint();
    }

    public void showMatchEffect(Card first, Card second) {
        // TODO: Animation khi match (fade out, scale, v.v.)
    }

    public void showNoMatchEffect(Card first, Card second) {
        // TODO: Animation khi no-match (shake, flip effect, v.v.)
    }

    public void updateScoreDisplay(int score) {
        // TODO: Cập nhật label điểm trên UI
    }

    public void showGameOver(int score, int moves) {
        JOptionPane.showMessageDialog(this,
                "Chúc mừng! Bạn đã hoàn thành!\nĐiểm: " + score + " | Số lượt: " + moves,
                "Kết thúc trò chơi",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // ===== HINT UI METHODS =====

    public void showHintEffect(Card cardX, Card cardY) {
        // TODO: Animation hiệu ứng gợi ý (highlight, glow, border, v.v.)
        // Ví dụ: highlight 2 thẻ bằng màu vàng
        System.out.println("Hint: Showing " + cardX.getValue() + " and " + cardY.getValue());
    }

    public void hideHintEffect(Card cardX, Card cardY) {
        // TODO: Tắt hiệu ứng gợi ý
        System.out.println("Hint: Hiding effects");
    }

    public void updateHintDisplay(int remainingHints) {
        // TODO: Cập nhật label số hint còn lại trên UI
        // Ví dụ: lblHints.setText("Gợi ý: " + remainingHints);
        System.out.println("Remaining hints: " + remainingHints);
    }

    public void showNotify(String message) {
        // TODO: Hiển thị thông báo cho người chơi (Toast, Dialog, Label, v.v.)
        JOptionPane.showMessageDialog(this, message, "Thông báo",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
