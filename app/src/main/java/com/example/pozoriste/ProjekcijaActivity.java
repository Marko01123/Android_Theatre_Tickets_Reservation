package com.example.pozoriste;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
/*
    Klasa ProjekcijaActivity služi da dovuče sliku sa linka iz API-ja, da ispiše sve ostale podatke o toj jednoj predstavi koja je u
    aktivnosti PredstaveActivity izabrana. Ovde se ta predstava upisuje u bazu kao i projekcija ili projekcije u zavisnosti od toga
    koliko ima projekcija te jedne predstave.
*/
public class ProjekcijaActivity extends AppCompatActivity implements View.OnClickListener{
    private TextView labelNaslovPredstave, labelGlumci, labelRezija, labelPisac, labelGodina, labelZanr, labelOpisTekst;
    private ImageView prikazSlike;
    private Database db;
    private int projekcija_id;
    private String sifratID;
    private String naslov;
    private List<String> vremeBaza;
    private int brojac = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projekcija);

        initComponents();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }

    private void initComponents(){
        labelNaslovPredstave = findViewById(R.id.labelNaslovPredstave);
        labelGlumci = findViewById(R.id.labelGlumci);
        labelRezija = findViewById(R.id.labelRezija);
        labelPisac = findViewById(R.id.labelPisac);
        labelGodina = findViewById(R.id.labelGodina);
        labelZanr = findViewById(R.id.labelZanr);
        labelOpisTekst = findViewById(R.id.labelOpisTekst);
        prikazSlike = findViewById(R.id.prikazSlike);
        db = new Database(this);
        vremeBaza = new ArrayList<>();

        Bundle extras = getIntent().getExtras();
        naslov = extras.getString("naslov");
        String rezija = extras.getString("rezija");
        String glumci = extras.getString("glumci");
        String pisac = extras.getString("pisac");
        String godina = extras.getString("godina");
        String zanr = extras.getString("zanr");
        String opis = extras.getString("opis");
        String vremenaProjekcija = extras.getString("vreme");
        sifratID = extras.getString("user_id");
        final String URL = extras.getString("slika");

        labelNaslovPredstave.append(naslov);

        String rezijaLabel = "Rezija: "+rezija;
        labelRezija.append(rezijaLabel);

        String glumciLabel = "Glumci: "+glumci;
        labelGlumci.append(glumciLabel);

        String pisacLabel = "Pisac: "+pisac;
        labelPisac.append(pisacLabel);

        String godinaLabel = "Godina prvog prikazivanja u Narodnom pozoristu: "+godina;
        labelGodina.append(godinaLabel);

        String zanrLabel = "Zanr: "+zanr;
        labelZanr.append(zanrLabel);

        labelOpisTekst.append(opis);

        Picasso.get().load(URL).into(prikazSlike);

        LinearLayout layout = findViewById(R.id.projekcije);

        if(!(db.returnShowTitle(naslov))){
            db.addShow(naslov, rezija, glumci);
        }

        /*
            Formatiramo zapis datuma i vremena projekcija dobijenih iz API-ja, ubacujemo u bazu, prethodno vršeći proveru da se ne
            stvore dupli zapisi u projekcija tabeli i vršimo ispis istih u TextView iznad svakog dugmeta za rezervaciju koji vodi
            na RezervacijaActivity gde se vrši rezervacija sedišta za tu projekciju.
        */
        String[] svaVremena = vremenaProjekcija.trim().split(";");

        for(String vreme : svaVremena){
            String[] datumVreme = vreme.trim().split(",");
            TextView vremeZapis = new TextView(layout.getContext());
            String zapis = "";
            brojac++;

            for(int i = 0; i < datumVreme.length-1; i++){
                vremeZapis.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                textParams.setMargins(16,0,16,5);
                vremeZapis.setLayoutParams(textParams);
                vremeZapis.setTextColor(Color.BLACK);
                vremeZapis.setGravity(Gravity.CENTER_VERTICAL);
                zapis += datumVreme[i] + datumVreme[1];
                vremeZapis.append(zapis);
            }

            vremeBaza.addAll(Arrays.asList(vremeZapis.getText().toString().split("  ")));

            if(brojac == 1){
                if(!db.returnDateAndTime(vremeBaza.get(0), vremeBaza.get(1))){
                    db.addProjection(vremeBaza.get(0), vremeBaza.get(1), db.returnShowIDBasedOnShowTitle(naslov));
                }
            }

            layout.addView(vremeZapis);

            Button rezervacija = new Button(layout.getContext());
            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            rezervacija.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            buttonParams.setMargins(20, 5, 20, 30);
            rezervacija.setLayoutParams(buttonParams);
            rezervacija.setBackgroundColor(Color.BLUE);
            rezervacija.setTextColor(Color.WHITE);
            rezervacija.setText("Rezervisi");
            rezervacija.setId(brojac);
            layout.addView(rezervacija);

            rezervacija.setOnClickListener(this);
        }

        if(brojac == 2){
            if(!db.returnDateAndTime(vremeBaza.get(2), vremeBaza.get(3))){
                db.addProjection(vremeBaza.get(2), vremeBaza.get(3), db.returnShowIDBasedOnShowTitle(naslov));
            }
        }
    }

    /*
        Proveravamo ukoliko ima više projekcija za izabranu predstavu da se nakon toga odgovarajući paramteri prosleđuju u
        RezervacijeActivity.
    */
    @SuppressLint("ResourceType")
    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this, RezervacijaActivity.class);

        Bundle extras = new Bundle();
        if(view.getId() == 1){
            projekcija_id = db.returnIdByDateAndTime(vremeBaza.get(0), vremeBaza.get(1));
            extras.putString("datum", vremeBaza.get(0));
            extras.putString("vreme", vremeBaza.get(1));
        }

        if(view.getId() == 2){
            projekcija_id = db.returnIdByDateAndTime(vremeBaza.get(2), vremeBaza.get(3));
            extras.putString("datum", vremeBaza.get(2));
            extras.putString("vreme", vremeBaza.get(3));
        }

        extras.putString("naslov", naslov);
        extras.putInt("projekcija_id", projekcija_id);
        extras.putString("user_id", sifratID);
        intent.putExtras(extras);
        startActivity(intent);
    }
}