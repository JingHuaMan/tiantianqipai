package pojo.data.landlord.card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardGroup extends CardSet {
    private final List<Card> cardGroup = new ArrayList<>();
    private final HandCard player1 = new HandCard();
    private final HandCard player2 = new HandCard();
    private final HandCard player3 = new HandCard();
    private final HandCard rest = new HandCard();

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
        for(int i = 0; i < 17; i++){
            this.player1.add(cardGroup.get(i));
        }

        for(int i = 17; i < 34; i++){
            this.player2.add(cardGroup.get(i));
        }

        for(int i = 34; i < 51; i++){
            this.player3.add(cardGroup.get(i));
        }

        for(int i = 51; i < 54; i++){
            this.rest.add(cardGroup.get(i));
        }

    }
}
