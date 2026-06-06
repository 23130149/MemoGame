package memorygame.view;

import memorygame.controller.ShopController;
import memorygame.model.PlayerProfile;
import memorygame.model.ShopCatalog;
import memorygame.model.ShopItem;
import memorygame.persistence.PlayerProfileStore;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

// UC-15 - Le VietKhanh: Giao diện cửa hàng quy đổi vàng lấy skin/thẻ.
public class ShopPanel extends JPanel {

    private static final Color BG_COLOR = new Color(0x1A1A2E);
    private static final Color CARD_BG = new Color(0x16213E);
    private static final Color TEXT_PRIMARY = Color.WHITE;
    private static final Color TEXT_SECONDARY = new Color(0xB0BEC5);
    private static final Color ACCENT_GOLD = new Color(0xFFD54F);
    private static final Color ACCENT_BLUE = new Color(0x2196F3);
    private static final Color ACCENT_GREEN = new Color(0x4CAF50);
    private static final Color ACCENT_GRAY = new Color(0x607D8B);

    private final PlayerProfile playerProfile;
    private final ShopController shopController;
    private final Runnable onBackToMenu;
    private final JLabel goldLabel = new JLabel();
    private final List<JButton> actionButtons = new ArrayList<>();

    public ShopPanel(PlayerProfile playerProfile, Runnable onBackToMenu) {
        this.playerProfile = playerProfile;
        this.shopController = new ShopController(playerProfile);
        this.onBackToMenu = onBackToMenu;

        setBackground(BG_COLOR);
        setLayout(new BorderLayout(0, 16));
        setBorder(new EmptyBorder(24, 36, 24, 36));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        refreshGoldLabel();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("CỬA HÀNG", SwingConstants.LEFT);
        title.setForeground(TEXT_PRIMARY);
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        header.add(title, BorderLayout.WEST);

        goldLabel.setForeground(ACCENT_GOLD);
        goldLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        header.add(goldLabel, BorderLayout.EAST);

        return header;
    }

    private JScrollPane buildContent() {
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        content.add(buildSection("Skin mặt sau thẻ", ShopItem.ItemType.BACK_SKIN));
        content.add(Box.createVerticalStrut(22));
        content.add(buildSection("Chủ đề mặt thẻ", ShopItem.ItemType.FACE_THEME));

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        return scrollPane;
    }

    private JPanel buildSection(String title, ShopItem.ItemType type) {
        JPanel section = new JPanel();
        section.setOpaque(false);
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(title);
        label.setForeground(TEXT_PRIMARY);
        label.setFont(new Font("SansSerif", Font.BOLD, 18));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(label);
        section.add(Box.createVerticalStrut(10));

        JPanel grid = new JPanel(new GridLayout(0, 2, 14, 14));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (ShopItem item : ShopCatalog.getItemsByType(type)) {
            grid.add(buildItemCard(item));
        }

        section.add(grid);
        return section;
    }

    private JPanel buildItemCard(ShopItem item) {
        JPanel card = new JPanel(new BorderLayout(10, 8));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x253A5C), 1, true),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel name = new JLabel(item.getName());
        name.setForeground(TEXT_PRIMARY);
        name.setFont(new Font("SansSerif", Font.BOLD, 15));

        JLabel desc = new JLabel("<html><body style='width:230px'>" + item.getDescription() + "</body></html>");
        desc.setForeground(TEXT_SECONDARY);
        desc.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(name);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(desc);

        JButton actionBtn = new JButton();
        actionBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        actionBtn.setFocusPainted(false);
        actionBtn.setBorderPainted(false);
        actionBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        actionBtn.setPreferredSize(new Dimension(130, 36));
        actionBtn.addActionListener(e -> handleItemAction(item));
        actionButtons.add(actionBtn);

        card.add(textPanel, BorderLayout.CENTER);
        card.add(actionBtn, BorderLayout.EAST);

        updateButtonState(actionBtn, item);
        return card;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        footer.setOpaque(false);

        JButton backBtn = new JButton("Quay lại Menu");
        styleFooterButton(backBtn, ACCENT_GRAY);
        backBtn.addActionListener(e -> {
            PlayerProfileStore.saveDefault(playerProfile);
            if (onBackToMenu != null) {
                onBackToMenu.run();
            }
        });

        footer.add(backBtn);
        return footer;
    }

    private void handleItemAction(ShopItem item) {
        ShopController.ShopActionResult result = shopController.buyOrEquip(item.getId());
        if (result.isSuccess()) {
            PlayerProfileStore.saveDefault(playerProfile);
            refreshAll();
        }

        JOptionPane.showMessageDialog(
                this,
                result.getMessage(),
                result.isSuccess() ? "Cửa hàng" : "Không thể mua",
                result.isSuccess() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE
        );
    }

    private void refreshAll() {
        refreshGoldLabel();
        for (JButton button : actionButtons) {
            ShopItem item = (ShopItem) button.getClientProperty("shopItem");
            if (item != null) {
                updateButtonState(button, item);
            }
        }
    }

    private void updateButtonState(JButton button, ShopItem item) {
        button.putClientProperty("shopItem", item);

        if (shopController.isEquipped(item)) {
            button.setText("Đang dùng");
            button.setEnabled(false);
            button.setBackground(ACCENT_GREEN);
            button.setForeground(Color.WHITE);
            return;
        }

        button.setEnabled(true);
        button.setForeground(Color.WHITE);

        if (shopController.isOwned(item)) {
            button.setText("Sử dụng");
            button.setBackground(ACCENT_BLUE);
        } else {
            button.setText(item.isFree() ? "Miễn phí" : item.getPrice() + " vàng");
            button.setBackground(ACCENT_GOLD);
            button.setForeground(new Color(0x1A1A2E));
        }
    }

    private void refreshGoldLabel() {
        long gold = playerProfile == null ? 0 : playerProfile.getGold();
        goldLabel.setText("Vàng: " + gold);
    }

    private void styleFooterButton(JButton btn, Color bgColor) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 40));
    }
}
