
package com.ppmrj.linebot;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.linecorp.bot.model.action.MessageAction;
import com.ppmrj.linebot.Responses.ImageResponse;
import com.ppmrj.linebot.Responses.InfoResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.linecorp.bot.client.LineMessagingServiceBuilder;
import com.linecorp.bot.client.LineSignatureValidator;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.Action;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.template.ButtonsTemplate;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.model.response.BotApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import retrofit2.Response;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.List;

@RestController
@RequestMapping(value="/linebot")
public class LineBotController
{
    ArrayList<Group> groups = new ArrayList<>();

    @Autowired
    @Qualifier("com.linecorp.channel_secret")
    String lChannelSecret;

    @Autowired
    @Qualifier("com.linecorp.channel_access_token")
    String lChannelAccessToken;

    @RequestMapping(value="/sendinfo", method=RequestMethod.POST)
    public ResponseEntity<String> info(
            @RequestBody String infoData
    ){
        if(infoData!=null && infoData.length() > 0){
            System.out.println("Data: "+infoData);
        }

        Gson gson = new Gson();
        InfoResponse infoResponse = gson.fromJson(infoData, InfoResponse.class);

        pushMessage(groups.get(0).getId(), infoResponse.data);
        return new ResponseEntity<String>(HttpStatus.OK);
    }

