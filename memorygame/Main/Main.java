package memorygame.Main;

import memorygame.controller.GameController;
import memorygame.model.*;
import memorygame.view.GameBoardPanel;
import javax.swing.*;
import java.util.*;

/**
 * Điểm khởi chạy ứng dụng, thiết lập mô hình MVC.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 1. Tạo Model
            GameModel model = new GameModel();

            // 2. Tạo dữ liệu thẻ bài ngẫu nhiên
            List<Card> cardList = new ArrayList<>();
            String[] vals = {"A", "B", "C", "D", "E", "F", "G", "H"};
            for (int i = 0; i < vals.length; i++) {
                cardList.add(new Card(i * 2, vals[i]));
                cardList.add(new Card(i * 2 + 1, vals[i]));
            }
            Collections.shuffle(cardList); // Trộn bài
            model.setCards(cardList);

            // 3. Tạo View
            GameBoardPanel view = new GameBoardPanel();

            // 4. Tạo Controller kết nối Model và View
            GameController controller = new GameController(model, view);

            // --- 5. KÍCH HOẠT XỬ LÝ SỰ KIỆN (DÒNG CỰC KỲ QUAN TRỌNG) ---
            // Nếu thiếu dòng này, nút Lưu/Tải sẽ không hoạt động
            view.setControlListeners(controller);

            // 6. Cấu hình bàn chơi lưới 4x4
            view.setupBoard(model.getCards(), 4, 4, controller);

            // 7. Hiển thị cửa sổ
            JFrame frame = new JFrame("Memory Game - IT NLU");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(view);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // 8. Bắt đầu game (Timer nhảy số - UC-06)
            controller.startGame();
        });
    }
}