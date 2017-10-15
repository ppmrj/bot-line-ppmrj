package com.ppmrj.linebot.Web_API.Model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

/**
 * Created by Asus on 10/15/2017.
 */
public class GrupResponse {
    @JsonProperty("success") private boolean success;
    @JsonProperty("message") private String message;
    @JsonProperty("result") private ArrayList<Grup> result;

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
