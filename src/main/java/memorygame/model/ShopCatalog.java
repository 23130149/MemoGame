package memorygame.model;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

// UC-15 - Danh mục chủ đề cửa hàng.
public final class ShopCatalog {

    public static final int THEME_PRICE = 150;
    public static final String DEFAULT_THEME_ID = "default";

    private static final List<ShopItem> ITEMS = createItems();

    private ShopCatalog() {
    }

    private static List<ShopItem> createItems() {
        List<ShopItem> items = new ArrayList<>();

        items.add(new ShopItem(
                "default",
                "Mặc định",
                "Chủ đề mặc định của trò chơi.",
                ShopItem.ItemType.THEME,
                0
        ));

        items.add(new ShopItem(
                "animal",
                "Động vật",
                "Chủ đề động vật dễ thương.",
                ShopItem.ItemType.THEME,
                THEME_PRICE
        ));

        items.add(new ShopItem(
                "anime",
                "Anime",
                "Chủ đề anime.",
                ShopItem.ItemType.THEME,
                THEME_PRICE
        ));

        items.add(new ShopItem(
                "foot",
                "Foot",
                "Chủ đề Foot.",
                ShopItem.ItemType.THEME,
                THEME_PRICE
        ));

        items.add(new ShopItem(
                "football",
                "Bóng đá",
                "Chủ đề bóng đá.",
                ShopItem.ItemType.THEME,
                THEME_PRICE
        ));

        items.add(new ShopItem(
                "lol",
                "Liên Minh Huyền Thoại",
                "Chủ đề LOL.",
                ShopItem.ItemType.THEME,
                THEME_PRICE
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

    public static String getBackImagePath(String themeId) {
        String normalizedThemeId = normalizeThemeId(themeId);
        String[] backExtensions = {".jpg", ".jpeg", ".png"};

        for (String extension : backExtensions) {
            String path = "/memorygame/themes/" + normalizedThemeId + "/back" + extension;
            if (resourceExists(path)) {
                return path;
            }
        }

        for (String extension : backExtensions) {
            String fallbackPath = "/memorygame/themes/" + DEFAULT_THEME_ID + "/back" + extension;
            if (resourceExists(fallbackPath)) {
                return fallbackPath;
            }
        }

        return "/memorygame/themes/" + normalizedThemeId + "/back.jpg";
    }

    public static String getFaceImagePath(String themeId, String cardValue) {
        int index = extractIndex(cardValue);
        if (index <= 0) {
            index = 1;
        }

        String normalizedThemeId = normalizeThemeId(themeId);
        String[] extensions = {".jpg", ".png", ".jpeg"};

        for (String extension : extensions) {
            String path = "/memorygame/themes/" + normalizedThemeId + "/" + index + extension;
            if (resourceExists(path)) {
                return path;
            }
        }

        for (String extension : extensions) {
            String fallbackPath = "/memorygame/themes/" + DEFAULT_THEME_ID + "/" + index + extension;
            if (resourceExists(fallbackPath)) {
                return fallbackPath;
            }
        }

        return "/memorygame/themes/" + normalizedThemeId + "/" + index + ".jpg";
    }

    private static String normalizeThemeId(String themeId) {
        if (themeId == null || themeId.trim().isEmpty()) {
            return DEFAULT_THEME_ID;
        }
        return themeId.trim().toLowerCase(Locale.ROOT);
    }

    private static int extractIndex(String value) {
        if (value == null || value.isEmpty()) {
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

    private static boolean resourceExists(String path) {
        URL url = ShopCatalog.class.getResource(path);
        return url != null;
    }
}