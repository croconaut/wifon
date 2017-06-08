package com.croconaut.tictactoe.ui.notifications;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.croconaut.ratemebuddy.AppData;
import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.activities.PeopleParentActivityBB;
import com.croconaut.ratemebuddy.utils.pojo.profiles.Profile;
import com.croconaut.tictactoe.communication.receiver.InviteBroadcastReceiver;
import com.croconaut.tictactoe.payload.games.Game;
import com.croconaut.tictactoe.payload.invites.InviteRequest;
import com.croconaut.tictactoe.payload.invites.InviteResponse;
import com.croconaut.tictactoe.ui.activities.GameActivity;
import com.croconaut.tictactoe.ui.activities.MenuActivity;
import com.croconaut.tictactoe.utils.SeedUtils;

import static com.croconaut.tictactoe.communication.receiver.InviteBroadcastReceiver.EXTRA_INVITE_REMOTE_PROFILE_ID;
import static com.croconaut.tictactoe.communication.receiver.InviteBroadcastReceiver.EXTRA_INVITE_REQUEST;
import static com.croconaut.tictactoe.utils.Assertions.assertNotNull;

public final class InviteNotificationManager extends BaseNotificationManager {

    private static final int PENDING_INTENT_BROADCAST_REQUEST_CODE = 12345;

    public static void createInviteRequestNotification(
            @NonNull final Context context, @NonNull final Profile profile,
            @NonNull final InviteRequest inviteRequest, final boolean gameReplaced) {
        assertNotNull(context, "context");
        assertNotNull(inviteRequest, "inviteRequest");

        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);

        mBuilder.setLargeIcon(createBitmapIcon(context, profile));
        mBuilder.setSmallIcon(R.drawable.ic_titactoe_game_asset);
        mBuilder.setContentTitle(profile.getName());

        @StringRes int messageResId = gameReplaced
                ? R.string.tictactoe_invite_notif_request_content_text_invite_replace
                : R.string.tictactoe_invite_notif_request_content_text_invite_new;

        final String contentText = context.getString(
                messageResId, profile.getName(),
                inviteRequest.getGameSize(), inviteRequest.getGameSize(),
                SeedUtils.getOppositeSeed(inviteRequest.getSeed()));
        mBuilder.setContentText(contentText);
        mBuilder.setAutoCancel(false);
        mBuilder.setOngoing(true);

        final Intent clickActionIntent = new Intent();
        clickActionIntent.setAction(InviteBroadcastReceiver.ACTION_INVITE_CLICKED);
        clickActionIntent.putExtra(EXTRA_INVITE_REQUEST, inviteRequest);
        clickActionIntent.putExtra(EXTRA_INVITE_REMOTE_PROFILE_ID, profile.getCrocoId());

        final PendingIntent resultPendingIntent =
                PendingIntent.getBroadcast(context, PENDING_INTENT_BROADCAST_REQUEST_CODE,
                        clickActionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        final Intent acceptActionIntent = new Intent();
        acceptActionIntent.setAction(InviteBroadcastReceiver.ACTION_INVITE_ACCEPTED);
        acceptActionIntent.putExtra(EXTRA_INVITE_REQUEST, inviteRequest);
        acceptActionIntent.putExtra(EXTRA_INVITE_REMOTE_PROFILE_ID, profile.getCrocoId());

        final PendingIntent pendingIntentAccept
                = PendingIntent.getBroadcast(context, PENDING_INTENT_BROADCAST_REQUEST_CODE,
                acceptActionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.addAction(R.drawable.ic_titactactoe_btn_accept,
                context.getString(R.string.tictactoe_invite_notif_request_btn_accept_text),
                pendingIntentAccept);

        final Intent declineActionIntent = new Intent();
        declineActionIntent.setAction(InviteBroadcastReceiver.ACTION_INVITE_DECLINED);
        declineActionIntent.putExtra(EXTRA_INVITE_REQUEST, inviteRequest);
        declineActionIntent.putExtra(EXTRA_INVITE_REMOTE_PROFILE_ID, profile.getCrocoId());

        final PendingIntent pendingIntentDecline
                = PendingIntent.getBroadcast(context, PENDING_INTENT_BROADCAST_REQUEST_CODE,
                declineActionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.addAction(R.drawable.ic_tictactoe_btn_decline,
                context.getString(R.string.tictactoe_invite_notif_request_btn_decline_text),
                pendingIntentDecline);

        final NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(profile.getName());
        bigTextStyle.bigText(contentText);
        mBuilder.setStyle(bigTextStyle);

        final NotificationManager mNotificationManager
                = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(profile.getCrocoId().hashCode(), mBuilder.build());
    }

    public static void createInviteResponseNotification(
            @NonNull final AppData appData, @NonNull final Profile profile,
            @NonNull final InviteResponse inviteResponse) {
        assertNotNull(appData, "appData");
        assertNotNull(inviteResponse, "inviteResponse");

        final String gameId = inviteResponse.getGameId();
        final Game game = appData.getGameRepository().getGameByGameId(gameId);

        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(appData);

        final String contentTitle = appData.getString(inviteResponse.isGameAccepted()
                ? R.string.tictactoe_invite_notif_response_accept_title
                : R.string.tictactoe_invite_notif_response_decline_title);

        final String contentText = appData.getString(inviteResponse.isGameAccepted()
                ? R.string.tictactoe_invite_notif_response_accept_text
                : R.string.tictactoe_invite_notif_response_decline_text, profile.getName());

        mBuilder.setLargeIcon(createBitmapIcon(appData, profile));
        mBuilder.setSmallIcon(R.drawable.ic_titactoe_game_asset);
        mBuilder.setContentTitle(contentTitle);

        final Intent intent = inviteResponse.isGameAccepted()
                ? GameActivity.newStartIntent(appData, game)
                : MenuActivity.newStartIntent(appData);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final TaskStackBuilder stackBuilder = TaskStackBuilder.create(appData);

        if (inviteResponse.isGameAccepted()) {
            stackBuilder.addParentStack(MenuActivity.class);
        }
        stackBuilder.addParentStack(PeopleParentActivityBB.class);
        stackBuilder.addNextIntent(intent);

        final PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(profile.getCrocoId().hashCode(), PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        mBuilder.setContentText(contentText);
        mBuilder.setAutoCancel(true);

        final NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(profile.getName());
        bigTextStyle.bigText(contentText);
        mBuilder.setStyle(bigTextStyle);

        final NotificationManager mNotificationManager
                = (NotificationManager) appData.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(profile.getCrocoId().hashCode(), mBuilder.build());
    }

    /**
     * Private constructor prevents instantiation.
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private InviteNotificationManager() {
        throw new UnsupportedOperationException("This class is non-instantiable"); //$NON-NLS-1$
    }
}
