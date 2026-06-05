package memorygame.controller;

import memorygame.model.Card;
import memorygame.model.CardState;

public class CardFlipController {
    public void flipCardFaceUp(Card card) {
        if (card.isFaceDown()) {
            card.setState(CardState.FACE_UP);
        }
    }

    public void flipCardFaceDown(Card card) {
        if (!card.isMatched()) {
            card.setState(CardState.FACE_DOWN);
        }
    }
}