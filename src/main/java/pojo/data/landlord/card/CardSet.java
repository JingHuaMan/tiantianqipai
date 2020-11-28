package pojo.data.landlord.card;

import java.util.ArrayList;
import java.util.List;

public class CardSet {
    private List<Card> cardList = new ArrayList<>();

    public void add(Card card) {
        this.cardList.add(card);
    }

    public List<Card> getList() {
        return cardList;
    }

    public CardSet() {

    }

    public CardSet(List<Card> cardList) {
        this.cardList = cardList;
    }

    public void setList(List<Card> cardList) {
        this.cardList = cardList;
    }

    public static void sortCard(List<Card> cardList) {
        cardList.sort((o1, o2) -> {
            int diff = o1.val - o2.val;
            if (diff == 0) {
                return o1.color.getValue() - o2.color.getValue();
            } else if (diff > 0) {
                return 1;
            } else {
                return -1;
            }
        });
    }

    public byte[] toByteArray() {
        byte[] result = new byte[cardList.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = cardList.get(i).toByte();
        }
        return result;
    }
}
