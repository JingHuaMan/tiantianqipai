package handler.eventHandler.landlord;

import config.Constants;
import pojo.data.landlord.Room;
import pojo.data.system.User;
import util.database.DatabaseUtil;

import java.sql.SQLException;
import java.util.*;

public class RoomManager {

    public static RoomManager instance = new RoomManager();

    private final List<Queue<Room>> roomWithSpace;

    private final HashMap<Room, Integer> roomWaitForGame;

    private final HashSet<Room> roomOnGame;

    private final HashMap<User, Room> userRoomMap;

    private final HashMap<Room, Integer> roomLevelMap;

    private RoomManager() {
        roomWithSpace = new ArrayList<>();
        for (int i = 0; i < Constants.ROOM_LEVEL_NUM; i++) {
            roomWithSpace.add(new LinkedList<>());
        }
        roomWaitForGame = new HashMap<>();
        roomOnGame = new HashSet<>();
        userRoomMap = new HashMap<>();
        roomLevelMap = new HashMap<>();
    }

    public static synchronized RoomManager getInstance() {
        if (instance == null) {
            instance = new RoomManager();
        }
        return instance;
    }

    public Room getAvailableRoom(User user, int level) throws SQLException {
        int currentBeanNum = DatabaseUtil.getInstance().getBeanNum(user.getId());
        if (currentBeanNum < getBeanCostByLevel(level)) {
            return null;
        }
        Room room;
        if (level >= Constants.ROOM_LEVEL_NUM) {
            return null;
        }
        synchronized (roomWithSpace) {
            if (roomWithSpace.size() == 0) {
                room = new Room();
                room.addUser(user);
                roomWithSpace.get(level).add(room);
                roomLevelMap.put(room, level);
            } else {
                room = roomWithSpace.get(level).peek();
                if (room != null && room.addUser(user)) {
                    roomWithSpace.get(level).poll();
                    synchronized (roomWaitForGame) {
                        roomWaitForGame.put(room, 0);
                    }
                }
            }
        }
        synchronized (userRoomMap) {
            userRoomMap.put(user, room);
        }
        return room;
    }

    // true means the num of users waiting for game reaches 3, the game starts automatically
    public boolean userWaitForGame(User user) throws SQLException {
        Room room = userRoomMap.get(user);
        synchronized (roomWaitForGame) {
            int numOfRoommate = roomWaitForGame.get(room) + 1;
            if (numOfRoommate == 3) {
                roomWaitForGame.remove(room);
                roomOnGame.add(room);
                startGameFromRoom(room);
                return true;
            } else {
                roomWaitForGame.put(room, numOfRoommate);
                return false;
            }
        }
    }

    private void startGameFromRoom(Room room) throws SQLException {
        int level = roomLevelMap.get(room);
        for (User user: room.getUsers()) {
            DatabaseUtil.getInstance().updateBeanNum(user.getId(), getBeanCostByLevel(roomLevelMap.get(room)));
        }
        GameManager.getInstance().startGameByRoom(room, level);
    }

    public Room getRoomByUser(User user) {
        return this.userRoomMap.get(user);
    }

    private int getBeanCostByLevel(int level) {
        return level * 50;
    }

}
