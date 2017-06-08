package com.croconaut.tictactoe.storage.utils.models;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.croconaut.tictactoe.payload.games.Game;
import com.croconaut.tictactoe.payload.invites.InviteRequest;

import static com.croconaut.tictactoe.utils.Assertions.assertNotNull;

public final class InviteLockWrapper {

    @Nullable
    private final InviteRequest mInviteRequest;

    @NonNull
    private final String mPlayerId;

    private final long mCreationDate;

    public InviteLockWrapper(@Nullable final InviteRequest inviteRequest,
                             @NonNull final String playerId,
                             final long creationDate) {
        assertNotNull(playerId, "playerId");

        this.mInviteRequest = inviteRequest;
        this.mPlayerId = playerId;
        this.mCreationDate = creationDate;
    }

    @Nullable
    public InviteRequest getmInviteRequest() {
        return mInviteRequest;
    }

    @NonNull
    public String getPlayerId() {
        return mPlayerId;
    }

    public long getCreationDate() {
        return mCreationDate;
    }
}
