package com.ppmrj.linebot.Web_API.Model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

/**
 * Created by Asus on 10/15/2017.
 */
public class GroupResponse {
    private boolean success;
    private String message;
    private ArrayList<Group> result;

    public GroupResponse(@JsonProperty("success") boolean success, @JsonProperty("message") String message, @JsonProperty("result") ArrayList<Group> result) {
        this.success = success;
        this.message = message;
        this.result = result;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public ArrayList<Group> getResult() {
        return result;
    }
}
