package com.example.pozoriste;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.crypto.SecretKey;

/*
    Projekat Pozorište simulira rezervaciju karata za predstave u Narodnom pozorištu. Sastoji se iz šest aktivnosti:
    -*registracija* korisnika
    -ako ima nalog, *prijava* korisnika
    -"home" gde se nalazi spisak svih *predstava* koje se daju u pozorištu, dovučene iz napravljenog API-ja
    -kada se odabere predstava, izbacuju se detaljnije informacije o samoj predstavi gde u dnu može da se odabere
    *projekcija* da bi se rezervisale karte
    -aktivnost *rezervacija* koji sadrži proizvoljan raspored sedišta u sali gde se karte za tu projekciju rezervišu
    -*prikaz rezervacija* do kojeg se dolazi iz aktivnosti predstave gde se iz baze citaju sve rezervacije prijavljenog korisnika.
    Aplikacija sadrži bazu podataka pozoriste_database koja sadrži četiri tabele:
    -users odnosno korisnici
    -predstave
    -projekcije
    -rezervacije
    API se nalazi na adresi: https://marko01123.github.io/singidunum_test/pozoriste_api.json
    Minimum API level za pokretanje aplikacije je 26.
    Projekat sadrži komentare iznad bitnijih metoda radi lakšeg praćenja koda.

*/
public class RegistracijaActivity extends AppCompatActivity implements View.OnClickListener{
    private final static String SHARED_PREF_CODE = "SharedPreferencesPozoristeRegistracija";
    private final static String SHARED_PREF_KEY_IME = "ime";
    private final static String SHARED_PREF_KEY_PREZIME = "prezime";
    private final static String SHARED_PREF_KEY_EMAIL = "email";
    private final static String SHARED_PREF_KEY_PASSWORD = "password";
    private final static String SHARED_PREF_KEY_PASSWORD_AGAIN = "password_again";
    private final static String SHARED_PREF_KEY_DATE = "rodjendan";
    private final static String SHARED_PREF_KEY_UNEXPECTED_SHUTDOWN = "shudown";
    private static boolean prijavaClicked;
    private static boolean registracijaClicked;
    private static boolean izlazClicked;
    private static SecretKey kljuc;
    private EditText inputIme;
    private EditText inputPrezime;
    private EditText inputEmail;
    private TextInputEditText inputLozinka;
    private TextInputEditText inputLozinka2;

    private DatePicker inputRodjendan;
    private CheckBox checkBoxUslovi;

