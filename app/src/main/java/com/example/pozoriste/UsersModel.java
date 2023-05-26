package com.example.pozoriste;

import android.provider.BaseColumns;

public class UsersModel implements BaseColumns {
    public static final String TABLE_NAME = "users";

    public static final String COLUMN_NAME = "ime";
    public static final String COLUMN_SURNAME = "prezime";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "lozinka";
    public static final String COLUMN_BIRTH = "rodjendan";
    public static final String COLUMN_NONCE = "nonce";

    private String ime, prezime, email, lozinka, rodjendan, nonce;

    public UsersModel(String ime, String prezime, String email, String lozinka, String rodjendan, String nonce) {
        this.ime = ime;
        this.prezime = prezime;
        this.email = email;
        this.lozinka = lozinka;
        this.rodjendan = rodjendan;
        this.nonce = nonce;
    }

    public UsersModel() {
    }
    public String getIme() {
        return ime;
    }

    public String getPrezime() {
        return prezime;
    }

    public String getEmail() {
        return email;
    }

    public String getLozinka() {
        return lozinka;
    }

    public String getRodjendan() {
        return rodjendan;
    }

    public String getNonce() {
        return nonce;
    }

    @Override
    public String toString() {
        return "UsersModel{" +
                "ime='" + ime + '\'' +
                ", prezime='" + prezime + '\'' +
                ", email='" + email + '\'' +
                ", lozinka='" + lozinka + '\'' +
                ", rodjendan='" + rodjendan + '\'' +
                ", nonce='" + nonce + '\'' +
                '}';
    }
}
