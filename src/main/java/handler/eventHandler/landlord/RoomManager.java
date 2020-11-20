package handler.eventHandler.landlord;

import lombok.Getter;
import pojo.data.landlord.Room;
import pojo.data.system.User;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class RoomManager {

    public static RoomManager instance = new RoomManager();

    private final Queue<Room> roomWithSpace;

    @Getter
    private final HashSet<Room> roomOnGame;

    private RoomManager() {
        roomWithSpace = new LinkedList<>();
        roomOnGame = new HashSet<>();
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
                    synchronized (roomOnGame) {
                        roomOnGame.add(room);
                    }
                    startGameFromRoom(room);
                }
            }
        }
        return room;
    }

    private void startGameFromRoom(Room room) {

    }

}
