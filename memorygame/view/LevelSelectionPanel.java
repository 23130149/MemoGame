package memorygame.view;

import memorygame.controller.LevelSelectionController;
import memorygame.model.DifficultyLevel;
import memorygame.model.GameSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class LevelSelectionPanel extends JPanel
        implements LevelSelectionController.LevelSelectionListener {

    private static final Color[] LEVEL_COLORS = {
            new Color(0x4CAF50),   // Dễ    – xanh lá
            new Color(0xFF9800),   // TB    – cam
            new Color(0xF44336),   // Khó   – đỏ
    };
    private static final Color BG_COLOR       = new Color(0x1A1A2E);
    private static final Color CARD_BG        = new Color(0x16213E);
    private static final Color CARD_SELECTED  = new Color(0x0F3460);
    private static final Color TEXT_PRIMARY   = Color.WHITE;
    private static final Color TEXT_SECONDARY = new Color(0xB0BEC5);

    private final LevelSelectionController controller;
    private final Consumer<GameSession> onGameStart;  // callback khi bắt đầu trò chơi

    private final JPanel levelCardsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
    private final JButton confirmBtn  = new JButton("Xác nhận");
    private final JButton cancelBtn   = new JButton("Trở về");
    private final JLabel  errorLabel  = new JLabel(" ");

    private LevelCard[] levelCards;
    private DifficultyLevel selectedLevel = null;
    private GameSession currentSession    = null;

    public LevelSelectionPanel(int playerId, Consumer<GameSession> onGameStart) {
        this.onGameStart  = onGameStart;
        this.controller   = new LevelSelectionController(playerId, this);

        setBackground(BG_COLOR);
        setLayout(new BorderLayout(0, 24));
        setBorder(new EmptyBorder(40, 60, 40, 60));

        buildUI();
        controller.loadLevelList();
    }

    private void buildUI() {
        JLabel title = new JLabel("Chọn Cấp Độ", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(TEXT_PRIMARY);
        add(title, BorderLayout.NORTH);

        levelCardsPanel.setBackground(BG_COLOR);
        add(levelCardsPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(BG_COLOR);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

        errorLabel.setForeground(new Color(0xFF5252));
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        errorLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        bottomPanel.add(errorLabel);
        bottomPanel.add(Box.createVerticalStrut(12));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnRow.setBackground(BG_COLOR);

        styleButton(cancelBtn, new Color(0x607D8B), TEXT_PRIMARY);
        cancelBtn.addActionListener(e -> controller.cancelSelection(currentSession));

        styleButton(confirmBtn, new Color(0x2196F3), TEXT_PRIMARY);
        // UC-02 - Le VietKhanh: Nút Xác nhận bị khóa ban đầu để bắt buộc người chơi chọn cấp độ trước.
        confirmBtn.setEnabled(false);
        // UC-02 - Le VietKhanh: Khi người chơi bấm Xác nhận, gọi controller kiểm tra và xác nhận GameSession.
        confirmBtn.addActionListener(e -> controller.confirmLevel(currentSession));

        btnRow.add(cancelBtn);
        btnRow.add(confirmBtn);
        bottomPanel.add(btnRow);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    @Override
    public void onLevelListLoaded(List<DifficultyLevel> levels) {
        SwingUtilities.invokeLater(() -> {
            levelCardsPanel.removeAll();
            levelCards = new LevelCard[levels.size()];
            for (int i = 0; i < levels.size(); i++) {
                levelCards[i] = new LevelCard(levels.get(i), LEVEL_COLORS[i]);
                final DifficultyLevel lvl = levels.get(i);
                final int idx = i;
                levelCards[i].addActionListener(e -> onLevelCardClicked(lvl, idx));
                levelCardsPanel.add(levelCards[i]);
            }
            levelCardsPanel.revalidate();
            levelCardsPanel.repaint();
        });
    }

    @Override
    public void onLevelDetailShown(DifficultyLevel level) {
        SwingUtilities.invokeLater(() -> showDetailPopup(level));
    }

    @Override
    public void onLevelConfirmed(GameSession session) {
        SwingUtilities.invokeLater(() -> {
            clearError();
            if (onGameStart != null) onGameStart.accept(session);
        });
    }

    @Override
    public void onSelectionCancelled() {
        SwingUtilities.invokeLater(() -> {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) window.dispose();
        });
    }

    @Override
    public void onError(String errorCode, String message) {
        SwingUtilities.invokeLater(() -> showError(message));
    }

    private void onLevelCardClicked(DifficultyLevel level, int idx) {
        clearError();
        // UC-02 - Le VietKhanh: Ghi nhận cấp độ người chơi vừa chọn trên giao diện.
        selectedLevel  = level;
        // UC-02 - Le VietKhanh: Tạo phiên chơi tạm theo cấp độ đã chọn để chờ bước Xác nhận.
        currentSession = controller.selectLevel(level);

        for (int i = 0; i < levelCards.length; i++) {
            // UC-02 - Le VietKhanh: Chỉ card đang được chọn được highlight, các card còn lại trở về bình thường.
            levelCards[i].setSelected(i == idx);
        }
        // UC-02 - Le VietKhanh: Mở nút Xác nhận sau khi đã chọn cấp độ hợp lệ.
        confirmBtn.setEnabled(true);
    }

    private void showDetailPopup(DifficultyLevel level) {
        String msg = "<html><body style='width:220px; font-size:13px'>"
                + "<b>" + level.getLevelName() + "</b><br><br>"
                + level.getDescription() + "<br><br>"
                + "<b>Lưới:</b> " + level.getGridRows() + "×" + level.getGridCols() + "<br>"
                + "<b>Số cặp:</b> " + level.getTotalPairs() + "<br>"
                + "<b>Thời gian:</b> " + level.getTimeLimitSec() + " giây<br>"
                + "<b>Hệ số điểm:</b> ×" + level.getScoreMultiplier()
                + "</body></html>";
        JOptionPane.showMessageDialog(this, msg,
                "Thông tin cấp độ – " + level.getLevelName(),
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        errorLabel.setText("⚠ " + message);
    }

    private void clearError() {
        errorLabel.setText(" ");
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(130, 40));
    }

    private class LevelCard extends JButton {
        private boolean selected = false;
        private final Color accentColor;
        private final DifficultyLevel level;

        LevelCard(DifficultyLevel level, Color accentColor) {
            this.level       = level;
            this.accentColor = accentColor;

            setLayout(new BorderLayout(0, 8));
            setBackground(CARD_BG);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(accentColor.darker(), 2, true),
                    new EmptyBorder(20, 16, 20, 16)));

            JLabel badge = new JLabel(level.getLevelName(), SwingConstants.CENTER);
            badge.setFont(new Font("SansSerif", Font.BOLD, 20));
            badge.setForeground(accentColor);
            add(badge, BorderLayout.NORTH);

            JPanel info = new JPanel();
            info.setOpaque(false);
            info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

            addInfoRow(info, "Lưới", level.getGridRows() + "×" + level.getGridCols());
            addInfoRow(info, "Cặp thẻ", String.valueOf(level.getTotalPairs()));
            addInfoRow(info, "Thời gian", level.getTimeLimitSec() + "s");
            addInfoRow(info, "Hệ số điểm", "×" + level.getScoreMultiplier());
            add(info, BorderLayout.CENTER);

            JButton infoBtn = new JButton("ℹ Chi tiết");
            infoBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
            infoBtn.setForeground(accentColor);
            infoBtn.setBackground(CARD_BG);
            infoBtn.setBorderPainted(false);
            infoBtn.setFocusPainted(false);
            infoBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            infoBtn.addActionListener(e -> controller.showLevelDetail(level));
            add(infoBtn, BorderLayout.SOUTH);
        }

        private void addInfoRow(JPanel panel, String key, String value) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
            row.setOpaque(false);
            JLabel k = new JLabel(key + ":");
            k.setFont(new Font("SansSerif", Font.PLAIN, 12));
            k.setForeground(TEXT_SECONDARY);
            JLabel v = new JLabel(value);
            v.setFont(new Font("SansSerif", Font.BOLD, 13));
            v.setForeground(TEXT_PRIMARY);
            row.add(k);
            row.add(v);
            panel.add(row);
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
            setBackground(selected ? CARD_SELECTED : CARD_BG);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(
                            selected ? accentColor : accentColor.darker(), selected ? 3 : 2, true),
                    new EmptyBorder(20, 16, 20, 16)));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (selected) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(accentColor.getRed(),
                        accentColor.getGreen(), accentColor.getBlue(), 30));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
        }
    }
}