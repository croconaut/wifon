package com.croconaut.ratemebuddy.activities.notifications;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.activities.PeopleFragment;
import com.croconaut.ratemebuddy.activities.PeopleParentActivityBB;
import com.croconaut.ratemebuddy.activities.SettingsActivity;
import com.croconaut.ratemebuddy.utils.pojo.profiles.Profile;

public class NearbyNotification extends Notification {

    private static final String TAG = NearbyNotification.class.getName();
    private static final String PREFS_NEXT_NOTIF = "PREFS_NEXT_NOTIF";
    private static final int NEXT_NOTIF_DELAY = 1000 * 60 * 25; //25 mins.

    public static final int NEARBY_NOTIF_ID = 3;

    public NearbyNotification(Context context, Profile profile) {
        super(context);

        if (notificationDisabled()) return;

        boolean nearBynotifEnabled = prefs.getBoolean(SettingsActivity.NEARBY_NOTIF_PREF, true);
        if (!nearBynotifEnabled) return;

        Log.d(TAG, "Nearby notif for profile " + profile.getName());

        // create a target intent
        Intent intent = new Intent(context, PeopleParentActivityBB.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(PeopleParentActivityBB.EXTRA_SCROLL_TO_PAGE,
                PeopleFragment.DisplayType.NEARBY.ordinal());

        // stack builder for intent
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(PeopleParentActivityBB.class);
        stackBuilder.addNextIntent(intent);

        // final pending intent
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(NEARBY_NOTIF_ID, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_notif_nearby_one).setContentIntent(resultPendingIntent).setAutoCancel(true);

        mBuilder.setContentTitle(res.getString(R.string.notif_first_time_nearby_title, profile.getName()));

        mBuilder.setContentText(res.getString(R.string.notif_first_time_nearby_text));

        mBuilder.setLargeIcon(createBitmapIcon(context, profile));

        if (prefs.getBoolean(SettingsActivity.VIB_PREF, true)) {
            mBuilder.setVibrate(new long[]{0, 500, 200, 500});
        }

        if (prefs.getBoolean(SettingsActivity.SOUND_PREF, true)) {
            mBuilder.setSound(Uri.parse(prefs.getString(SettingsActivity.SOUND_RING_TONE_PREF, Settings.System.DEFAULT_NOTIFICATION_URI.toString())));
        }

        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NEARBY_NOTIF_ID, mBuilder.build());


        prefs.edit().putLong(PREFS_NEXT_NOTIF, System.currentTimeMillis() + NEXT_NOTIF_DELAY).apply();
    }

    public static boolean showNearbyNotif(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return System.currentTimeMillis() > prefs.getLong(PREFS_NEXT_NOTIF, 0);
    }

}
