package com.croconaut.ratemebuddy.activities.notifications;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;

import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.activities.SettingsActivity;
import com.croconaut.ratemebuddy.activities.TimelineActivity;
import com.croconaut.ratemebuddy.data.pojo.Comment;
import com.croconaut.ratemebuddy.data.pojo.Status;
import com.croconaut.ratemebuddy.data.pojo.VoteUp;

import java.util.ArrayList;
import java.util.List;

public class OldStatusNotif extends Notification {
    public static final String EXTRA_CLEAR_OLD_NOTIF = "EXTRA_CLEAR_OLD_NOTIF";
    public static final int OLD_COMMENT_NOTIF_ID = 2;

    public OldStatusNotif(Context context) {
        super(context);

        if (notificationDisabled()) return;

        List<Status> statusesWithUnseen = new ArrayList<>();
        for (Status status : appData.getStatusDataSource().getAllMyStatuses()) {
            for(Comment comment: status.getComments()){
                if(!comment.isSeen()) statusesWithUnseen.add(status);
            }

            for(VoteUp voteUp: status.getVotes()){
                if(!voteUp.isSeen()) statusesWithUnseen.add(status);
            }
        }


        // create a target intent
        Intent intent = new Intent(context, TimelineActivity.class);
        intent.putExtra(EXTRA_CLEAR_OLD_NOTIF, true);

        // stack builder for intent
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(intent);

        // final pending intent
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(OLD_COMMENT_NOTIF_ID, PendingIntent.FLAG_CANCEL_CURRENT);
        // builder fot notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_notif_comments).setContentIntent(resultPendingIntent);
        mBuilder.setContentTitle(context.getResources().getString(R.string.notif_old_comment_title));
        mBuilder.setColor(ContextCompat.getColor(context, R.color.material_grey));
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_notif_comments));
        mBuilder.setAutoCancel(true);

        // if vibs are enabled, set vibs
        if (prefs.getBoolean(SettingsActivity.VIB_PREF, true)) {
            mBuilder.setVibrate(new long[]{0, 500, 200, 500});
        }
        // if sound is enabled, set sound
        if (prefs.getBoolean(SettingsActivity.SOUND_PREF, true)) {
            mBuilder.setSound(Uri.parse(prefs.getString(SettingsActivity.SOUND_RING_TONE_PREF, Settings.System.DEFAULT_NOTIFICATION_URI.toString())));
        }
        // finnaly, create and display new notification
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(OLD_COMMENT_NOTIF_ID, mBuilder.build());

    }
}
