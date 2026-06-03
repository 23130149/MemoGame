package memorygame.view;

import memorygame.model.Card;
import memorygame.model.CardState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

public class GameBoardPanel extends JPanel {

    private static final Color BG_COLOR = new Color(0x1A1A2E);
    private static final Color CARD_BACK = new Color(0x0F3460);
    private static final Color CARD_MATCHED = new Color(0x4CAF50);
    private static final Color CARD_FACE_UP = new Color(0x2196F3);
    private static final Color BORDER_COLOR = new Color(0x16213E);
    private static final Color HINT_HIGHLIGHT = new Color(255, 255, 0, 100);  // Vàng bán trong
    private static final Color HINT_BORDER = new Color(255, 215, 0, 200);    // Vàng đậm

    private List<Card> cards;
    private int gridRows;
    private int gridCols;
    private Consumer<Card> onCardClicked;
    private boolean boardLocked = false;

    private CardButton[] cardButtons;

    public GameBoardPanel(int gridRows, int gridCols) {
        this.gridRows = gridRows;
        this.gridCols = gridCols;

        setBackground(BG_COLOR);
        setLayout(new GridLayout(gridRows, gridCols, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
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
        // Cập nhật từ controller
        System.out.println("Remaining hints: " + remainingHints);
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

    // ===== INNER CLASS: CardButton =====
    private class CardButton extends JButton {
        private final Card card;
        private boolean hintHighlighted = false;
        private static final int MATCH_FLASH_DURATION = 500;

        CardButton(Card card) {
            this.card = card;
            setFocusPainted(false);
            setFont(new Font("SansSerif", Font.BOLD, 20));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            updateAppearance();
        }

        Card getCard() {
            return card;
        }

        void updateAppearance() {
            CardState state = card.getState();

            if (state == CardState.MATCHED) {
                setText("✓");
                setBackground(CARD_MATCHED);
                setForeground(Color.WHITE);
                setEnabled(false);
            } else if (state == CardState.FACE_UP) {
                setText(card.getValue());
                setBackground(CARD_FACE_UP);
                setForeground(Color.WHITE);
                setEnabled(true);
            } else { // FACE_DOWN
                setText("?");
                setBackground(CARD_BACK);
                setForeground(new Color(0xB0BEC5));
                setEnabled(true);
            }

            setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 2));
        }

        void showMatchEffect() {
            new Timer(MATCH_FLASH_DURATION, e -> {
                updateAppearance();
                ((Timer) e.getSource()).stop();
            }).start();
        }

        void showNoMatchEffect() {
            new Timer(MATCH_FLASH_DURATION, e -> {
                updateAppearance();
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
            super.paintComponent(g);

            // Vẽ hiệu ứng highlight khi gợi ý
            if (hintHighlighted) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Vùng phủ vàng bán trong
                g2.setColor(HINT_HIGHLIGHT);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Viền vàng đậm
                g2.setColor(HINT_BORDER);
                g2.setStroke(new BasicStroke(3));
                g2.drawRect(1, 1, getWidth() - 2, getHeight() - 2);
            }
        }
    }
}