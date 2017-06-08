package com.croconaut.tictactoe.ui.notifications;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.croconaut.ratemebuddy.AppData;
import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.activities.PeopleParentActivityBB;
import com.croconaut.ratemebuddy.utils.pojo.profiles.Profile;
import com.croconaut.tictactoe.payload.TicTacToeGame;
import com.croconaut.tictactoe.payload.games.Game;
import com.croconaut.tictactoe.payload.moves.Move;
import com.croconaut.tictactoe.ui.activities.GameActivity;
import com.croconaut.tictactoe.ui.activities.MenuActivity;

import static com.croconaut.tictactoe.utils.Assertions.assertNotNull;

public final class GameNotificationManager extends BaseNotificationManager {

    public static void createNewMoveNotification(@NonNull final AppData appData,
                                                 @NonNull final TicTacToeGame ticTacToeGame) {
        assertNotNull(appData, "appData");
        assertNotNull(ticTacToeGame, "move");

        final Game game = appData.getGameRepository().getGameByGameId(ticTacToeGame.getGameId());
        assertNotNull(game, "game");

        final Profile profile = appData.getProfileDataSource().getProfileByCrocoId(game.getRemoteProfileId());

        final TaskStackBuilder stackBuilder = TaskStackBuilder.create(appData);
        stackBuilder.addParentStack(PeopleParentActivityBB.class);

        final String contentTitle;
        final String contentText;
        final Intent intent;

        if (ticTacToeGame instanceof Move) {
            contentTitle = appData.getString(R.string.tictactoe_move_notif_title);
            contentText = appData.getString(
                    R.string.tictactoe_move_notif_text, profile.getName());
            intent = GameActivity.newStartIntent(appData, game);
            stackBuilder.addParentStack(MenuActivity.class);
        } else {
            contentTitle = appData.getString(R.string.tictactoe_surrender_notif_title);
            contentText = appData.getString(
                    R.string.tictactoe_surrender_notif_text, profile.getName());
            intent = MenuActivity.newStartIntent(appData);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        stackBuilder.addNextIntent(intent);

        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(appData);

        mBuilder.setLargeIcon(createBitmapIcon(appData, profile));
        mBuilder.setSmallIcon(R.drawable.ic_tictactoe_game_action);
        mBuilder.setContentTitle(contentTitle);

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
    private GameNotificationManager() {
        throw new UnsupportedOperationException("This class is non-instantiable"); //$NON-NLS-1$
    }
}
