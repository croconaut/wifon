package com.croconaut.tictactoe.ui.notifications;


import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.utils.ProfileUtils;
import com.croconaut.ratemebuddy.utils.pojo.profiles.IProfile;

import static com.croconaut.tictactoe.utils.Assertions.assertNotNull;

public abstract class BaseNotificationManager {

    public static void cancelNotification(@NonNull final Context context,
                                          final int notifId) {
        assertNotNull(context, "context");

        final NotificationManager nMgr = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancel(notifId);
    }

    /**
     * Copy&paste from RMB :-(
     */
    @NonNull
    @CheckResult
    /*package*/ static Bitmap createBitmapIcon(Context context, IProfile remoteProfile) {
        final Bitmap photoBitmap;
        if (remoteProfile.getThumbUri() != null) {
            photoBitmap = ProfileUtils.getThumbnail(context, remoteProfile.getThumbUri());
            final int size = (int) context.getResources().getDimension(R.dimen.notification_height);
            return Bitmap.createScaledBitmap(photoBitmap, size, size, true);
        } else {
            final ColorGenerator generator = ColorGenerator.MATERIAL;
            final int color = generator.getColor(remoteProfile.getName());
            final TextDrawable textDrawable = TextDrawable.builder().buildRect(remoteProfile.getName().substring(0, 1), color);
            final ImageView imageView = new ImageView(context);
            imageView.setImageDrawable(textDrawable);

            Bitmap bitmap;
            try {
                final Drawable d = imageView.getDrawable();
                bitmap = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                final Canvas canvas = new Canvas(bitmap);
                d.draw(canvas);
            } catch (Exception e) {
                final Drawable drawable = imageView.getDrawable();
                int width = drawable.getIntrinsicWidth();
                width = width > 0 ? width : 100;
                int height = drawable.getIntrinsicHeight();
                height = height > 0 ? height : 100;

                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                final Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);
            }

            return bitmap;
        }
    }
}
