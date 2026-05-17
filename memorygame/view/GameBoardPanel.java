package memorygame.view;

import memorygame.controller.GameController;
import memorygame.model.Card;
import memorygame.model.GameModel;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class GameBoardPanel extends JPanel {

    private GameController controller;
    private final GameModel model;

    // UI Components - Info Panel
    private JLabel timerLabel;
    private JLabel scoreLabel;

    // UI Components - Board
    private JPanel boardPanel;
    private Map<Card, JButton> cardButtonMap;

    public GameBoardPanel(GameModel model) {
        this.model = model;
        this.cardButtonMap = new HashMap<>();
        setLayout(new BorderLayout());
        initInfoPanel();
        initBoardPanel();
    }

    public void setController(GameController controller) {
        this.controller = controller;
    }

    // ===== KHỞI TẠO GIAO DIỆN =====

    private void initInfoPanel() {
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 10));

        timerLabel = new JLabel("⏱ 60");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 20));

        scoreLabel = new JLabel("⭐ 0");
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 20));

        // Chỉ có nút Menu
        JButton menuBtn = new JButton("Menu");
        menuBtn.addActionListener(e -> controller.backToMenu());

        infoPanel.add(timerLabel);
        infoPanel.add(scoreLabel);
        infoPanel.add(menuBtn);

        add(infoPanel, BorderLayout.NORTH);
    }

    private void initBoardPanel() {
        boardPanel = new JPanel();
        add(boardPanel, BorderLayout.CENTER);
    }

    // ===== SETUP BÀN CHƠI =====

    public void setupBoard(int rows, int cols) {
        boardPanel.removeAll();
        cardButtonMap.clear();
        boardPanel.setLayout(new GridLayout(rows, cols, 8, 8));

        for (Card card : model.getCards()) {
            JButton btn = createCardButton(card);
            cardButtonMap.put(card, btn);
            boardPanel.add(btn);
        }

        boardPanel.revalidate();
        boardPanel.repaint();
    }

    private JButton createCardButton(Card card) {
        JButton btn = new JButton("?");
        btn.setFont(new Font("Arial", Font.BOLD, 24));
        btn.setBackground(new Color(70, 130, 180));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> controller.onCardClick(card));
        return btn;
    }

    // ===== CÁC METHOD ĐƯỢC GỌI TỪ GAMECONTROLLER =====

    // UC-06: Cập nhật hiển thị thời gian
    public void updateTimerDisplay(int seconds) {
        timerLabel.setText("⏱ " + seconds);
    }

    // UC-06: Cảnh báo khi còn <= 10 giây
    public void showTimerWarning() {
        timerLabel.setForeground(Color.RED);
    }

    // UC-06: Cập nhật hiển thị điểm số
    public void updateScoreDisplay(int score) {
        scoreLabel.setText("⭐ " + score);
    }

    // UC-04: Repaint thẻ theo trạng thái hiện tại
    public void repaintCard(Card card) {
        JButton btn = cardButtonMap.get(card);
        if (btn == null) return;

        switch (card.getState()) {
            case FACE_UP -> {
                btn.setText(card.getValue());
                btn.setBackground(new Color(255, 255, 255));
                btn.setForeground(Color.BLACK);
            }
            case FACE_DOWN -> {
                btn.setText("?");
                btn.setBackground(new Color(70, 130, 180));
                btn.setForeground(Color.WHITE);
            }
            case MATCHED -> {
                btn.setText(card.getValue());
                btn.setBackground(new Color(60, 179, 113));
                btn.setForeground(Color.WHITE);
                btn.setEnabled(false);
            }
        }
    }

    // UC-05: Hiệu ứng khi ghép đúng
    public void showMatchEffect(Card c1, Card c2) {
        repaintCard(c1);
        repaintCard(c2);
    }

    // UC-06: Khóa toàn bộ bàn chơi
    public void lockBoard() {
        for (JButton btn : cardButtonMap.values()) {
            btn.setEnabled(false);
        }
    }

    // UC-06: Hiển thị kết quả khi game over
    public void showGameOver(int score, int moves) {
        JOptionPane.showMessageDialog(
                this,
                "Kết quả:\n⭐ Điểm: " + score + "\n🔄 Số lượt: " + moves,
                "Kết thúc",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}