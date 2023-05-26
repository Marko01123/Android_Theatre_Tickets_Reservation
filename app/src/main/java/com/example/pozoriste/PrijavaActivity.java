package com.example.pozoriste;

import androidx.appcompat.app.AppCompatActivity;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.SecretKey;

public class PrijavaActivity extends AppCompatActivity implements View.OnClickListener{
    private final static String SHARED_PREF_CODE = "SharedPreferencesPozoristePrijava";
    private final static String SHARED_PREF_KEY_EMAIL = "email";
    private final static String SHARED_PREF_KEY_PASSWORD = "password";
    private final static String SHARED_PREF_KEY_UNEXPECTED_SHUTDOWN = "shudown";
    private static SecretKey kljuc;
    private static boolean prijavaClicked = false;
    private static boolean vratiSeClicked = false;

    private EditText inputEmail2;
    private EditText inputLozinka3;
    private Button buttonPrijava2;
    private Button buttonPovratak;
    private Database db;
    private KeyguardManager km;
    private PowerManager pm;
    private ImageButton buttonLozinka3;
    private boolean isPasswordVisible = false;
    private boolean isBlankScreen;

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
        buttonLozinka3 = findViewById(R.id.buttonVidiLozinku3);

        Context context = getApplicationContext();
        km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        isBlankScreen = false;

        db = new Database(this);

        buttonPrijava2.setOnClickListener(this);
        buttonPovratak.setOnClickListener(this);
        buttonLozinka3.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.buttonPrijava2:
                prijavaClicked = true;
                logIn();
                break;
            case R.id.buttonPovratak:
                vratiSeClicked = true;
                returnBack();
                break;
            case R.id.buttonVidiLozinku3:
                revealPassword(inputLozinka3);
                break;
        }
    }

    private void revealPassword(EditText passPolje){
        if(isPasswordVisible){
            passPolje.setTransformationMethod(PasswordTransformationMethod.getInstance());
            isPasswordVisible = false;
        } else {
            passPolje.setTransformationMethod(null);
            isPasswordVisible = true;
        }
        passPolje.setSelection(passPolje.getText().length());
    }

    @Override
    protected void onStart() {
        super.onStart();
        kljuc = Ciphers.getAESKey();
        prijavaClicked = false;
        vratiSeClicked = false;
        if(checkForUnexpectedShutdown()) {
            loadData();
            resetRestartAfterCrashFlag();
        }
        deleteStorage();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(prijavaClicked || vratiSeClicked){
            resetRestartAfterCrashFlag();
        } else {
            saveData();
            setRestartAfterCrashFlag();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isBlankScreen){
            loadData();
        }
        isBlankScreen = false;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if(!hasFocus && (km.inKeyguardRestrictedInputMode() || !pm.isInteractive())) {
            isBlankScreen = true;
            saveData();
            deleteFields();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        kljuc = null;
        db.close();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        vratiSeClicked = true;
        deleteStorage();
    }

    /*
        Proveravamo da li se u bazi nalazi korisnik sa unetim pristupnim paramterima. Ukoliko je u bazi, nastavlja se na
        PredstaveActivity gde šaljemo potrebne Stringove za nastavak projekta.
    */
    private void logIn(){
        Ciphers cipher = new Ciphers();
        UsersModel user = db.returnUserByEmail(inputEmail2.getText().toString());

        if(inputEmail2.getText().toString().equals("") || inputLozinka3.getText().toString().equals("")){
            Toast.makeText(this, "Unos email-a ili lozinke ne moze biti prazan.", Toast.LENGTH_LONG).show();
            return;
        }

        if(user.getEmail().equals(inputEmail2.getText().toString())){
            char[] hashLozinka = cipher.gen_hash(Base64.getDecoder().decode(user.getNonce()), inputLozinka3.getText().toString()).toCharArray();
            if(user.getLozinka().equals(new String(hashLozinka))) {
                Arrays.fill(hashLozinka, '\0');
                byte[] sifratEmail = cipher.encryptAES(inputEmail2.getText().toString().getBytes(), kljuc);
                Intent intent = new Intent(this, PredstaveActivity.class);
                Bundle extras = new Bundle();

                extras.putString("ime", user.getIme());
                extras.putString("prezime", user.getPrezime());
                extras.putString("email", Base64.getEncoder().encodeToString(sifratEmail));

                intent.putExtras(extras);
                startActivity(intent);
                deleteStorage();
                deleteFields();
            } else {
                Toast.makeText(this, "Lozinka koju ste uneli nije tacna.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Korisnik ne postoji u bazi. Molimo Vas registrujte se.", Toast.LENGTH_LONG).show();
        }
    }

    private void deleteFields(){
        inputEmail2.setText("");
        inputLozinka3.setText("");
    }

    private void returnBack(){
        startActivity(new Intent(this, RegistracijaActivity.class));
        deleteStorage();
        deleteFields();
    }

    /*
        Implementirano je cuvanje podataka putem SharedPreferences-a čisto da ako nam Android ubije aplikaciju, da se podaci ne unose
        iznova. Svaki podatak se sifruje AES/CBC/PKCS7Padding pre pucanja aplikacije i desifruje prilikom povratka u aplikaciju
     */
    private void loadData(){
        Ciphers c = new Ciphers();
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_CODE, 0);

        if(sharedPreferences.getString(SHARED_PREF_KEY_EMAIL, "").length() > 0){
            byte[] sifratEmail =  Base64.getDecoder().decode(sharedPreferences.getString(SHARED_PREF_KEY_EMAIL, ""));
            inputEmail2.setText(new String(c.decryptAES(sifratEmail, kljuc), StandardCharsets.UTF_8));
        }

        if(sharedPreferences.getString(SHARED_PREF_KEY_PASSWORD, "").length() > 0){
            byte[] sifratLozinka =  Base64.getDecoder().decode(sharedPreferences.getString(SHARED_PREF_KEY_PASSWORD, ""));
            inputLozinka3.setText(new String(c.decryptAES(sifratLozinka, kljuc), StandardCharsets.UTF_8));
            Arrays.fill(sifratLozinka, (byte) 0);
            sifratLozinka = null;
        }
    }

    private void saveData(){
        Ciphers c = new Ciphers();
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_CODE, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if(inputEmail2.getText().toString().length() > 0){
            byte[] sifratEmail = c.encryptAES((inputEmail2.getText().toString()).getBytes(StandardCharsets.UTF_8), kljuc);
            editor.putString(SHARED_PREF_KEY_EMAIL, Base64.getEncoder().encodeToString(sifratEmail));
        }

        if(inputLozinka3.getText().toString().length() > 0){
            byte[] sifratPassword = c.encryptAES((inputLozinka3.getText().toString()).getBytes(StandardCharsets.UTF_8), kljuc);
            editor.putString(SHARED_PREF_KEY_PASSWORD, Base64.getEncoder().encodeToString(sifratPassword));
        }

        editor.apply();
    }

    //Flagovi koji nam pomazu da proverimo da li je aplikacija prestala sa radom
    private boolean checkForUnexpectedShutdown(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_CODE, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(SHARED_PREF_KEY_UNEXPECTED_SHUTDOWN, false);
    }

    private void resetRestartAfterCrashFlag(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_CODE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SHARED_PREF_KEY_UNEXPECTED_SHUTDOWN, false);
        editor.apply();
    }

    private void setRestartAfterCrashFlag(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_CODE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SHARED_PREF_KEY_UNEXPECTED_SHUTDOWN, true);
        editor.apply();
    }

    private void deleteStorage(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_CODE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}