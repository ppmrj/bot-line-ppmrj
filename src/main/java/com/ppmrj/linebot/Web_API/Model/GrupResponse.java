package com.ppmrj.linebot.Web_API.Model;

import java.util.ArrayList;

/**
 * Created by Asus on 10/15/2017.
 */
public class GrupResponse {
    private boolean success;
    private String message;
    private ArrayList<Grup> result;

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
