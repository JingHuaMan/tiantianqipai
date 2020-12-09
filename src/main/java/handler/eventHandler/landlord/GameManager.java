package handler.eventHandler.landlord;

import handler.eventHandler.system.BeanAndPropsManager;
import lombok.extern.slf4j.Slf4j;
import pojo.data.landlord.Game;
import pojo.data.landlord.Room;
import pojo.data.landlord.card.PlayCard;
import pojo.data.system.User;
import util.database.DatabaseUtil;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class GameManager {

    private static GameManager instance;

    // game on play
    private final HashMap<Room, Game> roomGameMap;

    private GameManager() {
        roomGameMap = new HashMap<>();
    }

    public synchronized static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    public void startGameByRoom(Room room, int level) {
        Game game = new Game(room, level);
        synchronized (roomGameMap) {
            roomGameMap.put(room, game);
        }
    }

    public int playCardByUser(User user, PlayCard playCard) {
        Room room = RoomManager.getInstance().getRoomByUser(user);
        Game game;
        synchronized (roomGameMap) {
            game = roomGameMap.get(room);
        }
        return game.playCard(playCard);
    }

    public Map<User, Integer> getGameResult(Room room) {
        Game game = roomGameMap.get(room);
        Map<User, Integer> result = game.getResult();
        for (Map.Entry<User, Integer> entry: result.entrySet()) {
            if (entry.getValue() > 0) {
                BeanAndPropsManager.getInstance().addBeans(entry.getKey(), entry.getValue());
            } else {
                BeanAndPropsManager.getInstance().spendBeans(entry.getKey(), entry.getValue());
            }
        }
        synchronized (roomGameMap) {
            roomGameMap.remove(room);
        }
        RoomManager.getInstance().endGameByRoom(room);
        return result;
    }

    public Game getGameByUser(User user) {
        Room room = RoomManager.getInstance().getRoomByUser(user);
        Game game;
        synchronized (roomGameMap) {
            game = roomGameMap.get(room);
        }
        return game;
    }
}
