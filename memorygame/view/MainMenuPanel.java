package memorygame.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainMenuPanel extends JPanel {

    private static final Color BG_COLOR = new Color(0x1A1A2E);
    private static final Color BG_LIGHTER = new Color(0x16213E);
    private static final Color ACCENT_PRIMARY = new Color(0x0F3460);
    private static final Color ACCENT_BLUE = new Color(0x2196F3);
    private static final Color ACCENT_GREEN = new Color(0x4CAF50);
    private static final Color ACCENT_AMBER = new Color(0xFF9800);
    private static final Color ACCENT_PURPLE = new Color(0x8E24AA);
    private static final Color ACCENT_RED = new Color(0xE53935);
    private static final Color TEXT_PRIMARY = Color.WHITE;
    private static final Color TEXT_SECONDARY = new Color(0xB0BEC5);
    private static final Color CARD_PATTERN_COLOR = new Color(255, 255, 255, 8);

    private static final String FONT_FAMILY = "Segoe UI";

    private Runnable onStartGame;
    private Runnable onContinueGame;
    private Runnable onOpenShop;
    private Runnable onExitGame;

    public MainMenuPanel() {
        setBackground(BG_COLOR);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(0, 0, 0, 0));

        add(buildHeaderPanel(), BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildFooterPanel(), BorderLayout.SOUTH);
    }

    public void setOnStartGame(Runnable callback) {
        this.onStartGame = callback;
    }

    public void setOnContinueGame(Runnable callback) {
        this.onContinueGame = callback;
    }

    public void setOnOpenShop(Runnable callback) {
        this.onOpenShop = callback;
    }

    public void setOnExitGame(Runnable callback) {
        this.onExitGame = callback;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Ve hoa tiet the bai mo nhat tren nen
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(CARD_PATTERN_COLOR);

        int cardW = 60;
        int cardH = 80;
        int arc = 10;
        int[][] positions = {
                {40, 60}, {160, 30}, {300, 80}, {450, 20}, {600, 70},
                {750, 40}, {900, 60}, {80, 500}, {250, 550}, {400, 520},
                {550, 560}, {700, 510}, {850, 540}, {130, 300}, {380, 280},
                {620, 310}, {800, 290}, {50, 180}, {500, 170}, {730, 180}
        };
        for (int[] pos : positions) {
            if (pos[0] < getWidth() && pos[1] < getHeight()) {
                g2.drawRoundRect(pos[0], pos[1], cardW, cardH, arc, arc);
            }
        }
        g2.dispose();
    }

    private JPanel buildHeaderPanel() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(50, 40, 10, 40));

        JLabel titleLabel = new JLabel("MEMORY CARD GAME") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(
                        0, 0, ACCENT_BLUE,
                        getWidth(), 0, new Color(0x64B5F6));
                g2.setPaint(gradient);
                g2.setFont(getFont());

                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        titleLabel.setFont(new Font(FONT_FAMILY, Font.BOLD, 46));
        titleLabel.setForeground(ACCENT_BLUE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setPreferredSize(new Dimension(700, 60));
        titleLabel.setMaximumSize(new Dimension(700, 60));
        header.add(titleLabel);

        header.add(Box.createVerticalStrut(14));

        JLabel subtitleLabel = new JLabel("Tìm cặp thẻ - Rèn trí nhớ");
        subtitleLabel.setFont(new Font(FONT_FAMILY, Font.PLAIN, 17));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(subtitleLabel);

        header.add(Box.createVerticalStrut(20));
        JPanel line = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(0x2196F3, true),
                        getWidth() / 2, 0, ACCENT_BLUE,
                        true);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
            }
        };
        line.setOpaque(false);
        line.setPreferredSize(new Dimension(240, 3));
        line.setMaximumSize(new Dimension(240, 3));
        line.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(line);

        return header;
    }

    private JPanel buildCenterPanel() {
        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new GridBagLayout());

        // Card container boc nhom nut
        JPanel cardContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                // Nen card
                g2.setColor(new Color(0x16213E, true));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                // Vien
                g2.setColor(new Color(0x0F3460));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
                g2.dispose();
            }
        };
        cardContainer.setOpaque(false);
        cardContainer.setLayout(new BoxLayout(cardContainer, BoxLayout.Y_AXIS));
        cardContainer.setBorder(new EmptyBorder(36, 50, 36, 50));

        // Tieu de nho trong card
        JLabel menuTitle = new JLabel("Menu chính");
        menuTitle.setFont(new Font(FONT_FAMILY, Font.BOLD, 16));
        menuTitle.setForeground(TEXT_SECONDARY);
        menuTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardContainer.add(menuTitle);
        cardContainer.add(Box.createVerticalStrut(24));

        MenuButton startBtn = new MenuButton("Bắt đầu trò chơi", ACCENT_BLUE);
        startBtn.addActionListener(e -> {
            if (onStartGame != null) onStartGame.run();
        });
        cardContainer.add(startBtn);
        cardContainer.add(Box.createVerticalStrut(16));

        MenuButton continueBtn = new MenuButton("Tiếp tục trò chơi", ACCENT_GREEN);
        continueBtn.addActionListener(e -> {
            if (onContinueGame != null) onContinueGame.run();
        });
        cardContainer.add(continueBtn);
        cardContainer.add(Box.createVerticalStrut(16));

        MenuButton shopBtn = new MenuButton("Cửa hàng", ACCENT_PURPLE);
        shopBtn.addActionListener(e -> {
            if (onOpenShop != null) onOpenShop.run();
        });
        buttonContainer.add(shopBtn);
        buttonContainer.add(Box.createVerticalStrut(14));

        MenuButton guideBtn = new MenuButton("Hướng dẫn chơi", ACCENT_AMBER);
        guideBtn.addActionListener(e -> showInstructions());
        cardContainer.add(guideBtn);
        cardContainer.add(Box.createVerticalStrut(16));

        MenuButton exitBtn = new MenuButton("Thoát", ACCENT_RED);
        exitBtn.addActionListener(e -> {
            if (onExitGame != null) onExitGame.run();
        });
        cardContainer.add(exitBtn);

        wrapper.add(cardContainer);
        return wrapper;
    }

    private JPanel buildFooterPanel() {
        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBorder(new EmptyBorder(16, 0, 28, 0));

        JLabel projectLabel = new JLabel("Đồ án Java Swing - MVC");
        projectLabel.setFont(new Font(FONT_FAMILY, Font.PLAIN, 12));
        projectLabel.setForeground(new Color(0x546E7A));
        projectLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        footer.add(projectLabel);

        footer.add(Box.createVerticalStrut(4));

        JLabel versionLabel = new JLabel("Memory Card Game v1.0");
        versionLabel.setFont(new Font(FONT_FAMILY, Font.PLAIN, 11));
        versionLabel.setForeground(new Color(0x455A64));
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        footer.add(versionLabel);

        return footer;
    }

    private void showInstructions() {
        String content =
                "<html><body style='width:360px; font-family:Segoe UI, SansSerif; color:#E0E0E0; "
                        + "background-color:#1A1A2E; padding:12px;'>"
                        + "<h2 style='color:#2196F3; margin-bottom:8px;'>Hướng dẫn chơi</h2>"
                        + "<p><b>1.</b> Chọn cấp độ: Dễ, Trung bình, hoặc Khó.</p>"
                        + "<p><b>2.</b> Nhấn vào thẻ để lật mở. Mỗi lượt lật được 2 thẻ.</p>"
                        + "<p><b>3.</b> Nếu 2 thẻ giống nhau, chúng sẽ được giữ mở và bạn được cộng điểm.</p>"
                        + "<p><b>4.</b> Nếu 2 thẻ khác nhau, chúng sẽ bị úp lại sau 1 giây.</p>"
                        + "<p><b>5.</b> Nhấn nút <b>Gợi ý</b> để xem tạm thời một cặp thẻ (số lượt giới hạn).</p>"
                        + "<p><b>6.</b> Điểm sau ván chơi được quy đổi thành vàng để mua vật phẩm trong Cửa hàng.</p>"
                        + "<p><b>7.</b> Hoàn thành tất cả các cặp thẻ trước khi hết giờ để thắng.</p>"
                        + "<hr style='border-color:#2196F3;'>"
                        + "<p style='color:#B0BEC5; font-size:11px;'>"
                        + "Mẹo: Ghi nhớ vị trí thẻ đã lật để tăng hiệu quả!</p>"
                        + "</body></html>";

        JOptionPane.showMessageDialog(
                SwingUtilities.getWindowAncestor(this),
                content,
                "Hướng dẫn chơi",
                JOptionPane.PLAIN_MESSAGE
        );
    }

    private static class MenuButton extends JButton {
        private final Color baseColor;
        private final Color hoverColor;
        private final Color pressedColor;
        private boolean hovering = false;

        MenuButton(String text, Color accentColor) {
            super(text);
            this.baseColor = accentColor;
            this.hoverColor = brighter(accentColor, 0.2f);
            this.pressedColor = darker(accentColor, 0.15f);

            setFont(new Font("Segoe UI", Font.BOLD, 17));
            setForeground(Color.WHITE);
            setBackground(baseColor);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.CENTER_ALIGNMENT);
            setPreferredSize(new Dimension(320, 52));
            setMaximumSize(new Dimension(320, 52));
            setMinimumSize(new Dimension(320, 52));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovering = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovering = false;
                    repaint();
                }
            });
        }

        private static Color brighter(Color c, float factor) {
            int r = Math.min(255, (int) (c.getRed() + 255 * factor));
            int g = Math.min(255, (int) (c.getGreen() + 255 * factor));
            int b = Math.min(255, (int) (c.getBlue() + 255 * factor));
            return new Color(r, g, b);
        }

        private static Color darker(Color c, float factor) {
            int r = Math.max(0, (int) (c.getRed() * (1 - factor)));
            int g = Math.max(0, (int) (c.getGreen() * (1 - factor)));
            int b = Math.max(0, (int) (c.getBlue() * (1 - factor)));
            return new Color(r, g, b);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 14;

            Color bg;
            if (getModel().isPressed()) {
                bg = pressedColor;
            } else if (hovering) {
                bg = hoverColor;
            } else {
                bg = baseColor;
            }
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            if (hovering && !getModel().isPressed()) {
                g2.setColor(new Color(255, 255, 255, 25));
                g2.fillRoundRect(0, 0, w, h / 2, arc, arc);
                g2.fillRect(0, h / 4, w, h / 4);
            }

            g2.setFont(getFont());
            g2.setColor(getForeground());
            FontMetrics fm = g2.getFontMetrics();
            int textX = (w - fm.stringWidth(getText())) / 2;
            int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), textX, textY);

            g2.dispose();
        }
    }
}
