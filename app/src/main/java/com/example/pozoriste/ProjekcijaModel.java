package com.example.pozoriste;

import android.provider.BaseColumns;
/*
    Pomocna klasa koja služi za komunikaciju sa bazom
*/
public class ProjekcijaModel implements BaseColumns {
    public static final String TABLE_NAME = "projekcije"; //naziv tabele

    //nazivi kolona
    public static final String COLUMN_DATE = "datum";
    public static final String COLUMN_TIME = "vreme";
    public static final String PREDSTAVA_ID = "predstava_id";

    private String date, time;
    private int predstava_id;

    public ProjekcijaModel(String date, String time, int predstava_id) {
        this.date = date;
        this.time = time;
        this.predstava_id = predstava_id;
    }

    public ProjekcijaModel() {
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public int getPredstava_id() {
        return predstava_id;
    }

    @Override
    public String toString() {
        return "Projekcija{" +
                "date='" + date + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
