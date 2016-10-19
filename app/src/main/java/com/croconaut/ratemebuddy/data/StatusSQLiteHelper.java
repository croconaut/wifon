package com.croconaut.ratemebuddy.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class StatusSQLiteHelper extends SQLiteOpenHelper {

    public static final String STATUS_TABLE = "status";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TIME_STAMP = "timestamp";
    public static final String COLUMN_STATUS_ID = "status_id";
    public static final String COLUMN_CROCO_ID = "croco_id";
    public static final String COLUMN_PROFILE_ID = "profile_id";
    public static final String COLUMN_PROFILE_NAME = "profile_name";
    public static final String COLUMN_STATUS_CONTENT = "status_content";
    public static final String COLUMN_VOTES = "votes";
    public static final String COLUMN_COMMENTS = "comments";

    private static final String CREATE_PROFILES_TABLE = "CREATE TABLE IF NOT EXISTS "
            + STATUS_TABLE
            + "("
            + COLUMN_ID
            + " integer primary key autoincrement, "
            + COLUMN_STATUS_ID
            + " text not null, "
            + COLUMN_PROFILE_ID
            + " text not null, "
            + COLUMN_PROFILE_NAME
            + " text not null, "
            + COLUMN_STATUS_CONTENT
            + " text, "
            + COLUMN_TIME_STAMP
            + " integer not null, "
            + COLUMN_CROCO_ID
            + " text, "
            + COLUMN_COMMENTS
            + " BLOB, "
            + COLUMN_VOTES
            + " BLOB );";

    private static final String DATABASE_NAME = "statuses.db";
    private static final int DATABASE_VERSION = 1;

    public StatusSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PROFILES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + STATUS_TABLE);
        onCreate(db);
    }
}
