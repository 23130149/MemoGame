package memorygame.controller;

import memorygame.model.PlayerProfile;
import memorygame.model.ShopCatalog;
import memorygame.model.ShopItem;

// UC-15 - Le VietKhanh: Logic mua và trang bị vật phẩm bằng vàng trong cửa hàng.
public class ShopController {

    private final PlayerProfile playerProfile;

    public ShopController(PlayerProfile playerProfile) {
        this.playerProfile = playerProfile;
    }

    public ShopActionResult buyOrEquip(String itemId) {
        if (playerProfile == null) {
            return ShopActionResult.fail("Không tìm thấy dữ liệu người chơi.");
        }

        ShopItem item = ShopCatalog.findById(itemId);
        if (item == null) {
            return ShopActionResult.fail("Vật phẩm không tồn tại trong cửa hàng.");
        }

        if (isOwned(item)) {
            equip(item);
            return ShopActionResult.success("Đã sử dụng vật phẩm: " + item.getName());
        }

        if (!playerProfile.spendGold(item.getPrice())) {
            return ShopActionResult.fail("Không đủ vàng để mua vật phẩm này.");
        }

        addOwnedItem(item);
        equip(item);
        return ShopActionResult.success("Mua thành công và đã sử dụng: " + item.getName());
    }

    public boolean isOwned(ShopItem item) {
        if (item == null || playerProfile == null) {
            return false;
        }
        if (item.getType() == ShopItem.ItemType.BACK_SKIN) {
            return playerProfile.ownsBackSkin(item.getId());
        }
        return playerProfile.ownsFaceTheme(item.getId());
    }

    public boolean isEquipped(ShopItem item) {
        if (item == null || playerProfile == null) {
            return false;
        }
        if (item.getType() == ShopItem.ItemType.BACK_SKIN) {
            return item.getId().equals(playerProfile.getSelectedBackSkinId());
        }
        return item.getId().equals(playerProfile.getSelectedFaceThemeId());
    }

    private void addOwnedItem(ShopItem item) {
        if (item.getType() == ShopItem.ItemType.BACK_SKIN) {
            playerProfile.addBackSkin(item.getId());
        } else {
            playerProfile.addFaceTheme(item.getId());
        }
    }

    private void equip(ShopItem item) {
        if (item.getType() == ShopItem.ItemType.BACK_SKIN) {
            playerProfile.setSelectedBackSkinId(item.getId());
        } else {
            playerProfile.setSelectedFaceThemeId(item.getId());
        }
    }

    public static class ShopActionResult {
        private final boolean success;
        private final String message;

        private ShopActionResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static ShopActionResult success(String message) {
            return new ShopActionResult(true, message);
        }

        public static ShopActionResult fail(String message) {
            return new ShopActionResult(false, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}
