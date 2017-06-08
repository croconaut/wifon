package com.croconaut.tictactoe.storage.utils;

import android.database.sqlite.SQLiteDatabase;

public interface Defaults {

    String NO_SELECTION = null;

    String[] NO_SELECTION_ARGS = null;

    String NO_SORT_ORDER = null;

    String NO_GROUP_BY = null;

    String NO_HAVING = null;

    SQLiteDatabase.CursorFactory DEFAULT_CURSOR_FACTORY = null;

    String NO_NULL_COLUMN_HACK = null;

    int NO_ROWS_AFFECTED = -1;

}
