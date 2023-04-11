package com.example.pozoriste;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONArray;

import java.util.ArrayList;

public class PredstaveActivity extends AppCompatActivity implements View.OnClickListener{
    private TextView labelPredstave;
    private Button buttonRezervacije, buttonOdjaviSe;
    private int user_id;
    private String ime, prezime, email;
    private final String url = "https://marko01123.github.io/singidunum_test/pozoriste_api.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_predstave);

        initComponents();
    }

    private void initComponents(){
        labelPredstave = findViewById(R.id.labelPredstave);
        buttonRezervacije = findViewById(R.id.buttonRezervacije);
        buttonOdjaviSe = findViewById(R.id.buttonOdjaviSe);

        Bundle extras = getIntent().getExtras();
        ime = extras.getString("ime");
        prezime = extras.getString("prezime");
        email = extras.getString("email");
        /*String lozinka = extras.getString("lozinka");
        String rodjendan = extras.getString("rodjendan");*/

        String predstaveTekst = ime + " " + prezime+", dobrodosli.";
        labelPredstave.append(predstaveTekst);

        buttonRezervacije.setOnClickListener(this);
        buttonOdjaviSe.setOnClickListener(this);

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.predstava, null);

        LinearLayout mLinearLayout = (LinearLayout)findViewById(R.id.predstave);
        mLinearLayout.addView(view);

        Database db = new Database(this);
        user_id = db.returnIdByEmail(email);

        dohvatiPodatke();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.buttonRezervacije:
                prikazRezervacija();
                break;
            case R.id.buttonOdjaviSe:
                odjaviSe();
                break;
        }
    }

    private void odjaviSe(){
        startActivity(new Intent(this, MainActivity.class));
    }

    private void prikazRezervacija(){
        Intent intent = new Intent(this, PrikazRezervacijaActivity.class);

        SharedPreferences sharedPred = getSharedPreferences("emailSHP", 0);
        SharedPreferences.Editor editor = sharedPred.edit();
        editor.putString("email", email);
        editor.apply();

        startActivity(intent);
    }

    /*
        Metoda u kojoj override-ujemo metodu handleMessage i koja sluzi da dohvati podatke iz API-ja, ubaci ih u listu i zatim prikaže
        u dinamički kreiranim TextView-ovima naslove predstava. Klikom na naslov predstave, dovučeni podaci iz API-ja se šalju u
        ProjekcijaActivity gde se vrši ispis svih detalja iz API-ja o toj predstavi. Prosleđujemo i korisnikov ID iz baze koji će
        nam kasnije trebati pri rezervaciji sedišta zbog stranog ključa u tabeli rezervacije.
     */
    private void dohvatiPodatke(){
        Api.getJSON(url, new ReadDataHandler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                String odgovor = getJson();

                try {
                    JSONArray array = new JSONArray(odgovor);
                    ArrayList<Predstava> predstave = Predstava.parseJSONArray(array);
                    LinearLayout layout = findViewById(R.id.predstava);

                    for(Predstava predstava : predstave){
                        TextView nazivPredstave = new TextView(layout.getContext());
                        nazivPredstave.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
                        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        textParams.setMargins(16,25,16,30);
                        nazivPredstave.setLayoutParams(textParams);
                        nazivPredstave.setTextColor(Color.BLACK);
                        nazivPredstave.setBackgroundResource(R.drawable.border_show);
                        nazivPredstave.setGravity(Gravity.CENTER);
                        nazivPredstave.setPadding(1, 35, 1, 35);
                        nazivPredstave.append(predstava.getNaslov());
                        layout.addView(nazivPredstave);

                        nazivPredstave.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(view.getContext(), ProjekcijaActivity.class);
                                Bundle extras = new Bundle();

                                extras.putString("naslov", predstava.getNaslov());
                                extras.putString("rezija", predstava.getRezija());
                                extras.putString("glumci", predstava.getGlumci());
                                extras.putString("pisac", predstava.getPisac());
                                extras.putString("godina", predstava.getGodina());
                                extras.putString("zanr", predstava.getZanr());
                                extras.putString("slika", predstava.getSlika());
                                extras.putString("opis", predstava.getOpis());
                                extras.putString("vreme", predstava.getVreme());
                                extras.putInt("user_id", user_id);

                                intent.putExtras(extras);
                                startActivity(intent);
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}