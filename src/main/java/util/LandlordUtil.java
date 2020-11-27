package util;


import pojo.data.landlord.card.*;


public class LandlordUtil {

    public static CardSet[] dealCards() {
        CardSet[] result = new CardSet[4];
        CardGroup cardGroup = new CardGroup();
        result[0] = cardGroup.getPlayer1Card();
        result[1] = cardGroup.getPlayer2Card();
        result[2] = cardGroup.getPlayer3Card();
        result[3] = cardGroup.getRestCard();
        return result;
    }

    public static boolean compareCards(PlayCard playCard, PlayCard lastCard) {
        return PlayCard.judge(playCard, lastCard);
    }

    public static boolean cardSetContains(PlayCard playCard, HandCard myCard) {
        for (Card card1 : playCard.getList()) {
            boolean find = false;
            for (Card card : myCard.getList()) {
                if (card1.equals(card)) {
                    find = true;
                    break;
                }
            }
            if (!find) {
                return false;
            }
        }
        return false;
    }

    public static boolean checkCardsValid(PlayCard cards) {
        return PlayCard.checkType(cards.getList()) != PlayCard.CardType.INVALID;
    }

}

