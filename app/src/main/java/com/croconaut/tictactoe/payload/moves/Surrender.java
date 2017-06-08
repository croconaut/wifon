package com.croconaut.tictactoe.payload.moves;

import android.support.annotation.NonNull;
import android.support.annotation.Size;

import com.croconaut.tictactoe.payload.TicTacToeGame;

import static com.croconaut.tictactoe.utils.Assertions.assertEquals;
import static com.croconaut.tictactoe.utils.Assertions.assertNotNull;
import static com.croconaut.tictactoe.utils.GameIdGenerator.GAME_ID_LENGTH;


public final class Surrender implements TicTacToeGame {

    @NonNull
    private final String mGameId;

    public Surrender(@NonNull @Size(GAME_ID_LENGTH) final String gameId){
        assertNotNull(gameId, "gameId"); //$NON-NLS-1$
        assertEquals(gameId.length(), GAME_ID_LENGTH, "gameIdLength"); //$NON-NLS-1$

        this.mGameId = gameId;
    }

    @NonNull
    @Override
    public String getGameId() {
        return mGameId;
    }
}