    @RequestMapping(value="/callback", method=RequestMethod.POST)
    public ResponseEntity<String> callback(
            @RequestHeader("X-Line-Signature") String aXLineSignature,
            @RequestBody String aPayload)
    {
        final String text=String.format("The Signature is: %s",
                (aXLineSignature!=null && aXLineSignature.length() > 0) ? aXLineSignature : "N/A");
        System.out.println(text);
        final boolean valid=new LineSignatureValidator(lChannelSecret.getBytes()).validateSignature(aPayload.getBytes(), aXLineSignature);
        System.out.println("The signature is: " + (valid ? "valid" : "tidak valid"));
        if(aPayload.length() > 0)
        {
            System.out.println("Payload: " + aPayload);
        }
        Gson gson = new Gson();
        Payload payload = gson.fromJson(aPayload, Payload.class);

        String msgText = " ";
        String idTarget = " ";
        String userId = " ";
        String eventType = payload.events[0].type;

        if (eventType.equals("join")){
            if (payload.events[0].source.type.equals("group")){
                replyToUser(payload.events[0].replyToken, "Hello Group");
            }
            if (payload.events[0].source.type.equals("room")){
                replyToUser(payload.events[0].replyToken, "Hello Room");
            }
        } else if (eventType.equals("message")){
            if (payload.events[0].source.type.equals("group")){
                idTarget = payload.events[0].source.groupId;
                userId = payload.events[0].source.userId;
            } else if (payload.events[0].source.type.equals("room")){
                idTarget = payload.events[0].source.roomId;
                userId = payload.events[0].source.userId;
            } else if (payload.events[0].source.type.equals("user")){
                idTarget = payload.events[0].source.userId;
                userId = payload.events[0].source.userId;
            }

            if (payload.events[0].message.type.equals("postback")) {
                pushMessage(idTarget, payload.events[0].postbackContent.getData());
            } else if (payload.events[0].message.type.equals("text")){
                msgText = payload.events[0].message.text;
                msgText = msgText.toLowerCase();


                if (payload.events[0].source.type.equals("group")) {
                    String source = payload.events[0].source.type;
                    String userid = payload.events[0].source.userId;
                    String groupid = payload.events[0].source.groupId;
                    String replyToken = payload.events[0].replyToken;

                    if(msgText.contains("/addgrouptolist")){
                        String[] cmd = msgText.split("\\s");
                        if(cmd.length == 2){
                            Group group = searchGroupById(groupid);
                            if(group==null){
                                groups.add(new Group(groupid, cmd[1]));
                                replyToUser(replyToken, "Group ini telah berhasil ditambahkan ke list dengan nama "+cmd[1]);
                            }
                            else
                                replyToUser(replyToken, "Group "+cmd[1]+ " sudah ada didalam list.");
                        } else {
                            replyToUser(replyToken, "Perintah salah. /addgrouptolist <nama>");
                        }
                    }

                    if (searchGroupById(groupid) == null) {
                        Group group = new Group(groupid, 0, 0);
                        groups.add(group);
                    }

                    if(msgText.equalsIgnoreCase("/sendpostback")){

                    }

                    Group currentGroup = searchGroupById(groupid);

                    if (msgText.equalsIgnoreCase("/credit")) {
                        pushMessage(groupid, "Bot PPM RJ" +
                                "\n" +
                                "V0.1b" +
                                "\n" +
                                "\n" +
                                "Dibuat oleh Divisi IT PPM RJ.");
                    }
                    if (msgText.equalsIgnoreCase("/listgame")) {
                        StringBuilder listGame = new StringBuilder();
                        for (int i = 0; i < Group.gameList.length; i++) {
                            listGame.append(Group.gameList[i][1].toString()).append("\n");
                        }
                        replyToUser(replyToken, listGame.toString());
                        pushMessage(groupid, "Untuk memulai game gunakan \n/main <nama game>.");
                    }
                    if (msgText.equalsIgnoreCase("/berhenti")) {
                        if (currentGroup != null) {
                            if (currentGroup.GAME_STATUS != 0) {
                                currentGroup.GAME_STATUS = 3;
                                replyToUser(replyToken, "Game " + currentGroup.getGameName(currentGroup.GAME_ID) + " telah diberhentikan.");
                                currentGroup.GAME_ID = -1;
                            } else {
                                replyToUser(replyToken, "Tidak ada game yang sedang dimainkan.");
                            }
                        } else {
                            System.out.println("Group unregistered.");
                        }
                    }
                    if (msgText.equalsIgnoreCase("/listpemain")) {
                        if (currentGroup != null) {
                            if (currentGroup.GAME_STATUS != 0) {
                                StringBuilder listPlayer = new StringBuilder();
                                int alive = 0;
                                if (currentGroup.playerList != null) {
                                    if (currentGroup.GAME_ID == 0) {
                                        for (User user : currentGroup.playerList) {
                                            if (currentGroup.GAME_STATUS != 2) {
                                                listPlayer.append(user.getName()).append("\n");
                                            } else {
                                                listPlayer.append(user.getName()).append(" ").append(user.getRole()).append("\n");
                                            }
                                            if(!user.getStatus().equalsIgnoreCase("Terciduk")){
                                                alive++;
                                            }
                                        }
                                        replyToUser(replyToken, listPlayer.toString()+"\nPemain yang masih bermain: " + alive + "/" + currentGroup.playerList.size()+".");
                                    } else if(currentGroup.GAME_ID == 1) {
                                        for(User user : currentGroup.playerList){
                                            if(currentGroup.GAME_STATUS != 2){
                                                listPlayer.append(user.getName()).append("\n");
                                            } else {
                                                listPlayer.append(user.getName()).append(" - Posisi: ").append(user.getPosition()).append("\n");
                                            }
                                        }
                                        replyToUser(replyToken, listPlayer.toString()+"\nJumlah pemain: "+currentGroup.playerList.size()+".");
                                    }
                                } else {
                                    replyToUser(replyToken, "Belum ada pemain yang join.");
                                }
                            } else {
                                replyToUser(replyToken, "Tidak ada game yang sedang dimainkan.");
                            }
                        } else {
                            System.out.println("Group unregistered.");
                        }
                    }
                    if(msgText.equalsIgnoreCase("/main ulartangga")){
                        if(userId == null){
                            replyToUser(replyToken, "Kamu belum mengupdate versi linemu ke yang paling baru. Update linemu terlebih dahulu.");
                        } else {
                            if(currentGroup != null){
                                if(currentGroup.GAME_STATUS == 0) {
                                    User user = getUserProfile(userId);
                                    if(user != null){
                                        currentGroup.GAME_STATUS = 1;
                                        currentGroup.GAME_ID = 1;
                                        currentGroup.addPlayerToList(user);
                                        pushMessage(groupid, user.getName() + " telah memulai permainan "+currentGroup.getGameName(currentGroup.GAME_ID)+". Ketik /join untuk bergabung. Game akan dimulai dalam "+currentGroup.PREGAME_TIME/60+" menit." +
                                                "\n" +
                                                "Minimal pemain: "+Group.gameList[currentGroup.GAME_ID][2]);
                                        startGame(currentGroup);
                                    }
                                }
                            }
                        }
                    }
                    if (msgText.equalsIgnoreCase("/main mafia")) {
                        if(userId == null){
                            replyToUser(replyToken, "Kamu belum mengupdate versi linemu ke yang paling baru. Update linemu terlebih dahulu.");
                        }
                        if (currentGroup != null) {
                            if (currentGroup.GAME_STATUS == 0) {
                                User user = getUserProfile(userId);
                                if(user != null){
                                    if (!user.getId().equals("0")) {
                                        currentGroup.GAME_ID = 0;
                                        currentGroup.GAME_STATUS = 1;
                                        currentGroup.addPlayerToList(user);
                                        pushMessage(groupid, user.getName() + " telah memulai permainan "+currentGroup.getGameName(currentGroup.GAME_ID)+". Ketik /join untuk bergabung. Game akan dimulai dalam "+currentGroup.PREGAME_TIME/60+" menit." +
                                                "\n" +
                                                "Minimal pemain: "+Group.gameList[currentGroup.GAME_ID][2]);
                                        startGame(currentGroup);
                                    } else {
                                        replyToUser(replyToken, "Kamu belum mengupdate versi linemu ke yang paling baru. Update linemu terlebih dahulu.");
                                    }
                                } else {
                                    replyToUser(replyToken, "Kamu belum menambahkan bot sebagai teman. Silahkan tambahkan bot sebagai teman dahulu.");
                                }
                            } else {
                                replyToUser(replyToken, "Permainan " + currentGroup.getGameName(currentGroup.GAME_ID) + " sedang berjalan.");
                            }
                        } else {
                            Group group = new Group(groupid, 1, 0);
                            User user = getUserProfile(userId);
                            if (user != null) {
                                if (!user.getId().equals("0")) {
                                    group.addPlayerToList(user);
                                    groups.add(group);
                                    pushMessage(groupid, user.getName() + " telah memulai permainan "+group.getGameName(group.GAME_ID)+". Ketik /join untuk bergabung. Game akan dimulai dalam 3 menit.");
                                } else {
                                    replyToUser(replyToken, "Kamu belum mengupdate versi linemu ke yang paling baru. Update linemu terlebih dahulu.");
                                }
                            } else {
                                replyToUser(replyToken, "Kamu belum menambahkan bot sebagai teman. Silahkan tambahkan bot sebagai teman dahulu.");
                            }
                        }

                    }
                    if (msgText.equalsIgnoreCase("/join")) {
                        if(userId == null){
                            replyToUser(replyToken, "Kamu belum mengupdate versi linemu ke yang paling baru. Update linemu terlebih dahulu.");
                        } else {
                            if (currentGroup != null) {
                                if (currentGroup.GAME_STATUS == 0) {
                                    replyToUser(replyToken, "Belum ada permainan yang dibuat. Ketik /listgame untuk melihat game yang tersedia.");
                                } else {
                                    if (currentGroup.GAME_STATUS != 0) {
                                        User user = getUserProfile(userId);
                                        if (user != null) {
                                            if(checkIfUserJoined(userId, currentGroup.playerList)){
                                                replyToUser(replyToken, "Kamu sudah tergabung ke dalam game, "+user.getName()+".");
                                            } else {
                                                int gameId = currentGroup.GAME_ID;
                                                currentGroup.addPlayerToList(user);
                                                pushMessage(groupid, user.getName() + " bergabung ke permainan " + Group.gameList[gameId][1].toString() +
                                                        "\n\n" + currentGroup.playerList.size() + " pemain telah tergabung.");
                                                if(currentGroup.playerList.size() == (int) Group.gameList[currentGroup.GAME_ID][3]){
                                                    currentGroup.GAME_STATUS = 1;
                                                }
                                            }
                                        } else {
                                            replyToUser(replyToken, "Kamu belum menambahkan bot sebagai teman. Silahkan tambahkan bot sebagai teman dahulu.");
                                        }
                                    } else {
                                        replyToUser(replyToken, "Game sudah dimulai.");
                                    }
                                }
                            }

                        }
                    }
                    if (msgText.equalsIgnoreCase("/mulai")) {
                        if(userId == null){
                            replyToUser(replyToken, "Kamu belum mengupdate versi linemu ke yang paling baru. Update linemu terlebih dahulu.");
                        } else {
                            if (currentGroup != null) {
                                if (currentGroup.GAME_STATUS == 0) {
                                    replyToUser(replyToken, "Belum ada permainan yang dibuat. Ketik /listgame untuk melihat game yang tersedia.");
                                } else {
                                    User user = getUserProfile(userId);
                                    if (user != null) {
                                        if(currentGroup.GAME_STATUS != 2){
                                            if (checkGameRequirement(currentGroup)) {
                                                replyToUser(replyToken, "Memulai game...");
                                                currentGroup.GAME_STATUS = 2;
                                                currentGroup.GAME_JUST_BEGIN = 1;
                                            } else {
                                                replyToUser(replyToken, "Belum ada cukup pemain untuk memulai game.");
                                            }
                                        } else {
                                            replyToUser(replyToken, "Game telah dimulai.");
                                        }
                                    } else {
                                        replyToUser(replyToken, "Kamu belum menambahkan bot sebagai teman. Silahkan tambahkan bot sebagai teman dahulu.");
                                    }
                                }
                            }
                        }
                    }
                    if(msgText.contains("/kocokdadu")){
                        if(userId == null){
                            replyToUser(replyToken, "Kamu belum mengupdate versi linemu ke yang paling baru. Update linemu terlebih dahulu.");
                        }
                        else {
                            if (currentGroup != null) {
                                if (currentGroup.GAME_STATUS == 0) {
                                    replyToUser(replyToken, "Belum ada permainan yang dibuat. Ketik /listgame untuk melihat game yang tersedia.");
                                } else if(currentGroup.GAME_STATUS != 2){
                                    replyToUser(replyToken, "Game belum dimulai, tunggu hingga game dimulai.");
                                } else {
                                    User user = getUserProfile(userId);
                                    if (user != null) {
                                        if (checkIfUserJoined(userId, currentGroup.playerList)) {
                                            if(!user.getId().equalsIgnoreCase(currentGroup.playerList.get(0).getId())){
                                                replyToUser(replyToken, "Sekarang bukan giliranmu, "+user.getName()+".");
                                            } else {
                                                String[] cmd = msgText.split("\\s");
                                                if(cmd.length > 1 && cmd[0].equalsIgnoreCase("/kocokdadu")){
                                                    if (userId.equalsIgnoreCase("U7a3f1c3b1a71e16d4cbe3f0975e95165")) {
                                                        int dice = Integer.valueOf(cmd[1]);
                                                        currentGroup.playerList.get(0).setDiceNumber(dice);
                                                        currentGroup.playerList.get(0).setDiceRollStatus(1);
                                                    } else {
                                                        replyToUser(replyToken, "Hanya Irfan Abyan yang diperbolehkan memakai perintah ini.");
                                                    }
                                                } else {
                                                    Random random = new Random();
                                                    int dice = random.nextInt(6) + 1;
                                                    currentGroup.playerList.get(0).setDiceNumber(dice);
                                                    currentGroup.playerList.get(0).setDiceRollStatus(1);
                                                }
                                            }
                                        } else {
                                            replyToUser(replyToken, "Kamu tidak tergabung kedalam game, "+user.getName()+".");
                                        }
                                    } else {
                                        replyToUser(replyToken, "Kamu belum menambahkan bot sebagai teman. Silahkan tambahkan bot sebagai teman dahulu.");
                                    }
                                }
                            }
                        }
                    }
                    if(msgText.equalsIgnoreCase("/kocokdadu6")){
                        if(userId == null){
                            replyToUser(replyToken, "Kamu belum mengupdate versi linemu ke yang paling baru. Update linemu terlebih dahulu.");
                        }
                        if (userId.equalsIgnoreCase("U7a3f1c3b1a71e16d4cbe3f0975e95165")) {
                            if (currentGroup != null) {
                                if (currentGroup.GAME_STATUS == 0) {
                                    replyToUser(replyToken, "Belum ada permainan yang dibuat. Ketik /listgame untuk melihat game yang tersedia.");
                                } else if(currentGroup.GAME_STATUS != 2){
                                    replyToUser(replyToken, "Game belum dimulai, tunggu hingga game dimulai.");
                                } else {
                                    User user = getUserProfile(userId);
                                    if (user != null) {
                                        if(!user.getId().equalsIgnoreCase(currentGroup.playerList.get(0).getId())){
                                            replyToUser(replyToken, "Sekarang bukan giliranmu, "+user.getName()+".");
                                        } else {
                                            currentGroup.playerList.get(0).setDiceNumber(6);
                                            currentGroup.playerList.get(0).setDiceRollStatus(1);
                                        }
                                    } else {
                                        replyToUser(replyToken, "Kamu belum menambahkan bot sebagai teman. Silahkan tambahkan bot sebagai teman dahulu.");
                                    }
                                }
                            }
                        } else {
                            replyToUser(replyToken, "Hanya Irfan Abyan yang diperbolehkan memakai perintah ini.");
                        }
                    }
                    if(msgText.equalsIgnoreCase("/map")){
                        if(userId == null){
                            replyToUser(replyToken, "Kamu belum mengupdate versi linemu ke yang paling baru. Update linemu terlebih dahulu.");
                        } else {
                            if(currentGroup != null){
                                if(currentGroup.GAME_STATUS == 0){
                                    replyToUser(replyToken, "Belum ada permainan yang dibuat. Ketik /listgame untuk melihat game yang tersedia.");
                                } else {
                                    if(currentGroup.GAME_ID != 1 ){
                                        replyToUser(replyToken, "Game ular tangga sedang tidak dimainkan.");
                                    } else {
                                        if(currentGroup.GAME_STATUS != 2){
                                            pushImage(groupid, currentGroup.MAP_URL);
                                        } else {
                                            if(currentGroup.ANTI_SPAM_MAP != 0){
                                                Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                                                        "cloud_name", "biglebomb",
                                                        "api_key", "914939112251975",
                                                        "api_secret", "mTgyRz24r8OTMOYDRSPiCC2vQ4o"));
                                                try {
                                                    showMap(currentGroup, cloudinary);
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if(msgText.equalsIgnoreCase("/map2")) {
                        if (userId == null) {
                            replyToUser(replyToken, "Kamu belum mengupdate versi linemu ke yang paling baru. Update linemu terlebih dahulu.");
                        }
                        if (currentGroup != null) {
                            if (currentGroup.GAME_STATUS == 0) {
                                replyToUser(replyToken, "Belum ada permainan yang dibuat. Ketik /listgame untuk melihat game yang tersedia.");
                            } else {
                                if (currentGroup.GAME_ID != 1) {
                                    replyToUser(replyToken, "Game ular tangga sedang tidak dimainkan.");
                                } else {
                                    pushImage(groupid, currentGroup.MAP_URL);
                                }
                            }
                        }
                    }
                }

                /************* END OF MAFIA MINIGAME ************************/

                if (!msgText.contains("bot leave")) {
//                    try {
////                        getMessageData(msgText, idTarget, userId);
//                    } catch (IOException e) {
//                        System.out.println("Exception is raised ");
//                        e.printStackTrace();
//                    }
                } else {
                    if (payload.events[0].source.type.equals("group")) {
                        leaveGR(payload.events[0].source.groupId, "group");
                    } else if (payload.events[0].source.type.equals("room")) {
                        leaveGR(payload.events[0].source.roomId, "room");
                    }
                }

            } else {
                System.out.println("Unknown message.");
            }
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }


    public void startGame(Group group){
        if(group.GAME_ID == 0) {
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    int total_mafia = group.playerList.size() / 3;
                    int total_sheriff = group.playerList.size() / 3;
                    int total_doctor = group.playerList.size() / 3;
                    int total_detective = group.playerList.size() / 3;
                    if (group.GAME_STATUS == 1) {
                        /**
                         * Roles assignment
                         */
                        if (group.ROLES_ASSIGNED == 0) {
                            Collections.shuffle(group.playerList);
                            int index = 0;
                            for(int i=0; i<User.roles.length; i++){
                                if(i < User.roles.length ){
                                    if(total_mafia <= group.playerList.size() / 3){
                                        group.playerList.get(i).setRole(index);
                                        index++;
                                        total_mafia++;
                                    }
                                    if(total_sheriff <= group.playerList.size() / 3){
                                        group.playerList.get(i).setRole(index);
                                        index++;
                                        total_sheriff++;
                                    }
                                    if(total_doctor <= group.playerList.size() / 3){
                                        group.playerList.get(i).setRole(index);
                                        index++;
                                        total_doctor++;
                                    }
                                    if(total_sheriff <= group.playerList.size() / 3){
                                        group.playerList.get(i).setRole(index);
                                        index++;
                                        total_detective++;
                                    } else {
                                        if(index != 5){
                                            group.playerList.get(i).setRole(index);
                                            index = 0;
                                        } else
                                            index++;
                                    }
                                } else {
                                    index = 0;
                                    group.playerList.get(i).setRole(index);
                                    index++;
                                }
                            }
                            group.ROLES_ASSIGNED = 1;
                        }
                        else {
                            group.PREGAME_TIME--;
                            if (group.PREGAME_TIME == 120)
                                pushMessage(group.getId(), "Game akan dimulai dalam waktu 2 menit. Ketik /join untuk bergabung.");
                            else if (group.PREGAME_TIME == 60)
                                pushMessage(group.getId(), "Game akan dimulai dalam waktu 1 menit. Ketik /join untuk bergabung.");
                            else if (group.PREGAME_TIME == 30)
                                pushMessage(group.getId(), "Game akan dimulai dalam waktu 30 detik. Ketik /join untuk bergabung.");
                            else if (group.PREGAME_TIME == 10)
                                pushMessage(group.getId(), "Game akan dimulai dalam waktu 10 detik. Ketik /join untuk bergabung.");
                            else if (group.PREGAME_TIME == 0 && group.playerList.size() >= (Integer) Group.gameList[group.GAME_ID][2]){
                                pushMessage(group.getId(), "Game mafia dimulai dengan "+group.playerList.size()+" pemain");
                                group.GAME_STATUS = 2;
                                group.GAME_JUST_BEGIN = 1;
                                group.PREGAME_TIME = 120;
                            }
                            else if (group.PREGAME_TIME == 0){
                                pushMessage(group.getId(), "Tidak ada cukup pemain untuk memulai game.");
                                group.GAME_STATUS = 0;
                                group.GAME_ID = -1;
                                group.playerList.clear();
                                group.PREGAME_TIME = 120;
                            }
                        }
                    } else if (group.GAME_STATUS == 2){
                        if (group.GAME_JUST_BEGIN == 1){
                            pushMessage(group.getId(), "Game akan dimulai...");
                            pushMessage(group.getId(), "Malam hari tiba. Setiap malam di PPM akan ada warga PPM yang diculik oleh Mafia. Tugas kalian adalah untuk mencari tahu siapa Mafianya dengan cara " +
                                    "melakukan voting terhadap orang yang kalian anggap paling mencurigakan.");
                        }
                        if (group.VOTING_STARTED == 0) {
                            pushMessage(group.getId(), "Voting dimulai...");
                            for(int i=0; i<group.playerList.size(); i+=4){
                                List<Action> listUser = new ArrayList<>();
                                for(int j=i; j<=i+4; j++){
                                    listUser.add(new PostbackAction(group.playerList.get(j).getName(), group.playerList.get(j).getId()+"&groupId="+group.getId()));
                                }
                                ButtonsTemplate buttonsTemplate = new ButtonsTemplate("N/A", "Voting pemain", "Pilih pemain yang ingin diciduk", listUser);
                                buttonMessage(group.playerList.get(i).getId(), buttonsTemplate);
                                group.GAME_JUST_BEGIN = 0;
                            }
                            group.VOTING_STARTED = 1;
                        } else {
                            group.VOTING_TIME--;
                            if(group.VOTING_TIME == 10)
                                pushMessage(group.getId(), "Voting akan berakhir dalam 10 detik.");
                            else if(group.VOTING_TIME == 0){
                                group.playerList.sort(new Comparator<User>() {
                                    @Override
                                    public int compare(User u1, User u2) {
                                        return u1.voted - u2.voted;
                                    }
                                });
                                pushMessage(group.getId(), "Para warga telah melakukan voting, dari hasil voting warga maka "+group.playerList.get(1).getName()+" akan diciduk karena dianggap sebagai Mafia.");
                                group.playerList.get(0).setStatus("Terciduk");
                                Collections.rotate(group.playerList, -1);
                                pushMessage(group.getId(), group.playerList.get(0).getName()+ " adalah " + group.playerList.get(1).getRoleName(group.GAME_ID, group.playerList.get(1).getRole()));
                                int count = 0;
                                for(User user: group.playerList){
                                    if(user.getStatus().equalsIgnoreCase("Terciduk"))
                                        count++;
                                }
                                if(count == total_mafia){
                                    pushMessage(group.getId(), "Semua mafia telah terciduk para warga menang!");
                                    group.GAME_STATUS = 3;
                                    timer.cancel();
                                    timer.purge();
                                }
                                group.VOTING_TIME = 60;
                                group.VOTING_STARTED = 0;
                            }
                        }
                    } else if (group.GAME_STATUS == 3) {
                        pushMessage(group.getId(), "Game telah berakhir.");
                        group.playerList.clear();
                        group.GAME_STATUS = 0;
                    }
                }
            }, 0, 1000);
        }
        else if(group.GAME_ID == 1){
            Timer timer = new Timer();
            int[][] ladderArray = {
                    {3, 21},
                    {8, 30},
                    {28, 84},
                    {58, 77},
                    {75, 86},
                    {90, 91}
            };
            int[][] snakeArray = {
                    {17, 13},
                    {52, 29},
                    {57, 40},
                    {62, 22},
                    {88, 18},
                    {95, 51},
                    {97, 79}
            };
            int PRE_GAME_TIME_DEFAULT = (int)Group.gameList[group.GAME_ID][4];
            int ROLLING_TIME_DEFAULT = (int)Group.gameList[group.GAME_ID][5];
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {

                    if(group.GAME_STATUS == 1){
                        group.PREGAME_TIME--;
                        if (group.PREGAME_TIME == PRE_GAME_TIME_DEFAULT) {
                            ButtonsTemplate buttonsTemplate = new ButtonsTemplate(null, "Gabung ke permainan", "Game akan dimulai dalam waktu 2 menit. Ketik /join untuk bergabung.", Arrays.asList(new MessageAction("Gabung", "/join")));
                            buttonMessage(group.getId(), buttonsTemplate);
                        }
                        else if (group.PREGAME_TIME == 60){
                            ButtonsTemplate buttonsTemplate = new ButtonsTemplate(null, "Gabung ke permainan", "Game akan dimulai dalam waktu 1 menit. Ketik /join untuk bergabung.", Arrays.asList(new MessageAction("Gabung", "/join")));
                            buttonMessage(group.getId(), buttonsTemplate);
                        }
                        else if (group.PREGAME_TIME == 30){
                            ButtonsTemplate buttonsTemplate = new ButtonsTemplate(null, "Gabung ke permainan", "Game akan dimulai dalam waktu 30 detik. Ketik /join untuk bergabung.", Arrays.asList(new MessageAction("Gabung", "/join")));
                            buttonMessage(group.getId(), buttonsTemplate);
                        }
                        else if (group.PREGAME_TIME == 0 && group.playerList.size() >= (int) Group.gameList[group.GAME_ID][2]){
                            pushMessage(group.getId(), "Game "+group.getGameName(group.GAME_ID)+" dimulai dengan "+group.playerList.size()+" pemain");
                            group.GAME_STATUS = 2;
                            group.GAME_JUST_BEGIN = 1;
                            group.PREGAME_TIME = PRE_GAME_TIME_DEFAULT;
                        }
                        else if (group.PREGAME_TIME == 0){
                            pushMessage(group.getId(), "Tidak ada cukup pemain untuk memulai game.");
                            group.GAME_STATUS = 0;
                            group.GAME_ID = -1;
                            group.playerList.clear();
                            group.PREGAME_TIME = PRE_GAME_TIME_DEFAULT;
                        }
                    } else if(group.GAME_STATUS == 2){
                        User currentPlayer = group.playerList.get(0);
                        if(group.GAME_JUST_BEGIN == 1){
                            pushMessage(group.getId(), "Game akan dimulai...");
                            pushMessage(group.getId(), "Setiap pemain diharuskan mengocok dadu dengan batas waktu "+Group.gameList[group.GAME_ID][5].toString()+" detik dan maju sesuai hasil dari angka dadu." +
                                    "\nApabila waktu habis maka pemain mendapat satu pelanggaran.");
                            pushMessage(group.getId(), "Jika pemain mendapatkan "+group.MAX_STRIKE+" maka dia akan otomatis dikeluarkan.");
                            pushMessage(group.getId(), "Apabila pemain mendapatkan angka 6 saat mengocok dadu, maka dia diperbolehkan untuk mengocok dadu kembali.");
                            Collections.shuffle(group.playerList);
                            pushMessage(group.getId(), "Pemain pertama adalah "+group.playerList.get(0).getName());
                        }
                        group.GAME_JUST_BEGIN = 0;
                        if(currentPlayer.getDiceRollStatus() == 1) {
                            String msg = "";
                            msg += currentPlayer.getName() + " telah mengocok dadu dan hasilnya adalah " + currentPlayer.getDiceNumber()+".\n";
                            currentPlayer.setPosition(currentPlayer.getPosition() + currentPlayer.getDiceNumber());
                            if(isInLadderColumn(currentPlayer.getPosition(), ladderArray)){
                                System.out.println(currentPlayer.getName()+" berhenti di tangga.");
                                msg += currentPlayer.getName() + " berhenti di kolom tangga dan naik ke kolom nomor "+getLadderData(currentPlayer.getPosition(), ladderArray);
                                currentPlayer.setPosition(getLadderData(currentPlayer.getPosition(), ladderArray));

                            } else if(isInSnakeColumn(currentPlayer.getPosition(), snakeArray)){
                                System.out.println(currentPlayer.getName()+" berhenti di ular.");
                                msg += currentPlayer.getName() + " berhenti di kolom ular dan turun ke kolom nomor "+getSnakeData(currentPlayer.getPosition(), snakeArray);
                                currentPlayer.setPosition(getSnakeData(currentPlayer.getPosition(), snakeArray));
                            }
                            else {
                                if (currentPlayer.getPosition() > 100) {
                                    currentPlayer.setPosition(currentPlayer.getPosition() - (currentPlayer.getPosition() - 100));
                                    msg += "Hasil kocokan dadu untuk maju melebihi 100 karenanya " + currentPlayer.getName() + " mundur lagi ke " + currentPlayer.getPosition();
                                } else if (currentPlayer.getPosition() == 100) {
                                    msg += currentPlayer.getName() + " berhasil memenangkan game karena mencapai kotak nomor 100.";
                                    group.GAME_STATUS = 3;
                                } else {
                                    msg += currentPlayer.getName() + " maju " + currentPlayer.getDiceNumber() + " langkah ke kotak nomor " + currentPlayer.getPosition();
                                }
                            }
                            pushMessage(group.getId(), msg);
                            currentPlayer.setDiceRollStatus(0);
                            if(currentPlayer.getDiceNumber() == 6){
                                group.ROLLING_TIME = ROLLING_TIME_DEFAULT;
                            } else {
                                group.ROLLING_TIME = ROLLING_TIME_DEFAULT;
                                Collections.rotate(group.playerList, -1);
                            }
                        } else {
                            if(group.ROLLING_TIME == ROLLING_TIME_DEFAULT-1){
                                if(currentPlayer.getDiceNumber() == 6)
                                    pushMessage(group.getId(), currentPlayer.getName()+" silahkan mengocok dadu kembali dengan \n/kocokdadu.\nWaktu "+Group.gameList[group.GAME_ID][5]+" detik.");
                                else
                                    pushMessage(group.getId(), currentPlayer.getName()+" silahkan mengocok dadu dengan \n/kocokdadu.\nWaktu "+Group.gameList[group.GAME_ID][5]+" detik.");
                            }
                            else if(group.ROLLING_TIME == 0) {
                                if(currentPlayer.getDiceRollStatus() == 0){
                                    pushMessage(group.getId(), currentPlayer.getName()+" gagal mengocok dadu dalam batas waktu yang ditentukan.");
                                    currentPlayer.strike++;
                                }
                                if(currentPlayer.strike == group.MAX_STRIKE){
                                    removePlayerFromGame(currentPlayer.getId(), group.playerList);
                                    pushMessage(group.getId(), currentPlayer.getName()+" dikeluarkan dari game karena sudah tidak mengocok dadu sebanyak "+group.MAX_STRIKE+" kali.");
                                    if(group.playerList.size() < 2){
                                        group.GAME_STATUS = 3;
                                        pushMessage(group.getId(), "Tidak ada cukup pemain untuk melanjutkan game.");
                                    }
                                }
                                group.ROLLING_TIME = ROLLING_TIME_DEFAULT;
                                Collections.rotate(group.playerList, -1);
                            }
                            group.ROLLING_TIME--;
                        }
                        group.ANTI_SPAM_MAP--;
                    } else if(group.GAME_STATUS == 3){
                        Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                            "cloud_name", "biglebomb",
                            "api_key", "914939112251975",
                            "api_secret", "mTgyRz24r8OTMOYDRSPiCC2vQ4o"));
                        try {
                            showMap(group, cloudinary);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        pushMessage(group.getId(), "Game telah berakhir.");
                        group.playerList.clear();
                        group.GAME_STATUS = 0;
                    }
                }
            },0, 1000);
        }
    }

    public void showMap(Group group, Cloudinary cloudinary) throws IOException {
        Gson gson = new Gson();
        BufferedImage map = ImageIO.read(new URL(group.MAP_URL));

        BufferedImage[] playerAvatar = new BufferedImage[group.playerList.size()];
        int w = map.getWidth();
        int h = map.getHeight();
        System.out.println("WIDTH: "+w+" HEIGHT: "+h);
        BufferedImage combined = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        int lastPosX=0, lastPosY=0, currentPosX, currentPosY, playerCount=0;
        User currentPlayer;

        Graphics graphics = combined.getGraphics();
        graphics.drawImage(map, 0, 0, null);
        for(int i=0; i<group.playerList.size(); i++){
            currentPlayer = group.playerList.get(i);
            playerAvatar[i] = resize(new URL(currentPlayer.getPictureUrl()), new Dimension((map.getWidth()/10)/ 2, (map.getHeight()/10) / 2));
            int[] imageCoordinate = getImageCoordinateFromPosition(currentPlayer.getPosition(), map, 2, 5);
            currentPosX = imageCoordinate[0];
            currentPosY = imageCoordinate[1];
            System.out.println(currentPlayer.getName()+"'s Coordinate: X: "+currentPosX
                    +" || Y: "+currentPosY+" || ROW: "+checkPosition(currentPlayer.getPosition()));

            if(lastPosX == currentPosX && lastPosY == currentPosY && playerCount == 1){
                graphics.drawImage(playerAvatar[i], currentPosX+((map.getWidth()/10)/2), currentPosY, null);
                playerCount++;
            } else if(lastPosX == currentPosX && lastPosY == currentPosY && playerCount == 2){
                graphics.drawImage(playerAvatar[i], currentPosX, currentPosY+((map.getHeight()/10)/2), null);
                playerCount++;
            } else if(lastPosX == currentPosX && lastPosY == currentPosY && playerCount == 3){
                graphics.drawImage(playerAvatar[i], currentPosX+((map.getWidth()/10)/2), currentPosY+((map.getHeight()/10)/2), null);
                playerCount++;
            } else {
                graphics.drawImage(playerAvatar[i], currentPosX, currentPosY, null);
                playerCount++;
            }
            lastPosX = currentPosX;
            lastPosY = currentPosY;
        }
        File finalFile = new File("final.png");
        ImageIO.write(combined, "PNG", finalFile);
        Map uploadResult = cloudinary.uploader().upload(finalFile, ObjectUtils.asMap(
                "public_id", "ulartangga_"+group.getId()
        ));
        Gson gson2 = new GsonBuilder().create();
        String json = gson2.toJson(uploadResult);
        ImageResponse imageResponse = gson.fromJson(json, ImageResponse.class);
        pushImage(group.getId(), imageResponse.secure_url);
        group.ANTI_SPAM_MAP=30;
    }

    public Boolean checkGameRequirement(Group group){
        return group.playerList.size() >= (int) Group.gameList[group.GAME_ID][2];
    }

    public int getSnakeData(int position, int[][] snakeArray){
        for(int i=0; i<snakeArray.length; i++){
            if(position == snakeArray[i][0]){
                return snakeArray[i][1];
            }
        }
        return 0;
    }

    public Boolean isInSnakeColumn(int position, int[][] snakeArray){
        for(int i=0; i<snakeArray.length; i++){
            if(position == snakeArray[i][0])
                return true;
            else
                return false;
        }
        return false;
    }

    public int getLadderData(int position, int[][] ladderArray){
        for(int i=0; i<ladderArray.length; i++){
            if(position == ladderArray[i][0]){
                return ladderArray[i][1];
            }
        }
        return 0;
    }

    public Boolean isInLadderColumn(int position, int[][] ladderArray){
        for(int i=0; i<ladderArray.length; i++){
            if(position == ladderArray[i][0])
                return true;
            else
                return false;
        }
        return false;
    }

    /**
     * Resize Snippet
     * @source https://stackoverflow.com/questions/18550284/java-resize-image-from-an-url
     */

    public BufferedImage resize(final URL url, final Dimension size) throws IOException{
        final BufferedImage image = ImageIO.read(url);
        final BufferedImage resized = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = resized.createGraphics();
        g.drawImage(image, 0, 0, size.width, size.height, null);
        g.dispose();
        return resized;
    }

    private int[] getImageCoordinateFromPosition(int position, BufferedImage image, int offsetX, int offsetY){
        int width = (image.getWidth()/10);
        int height = (image.getHeight()/10);
        int x, y;
        int pos;
        if(checkPosition(position).equalsIgnoreCase("asc")){
            if(position > 10){
                pos = Integer.parseInt(String.valueOf(position).substring(1));
                if(pos == 0)
                    x = width*9;
                else
                    x = width*(pos-1);
            } else {
                x = width*(position-1);
            }
        }
        else{
            pos = Integer.parseInt(String.valueOf(position).substring(1));
            if(pos == 0)
                x = 0;
            else
                x = width*(10-pos);
        }
        y = height*getPositionRow(position);

        return new int[]{x+offsetX, y+offsetY};
    }

    private String checkPosition(int position){
        if((position > 0 && position <= 10) || (position> 20 && position <= 30) || (position>40 && position <= 50) || (position>60 && position <= 70) || (position>80 && position<=90))
            return "asc";
        else
            return "desc";
    }
    private int getPositionRow(int position){
        if(position > 0 && position <= 10){
            return 9;
        } else if(position > 10 && position <= 20){
            return 8;
        } else if(position > 20 && position <= 30){
            return 7;
        } else if(position > 30 && position <= 40){
            return 6;
        } else if(position > 40 && position <= 50){
            return 5;
        } else if(position > 50 && position <= 60){
            return 4;
        } else if(position > 60 && position <= 70){
            return 3;
        } else if(position > 70 && position <= 80){
            return 2;
        } else if(position > 80 && position <= 90){
            return 1;
        } else if(position > 90 && position <= 100){
            return 0;
        }
        return 0;
    }

    private void removePlayerFromGame(String userId, ArrayList<User> players){
        for(int i=0; i<players.size(); i++){
            if(players.get(i).getId().equalsIgnoreCase(userId)){
                players.remove(i);
            }
        }
    }

    private Group searchGroupById(String groupId){
        for (Group group : groups) {
            if (group.getId().equals(groupId)) {
                return group;
            }
        }
        return null;
    }

    private Boolean checkIfUserJoined(String userId, ArrayList<User> users){
        for (User user : users) {
            if (user.getId().equalsIgnoreCase(userId)) {
                return true;
            }
        }
        return false;
    }

    private User getUserProfile(String userId){
        try {
            Response<UserProfileResponse> response = LineMessagingServiceBuilder
                    .create(lChannelAccessToken)
                    .build()
                    .getProfile(userId)
                    .execute();
            if(!response.message().equalsIgnoreCase("not found")){
                System.out.println("ID: "+response.body() + " | Name: "+response.body().getDisplayName()+" | URL: "+response.body().getPictureUrl());
                return new User(response.body().getUserId(), response.body().getDisplayName(), response.body().getPictureUrl());
            } else {
                return null;
            }
        } catch (IOException e) {
            System.out.println("Exception is raised ");
            e.printStackTrace();
        }
        return null;
    }

    private void getMessageData(String message, String targetID, String userId) throws IOException{
        if (message!=null){
            Response<UserProfileResponse> response = LineMessagingServiceBuilder
                    .create(lChannelAccessToken)
                    .build()
                    .getProfile(userId)
                    .execute();
            if(response.message().equalsIgnoreCase("not found"))
                pushMessage(targetID, "Unknown user: "+message);
            else
                pushMessage(targetID, response.body().getDisplayName()+": "+message);
        }
    }

    private void replyToUser(String rToken, String messageToUser){
        TextMessage textMessage = new TextMessage(messageToUser);
        ReplyMessage replyMessage = new ReplyMessage(rToken, textMessage);
        try {
            Response<BotApiResponse> response = LineMessagingServiceBuilder
                    .create(lChannelAccessToken)
                    .build()
                    .replyMessage(replyMessage)
                    .execute();
            System.out.println("Reply Message: " + response.code() + " " + response.message());
        } catch (IOException e) {
            System.out.println("Exception is raised ");
            e.printStackTrace();
        }
    }

    private void buttonMessage(String sourceId, ButtonsTemplate template){
        TemplateMessage templateMessage = new TemplateMessage("Voting", template);
        PushMessage pushMessage = new PushMessage(sourceId, templateMessage);
        try {
            Response<BotApiResponse> response = LineMessagingServiceBuilder
                    .create(lChannelAccessToken)
                    .build()
                    .pushMessage(pushMessage)
                    .execute();
            System.out.println(response.code() + " " + response.message());
        } catch (IOException e) {
            System.out.println("Exception is raised ");
            e.printStackTrace();
        }
    }

    private void pushImage(String sourceId, String imageUrl){
        ImageMessage imageMessage = new ImageMessage(imageUrl, imageUrl);
        PushMessage pushMessage = new PushMessage(sourceId, imageMessage);
        try {
            Response<BotApiResponse> response = LineMessagingServiceBuilder
                    .create(lChannelAccessToken)
                    .build()
                    .pushMessage(pushMessage)
                    .execute();
            System.out.println(response.code() + " " + response.message());
        } catch (IOException e) {
            System.out.println("Exception is raised ");
            e.printStackTrace();
        }
    }

    private void pushMessage(String sourceId, String txt){
        TextMessage textMessage = new TextMessage(txt);
        PushMessage pushMessage = new PushMessage(sourceId,textMessage);
        try {
            Response<BotApiResponse> response = LineMessagingServiceBuilder
                    .create(lChannelAccessToken)
                    .build()
                    .pushMessage(pushMessage)
                    .execute();
            System.out.println(response.code() + " " + response.message());
        } catch (IOException e) {
            System.out.println("Exception is raised ");
            e.printStackTrace();
        }
    }

    private void leaveGR(String id, String type){
        try {
            if (type.equals("group")){
                Response<BotApiResponse> response = LineMessagingServiceBuilder
                        .create(lChannelAccessToken)
                        .build()
                        .leaveGroup(id)
                        .execute();
                System.out.println(response.code() + " " + response.message());
            } else if (type.equals("room")){
                Response<BotApiResponse> response = LineMessagingServiceBuilder
                        .create(lChannelAccessToken)
                        .build()
                        .leaveRoom(id)
                        .execute();
                System.out.println(response.code() + " " + response.message());
            }
        } catch (IOException e) {
            System.out.println("Exception is raised ");
            e.printStackTrace();
        }
    }
}
