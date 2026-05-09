package memorygame.model;
public class GameEngine {

    private GameSession session;
    private GameState   gameState;

    public boolean initBoard(GameSession session) {
        if (session == null || session.getStatus() != GameSession.Status.CONFIRMED) {
            return false;
        }
        this.session   = session;
        DifficultyLevel level = session.getLevel();
        this.gameState = new GameState(level.getTotalPairs());
        return true;
    }

    public void startGame(GameSession session) {
        if (this.session == null || this.gameState == null) {
            throw new IllegalStateException("Gọi initBoard() trước khi startGame().");
        }
        System.out.println("Trò chơi bắt đầu – cấp độ: " + session.getLevel().getLevelName()
                + " | Cặp thẻ: " + session.getLevel().getTotalPairs());
    }

    public GameState getGameState() { return gameState; }
}