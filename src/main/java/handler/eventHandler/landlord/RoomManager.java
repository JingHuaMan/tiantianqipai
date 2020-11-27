package handler.eventHandler.landlord;

import lombok.Getter;
import pojo.data.landlord.Room;
import pojo.data.system.User;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class RoomManager {

    public static RoomManager instance = new RoomManager();

    private final Queue<Room> roomWithSpace;

    private final HashMap<Room, Integer> roomWaitForGame;

    private final HashSet<Room> roomOnGame;

    private final HashMap<User, Room> userRoomMap;

    private RoomManager() {
        roomWithSpace = new LinkedList<>();
        roomWaitForGame = new HashMap<>();
        roomOnGame = new HashSet<>();
        userRoomMap = new HashMap<>();
    }

    public static synchronized RoomManager getInstance() {
        if (instance == null) {
            instance = new RoomManager();
        }
        return instance;
    }

    public Room getAvailableRoom(User user) {
        Room room;
        synchronized (roomWithSpace) {
            if (roomWithSpace.size() == 0) {
                room = new Room();
                room.addUser(user);
                roomWithSpace.add(room);
            } else {
                room = roomWithSpace.peek();
                if (room.addUser(user)) {
                    roomWithSpace.poll();
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
    public boolean userWaitForGame(User user) {
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

    private void startGameFromRoom(Room room) {

    }

    public Room getRoomByUser(User user) {
        return this.userRoomMap.get(user);
    }

}
