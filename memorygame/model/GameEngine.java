package memorygame.model;

import memorygame.persistence.LoadedGame;
import memorygame.persistence.SaveGameService;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameEngine {

    private GameSession session;
    private GameState gameState;
    private List<Card> cards = new ArrayList<>();

    public boolean initBoard(GameSession session) {
        if (session == null || session.getStatus() != GameSession.Status.CONFIRMED) {
            return false;
        }
        this.session = session;
        DifficultyLevel level = session.getLevel();
        this.gameState = new GameState(level.getTotalPairs());
        this.cards = generateDeck(level.getTotalPairs());
        return true;
    }

    private List<Card> generateDeck(int totalPairs) {
        List<String> values = new ArrayList<>(totalPairs * 2);
        for (int i = 1; i <= totalPairs; i++) {
            String v = "C" + i;
            values.add(v);
            values.add(v);
        }
        Collections.shuffle(values);

        List<Card> deck = new ArrayList<>(values.size());
        for (int i = 0; i < values.size(); i++) {
            deck.add(new Card(i + 1, values.get(i)));
        }
        return deck;
    }

    public void restore(GameSession session, GameState gameState, List<Card> cards) {
        this.session = session;
        this.gameState = gameState;
        this.cards = cards;
    }

    public void saveProgress(Path savePath) throws IOException {
        SaveGameService.save(savePath, session, gameState, cards);
    }

    public void loadProgress(Path savePath) throws IOException, ClassNotFoundException {
        LoadedGame loaded = SaveGameService.load(savePath);
        restore(loaded.getSession(), loaded.getGameState(), loaded.getCards());
    }

    public GameSession getSession() { return session; }
    public GameState getGameState() { return gameState; }
    public List<Card> getCards() { return cards; }
}
