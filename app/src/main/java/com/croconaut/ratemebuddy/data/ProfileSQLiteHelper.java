package com.croconaut.ratemebuddy.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ProfileSQLiteHelper extends SQLiteOpenHelper {

    public static final String PROFILES_TABLE = "profiles";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_CROCO_ID = "croco_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_PHOTO_URI = "photo_thumb";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_UNREAD_MESS = "unread_mess";
    public static final String COLUMN_UNREAD_COUNT = "unread_count";
    public static final String COLUMN_ACTUAL_STATE = "actual";
    public static final String COLUMN_FRIENDS = "friends";
    public static final String COLUMN_SHOW_UPLOAD_DIALOG = "show_upload_dialog";
    public static final String COLUMN_PROFILE_ID = "column_profile_id";
    public static final String COLUMN_ENABLE_SHOW_IN_FRIENDS = "enable_show_in_friends";

    private static final String CREATE_PROFILES_TABLE = "CREATE TABLE IF NOT EXISTS "
            + PROFILES_TABLE
            + "("
            + COLUMN_ID
            + " integer primary key autoincrement, "
            + COLUMN_CROCO_ID
            + " text not null, "
            + COLUMN_NAME
            + " text not null, "
            + COLUMN_PROFILE_ID
            + " text, "
            + COLUMN_TYPE
            + " integer not null, "
            + COLUMN_TIMESTAMP
            + " integer, "
            + COLUMN_UNREAD_COUNT
            + " integer, "
            + COLUMN_SHOW_UPLOAD_DIALOG
            + " integer, "
            + COLUMN_ENABLE_SHOW_IN_FRIENDS
            + " integer, "
            + COLUMN_ACTUAL_STATE
            + " BLOB, "
            + COLUMN_UNREAD_MESS
            + " BLOB, "
            + COLUMN_STATUS
            + " BLOB, "
            + COLUMN_FRIENDS
            + " BLOB, "
            + COLUMN_PHOTO_URI
            + " text );";

    private static final String DATABASE_NAME = "profiles.db";
    private static final int DATABASE_VERSION = 1;

    public ProfileSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PROFILES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PROFILES_TABLE);
        onCreate(db);
    }
}
