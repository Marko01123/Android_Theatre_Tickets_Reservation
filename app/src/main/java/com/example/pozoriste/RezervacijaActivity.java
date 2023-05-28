package com.example.pozoriste;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.crypto.SecretKey;

/*
    RezervacijaActivity ima zadatak da prikaže detalje svakog izabranog termina odnosno projekcije (datum, vreme) i dinamički stvori
    tabelu 5x6 koja simulira pozorišnu dvoranu. Na klik na polje u tabeli, koje je dugme, odabira se sedište u dvorani, boja dugmeta se
    menja u zelenu (ponovni klik je vraća u početno definisanu boju) i nakon što se sedišta odaberu, klikom na dugme Rezervisati nakon
    provere da li ta sedišta već postoje u bazi, se ubacuju u bazu. Jedini uslov je da jedan korisnik može maksimalno da rezerviše dva
    sedišta. Odjednom ili rezervacijom iz delova, u oba slučaja se vrši provera i ne može da rezerviše više od dva sedišta po projekciji.
    Ukoliko korisnik koji je već rezervisao dva sedišta uđe u ovu aktivnost, odabir sedišta kao i dugme za rezervaciju će mu biti
    onemogućeni. Takođe, već rezervisana sedišta ponovnim ulaskom u ovu aktivnost, biće obojena crvenom bojom i neće se dozvoliti klik na
    njih. Ukoliko je uslov rezervacije zadovoljen, sedišta se ubacuju u bazu i korisnik se vraća na "home" odnosno PredstaveActivity gde će
    moći da pristupi PrikazRezervacijaActivity gde se čitanjem iz baze ispisuju sva rezervisana sedišta korisnika ove aplikacije.
*/
public class RezervacijaActivity extends AppCompatActivity implements View.OnClickListener{
    private TextView labelNaslovRezervacija, labelDatum, labelVreme;
    private Button buttonRezervisi;
    private TableLayout tl;
    private boolean click = true;
    private int user_id, projekcija_id;
    private Database db;
    private int brojac = 0;
    private List<String> sedista;
    private boolean[] daLiJeIzabranoSediste;
    private SecretKey kljuc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rezervacija);

        initComponents();
    }

    private void initComponents(){
        labelNaslovRezervacija = findViewById(R.id.labelNaslovRezervacija);
        labelDatum = findViewById(R.id.labelDatum);
        labelVreme = findViewById(R.id.labelVreme);
        tl = findViewById(R.id.tableLayout);
        sedista = new ArrayList<>();
        Ciphers c = new Ciphers();
        db = new Database(this);

        buttonRezervisi = findViewById(R.id.buttonRezervisi);
        buttonRezervisi.setOnClickListener(this);

        Bundle extras = getIntent().getExtras();
        String naslov = extras.getString("naslov");
        String datum = extras.getString("datum");
        String vreme = extras.getString("vreme");

        kljuc = Ciphers.getAESKey();

        String B64SifratProjekcijaId = extras.getString("projekcija_id");
        byte[] decryptedDataProjectionId = c.decryptAES(Base64.getDecoder().decode(B64SifratProjekcijaId), kljuc);
        projekcija_id = Integer.parseInt(new String(decryptedDataProjectionId));

        String B64SifratUserId = extras.getString("user_id");
        byte[] decryptedDataUserId = c.decryptAES(Base64.getDecoder().decode(B64SifratUserId), kljuc);

        ByteBuffer buffer = ByteBuffer.wrap(decryptedDataUserId);
        user_id = buffer.getInt();

        String naslovLabel = "Naslov projekcije: "+naslov;
        String datumLabel = "Datum projekcije: "+datum;
        String vremeLabel = "Vreme projekcije: "+vreme;

        labelNaslovRezervacija.append(naslovLabel);
        labelDatum.append(datumLabel);
        labelVreme.append(vremeLabel);

        makeTable();
        daLiJeIzabranoSediste = new boolean[brojac];

        if(db.checkIfTwoTicketsReserved(user_id, projekcija_id)){
            Toast.makeText(this, "Ne mozete rezervisati vise od dve karte", Toast.LENGTH_LONG).show();
            buttonRezervisi.setEnabled(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        kljuc = null;
        db.close();
    }

    @Override
    public void onClick(View view) {
        if(db.checkIfTwoTicketsReserved(user_id, projekcija_id)){
            for(int i = 0; i<brojac; i++){
                if (view.getId() == i){
                    view.setEnabled(false);
                }
            }
        } else {
            for (int i = 0; i < brojac; i++) {
                if (view.getId() == i) {
                    if (click) {
                        view.setBackgroundColor(getResources().getColor(R.color.buttonNotPressed));
                        click = false;
                        daLiJeIzabranoSediste[i] = false;
                    } else {
                        view.setBackgroundColor(getResources().getColor(R.color.buttonPressed));
                        click = true;
                        daLiJeIzabranoSediste[i] = true;
                    }
                }
            }
        }

        if (view.getId() == R.id.buttonRezervisi) {
            int izabranaSedista = 0;
            for(int i = 0; i<brojac; i++){
                if(daLiJeIzabranoSediste[i]){
                    izabranaSedista++;
                }
            }

            if(izabranaSedista > 2){
                Toast.makeText(this, "Ne moze se rezervisati vise od dva sedista", Toast.LENGTH_LONG).show();
            } else if(db.checkFirstTicketReservedOnlyOneAfterCan(user_id, projekcija_id) && izabranaSedista >= 2) {
                Toast.makeText(this, "Ne moze se rezervisati vise od dva sedista", Toast.LENGTH_LONG).show();
            } else {
                for (int i = 0; i < brojac; i++) {
                    if(daLiJeIzabranoSediste[i]){
                        if(!db.returnSeatAndProjection(sedista.get(i), projekcija_id)){
                            db.addReservation(sedista.get(i), user_id, projekcija_id);
                        }
                    }
                }

                Ciphers c = new Ciphers();
                Intent intent = new Intent(this, PredstaveActivity.class);
                UsersModel user = db.returnUserById(user_id);
                user_id = -1;
                projekcija_id = -1;
                byte[] sifratEmail = c.encryptAES(user.getEmail().getBytes(), kljuc);

                Bundle extras = new Bundle();
                extras.putString("ime", user.getIme());
                extras.putString("prezime", user.getPrezime());
                extras.putString("email", Base64.getEncoder().encodeToString(sifratEmail));
                intent.putExtras(extras);
                Toast.makeText(this, "Rezervacija je bila uspesna.", Toast.LENGTH_LONG).show();
                startActivity(intent);

            }
        }
    }

    private void makeTable(){
        for(int i = 1; i <= 5; i++){
            TableRow tr = new TableRow(this);

            TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            tr.setBackgroundResource(R.drawable.border_table);

            for(int j = 1; j <= 6; j++) {
                Button tv = new Button(this);
                tv.setBackgroundResource(R.drawable.border_table);
                String tekstPolja = i+""+j;
                sedista.add(tekstPolja);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
                params.setMargins(10, 10, 10, 15);
                tv.setText(tekstPolja);
                tv.setTextColor(Color.WHITE);

                if(db.returnSeatAndProjection(tekstPolja, projekcija_id)){
                    tv.setBackgroundColor(getResources().getColor(R.color.seatUnavalable));
                    tv.setEnabled(false);
                } else {
                    tv.setBackgroundColor(getResources().getColor(R.color.buttonNotPressed));
                }

                tv.setGravity(Gravity.CENTER);
                tv.setId(brojac);
                brojac++;
                tv.setLayoutParams(params);
                tr.addView(tv);
                tv.setOnClickListener(this);
            }

            tl.addView(tr);
        }
    }
}