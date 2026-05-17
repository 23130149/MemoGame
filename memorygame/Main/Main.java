package memorygame.Main;

import memorygame.controller.GameController;
import memorygame.model.Card;
import memorygame.model.CardState;
import memorygame.model.GameModel;
import memorygame.model.LevelConfig;
import memorygame.view.GameBoardPanel;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            // 1. Chọn cấp độ
            LevelConfig level = chooseLevel();
            if (level == null) return; // Người dùng bấm Cancel

            // 2. Khởi tạo Model
            GameModel model = new GameModel();
            model.setLevel(level);

            // 3. Tạo danh sách thẻ theo số cặp của cấp độ
            List<Card> cards = createCards(level.getTotalPairs());
            model.setCards(cards);

            // 4. Khởi tạo View
            GameBoardPanel view = new GameBoardPanel(model);

            // 5. Khởi tạo Controller
            GameController controller = new GameController(model, view);
            view.setController(controller);

            // 6. Tạo JFrame
            JFrame frame = new JFrame("Memory Card – " + level.getDisplayName());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(view);
            frame.setSize(700, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // 7. Bắt đầu game
            controller.startGame(level);
        });
    }

    // Hộp thoại chọn cấp độ
    private static LevelConfig chooseLevel() {
        String[] options = {
                "Dễ (4×4, 120s, ×1.0)",
                "Trung bình (6×6, 90s, ×1.5)",
                "Khó (8×8, 60s, ×2.0)"
        };
        int choice = JOptionPane.showOptionDialog(
                null,
                "Chọn cấp độ:",
                "Memory Card – Chọn Cấp Độ",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );
        return switch (choice) {
            case 0 -> LevelConfig.DE;
            case 1 -> LevelConfig.TRUNG_BINH;
            case 2 -> LevelConfig.KHO;
            default -> null;
        };
    }

    // Tạo danh sách thẻ và xáo trộn
    private static List<Card> createCards(int totalPairs) {
        String[] allValues = {
                "🍎","🍌","🍇","🍓","🍒","🍑",
                "🥝","🍉","🍋","🍊","🍍","🥭",
                "🍆","🥑","🌽","🥕","🍄","🧅",
                "🐶","🐱","🐭","🐹","🐰","🦊",
                "🐻","🐼","🐨","🐯","🦁","🐸",
                "🌸","🌺","🌻","🌹","🌷","🍀"
        };

        List<Card> cards = new ArrayList<>();
        int id = 0;
        for (int i = 0; i < totalPairs; i++) {
            String value = allValues[i % allValues.length];
            cards.add(new Card(id++, value));
            cards.add(new Card(id++, value));
        }

        Collections.shuffle(cards);
        return cards;
    }
}