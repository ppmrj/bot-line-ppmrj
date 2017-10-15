package com.ppmrj.linebot.Web_API.Model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class DivisiResponse {
    @JsonProperty("success") private boolean success;
    @JsonProperty("message") private String message;
    @JsonProperty("result") private ArrayList<Divisi> result;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public ArrayList<Divisi> getResult() {
        return result;
    }
}