    private Button buttonRegistracija;
    private Button buttonPrijava;
    private ImageButton buttonLozinka1;
    private ImageButton buttonLozinka2;
    private TextInputLayout textInputLayoutLozinka;
    private TextInputLayout textInputLayoutLozinka2;
    private Database db;
    private KeyguardManager km;
    private PowerManager pm;
    private boolean isPasswordVisible = false;
    private boolean isBlankScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registracija);
        initComponents();
    }

    private void initComponents(){
        buttonPrijava = findViewById(R.id.buttonPrijava2);

        inputIme = findViewById(R.id.inputIme);
        inputPrezime = findViewById(R.id.inputPrezime);
        inputEmail = findViewById(R.id.inputEmail);
        inputLozinka = findViewById(R.id.inputLozinka);
        inputLozinka2 = findViewById(R.id.inputLozinka2);
        inputRodjendan = findViewById(R.id.inputRodjendan);
        textInputLayoutLozinka = findViewById(R.id.textInputLayoutLozinka);
        textInputLayoutLozinka2 = findViewById(R.id.textInputLayoutLozinka2);

        checkBoxUslovi = findViewById(R.id.checkBoxUslovi);
        buttonRegistracija = findViewById(R.id.buttonRegistracija);
        buttonLozinka1 = findViewById(R.id.buttonVidiLozinku);
        buttonLozinka2 = findViewById(R.id.buttonVidiLozinku2);

        checkBoxUslovi.setOnClickListener(this);
        buttonPrijava.setOnClickListener(this);
        buttonRegistracija.setOnClickListener(this);
        buttonLozinka1.setOnClickListener(this);
        buttonLozinka2.setOnClickListener(this);

        Context context = getApplicationContext();
        km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        isBlankScreen = false;

        db = new Database(this);
        addSomeUsers();

        inputLozinka.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editable.toString().equals("") &&
                        !editable.toString().matches("(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+])[A-Za-z\\d!@#$%^&*()_+]{8,}")) {
                    textInputLayoutLozinka.setError("Lozinka mora biti duža od 8 karaktera, mora da sadrži makar jedno veliko slovo, broj i specijalni karakter.");
                } else {
                    textInputLayoutLozinka.setError(null);
                }
            }
        });

        inputLozinka2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if(!editable.toString().equals("") &&
                        !editable.toString().matches("(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+])[A-Za-z\\d!@#$%^&*()_+]{8,}")){
                    textInputLayoutLozinka2.setError("Lozinka mora biti duža od 8 karaktera, mora da sadrži makar jedno veliko slovo, broj i specijalni karakter.");
                } else {
                    textInputLayoutLozinka2.setError(null);
                }
            }
        });

        inputLozinka.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus || inputLozinka.getText().length() > 0){
                    textInputLayoutLozinka.setHint("");
                } else {
                    textInputLayoutLozinka.setHint(getString(R.string.inputLozinka));
                }
            }
        });

        inputLozinka2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus || inputLozinka2.getText().length() > 0){
                    textInputLayoutLozinka2.setHint("");
                } else {
                    textInputLayoutLozinka2.setHint(getString(R.string.inputLozinka));
                }
            }
        });
    }

    @SuppressLint("DefaultLocale")
    private String dateToString(){
        return String.format("%4d. %2d. %2d.", inputRodjendan.getYear(),
                inputRodjendan.getMonth() + 1,
                inputRodjendan.getDayOfMonth());
    }

    private void stringToDate(String dateStr) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy. MM. dd.", Locale.getDefault());
        try {
            Date date = format.parse(dateStr);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

            inputRodjendan.updateDate(year, month, dayOfMonth);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        kljuc = Ciphers.getAESKey();
        prijavaClicked = false;
        registracijaClicked = false;
        izlazClicked = false;
        if(checkForUnexpectedShutdown()) {
            loadData();
        }
        resetRestartAfterCrashFlag();
        deleteStorage();
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
    protected void onStop() {
        super.onStop();
        if(prijavaClicked || registracijaClicked || izlazClicked){
            resetRestartAfterCrashFlag();
        } else {
            saveData();
            setRestartAfterCrashFlag();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        kljuc = null;
        db.close();
    }

    public int itemClicked(View v){
        if(((CheckBox)v).isChecked()){
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public void onBackPressed() {
        showExitDialog();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.buttonPrijava2:
                prijavaClicked = true;
                logIn();
                break;
            case R.id.buttonRegistracija:
                registracijaClicked = true;
                register();
                break;
            case R.id.buttonVidiLozinku:
                revealPassword(inputLozinka);
                break;
            case R.id.buttonVidiLozinku2:
                revealPassword(inputLozinka2);
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

    //izlaz iz aplikacije na dijalog
    private void showExitDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Da li ste sigurni da želite da izađete iz aplikacije?")
                .setPositiveButton("Da", (dialogInterface, i) -> {
                    izlazClicked = true;
                    db.close();
                    kljuc = null;
                    deleteStorage();
                    finishAffinity();
                })
                .setNegativeButton("Ne", (dialogInterface, i) -> dialogInterface.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //proveravamo da li je aplikacija izgubila fokus i da li je uredjaj zakljucan ili ekran neaktivan
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if(!hasFocus && (km.inKeyguardRestrictedInputMode() || !pm.isInteractive())){
            isBlankScreen = true;
            saveData();
            deleteFields();
        }
    }

    //dodavanje "probnih" (vec postojecih) korisnika u bazu
    private void addSomeUsers(){
        if(!db.returnEmail("pera.peric.18@singimail.rs") || !db.returnNonce("PQJFZl2p5fo=")){
            db.addUser("Pera", "Peric", "pera.peric.18@singimail.rs", "zuom4lQIywSuwj6J7ceklXOR9KXoMBf/BtRcNTzO1O4=", "1998. 05. 11.", "PQJFZl2p5fo=");
        }
        if(!db.returnEmail("mika.mikic.18@singimail.rs") || !db.returnNonce("Hw8FKE1YFYA=")){
            db.addUser("Mika", "Mikic", "mika.mikic.18@singimail.rs", "0Sh3CtnDekPnAF+w+rb8mNwmBMD+o1tjL+jZx4QIoSk=", "1997. 02. 05.", "Hw8FKE1YFYA=");
        }
    }

    /*
        U metodi registrujSe vršimo više provera pre nego što korisnika ubacujemo u tabelu users. Prvo mora da se složi sa fiktivnim
        uslovima korišćenja, nakon toga je provera da neko polje za unos nije prazno, nakon toga da se ponovno uneta lozinka poklapa sa
        prvom unetom, da li lozinka sadrzi minimum jedno veliko slovo, jedan broj i jedan specijalan znak pri tome da je duga minimum
        8 karaktera, da li je email u ispravnom formatu i na kraju proveravamo da li se korisnik već nalazi u bazi na osnovu unetog
        email-a. Ukoliko su svi uslovi zadovoljeni dodajemo korisnika u bazu, potrebne Stringove za nastavak projekta šaljemo preko
        Bundle-a u PredstaveActivity.
     */
    private void register(){
        if(inputIme.getText().toString().isEmpty() || inputPrezime.getText().toString().isEmpty() || inputEmail.getText().toString().isEmpty()
        || inputLozinka.getText().toString().isEmpty() || inputLozinka2.getText().toString().isEmpty()){
            Toast.makeText(this, "Unesite sva polja u formi.", Toast.LENGTH_LONG).show();
            return;
        }

        if(!inputIme.getText().toString().matches("([A-Z][a-z]+)")){
            Toast.makeText(this, "Ime nije u dobrom formatu.", Toast.LENGTH_LONG).show();
            return;
        }

        if(!inputPrezime.getText().toString().matches("([A-Z][a-z]+(-[A-Z][a-z]+)?)")){
            Toast.makeText(this, "Prezime nije u dobrom formatu.", Toast.LENGTH_LONG).show();
            return;
        }

        if(!inputEmail.getText().toString().matches("([a-zA-Z0-9_\\-\\.]+)@([a-zA-Z0-9_\\-\\.]+)\\.([a-zA-Z]{2,5})")){
            Toast.makeText(this, "Email nije unet u dobrom formatu.", Toast.LENGTH_LONG).show();
            return;
        }

        if(!inputLozinka.getText().toString().matches("(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+])[A-Za-z\\d!@#$%^&*()_+]{8,}")){
            Toast.makeText(this, "Lozinka mora da sadrži makar jedno veliko slovo, broj i specijalni karakter.", Toast.LENGTH_LONG).show();
            return;
        }

        if(!inputLozinka.getText().toString().equals(inputLozinka2.getText().toString())){
            Toast.makeText(this, "Unete lozinke se ne poklapaju.", Toast.LENGTH_LONG).show();
            return;
        }

        if(itemClicked((checkBoxUslovi)) == 0){
            Toast.makeText(this, "Molimo Vas, složite se sa uslovima korišćenja aplikacije.", Toast.LENGTH_LONG).show();
            return;
        }

        if(db.returnEmail(inputEmail.getText().toString())){
            Toast.makeText(this, "Korisnik već postoji u bazi. Prijavite se.", Toast.LENGTH_LONG).show();
            return;
        }

        Ciphers cipher = new Ciphers();
        byte[] nonce = cipher.gen_nonce();

        while(db.returnNonce(Base64.getEncoder().encodeToString(nonce))){
            nonce = cipher.gen_nonce();
        }

        char[] hashPass = cipher.gen_hash(nonce, inputLozinka.getText().toString()).toCharArray();

        db.addUser((inputIme.getText().toString()), (inputPrezime.getText().toString()),
                (inputEmail.getText().toString()), new String(hashPass), dateToString(), Base64.getEncoder().encodeToString(nonce));

        Arrays.fill(hashPass, '\0');
        byte[] sifratEmail = cipher.encryptAES(inputEmail.getText().toString().getBytes(), kljuc);

        Intent intent = new Intent(this, PredstaveActivity.class);

        Bundle extras = new Bundle();
        extras.putString("ime", inputIme.getText().toString());
        extras.putString("prezime", inputPrezime.getText().toString());
        extras.putString("email", Base64.getEncoder().encodeToString(sifratEmail));

        intent.putExtras(extras);
        startActivity(intent);
        deleteStorage();
        deleteFields();
    }

    private void deleteFields(){
        inputIme.setText("");
        inputPrezime.setText("");
        inputLozinka.setText("");
        inputLozinka2.setText("");
        inputEmail.setText("");
        Calendar calendar = Calendar.getInstance();
        inputRodjendan.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), null);
    }

    private void logIn(){
        startActivity(new Intent(this, PrijavaActivity.class));
        deleteFields();
        deleteStorage();
    }

    /*
        Implementirano je cuvanje podataka putem SharedPreferences-a čisto da ako nam Android ubije aplikaciju, da se podaci ne unose
        iznova. Svaki podatak se sifruje AES/CBC/PKCS7Padding pre pucanja aplikacije i desifruje prilikom povratka u aplikaciju
     */
    private void loadData(){
        Ciphers c = new Ciphers();
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_CODE, Context.MODE_PRIVATE);

        if(sharedPreferences.getString(SHARED_PREF_KEY_IME, "").length() > 0){
            byte[] sifratIme = Base64.getDecoder().decode(sharedPreferences.getString(SHARED_PREF_KEY_IME, ""));
            inputIme.setText(new String(c.decryptAES(sifratIme, kljuc), StandardCharsets.UTF_8));
        }

        if(sharedPreferences.getString(SHARED_PREF_KEY_PREZIME, "").length() > 0){
            byte[] sifratPrezime = Base64.getDecoder().decode(sharedPreferences.getString(SHARED_PREF_KEY_PREZIME, ""));
            inputPrezime.setText(new String(c.decryptAES(sifratPrezime, kljuc), StandardCharsets.UTF_8));
        }

        if(sharedPreferences.getString(SHARED_PREF_KEY_EMAIL, "").length() > 0){
            byte[] sifratEmail =  Base64.getDecoder().decode(sharedPreferences.getString(SHARED_PREF_KEY_EMAIL, ""));
            inputEmail.setText(new String(c.decryptAES(sifratEmail, kljuc), StandardCharsets.UTF_8));
        }

        if(sharedPreferences.getString(SHARED_PREF_KEY_PASSWORD, "").length() > 0){
            byte[] sifratLozinka =  Base64.getDecoder().decode(sharedPreferences.getString(SHARED_PREF_KEY_PASSWORD, ""));
            inputLozinka.setText(new String(c.decryptAES(sifratLozinka, kljuc), StandardCharsets.UTF_8));
            Arrays.fill(sifratLozinka, (byte) 0);
            sifratLozinka = null;
        }

        if(sharedPreferences.getString(SHARED_PREF_KEY_PASSWORD_AGAIN, "").length() > 0){
            byte[] sifratLozinkaPonovo =  Base64.getDecoder().decode(sharedPreferences.getString(SHARED_PREF_KEY_PASSWORD_AGAIN, ""));
            inputLozinka2.setText(new String(c.decryptAES(sifratLozinkaPonovo, kljuc), StandardCharsets.UTF_8));
            Arrays.fill(sifratLozinkaPonovo, (byte) 0);
            sifratLozinkaPonovo = null;
        }

        if(sharedPreferences.getString(SHARED_PREF_KEY_DATE, "").length() > 0){
            byte[] sifratRodjendan = Base64.getDecoder().decode(sharedPreferences.getString(SHARED_PREF_KEY_DATE, ""));
            stringToDate(new String(c.decryptAES(sifratRodjendan, kljuc), StandardCharsets.UTF_8));
        }
    }

    private void saveData(){
        Ciphers c = new Ciphers();
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_CODE, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if(inputIme.getText().toString().length() > 0){
            byte[] sifratIme = c.encryptAES((inputIme.getText().toString()).getBytes(StandardCharsets.UTF_8), kljuc);
            editor.putString(SHARED_PREF_KEY_IME, Base64.getEncoder().encodeToString(sifratIme));
        }

        if(inputPrezime.getText().toString().length() > 0) {
            byte[] sifratPrezime = c.encryptAES((inputPrezime.getText().toString()).getBytes(StandardCharsets.UTF_8), kljuc);
            editor.putString(SHARED_PREF_KEY_PREZIME, Base64.getEncoder().encodeToString(sifratPrezime));
        }

        if(inputEmail.getText().toString().length() > 0){
            byte[] sifratEmail = c.encryptAES((inputEmail.getText().toString()).getBytes(StandardCharsets.UTF_8), kljuc);
            editor.putString(SHARED_PREF_KEY_EMAIL, Base64.getEncoder().encodeToString(sifratEmail));
        }

        if(inputLozinka.getText().toString().length() > 0){
            byte[] sifratPassword = c.encryptAES((inputLozinka.getText().toString()).getBytes(StandardCharsets.UTF_8), kljuc);
            editor.putString(SHARED_PREF_KEY_PASSWORD, Base64.getEncoder().encodeToString(sifratPassword));
        }

        if(!inputLozinka2.getText().toString().trim().equals("")){
            byte[] sifratPasswordAgain = c.encryptAES((inputLozinka2.getText().toString()).getBytes(StandardCharsets.UTF_8), kljuc);
            editor.putString(SHARED_PREF_KEY_PASSWORD_AGAIN, Base64.getEncoder().encodeToString(sifratPasswordAgain));
        }

        Calendar currentDate = Calendar.getInstance();
        if(!(inputRodjendan.getYear() == currentDate.get(Calendar.YEAR)
                && inputRodjendan.getMonth() == currentDate.get(Calendar.MONTH)
                && inputRodjendan.getDayOfMonth() == currentDate.get(Calendar.DAY_OF_MONTH))){
            byte[] sifratRodjendan = c.encryptAES(dateToString().getBytes(StandardCharsets.UTF_8), kljuc);
            editor.putString(SHARED_PREF_KEY_DATE, Base64.getEncoder().encodeToString(sifratRodjendan));
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