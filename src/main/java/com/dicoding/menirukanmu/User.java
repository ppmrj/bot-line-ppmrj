package com.dicoding.menirukanmu;

import java.util.ArrayList;

/**
 * Created by Asus on 10/9/2017.
 */
public class User {
    private String id;
    private String name;
    private int role;
    private String status = "";
    int voted;


    private int position=0;
    private int diceRollStatus;
    private int diceNumber;

    static Object[][][] roles = {
            {
                    {0, "Mafia"},
                    {1, "Sherif"},
                    {2, "Dokter"},
                    {3, "Detektif"},
                    {4, "Preman"},
                    {5, "Warga"}
            },
    };

    ArrayList<String> playingGroupList = new ArrayList<String>();

    public User(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRole() {
        return role;
    }

    public String getRoleName(int gameID, int role) {
        return roles[gameID][role][1].toString();
    }

    public void setRole(int role) {
        this.role = role;
    }

    public void addGroupToPlayingList(String id){
        playingGroupList.add(id);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getDiceRollStatus() {
        return diceRollStatus;
    }

    public void setDiceRollStatus(int diceRollStatus) {
        this.diceRollStatus = diceRollStatus;
    }

    public int getDiceNumber() {
        return diceNumber;
    }

    public void setDiceNumber(int diceNumber) {
        this.diceNumber = diceNumber;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}

