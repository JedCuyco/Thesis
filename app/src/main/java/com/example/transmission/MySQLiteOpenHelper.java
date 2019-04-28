package com.example.transmission;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Kielle on 7/15/2016.
 */
public class MySQLiteOpenHelper extends SQLiteOpenHelper {

    public static final String TABLE_CHAT_HISTORY = "chat_history";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_MOBILE_NUMBER = "mobile_number"; //mobile number of receiver
    public static final String COLUMN_SENDER_OR_RECEIVER = "sender_or_receiver"; //if message is from sender or receiver, if sender = true, else false
    public static final String COLUMN_MESSAGE = "message";
    public static final String COLUMN_CONTACT_NAME = "contact_name";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    private static final String DATABASE_NAME = "chat_history.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_CHAT_HISTORY + "( "
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_MOBILE_NUMBER + " text not null, "
            + COLUMN_CONTACT_NAME + " text not null, "
            + COLUMN_SENDER_OR_RECEIVER + " bool not null, "
            + COLUMN_MESSAGE + " text not null, "
            + COLUMN_TIMESTAMP + " timestamp default current_timestamp not null);";

    public MySQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteOpenHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT_HISTORY);
        onCreate(db);
    }
}
