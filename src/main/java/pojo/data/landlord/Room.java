package pojo.data.landlord;

import pojo.data.system.User;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.LinkedList;
import java.util.UUID;

@EqualsAndHashCode(exclude = {"id"})
public class Room {

    @Getter
    private final UUID id;

    @Getter
    private final LinkedList<User> users;

    public Room() {
        id = UUID.randomUUID();
        users = new LinkedList<>();
    }

    // true means the room is full
    public boolean addUser(User user) {
        synchronized (this) {
            users.add(user);
            return users.size() == 3;
        }
    }

    // true means the room is empty
    public boolean removeUser(User user) {
        synchronized (this) {
            users.remove(user);
            return users.size() == 0;
        }
    }
}
