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

    private Runnable onStartGame;
    private Runnable onContinueGame;
    private Runnable onOpenShop;
    private Runnable onExitGame;

    public MainMenuPanel() {
        setBackground(BG_COLOR);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(0, 0, 0, 0));

        add(buildHeaderPanel(), BorderLayout.NORTH);
        add(buildButtonPanel(), BorderLayout.CENTER);
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

    private JPanel buildHeaderPanel() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(BG_COLOR);
        header.setBorder(new EmptyBorder(60, 40, 20, 40));

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
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 42));
        titleLabel.setForeground(ACCENT_BLUE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setPreferredSize(new Dimension(600, 55));
        titleLabel.setMaximumSize(new Dimension(600, 55));
        header.add(titleLabel);

        header.add(Box.createVerticalStrut(12));

        JLabel subtitleLabel = new JLabel("Tìm cặp thẻ - Rèn trí nhớ");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
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
        line.setPreferredSize(new Dimension(200, 3));
        line.setMaximumSize(new Dimension(200, 3));
        line.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(line);

        return header;
    }

    private JPanel buildButtonPanel() {
        JPanel wrapper = new JPanel();
        wrapper.setBackground(BG_COLOR);
        wrapper.setLayout(new GridBagLayout());

        JPanel buttonContainer = new JPanel();
        buttonContainer.setBackground(BG_COLOR);
        buttonContainer.setLayout(new BoxLayout(buttonContainer, BoxLayout.Y_AXIS));

        MenuButton startBtn = new MenuButton("Bắt đầu trò chơi", ACCENT_BLUE);
        startBtn.addActionListener(e -> {
            if (onStartGame != null) onStartGame.run();
        });
        buttonContainer.add(startBtn);
        buttonContainer.add(Box.createVerticalStrut(14));

        MenuButton continueBtn = new MenuButton("Tiếp tục trò chơi", ACCENT_GREEN);
        continueBtn.addActionListener(e -> {
            if (onContinueGame != null) onContinueGame.run();
        });
        buttonContainer.add(continueBtn);
        buttonContainer.add(Box.createVerticalStrut(14));

        MenuButton shopBtn = new MenuButton("Cửa hàng", ACCENT_PURPLE);
        shopBtn.addActionListener(e -> {
            if (onOpenShop != null) onOpenShop.run();
        });
        buttonContainer.add(shopBtn);
        buttonContainer.add(Box.createVerticalStrut(14));

        MenuButton guideBtn = new MenuButton("Hướng dẫn chơi", ACCENT_AMBER);
        guideBtn.addActionListener(e -> showInstructions());
        buttonContainer.add(guideBtn);
        buttonContainer.add(Box.createVerticalStrut(14));

        MenuButton exitBtn = new MenuButton("Thoát", ACCENT_RED);
        exitBtn.addActionListener(e -> {
            if (onExitGame != null) onExitGame.run();
        });
        buttonContainer.add(exitBtn);

        wrapper.add(buttonContainer);
        return wrapper;
    }

    private JPanel buildFooterPanel() {
        JPanel footer = new JPanel();
        footer.setBackground(BG_COLOR);
        footer.setBorder(new EmptyBorder(20, 0, 30, 0));

        JLabel versionLabel = new JLabel("Java Swing MVC - v1.0");
        versionLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        versionLabel.setForeground(new Color(0x546E7A));
        footer.add(versionLabel);

        return footer;
    }

    private void showInstructions() {
        String content =
                "<html><body style='width:340px; font-family:SansSerif; color:#E0E0E0; "
                        + "background-color:#1A1A2E; padding:10px;'>"
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

            setFont(new Font("SansSerif", Font.BOLD, 16));
            setForeground(Color.WHITE);
            setBackground(baseColor);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.CENTER_ALIGNMENT);
            setPreferredSize(new Dimension(280, 48));
            setMaximumSize(new Dimension(280, 48));
            setMinimumSize(new Dimension(280, 48));

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
            int arc = 12;

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
