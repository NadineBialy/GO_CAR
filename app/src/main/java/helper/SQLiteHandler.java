package helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;

public class SQLiteHandler extends SQLiteOpenHelper {

    private static final String TAG = SQLiteHandler.class.getSimpleName();

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "android_api";

    // Login table name
    private static final String TABLE_USER = "user";
    private static final String TABLE_CARS = "cars";


    // Login Table Columns names

    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_AGE = "age";
    private static final String KEY_PHONENUMBER = "PhoneNumber";
    private static final String KEY_NATIONALITY = "Nationality";
    private static final String KEY_UID = "uid";
    private static final String KEY_CREATED_AT = "created_at";


    private static final String ID="ID";
    private static final String NAME="Name";
    private static final String LATITUDE="Latitude";
    private static final String LONGITUDE="Longitude";
    private static final String IMAGE_PATH="Image_Path";
    private static final String FUEL_LEVEL="Fuel Level";
    private static final String PROD_YEAR="Prod_Year";




    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_EMAIL + " TEXT UNIQUE," + KEY_AGE + "TEXT"+ KEY_PHONENUMBER + "TEXT"+ KEY_NATIONALITY + "TEXT"+KEY_UID + " TEXT,"
                + KEY_CREATED_AT + " TEXT" + ")";
        db.execSQL(CREATE_LOGIN_TABLE);


        String CREATE_CARS_TABLE = "CREATE TABLE " + TABLE_CARS + "("
                + ID + " INTEGER PRIMARY KEY," + NAME + " TEXT,"
                + LATITUDE + " TEXT ," + LONGITUDE + "TEXT"+ IMAGE_PATH + "TEXT"+ FUEL_LEVEL + "TEXT"+PROD_YEAR + "TEXT" + ")";
        db.execSQL(CREATE_CARS_TABLE);


        Log.d(TAG, "Database tables created");


    }



    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CARS);

        // Create tables again
        onCreate(db);
    }

    /**
     * Storing user details in database
     * */
    public void addUser(String name, String email, String age , String PhoneNumber , String Nationality, String uid, String created_at) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name); // Name
        values.put(KEY_EMAIL, email); // Email
        values.put(KEY_AGE, age);
        values.put(KEY_PHONENUMBER, PhoneNumber);
        values.put(KEY_NATIONALITY, Nationality);
        values.put(KEY_UID, uid); // Email
        values.put(KEY_CREATED_AT, created_at); // Created At

        // Inserting Row
        long id = db.insert(TABLE_USER, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New user inserted into sqlite: " + id);
    }

    /**
     * Getting user data from database
     * */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_USER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {

            user.put("name", cursor.getString(1));
            user.put("email", cursor.getString(2));
            user.put("age", cursor.getString(3));
            user.put("PhoneNumber", cursor.getString(4));
            user.put("nationality", cursor.getString(5));
            user.put("uid", cursor.getString(6));
            user.put("created_at", cursor.getString(7));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching user from Sqlite: " + user.toString());

        return user;
    }
    public HashMap<String, String> getCarsDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_CARS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            user.put("Name", cursor.getString(1));
            user.put("Latitude", cursor.getString(2));
            user.put("Longitude", cursor.getString(3));
            user.put("Image_Path", cursor.getString(4));
            user.put("Fuel Level", cursor.getString(5));
            user.put("Prod_Year", cursor.getString(6));

        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching user from Sqlite: " + user.toString());

        return user;
    }

    /**
     * Re crate database Delete all tables and create them again
     * */
    public void deleteUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_USER, null, null);
        db.close();

        Log.d(TAG, "Deleted all user info from sqlite");
    }

}