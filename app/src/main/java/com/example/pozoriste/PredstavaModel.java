package com.example.pozoriste;

import android.provider.BaseColumns;
/*
    Pomocna klasa koja služi za komunikaciju sa bazom
*/
public class PredstavaModel implements BaseColumns {
    public static final String TABLE_NAME = "predstave"; //naziv tabele

    //nazivi kolona
    public static final String COLUMN_TITLE = "naslov";
    public static final String COLUMN_DIRECTOR = "rezija";
    public static final String COLUMN_ACTORS = "glumci";

    private String naslov, rezija, glumci;

    public String getNaslov() {
        return naslov;
    }

    public String getRezija() {
        return rezija;
    }

    public String getGlumci() {
        return glumci;
    }

    public PredstavaModel(String naslov, String rezija, String glumci) {
        this.naslov = naslov;
        this.rezija = rezija;
        this.glumci = glumci;
    }

    public PredstavaModel() {
    }

    @Override
    public String toString() {
        return "PredstavaModel{" +
                "naslov='" + naslov + '\'' +
                ", rezija='" + rezija + '\'' +
                ", glumci='" + glumci + '\'' +
                '}';
    }
}
