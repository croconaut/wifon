package com.croconaut.tictactoe.storage.database.columns;




public interface Invite extends ID {

    String INVITE_TABLE_NAME = "invites";

    String COLUMN_INVITE_GAME_REQUEST = "invite_game_request";
    String COLUMN_INVITE_LOCK_CREATION_TIME = "invite_game_lock_creation_time";
    String COLUMN_INVITE_GAME_REMOTE_PLAYER_ID = "invite_game_remote_player_id";

    String[] INVITE_ALL_COLUMNS = {
            COLUMN_ID,
            COLUMN_INVITE_GAME_REQUEST,
            COLUMN_INVITE_LOCK_CREATION_TIME,
            COLUMN_INVITE_GAME_REMOTE_PLAYER_ID
    };

}
