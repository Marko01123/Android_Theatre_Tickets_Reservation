package com.example.pozoriste;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/*
    Klasa koja služi za pravljenje četiri tabele users, predstave, projekcije i rezervacije, zatim povezivanje njihovih ključeva.
    Sve metode koje su vezane za proveru podataka iz baze su pisane u ovoj klasi.
*/
public class Database extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "pozoriste_database"; //ime fajla baze

    private static final String CREATE_USER_TABLE = "CREATE TABLE IF NOT EXISTS " + UsersModel.TABLE_NAME + " (" +
            UsersModel._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            UsersModel.COLUMN_NAME + " TEXT," +
            UsersModel.COLUMN_SURNAME + " TEXT," +
            UsersModel.COLUMN_EMAIL + " TEXT UNIQUE," +
            UsersModel.COLUMN_PASSWORD + " TEXT," +
            UsersModel.COLUMN_BIRTH + " TEXT);";

    private static final String CREATE_SHOW_TABLE = "CREATE TABLE IF NOT EXISTS " + PredstavaModel.TABLE_NAME + " (" +
            PredstavaModel._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            PredstavaModel.COLUMN_TITLE + " TEXT UNIQUE," +
            PredstavaModel.COLUMN_DIRECTOR + " TEXT," +
            PredstavaModel.COLUMN_ACTORS + " TEXT);";

    private static final String CREATE_PROJECTION_TABLE = "CREATE TABLE IF NOT EXISTS " + ProjekcijaModel.TABLE_NAME + " (" +
            ProjekcijaModel._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            ProjekcijaModel.COLUMN_DATE + " TEXT UNIQUE," +
            ProjekcijaModel.COLUMN_TIME + " TEXT," +
            ProjekcijaModel.PREDSTAVA_ID + " INTEGER," +
            "FOREIGN KEY ("+ProjekcijaModel.PREDSTAVA_ID+") REFERENCES " + PredstavaModel.TABLE_NAME + "("+PredstavaModel._ID+"));";

    private static final String CREATE_RESERVATION_TABLE = "CREATE TABLE IF NOT EXISTS " + RezervacijaModel.TABLE_NAME + " (" +
            RezervacijaModel._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            RezervacijaModel.COLUMN_SEAT + " TEXT," +
            RezervacijaModel.USER_ID + " INTEGER," +
            RezervacijaModel.PROJEKCIJA_ID + " INTEGER," +
            "FOREIGN KEY ("+RezervacijaModel.USER_ID+") REFERENCES " + UsersModel.TABLE_NAME + "("+UsersModel._ID+")," +
            "FOREIGN KEY ("+RezervacijaModel.PROJEKCIJA_ID+") REFERENCES " + ProjekcijaModel.TABLE_NAME + "("+ProjekcijaModel._ID+"));";

    private static final String DELETE_USER_TABLE = "DROP TABLE IF EXISTS " + UsersModel.TABLE_NAME;
    private static final String DELETE_SHOW_TABLE = "DROP TABLE IF EXISTS " + PredstavaModel.TABLE_NAME;
    private static final String DELETE_PROJECTION_TABLE = "DROP TABLE IF EXISTS " + ProjekcijaModel.TABLE_NAME;
    private static final String DELETE_RESERVATION_TABLE = "DROP TABLE IF EXISTS " +RezervacijaModel.TABLE_NAME;

    public Database(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
        db.execSQL("PRAGMA foreign_keys=ON");

    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_USER_TABLE);
        sqLiteDatabase.execSQL(CREATE_SHOW_TABLE);
        sqLiteDatabase.execSQL(CREATE_PROJECTION_TABLE);
        sqLiteDatabase.execSQL(CREATE_RESERVATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL(DELETE_USER_TABLE);
        sqLiteDatabase.execSQL(DELETE_SHOW_TABLE);
        sqLiteDatabase.execSQL(DELETE_PROJECTION_TABLE);
        sqLiteDatabase.execSQL(DELETE_RESERVATION_TABLE);
        onCreate(sqLiteDatabase);
    }

    public void addUser(String ime, String prezime, String email, String lozinka, String rodjendan){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(UsersModel.COLUMN_NAME, ime);
        cv.put(UsersModel.COLUMN_SURNAME, prezime);
        cv.put(UsersModel.COLUMN_EMAIL, email);
        cv.put(UsersModel.COLUMN_PASSWORD, lozinka);
        cv.put(UsersModel.COLUMN_BIRTH, rodjendan);

        db.insert(UsersModel.TABLE_NAME, null, cv);
    }

    public void addShow(String naslov, String rezija, String glumci){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(PredstavaModel.COLUMN_TITLE, naslov);
        cv.put(PredstavaModel.COLUMN_DIRECTOR, rezija);
        cv.put(PredstavaModel.COLUMN_ACTORS, glumci);

        db.insert(PredstavaModel.TABLE_NAME, null, cv);
    }

    public void addProjection(String datum, String vreme, int show_id){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(ProjekcijaModel.COLUMN_DATE, datum);
        cv.put(ProjekcijaModel.COLUMN_TIME, vreme);
        cv.put(ProjekcijaModel.PREDSTAVA_ID, show_id);

        db.insert(ProjekcijaModel.TABLE_NAME,null, cv);
    }

    public void addReservation(String sediste, int user_id, int projekcija_id){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(RezervacijaModel.COLUMN_SEAT, sediste);
        cv.put(RezervacijaModel.USER_ID, user_id);
        cv.put(RezervacijaModel.PROJEKCIJA_ID, projekcija_id);

        db.insert(RezervacijaModel.TABLE_NAME, null, cv);
    }

    public UsersModel returnUserByEmail(String email, String password){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor result = db.rawQuery("SELECT * FROM "+UsersModel.TABLE_NAME+" WHERE "+UsersModel.COLUMN_EMAIL+" = '"+email+"' AND "+UsersModel.COLUMN_PASSWORD+" = '"+password+"';", null);

        if(result.moveToFirst()){
            @SuppressLint("Range") String ime = result.getString(result.getColumnIndex(UsersModel.COLUMN_NAME));
            @SuppressLint("Range") String prezime = result.getString(result.getColumnIndex(UsersModel.COLUMN_SURNAME));
            @SuppressLint("Range") String email2 = result.getString(result.getColumnIndex(UsersModel.COLUMN_EMAIL));
            @SuppressLint("Range") String lozinka = result.getString(result.getColumnIndex(UsersModel.COLUMN_PASSWORD));
            @SuppressLint("Range") String rodjendan = result.getString(result.getColumnIndex(UsersModel.COLUMN_BIRTH));

            return new UsersModel(ime, prezime, email2, lozinka, rodjendan);
        } else {
            return new UsersModel("", "", "", "", "");
        }
    }

    public int returnShowIDBasedOnShowTitle(String title){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor result = db.rawQuery("SELECT "+PredstavaModel._ID+" FROM "+PredstavaModel.TABLE_NAME+" WHERE "+PredstavaModel.COLUMN_TITLE+" = '"+title+"';", null);

        result.moveToFirst();
        return result.getInt(0);
    }

    public boolean returnShowTitle(String title){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor result = db.rawQuery("SELECT * FROM "+PredstavaModel.TABLE_NAME+" WHERE "+PredstavaModel.COLUMN_TITLE+" = '"+title+"';", null);

        if ((result != null) && result.getCount() > 0){
            return true;
        }

        return false;
    }

    public boolean returnDateAndTime(String date, String time){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor result = db.rawQuery("SELECT * FROM "+ProjekcijaModel.TABLE_NAME+" WHERE "+ProjekcijaModel.COLUMN_DATE+" = '"+date+"' AND "+ProjekcijaModel.COLUMN_TIME+" = '"+time+"';", null);

        if((result != null) && result.getCount() > 0){
            return true;
        }

        return false;
    }

    public int returnIdByDateAndTime(String date, String time){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor result = db.rawQuery("SELECT "+ProjekcijaModel._ID+" FROM "+ProjekcijaModel.TABLE_NAME+" WHERE "+ProjekcijaModel.COLUMN_DATE+" = '"+date+"' AND "+ProjekcijaModel.COLUMN_TIME+" = '"+time+"';", null);

        result.moveToFirst();
        return result.getInt(0);
    }

    public int returnIdByEmail(String email){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor result = db.rawQuery("SELECT "+UsersModel._ID+" FROM "+UsersModel.TABLE_NAME+" WHERE "+UsersModel.COLUMN_EMAIL+" = '"+email+"';", null);

        result.moveToFirst();
        return result.getInt(0);
    }

    public boolean returnEmail(String email){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor result = db.rawQuery("SELECT "+UsersModel.COLUMN_EMAIL+" FROM "+UsersModel.TABLE_NAME+" WHERE "+UsersModel.COLUMN_EMAIL+" = '"+email+"';", null);

        if((result != null) && result.getCount() > 0){
            return true;
        }

        return false;
    }

    public boolean returnSeatAndProjection(String seat, int projectionId){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor result = db.rawQuery("SELECT * FROM "+RezervacijaModel.TABLE_NAME+" WHERE "+RezervacijaModel.COLUMN_SEAT+" = '"+seat+"' AND "+RezervacijaModel.PROJEKCIJA_ID+" = "+projectionId+";", null);

        if((result != null) && result.getCount() > 0){
            return true;
        }

        return false;
    }

    public boolean checkFirstTicketReservedOnlyOneAfterCan(int userId, int projectionId){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor result = db.rawQuery("SELECT "+RezervacijaModel._ID+" FROM "+RezervacijaModel.TABLE_NAME+" WHERE "+RezervacijaModel.USER_ID+" = '"+userId+"' AND "+RezervacijaModel.PROJEKCIJA_ID+" = "+projectionId+";", null);

        if((result != null) && result.getCount() == 1){
            return true;
        }

        return false;
    }

    public boolean checkIfTwoTicketsReserved(int userId, int projectionId){ //greska
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor result = db.rawQuery("SELECT "+RezervacijaModel._ID+" FROM "+RezervacijaModel.TABLE_NAME+" WHERE "+RezervacijaModel.USER_ID+" = '"+userId+"' AND "+RezervacijaModel.PROJEKCIJA_ID+" = "+projectionId+";", null);

        if((result != null) && result.getCount() == 2){
            return true;
        }

        return false;
    }

    public UsersModel returnUserById(int user_id){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor result = db.rawQuery("SELECT * FROM "+UsersModel.TABLE_NAME+" WHERE "+UsersModel._ID+" = "+user_id, null);

        if(result.moveToFirst()){
            @SuppressLint("Range") String ime = result.getString(result.getColumnIndex(UsersModel.COLUMN_NAME));
            @SuppressLint("Range") String prezime = result.getString(result.getColumnIndex(UsersModel.COLUMN_SURNAME));
            @SuppressLint("Range") String email2 = result.getString(result.getColumnIndex(UsersModel.COLUMN_EMAIL));
            @SuppressLint("Range") String lozinka = result.getString(result.getColumnIndex(UsersModel.COLUMN_PASSWORD));
            @SuppressLint("Range") String rodjendan = result.getString(result.getColumnIndex(UsersModel.COLUMN_BIRTH));

            return new UsersModel(ime, prezime, email2, lozinka, rodjendan);
        } else {
            return new UsersModel("", "", "", "", "");
        }
    }

    @SuppressLint("Range")
    public List<String> returnReservationDetails(String email){
        SQLiteDatabase db = this.getReadableDatabase();

        List<String> lista = new ArrayList<>();

        Cursor result = db.rawQuery("SELECT rezervacije._id, GROUP_CONCAT(rezervacije.sediste) AS sedista,  projekcije.datum AS datum," +
                " projekcije.vreme AS vreme, predstave.naslov AS naslov FROM rezervacije" +
                " INNER JOIN projekcije ON rezervacije.projekcija_id = projekcije._id" +
                " INNER JOIN predstave ON projekcije.predstava_id = predstave._id" +
                " INNER JOIN users ON rezervacije.users_id = users._id" +
                " WHERE users.email = '"+email+"'"+
                " GROUP BY projekcije._id", null);

        for(int i = 0; i<result.getCount(); i++) {
            String rezervacija = "";
            result.moveToPosition(i);
            String sedista = result.getString(result.getColumnIndex("sedista"));
            String datum = result.getString(result.getColumnIndex("datum"));
            String vreme = result.getString(result.getColumnIndex("vreme"));
            String naslov = result.getString(result.getColumnIndex("naslov"));

            rezervacija = "Naslov: "+naslov+"\n" +
                    "Datum: "+datum+"\n" +
                    "Vreme: "+vreme+"\n" +
                    "Rezervisana sedista: "+sedista;

            lista.add(rezervacija);
        }

        return lista;
    }
}
