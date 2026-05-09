package memorygame.service;

import memorygame.model.GameSaveData;
import java.io.*;

public class PersistenceManager {
    public static boolean saveGame(String filePath, GameSaveData data) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(data);
            return true;
        } catch (IOException e) {
            System.err.println("Lỗi khi lưu game: " + e.getMessage());
            return false;
        }
    }

    public static GameSaveData loadGame(String filePath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (GameSaveData) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }
}