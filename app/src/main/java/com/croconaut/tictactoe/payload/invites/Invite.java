package com.croconaut.tictactoe.payload.invites;


import android.support.annotation.NonNull;
import android.support.annotation.Size;

import com.croconaut.tictactoe.payload.TicTacToeGame;

import static com.croconaut.tictactoe.utils.Assertions.assertIsBiggerThan;
import static com.croconaut.tictactoe.utils.Assertions.assertNotNull;

public abstract class Invite implements TicTacToeGame {

    /*
    @NonNull
    private final String mPlayerId;

    public Invite(@NonNull @Size(min = 1) final String playerId) {
        assertNotNull(playerId, "playerId"); //$NON-NLS-1$
        assertIsBiggerThan(playerId.length(), 1, "playerIdSize"); //$NON-NLS-1$

        this.mPlayerId = playerId;
    }

    @NonNull
    @Size(min = 1)
    public String getPlayerId() {
        return mPlayerId;
    }
    */
}
