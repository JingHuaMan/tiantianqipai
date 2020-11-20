package handler.eventHandler.landlord;

import pojo.data.landlord.Game;

import java.util.HashSet;

public class GameManager {

    private static GameManager instance;

    private HashSet<Game> gameOnPlay;

    private GameManager() {
        gameOnPlay = new HashSet<>();
    }

    public synchronized static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }


}
