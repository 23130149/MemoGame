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
    private PlayerProfile playerProfile;

    public boolean initBoard(GameSession session) {
        if (session == null || session.getStatus() != GameSession.Status.CONFIRMED) {
            return false;
        }

        DifficultyLevel level = session.getLevel();
        if (level == null || !level.validate()) {
            return false;
        }

        this.session = session;
        this.cards = generateDeck(level.getTotalPairs());

        // Tạo GameState với số lượng cặp và hint từ level
        this.gameState = new GameState(level.getTotalPairs(), level.getHintCount());

        // Set danh sách thẻ vào GameState để findMatchPair() có thể tìm
        this.gameState.setCards(this.cards);

        // Set thời gian từ level
        this.gameState.setTimeLeftSec(level.getTimeLimitSec());

        return true;
    }

    private List<Card> generateDeck(int totalPairs) {
        if (totalPairs <= 0) {
            return new ArrayList<>();
        }

        List<String> values = new ArrayList<>(totalPairs * 2);
        for (int i = 1; i <= totalPairs; i++) {
            String value = "C" + i;
            values.add(value);
            values.add(value);
        }

        Collections.shuffle(values);

        List<Card> deck = new ArrayList<>(values.size());
        for (int i = 0; i < values.size(); i++) {
            deck.add(new Card(i + 1, values.get(i)));
        }

        return deck;
    }

    public void restore(GameSession session, GameState gameState, List<Card> cards) {
        restore(session, gameState, cards, null);
    }

    public void restore(GameSession session, GameState gameState, List<Card> cards, PlayerProfile playerProfile) {
        this.session = session;
        this.gameState = gameState;
        this.cards = (cards != null) ? cards : new ArrayList<>();
        this.playerProfile = playerProfile;

        if (this.gameState != null) {
            this.gameState.setCards(this.cards);
        }
    }

    public void saveProgress(Path savePath) throws IOException {
        saveProgress(savePath, playerProfile);
    }

    public void saveProgress(Path savePath, PlayerProfile playerProfile) throws IOException {
        SaveGameService.save(savePath, session, gameState, cards, playerProfile);
    }

    public void loadProgress(Path savePath) throws IOException, ClassNotFoundException {
        LoadedGame loaded = SaveGameService.load(savePath);
        restore(loaded.getSession(), loaded.getGameState(), loaded.getCards(), loaded.getPlayerProfile());
    }

    // ===== GETTERS =====
    public GameSession getSession() {
        return session;
    }

    public GameState getGameState() {
        return gameState;
    }

    public List<Card> getCards() {
        return cards;
    }

    public PlayerProfile getPlayerProfile() {
        return playerProfile;
    }
}
