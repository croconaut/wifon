package com.croconaut.tictactoe.storage.database.columns;



public interface Game extends ID {
    String GAME_TABLE_NAME = "games";

    String COLUMN_GAME_ID = "game_id";
    String COLUMN_GAME_TIMESTAMP = "game_timestamp";
    String COLUMN_GAME_REMOTE_PLAYER_ID = "game_remote_player_id";
    String COLUMN_GAME_SEED = "game_seed";
    String COLUMN_GAME_SIZE = "game_size";
    String COLUMN_GAME_STATE = "game_state";

    String[] GAME_ALL_COLUMNS = {
            COLUMN_ID,
            COLUMN_GAME_TIMESTAMP,
            COLUMN_GAME_REMOTE_PLAYER_ID,
            COLUMN_GAME_ID,
            COLUMN_GAME_SIZE,
            COLUMN_GAME_STATE,
            COLUMN_GAME_SEED
    };
}
