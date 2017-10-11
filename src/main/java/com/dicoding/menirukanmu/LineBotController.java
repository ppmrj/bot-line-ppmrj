
package com.dicoding.menirukanmu;

import com.dicoding.menirukanmu.Responses.InfoResponse;
import com.dicoding.menirukanmu.Responses.RegistrasiResponse;
import com.google.gson.Gson;
import com.linecorp.bot.client.LineMessagingServiceBuilder;
import com.linecorp.bot.client.LineSignatureValidator;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.Action;
import com.linecorp.bot.model.action.PostbackAction;
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

import java.io.IOException;
import java.util.*;

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

    @RequestMapping(value="/newregistration", method=RequestMethod.POST)
    public ResponseEntity<String> newRegistration(
            @RequestBody String infoSantri
    ){
        if(infoSantri != null && infoSantri.length() > 0){
            System.out.println("Data santri: "+infoSantri);
        }

        Gson gson = new Gson();
        RegistrasiResponse registrasiResponse = gson.fromJson(infoSantri, RegistrasiResponse.class);

        pushMessage(groups.get(0).getId(), "Santri baru dengan nama: "+registrasiResponse.nama+" telah melakukan pendaftaran di website. ID Pendaftara: "+registrasiResponse.id);
        return new ResponseEntity<String>(HttpStatus.OK);
    }

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

            if (!payload.events[0].message.type.equals("text")){
                replyToUser(payload.events[0].replyToken, "Unknown message");
            } else if (payload.events[0].message.type.equals("postback")) {
                pushMessage(idTarget, payload.events[0].postbackContent.getData());
            } else {
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

                    /**
                     * Mafia minigame
                     * @author Irfan Abyan 2017
                     */
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
                        for (int i = 0; i < Group.gameList.length; i++) {
                            replyToUser(replyToken, Group.gameList[i][1].toString() + "\n");
                        }
                        pushMessage(groupid, "Untuk memulai game gunakan \n/main <nama game>.");
                    }
                    if (msgText.equalsIgnoreCase("/berhenti")) {
                        if (currentGroup != null) {
                            if (currentGroup.getGAME_STATUS() != 0) {
                                currentGroup.playerList.clear();
                                currentGroup.setGAME_STATUS(0);
                                replyToUser(replyToken, "Game " + Group.gameList[currentGroup.getGAME_ID()][1].toString() + " telah diberhentikan.");
                                currentGroup.setGAME_ID(-1);
                            } else {
                                replyToUser(replyToken, "Tidak ada game yang sedang dimainkan.");
                            }
                        } else {
                            System.out.println("Group unregistered.");
                        }
                    }
                    if (msgText.equalsIgnoreCase("/listpemain")) {
                        if (currentGroup != null) {
                            if (currentGroup.getGAME_STATUS() != 0) {
                                StringBuilder listPlayer = new StringBuilder();
                                int alive = 0;
                                if (currentGroup.playerList != null) {
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
                                    replyToUser(replyToken, listPlayer.toString());
                                    pushMessage(groupid, "Pemain yang masih bermain: " + alive + "/" + currentGroup.playerList.size());
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
                        }
                        if(currentGroup != null){
                            if(currentGroup.GAME_STATUS == 0) {
                                User user = getUserProfile(userId);
                                if(user != null){
                                    currentGroup.GAME_STATUS = 1;
                                    currentGroup.GAME_ID = 1;
                                    currentGroup.addPlayerToList(user);
                                    pushMessage(groupid, user.getName() + " telah memulai permainan "+Group.gameList[currentGroup.GAME_ID][1].toString()+". Ketik /join untuk bergabung. Game akan dimulai dalam "+currentGroup.PREGAME_TIME/60+" menit." +
                                            "\n" +
                                            "Minimal pemain: "+Group.gameList[currentGroup.GAME_ID][2]);
                                    startGame(currentGroup);
                                }
                            }
                        }
                    }
                    if (msgText.equalsIgnoreCase("/main mafia")) {
                        if(userId == null){
                            replyToUser(replyToken, "Kamu belum mengupdate versi linemu ke yang paling baru. Update linemu terlebih dahulu.");
                        }
                        if (currentGroup != null) {
                            if (currentGroup.getGAME_STATUS() == 0) {
                                User user = getUserProfile(userId);
                                if(user != null){
                                    if (!user.getId().equals("0")) {
                                        currentGroup.setGAME_ID(0);
                                        currentGroup.setGAME_STATUS(1);
                                        currentGroup.addPlayerToList(user);
                                        pushMessage(groupid, user.getName() + " telah memulai permainan "+Group.gameList[currentGroup.GAME_ID][1].toString()+". Ketik /join untuk bergabung. Game akan dimulai dalam "+currentGroup.PREGAME_TIME/60+" menit." +
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
                                replyToUser(replyToken, "Permainan " + Group.gameList[currentGroup.getGAME_ID()][1].toString() + " sedang berjalan.");
                            }
                        } else {
                            Group group = new Group(groupid, 1, 0);
                            User user = getUserProfile(userId);
                            if (user != null) {
                                if (!user.getId().equals("0")) {
                                    group.addPlayerToList(user);
                                    groups.add(group);
                                    pushMessage(groupid, user.getName() + " telah memulai permainan "+Group.gameList[group.GAME_ID][1].toString()+". Ketik /join untuk bergabung. Game akan dimulai dalam 3 menit.");
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
                        }
                        if (currentGroup != null) {
                            if (currentGroup.getGAME_STATUS() == 0) {
                                replyToUser(replyToken, "Belum ada permainan yang dibuat. Ketik /listgame untuk melihat game yang tersedia.");
                            } else {
                                User user = getUserProfile(userId);
                                if (user != null) {
                                    if (!user.getId().equals("0")) {
                                        if(checkIfUserJoined(userId, currentGroup.playerList)){
                                            replyToUser(replyToken, "Kamu sudah tergabung ke dalam game, "+user.getName());
                                        } else {
                                            int gameId = currentGroup.getGAME_ID();
                                            currentGroup.addPlayerToList(user);
                                            pushMessage(groupid, user.getName() + " bergabung ke permainan " + Group.gameList[gameId][1].toString() +
                                                    "\n\n" + currentGroup.playerList.size() + " pemain telah tergabung.");
                                        }
                                        if(currentGroup.playerList.size() == (int) Group.gameList[currentGroup.GAME_ID][3]){
                                            currentGroup.GAME_STATUS = 1;
                                        }
                                    } else {
                                        replyToUser(replyToken, "Kamu belum mengupdate versi linemu ke yang paling baru. Update linemu terlebih dahulu.");
                                    }
                                } else {
                                    replyToUser(replyToken, "Kamu belum menambahkan bot sebagai teman. Silahkan tambahkan bot sebagai teman dahulu.");
                                }
                            }
                        }
                    }
                    if(msgText.equalsIgnoreCase("/kocokdadu")){
                        if(userId == null){
                            replyToUser(replyToken, "Kamu belum mengupdate versi linemu ke yang paling baru. Update linemu terlebih dahulu.");
                        }
                        if (currentGroup != null) {
                            if (currentGroup.getGAME_STATUS() == 0) {
                                replyToUser(replyToken, "Belum ada permainan yang dibuat. Ketik /listgame untuk melihat game yang tersedia.");
                            } else {
                                User user = getUserProfile(userId);
                                if (user != null) {
                                    if(!user.getId().equalsIgnoreCase(currentGroup.playerList.get(0).getId())){
                                        replyToUser(replyToken, "Sekarang bukan giliranmu, "+user.getName()+".");
                                    } else {
                                        int gameId = currentGroup.getGAME_ID();
                                        Random random = new Random();
                                        int dice = random.nextInt(6) + 1;
                                        user.setDiceNumber(dice);
                                        user.setDiceRollStatus(1);
                                        pushMessage(currentGroup.getId(), user.getName()+" mengocok dadu...\n.\n.\n.\nHasilnya "+dice+".");
                                    }
                                } else {
                                    replyToUser(replyToken, "Kamu belum menambahkan bot sebagai teman. Silahkan tambahkan bot sebagai teman dahulu.");
                                }
                            }
                        }
                    }
//                    try {
//                        Response<UserProfileResponse> userProfile = LineMessagingServiceBuilder
//                                .create(lChannelAccessToken)
//                                .build()
//                                .getProfile(userId)
//                                .execute();
//
//                    } catch (IOException e) {
//                        pushMessage(groupid, "Perintah gagal dijalankan. Pastikan kamu sudah menambahkan bot ini sebagai teman.");
//                        e.printStackTrace();
//                    }
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

            }
        }

        return new ResponseEntity<String>(HttpStatus.OK);
    }


    public void startGame(Group group){
        if(group.GAME_ID == 0){
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
                            for(int i=0; i<User.roles.length-1; i++){
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
                                int index = i;
                                List<Action> listUser = new ArrayList<>();
                                for(int j=i; j<=i+4; j++){
                                    listUser.add(new PostbackAction(group.playerList.get(j).getName(), group.playerList.get(j).getId()+"&groupId="+group.getId()));
                                }
                                ButtonsTemplate buttonsTemplate = new ButtonsTemplate("N/A", "Voting pemain", "Pilih pemain yang ingin diciduk", listUser);
                                votingMessage(group.playerList.get(i).getId(), buttonsTemplate);
                                group.GAME_JUST_BEGIN = 0;
                            }
                            group.VOTING_STARTED = 1;
                        } else {
                            group.VOTING_TIME--;
                            if(group.VOTING_TIME == 30)
                                pushMessage(group.getId(), "Voting akan berakhir dalam 30 detik.");
                            else if(group.VOTING_TIME == 10)
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
        } else if(group.GAME_ID == 1){
            Timer timer = new Timer();
            int[][] ladderArray = {
                    {3, 12},
                    {7, 32},
                    {25, 45},
                    {37, 78},
                    {42, 54},
                    {69, 94},
                    {82, 90}
            };
            int[][] snakeArray = {
                    {10, 5},
                    {36, 22},
                    {57, 31},
                    {29, 3},
                    {49, 25},
                    {98, 78},
                    {63, 33}
            };
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if(group.GAME_STATUS == 1){
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
                            pushMessage(group.getId(), "Game "+Group.gameList[group.GAME_ID][1].toString()+" dimulai dengan "+group.playerList.size()+" pemain");
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
                    } else if(group.GAME_STATUS == 2){
                        User currentPlayer = group.playerList.get(0);
                        if(group.GAME_JUST_BEGIN == 1){
                            pushMessage(group.getId(), "Game akan dimulai...");
                            pushMessage(group.getId(), "Setiap pemain diharuskan mengocok dadu dengan batas waktu 10 detik dan maju sesuai hasil dari angka dadu." +
                                    "\nApabila waktu habis maka pemain mendapat satu pelanggaran.");
                            pushMessage(group.getId(), "Jika pemain mendapatkan "+group.MAX_STRIKE+" maka dia akan otomatis dikeluarkan.");
                            pushMessage(group.getId(), "Pemain pertama adalah "+group.playerList.get(0).getName());
                        }
                        group.GAME_JUST_BEGIN = 0;
                        if(currentPlayer.getDiceRollStatus() == 1) {
                            pushMessage(group.getId(), currentPlayer.getName() + " telah mengocok dadu dan hasilnya adalah: " + currentPlayer.getDiceNumber());
                            currentPlayer.setPosition(currentPlayer.getPosition() + currentPlayer.getDiceNumber());
                            if (currentPlayer.getPosition() > 100) {
                                int mundur = currentPlayer.getPosition() - 100;
                                currentPlayer.setPosition(currentPlayer.getPosition() - mundur);
                                pushMessage(group.getId(), "Hasil kocokan dadu melebihi 100 karenanya" + currentPlayer.getName() + " mundur lagi ke " + currentPlayer.getPosition());
                            } else if (currentPlayer.getPosition() == 100) {
                                pushMessage(group.getId(), currentPlayer.getName() + " berhasil memenangkan game karena mencapai kotak nomor 100.");
                                group.GAME_STATUS = 3;
                            } else if (currentPlayer.getPosition() < 100){
                                pushMessage(group.getId(), currentPlayer.getName() + " maju " + currentPlayer.getDiceNumber() + " langkah ke kotak nomor" + currentPlayer.getPosition());
                            }
                            currentPlayer.setDiceRollStatus(0);
                            group.ROLLING_TIME = 30;
                            Collections.rotate(group.playerList, -1);
                        } else {
                            if(group.ROLLING_TIME == 30){
                                pushMessage(group.getId(), currentPlayer.getName()+" silahkan mengocok dadu dengan /kocokdadu.");
                            } else if(group.ROLLING_TIME == 5) {
                                pushMessage(group.getId(), currentPlayer.getName()+" silahkan mengocok dadu dengan /kocokdadu. Waktu tinggal 5 detik.");
                            } else if(group.ROLLING_TIME == 2) {
                                pushMessage(group.getId(), currentPlayer.getName()+" silahkan mengocok dadu dengan /kocokdadu. Waktu tinggal 2 detik.");
                            } else if(group.ROLLING_TIME == 0) {
                                if(currentPlayer.getDiceRollStatus() == 0){
                                    pushMessage(group.getId(), currentPlayer.getName()+" gagal mengocok dadu dalam batas waktu yang ditentukan.");
                                }
                                group.ROLLING_TIME = 30;
                                Collections.rotate(group.playerList, -1);
                            }
                            group.ROLLING_TIME--;
                        }
                    } else if(group.GAME_STATUS == 3){
                        pushMessage(group.getId(), "Game telah berakhir.");
                        group.playerList.clear();
                        group.GAME_STATUS = 0;
                    }
                }
            },0, 1000);
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
                return new User(response.body().getUserId(), response.body().getDisplayName());
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

    private void votingMessage(String sourceId, ButtonsTemplate template){
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
