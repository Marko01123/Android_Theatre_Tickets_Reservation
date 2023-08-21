package com.example.pozoriste;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

/*
    Klasa koja zapise iz API-ja pretvara u domenski objekat
*/
public class Predstava{
    private String naslov, rezija, glumci, pisac, godina, zanr, slika, opis, vreme;

    public Predstava(String naslov, String rezija, String glumci, String pisac, String godina, String zanr, String slika, String opis, String vreme) {
        this.naslov = naslov;
        this.rezija = rezija;
        this.glumci = glumci;
        this.pisac = pisac;
        this.godina = godina;
        this.zanr = zanr;
        this.slika = slika;
        this.opis = opis;
        this.vreme = vreme;
    }

    public Predstava() {
    }

    public String getNaslov() {
        return naslov;
    }

    public void setNaslov(String naslov) {
        this.naslov = naslov;
    }

    public String getRezija() {
        return rezija;
    }

    public String getSlika() {
        return slika;
    }

    public void setSlika(String slika) {
        this.slika = slika;
    }

    public void setRezija(String rezija) {
        this.rezija = rezija;
    }

    public String getGlumci() {
        return glumci;
    }

    public void setGlumci(String glumci) {
        this.glumci = glumci;
    }

    public String getPisac() {
        return pisac;
    }

    public void setPisac(String pisac) {
        this.pisac = pisac;
    }

    public String getGodina() {
        return godina;
    }

    public void setGodina(String godina) {
        this.godina = godina;
    }

    public String getZanr() {
        return zanr;
    }

    public void setZanr(String zanr) {
        this.zanr = zanr;
    }

    public String getOpis() {
        return opis;
    }

    public void setOpis(String opis) {
        this.opis = opis;
    }

    public String getVreme() {
        return vreme;
    }

    public void setVreme(String vreme) {
        this.vreme = vreme;
    }

    public static Predstava parseJSONObject(JSONObject object){
        Predstava predstava = new Predstava();
        //parsira jedan JSON objekat u domenski objekat
        try{
            if(object.has("naslov")){
                predstava.setNaslov(object.getString("naslov"));
            }

            if(object.has("rezija")){
                predstava.setRezija(object.getString("rezija"));
            }

            if(object.has("glumci")){
                predstava.setGlumci(object.getString("glumci"));
            }

            if(object.has("pisac")){
                predstava.setPisac(object.getString("pisac"));
            }

            if(object.has("godina prvog izvodjenja")){
                predstava.setGodina(object.getString("godina prvog izvodjenja"));
            }

            if(object.has("zanr")){
                predstava.setZanr(object.getString("zanr"));
            }

            if(object.has("slika")){
                predstava.setSlika(object.getString("slika"));
            }

            if(object.has("opis")){
                predstava.setOpis(object.getString("opis"));
            }

            if(object.has("vreme")){
                predstava.setVreme(object.getString("vreme"));
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return predstava;
    }
    //parsira niz JSON objekata
    public static ArrayList<Predstava> parseJSONArray(JSONArray array){
        ArrayList<Predstava> predstave = new ArrayList<>();

        try{
            for(int i = 0; i < array.length(); i++){
                predstave.add(parseJSONObject(array.getJSONObject(i)));
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return predstave;
    }
}
