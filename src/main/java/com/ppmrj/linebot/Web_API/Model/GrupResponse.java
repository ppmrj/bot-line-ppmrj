package com.ppmrj.linebot.Web_API.Model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

/**
 * Created by Asus on 10/15/2017.
 */
public class GrupResponse {
    private boolean success;
    private String message;
    private ArrayList<Grup> result;

    public GrupResponse(@JsonProperty("success") boolean success, @JsonProperty("message") String message, @JsonProperty("result") ArrayList<Grup> result) {
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

    public ArrayList<Grup> getResult() {
        return result;
    }
}
