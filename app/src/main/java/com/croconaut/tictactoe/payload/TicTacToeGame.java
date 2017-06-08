package com.croconaut.tictactoe.payload;


import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Size;

import com.croconaut.tictactoe.utils.GameIdGenerator;

import java.io.Serializable;

/**
 * Marker interface that is used to identity a concrete TicTacToe game based on the
 * {@link TicTacToeGame#getGameId()}.
 */
public interface TicTacToeGame extends Serializable {

    /**
     * @return The unique game identification.
     */
    @NonNull
    @CheckResult
    @Size(GameIdGenerator.GAME_ID_LENGTH)
    String getGameId();

}
