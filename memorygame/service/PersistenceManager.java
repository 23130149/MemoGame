package memorygame.service;

import memorygame.model.GameSaveData;
import java.io.*;

/**
 * Xử lý việc ghi/đọc file nhị phân (UC-10, UC-12).
 */
public class PersistenceManager {

    // UC-10: Ghi đối tượng vào bộ nhớ
    public static boolean saveGame(String filePath, GameSaveData data) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(data); // Tuần tự hóa đối tượng.
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // UC-12: Đọc đối tượng từ bộ nhớ
    public static GameSaveData loadGame(String filePath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (GameSaveData) ois.readObject(); // Giải tuần tự hóa.
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }
}