package memorygame.view;

import memorygame.model.Card;
import memorygame.model.CardState;
import memorygame.model.PlayerProfile;
import memorygame.model.ShopCatalog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

public class GameBoardPanel extends JPanel {

    private static final Color BG_COLOR = new Color(0x1A1A2E);
    private static final Color CARD_BACK = new Color(0x0F3460);
    private static final Color CARD_BACK_HOVER = new Color(0x154785);
    private static final Color CARD_MATCHED = new Color(0x2E7D32);
    private static final Color CARD_FACE_UP = new Color(0x1976D2);
    private static final Color BORDER_COLOR = new Color(0x16213E);
    private static final Color HINT_HIGHLIGHT = new Color(255, 255, 0, 100);  // Vang ban trong
    private static final Color HINT_BORDER = new Color(255, 215, 0, 200);  // Vang dam

    private static final Font CARD_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font CARD_FONT_VALUE = new Font("Segoe UI", Font.BOLD, 22);

    private static final int CARD_ARC = 14;
    private static final int CARD_GAP = 10;
    private static final int BOARD_PAD = 20;

    private List<Card> cards;
    private final PlayerProfile playerProfile;
    private int gridRows;
    private int gridCols;
    private Consumer<Card> onCardClicked;
    private Consumer<Integer> onHintCountChanged;
    private Runnable onHintAnimationDone;
    private boolean boardLocked = false;

    private CardButton[] cardButtons;

    public GameBoardPanel(int gridRows, int gridCols) {
        this(gridRows, gridCols, null);
    }

    // UC-15 - Le VietKhanh: Nhận PlayerProfile để áp dụng skin/theme đã mua từ cửa hàng.
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
        for (CardButton btn : cardButtons) {
            if (btn.getCard() == card) {
                btn.updateAppearance();
                btn.repaint();
                break;
            }
        }
    }

    public void showMatchEffect(Card first, Card second) {
        for (CardButton btn : cardButtons) {
            if (btn.getCard() == first || btn.getCard() == second) {
                btn.showMatchEffect();
            }
        }
    }

    public void showNoMatchEffect(Card first, Card second) {
        for (CardButton btn : cardButtons) {
            if (btn.getCard() == first || btn.getCard() == second) {
                btn.showNoMatchEffect();
            }
        }
    }

    public void showHintEffect(Card cardX, Card cardY) {
        for (CardButton btn : cardButtons) {
            if (btn.getCard() == cardX || btn.getCard() == cardY) {
                btn.showHintHighlight();
            }
        }
    }

    public void hideHintEffect(Card cardX, Card cardY) {
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
        JOptionPane.showMessageDialog(this, message, "Thông báo",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void showGameOver(int score, int moves) {
        JOptionPane.showMessageDialog(this,
                "Chúc mừng! Bạn đã hoàn thành!\nĐiểm: " + score + " | Số lượt: " + moves,
                "Kết thúc trò chơi",
                JOptionPane.INFORMATION_MESSAGE);
    }


    private String getSelectedBackSkinId() {
        if (playerProfile == null) {
            return ShopCatalog.DEFAULT_BACK_SKIN_ID;
        }
        return playerProfile.getSelectedBackSkinId();
    }

    private String getSelectedFaceThemeId() {
        if (playerProfile == null) {
            return ShopCatalog.DEFAULT_FACE_THEME_ID;
        }
        return playerProfile.getSelectedFaceThemeId();
    }

    private Color getCardBackColor(boolean hovering) {
        String skinId = getSelectedBackSkinId();
        return hovering ? ShopCatalog.getBackHoverColor(skinId) : ShopCatalog.getBackColor(skinId);
    }

    private Color getCardFaceTextColor() {
        return ShopCatalog.getFaceColor(getSelectedFaceThemeId());
    }

    private String getCardFaceText(Card card) {
        if (card == null) {
            return "";
        }
        return ShopCatalog.resolveFaceText(card.getValue(), getSelectedFaceThemeId());
    }

    private class CardButton extends JButton {
        private static final int MATCH_FLASH_DURATION = 500;
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

            if (state == CardState.MATCHED) {
                setText("OK");
                setFont(CARD_FONT);
                setForeground(new Color(0xC8E6C9));
                setEnabled(false);
            } else if (state == CardState.FACE_UP) {
                setText(getCardFaceText(card));
                setFont(CARD_FONT_VALUE);
                setForeground(getCardFaceTextColor());
                setEnabled(true);
            } else {
                setText("?");
                setFont(CARD_FONT);
                setForeground(new Color(0x64B5F6));
                setEnabled(true);
            }
        }

        void showMatchEffect() {
            flashWhite = true;
            repaint();
            new Timer(MATCH_FLASH_DURATION, e -> {
                flashWhite = false;
                updateAppearance();
                repaint();
                ((Timer) e.getSource()).stop();
            }).start();
        }

        void showNoMatchEffect() {
            new Timer(MATCH_FLASH_DURATION, e -> {
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
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            CardState state = card.getState();

            Color bgColor;
            if (flashWhite) {
                bgColor = new Color(0xE8F5E9);
            } else if (state == CardState.MATCHED) {
                bgColor = CARD_MATCHED;
            } else if (state == CardState.FACE_UP) {
                bgColor = CARD_FACE_UP;
            } else {
                bgColor = getCardBackColor(hovering && !boardLocked);
            }
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, w, h, CARD_ARC, CARD_ARC);

            if (state == CardState.MATCHED) {
                g2.setColor(new Color(0x4CAF50));
            } else if (state == CardState.FACE_UP) {
                g2.setColor(new Color(0x42A5F5));
            } else {
                g2.setColor(BORDER_COLOR);
            }
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, CARD_ARC, CARD_ARC);

            if (state == CardState.FACE_DOWN && !flashWhite) {
                g2.setColor(new Color(255, 255, 255, 12));
                g2.fillRoundRect(2, 2, w - 4, h / 3, CARD_ARC, CARD_ARC);
            }

            g2.setFont(getFont());
            if (flashWhite) {
                g2.setColor(CARD_MATCHED);
            } else {
                g2.setColor(getForeground());
            }
            FontMetrics fm = g2.getFontMetrics();
            String text = getText();
            if (text != null && !text.isEmpty()) {
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