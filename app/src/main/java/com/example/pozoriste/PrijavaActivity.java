package com.example.pozoriste;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

public class PrijavaActivity extends AppCompatActivity implements View.OnClickListener{
    private final static String SHARED_PREF_CODE = "SharedPreferencesPozoristePrijava";
    private final static String SHARED_PREF_KEY_EMAIL = "email";
    private final static String SHARED_PREF_KEY_PASSWORD = "password";

    private EditText inputEmail2;
    private EditText inputLozinka3;
    private Button buttonPrijava2;
    private Button buttonPovratak;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prijava);

        initComponents();
    }

    private void initComponents(){
        inputEmail2 = findViewById(R.id.inputEmail2);
        inputLozinka3 = findViewById(R.id.inputLozinka3);
        buttonPrijava2 = findViewById(R.id.buttonPrijava2);
        buttonPovratak = findViewById(R.id.buttonPovratak);

        buttonPrijava2.setOnClickListener(this);
        buttonPovratak.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.buttonPrijava2:
                prijaviSe();
                break;
            case R.id.buttonPovratak:
                vratiSe();
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        ucitajPodatke();
    }

    @Override
    protected void onStop() {
        super.onStop();
        sacuvajPodatke();
    }
    /*
        Proveravamo da li se u bazi nalazi korisnik sa unetim pristupnim paramterima. Ukoliko je u bazi, nastavlja se na
        PredstaveActivity gde šaljemo potrebne Stringove za nastavak projekta.
     */
    private void prijaviSe(){
        Database db = new Database(this);
        UsersModel user = db.returnUserByEmail(inputEmail2.getText().toString(), inputLozinka3.getText().toString());

        if(user.getEmail().equals(inputEmail2.getText().toString()) && user.getLozinka().equals(inputLozinka3.getText().toString())){
            Intent intent = new Intent(this, PredstaveActivity.class);
            Bundle extras = new Bundle();

            extras.putString("ime", user.getIme());
            extras.putString("prezime", user.getPrezime());
            extras.putString("email", user.getEmail());
            /*extras.putString("lozinka", user.getLozinka());
            extras.putString("rodjendan", user.getRodjendan());*/

            intent.putExtras(extras);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Ne postoji nalog sa unetim parametrima. Pokusajte opet ili se registrujte.", Toast.LENGTH_LONG).show();
        }
    }

    private void vratiSe(){
        startActivity(new Intent(this, MainActivity.class));
    }

    /*
        Implementirano je cuvanje podataka putem SharedPreferences-a čisto da ako nam Android ubije aplikaciju, da se podaci ne
        unose iznova.
     */
    private void ucitajPodatke(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_CODE, 0);
        String email = sharedPreferences.getString(SHARED_PREF_KEY_EMAIL, "");
        String lozinka = sharedPreferences.getString(SHARED_PREF_KEY_PASSWORD, "");

        inputEmail2.setText(email);
        inputLozinka3.setText(lozinka);
    }

    private void sacuvajPodatke(){
        String email = inputEmail2.getText().toString();
        String lozinka = inputLozinka3.getText().toString();

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_CODE, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(SHARED_PREF_KEY_EMAIL, email);
        editor.putString(SHARED_PREF_KEY_PASSWORD, lozinka);

        editor.commit();
    }
}