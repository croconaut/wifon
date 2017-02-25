package com.croconaut.tictactoe.storage.database;


import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.croconaut.tictactoe.storage.database.columns.Game;
import com.croconaut.tictactoe.storage.database.columns.Invite;
import com.croconaut.tictactoe.storage.database.columns.Move;

import static com.croconaut.tictactoe.storage.utils.Defaults.DEFAULT_CURSOR_FACTORY;

public final class SQLiteDbHelper extends SQLiteOpenHelper {

    private static final String TAG = SQLiteDbHelper.class.getName();

    @NonNull
    private static final String DATABASE_NAME = "tictactoe_app.db"; //$NON-NLS-1$

    private static final int DATABASE_VERSION = 1;

    public SQLiteDbHelper(@NonNull final Context context) {
        super(
                context,
                DATABASE_NAME,
                DEFAULT_CURSOR_FACTORY,
                DATABASE_VERSION
        );
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            String gameTableSql = "CREATE TABLE " + Game.GAME_TABLE_NAME + "("
                    + Game.COLUMN_ID + " INTEGER PRIMARY KEY,"
                    + Game.COLUMN_GAME_ID + " TEXT NOT NULL UNIQUE,"
                    + Game.COLUMN_GAME_REMOTE_PLAYER_ID + " TEXT NOT NULL,"
                    + Game.COLUMN_GAME_SEED + " INTEGER NOT NULL,"
                    + Game.COLUMN_GAME_TIMESTAMP + " INTEGER NOT NULL,"
                    + Game.COLUMN_GAME_SIZE + " INTEGER NOT NULL,"
                    + Game.COLUMN_GAME_STATE + " INTEGER NOT NULL"
                    + ")";
            db.execSQL(gameTableSql);
        } catch (SQLiteException e) {
            Log.e(TAG, "Error on create table '" + Game.GAME_TABLE_NAME
                    + "' in db '" + DATABASE_NAME + "'", e);
        }

        try {
            String moveTableSql = "CREATE TABLE " + Move.MOVE_TABLE_NAME + "("
                    + Move.COLUMN_ID + " INTEGER PRIMARY KEY,"
                    + Move.COLUMN_MOVE_GAME_ID + " TEXT NOT NULL,"
                    + Move.COLUMN_MOVE_POS_X + " INTEGER NOT NULL,"
                    + Move.COLUMN_MOVE_POS_Y + " INTEGER NOT NULL,"
                    + Move.COLUMN_MOVE_SEED + " INTEGER NOT NULL,"
                    + Move.COLUMN_MOVE_GAME_STATE + " INTEGER NOT NULL,"
                    + "FOREIGN KEY (" + Move.COLUMN_MOVE_GAME_ID + ") " +
                    "REFERENCES " + Game.GAME_TABLE_NAME + " (" + Game.COLUMN_GAME_ID + ")"
                    + ")";
            db.execSQL(moveTableSql);
        } catch (SQLiteException e) {
            Log.e(TAG, "Error on create table '" + Move.MOVE_TABLE_NAME
                    + "' in db '" + DATABASE_NAME + "'", e);
        }

        try {
            String inviteTableSql = "CREATE TABLE " + Invite.INVITE_TABLE_NAME + "("
                    + Invite.COLUMN_ID + " INTEGER PRIMARY KEY,"
                    + Invite.COLUMN_INVITE_GAME_REQUEST + " BLOB,"
                    + Invite.COLUMN_INVITE_LOCK_CREATION_TIME + " INTEGER NOT NULL,"
                    + Invite.COLUMN_INVITE_GAME_REMOTE_PLAYER_ID + " TEXT NOT NULL"
                    + ")";
            db.execSQL(inviteTableSql);
        } catch (SQLiteException e) {
            Log.e(TAG, "Error on create table '" + Invite.INVITE_TABLE_NAME
                    + "' in db '" + DATABASE_NAME + "'", e);
        }


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + Game.GAME_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + Move.MOVE_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + Invite.INVITE_TABLE_NAME);
            onCreate(db);
        } catch (SQLException e) {
            Log.e(TAG, "Error on update db '" + DATABASE_NAME + "'", e);
        }
    }
}
