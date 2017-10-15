package com.ppmrj.linebot.Responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ppmrj.linebot.Model.Group;

import java.util.ArrayList;

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
