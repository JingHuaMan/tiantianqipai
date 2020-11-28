package handler.eventHandler.landlord;

import lombok.Getter;
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
    @Getter
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
        Game game = new Game(room, level * 100);
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

    public Map<User, Integer> endGame(Room room) {
        Game game = roomGameMap.get(room);
        Map<User, Integer> result = game.getResult();
        try {
            for (Map.Entry<User, Integer> entry: result.entrySet()) {
                DatabaseUtil.getInstance().updateBeanNum(entry.getKey().getId(), entry.getValue());
            }
        } catch (SQLException e) {
            log.error("SQLException when updating the bean nums");
        }
        synchronized (roomGameMap) {
            roomGameMap.remove(room);
        }
        return result;
    }
}
