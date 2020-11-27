package pojo.data.landlord;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import pojo.data.landlord.card.CardSet;
import pojo.data.landlord.card.HandCard;
import pojo.data.landlord.card.PlayCard;
import pojo.data.system.User;
import util.LandlordUtil;

import java.util.*;

@EqualsAndHashCode(exclude = "id")
public class Game {

    // for hash
    @Getter
    private final UUID id;

    private final Room room;

    // get hand card by user
    @Getter
    private final HashMap<User, HandCard> userHandCardMap;

    // users take turns to play cards
    private final UserLoop userLoop;

    // temp landlord and final landlord
    private User landlord;

    // the value should be 0 1 2 3
    private int tempMaxLandlordValue;

    // num of users call for landlord
    private int amountCallForLandlord;

    private User lastPlayUser;

    private PlayCard lastPlayCard;

    @Getter
    private int basePoint;

    // false means farmers win
    private boolean isLandlordWin;

    public Game(Room room, int basePoint) {
        this.id = UUID.randomUUID();
        this.room = room;
        this.userHandCardMap = new HashMap<>();
        CardSet[] sets = LandlordUtil.dealCards();
        for (int i = 0; i < 3; i++) {
            this.userHandCardMap.put(room.getUsers().get(i), (HandCard) sets[i]);
        }
        this.userHandCardMap.put(new User(-1), (HandCard) sets[3]);
        this.userLoop = new UserLoop(room.getUsers());
        this.landlord = null;
        this.tempMaxLandlordValue = 0;
        this.amountCallForLandlord = 0;
        this.lastPlayUser = null;
        this.lastPlayCard = null;
        this.basePoint = basePoint;
    }

    // 0 means nothing happen, 1 means landlord is set, 2 means a new round is needed
    public int callForLandlord(User user, int value) {
        if (!room.getUsers().contains(user)) {
            return 0;
        }
        this.amountCallForLandlord++;
        if (value > this.tempMaxLandlordValue) {
            this.landlord = user;
            this.tempMaxLandlordValue = value;
            if (value >= 3 || this.amountCallForLandlord >= 3) {
                userLoop.setPointer(user);
                return 1;
            }
        }
        if (this.amountCallForLandlord >= 3) {
            this.landlord = null;
            this.tempMaxLandlordValue = 0;
            this.amountCallForLandlord = 0;
            return 2;
        }
        return 0;
    }

    // 0 means invalid, 1 means valid, 2 means game finishes
    public int playCard(PlayCard tempCard) {
        User tempUser = this.userLoop.getNext();
        HandCard userCard = this.userHandCardMap.get(tempUser);
        if (!LandlordUtil.checkCardsValid(tempCard) || !LandlordUtil.cardSetContains(tempCard, userCard)) {
            return 0;
        }
        if (this.lastPlayCard == null || this.lastPlayUser == tempUser || LandlordUtil.compareCards(tempCard, this.lastPlayCard)) {
            this.lastPlayUser = tempUser;
            this.lastPlayCard = tempCard;
            userCard.removeCard(tempCard);
            PlayCard.CardType type = PlayCard.checkType(tempCard.getList());
            if (type == PlayCard.CardType.BOMB || type == PlayCard.CardType.KING_BOMB) {
                this.basePoint *= 2;
            }
            if (userCard.getList().size() == 0) {
                isLandlordWin = tempUser.equals(this.landlord);
                return 2;
            } else {
                return 1;
            }
        }
        return 0;
    }

    public Map<User, Integer> getResult() {
        Map<User, Integer> userPoints = new HashMap<>();
        for (User user : room.getUsers()) {
            if (user.equals(landlord)) {
                userPoints.put(user, 2 * basePoint * (this.isLandlordWin ? 1 : -1));
            } else {
                userPoints.put(user, basePoint * (this.isLandlordWin ? -1 : 1));
            }
        }
        return userPoints;
    }

    private static class UserLoop{

        List<User> loop = new ArrayList<>();

        int pointer;

        UserLoop(List<User> users) {
            loop.addAll(users);
            Collections.shuffle(users);
            pointer = 0;
        }

        void setPointer(User user) {
            for (int i = 0; i < 3; i++) {
                if (loop.get(i).equals(user)) {
                    pointer = i;
                    return;
                }
            }
        }

        User getNext() {
            User next = loop.get(pointer++);
            if (pointer >= 3) {
                pointer = 0;
            }
            return next;
        }
    }
}
