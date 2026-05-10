package memorygame.view;

import memorygame.controller.GameController;
import memorygame.model.Card;
import memorygame.model.CardState;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameBoardPanel extends JPanel {
    private JLabel lblScore, lblTimer;
    private JPanel gridPanel;
    private JButton btnSave, btnLoad; // Thêm 2 nút bấm ở đây
    private Map<Card, JButton> cardButtons = new HashMap<>();

    public GameBoardPanel() {
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        // Tầng trên: Score và Timer
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        lblScore = new JLabel("Score: 0");
        lblTimer = new JLabel("Time: 60s");
        lblScore.setFont(new Font("Arial", Font.BOLD, 18));
        lblTimer.setFont(new Font("Arial", Font.BOLD, 18));
        infoPanel.add(lblScore);
        infoPanel.add(lblTimer);
        add(infoPanel, BorderLayout.NORTH);

        // Tầng giữa: Lưới thẻ bài
        gridPanel = new JPanel();
        add(gridPanel, BorderLayout.CENTER);

        // --- TẦNG DƯỚI: NÚT ĐIỀU KHIỂN (UC-09) ---
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnSave = new JButton("Lưu Game");
        btnLoad = new JButton("Tải Game Cũ");

        // Trang trí nút cho đẹp chút
        btnSave.setBackground(new Color(70, 130, 180));
        btnSave.setForeground(Color.WHITE);
        btnLoad.setBackground(new Color(60, 179, 113));
        btnLoad.setForeground(Color.WHITE);

        controlPanel.add(btnSave);
        controlPanel.add(btnLoad);
        add(controlPanel, BorderLayout.SOUTH);
    }

    // Hàm để Controller gắn sự kiện vào nút (Gửi từ Main)
    public void setControlListeners(GameController controller) {
        btnSave.addActionListener(e -> controller.saveGame()); // Kích hoạt UC-09
        btnLoad.addActionListener(e -> controller.loadGame()); // Kích hoạt UC-12
    }

    public void setupBoard(List<Card> cards, int rows, int cols, GameController controller) {
        gridPanel.removeAll();
        gridPanel.setLayout(new GridLayout(rows, cols, 8, 8));
        cardButtons.clear();

        for (Card card : cards) {
            JButton btn = new JButton("?");
            btn.setPreferredSize(new Dimension(80, 110));
            btn.setFont(new Font("Arial", Font.BOLD, 20));
            btn.addActionListener(e -> controller.onCardClick(card));
            cardButtons.put(card, btn);
            gridPanel.add(btn);
            repaintCard(card);
        }
        revalidate();
        repaint();
    }

    public void repaintCard(Card card) {
        JButton btn = cardButtons.get(card);
        if (btn == null) return;
        if (card.isMatched()) {
            btn.setText("✓");
            btn.setBackground(Color.GREEN);
            btn.setEnabled(false);
        } else if (card.getState() == CardState.FACE_UP) {
            btn.setText(card.getValue());
            btn.setBackground(Color.WHITE);
        } else {
            btn.setText("?");
            btn.setBackground(null);
            btn.setEnabled(true);
        }
    }

    public void updateScoreDisplay(int score) { lblScore.setText("Score: " + score); }
    public void updateTimerDisplay(int sec) { lblTimer.setText("Time: " + sec + "s"); }
    public void showGameOver(int s, int m) {
        JOptionPane.showMessageDialog(this, "KẾT THÚC!\nĐiểm: " + s + "\nSố lượt đi: " + m);
    }
}