package memorygame.model;

import java.util.ArrayList;
import java.util.List;

public class GameModel {
    private List<Card> cards;
    private int score;
    private int timer; // Đếm ngược (giây)
    private Card firstSelectedCard;
    private Card secondSelectedCard;

    public GameModel() {
        this.cards = new ArrayList<>();
        this.score = 0;
        this.timer = 60;
    }

    // Logic UC6: Cập nhật điểm không để âm
    public void updateScore(int points) {
        this.score = Math.max(0, this.score + points);
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

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public Card getFirstSelected() {
        return firstSelectedCard;
    }

    public void setFirstSelected(Card card) {
        this.firstSelectedCard = card;
    }

    public Card getSecondSelected() {
        return secondSelectedCard;
    }

    public void setSecondSelected(Card card) {
        this.secondSelectedCard = card;
    }
}

