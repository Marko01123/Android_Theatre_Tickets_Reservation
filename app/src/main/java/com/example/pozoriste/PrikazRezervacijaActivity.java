package com.example.pozoriste;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Base64;
import java.util.List;

import javax.crypto.SecretKey;

/*
    U ovoj aktivnosti, spisak svih zapisa rezervisanih sedišta za svaku projekciju za prijavljenog korisnika se ubacuje u listu putem
    metode iz baze. Ukoliko je ta lista prazna, ispisujemo poruku u jednom TextView-u da korisnik nema rezervisanih ulaznica. Ukoliko
    lista nije prazna, pravimo onoliko TextView-ova koliko ima zapisa iz baze i izbacujemo na ekran. Takođe, dohvatamo putem SharedPreferences
    podatak email koji je jedinstven za svakog korisnika i koji nam služi kao parametar u metodi iz baze za samu pretragu.
*/

public class PrikazRezervacijaActivity extends AppCompatActivity {
    private Database db;
    private SecretKey kljuc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prikaz_rezervacija);

        initComponents();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
        kljuc = null;
    }

    private void initComponents(){
        List<String> lista;
        db = new Database(this);
        kljuc = Ciphers.getAESKey();
        Ciphers c = new Ciphers();
        SharedPreferences sharedPref = getSharedPreferences("emailSHP", 0);
        byte[] sifratEmail = Base64.getDecoder().decode(sharedPref.getString("email", ""));

        lista = db.returnReservationDetails(new String(c.decryptAES(sifratEmail, kljuc)));
        LinearLayout layout = findViewById(R.id.rezervacije);

        if(lista.isEmpty()){
            TextView rezervacije = new TextView(this);
            String zapis = "Nemate rezervisanih ulaznica";
            rezervacije.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            textParams.setMargins(16,20,16,20);
            rezervacije.setLayoutParams(textParams);
            rezervacije.setTextColor(Color.BLACK);
            rezervacije.setBackgroundResource(R.drawable.border_show);
            rezervacije.setGravity(Gravity.CENTER);
            rezervacije.append(zapis);
            layout.addView(rezervacije);
        } else {
            for(String zapis : lista){
                TextView rezervacije = new TextView(this);
                rezervacije.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                textParams.setMargins(16,20,16,20);
                rezervacije.setLayoutParams(textParams);
                rezervacije.setTextColor(Color.BLACK);
                rezervacije.setBackgroundResource(R.drawable.border_show);
                rezervacije.setPadding(10,1,1,1);
                rezervacije.setGravity(Gravity.CENTER_VERTICAL);
                rezervacije.append(zapis);
                layout.addView(rezervacije);
            }
        }
    }
}