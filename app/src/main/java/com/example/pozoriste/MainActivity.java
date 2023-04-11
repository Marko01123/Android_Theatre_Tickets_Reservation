package com.example.pozoriste;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.os.Bundle;
import android.widget.Toast;
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
    Minimum API level za pokretanje aplikacije je 21.
    Projekat sadrži komentare iznad bitnijih metoda radi lakšeg praćenja koda.

*/
public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private final static String SHARED_PREF_CODE = "SharedPreferencesPozoristeRegistracija";
    private final static String SHARED_PREF_KEY_IME = "ime";
    private final static String SHARED_PREF_KEY_PREZIME = "prezime";
    private final static String SHARED_PREF_KEY_EMAIL = "email";
    private final static String SHARED_PREF_KEY_PASSWORD = "password";
    private final static String SHARED_PREF_KEY_PASSWORD_AGAIN = "password_again";

    private EditText inputIme;
    private EditText inputPrezime;
    private EditText inputEmail;
    private EditText inputLozinka;
    private EditText inputLozinka2;

    private DatePicker inputRodjendan;
    private CheckBox checkBoxUslovi;

    private Button buttonRegistracija;
    private Button buttonPrijava;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        checkBoxUslovi = findViewById(R.id.checkBoxUslovi);
        buttonRegistracija = findViewById(R.id.buttonRegistracija);

        checkBoxUslovi.setOnClickListener(this);
        buttonPrijava.setOnClickListener(this);
        buttonRegistracija.setOnClickListener(this);

        addSomeUsers();
    }

    private String dateToString(){
        return String.format("%4d. %2d. %2d.", inputRodjendan.getYear(),
                inputRodjendan.getMonth() + 1,
                inputRodjendan.getDayOfMonth());
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

    public int itemClicked(View v){
        if(((CheckBox)v).isChecked()){
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.buttonPrijava2:
                prijaviSe();
                break;
            case R.id.buttonRegistracija:
                registrujSe();
                break;
        }
    }

    //dodavanje "probnih" (vec postojecih) korisnika u bazu
    private void addSomeUsers(){
        Database db = new Database(this);

        db.addUser("Pera", "Peric", "pera.peric.18@singimail.rs", "singidunum", "1998. 05. 11.");
        db.addUser("Mika", "Mikic", "mika.mikic.18@singimail.rs", "singidunum", "1997. 02. 05.");
    }

    /*
        U metodi registrujSe vršimo više provera pre nego što korisnika ubacujemo u tabelu users. Prvo mora da se složi sa fiktivnim
        uslovima korišćenja, nakon toga je provera da neko polje za unos nije prazno, nakon toga da se ponovno uneta lozinka poklapa sa
        prvom unetom, da li je email u ispravnom formatu i na kraju proveravamo da li se korisnik već nalazi u bazi na osnovu unetog
        email-a. Ukoliko su svi uslovi zadovoljeni dodajemo korisnika u bazu, potrebne Stringove za nastavak projekta šaljemo preko
        Bundle-a u PredstaveActivity.
     */
    private void registrujSe(){
        if(inputIme.getText().toString().isEmpty() || inputPrezime.getText().toString().isEmpty() || inputEmail.getText().toString().isEmpty()
        || inputLozinka.getText().toString().isEmpty() || inputLozinka2.getText().toString().isEmpty()){
            Toast.makeText(this, "Unesite sva polja u formi.", Toast.LENGTH_LONG).show();
            return;
        }

        if(!inputEmail.getText().toString().matches("([a-zA-Z0-9_\\-\\.]+)@([a-zA-Z0-9_\\-\\.]+)\\.([a-zA-Z]{2,5})")){
            Toast.makeText(this, "Email nije unet u dobrom formatu.", Toast.LENGTH_LONG).show();
            return;
        }

        if(!inputLozinka.getText().toString().equals(inputLozinka2.getText().toString())){
            Toast.makeText(this, "Unete lozinke se ne poklapaju.", Toast.LENGTH_LONG).show();
            return;
        }

        if(itemClicked((checkBoxUslovi)) == 0){
            Toast.makeText(this, "Molimo Vas, slozite se sa uslovima koriscenja aplikacije.", Toast.LENGTH_LONG).show();
            return;
        }

        Database db = new Database(this);

        if(db.returnEmail(inputEmail.getText().toString())){
            Toast.makeText(this, "Korisnik vec postoji u bazi. Prijavite se.", Toast.LENGTH_LONG).show();
            return;
        }

        db.addUser((inputIme.getText().toString()), (inputPrezime.getText().toString()),
                (inputEmail.getText().toString()), (inputLozinka.getText().toString()), dateToString());

        db.close();

        Intent intent = new Intent(this, PredstaveActivity.class);

        Bundle extras = new Bundle();
        extras.putString("ime", inputIme.getText().toString());
        extras.putString("prezime", inputPrezime.getText().toString());
        extras.putString("email", inputEmail.getText().toString());
        /*extras.putString("lozinka", inputLozinka.getText().toString());
        extras.putString("rodjendan", dateToString());*/

        intent.putExtras(extras);
        startActivity(intent);
    }

    private void prijaviSe(){
        startActivity(new Intent(this, PrijavaActivity.class));
    }

    /*
        Implementirano je cuvanje podataka putem SharedPreferences-a čisto da ako nam Android ubije aplikaciju, da se podaci ne
        unose iznova.
     */
    private void ucitajPodatke(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_CODE, Context.MODE_PRIVATE);
        String ime = sharedPreferences.getString(SHARED_PREF_KEY_IME, "");
        String prezime = sharedPreferences.getString(SHARED_PREF_KEY_PREZIME, "");
        String email = sharedPreferences.getString(SHARED_PREF_KEY_EMAIL, "");
        String lozinka = sharedPreferences.getString(SHARED_PREF_KEY_PASSWORD, "");
        String lozinkaPonovo = sharedPreferences.getString(SHARED_PREF_KEY_PASSWORD_AGAIN, "");

        inputIme.setText(ime);
        inputPrezime.setText(prezime);
        inputEmail.setText(email);
        inputLozinka.setText(lozinka);
        inputLozinka2.setText(lozinkaPonovo);
    }

    private void sacuvajPodatke(){
        String ime = inputIme.getText().toString();
        String prezime = inputPrezime.getText().toString();
        String email = inputEmail.getText().toString();
        String lozinka = inputLozinka.getText().toString();
        String lozinkaPonovo = inputLozinka2.getText().toString();

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_CODE, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(SHARED_PREF_KEY_IME, ime);
        editor.putString(SHARED_PREF_KEY_PREZIME, prezime);
        editor.putString(SHARED_PREF_KEY_EMAIL, email);
        editor.putString(SHARED_PREF_KEY_PASSWORD, lozinka);
        editor.putString(SHARED_PREF_KEY_PASSWORD_AGAIN, lozinkaPonovo);

        editor.commit();
    }
}