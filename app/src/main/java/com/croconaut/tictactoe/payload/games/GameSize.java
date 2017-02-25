package com.croconaut.tictactoe.payload.games;


import android.support.annotation.IntDef;

import java.lang.annotation.Retention;

import static com.croconaut.tictactoe.payload.games.GameSize.BIG;
import static com.croconaut.tictactoe.payload.games.GameSize.HUGE;
import static com.croconaut.tictactoe.payload.games.GameSize.NORMAL;
import static java.lang.annotation.RetentionPolicy.SOURCE;

@Retention(SOURCE)
@IntDef({NORMAL, BIG, HUGE})
public @interface GameSize {
    int NORMAL = 3;
    int BIG = 4;
    int HUGE = 5;
}
