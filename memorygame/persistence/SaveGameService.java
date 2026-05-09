package memorygame.persistence;

import memorygame.model.Card;
import memorygame.model.DifficultyLevel;
import memorygame.model.GameSession;
import memorygame.model.GameState;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class SaveGameService {

    private SaveGameService() {
    }

    public static boolean hasSave(Path savePath) {
        return Files.exists(savePath) && Files.isRegularFile(savePath);
    }

    public static void deleteSave(Path savePath) throws IOException {
        Files.deleteIfExists(savePath);
    }

    public static void save(Path savePath, GameSession session, GameState state, List<Card> cards) throws IOException {
        if (session == null || state == null || cards == null || cards.isEmpty()) {
            throw new IllegalArgumentException("Dữ liệu game để lưu không hợp lệ.");
        }
        if (state.isLocked()) {
            throw new IllegalStateException("Không thể lưu khi đang chờ lật lại thẻ.");
        }

        List<CardSaveData> cardData = new ArrayList<>(cards.size());
        for (Card c : cards) {
            cardData.add(new CardSaveData(c.getId(), c.getValue(), c.getState()));
        }

        Integer firstId = state.getFirstCard() == null ? null : state.getFirstCard().getId();
        Integer secondId = state.getSecondCard() == null ? null : state.getSecondCard().getId();

        GameSaveData saveData = new GameSaveData(
                session.getPlayerId(),
                session.getLevel().getLevelId(),
                state.getScore(),
                state.getMovesCount(),
                state.getRemainingPairs(),
                state.isLocked(),
                firstId,
                secondId,
                cardData
        );

        Path parent = savePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(savePath.toFile()))) {
            out.writeObject(saveData);
        }
    }

    public static LoadedGame load(Path savePath) throws IOException, ClassNotFoundException {
        if (!hasSave(savePath)) {
            throw new FileNotFoundException("Không tìm thấy file save: " + savePath);
        }

        GameSaveData data;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(savePath.toFile()))) {
            data = (GameSaveData) in.readObject();
        }

        DifficultyLevel.Level[] levels = DifficultyLevel.Level.values();
        int idx = data.getLevelId() - 1;
        if (idx < 0 || idx >= levels.length) {
            throw new IllegalStateException("levelId trong save không hợp lệ: " + data.getLevelId());
        }

        DifficultyLevel level = new DifficultyLevel(levels[idx]);
        GameSession session = new GameSession(data.getPlayerId(), level);
        session.save();
        session.confirm();

        List<Card> cards = new ArrayList<>(data.getCards().size());
        Map<Integer, Card> byId = new HashMap<>();

        for (CardSaveData c : data.getCards()) {
            Card card = new Card(c.getId(), c.getValue());
            card.setState(c.getState());
            cards.add(card);
            byId.put(card.getId(), card);
        }

        Card first = data.getFirstCardId() == null ? null : byId.get(data.getFirstCardId());
        Card second = data.getSecondCardId() == null ? null : byId.get(data.getSecondCardId());

        GameState state = new GameState(data.getRemainingPairs());
        state.restoreState(
                data.getScore(),
                data.getMovesCount(),
                data.getRemainingPairs(),
                data.isBoardLocked(),
                first,
                second
        );

        return new LoadedGame(session, state, cards);
    }
}
