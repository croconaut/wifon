package com.croconaut.tictactoe.storage.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.croconaut.ratemebuddy.utils.CommonUtils;
import com.croconaut.tictactoe.model.board.GameSeed;
import com.croconaut.tictactoe.payload.games.Game;
import com.croconaut.tictactoe.payload.games.GameSize;
import com.croconaut.tictactoe.payload.games.GameState;
import com.croconaut.tictactoe.payload.invites.InviteRequest;
import com.croconaut.tictactoe.payload.moves.Move;
import com.croconaut.tictactoe.storage.GameRepository;
import com.croconaut.tictactoe.storage.database.SQLiteDbHelper;
import com.croconaut.tictactoe.storage.utils.models.InviteLockWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.database.sqlite.SQLiteDatabase.CONFLICT_FAIL;
import static android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE;
import static com.croconaut.ratemebuddy.utils.CommonUtils.deserializeObject;
import static com.croconaut.tictactoe.storage.database.columns.Game.COLUMN_GAME_ID;
import static com.croconaut.tictactoe.storage.database.columns.Game.COLUMN_GAME_REMOTE_PLAYER_ID;
import static com.croconaut.tictactoe.storage.database.columns.Game.COLUMN_GAME_SEED;
import static com.croconaut.tictactoe.storage.database.columns.Game.COLUMN_GAME_SIZE;
import static com.croconaut.tictactoe.storage.database.columns.Game.COLUMN_GAME_STATE;
import static com.croconaut.tictactoe.storage.database.columns.Game.COLUMN_GAME_TIMESTAMP;
import static com.croconaut.tictactoe.storage.database.columns.Game.GAME_ALL_COLUMNS;
import static com.croconaut.tictactoe.storage.database.columns.Game.GAME_TABLE_NAME;
import static com.croconaut.tictactoe.storage.database.columns.Invite.COLUMN_INVITE_GAME_REQUEST;
import static com.croconaut.tictactoe.storage.database.columns.Invite.COLUMN_INVITE_GAME_REMOTE_PLAYER_ID;
import static com.croconaut.tictactoe.storage.database.columns.Invite.COLUMN_INVITE_LOCK_CREATION_TIME;
import static com.croconaut.tictactoe.storage.database.columns.Invite.INVITE_ALL_COLUMNS;
import static com.croconaut.tictactoe.storage.database.columns.Invite.INVITE_TABLE_NAME;
import static com.croconaut.tictactoe.storage.database.columns.Move.COLUMN_MOVE_GAME_ID;
import static com.croconaut.tictactoe.storage.database.columns.Move.COLUMN_MOVE_GAME_STATE;
import static com.croconaut.tictactoe.storage.database.columns.Move.COLUMN_MOVE_POS_X;
import static com.croconaut.tictactoe.storage.database.columns.Move.COLUMN_MOVE_POS_Y;
import static com.croconaut.tictactoe.storage.database.columns.Move.COLUMN_MOVE_SEED;
import static com.croconaut.tictactoe.storage.database.columns.Move.MOVE_ALL_COLUMNS;
import static com.croconaut.tictactoe.storage.database.columns.Move.MOVE_TABLE_NAME;
import static com.croconaut.tictactoe.storage.utils.Defaults.NO_GROUP_BY;
import static com.croconaut.tictactoe.storage.utils.Defaults.NO_HAVING;
import static com.croconaut.tictactoe.storage.utils.Defaults.NO_NULL_COLUMN_HACK;
import static com.croconaut.tictactoe.storage.utils.Defaults.NO_ROWS_AFFECTED;
import static com.croconaut.tictactoe.storage.utils.Defaults.NO_SORT_ORDER;
import static com.croconaut.tictactoe.utils.Assertions.assertEquals;
import static com.croconaut.tictactoe.utils.Assertions.assertNotNull;
import static com.croconaut.tictactoe.utils.GameIdGenerator.GAME_ID_LENGTH;


public final class GameRepositoryImpl implements GameRepository {

    private static final String TAG = GameRepositoryImpl.class.getName();

    @NonNull
    private final SQLiteDbHelper mDbHelper;

