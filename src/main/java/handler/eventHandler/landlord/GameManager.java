package handler.eventHandler.landlord;

import pojo.data.landlord.Game;
import pojo.data.landlord.Room;
import pojo.data.landlord.card.PlayCard;
import pojo.data.system.User;

import java.util.HashMap;

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
        Game game = new Game(room, level * 200);
        synchronized (roomGameMap) {
            roomGameMap.put(room, game);
        }
    }

    public void playCardByUser(User user, PlayCard playCard) {
        Room room = RoomManager.getInstance().getRoomByUser(user);
        Game game;
        synchronized (roomGameMap) {
            game = roomGameMap.get(room);
        }
        int result = game.playCard(playCard);
    }

    public void endGame(Room room) {

    }
}
