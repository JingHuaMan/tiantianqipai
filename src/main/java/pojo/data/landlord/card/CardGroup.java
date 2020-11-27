package pojo.data.landlord.card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardGroup extends CardSet {
    private final List<Card> cardGroup = new ArrayList<>();
    private HandCard player1;
    private HandCard player2;
    private HandCard player3;
    private HandCard rest;

    public CardGroup() {
        this.createPoker();
        this.shufflePoker();
        this.slicePoker();
    }

    public HandCard getPlayer1Card() {
        return player1;
    }

    public HandCard getPlayer2Card() {
        return player2;
    }

    public HandCard getPlayer3Card() {
        return player3;
    }

    public HandCard getRestCard() {
        return rest;
    }

    public void createPoker() {
        for (int i = 0; i < 4; i++) {
            for (int j = 1; j < 14; j++) {
                Card card = new Card(j, Card.Color.values()[i]);
                this.cardGroup.add(card);
            }
        }
        Card bigJoker = new Card(15, Card.Color.DIAMOND);
        Card smallJoker = new Card(14, Card.Color.DIAMOND);
        this.cardGroup.add(bigJoker);
        this.cardGroup.add(smallJoker);
    }

    public void shufflePoker() {
        Collections.shuffle(this.cardGroup);
    }

    public void slicePoker() {
        this.player1 = new HandCard(cardGroup.subList(0, 17));
        this.player2 = new HandCard(cardGroup.subList(17, 34));
        this.player3 = new HandCard(cardGroup.subList(34, 51));
        this.rest = new HandCard(cardGroup.subList(51, 54));
    }
}
