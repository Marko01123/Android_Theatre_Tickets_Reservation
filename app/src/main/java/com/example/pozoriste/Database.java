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
            UsersModel.COLUMN_BIRTH + " TEXT," +
            UsersModel.COLUMN_NONCE + " TEXT UNIQUE);";

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


    public void addUser(String ime, String prezime, String email, String lozinka, String rodjendan, String nonce){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(UsersModel.COLUMN_NAME, ime);
        cv.put(UsersModel.COLUMN_SURNAME, prezime);
        cv.put(UsersModel.COLUMN_EMAIL, email);
        cv.put(UsersModel.COLUMN_PASSWORD, lozinka);
        cv.put(UsersModel.COLUMN_BIRTH, rodjendan);
        cv.put(UsersModel.COLUMN_NONCE, nonce);

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

    public UsersModel returnUserByEmail(String email){
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + UsersModel.TABLE_NAME + " WHERE " +
                UsersModel.COLUMN_EMAIL + " = ? ";

        String[] selectionArgs = {email};

        Cursor result = db.rawQuery(query, selectionArgs);

        if(result.moveToFirst()){
            @SuppressLint("Range") String ime = result.getString(result.getColumnIndex(UsersModel.COLUMN_NAME));
            @SuppressLint("Range") String prezime = result.getString(result.getColumnIndex(UsersModel.COLUMN_SURNAME));
            @SuppressLint("Range") String email2 = result.getString(result.getColumnIndex(UsersModel.COLUMN_EMAIL));
            @SuppressLint("Range") String lozinka = result.getString(result.getColumnIndex(UsersModel.COLUMN_PASSWORD));
            @SuppressLint("Range") String rodjendan = result.getString(result.getColumnIndex(UsersModel.COLUMN_BIRTH));
            @SuppressLint("Range") String nonce = result.getString(result.getColumnIndex(UsersModel.COLUMN_NONCE));

            result.close();

            return new UsersModel(ime, prezime, email2, lozinka, rodjendan, nonce);
        } else {
            return new UsersModel("", "", "", "", "", "");
        }
    }

    @SuppressLint("Range")
    public int returnShowIDBasedOnShowTitle(String title){
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + PredstavaModel._ID + " FROM " + PredstavaModel.TABLE_NAME +
                " WHERE " + PredstavaModel.COLUMN_TITLE + " = ?";

        String[] selectionArgs = {title};

        Cursor result = db.rawQuery(query, selectionArgs);

        result.moveToFirst();
        int showID = -1;
        if (result.moveToFirst()) {
            showID = result.getInt(result.getColumnIndex(PredstavaModel._ID));
        }
        result.close();
        return showID;
    }

    public boolean returnShowTitle(String title){
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + PredstavaModel.TABLE_NAME +
                " WHERE " + PredstavaModel.COLUMN_TITLE + " = ?";

        String[] selectionArgs = {title};

        Cursor result = db.rawQuery(query, selectionArgs);

        boolean hasTitle = false;

        if (result != null && result.getCount() > 0) {
            hasTitle = true;
        }

        result.close();
        return hasTitle;
    }

    public boolean returnDateAndTime(String date, String time){
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + ProjekcijaModel.TABLE_NAME +
                " WHERE " + ProjekcijaModel.COLUMN_DATE + " = ? AND " +
                ProjekcijaModel.COLUMN_TIME + " = ?";

        String[] selectionArgs = {date, time};

        Cursor result = db.rawQuery(query, selectionArgs);

        boolean exists = false;

        if (result != null && result.getCount() > 0) {
            exists = true;
        }

        result.close();
        return exists;
    }

    public int returnIdByDateAndTime(String date, String time){
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + ProjekcijaModel._ID +
                " FROM " + ProjekcijaModel.TABLE_NAME +
                " WHERE " + ProjekcijaModel.COLUMN_DATE + " = ? AND " +
                ProjekcijaModel.COLUMN_TIME + " = ?";

        String[] selectionArgs = {date, time};

        Cursor result = db.rawQuery(query, selectionArgs);

        int projectionId = -1;

        if (result != null && result.moveToFirst()) {
            projectionId = result.getInt(0);
        }

        result.close();
        return projectionId;
    }

    public int returnIdByEmail(String email){
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + UsersModel._ID +
                " FROM " + UsersModel.TABLE_NAME +
                " WHERE " + UsersModel.COLUMN_EMAIL + " = ?";

        String[] selectionArgs = {email};

        Cursor result = db.rawQuery(query, selectionArgs);

        int userId = -1;

        if (result != null && result.moveToFirst()) {
            userId = result.getInt(0);
        }

        result.close();
        return userId;
    }

    public boolean returnEmail(String email){
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + UsersModel.COLUMN_EMAIL + " FROM " + UsersModel.TABLE_NAME + " WHERE " + UsersModel.COLUMN_EMAIL + " = ?";
        String[] selectionArgs = { email };

        Cursor result = db.rawQuery(query, selectionArgs);

        if(result != null && result.getCount() > 0){
            result.close();
            return true;
        }

        return false;
    }

    public boolean returnNonce(String nonce){
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + UsersModel.COLUMN_NONCE + " FROM " + UsersModel.TABLE_NAME + " WHERE " + UsersModel.COLUMN_NONCE + " = ?";
        String[] selectionArgs = { nonce };

        Cursor result = db.rawQuery(query, selectionArgs);

        if(result != null && result.getCount() > 0){
            result.close();
            return true;
        }

        return false;
    }

    public boolean returnSeatAndProjection(String seat, int projectionId){
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + RezervacijaModel.TABLE_NAME + " WHERE " + RezervacijaModel.COLUMN_SEAT + " = ? AND " + RezervacijaModel.PROJEKCIJA_ID + " = ?";
        String[] selectionArgs = { seat, String.valueOf(projectionId) };

        Cursor result = db.rawQuery(query, selectionArgs);

        if(result != null && result.getCount() > 0){
            result.close();
            return true;
        }

        return false;
    }

    public boolean checkFirstTicketReservedOnlyOneAfterCan(int userId, int projectionId){
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + RezervacijaModel._ID + " FROM " + RezervacijaModel.TABLE_NAME + " WHERE " + RezervacijaModel.USER_ID + " = ? AND " + RezervacijaModel.PROJEKCIJA_ID + " = ?";
        String[] selectionArgs = { String.valueOf(userId), String.valueOf(projectionId) };

        Cursor result = db.rawQuery(query, selectionArgs);

        if(result != null && result.getCount() == 1){
            result.close();
            return true;
        }

        return false;
    }

    public boolean checkIfTwoTicketsReserved(int userId, int projectionId){
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + RezervacijaModel._ID + " FROM " + RezervacijaModel.TABLE_NAME + " WHERE " + RezervacijaModel.USER_ID + " = ? AND " + RezervacijaModel.PROJEKCIJA_ID + " = ?";
        String[] selectionArgs = { String.valueOf(userId), String.valueOf(projectionId) };

        Cursor result = db.rawQuery(query, selectionArgs);

        if(result != null && result.getCount() == 2){
            result.close();
            return true;
        }

        return false;
    }

    public UsersModel returnUserById(int user_id){
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + UsersModel.TABLE_NAME + " WHERE " + UsersModel._ID + " = ?";
        String[] selectionArgs = { String.valueOf(user_id) };

        Cursor result = db.rawQuery(query, selectionArgs);

        if(result.moveToFirst()){
            @SuppressLint("Range") String ime = result.getString(result.getColumnIndex(UsersModel.COLUMN_NAME));
            @SuppressLint("Range") String prezime = result.getString(result.getColumnIndex(UsersModel.COLUMN_SURNAME));
            @SuppressLint("Range") String email2 = result.getString(result.getColumnIndex(UsersModel.COLUMN_EMAIL));
            @SuppressLint("Range") String lozinka = result.getString(result.getColumnIndex(UsersModel.COLUMN_PASSWORD));
            @SuppressLint("Range") String rodjendan = result.getString(result.getColumnIndex(UsersModel.COLUMN_BIRTH));
            @SuppressLint("Range") String nonce = result.getString(result.getColumnIndex(UsersModel.COLUMN_NONCE));

            result.close();

            return new UsersModel(ime, prezime, email2, lozinka, rodjendan, nonce);
        } else {
            return new UsersModel("", "", "", "", "", "");
        }
    }

    @SuppressLint("Range")
    public List<String> returnReservationDetails(String email){
        SQLiteDatabase db = this.getReadableDatabase();

        List<String> lista = new ArrayList<>();

        String query = "SELECT rezervacije._id, GROUP_CONCAT(rezervacije.sediste) AS sedista, " +
                "projekcije.datum AS datum, projekcije.vreme AS vreme, predstave.naslov AS naslov " +
                "FROM rezervacije " +
                "INNER JOIN projekcije ON rezervacije.projekcija_id = projekcije._id " +
                "INNER JOIN predstave ON projekcije.predstava_id = predstave._id " +
                "INNER JOIN users ON rezervacije.users_id = users._id " +
                "WHERE users.email = ? " +
                "GROUP BY projekcije._id";
        String[] selectionArgs = { email };

        Cursor result = db.rawQuery(query, selectionArgs);

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

        result.close();

        return lista;
    }
}
