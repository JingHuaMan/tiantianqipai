package handler.eventHandler.system;

import info.data.system.User;

import java.util.HashSet;

public class UserManager {

    private static UserManager instance;

    private final HashSet<User> usersOnline;

    private UserManager() {
        usersOnline = new HashSet<>();
    }

    public synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    public void userLogin(User user) {
        synchronized (usersOnline) {
            usersOnline.add(user);
        }
    }

    public boolean isUserOnline(User user) {
        synchronized (usersOnline) {
            return usersOnline.contains(user);
        }
    }

    public void userLogoff(User user) {
        synchronized (usersOnline) {
            usersOnline.remove(user);
        }
    }
}
