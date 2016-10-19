package com.croconaut.ratemebuddy.activities.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.Html;
import android.util.Log;

import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.activities.CommentActivity;
import com.croconaut.ratemebuddy.activities.SettingsActivity;
import com.croconaut.ratemebuddy.data.pojo.VoteUp;
import com.croconaut.ratemebuddy.utils.pojo.profiles.IProfile;
import com.croconaut.ratemebuddy.utils.pojo.profiles.MyProfile;

import java.util.ArrayList;

public class VoteUpNotification extends Notification {
    public static final int VOTE_UP_NOTIF_ID = 1;
    private static final String TAG = VoteUpNotification.class.getName();


    public VoteUpNotification(Context context) {
        super(context);

        MyProfile myProfile = MyProfile.getInstance(context);
        if (notificationDisabled()) return;

        // retrieve vote-up profiles with notification displayed
        ArrayList<IProfile> voteUpProfiles = new ArrayList<>();
        ArrayList<VoteUp> unseenVotes = myProfile.getStatus().getUnseenVotes();


        for (VoteUp voteUp : unseenVotes) {
            IProfile profile = profileUtils.findProfile(voteUp.getCrocoId(), voteUp.getProfileName());
            if (!voteUpProfiles.contains(profile)) {
                voteUpProfiles.add(profile);
            }
        }

        IProfile profile = voteUpProfiles.get(0);
        if (profile == null) {
            Log.e(TAG, "First profile is null!");
            return;
        }

        // get voteUpProfiles size
        int voteUpProfilesSize = voteUpProfiles.size();
        int unseenVotesSize = unseenVotes.size();

        // create a target intent
        Intent intent = new Intent(context, CommentActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(CommentActivity.EXTRA_CROCO_ID, myProfile.getProfileId());

        // stack builder for intent
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(CommentActivity.class);
        stackBuilder.addNextIntent(intent);


        // final pending intent
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(VOTE_UP_NOTIF_ID, PendingIntent.FLAG_CANCEL_CURRENT);
        // builder fot notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_notif_vote_up).setContentIntent(resultPendingIntent).setAutoCancel(false);
        mBuilder.setContentTitle(res.getQuantityString(R.plurals.notif_voteup_title, unseenVotesSize, unseenVotesSize));
        mBuilder.setNumber(unseenVotesSize);

//        String firstArg = voteUpProfilesSize > 1 ? String.valueOf(voteUpProfilesSize) : profile.getName();
//        mBuilder.setContentText(res.getQuantityString(R.plurals.notif_voteup_text, voteUpProfilesSize, firstArg, myProfile.getStatus().getContent()));

        if (voteUpProfilesSize == 1) {
            mBuilder.setLargeIcon(createBitmapIcon(context, profile));
        }

        // big text style, so notificaion will expend in bar if it have space
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(res.getQuantityString(R.plurals.notif_voteup_title, voteUpProfilesSize, voteUpProfilesSize));
        // retrieve first profile
        IProfile firstProfile = voteUpProfiles.get(FIRST_USER);

        if (voteUpProfilesSize <= CRITICAL_COUNT) {
            String status = "<b>" + myProfile.getStatus().getContent() + "</b>";

            String msg = res.getQuantityString(
                    R.plurals.mesage_voteup_big_style,
                    voteUpProfilesSize,
                    firstProfile.getName(),
                    voteUpProfilesSize <= 1 ? status : voteUpProfiles.get(SECOND_USER).getName(),
                    status
            );

            bigTextStyle.bigText(Html.fromHtml(msg));
            mBuilder.setContentText(Html.fromHtml(msg));
        } else {
            IProfile secondProfile = voteUpProfiles.get(SECOND_USER);
            String bigText =  res.getQuantityString(
                    R.plurals.notif_voteup_big_style_text_body,
                    (voteUpProfilesSize - CRITICAL_COUNT),
                    firstProfile.getName(),
                    secondProfile.getName(),
                    (voteUpProfilesSize - CRITICAL_COUNT)
            );

            bigTextStyle.bigText(Html.fromHtml(bigText));
            mBuilder.setContentText(Html.fromHtml(bigText));
        }

        mBuilder.setStyle(bigTextStyle);
        mBuilder.setAutoCancel(true);

        // if vibs are enabled, set vibs
        if (prefs.getBoolean(SettingsActivity.VIB_PREF, true)) {
            mBuilder.setVibrate(new long[]{0, 500, 200, 500});
        }
        // if sound is enabled, set sound
        if (prefs.getBoolean(SettingsActivity.SOUND_PREF, true)) {
            mBuilder.setSound(Uri.parse(prefs.getString(SettingsActivity.SOUND_RING_TONE_PREF, Settings.System.DEFAULT_NOTIFICATION_URI.toString())));
        }

        // finally, create and display new notification
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(VOTE_UP_NOTIF_ID, mBuilder.build());
    }
}