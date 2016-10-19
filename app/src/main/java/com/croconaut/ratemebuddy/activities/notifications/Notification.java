package com.croconaut.ratemebuddy.activities.notifications;


import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.croconaut.ratemebuddy.AppData;
import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.activities.SettingsActivity;
import com.croconaut.ratemebuddy.utils.CommonUtils;
import com.croconaut.ratemebuddy.utils.ProfileUtils;
import com.croconaut.ratemebuddy.utils.pojo.profiles.IProfile;

public abstract class Notification {
    private static final String TAG = Notification.class.getName();

    protected static final int FIRST_USER = 0;
    protected static final int SECOND_USER = 1;
    protected static final int CRITICAL_COUNT = 2;

    protected SharedPreferences prefs;
    protected AppData appData;
    protected Resources res;
    protected CommonUtils commonUtils;
    protected ProfileUtils profileUtils;

    public Notification(Context context) {
        appData = (AppData) context;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        res = context.getResources();
        commonUtils = new CommonUtils();
        profileUtils = new ProfileUtils(appData);
    }

    protected boolean notificationDisabled() {
        return !prefs.getBoolean(SettingsActivity.NOTIFICATION_PREF, true);
    }

    public static void clearNotification(Context context, int id) {
        Log.e(TAG, "Canceling notification with ID: " + id);
        NotificationManager nMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancel(id);
    }

    // creates/updates notification
    public static void createOrUpdate(int action, AppData appData) {
        Log.e(TAG, "Showing notification with ID: " + action);
        switch (action) {
            case VoteUpNotification.VOTE_UP_NOTIF_ID:
                new VoteUpNotification(appData);
                break;
            case MessageNotification.MESSAGE_NOTIF_ID:
                new MessageNotification(appData);
                break;
            case CommentNotification.COMMENT_NOTIF_ID:
                new CommentNotification(appData);
                break;
            case OldStatusNotif.OLD_COMMENT_NOTIF_ID:
                new OldStatusNotif(appData);
                break;
            default:
                break;
        }
    }

       //TODO: check for performance and memory leak
    protected Bitmap createBitmapIcon(Context context, IProfile remoteProfile) {
        Bitmap photoBitmap;
        if (remoteProfile.getThumbUri() != null) {
            photoBitmap = ProfileUtils.getThumbnail(context, remoteProfile.getThumbUri());
            int size = (int) context.getResources().getDimension(R.dimen.notification_height);
            return Bitmap.createScaledBitmap(photoBitmap, size, size, true);
        } else {
            ColorGenerator generator = ColorGenerator.MATERIAL;
            int color = generator.getColor(remoteProfile.getName());
            TextDrawable textDrawable = TextDrawable.builder().buildRect(remoteProfile.getName().substring(0, 1), color);
            ImageView imageView = new ImageView(context);
            imageView.setImageDrawable(textDrawable);

            Bitmap bitmap;
            try {
                Drawable d = imageView.getDrawable();
                bitmap = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                d.draw(canvas);
            } catch (Exception e) {
                Drawable drawable = imageView.getDrawable();
                int width = drawable.getIntrinsicWidth();
                width = width > 0 ? width : 100;
                int height = drawable.getIntrinsicHeight();
                height = height > 0 ? height : 100;

                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);
            }

            return  bitmap;
        }
    }

}
