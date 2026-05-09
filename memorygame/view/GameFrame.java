package memorygame.view;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class GameFrame extends JFrame {
    private JLabel lblScore = new JLabel("Score: 0");
    private JLabel lblTimer = new JLabel("Time: 60s");
    private JButton btnSave = new JButton("Save Game");
    private JButton btnTestMatch = new JButton("Test Match (+10)"); // Để test UC6

    public GameFrame() {
        setTitle("Memory Game Testing");
        setSize(400, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        add(lblScore);
        add(lblTimer);
        add(btnTestMatch);
        add(btnSave);
    }

    public void updateUI(int score, int time) {
        lblScore.setText("Score: " + score);
        lblTimer.setText("Time: " + time + "s");
    }

    public void addSaveListener(ActionListener al) { btnSave.addActionListener(al); }
    public void addTestMatchListener(ActionListener al) { btnTestMatch.addActionListener(al); }
}