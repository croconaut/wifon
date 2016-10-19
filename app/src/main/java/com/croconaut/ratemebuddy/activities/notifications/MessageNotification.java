package com.croconaut.ratemebuddy.activities.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;

import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.activities.CommunicationActivity;
import com.croconaut.ratemebuddy.activities.PeopleFragment;
import com.croconaut.ratemebuddy.activities.PeopleParentActivityBB;
import com.croconaut.ratemebuddy.activities.SettingsActivity;
import com.croconaut.ratemebuddy.utils.CommonUtils;
import com.croconaut.ratemebuddy.utils.ProfileUtils;
import com.croconaut.ratemebuddy.utils.pojo.UIMessage;
import com.croconaut.ratemebuddy.utils.pojo.profiles.Profile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MessageNotification extends Notification {
    private static final String TAG = MessageNotification.class.getName();

    public static final int MESSAGE_NOTIF_ID = 4;
    public static Profile currentProfile = null;

    public MessageNotification(Context context, boolean silent) {
        super(context);
        createNotification(context, silent);
    }

    public MessageNotification(Context context) {
        super(context);
        createNotification(context, false);
    }

    private void createNotification(Context context, boolean silent) {
        if (notificationDisabled()) {
            Log.e(TAG, "Notification not enabled!");
            return;
        }

        List<Profile> unreadProfiles = appData.getProfileDataSource().getUnreadProfiles();

        Log.e(TAG, "Number of unread profiles (messages): " + unreadProfiles.size());

        Profile remoteProfile = unreadProfiles.get(0);
        UIMessage message = remoteProfile.getUnreadMessages().get(0);

        int unreadMessagesCount = ProfileUtils.getUnreadMessagesCount(unreadProfiles);
        int unreadUsersCount = unreadProfiles.size();

        Intent resultIntent = (unreadUsersCount > 1) ? new Intent(context, PeopleParentActivityBB.class) : new Intent(context, CommunicationActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        resultIntent.putExtra(CommonUtils.EXTRA_TARGET_CROCO_ID, remoteProfile.getCrocoId());
        resultIntent.putExtra(CommonUtils.EXTRA_CLEAR_NOTIF_MESS_PROFILES, true);
        resultIntent.putExtra(PeopleParentActivityBB.EXTRA_SCROLL_TO_PAGE,
                PeopleFragment.DisplayType.UNREAD.ordinal());

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(CommunicationActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(MESSAGE_NOTIF_ID, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder mBuilder;


        mBuilder = new NotificationCompat.Builder(context).setContentTitle(
                unreadUsersCount > 1
                        ? res.getQuantityString(R.plurals.notif_message_title_plural,
                        unreadUsersCount,
                        unreadUsersCount)
                        : remoteProfile.getName());


        mBuilder.setContentText(unreadMessagesCount == 1
                ? message.getContent()
                : res.getQuantityString(
                R.plurals.notif_message_one_user_many_text,
                unreadMessagesCount,
                unreadMessagesCount)
        );


        mBuilder.setAutoCancel(true).setSmallIcon(
                unreadMessagesCount > 1 ? R.drawable.ic_notif_multiple_message : R.drawable.ic_notif_message
        ).setNumber(unreadMessagesCount).setContentInfo(Integer.toString(unreadMessagesCount));

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle((unreadUsersCount > 1) ? res.getQuantityString(R.plurals.notif_message_title_plural, unreadUsersCount, unreadUsersCount) : remoteProfile.getName());

        if (unreadUsersCount > 1) {
            for (Profile prof : unreadProfiles) {
                String line = Html.fromHtml("<b><i>" + prof.getName() + "</b></i>: ").toString() + " " + prof.getUnreadMessages().get(prof.getUnreadMessages().size() - 1).getContent();
                Spannable out = new SpannableString(line);
                StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
                out.setSpan(boldSpan, 0, line.indexOf(":"), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                inboxStyle.addLine(out);
            }
        } else {
            ArrayList<String> messages = new ArrayList<>();
            for (UIMessage uiMessage : remoteProfile.getUnreadMessages()) {
                messages.add(uiMessage.getContent());
            }
            Collections.reverse(messages);
            for (String mess : messages) {
                inboxStyle.addLine(mess);
            }
        }

        mBuilder.setStyle(inboxStyle);
        mBuilder.setTicker(remoteProfile.getName() + ": " + message.getContent());

        if (unreadUsersCount == 1) {
            mBuilder.setLargeIcon(createBitmapIcon(context, remoteProfile));
        }

        if (prefs.getBoolean(SettingsActivity.VIB_PREF, true) && !silent) {
            mBuilder.setVibrate(new long[]{0, 500, 200, 500});
        }

        if (prefs.getBoolean(SettingsActivity.SOUND_PREF, true) && !silent) {
            mBuilder.setSound(Uri.parse(prefs.getString(SettingsActivity.SOUND_RING_TONE_PREF, Settings.System.DEFAULT_NOTIFICATION_URI.toString())));
        }

        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(MESSAGE_NOTIF_ID, mBuilder.build());


        Log.e(TAG, "Message notification showed!");
    }
}
