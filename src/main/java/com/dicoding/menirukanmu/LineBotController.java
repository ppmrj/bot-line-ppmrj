
package com.dicoding.menirukanmu;

import com.google.gson.Gson;
import com.linecorp.bot.client.LineMessagingService;
import com.linecorp.bot.client.LineMessagingServiceBuilder;
import com.linecorp.bot.client.LineSignatureValidator;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.model.response.BotApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;

@RestController
@RequestMapping(value="/linebot")
public class LineBotController
{
    ArrayList<Group> groups = new ArrayList<Group>();

    @Autowired
    @Qualifier("com.linecorp.channel_secret")
    String lChannelSecret;
    
    @Autowired
    @Qualifier("com.linecorp.channel_access_token")
    String lChannelAccessToken;

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
        if(aPayload!=null && aPayload.length() > 0)
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
            } else {
                msgText = payload.events[0].message.text;
                msgText = msgText.toLowerCase();

                /**
                 * Mafia minigame
                 */

                if(payload.events[0].source.type.equals("group")){
                    String source = payload.events[0].source.type;
                    String userid = payload.events[0].source.userId;
                    String groupid = payload.events[0].source.groupId;

                    if(searchGroupById(groupid) == null){
                        Group group = new Group(groupid, 0, 0);
                        groups.add(group);
                    }

                    if(msgText.equalsIgnoreCase("/credit")){
                        pushMessage(groupid, "Bot PPM RJ" +
                                "\n" +
                                "V0.1b" +
                                "\n" +
                                "\n" +
                                "Dibuat oleh Divisi IT PPM RJ.");
                    }
                    if(msgText.equalsIgnoreCase("/listgame")){
                        for(int i=0; i<Group.gameList.length; i++){
                            pushMessage(groupid, Group.gameList[i].toString()+ "\n");
                        }
                        pushMessage(groupid, "Untuk memulai game gunakan /main <nama game>.");
                    }

                    /**
                     * Game logic
                     */
                    Group currentGroup = searchGroupById(groupid);
                    if(msgText.equalsIgnoreCase("/berhenti")){
                        if(currentGroup != null){
                            if (currentGroup.getGAME_STATUS() != 0){
                                currentGroup.setGAME_STATUS(0);
                                pushMessage(groupid, "Game "+Group.gameList[currentGroup.getGAME_ID()]+ " telah diberhentikan.");
                                currentGroup.setGAME_ID(-1);
                            } else {
                                pushMessage(groupid, "Tidak ada game yang sedang dimainkan.");
                            }
                        } else {
                            System.out.println("Group unregistered.");
                        }
                    }
                    if(msgText.equalsIgnoreCase("/listpemain")){
                        if(currentGroup != null){
                            if (currentGroup.getGAME_STATUS() != 0){
                                String listPlayer = "";
                                int alive = 0;
                                for(User user: currentGroup.playerList){
                                    listPlayer += user.getName()+"\n";
                                    if(!user.getStatus().equalsIgnoreCase("Terciduk")){
                                        alive++;
                                    }
                                }
                                pushMessage(groupid, "Pemain yang masih bermain: "+alive+"/"+currentGroup.playerList.size());
                            } else {
                                pushMessage(groupid, "Tidak ada game yang sedang dimainkan.");
                            }
                        } else {
                            System.out.println("Group unregistered.");
                        }
                    }
                    try {
                        Response<UserProfileResponse> userProfile = LineMessagingServiceBuilder
                                .create(lChannelAccessToken)
                                .build()
                                .getProfile(userId)
                                .execute();
                        if(msgText.equalsIgnoreCase("/main mafia")){
                            if (currentGroup != null) {
                                if (currentGroup.getGAME_STATUS() == 0) {
                                    currentGroup.setGAME_ID(0);
                                    currentGroup.setGAME_STATUS(1);
                                    User user = new User(userId, userProfile.body().getDisplayName());
                                    currentGroup.addPlayerToList(user);
                                    pushMessage(groupid, userProfile.body().getDisplayName() + " telah memulai permainan Mafia. Ketik /join untuk mengikuti. Game akan dimulai dalam 3 menit.");
                                    currentGroup.startGame();
                                } else {
                                    pushMessage(groupid, "Permainan "+Group.gameList[currentGroup.getGAME_ID()] + " sedang berjalan.");
                                }
                            } else {
                                Group group = new Group(groupid, 1, 0);
                                User user = new User(userId, userProfile.body().getDisplayName());
                                currentGroup.addPlayerToList(user);
                                groups.add(group);
                                pushMessage(groupid, userProfile.body().getDisplayName() + " telah memulai permainan Mafia. Ketik /join untuk mengikuti. Game akan dimulai dalam 3 menit.");
                            }

                        }
                        if(msgText.equalsIgnoreCase("/join")){
                            if(currentGroup != null){
                                if(currentGroup.getGAME_STATUS() == 0){
                                    pushMessage(groupid, "Belum ada permainan yang dibuat. Ketik /listgame untuk melihat game yang tersedia.");
                                } else {
                                    int gameId = currentGroup.getGAME_ID();
                                    User user = new User(userId, userProfile.body().getDisplayName());
                                    currentGroup.addPlayerToList(user);
                                    pushMessage(groupid, userProfile.body().getDisplayName()+" bergabung ke permainan "+Group.gameList[gameId]+
                                    "\n\n" + currentGroup.playerList.size() + " pemain telah tergabung.");
                                }
                            }
                        }
                    } catch (IOException e) {
                        pushMessage(groupid, "Perintah gagal dijalankan. Pastikan kamu sudah menambahkan bot ini sebagai teman.");
                        e.printStackTrace();
                    }
                }

                /************* END OF MAFIA MINIGAME ************************/

                if (!msgText.contains("bot leave")){
//                    try {
////                        getMessageData(msgText, idTarget, userId);
//                    } catch (IOException e) {
//                        System.out.println("Exception is raised ");
//                        e.printStackTrace();
//                    }
                } else {
                    if (payload.events[0].source.type.equals("group")){
                        leaveGR(payload.events[0].source.groupId, "group");
                    } else if (payload.events[0].source.type.equals("room")){
                        leaveGR(payload.events[0].source.roomId, "room");
                    }
                }

            }
        }
         
        return new ResponseEntity<String>(HttpStatus.OK);
    }

    private Group searchGroupById(String groupId){
        for (Group group : groups) {
            if (group.getId().equals(groupId)) {
                return group;
            }
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
