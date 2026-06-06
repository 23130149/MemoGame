package memorygame.model;

import java.util.ArrayList;
import java.util.List;

public class GameModel {

    private List<Card> cards;
    private int score;
    private int timer;
    private int movesCount;
    private int remainingPairs;
    private double scoreMultiplier;
    private LevelConfig level;
    private Card firstSelectedCard;
    private Card secondSelectedCard;

    public GameModel() {
        this.cards = new ArrayList<>();
        this.score = 0;
        this.timer = 60;
        this.movesCount = 0;
        this.scoreMultiplier = 1.0;
        this.level = LevelConfig.DE; // Mặc định cấp Dễ
    }

    // UC-06: Thiết lập cấp độ, cập nhật timer và hệ số điểm
    public void setLevel(LevelConfig level) {
        this.level = level;
        this.timer = level.getTimeSeconds();
        this.scoreMultiplier = level.getScoreMultiplier();
    }

    // UC-06: Cập nhật điểm theo hệ số cấp độ, không âm
    public void updateScore(int basePoints) {
        int pts = (int) (basePoints * scoreMultiplier);
        this.score = Math.max(0, this.score + pts);
    }

    // UC-06: Reset điểm về 0
    public void resetScore() {
        this.score = 0;
        this.movesCount = 0;
    }

    // UC-06: Reset timer theo cấp độ hiện tại
    public void resetTimer() {
        this.timer = (level != null) ? level.getTimeSeconds() : 60;
    }

    // UC-04: Tăng số lượt đi
    public void incrementMoves() {
        this.movesCount++;
    }

    // Getters & Setters
    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getTimer() {
        return timer;
    }

    public void setTimer(int timer) {
        this.timer = timer;
    }

    public double getScoreMultiplier() {
        return scoreMultiplier;
    }

    public LevelConfig getLevel() {
        return level;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
        this.remainingPairs = cards.size() / 2;
    }

    public int getRemainingPairs() {
        return remainingPairs;
    }

    public void decrementRemainingPairs() {
        this.remainingPairs--;
    }

    public int getMovesCount() {
        return movesCount;
    }

    public void setMovesCount(int moves) {
        this.movesCount = moves;
    }

    public Card getFirstSelected() {
        return firstSelectedCard;
    }

    public void setFirstSelected(Card c) {
        this.firstSelectedCard = c;
    }

    public Card getSecondSelected() {
        return secondSelectedCard;
    }

    public void setSecondSelected(Card c) {
        this.secondSelectedCard = c;
    }

    public void resetTurn() {
        this.firstSelectedCard = null;
        this.secondSelectedCard = null;
    }
}