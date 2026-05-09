package memorygame.view;

import memorygame.model.Card;

import javax.swing.*;

public class GameBoardPanel extends JPanel {

    public void repaintCard(Card card) {
        repaint();
    }

    public void showMatchEffect(Card first, Card second) {
    }

    public void showNoMatchEffect(Card first, Card second) {
    }

    public void updateScoreDisplay(int score) {
    }

    public void showGameOver(int score, int moves) {
        JOptionPane.showMessageDialog(this,
                "Chúc mừng! Bạn đã hoàn thành!\nĐiểm: " + score + " | Số lượt: " + moves,
                "Kết thúc trò chơi",
                JOptionPane.INFORMATION_MESSAGE);
    }
}