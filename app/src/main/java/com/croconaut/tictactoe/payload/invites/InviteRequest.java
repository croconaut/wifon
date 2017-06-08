package com.croconaut.tictactoe.payload.invites;


import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import com.croconaut.tictactoe.model.board.GameSeed;
import com.croconaut.tictactoe.payload.games.Game;
import com.croconaut.tictactoe.payload.games.GameSize;

import static com.croconaut.tictactoe.utils.Assertions.assertNotNull;

public final class InviteRequest extends Invite {

    @NonNull
    private Game mGame;

    public InviteRequest(@NonNull final Game game) {
        assertNotNull(game, "game"); //$NON-NLS-1$

        this.mGame = game;
    }

    public long getGameTimestamp() {
        return mGame.getGameTimestamp();
    }

    @NonNull
    @CheckResult
    @Override
    public String getGameId() {
        return mGame.getGameId();
    }

    @GameSeed
    public int getSeed() {
        return mGame.getGameSeed();
    }

    @GameSize
    public int getGameSize() {
        return mGame.getGameSize();
    }

    @NonNull
    public Game getGame() {
        return mGame;
    }
}
