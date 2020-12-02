package handler.eventHandler.landlord;

import config.Constants;
import lombok.AllArgsConstructor;
import pojo.data.landlord.Room;
import pojo.data.system.User;
import util.database.DatabaseUtil;

import java.sql.SQLException;
import java.util.*;

public class RoomManager {

    private static RoomManager instance = new RoomManager();

    private final List<Queue<Room>> roomWithSpace;

    private final HashMap<Room, Integer> roomWaitForGame;

    private final HashMap<Room, Pair> roomWaitForGameEnd;

    private final HashMap<User, Room> userRoomMap;

    private final HashMap<Room, Integer> roomLevelMap;

    private RoomManager() {
        roomWithSpace = new ArrayList<>();
        for (int i = 0; i < Constants.ROOM_LEVEL_NUM; i++) {
            roomWithSpace.add(new LinkedList<>());
        }
        roomWaitForGame = new HashMap<>();
        roomWaitForGameEnd = new HashMap<>();
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
                startGameFromRoom(room);
                return true;
            } else {
                roomWaitForGame.put(room, numOfRoommate);
                return false;
            }
        }
    }

    public void endGameByRoom(Room room) {
        synchronized (roomWaitForGameEnd) {
            roomWaitForGameEnd.put(room, new Pair(0, 0));
        }
    }

    // 0 means just end game, 1 means end game with a new game, 2 means new game already start, 3 means everyone leaves
    public int userEndGame(User user, boolean stayOrLeave) {
        Room room;
        synchronized (userRoomMap) {
            room = userRoomMap.get(user);
            if (!stayOrLeave) {
                userRoomMap.remove(user);
            }
        }
        synchronized (roomWaitForGameEnd) {
            Pair lastPair = roomWaitForGameEnd.get(room);
            lastPair.totalNum++;
            if (stayOrLeave) {
                lastPair.numForStay++;
            } else {
                room.removeUser(user);
            }
            if (lastPair.totalNum == 3) {
                roomWaitForGameEnd.remove(room);
                if (lastPair.numForStay == 1 || lastPair.numForStay == 2) {
                    synchronized (roomWithSpace) {
                        roomWithSpace.get(roomLevelMap.get(room)).add(room);
                    }
                    return 1;
                } else if (lastPair.numForStay == 3) {
                    roomWaitForGame.put(room, 0);
                    return 2;
                } else {
                    synchronized (roomLevelMap) {
                        roomLevelMap.remove(room);
                    }
                    return 3;
                }
            }
        }
        return 0;
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

    @AllArgsConstructor
    private static class Pair{
        int totalNum;
        int numForStay;
    }

}
