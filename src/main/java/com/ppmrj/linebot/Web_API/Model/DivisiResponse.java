package com.ppmrj.linebot.Web_API.Model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class DivisiResponse {
    private boolean success;
    private String message;
    private ArrayList<Divisi> result;

    public DivisiResponse(@JsonProperty("success") boolean success, @JsonProperty("message") String message, @JsonProperty("result") ArrayList<Divisi> result) {
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

    public ArrayList<Divisi> getResult() {
        return result;
    }
}
