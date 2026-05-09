package memorygame.Main;

import memorygame.controller.GameController;
import memorygame.model.GameModel;
import memorygame.view.GameFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameModel model = new GameModel();
            GameFrame view = new GameFrame();
            GameController controller = new GameController(model, view);

            view.setVisible(true);
            controller.startGame();
        });

    }
}
