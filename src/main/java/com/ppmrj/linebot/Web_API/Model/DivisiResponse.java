package com.ppmrj.linebot.Web_API.Model;

import java.util.ArrayList;

public class DivisiResponse {
    private boolean success;
    private String message;
    private ArrayList<Divisi> result;

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
