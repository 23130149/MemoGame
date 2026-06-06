package memorygame.controller;

import memorygame.model.PlayerProfile;
import memorygame.model.ShopCatalog;
import memorygame.model.ShopItem;

// UC-15 - Logic mua và trang bị chủ đề bằng vàng trong cửa hàng.
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
            return ShopActionResult.success("Đã sử dụng chủ đề: " + item.getName());
        }

        if (!playerProfile.spendGold(item.getPrice())) {
            return ShopActionResult.fail("Không đủ vàng để mua chủ đề này.");
        }

        addOwnedItem(item);
        equip(item);

        return ShopActionResult.success("Mua thành công và đã sử dụng chủ đề: " + item.getName());
    }

    public boolean isOwned(ShopItem item) {
        if (item == null || playerProfile == null) {
            return false;
        }

        return playerProfile.ownsTheme(item.getId());
    }

    public boolean isEquipped(ShopItem item) {
        if (item == null || playerProfile == null) {
            return false;
        }

        return item.getId().equals(playerProfile.getSelectedThemeId());
    }

    private void addOwnedItem(ShopItem item) {
        playerProfile.addTheme(item.getId());
    }

    private void equip(ShopItem item) {
        playerProfile.setSelectedThemeId(item.getId());
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