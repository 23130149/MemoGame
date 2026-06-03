package memorygame.persistence;

import memorygame.model.Card;
import memorygame.model.GameSession;
import memorygame.model.GameState;

import java.util.List;

public class LoadedGame {
    private final GameSession session;
    private final GameState gameState;
    private final List<Card> cards;

    public LoadedGame(GameSession session, GameState gameState, List<Card> cards) {
        this.session = session;
        this.gameState = gameState;
        this.cards = cards;
    }

    public GameSession getSession() {
        return session;
    }

    public GameState getGameState() {
        return gameState;
    }

    public List<Card> getCards() {
        return cards;
    }
}
