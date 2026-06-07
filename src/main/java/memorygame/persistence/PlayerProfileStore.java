package memorygame.persistence;

import memorygame.model.PlayerProfile;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

// UC-15 - Le VietKhanh: Lưu riêng vàng và vật phẩm cửa hàng để người chơi không mất sau khi tắt game.
public final class PlayerProfileStore {

    private static final Path DEFAULT_PROFILE_FILE = Paths.get("player_profile.dat");

    private PlayerProfileStore() {
    }

    public static void loadDefault(PlayerProfile target) {
        load(DEFAULT_PROFILE_FILE, target);
    }

    public static void saveDefault(PlayerProfile profile) {
        try {
            save(DEFAULT_PROFILE_FILE, profile);
        } catch (IOException ex) {
            System.err.println("[PlayerProfileStore] Không thể lưu hồ sơ người chơi: " + ex.getMessage());
        }
    }

    public static void load(Path profilePath, PlayerProfile target) {
        if (profilePath == null || target == null || !Files.exists(profilePath)) {
            return;
        }

        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(profilePath))) {
            Object data = in.readObject();
            if (data instanceof PlayerProfile profile) {
                target.copyFrom(profile);
            }
        } catch (IOException | ClassNotFoundException ex) {
            System.err.println("[PlayerProfileStore] Không thể tải hồ sơ người chơi: " + ex.getMessage());
        }
    }

    public static void save(Path profilePath, PlayerProfile profile) throws IOException {
        if (profilePath == null || profile == null) {
            return;
        }

        Path parent = profilePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(profilePath))) {
            out.writeObject(profile);
        }
    }
}
