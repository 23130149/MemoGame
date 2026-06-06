package memorygame.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

// Lưu trạng thái người chơi: vàng, skin/theme đang dùng và danh sách vật phẩm sở hữu.
public class PlayerProfile implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_BACK_SKIN_ID = "default_back";
    private static final String DEFAULT_FACE_THEME_ID = "default_face";

    private long gold;
    private String selectedBackSkinId;
    private String selectedFaceThemeId;
    private final Set<String> ownedBackSkinIds;
    private final Set<String> ownedFaceThemeIds;

    public PlayerProfile() {
        this.gold = 0;
        this.selectedBackSkinId = DEFAULT_BACK_SKIN_ID;
        this.selectedFaceThemeId = DEFAULT_FACE_THEME_ID;
        this.ownedBackSkinIds = new HashSet<>();
        this.ownedFaceThemeIds = new HashSet<>();

        this.ownedBackSkinIds.add(DEFAULT_BACK_SKIN_ID);
        this.ownedFaceThemeIds.add(DEFAULT_FACE_THEME_ID);
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
        if (amount <= 0) return false;

        if (gold >= amount) {
            gold -= amount;
            return true;
        }

        return false;
    }

    public String getSelectedBackSkinId() {
        return selectedBackSkinId;
    }

    public void setSelectedBackSkinId(String selectedBackSkinId) {
        if (ownsBackSkin(selectedBackSkinId)) {
            this.selectedBackSkinId = selectedBackSkinId;
        }
    }

    public String getSelectedFaceThemeId() {
        return selectedFaceThemeId;
    }

    public void setSelectedFaceThemeId(String selectedFaceThemeId) {
        if (ownsFaceTheme(selectedFaceThemeId)) {
            this.selectedFaceThemeId = selectedFaceThemeId;
        }
    }

    public Set<String> getOwnedBackSkinIds() {
        return Collections.unmodifiableSet(ownedBackSkinIds);
    }

    public Set<String> getOwnedFaceThemeIds() {
        return Collections.unmodifiableSet(ownedFaceThemeIds);
    }

    public boolean ownsBackSkin(String skinId) {
        return isValidItemId(skinId) && ownedBackSkinIds.contains(skinId);
    }

    public boolean ownsFaceTheme(String themeId) {
        return isValidItemId(themeId) && ownedFaceThemeIds.contains(themeId);
    }

    public void addBackSkin(String skinId) {
        if (isValidItemId(skinId)) {
            ownedBackSkinIds.add(skinId);
        }
    }

    public void addFaceTheme(String themeId) {
        if (isValidItemId(themeId)) {
            ownedFaceThemeIds.add(themeId);
        }
    }

    public void copyFrom(PlayerProfile other) {
        if (other == null) {
            return;
        }

        applySnapshot(
                Math.max(0, other.getGold()),
                other.getSelectedBackSkinId(),
                other.getSelectedFaceThemeId(),
                other.getOwnedBackSkinIds(),
                other.getOwnedFaceThemeIds()
        );
    }

    public void restoreFromSave(long gold, String selectedBackSkinId, String selectedFaceThemeId,
            Set<String> ownedBackSkinIds, Set<String> ownedFaceThemeIds) {
        applySnapshot(gold, selectedBackSkinId, selectedFaceThemeId, ownedBackSkinIds, ownedFaceThemeIds);
    }

    private void applySnapshot(long gold, String selectedBackSkinId, String selectedFaceThemeId,
            Set<String> ownedBackSkinIds, Set<String> ownedFaceThemeIds) {
        this.gold = Math.max(0, gold);

        this.ownedBackSkinIds.clear();
        this.ownedFaceThemeIds.clear();
        this.ownedBackSkinIds.add(DEFAULT_BACK_SKIN_ID);
        this.ownedFaceThemeIds.add(DEFAULT_FACE_THEME_ID);

        if (ownedBackSkinIds != null) {
            for (String skinId : ownedBackSkinIds) {
                addBackSkin(skinId);
            }
        }
        if (ownedFaceThemeIds != null) {
            for (String themeId : ownedFaceThemeIds) {
                addFaceTheme(themeId);
            }
        }

        this.selectedBackSkinId = DEFAULT_BACK_SKIN_ID;
        this.selectedFaceThemeId = DEFAULT_FACE_THEME_ID;
        setSelectedBackSkinId(selectedBackSkinId);
        setSelectedFaceThemeId(selectedFaceThemeId);
    }

    private boolean isValidItemId(String itemId) {
        return itemId != null && !itemId.isBlank() && Objects.equals(itemId.trim(), itemId);
    }
}