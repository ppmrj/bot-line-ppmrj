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

/**
 * Created by Asus on 10/10/2017.
 */
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

    int PREGAME_TIME = 60; // Seconds
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
//
//    public void startGame(){
//        if(GAME_ID == 0){
//            Timer timer = new Timer();
//            timer.scheduleAtFixedRate(new TimerTask() {
//                @Override
//                public void run() {
//
//                    int total_mafia = playerList.size() / 3;
//                    int total_sheriff = playerList.size() / 3;
//                    int total_doctor = playerList.size() / 3;
//                    int total_detective = playerList.size() / 3;
//                    if (GAME_STATUS == 1) {
//
//                        /**
//                         * Roles assignment
//                         */
//                        if (ROLES_ASSIGNED == 0) {
//                            Collections.shuffle(playerList);
//                            int index = 0;
//                            for(int i=0; i<User.roles.length-1; i++){
//                                if(i < User.roles.length ){
//                                    if(total_mafia <= playerList.size() / 3){
//                                        playerList.get(i).setRole(index);
//                                        index++;
//                                        total_mafia++;
//                                    }
//                                    if(total_sheriff <= playerList.size() / 3){
//                                        playerList.get(i).setRole(index);
//                                        index++;
//                                        total_sheriff++;
//                                    }
//                                    if(total_doctor <= playerList.size() / 3){
//                                        playerList.get(i).setRole(index);
//                                        index++;
//                                        total_doctor++;
//                                    }
//                                    if(total_sheriff <= playerList.size() / 3){
//                                        playerList.get(i).setRole(index);
//                                        index++;
//                                        total_detective++;
//                                    } else {
//                                        if(index != 5){
//                                            playerList.get(i).setRole(index);
//                                            index = 0;
//                                        } else
//                                            index++;
//                                    }
//                                } else {
//                                    index = 0;
//                                    playerList.get(i).setRole(index);
//                                    index++;
//                                }
//                            }
//                            ROLES_ASSIGNED = 1;
//                        }
//                        else {
//                            PREGAME_TIME--;
//                            if (PREGAME_TIME == 120)
//                                pushMessage(id, "Game akan dimulai dalam waktu 2 menit. Ketik /join untuk mengikuti.");
//                            else if (PREGAME_TIME == 60)
//                                pushMessage(id, "Game akan dimulai dalam waktu 1 menit. Ketik /join untuk mengikuti.");
//                            else if (PREGAME_TIME == 30)
//                                pushMessage(id, "Game akan dimulai dalam waktu 30 detik. Ketik /join untuk mengikuti.");
//                            else if (PREGAME_TIME == 10)
//                                pushMessage(id, "Game akan dimulai dalam waktu 10 detik. Ketik /join untuk mengikuti.");
//                            else if (PREGAME_TIME == 0 && playerList.size() >= 5){
//                                pushMessage(id, "Game mafia dimulai dengan "+playerList.size()+" pemain");
//                                GAME_STATUS = 2;
//                                GAME_JUST_BEGIN = 1;
//                            }
//                            else
//                                pushMessage(id, "Tidak ada cukup pemain untuk memulai game.");
//                        }
//                    } else if (GAME_STATUS == 2){
//                        if (GAME_JUST_BEGIN == 1){
//                            pushMessage(id, "Game akan dimulai...");
//                            pushMessage(id, "Malam hari tiba. Setiap malam di PPM akan ada warga PPM yang diculik oleh Mafia. Tugas kalian adalah untuk mencari tahu siapa Mafianya dengan cara " +
//                                    "melakukan voting terhadap orang yang kalian anggap paling mencurigakan.");
//                        }
//                        if (VOTING_STARTED == 0) {
//                            pushMessage(id, "Voting dimulai...");
//                            for(int i=0; i<playerList.size(); i+=4){
//                                int index = i;
//                                List<Action> listUser = new ArrayList<>();
//                                for(int j=i; j<=i+4; j++){
//                                    listUser.add(new PostbackAction(playerList.get(j).getName(), playerList.get(j).getId()+"&groupId="+id));
//                                }
//                                ButtonsTemplate buttonsTemplate = new ButtonsTemplate("N/A", "Voting pemain", "Pilih pemain yang ingin diciduk", listUser);
//                                votingMessage(playerList.get(i).getId(), buttonsTemplate);
//                            }
//                            VOTING_STARTED = 1;
//                        } else {
//                            VOTING_TIME--;
//                            if(VOTING_TIME == 30)
//                                pushMessage(id, "Voting akan berakhir dalam 30 detik.");
//                            else if(VOTING_TIME == 10)
//                                pushMessage(id, "Voting akan berakhir dalam 10 detik.");
//                            else if(VOTING_TIME == 0){
//                                Collections.sort(playerList, new Comparator<User>() {
//                                    @Override
//                                    public int compare(User u1, User u2) {
//                                        return u1.voted - u2.voted;
//                                    }
//                                });
//                                pushMessage(id, "Para warga telah melakukan voting, dari hasil voting warga maka "+playerList.get(1).getName()+" akan diciduk karena dianggap sebagai Mafia.");
//                                playerList.get(0).setStatus("Terciduk");
//                                pushMessage(id, playerList.get(0).getName()+ " adalah " + playerList.get(1).getRoleName(GAME_ID, playerList.get(1).getRole()));
//                                int count = 0;
//                                for(User user: playerList){
//                                    if(user.getStatus().equalsIgnoreCase("Terciduk"))
//                                        count++;
//                                }
//                                if(count == total_mafia){
//                                    pushMessage(id, "Semua mafia telah terciduk para warga menang!");
//                                    timer.cancel();
//                                    timer.purge();
//                                }
//                                VOTING_TIME = 60;
//                                VOTING_STARTED = 0;
//                            }
//                        }
//                    } else if (GAME_STATUS == 3) {
//
//                    }
//                }
//            }, 0, 1000);
//        }
//    }

}
