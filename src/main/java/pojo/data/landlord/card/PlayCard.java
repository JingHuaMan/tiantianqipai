package pojo.data.landlord.card;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;


public class PlayCard extends CardSet {
    public CardType cardType;

    @Getter
    private final byte[] thisBytes;

    public enum CardType {
        INVALID(0),
        SINGLE_CARD(1),
        DOUBLE_CARD(2),
        TRIPLE_CARD(3),
        TRIPLE_WITH_ONE(4),
        TRIPLE_WITH_TWO(5),
        BOMB(6),
        QUADRUPLE_WITH_TWO(7),
        QUADRUPLE_WITH_FOUR(8),
        STRAIGHT(9),
        PAIR_STRAIGHT(10),
        AIRPLANE(11),
        AIRPLANE_WITH_SINGLE(12),
        AIRPLANE_WITH_DOUBLE(13),
        KING_BOMB(14);

        private final int value;

        CardType(int value) {
            this.value = value;
        }
    }

    public PlayCard() {
        super(new ArrayList<>());
        this.thisBytes = new byte[0];
    }

    public PlayCard(byte[] input) {
        super(byteArrayToCardList(input));
        thisBytes = input;
    }

    public static List<Card> byteArrayToCardList(byte[] msg) {
        List<Card> result = new ArrayList<>();
        for (byte b : msg) {
            result.add(new Card(b));
        }
        return result;
    }

    //查询MyCard中出现频率最多的牌中的频率
    public static int maxCount(List<Card> myCard) {
        int count = 1;
        int maxCount = 0;
        int val = myCard.get(0).val;
        for (int i = 1; i < myCard.size(); i++) {
            if (myCard.get(i).val == val) {
                count++;
            } else {
                if (count >= maxCount) {
                    maxCount = count;
                }
                val = myCard.get(i).val;
                count = 1;
            }
        }
        if (count >= maxCount) {
            maxCount = count;
        }
        return maxCount;
    }

    //查询拥有最大count的最大val
    public static int valOfMaxCount(List<Card> myCard) {
        int count = 1;
        int maxCount = 0;
        int val = myCard.get(0).val;
        int maxVal = 0;
        for (int i = 1; i < myCard.size(); i++) {
            if (myCard.get(i).val == val) {
                count++;
            } else {
                if (count >= maxCount) {
                    maxCount = count;
                    maxVal = myCard.get(i - 1).val;
                }
                val = myCard.get(i).val;
                count = 1;
            }
        }
        if (count >= maxCount) {
            maxVal = myCard.get(myCard.size() - 1).val;
        }
        return maxVal;
    }

    //查询此牌值在MyCard中的出现次数
    public static int checkCount(List<Card> myCard, int val) {
        int count = 0;
        for (Card card : myCard) {
            if (card.val == val) {
                count++;
            }
        }
        return count;
    }

    //根据所给的myCard的长度来查询牌型
    public static CardType checkType(PlayCard myCard) {
        CardSet.sortCard(myCard.getList());
        CardType cardType = CardType.INVALID;
        switch (myCard.getList().size()) {
            case 1:
                cardType = CardType.SINGLE_CARD; //单牌
                break;
            case 2:
                if (maxCount(myCard.getList()) == 2) {
                    cardType = CardType.values()[2]; //对牌
                } else if (myCard.getList().get(0).val == 14 && myCard.getList().get(1).val == 15) {
                    cardType = CardType.values()[14]; //王炸
                } else {
                    cardType = CardType.values()[0];
                }
                break;
            case 3:
                if (maxCount(myCard.getList()) == 3) {
                    cardType = CardType.values()[3]; //三张
                } else {
                    cardType = CardType.values()[0];
                }
                break;
            case 4:
                if (maxCount(myCard.getList()) == 3) {
                    cardType = CardType.values()[4]; //三带1
                } else if (maxCount(myCard.getList()) == 4) {
                    cardType = CardType.values()[6]; //炸弹
                } else {
                    cardType = CardType.values()[0];
                }
                break;
            case 5:
                if (maxCount(myCard.getList()) == 3) {
                    if (checkCount(myCard.getList(), myCard.getList().get(0).val) == 2 || checkCount(myCard.getList(), myCard.getList().get(4).val) == 2) {
                        cardType = CardType.values()[5];
                    }
                } else if (checkSZ(myCard.getList())) {
                    cardType = CardType.values()[9];
                } else {
                    cardType = CardType.values()[0];
                }
                break;
            case 6:
                if (checkLD(myCard.getList())) {
                    cardType = CardType.values()[10];
                } else if (checkFJ(myCard.getList())) {
                    cardType = CardType.values()[11];
                } else if (checkSZ(myCard.getList())) {
                    cardType = CardType.values()[9];
                } else if (maxCount(myCard.getList()) == 4) {
                    cardType = CardType.values()[7];
                } else {
                    cardType = CardType.values()[0];
                }
                break;
            case 7:
                if (checkSZ(myCard.getList())) {
                    cardType = CardType.values()[9];
                } else {
                    cardType = CardType.values()[0];
                }
                break;
            case 8:
                if (maxCount(myCard.getList()) == 4) {
                    int count = 0;
                    for (int i = 0; i < myCard.getList().size(); i++) {
                        if (checkCount(myCard.getList(), myCard.getList().get(i).val) == 2) {
                            count++;
                        }
                    }
                    if (count == 4) {
                        cardType = CardType.values()[8];
                    } else {
                        cardType = CardType.values()[0];
                    }
                } else if (checkSZ(myCard.getList())) {
                    cardType = CardType.values()[9];
                } else if (checkLD(myCard.getList())) {
                    cardType = CardType.values()[10];
                } else if (checkFJ1(myCard.getList())) {
                    cardType = CardType.values()[12];
                } else {
                    cardType = CardType.values()[0];
                }
                break;
        }
        if (myCard.getList().size() >= 9) {
            if (checkLD(myCard.getList())) {
                cardType = CardType.values()[10];
            } else if (checkFJ(myCard.getList())) {
                cardType = CardType.values()[11];
            } else if (checkFJ1(myCard.getList())) {
                cardType = CardType.values()[12];
            } else if (checkFJ2(myCard.getList())) {
                cardType = CardType.values()[13];
            } else if (checkSZ(myCard.getList())) {
                cardType = CardType.values()[9];
            } else {
                cardType = CardType.values()[0];
            }
        }
        return cardType;
    }

