package com.njdp.njdp_farmer.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by USER-PC on 2016/4/13.
 */
public class SQLiteHandler extends SQLiteOpenHelper {
    private static final String TAG = SQLiteHandler.class.getSimpleName();

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "db_njdp";

    // table name
    private static final String TABLE_USER = "user";
    private static final String TABLE_DRIVER = "driver";

    // Login Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_TELEPHONE = "telephone";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_URL = "imageUrl";

    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_TELEPHONE + " TEXT," + KEY_PASSWORD + " TEXT," + KEY_URL +" TEXT" + ")";
        db.execSQL(CREATE_LOGIN_TABLE);

        Log.d(TAG, "Database tables created !");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);

        // Create tables again
        onCreate(db);
    }

    /**
     * 登录用户的账号信息
     * */
    public void addUser(int fm_id, String name, String telephone, String password, String imageUrl) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, fm_id); //id
        values.put(KEY_NAME, name); // name
        values.put(KEY_PASSWORD, password); // password
        values.put(KEY_TELEPHONE, telephone); // telephone
        values.put(KEY_URL, imageUrl); // imageUrl

        //未查询到记录才插入
        Cursor cursor = db.query(TABLE_USER, null, KEY_ID + "=?", new String[]{String.valueOf(fm_id)}, null, null, null);
        if(cursor == null) {
            // Inserting Row
            long id = db.insert(TABLE_USER, null, values);
            Log.d(TAG, "New user inserted into sqlite: " + id);
        }
        else{
            db.update(TABLE_USER, values, "id=?", new String[]{String.valueOf(fm_id)});
        }

        db.close(); // Closing database connection

    }

    /**
     * 修改登录用户的账号信息
     * */
    public void editUser(int fm_id, String name, String telephone, String password, String imageUrl) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name); // name
        values.put(KEY_PASSWORD, password); // password
        values.put(KEY_TELEPHONE, telephone); // telephone
        values.put(KEY_URL, imageUrl); // imageUrl
        // Updateing Row
        long id = db.update(TABLE_USER, values, "id=?", new String[]{String.valueOf(fm_id)});

        db.close(); // Closing database connection

        Log.d(TAG, "User " + name + " updated into sqlite: " + id);
    }

    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_USER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            user.put("telephone", cursor.getString(1));
            user.put("password", cursor.getString(2));
            user.put("name", cursor.getString(3));
            user.put("imageUrl", cursor.getString(4));
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
