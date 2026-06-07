package memorygame.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

// Lưu trạng thái người chơi: vàng, chủ đề đang dùng và danh sách chủ đề sở hữu.
public class PlayerProfile implements Serializable {

    private static final long serialVersionUID = 1L;

    private long gold;
    private String selectedThemeId;
    private final Set<String> ownedThemeIds;

    public PlayerProfile() {
        this.gold = 0;
        this.selectedThemeId = ShopCatalog.DEFAULT_THEME_ID;
        this.ownedThemeIds = new HashSet<>();

        this.ownedThemeIds.add(ShopCatalog.DEFAULT_THEME_ID);
    }

    public long getGold() {
        return gold;
    }

    public void creditGold(long amount) {
        if (amount > 0) {
            if (Long.MAX_VALUE - gold < amount) {
                gold = Long.MAX_VALUE;
            } else {
                gold += amount;
            }
        }
    }

    public boolean spendGold(long amount) {
        if (amount <= 0) {
            return false;
        }

        if (gold >= amount) {
            gold -= amount;
            return true;
        }

        return false;
    }

    public String getSelectedThemeId() {
        if (!ownsTheme(selectedThemeId)) {
            return ShopCatalog.DEFAULT_THEME_ID;
        }

        return selectedThemeId;
    }

    public void setSelectedThemeId(String selectedThemeId) {
        if (ownsTheme(selectedThemeId)) {
            this.selectedThemeId = selectedThemeId;
        }
    }

    public Set<String> getOwnedThemeIds() {
        if (ownedThemeIds == null) {
            Set<String> defaultSet = new HashSet<>();
            defaultSet.add(ShopCatalog.DEFAULT_THEME_ID);
            return Collections.unmodifiableSet(defaultSet);
        }

        return Collections.unmodifiableSet(ownedThemeIds);
    }

    public boolean ownsTheme(String themeId) {
        return isValidItemId(themeId) && ownedThemeIds.contains(themeId);
    }

    public void addTheme(String themeId) {
        if (isValidItemId(themeId)) {
            ownedThemeIds.add(themeId);
        }
    }

    public void copyFrom(PlayerProfile other) {
        if (other == null) {
            return;
        }

        applySnapshot(
                Math.max(0, other.getGold()),
                other.getSelectedThemeId(),
                other.getOwnedThemeIds()
        );
    }

    public void restoreFromSave(long gold, String selectedThemeId, Set<String> ownedThemeIds) {
        applySnapshot(gold, selectedThemeId, ownedThemeIds);
    }

    private void applySnapshot(long gold, String selectedThemeId, Set<String> ownedThemeIds) {
        this.gold = Math.max(0, gold);

        this.ownedThemeIds.clear();
        this.ownedThemeIds.add(ShopCatalog.DEFAULT_THEME_ID);

        if (ownedThemeIds != null) {
            for (String themeId : ownedThemeIds) {
                addTheme(themeId);
            }
        }

        this.selectedThemeId = ShopCatalog.DEFAULT_THEME_ID;
        setSelectedThemeId(selectedThemeId);
    }

    private boolean isValidItemId(String itemId) {
        return itemId != null && !itemId.isBlank() && Objects.equals(itemId.trim(), itemId);
    }
}