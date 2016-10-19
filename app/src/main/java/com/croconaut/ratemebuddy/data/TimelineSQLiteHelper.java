package com.croconaut.ratemebuddy.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TimelineSQLiteHelper extends SQLiteOpenHelper {
    public static final String TIMELINE_TABLE = "timeline";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_CROCO_ID = "croco_id";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_FILE_URI = "file_uri";
    public static final String COLUMN_MESS_TYPE = "mess_type";
    public static final String COLUMN_SEND_TYPE = "send_type";
    public static final String COLUMN_STATUS_ID = "status_id";
    public static final String COLUMN_SEEN = "timeline_seen";


    private static final String CREATE_PROFILES_TABLE = "CREATE TABLE IF NOT EXISTS "
            + TIMELINE_TABLE
            + "("
            + COLUMN_ID
            + " integer primary key autoincrement, "
            + COLUMN_TIME
            + " text not null, "
            + COLUMN_CROCO_ID
            + " text not null, "
            + COLUMN_CONTENT
            + " text not null, "
            + COLUMN_NAME
            + " text not null, "
            + COLUMN_FILE_URI
            + " text, "
            + COLUMN_STATUS_ID
            + " text, "
            + COLUMN_SEEN
            + " integer not null, "
            + COLUMN_MESS_TYPE
            + " integer not null, "
            + COLUMN_SEND_TYPE
            + " integer not null );";

    private static final String DATABASE_NAME = "timeline.db";
    private static final int DATABASE_VERSION = 1;

    public TimelineSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PROFILES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TIMELINE_TABLE);
                onCreate(db);
    }
}
