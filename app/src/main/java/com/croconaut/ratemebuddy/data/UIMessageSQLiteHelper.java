package com.croconaut.ratemebuddy.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UIMessageSQLiteHelper extends SQLiteOpenHelper {

    public static final String MESSAGES_TABLE = "messages";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_CROCO_ID = "croco_id";
    public static final String COLUMN_SEND_TYPE = "send_type";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_HOPS = "hops";
    public static final String COLUMN_ATTACHMENT_ID = "attachment_id";
    public static final String COLUMN_ATTACHMENT = "attachment";


    public static final String COLUMN_CREATION_TIME = "creationTime";
    public static final String COLUMN_SENT_TO_OTHER_DEVICE_TIME = "sentToOtherDeviceTime";
    public static final String COLUMN_SENT_TO_INTERNET_TIME = "sentToInternetTime";
    public static final String COLUMN_SENT_TO_RECIPIENT_TIME = "sentToRecipientTime";
    public static final String COLUMN_FIRST_SENT_TIME = "firstSentTime";
    public static final String COLUMN_LAST_SENT_TIME = "lastSentTime";
    public static final String COLUMN_SEEN_TIME = "seenTime";
    public static final String COLUMN_RECEIVED_TIME = "receivedTime";


    private static final String CREATE_MESSAGES_TABLE = "CREATE TABLE IF NOT EXISTS "
            + MESSAGES_TABLE
            + "("
            + COLUMN_ID
            + " integer primary key autoincrement, "
            + COLUMN_CROCO_ID
            + " text not null, "
            + COLUMN_CREATION_TIME
            + " integer not null, "
            + COLUMN_SENT_TO_OTHER_DEVICE_TIME
            + " integer, "
            + COLUMN_SENT_TO_INTERNET_TIME
            + " integer, "
            + COLUMN_SENT_TO_RECIPIENT_TIME
            + " integer, "
            + COLUMN_FIRST_SENT_TIME
            + " integer, "
            + COLUMN_LAST_SENT_TIME
            + " integer, "
            + COLUMN_SEEN_TIME
            + " integer, "
            + COLUMN_RECEIVED_TIME
            + " integer, "
            + COLUMN_SEND_TYPE
            + " integer not null, "
            + COLUMN_CONTENT
            + " text not null, "
            + COLUMN_HOPS
            + " text, "
            + COLUMN_ATTACHMENT_ID
            + " integer, "
            + COLUMN_ATTACHMENT
            + " BLOB );";

    private static final String DATABASE_NAME = "ui_messages.db";
    private static final int DATABASE_VERSION = 1;

    public UIMessageSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_MESSAGES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MESSAGES_TABLE);
        onCreate(db);
    }

}
