package com.croconaut.tictactoe.payload.invites;


import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Size;

import static com.croconaut.tictactoe.utils.Assertions.assertEquals;
import static com.croconaut.tictactoe.utils.Assertions.assertNotNull;
import static com.croconaut.tictactoe.utils.GameIdGenerator.GAME_ID_LENGTH;

public final class InviteResponse extends Invite {

    @NonNull
    private final String mGameId;

    private final boolean mGameAccepted;

    public InviteResponse(@NonNull @Size(GAME_ID_LENGTH) final String gameId,
                          final boolean gameAccepted){
        assertNotNull(gameId, "gameId"); //$NON-NLS-1$
        assertEquals(gameId.length(), GAME_ID_LENGTH, "gameId"); //$NON-NLS-1$

        this.mGameId = gameId;
        this.mGameAccepted = gameAccepted;
    }

    @NonNull
    @Override
    public String getGameId() {
        return mGameId;
    }

    @CheckResult
    public boolean isGameAccepted() {
        return mGameAccepted;
    }
}
