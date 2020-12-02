package pojo.data.landlord.card;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HandCard extends CardSet {

    public HandCard(List<Card> cardList) {
        this.setList(cardList);
    }

    public HandCard(){
        this.setList(new ArrayList<>());
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
        CardSet.sortCard(this.getList());
    }

    public void addRest(HandCard rest){
        for(Card c: rest.getList()){
            this.add(c);
        }
    }

    public PlayCard getAvailablePlayCard(PlayCard lastCard){
        PlayCard.CardType type = PlayCard.checkType(lastCard);
        PlayCard res = new PlayCard();

        //检测是否有王炸
        boolean bigJoker = false;
        boolean smallJoker = false;

        for(Card c: this.getList()){
            if(c.val == 14){
                smallJoker = true;
            }

            if(c.val == 15){
                bigJoker = true;
            }
        }

        if(smallJoker && bigJoker){
            res.add(new Card(14, Card.Color.DIAMOND));
            res.getList().add(new Card(15, Card.Color.DIAMOND));
            return res;
        }

        //如果上家的牌不是炸弹则检测自己的手牌是否有炸弹
        if(type != PlayCard.CardType.BOMB){
            for(Card c: this.getList()){
                if(PlayCard.checkCount(this.getList(), c.val) == 4){
                    this.findCard(c.val, res);
                    return res;
                }
            }
        }

        switch (type){
            case SINGLE_CARD:
                for(Card c: this.getList()){
                    if(c.val > lastCard.getList().get(0).val){
                        res.add(c);
                        return res;
                    }
                }
            case DOUBLE_CARD:
                List<Integer> D_C_SUP = this.getSameCard(2, lastCard.getList().get(0).val);
                List<Integer> D_C_T_SUP = this.getSameCard(3, lastCard.getList().get(0).val);
                if(D_C_SUP.size()!=0){
                    this.findCard(D_C_SUP.get(0), res);
                }
                else{
                    if(D_C_T_SUP.size()!=0){
                        for(Card c: this.getList()){
                            if(c.val == D_C_T_SUP.get(0)){
                                res.add(c);
                            }
                            if(res.getList().size()==2){
                                return res;
                            }
                        }
                    }
                }
                return res;
            case TRIPLE_CARD:
                List<Integer> T_C_SUP = this.getSameCard(3, lastCard.getList().get(0).val);
                if(T_C_SUP.size()!=0){
                    this.findCard(T_C_SUP.get(0), res);
                }
                return res;
            case TRIPLE_WITH_ONE:
                List<Integer> T_W_O_SUP = this.getSameCard(3, lastCard.getList().get(1).val);
                if(T_W_O_SUP.size()!=0){
                    for(Card c: this.getList()){
                        if(c.val != T_W_O_SUP.get(0)){
                            res.add(c);
                            this.findCard(T_W_O_SUP.get(0), res);
                            break;
                        }
                    }
                }
                return res;
            case TRIPLE_WITH_TWO:
                List<Integer> T_W_T_SUP = this.getSameCard(3, lastCard.getList().get(2).val);
                if(T_W_T_SUP.size()!=0){
                    List<Integer> T_W_T_D = this.getSameCard(2,0);
                    if(T_W_T_D.size()!=0){
                        this.findCard(T_W_T_SUP.get(0), res);
                        this.findCard(T_W_T_D.get(0), res);
                    }
                    else{
                        if(T_W_T_SUP.size()>1){
                            this.findCard(T_W_T_SUP.get(0), res);
                            for(Card c: this.getList()){
                                if(c.val == T_W_T_SUP.get(1)){
                                    res.add(c);
                                }
                                if(res.getList().size()==5){
                                    return res;
                                }
                            }
                        }
                    }
                }
                return res;
            case BOMB:
                List<Integer> B_SUP = this.getSameCard(4, lastCard.getList().get(0).val);
                if(B_SUP.size()!=0){
                    this.findCard(B_SUP.get(0), res);
                }
                return res;
            case QUADRUPLE_WITH_TWO:
            case QUADRUPLE_WITH_FOUR:
                List<Integer> Q_W_T_SUP = this.getSameCard(4, 0);
                if(Q_W_T_SUP.size()!=0){
                    this.findCard(Q_W_T_SUP.get(0), res);
                }
                return res;
            case STRAIGHT:
                int length = lastCard.getList().size();
                int start = lastCard.getList().get(0).val + 1;

                if(lastCard.getList().get(length-1).val >= 12){
                    return res;
                }

                int[] S_SUP = new int[13];
                for(Card c: this.getList()){
                    if(c.val <= 12){
                        S_SUP[c.val] = 1;
                    }
                }

                for(int i = start; i < 14 - length; i++){
                    boolean find = true;
                    for(int j = 0; j < length; j++){
                        if (S_SUP[i + j] == 0){
                            find = false;
                            break;
                        }
                    }
                    if(find){
                        for(int k = 0; k < length; k++){
                            for(Card c: this.getList()){
                                if(c.val == i + k){
                                    res.add(c);
                                    break;
                                }
                            }
                        }
                        return res;
                    }
                }
                return res;
            case PAIR_STRAIGHT:
                int pairLength = lastCard.getList().size() / 2;
                int pairStart = lastCard.getList().get(0).val + 1;

                if(lastCard.getList().get(pairLength-1).val >= 12){
                    return res;
                }

                int[] P_SUP = new int[13];

                for(int i = 0; i < this.getList().size()-1; i++){
                    if(this.getList().get(i).val <= 12 && this.getList().get(i).val == this.getList().get(i+1).val){
                        P_SUP[this.getList().get(i).val] = 1;
                    }
                }

                for(int i = pairStart; i < 14 - pairLength; i++){
                    boolean find = true;
                    for(int j = 0; j < pairLength; j++){
                        if(P_SUP[i + j] == 0){
                            find = false;
                            break;
                        }
                    }
                    if(find){
                        for(int k = 0; k < pairLength; k++){
                            int count = 0;
                            for(Card c: this.getList()){
                                if(c.val == i + k){
                                    res.add(c);
                                    count++;
                                }
                                if(count == 2){
                                    break;
                                }
                            }
                        }
                        return res;
                    }
                }
                return res;
            case AIRPLANE:
                List<Integer> A_SUP = this.getSameCard(3, lastCard.getList().get(3).val);
                if(A_SUP.size() < 1){
                    return res;
                }
                else{
                    for(int i = 0; i < A_SUP.size() - 1; i++){
                        if(A_SUP.get(i) == A_SUP.get(i + 1) - 1){
                            this.findCard(A_SUP.get(i), res);
                            this.findCard(A_SUP.get(i + 1), res);
                            return res;
                        }
                    }
                }
            case AIRPLANE_WITH_SINGLE:
                if(this.getList().size() < 8){
                    return res;
                }
                List<Integer> A_W_S_SUP = this.getSameCard(3, lastCard.getList().get(3).val);
                if(A_W_S_SUP.size() < 1){
                    return res;
                }
                else{
                    for(int i = 0; i < A_W_S_SUP.size() - 1; i++){
                        if(A_W_S_SUP.get(i) == A_W_S_SUP.get(i + 1) - 1){
                            this.findCard(A_W_S_SUP.get(i), res);
                            this.findCard(A_W_S_SUP.get(i + 1), res);
                            for(Card c: this.getList()){
                                if(c.val != A_W_S_SUP.get(i) && c.val != A_W_S_SUP.get(i+1)){
                                    res.add(c);
                                }
                                if(res.getList().size()==8){
                                    return res;
                                }
                            }
                        }
                    }
                }
                return res;
            case AIRPLANE_WITH_DOUBLE:
                if(this.getList().size() < 10){
                    return res;
                }
                List<Integer> A_W_D_SUP = this.getSameCard(3, lastCard.getList().get(4).val);
                List<Integer> D_S = this.getSameCard(2, 0);
                if(A_W_D_SUP.size() < 1){
                    return res;
                }
                else{
                    for(int i = 0; i < A_W_D_SUP.size() - 1; i++){
                        if(A_W_D_SUP.get(i) == A_W_D_SUP.get(i + 1) - 1){
                            int dSize = D_S.size() + A_W_D_SUP.size();
                            if(dSize > 3){
                                this.findCard(A_W_D_SUP.get(i), res);
                                this.findCard(A_W_D_SUP.get(i + 1), res);
                                for(Card c: this.getList()){
                                    if(c.val != A_W_D_SUP.get(i) && c.val != A_W_D_SUP.get(i+1)){
                                        if(PlayCard.checkCount(this.getList(), c.val) == 2){
                                            this.findCard(c.val, res);
                                        }
                                        if(PlayCard.checkCount(this.getList(), c.val) == 3){
                                            int ctime = 0;
                                            for(Card d: this.getList()){
                                                if(d.val == c.val){
                                                    res.add(c);
                                                    ctime++;
                                                }
                                                if(ctime==2){
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            case KING_BOMB:
                return res;
        }
        return res;
    }

    public List<Integer> getSameCard(int k, int lastVal){
        List<Integer> sup = new ArrayList<>();
        for(Card c: this.getList()){
            if(c.val > lastVal && PlayCard.checkCount(this.getList(), c.val) == k && !sup.contains(c.val)){
                sup.add(c.val);
            }
        }
        return sup;
    }

    public void findCard(int val, PlayCard res){
        for(Card c: this.getList()){
            if(c.val == val){
                res.add(c);
            }
        }
    }


}
