package com.dicoding.menirukanmu;

import com.linecorp.bot.client.LineMessagingServiceBuilder;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.action.Action;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.template.ButtonsTemplate;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.model.response.BotApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import retrofit2.Response;

import java.io.IOException;
import java.util.*;

public class Group {
    private String id;
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
//    private int VOTING_STATUS = 0;

    int PREGAME_TIME = 120; // Seconds
    int VOTING_TIME = 60; // Seconds
    int DELAY_TIME = 3; // Seconds

    ArrayList<String> playerIDList = new ArrayList<String>();
    ArrayList<User> playerList = new ArrayList<User>();

    static Object[][] gameList = {
            {0, "Mafia"}
    };

    public Group(String id, int GAME_STATUS, int GAME_ID) {
        this.id = id;
        this.GAME_STATUS = GAME_STATUS;
        this.GAME_ID = GAME_ID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getGAME_STATUS() {
        return GAME_STATUS;
    }

    public void setGAME_STATUS(int GAME_STATUS) {
        this.GAME_STATUS = GAME_STATUS;
    }

    public int getGAME_ID() {
        return GAME_ID;
    }

    public void setGAME_ID(int GAME_ID) {
        this.GAME_ID = GAME_ID;
    }

    public String getGameByID(int id){
        return gameList[id][1].toString();
    }

    public void addPlayerIDToList(String id){
        playerIDList.add(id);
    }

    public void addPlayerToList(User user){
        playerList.add(user);
    }
}
