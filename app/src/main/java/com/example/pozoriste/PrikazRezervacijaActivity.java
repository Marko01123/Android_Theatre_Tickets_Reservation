package com.example.pozoriste;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private int user_id = -1;
    private int projekcija_id = -1;
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
        projekcija_id = -1;
        user_id = -1;
    }

    private void initComponents(){
        List<String> lista;
        db = new Database(this);
        kljuc = Ciphers.getAESKey();
        Ciphers c = new Ciphers();
        SharedPreferences sharedPref = getSharedPreferences("emailSHP", 0);
        byte[] sifratEmail = Base64.getDecoder().decode(sharedPref.getString("email", ""));

        lista = db.returnReservationDetails(new String(c.decryptAES(sifratEmail, kljuc)));
        user_id = db.returnIdByEmail(new String(c.decryptAES(sifratEmail, kljuc)));
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
                LinearLayout linearLayout = new LinearLayout(this);
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                TextView rezervacije = new TextView(this);
                rezervacije.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.8f);
                textParams.setMargins(16,20,0,20);
                rezervacije.setLayoutParams(textParams);
                rezervacije.setTextColor(Color.BLACK);
                rezervacije.setBackgroundResource(R.drawable.border_show);
                rezervacije.setPadding(10,1,1,1);
                rezervacije.setGravity(Gravity.CENTER_VERTICAL);
                rezervacije.append(zapis);
                linearLayout.addView(rezervacije);

                String datumRegex = "Datum: (.*)";
                String vremeRegex = "Vreme: (.*)";
                Pattern datumPattern = Pattern.compile(datumRegex);
                Pattern vremePattern = Pattern.compile(vremeRegex);
                Matcher datumMatcher = datumPattern.matcher(zapis);
                Matcher vremeMatcher = vremePattern.matcher(zapis);
                String datum = "";
                String vreme = "";
                if (datumMatcher.find()) {
                    datum = datumMatcher.group(1);
                }
                if (vremeMatcher.find()) {
                    vreme = vremeMatcher.group(1);
                }
                projekcija_id = db.returnIdByDateAndTime(datum, vreme);

                ImageButton brisanje = new ImageButton(this);
                brisanje.setImageResource(R.drawable.trash);
                brisanje.setScaleType(ImageView.ScaleType.FIT_CENTER);
                LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.15f);
                imageParams.setMargins(16,20,16,20);
                brisanje.setBackgroundResource(R.drawable.border_delete);
                brisanje.setImageTintList(null);
                brisanje.setLayoutParams(imageParams);
                linearLayout.addView(brisanje);
                brisanje.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(PrikazRezervacijaActivity.this);
                        builder.setMessage("Da li ste sigurni da želite da obrišete rezervaciju?")
                                .setPositiveButton("Da", (dialogInterface, i) -> {
                                    db.deleteReservations(user_id, projekcija_id);
                                    user_id = -1;
                                    projekcija_id = -1;
                                    recreate();
                                })
                                .setNegativeButton("Ne", (dialogInterface, i) -> dialogInterface.dismiss());
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });
                layout.addView(linearLayout);
            }
        }
    }
}