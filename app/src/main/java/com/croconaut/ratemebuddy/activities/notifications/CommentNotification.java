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
import com.croconaut.ratemebuddy.activities.CommentActivity;
import com.croconaut.ratemebuddy.activities.SettingsActivity;
import com.croconaut.ratemebuddy.data.pojo.Comment;
import com.croconaut.ratemebuddy.data.pojo.Status;
import com.croconaut.ratemebuddy.utils.pojo.profiles.IProfile;
import com.croconaut.ratemebuddy.utils.pojo.profiles.MyProfile;
import com.croconaut.ratemebuddy.utils.pojo.profiles.Profile;

import java.util.ArrayList;

public class CommentNotification extends Notification {

    private static final String TAG = CommentNotification.class.getName();
    public static final int COMMENT_NOTIF_ID = 5;
    public static Status currentStatus = null;

    public CommentNotification(Context context) {
        super(context);

        MyProfile myProfile = MyProfile.getInstance(context);

        if (notificationDisabled()) return;

        String statusContent = myProfile.getStatus().getContent();

        ArrayList<IProfile> commentProfiles = new ArrayList<>();
        ArrayList<Comment> unseenComments = myProfile.getStatus().getUnseenComments();

        for (Comment comment : unseenComments) {
            IProfile profile = profileUtils.findProfile(comment.getCrocoId());
            if (!commentProfiles.contains(profile)) {
                commentProfiles.add(profile);
            }
        }

        //retrieve profiles
        IProfile profile = commentProfiles.get(0);
        if (profile == null) {
            Log.e(TAG, "First profile is null");
            return;
        }

        // get commentProfiles size
        int commentProfilesSize = commentProfiles.size();
        int unseenCommentSize = unseenComments.size();

        // create a target intent
        Intent intent = new Intent(context, CommentActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(CommentActivity.EXTRA_CROCO_ID, myProfile.getProfileId());

        // stack builder for intent
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(CommentActivity.class);
        stackBuilder.addNextIntent(intent);

        // final pending intent
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(COMMENT_NOTIF_ID, PendingIntent.FLAG_CANCEL_CURRENT);
        // builder fot notification
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setContentIntent(resultPendingIntent);

        mBuilder.setContentTitle(commentProfilesSize == 1
                ? commentProfiles.get(0).getName()
                : res.getQuantityString(R.plurals.notif_comment_title, unseenCommentSize, unseenCommentSize, statusContent)
        );
        mBuilder.setNumber(unseenCommentSize);

        mBuilder.setContentText(unseenCommentSize == 1
                ? unseenComments.get(0).getComment()
                : commentProfilesSize == 1
                    ? res.getQuantityString(R.plurals.notif_comment_title, unseenCommentSize, unseenCommentSize, statusContent)
                    : getCorrectContentText(commentProfiles, statusContent)
        );

        int smallIconRes = unseenCommentSize > 1 ? R.drawable.ic_notif_comments : R.drawable.ic_notif_comment;
        mBuilder.setSmallIcon(smallIconRes);

        if (commentProfilesSize == 1) {
            mBuilder.setLargeIcon(createBitmapIcon(context, profile));
        }

        mBuilder.setAutoCancel(true);

        // if vibs are enabled, set vibs
        if (prefs.getBoolean(SettingsActivity.VIB_PREF, true)) {
            mBuilder.setVibrate(new long[]{0, 500, 200, 500});
        }
        // if sound is enabled, set sound
        if (prefs.getBoolean(SettingsActivity.SOUND_PREF, true)) {
            mBuilder.setSound(Uri.parse(prefs.getString(SettingsActivity.SOUND_RING_TONE_PREF, Settings.System.DEFAULT_NOTIFICATION_URI.toString())));
        }

        //inbox style comment
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(
                res.getQuantityString(R.plurals.notif_comment_title, unseenCommentSize, unseenCommentSize, statusContent)
        );

        for (Comment comment : unseenComments) {
            Profile commentProfile = appData.getProfileDataSource().getProfileByCrocoId(comment.getCrocoId());
            String line = Html.fromHtml(
                    "<b><i>" + commentProfile.getName() + "</b></i>: ").toString() + " "
                    + comment.getComment();
            Spannable out = new SpannableString(line);
            StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
            out.setSpan(boldSpan, 0, line.indexOf(":"), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            inboxStyle.addLine(out);
        }

        mBuilder.setAutoCancel(true);

        mBuilder.setStyle(inboxStyle);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(COMMENT_NOTIF_ID, mBuilder.build());


    }

    private String getCorrectContentText(ArrayList<IProfile> commentProfiles, String statusContent){
        String contentText ;
        if(commentProfiles.size() == 2){
            contentText = res.getQuantityString(R.plurals.mesage_comment_big_style,
                    commentProfiles.size(), statusContent,
                    commentProfiles.get(0).getName(), commentProfiles.get(1).getName());
        }else {
            contentText = res.getQuantityString(R.plurals.notif_comment_big_style_text_body,
                    commentProfiles.size(), statusContent,
                    commentProfiles.get(0).getName(), commentProfiles.get(1).getName(), commentProfiles.get(2).getName());
        }

        return contentText;
    }
}