    public GameRepositoryImpl(@NonNull final SQLiteDbHelper dbHelper) {
        assertNotNull(dbHelper, "dbHelper");  //$NON-NLS-1$

        this.mDbHelper = dbHelper;
    }

    @Override
    @NonNull
    public List<Move> getAllMovesByGameId(@NonNull final String gameId) {
        assertNotNull(gameId, "gameId"); //$NON-NLS-1$
        assertEquals(gameId.length(), GAME_ID_LENGTH, "gameIdLength"); //$NON-NLS-1$

        final SQLiteDatabase database = mDbHelper.getReadableDatabase();
        final List<Move> movesList = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor = database.query(MOVE_TABLE_NAME, MOVE_ALL_COLUMNS, COLUMN_MOVE_GAME_ID + " = ?",
                    new String[]{gameId}, NO_GROUP_BY, NO_HAVING, NO_SORT_ORDER);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    movesList.add(getMove(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return Collections.unmodifiableList(movesList);
    }

    @NonNull
    @Override
    public List<Game> getGamesByPlayerId(@NonNull final String playerId) {
        assertNotNull(playerId, "playerId"); //$NON-NLS-1$

        final SQLiteDatabase database = mDbHelper.getReadableDatabase();
        final List<Game> gameList = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor = database.query(GAME_TABLE_NAME, GAME_ALL_COLUMNS,
                    COLUMN_GAME_REMOTE_PLAYER_ID + " = ?", new String[]{playerId},
                    NO_GROUP_BY, NO_HAVING, NO_SORT_ORDER);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    gameList.add(getGame(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return Collections.unmodifiableList(gameList);
    }

    @Override
    @NonNull
    public List<Game> getAllGamesWithState(@NonNull final String remoteProfileId, @GameState final int gameState) {
        assertNotNull(remoteProfileId, "remoteProfileId");

        final SQLiteDatabase database = mDbHelper.getReadableDatabase();
        final List<Game> gameList = new ArrayList<>();

        Cursor cursor = null;
        try {
            final StringBuilder w = new StringBuilder();
            w.append(COLUMN_GAME_STATE + " =? ");
            w.append(" AND ");
            w.append(COLUMN_GAME_REMOTE_PLAYER_ID + " =? ");
            final String selection = new String(w);

            final String[] selectionArgs = new String[]{String.valueOf(gameState), remoteProfileId};

            cursor = database.query(GAME_TABLE_NAME, GAME_ALL_COLUMNS, selection, selectionArgs,
                    NO_GROUP_BY, NO_HAVING, NO_SORT_ORDER);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    gameList.add(getGame(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return Collections.unmodifiableList(gameList);
    }

    @Override
    @Nullable
    public Game getGameByGameId(@NonNull final String gameId) {
        assertNotNull(gameId, "gameId"); //$NON-NLS-1$
        assertEquals(gameId.length(), GAME_ID_LENGTH, "gameIdLength"); //$NON-NLS-1$

        final SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor = null;
        try {
            cursor = database.query(GAME_TABLE_NAME, GAME_ALL_COLUMNS, COLUMN_GAME_ID + " = ?",
                    new String[]{gameId}, NO_GROUP_BY, NO_HAVING, NO_SORT_ORDER);

            if (cursor != null && cursor.moveToFirst()) {
                return getGame(cursor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return null;
    }

    @Override
    public void insertGame(@NonNull final Game game) {
        assertNotNull(game, "game"); //$NON-NLS-1$

        final SQLiteDatabase database = mDbHelper.getWritableDatabase();

        database.beginTransaction();
        try {

            final ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_GAME_ID, game.getGameId());
            contentValues.put(COLUMN_GAME_TIMESTAMP, game.getGameTimestamp());
            contentValues.put(COLUMN_GAME_REMOTE_PLAYER_ID, game.getRemoteProfileId());
            contentValues.put(COLUMN_GAME_SEED, game.getGameSeed());
            contentValues.put(COLUMN_GAME_SIZE, game.getGameSize());
            contentValues.put(COLUMN_GAME_STATE, game.getGameState());

            final long rowId =
                    database.insertOrThrow(GAME_TABLE_NAME, NO_NULL_COLUMN_HACK, contentValues);

            if (rowId == NO_ROWS_AFFECTED) {
                Log.e(TAG, "Insertion has failed: " + game.getGameId());
            }

            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    @Override
    public void deleteGame(@NonNull final Game game) {
        assertNotNull(game, "game"); //$NON-NLS-1$

        final SQLiteDatabase database = mDbHelper.getWritableDatabase();

        database.beginTransaction();
        try {

            final long rowCount =
                    database.delete(
                            GAME_TABLE_NAME,
                            COLUMN_GAME_ID + " = ?", new String[]{game.getGameId()});

            if (rowCount == NO_ROWS_AFFECTED) {
                Log.e(TAG, "Deletion has failed: " + game.getGameId());
            }

            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    @Override
    public void updateGameState(@NonNull final Game game, @GameState final int newGameState) {
        assertNotNull(game, "game"); //$NON-NLS-1$

        final SQLiteDatabase database = mDbHelper.getWritableDatabase();

        database.beginTransaction();
        try {

            final ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_GAME_STATE, newGameState);

            final long rowCount =
                    database.updateWithOnConflict(
                            GAME_TABLE_NAME, contentValues,
                            COLUMN_GAME_ID + " = ?", new String[]{game.getGameId()},
                            CONFLICT_REPLACE);

            if (rowCount == NO_ROWS_AFFECTED) {
                Log.e(TAG, "Update has failed: " + game.getGameId());
            }

            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    @Override
    public void insertMove(@NonNull final Move move) {
        assertNotNull(move, "move"); //$NON-NLS-1$

        final SQLiteDatabase database = mDbHelper.getWritableDatabase();

        database.beginTransaction();
        try {

            final ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_MOVE_GAME_ID, move.getGameId());
            contentValues.put(COLUMN_MOVE_GAME_STATE, move.getGameState());
            contentValues.put(COLUMN_MOVE_POS_X, move.getXPos());
            contentValues.put(COLUMN_MOVE_POS_Y, move.getYPos());
            contentValues.put(COLUMN_MOVE_SEED, move.getSeed());

            final long rowId =
                    database.insertOrThrow(MOVE_TABLE_NAME, NO_NULL_COLUMN_HACK, contentValues);

            if (rowId == NO_ROWS_AFFECTED) {
                Log.e(TAG, "Invite insertion has failed: " + move.getGameId());
            }

            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    @Override
    public void insertInviteLock(@NonNull final String playerId, final long creationTime) {
        assertNotNull(playerId, "playerId"); //$NON-NLS-1$

        final SQLiteDatabase database = mDbHelper.getWritableDatabase();

        database.beginTransaction();
        try {

            final ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_INVITE_GAME_REMOTE_PLAYER_ID, playerId);
            contentValues.put(COLUMN_INVITE_LOCK_CREATION_TIME, creationTime);

            final long rowId =
                    database.insertOrThrow(INVITE_TABLE_NAME, NO_NULL_COLUMN_HACK, contentValues);

            if (rowId == NO_ROWS_AFFECTED) {
                Log.e(TAG, "Insertion has failed for player: " + playerId);
            }

            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    @Override
    public void deleteInviteLock(final long creationTime) {
        final SQLiteDatabase database = mDbHelper.getWritableDatabase();

        database.beginTransaction();
        try {

            final long rowCount =
                    database.delete(
                            INVITE_TABLE_NAME,
                            COLUMN_INVITE_LOCK_CREATION_TIME + " = ?",
                            new String[]{String.valueOf(creationTime)});

            if (rowCount == NO_ROWS_AFFECTED) {
                Log.e(TAG, "INVITE LOCK - Invite deletion has failed: " + creationTime);
            }

            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    @Override
    public void updateInviteLock(@NonNull final String playerId, @NonNull final InviteRequest inviteRequest) {
        assertNotNull(inviteRequest, "inviteRequest"); //$NON-NLS-1$
        assertNotNull(playerId, "playerId"); //$NON-NLS-1$

        final SQLiteDatabase database = mDbHelper.getWritableDatabase();

        database.beginTransaction();
        try {

            final ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_INVITE_GAME_REQUEST, CommonUtils.serializeObject(inviteRequest));

            final long rowCount =
                    database.updateWithOnConflict(
                            INVITE_TABLE_NAME, contentValues,
                            COLUMN_INVITE_GAME_REMOTE_PLAYER_ID + " = ?", new String[]{String.valueOf(playerId)},
                            CONFLICT_FAIL);

            if (rowCount == NO_ROWS_AFFECTED) {
                Log.e(TAG, "INVITE LOCK - Update has failed: " + playerId);
            }

            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    @Override
    public InviteLockWrapper isInviteLockPresent(@NonNull final String playerId) {
        assertNotNull(playerId, "playerId"); //$NON-NLS-1$

        final SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor = null;
        try {
            cursor = database.query(INVITE_TABLE_NAME, INVITE_ALL_COLUMNS,
                    COLUMN_INVITE_GAME_REMOTE_PLAYER_ID + " = ?", new String[]{playerId},
                    NO_GROUP_BY, NO_HAVING, NO_SORT_ORDER);

            if (cursor != null && cursor.moveToFirst()) {
                return getInvite(cursor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return null;
    }

    @Override
    public InviteLockWrapper isInviteLockPresent(final long creationTime) {
        final SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor = null;
        try {
            cursor = database.query(INVITE_TABLE_NAME, INVITE_ALL_COLUMNS,
                    COLUMN_INVITE_LOCK_CREATION_TIME + " = ?", new String[]{String.valueOf(creationTime)},
                    NO_GROUP_BY, NO_HAVING, NO_SORT_ORDER);

            if (cursor != null && cursor.moveToFirst()) {
                return getInvite(cursor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return null;
    }

    @CheckResult
    private InviteLockWrapper getInvite(@NonNull final Cursor cursor) {
        assertNotNull(cursor, "cursor"); //$NON-NLS-1$

        final InviteRequest inviteRequest = (InviteRequest) deserializeObject(cursor.getBlob(cursor.getColumnIndex(COLUMN_INVITE_GAME_REQUEST)));
        final String remotePlayerId = cursor.getString(cursor.getColumnIndex(COLUMN_INVITE_GAME_REMOTE_PLAYER_ID));
        final long creationDate = cursor.getLong(cursor.getColumnIndex(COLUMN_INVITE_LOCK_CREATION_TIME));

        return new InviteLockWrapper(inviteRequest, remotePlayerId, creationDate);
    }

    @CheckResult
    private Game getGame(@NonNull final Cursor cursor) {
        assertNotNull(cursor, "cursor"); //$NON-NLS-1$

        final String gameId = cursor.getString(cursor.getColumnIndex(COLUMN_GAME_ID));
        final String remoteProfileId = cursor.getString(cursor.getColumnIndex(COLUMN_GAME_REMOTE_PLAYER_ID));
        final long gameTimestamp = cursor.getLong(cursor.getColumnIndex(COLUMN_GAME_TIMESTAMP));
        @GameSize final int size = cursor.getInt(cursor.getColumnIndex(COLUMN_GAME_SIZE));
        @GameState final int state = cursor.getInt(cursor.getColumnIndex(COLUMN_GAME_STATE));
        @GameSeed final int seed = cursor.getInt(cursor.getColumnIndex(COLUMN_GAME_SEED));
        return new Game(gameId, gameTimestamp, remoteProfileId, seed, size, state);
    }

    @CheckResult
    private Move getMove(@NonNull final Cursor cursor) {
        assertNotNull(cursor, "cursor"); //$NON-NLS-1$

        final String gameId = cursor.getString(cursor.getColumnIndex(COLUMN_MOVE_GAME_ID));
        final int posX = cursor.getInt(cursor.getColumnIndex(COLUMN_MOVE_POS_X));
        final int posY = cursor.getInt(cursor.getColumnIndex(COLUMN_MOVE_POS_Y));
        @GameSeed final int seed = cursor.getInt(cursor.getColumnIndex(COLUMN_MOVE_SEED));
        @GameState final int gameState = cursor.getInt(cursor.getColumnIndex(COLUMN_MOVE_GAME_STATE));
        return new Move(gameId, seed, gameState, posX, posY);
    }
}
