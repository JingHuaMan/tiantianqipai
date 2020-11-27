package pojo.data.landlord.card;

import java.util.ArrayList;
import java.util.List;

public class HandCard extends CardSet {

    public HandCard(List<Card> cardList) {
        this.setList(cardList);
    }

    public void removeCard(PlayCard playCard) {
        for (Card h1 : playCard.getList()) {
            Card temp = null;
            for (Card h2 : this.getList()) {
                if (h1.equals(h2)) {
                    temp = h1;
                    break;
                }
            }
            this.getList().remove(temp);
        }
    }

    public static List<PlayCard> AI(PlayCard otherCard, PlayCard myCard) {
        List<PlayCard> result = new ArrayList<>();
        return result;
    }


}
