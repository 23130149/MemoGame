package memorygame.persistence;

import memorygame.model.Card;
import memorygame.model.GameSession;
import memorygame.model.GameState;
import memorygame.model.PlayerProfile;

import java.util.List;

public class LoadedGame {
    private final GameSession session;
    private final GameState gameState;
    private final List<Card> cards;
    private final PlayerProfile playerProfile;

    public LoadedGame(GameSession session, GameState gameState, List<Card> cards) {
        this(session, gameState, cards, null);
    }

    public LoadedGame(GameSession session, GameState gameState, List<Card> cards, PlayerProfile playerProfile) {
        this.session = session;
        this.gameState = gameState;
        this.cards = cards;
        this.playerProfile = playerProfile;
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

    public PlayerProfile getPlayerProfile() {
        return playerProfile;
    }
}
