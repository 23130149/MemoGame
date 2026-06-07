package memorygame.model;

import java.io.Serializable;

// UC-15 - Le VietKhanh: Model vật phẩm trong cửa hàng dùng vàng để mua/đổi skin.
public class ShopItem implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum ItemType {
        THEME("Chủ đề thẻ");

        private final String displayName;

        ItemType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private final String id;
    private final String name;
    private final String description;
    private final ItemType type;
    private final long price;

    public ShopItem(String id, String name, String description, ItemType type, long price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.price = Math.max(0, price);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ItemType getType() {
        return type;
    }

    public long getPrice() {
        return price;
    }

    public boolean isFree() {
        return price == 0;
    }
}
