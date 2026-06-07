package memorygame.view;

import memorygame.model.Card;
import memorygame.model.CardState;
import memorygame.model.PlayerProfile;
import memorygame.model.ShopCatalog;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class GameBoardPanel extends JPanel {

    private static final Color BG_COLOR = new Color(0x1A1A2E);
    private static final Color CARD_BACK = new Color(0x0F3460);
    private static final Color CARD_BACK_HOVER = new Color(0x154785);
    private static final Color CARD_MATCHED = new Color(0x1B5E20);
    private static final Color CARD_FACE_UP = new Color(0x1976D2);
    private static final Color BORDER_COLOR = new Color(0x16213E);
    private static final Color HINT_HIGHLIGHT = new Color(255, 255, 0, 100);
    private static final Color HINT_BORDER = new Color(255, 215, 0, 200);

    private static final Font CARD_FONT = new Font("SansSerif", Font.BOLD, 22);
    private static final Font CARD_FONT_VALUE = new Font("Arial", Font.BOLD, 20);

    private static final int CARD_ARC = 14;
    private static final int CARD_GAP = 8;
    private static final int BOARD_PAD = 16;

    private List<Card> cards;
    private final PlayerProfile playerProfile;
    private int gridRows;
    private int gridCols;
    private Consumer<Card> onCardClicked;
    private Consumer<Integer> onHintCountChanged;
    private Runnable onHintAnimationDone;
    private boolean boardLocked = false;

    private CardButton[] cardButtons;
    private final Map<String, Image> imageCache = new HashMap<>();

    public GameBoardPanel(int gridRows, int gridCols) {
        this(gridRows, gridCols, null);
    }

    public GameBoardPanel(int gridRows, int gridCols, PlayerProfile playerProfile) {
        this.gridRows = gridRows;
        this.gridCols = gridCols;
        this.playerProfile = playerProfile;

        setBackground(BG_COLOR);
        setLayout(new GridLayout(gridRows, gridCols, CARD_GAP, CARD_GAP));
        setBorder(BorderFactory.createEmptyBorder(BOARD_PAD, BOARD_PAD, BOARD_PAD, BOARD_PAD));
    }

    public void initializeBoard(List<Card> cards) {
        this.cards = cards;
        removeAll();

        cardButtons = new CardButton[cards.size()];

        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            CardButton btn = new CardButton(card);

            btn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!boardLocked && onCardClicked != null) {
                        onCardClicked.accept(card);
                    }
                }
            });

            cardButtons[i] = btn;
            add(btn);
        }

        revalidate();
        repaint();
    }

    public void repaintCard(Card card) {
        if (cardButtons == null) {
            return;
        }

        for (CardButton btn : cardButtons) {
            if (btn.getCard() == card) {
                btn.updateAppearance();
                btn.repaint();
                break;
            }
        }
    }

    public void showMatchEffect(Card first, Card second) {
        if (cardButtons == null) {
            return;
        }

        for (CardButton btn : cardButtons) {
            if (btn.getCard() == first || btn.getCard() == second) {
                btn.showMatchEffect();
            }
        }
    }

    public void showNoMatchEffect(Card first, Card second) {
        if (cardButtons == null) {
            return;
        }

        for (CardButton btn : cardButtons) {
            if (btn.getCard() == first || btn.getCard() == second) {
                btn.showNoMatchEffect();
            }
        }
    }

    public void showHintEffect(Card cardX, Card cardY) {
        if (cardButtons == null) {
            return;
        }

        for (CardButton btn : cardButtons) {
            if (btn.getCard() == cardX || btn.getCard() == cardY) {
                btn.showHintHighlight();
            }
        }
    }

    public void hideHintEffect(Card cardX, Card cardY) {
        if (cardButtons == null) {
            return;
        }

        for (CardButton btn : cardButtons) {
            if (btn.getCard() == cardX || btn.getCard() == cardY) {
                btn.hideHintHighlight();
            }
        }
    }

    public void setBoardLocked(boolean locked) {
        this.boardLocked = locked;
    }

    public void setOnCardClicked(Consumer<Card> callback) {
        this.onCardClicked = callback;
    }

    // ===== PHẦN PHÁT TRIỂN BỞI NGUYỄN VĂN THẮNG - UC07/UC08 =====
    // Đồng bộ giao diện với logic gợi ý.
    // UC07: cập nhật số lượt gợi ý còn lại sau khi Controller trừ lượt hợp lệ.
    // UC08: thông báo cho GameFlowController khi animation gợi ý kết thúc để bật lại nút Gợi ý.
    public void updateHintDisplay(int remainingHints) {
        if (onHintCountChanged != null) {
            onHintCountChanged.accept(remainingHints);
        }
    }

    public void setOnHintCountChanged(Consumer<Integer> callback) {
        this.onHintCountChanged = callback;
    }

    public void setOnHintAnimationDone(Runnable callback) {
        this.onHintAnimationDone = callback;
    }

    public void notifyHintAnimationDone() {
        if (onHintAnimationDone != null) {
            onHintAnimationDone.run();
        }
    }

    public void showNotify(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Thông báo",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public void showGameOver(int score, int moves) {
        JOptionPane.showMessageDialog(
                this,
                "Chúc mừng! Bạn đã hoàn thành!\nĐiểm: " + score + " | Số lượt: " + moves,
                "Kết thúc trò chơi",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private String getSelectedThemeId() {
        if (playerProfile == null) {
            return ShopCatalog.DEFAULT_THEME_ID;
        }

        String selectedThemeId = playerProfile.getSelectedThemeId();
        if (selectedThemeId == null || selectedThemeId.isBlank()) {
            return ShopCatalog.DEFAULT_THEME_ID;
        }

        return selectedThemeId;
    }

    private Image getCardBackImage() {
        String themeId = getSelectedThemeId();

        if (playerProfile == null || ShopCatalog.DEFAULT_THEME_ID.equals(themeId)) {
            Image img = loadImage("/memorygame/themes/back_question.png");
            if (img != null) {
                return img;
            }
            return createQuestionMarkImage(128, 128);
        }

        return loadImage(ShopCatalog.getBackImagePath(themeId));
    }

    private Image createQuestionMarkImage(int width, int height) {
        try {
            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(
                    width,
                    height,
                    java.awt.image.BufferedImage.TYPE_INT_ARGB
            );

            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setColor(new Color(0x0F3460));
            g.fillRect(0, 0, width, height);

            g.setColor(Color.WHITE);
            Font f = new Font("SansSerif", Font.BOLD, Math.max(12, width / 2));
            g.setFont(f);

            String q = "?";
            FontMetrics fm = g.getFontMetrics();
            int tx = (width - fm.stringWidth(q)) / 2;
            int ty = (height - fm.getHeight()) / 2 + fm.getAscent();

            g.drawString(q, tx, ty);
            g.dispose();

            return img;
        } catch (Exception ex) {
            return null;
        }
    }

    private Image getCardFaceImage(Card card) {
        if (card == null) {
            return null;
        }

        String themeId = getSelectedThemeId();

        Image image = loadImage(
                ShopCatalog.getFaceImagePath(
                        themeId,
                        card.getValue()
                )
        );

        if (image != null) {
            return image;
        }

        return createFaceValueImage(card.getValue());
    }

    private Image createFaceValueImage(String value) {
        int width = 128;
        int height = 128;

        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(
                width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D g = img.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            String text = value == null ? "" : value.trim();
            if (text.isEmpty()) {
                text = "C?";
            }

            int fontSize = text.length() <= 2 ? 42 : 34;
            Font font = new Font("SansSerif", Font.BOLD, fontSize);
            g.setFont(font);
            g.setColor(Color.WHITE);

            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            int x = (width - textWidth) / 2;
            int y = (height - fm.getHeight()) / 2 + fm.getAscent();

            g.drawString(text, x, y);
        } finally {
            g.dispose();
        }

        return img;
    }

    private Image loadImage(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }

        path = path.replace("\\", "/");

        if (imageCache.containsKey(path)) {
            return imageCache.get(path);
        }

        Image image = null;

        String[] classpathCandidates = {
                path,
                path.startsWith("/memorygame/") ? path.substring("/memorygame".length()) : path,
                path.startsWith("/") ? path.substring(1) : "/" + path
        };

        for (String candidate : classpathCandidates) {
            URL url = getClass().getResource(candidate.startsWith("/") ? candidate : "/" + candidate);
            if (url != null) {
                image = new ImageIcon(url).getImage();
                break;
            }
        }

        if (image == null) {
            image = loadImageFromFileSystem(path);
        }

        if (image == null) {
            System.err.println("Không tìm thấy ảnh: " + path);
        }

        imageCache.put(path, image);
        return image;
    }

    private Image loadImageFromFileSystem(String resourcePath) {
        try {
            String themePath = resourcePath
                    .replaceFirst("^/memorygame/themes/", "")
                    .replaceFirst("^memorygame/themes/", "")
                    .replaceFirst("^/themes/", "")
                    .replaceFirst("^themes/", "");

            File imageFile = findThemeFile(themePath);

            if (imageFile == null || !imageFile.exists()) {
                return null;
            }

            return ImageIO.read(imageFile);
        } catch (Exception e) {
            System.err.println("Lỗi load ảnh từ file system: " + e.getMessage());
            return null;
        }
    }

    private File findThemeFile(String themePath) {
        String userDir = System.getProperty("user.dir");
        String fixedPath = themePath.replace("/", File.separator);

        File[] candidates = {
                new File(userDir, "src/memorygame/themes/" + fixedPath),
                new File(userDir, "memorygame/themes/" + fixedPath),
                new File(userDir, "themes/" + fixedPath),
                new File(userDir, "target/classes/themes/" + fixedPath),
                new File(userDir, "target/classes/memorygame/themes/" + fixedPath),
                new File(userDir, "../src/memorygame/themes/" + fixedPath),
                new File(userDir, "../memorygame/themes/" + fixedPath)
        };

        for (File file : candidates) {
            if (file.exists()) {
                return file;
            }
        }

        return null;
    }

    private class CardButton extends JButton {

        private static final int MATCH_FLASH_DURATION = 500;

        private boolean flashGreen = false;
        private boolean flashRed = false;

        private final Card card;
        private boolean hintHighlighted = false;
        private boolean hovering = false;
        private boolean flashWhite = false;

        CardButton(Card card) {
            this.card = card;

            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setFont(CARD_FONT);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

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

            updateAppearance();
        }

        Card getCard() {
            return card;
        }

        void updateAppearance() {
            CardState state = card.getState();
            setText("");
            if (state == CardState.MATCHED) {
                setText(card.getValue());
                setFont(CARD_FONT_VALUE);
                setForeground(Color.WHITE);
                setEnabled(false);
            } else if (state == CardState.FACE_UP) {
                setText(card.getValue());
                setFont(CARD_FONT_VALUE);
                setForeground(Color.WHITE);
                setEnabled(true);
            } else {
                setText("");
                setFont(CARD_FONT);
                setForeground(new Color(0x64B5F6));
                setEnabled(true);
            }
            setEnabled(state != CardState.MATCHED);
        }

        void showMatchEffect() {
            flashGreen = true;
            repaint();

            new Timer(MATCH_FLASH_DURATION, e -> {
                flashGreen = false;
                updateAppearance();
                repaint();
                ((Timer) e.getSource()).stop();
            }).start();
        }

        void showNoMatchEffect() {
            flashRed = true;
            repaint();
            new Timer(MATCH_FLASH_DURATION, e -> {
                flashRed = false;
                updateAppearance();
                repaint();
                ((Timer) e.getSource()).stop();
            }).start();
        }

        void showHintHighlight() {
            hintHighlighted = true;
            repaint();
        }

        void hideHintHighlight() {
            hintHighlighted = false;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            CardState state = card.getState();

            Color bgColor;
            if (flashGreen) {
                bgColor = new Color(0x1B5E20);
            } else if (flashRed) {
                bgColor = new Color(0xEF5350);
            } else if (state == CardState.MATCHED) {
                bgColor = CARD_MATCHED;
            } else if (state == CardState.FACE_UP) {
                bgColor = CARD_FACE_UP;
            } else {
                bgColor = hovering && !boardLocked ? CARD_BACK_HOVER : CARD_BACK;
            }

            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, w, h, CARD_ARC, CARD_ARC);

            if (flashRed) {
                g2.setColor(new Color(0xFF1744));
            } else if (state == CardState.MATCHED) {
                g2.setColor(new Color(0x00E676));
            } else if (state == CardState.FACE_UP) {
                g2.setColor(new Color(0x42A5F5));
            } else {
                g2.setColor(BORDER_COLOR);
            }
            g2.setStroke(new BasicStroke(state == CardState.MATCHED ? 3f : 2f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, CARD_ARC, CARD_ARC);

            Image image = null;
            if (state == CardState.FACE_DOWN && image == null) {
                g2.setFont(CARD_FONT);
                g2.setColor(new Color(0x64B5F6));
                FontMetrics fm = g2.getFontMetrics();
                String q = "?";
                int textX = (w - fm.stringWidth(q)) / 2;
                int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(q, textX, textY);
            } else if (state == CardState.FACE_UP || state == CardState.MATCHED) {
                image = getCardFaceImage(card);
            }

            if (image != null) {
                int padding = 8;
                int availableW = w - padding * 2;
                int availableH = h - padding * 2;
                int imgW = image.getWidth(this);
                int imgH = image.getHeight(this);

                if (imgW > 0 && imgH > 0) {
                    double scale = Math.min((double) availableW / imgW, (double) availableH / imgH);
                    int drawW = (int) (imgW * scale);
                    int drawH = (int) (imgH * scale);
                    int x = (w - drawW) / 2;
                    int y = (h - drawH) / 2;
                    g2.drawImage(image, x, y, drawW, drawH, this);
                }
            }

            g2.setFont(getFont());
            g2.setColor((flashGreen || flashRed) ? Color.WHITE : getForeground());
            FontMetrics fm = g2.getFontMetrics();
            String text = getText();
            if (text != null && !text.isEmpty() && image == null) {
                int textX = (w - fm.stringWidth(text)) / 2;
                int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(text, textX, textY);
            }

            if (hintHighlighted) {
                g2.setColor(HINT_HIGHLIGHT);
                g2.fillRoundRect(0, 0, w, h, CARD_ARC, CARD_ARC);
                g2.setColor(HINT_BORDER);
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(1, 1, w - 3, h - 3, CARD_ARC, CARD_ARC);
            }

            g2.dispose();
        }
    }
}
