package com.croconaut.tictactoe.storage.database.columns;


public interface Move extends ID {
    String MOVE_TABLE_NAME = "moves";

    String COLUMN_MOVE_GAME_ID = "move_game_id";

    String COLUMN_MOVE_POS_X = "move_position_x";
    String COLUMN_MOVE_POS_Y = "move_position_y";

    String COLUMN_MOVE_SEED = "move_seed";

    String COLUMN_MOVE_GAME_STATE = "move_game_state";

    String[] MOVE_ALL_COLUMNS = {
            COLUMN_ID,
            COLUMN_MOVE_GAME_ID,
            COLUMN_MOVE_POS_X,
            COLUMN_MOVE_POS_Y,
            COLUMN_MOVE_SEED,
            COLUMN_MOVE_GAME_STATE
    };
}
