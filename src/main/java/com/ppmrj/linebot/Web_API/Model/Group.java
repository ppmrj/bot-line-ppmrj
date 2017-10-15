package com.ppmrj.linebot.Web_API.Model;

public class Group {
    private int id;
    private String id_grup_line;
    private String nama;
    private String status_game;
    private String tipe_grup;
    private int id_divisi;
    private String divisi;
    private String message;

    public Group(String id_grup_line, String nama, String status_game, String tipe_grup) {
        this.id_grup_line = id_grup_line;
        this.nama = nama;
        this.status_game = status_game;
        this.tipe_grup = tipe_grup;
    }

    public Group(int id, String id_grup_line, String nama, String status_game, String tipe_grup, int id_divisi) {
        this.id = id;
        this.id_grup_line = id_grup_line;
        this.nama = nama;
        this.status_game = status_game;
        this.tipe_grup = tipe_grup;
        this.id_divisi = id_divisi;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getId_grup_line() {
        return id_grup_line;
    }

    public void setId_grup_line(String id_grup_line) {
        this.id_grup_line = id_grup_line;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getStatus_game() {
        return status_game;
    }

    public void setStatus_game(String status_game) {
        this.status_game = status_game;
    }

    public String getTipe_grup() {
        return tipe_grup;
    }

    public void setTipe_grup(String tipe_grup) {
        this.tipe_grup = tipe_grup;
    }

    public int getId_divisi() {
        return id_divisi;
    }

    public void setId_divisi(int id_divisi) {
        this.id_divisi = id_divisi;
    }

    public String getDivisi() {
        return divisi;
    }

    public void setDivisi(String divisi) {
        this.divisi = divisi;
    }
}