    //检验是否为牌型是否为顺子(像这种牌型能有多种长度的，就写个方法来检验）
    public static boolean checkSZ(List<Card> myCard) {
        if (myCard.get(myCard.size() - 1).val > 14) {
            return false;
        }
        for (int i = 0; i < myCard.size() - 1; i++) {
            if (myCard.get(i).val + 1 != myCard.get(i + 1).val) {
                return false;
            }
        }
        return true;
    }

    //检验是否为连对
    public static boolean checkLD(List<Card> myCard) {
        if (myCard.get(myCard.size() - 1).val > 14 && checkCount(myCard, myCard.get(myCard.size() - 1).val) != 2) {
            return false;
        }
        for (int i = 0; i < myCard.size() - 2; i = i + 2) {
            if (checkCount(myCard, myCard.get(i).val) != 2) {
                return false;
            }
            if (myCard.get(i).val + 1 != myCard.get(i + 2).val) {
                return false;
            }
        }
        return true;
    }

    //检验是否为飞机
    public static boolean checkFJ(List<Card> myCard) {
        if (myCard.get(myCard.size() - 1).val > 14 && checkCount(myCard, myCard.get(myCard.size() - 1).val) != 3) {
            return false;
        }
        for (int i = 0; i < myCard.size() - 3; i = i + 3) {
            if (checkCount(myCard, myCard.get(i).val) != 3) {
                return false;
            }
            if (myCard.get(i).val + 1 != myCard.get(i + 3).val) {
                return false;
            }
        }
        return true;
    }

    //检验是否为飞机带单牌(这两张单牌可以为一对如JJJQQQKK）
    public static boolean checkFJ1(List<Card> myCard) {
        if (maxCount(myCard) != 3) {
            return false;
        }
        if (valOfMaxCount(myCard) > 14) {
            return false;
        }
        int i = 0;
        int count = 0;
        int single = 0;
        ArrayList<Integer> FJ = new ArrayList<>();
        while (i < myCard.size()) {
            if (checkCount(myCard, myCard.get(i).val) == 3) {
                FJ.add(myCard.get(i).val);
                i = i + 3;
                count++;
            } else {
                single++;
                i++;
            }
        }
        if (count != single) {
            return false;
        }
        for (int j = 0; j < FJ.size() - 1; j++) {
            if (FJ.get(j) != FJ.get(j + 1) - 1) {
                return false;
            }
        }
        return true;
    }

    //检验是否为飞机带对牌
    public static boolean checkFJ2(List<Card> myCard) {
        if (maxCount(myCard) != 3) {
            return false;
        }
        int i = 0;
        int count = 0;
        int single = 0;
        ArrayList<Integer> FJ = new ArrayList<>();
        while (i < myCard.size()) {
            if (checkCount(myCard, myCard.get(i).val) == 3) {
                FJ.add(myCard.get(i).val);
                i = i + 3;
                count++;
            } else if (checkCount(myCard, myCard.get(i).val) == 2) {
                i = i + 2;
                single++;
            } else {
                return false;
            }
        }
        if (single != count) {
            return false;
        }
        for (int j = 0; j < FJ.size() - 1; j++) {
            if (FJ.get(j) != FJ.get(j + 1) - 1) {
                return false;
            }
        }
        return true;
    }

    public static boolean judge(PlayCard myCards, PlayCard formalCards) {
        myCards.cardType = PlayCard.checkType(myCards);
        formalCards.cardType = PlayCard.checkType(formalCards);

        if (myCards.cardType == CardType.INVALID || formalCards.cardType == CardType.INVALID) {
            return false;
        }

        if (myCards.cardType == formalCards.cardType) {
            if (myCards.getList().size() == formalCards.getList().size()) {
                int myMax = PlayCard.valOfMaxCount(myCards.getList());
                int otherMax = PlayCard.valOfMaxCount(formalCards.getList());
                return myMax > otherMax;
            } else {
                return false;
            }
        } else {
            if (myCards.cardType == CardType.KING_BOMB) {
                return true;
            } else if (myCards.cardType == CardType.BOMB) {
                return formalCards.cardType != CardType.KING_BOMB;
            } else {
                return false;
            }
        }
    }
}
