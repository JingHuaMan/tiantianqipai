package handler.eventHandler.system;

import io.netty.channel.ChannelHandlerContext;
import pojo.data.system.User;

import java.util.HashMap;

public class UserManager {

    private static UserManager instance;

    private final HashMap<Integer, User> idUserMap;

    private final HashMap<User, ChannelHandlerContext> usersOnline;

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

    public boolean userLogin(User user, ChannelHandlerContext ctx) {
        synchronized (usersOnline) {
            if (!usersOnline.containsKey(user)) {
                synchronized (idUserMap) {
                    idUserMap.put(user.getId(), user);
                }
                usersOnline.put(user, ctx);
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

    public ChannelHandlerContext getCtxByUser(User user) {
        return usersOnline.get(user);
    }

    public boolean isUserOnline(User user) {
        synchronized (usersOnline) {
            return usersOnline.containsKey(user);
        }
    }

    public void userLogoff(User user) {
        synchronized (usersOnline) {
            usersOnline.remove(user);
        }
    }
}
