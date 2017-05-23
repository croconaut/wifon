package com.croconaut.tictactoe.communication.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.croconaut.ratemebuddy.AppData;
import com.croconaut.tictactoe.payload.games.Game;
import com.croconaut.tictactoe.payload.games.GameState;
import com.croconaut.tictactoe.payload.invites.InviteRequest;
import com.croconaut.tictactoe.ui.activities.GameActivity;
import com.croconaut.tictactoe.ui.activities.MenuActivity;
import com.croconaut.tictactoe.ui.notifications.InviteNotificationManager;
import com.croconaut.tictactoe.utils.Assertions;


public final class InviteBroadcastReceiver extends BroadcastReceiver {

    @NonNull
    public static final String ACTION_INVITE_CLICKED
            = "com.croconaut.tictactoe.communication.receiver.action.INVITE_CLICKED";

    @NonNull
    public static final String ACTION_INVITE_ACCEPTED
            = "com.croconaut.tictactoe.communication.receiver.action.INVITE_ACCEPTED";

    @NonNull
    public static final String ACTION_INVITE_DECLINED
            = "com.croconaut.tictactoe.communication.receiver.action.INVITE_DECLINED";

    @NonNull
    public static final String EXTRA_INVITE_REQUEST
            = "com.croconaut.tictactoe.communication.receiver.extra.INVITE_REQUEST";

    @NonNull
    public static final String EXTRA_INVITE_REMOTE_PROFILE_ID
            = "com.croconaut.tictactoe.communication.receiver.extra.INVITE_REMOTE_PROFILE_ID";

    @Override
    public void onReceive(@NonNull final Context context, @NonNull final Intent intent) {
        final AppData appData = ((AppData) context.getApplicationContext());
        final InviteRequest inviteRequest
                = ((InviteRequest) intent.getSerializableExtra(EXTRA_INVITE_REQUEST));
        final String remoteProfileId = intent.getStringExtra(EXTRA_INVITE_REMOTE_PROFILE_ID);

        switch (intent.getAction()) {
            case ACTION_INVITE_CLICKED:
                final Game pendingGame
                        = appData.getGameRepository()
                        .getAllGamesWithState(
                                remoteProfileId, GameState.PENDING_WAITING_FOR_INVITE_REQUEST)
                        .get(0);

                context.startActivity(
                        GameActivity.newStartIntent(context, pendingGame)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
            case ACTION_INVITE_ACCEPTED:
                final Game newGame
                        = appData.getGameCommunication().acceptInvite(
                        remoteProfileId, inviteRequest.getGameId(), inviteRequest.getSeed());
                Assertions.assertNotNull(newGame, "newGame");

                context.startActivity(
                        GameActivity.newStartIntent(context, newGame)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
            case ACTION_INVITE_DECLINED:
                appData.getGameCommunication().declineInvite(remoteProfileId, inviteRequest.getGameId());
                context.startActivity(
                        MenuActivity.newStartIntent(context).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
        }

        InviteNotificationManager.cancelNotification(context, remoteProfileId.hashCode());
    }
}
