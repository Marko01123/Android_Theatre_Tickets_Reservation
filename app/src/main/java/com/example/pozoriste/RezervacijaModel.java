package com.example.pozoriste;

import android.provider.BaseColumns;
/*
    Pomocna klasa koja služi za komunikaciju sa bazom
*/
public class RezervacijaModel implements BaseColumns {
    public static final String TABLE_NAME = "rezervacije"; //ime tabele

    //nazivi kolona
    public static final String COLUMN_SEAT = "sediste";
    public static final String USER_ID = "users_id";
    public static final String PROJEKCIJA_ID = "projekcija_id";

    private String sediste;
    private int korisnik_id, projekcija_id;

    public RezervacijaModel(String sediste, int korisnik_id, int projekcija_id) {
        this.sediste = sediste;
        this.korisnik_id = korisnik_id;
        this.projekcija_id = projekcija_id;
    }

    public RezervacijaModel() {
    }

    public String getSediste() {
        return sediste;
    }

    public int getKorisnik_id() {
        return korisnik_id;
    }

    public int getProjekcija_id() {
        return projekcija_id;
    }

    @Override
    public String toString() {
        return "RezervacijaModel{" +
                "sediste='" + sediste + '\'' +
                ", korisnik_id=" + korisnik_id +
                ", projekcija_id=" + projekcija_id +
                '}';
    }
}
