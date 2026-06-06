package memorygame.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// UC-15 - Le VietKhanh: Danh mục vật phẩm cửa hàng và cách áp dụng skin/theme vào bàn chơi.
public final class ShopCatalog {

    public static final String DEFAULT_BACK_SKIN_ID = "default_back";
    public static final String DEFAULT_FACE_THEME_ID = "default_face";

    private static final List<ShopItem> ITEMS = createItems();

    private ShopCatalog() {
    }

    private static List<ShopItem> createItems() {
        List<ShopItem> items = new ArrayList<>();

        items.add(new ShopItem(
                DEFAULT_BACK_SKIN_ID,
                "Mặc định",
                "Mặt sau thẻ xanh dương mặc định.",
                ShopItem.ItemType.BACK_SKIN,
                0
        ));
        items.add(new ShopItem(
                "royal_back",
                "Hoàng gia",
                "Đổi mặt sau thẻ sang tông tím nổi bật.",
                ShopItem.ItemType.BACK_SKIN,
                30
        ));
        items.add(new ShopItem(
                "forest_back",
                "Rừng xanh",
                "Đổi mặt sau thẻ sang tông xanh lá.",
                ShopItem.ItemType.BACK_SKIN,
                45
        ));
        items.add(new ShopItem(
                "sunset_back",
                "Hoàng hôn",
                "Đổi mặt sau thẻ sang tông cam ấm.",
                ShopItem.ItemType.BACK_SKIN,
                60
        ));

        items.add(new ShopItem(
                DEFAULT_FACE_THEME_ID,
                "Mặc định",
                "Mặt thẻ hiển thị dạng C1, C2, C3...",
                ShopItem.ItemType.FACE_THEME,
                0
        ));
        items.add(new ShopItem(
                "symbol_face",
                "Biểu tượng",
                "Đổi mặt thẻ sang các ký hiệu dễ nhìn.",
                ShopItem.ItemType.FACE_THEME,
                50
        ));
        items.add(new ShopItem(
                "number_face",
                "Số học",
                "Đổi mặt thẻ sang dạng số 01, 02, 03...",
                ShopItem.ItemType.FACE_THEME,
                70
        ));
        items.add(new ShopItem(
                "emoji_face",
                "Emoji",
                "Đổi mặt thẻ sang các biểu tượng vui nhộn.",
                ShopItem.ItemType.FACE_THEME,
                90
        ));

        return Collections.unmodifiableList(items);
    }

    public static List<ShopItem> getAllItems() {
        return ITEMS;
    }

    public static List<ShopItem> getItemsByType(ShopItem.ItemType type) {
        List<ShopItem> result = new ArrayList<>();
        for (ShopItem item : ITEMS) {
            if (item.getType() == type) {
                result.add(item);
            }
        }
        return result;
    }

    public static ShopItem findById(String itemId) {
        if (itemId == null) {
            return null;
        }
        for (ShopItem item : ITEMS) {
            if (item.getId().equals(itemId)) {
                return item;
            }
        }
        return null;
    }

    public static Color getBackColor(String skinId) {
        if ("royal_back".equals(skinId)) {
            return new Color(0x6A1B9A);
        }
        if ("forest_back".equals(skinId)) {
            return new Color(0x1B5E20);
        }
        if ("sunset_back".equals(skinId)) {
            return new Color(0xE65100);
        }
        return new Color(0x0F3460);
    }

    public static Color getBackHoverColor(String skinId) {
        if ("royal_back".equals(skinId)) {
            return new Color(0x8E24AA);
        }
        if ("forest_back".equals(skinId)) {
            return new Color(0x2E7D32);
        }
        if ("sunset_back".equals(skinId)) {
            return new Color(0xF57C00);
        }
        return new Color(0x154785);
    }

    public static Color getFaceColor(String themeId) {
        if ("symbol_face".equals(themeId)) {
            return new Color(0x00BCD4);
        }
        if ("number_face".equals(themeId)) {
            return new Color(0xFFCA28);
        }
        if ("emoji_face".equals(themeId)) {
            return new Color(0xF8BBD0);
        }
        return Color.WHITE;
    }

    public static String resolveFaceText(String value, String themeId) {
        int index = extractIndex(value);
        if (index <= 0) {
            return value;
        }

        if ("symbol_face".equals(themeId)) {
            String[] symbols = {
                    "★", "◆", "●", "▲", "■", "♥", "♣", "♠",
                    "☀", "☁", "☂", "☕", "♫", "⚑", "✿", "✦",
                    "⬟", "⬢", "⬣", "⬤", "⬥", "⬦", "⬧", "⬨",
                    "◇", "○", "△", "□", "✚", "✖", "✓", "∞"
            };
            return symbols[(index - 1) % symbols.length];
        }

        if ("number_face".equals(themeId)) {
            return String.format("%02d", index);
        }

        if ("emoji_face".equals(themeId)) {
            String[] emojis = {
                    "🍎", "🍋", "🍇", "🍓", "🍒", "🍉", "🍍", "🥝",
                    "🐶", "🐱", "🐰", "🐼", "🦊", "🐸", "🐵", "🐧",
                    "⭐", "🌙", "☀", "🌈", "🔥", "💧", "🍀", "🎵",
                    "⚽", "🎮", "🎲", "🚀", "💎", "🎁", "🏆", "❤️"
            };
            return emojis[(index - 1) % emojis.length];
        }

        return value;
    }

    private static int extractIndex(String value) {
        if (value == null || value.length() < 2) {
            return -1;
        }
        try {
            String numberPart = value.replaceAll("\\D+", "");
            if (numberPart.isEmpty()) {
                return -1;
            }
            return Integer.parseInt(numberPart);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }
}
