package handler.eventHandler.system;

import io.netty.channel.Channel;
import pojo.data.system.User;

import java.util.HashMap;

public class UserManager {

    private static UserManager instance;

    private final HashMap<Integer, User> idUserMap;

    private final HashMap<User, Channel> usersOnline;

    private UserManager() {
        idUserMap = new HashMap<>();
        usersOnline = new HashMap<>();
    }

    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    public boolean userLogin(User user, Channel channel) {
        synchronized (usersOnline) {
            if (!usersOnline.containsKey(user)) {
                synchronized (idUserMap) {
                    idUserMap.put(user.getId(), user);
                }
                usersOnline.put(user, channel);
                return true;
            }
            return false;
        }
    }

    public User getUserById(int userId) {
        synchronized (idUserMap) {
            return idUserMap.get(userId);
        }
    }

    public Channel getChannelByUser(User user) {
        return usersOnline.get(user);
    }
}
