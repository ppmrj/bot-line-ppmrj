package com.ppmrj.linebot;

import java.util.*;

public class Group {
    private String id;
    private String label;
    private int gameStatus;

    /**
     * Game status
     * 1 = PRE-GAME;
     * 2 = GAME STARTED;
     * 3 = POST-GAME;
     * 0 = GAME ENDED / NO GAME;
     */
    int GAME_STATUS = 0;
    int GAME_ID = -1;

    int ROLES_ASSIGNED = 0;
    int VOTING_STARTED = 0;
    int GAME_JUST_BEGIN = 0;

    int MAX_STRIKE = 3;

    int PREGAME_TIME = 120; // Seconds
    int VOTING_TIME = 60; // Seconds
    int ROLLING_TIME = 30;
    int DELAY_TIME = 3; // Seconds

    int ANTI_SPAM_MAP = 30;
    String MAP_URL = "https://res.cloudinary.com/biglebomb/image/upload/v1507747592/mapulartangga2_ji2t3c.jpg";

    ArrayList<User> playerList = new ArrayList<>();

    static Object[][] gameList = {
            {0, "Mafia", 4, 20, 120, 60}, // ID, Game name, Minimum player, Maximum player, Pregame time, Roll time
            {1, "Ular tangga", 2, 4, 120, 40}
    };

    static Object[][] diceSymbol = {
            {0, "⚀"},
            {1, "⚁"},
            {2, "⚂"},
            {3, "⚃"},
            {4, "⚄"},
            {5, "⚅"}
    };

    public Group(String id, int GAME_STATUS, int GAME_ID) {
        this.id = id;
        this.GAME_STATUS = GAME_STATUS;
        this.GAME_ID = GAME_ID;
    }

    public Group(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGameName(int id){
        return gameList[id][1].toString();
    }

    public void addPlayerToList(User user){
        playerList.add(user);
    }
}
