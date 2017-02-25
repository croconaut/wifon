package com.croconaut.tictactoe.model.board;


import android.support.annotation.IntDef;

import java.lang.annotation.Retention;

import static com.croconaut.tictactoe.model.board.GameSeed.BLANK;
import static com.croconaut.tictactoe.model.board.GameSeed.CROSS;
import static com.croconaut.tictactoe.model.board.GameSeed.NOUGHT;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * This class represent values of {@link Board} entries.
 * <p>
 * For better understanding, their string representation that mirrors TicTacToe
 * game could be imagined as:
 * {@link #BLANK}:  ' '
 * {@link #CROSS}:  'X'
 * {@link #NOUGHT}: 'O'
 */
@Retention(SOURCE)
@IntDef({BLANK, CROSS, NOUGHT})
public @interface GameSeed {
    int BLANK = 0;
    int CROSS = 1;
    int NOUGHT = 2;
}



