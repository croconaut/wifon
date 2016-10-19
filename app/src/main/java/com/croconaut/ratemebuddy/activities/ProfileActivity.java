package com.croconaut.ratemebuddy.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.ui.adapters.ChoosePhotoAdapter;
import com.croconaut.ratemebuddy.ui.views.transformation.CircleTransform;
import com.croconaut.ratemebuddy.utils.ProfileUtils;
import com.croconaut.ratemebuddy.utils.pojo.TimelineInfo;
import com.croconaut.ratemebuddy.utils.pojo.profiles.MyProfile;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public abstract class ProfileActivity extends WifonActivity implements CptProcessor {
    private static final String TAG = ProfileActivity.class.getName();

    protected static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

    protected static final int NAME_CHECK_MAX_LENGTH = 22;
    protected static final int CHECK_MIN_LENGTH = 1;

    protected Toast toast;
    protected EditText etName, etStatus;
    protected TextView nrOfLikes, nrOfComments, tvStatus;
    protected ImageView ivCover;
    protected ImageView ivPhoto;
    protected Uri currentUri;
    protected Uri currentUriBeforeCrop;

    protected abstract void collectInfoAndCreateProfile();

    protected boolean setCollectClickable = true;

    // show the toast
    protected void toast(String msg) {
        // cancel the previous toast if its displayed at the time
        if (toast != null)
            toast.cancel();
        toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        toast.show();
    }

    // we really need to remove this value, i mean, really :D
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.menuCheck);
        item.setVisible(setCollectClickable);
        return true;
    }

    @Override
    public boolean process(Intent cptIntent) throws IOException, ClassNotFoundException {
        return false;
    }

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    /**
     * Create a file Uri for saving an image or video
     */
    protected Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * Create a File for saving an image or video
     */
    protected File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), getPackageName());
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(getPackageName(), "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    // creates dialog for choosing a new photo
    protected void createImageDialog(final Activity activity) {
        final String[] items = {
                mRes.getString(R.string.profile_dialog_photo_opt_one),
                mRes.getString(R.string.profile_dialog_photo_opt_two)
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder
                .setTitle(mRes.getString(R.string.profile_dialog_photo_title))
                .setNegativeButton(getString(R.string.cancel), null);

        ArrayAdapter<CharSequence> photoAdapter = new ChoosePhotoAdapter(
                ProfileActivity.this,
                items
        );
        builder.setAdapter(photoAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        currentUriBeforeCrop = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, currentUriBeforeCrop);
                        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                        break;
                    case 1:
                        Crop.pickImage(activity);
                        break;
                }
            }
        });

        Dialog dialog = builder.create();
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuCheck:
                collectInfoAndCreateProfile();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void initPhoto() {
        ColorGenerator generator = ColorGenerator.MATERIAL;
        int color = generator.getColor(commonUtils.getMyName(mContext) == null
                ? "W"
                : commonUtils.getMyName(mContext));
        TextDrawable drawable = TextDrawable.builder().buildRound(
                commonUtils.getMyName(mContext) == null
                        ? "W"
                        : commonUtils.getMyName(mContext).substring(0, 1), color);
        ivPhoto.setImageDrawable(drawable);
    }

    protected void beginCrop(Uri source, final Activity activity) {
        ivPhoto.setImageDrawable(null);
        Uri outputUri = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, outputUri).asSquare().start(activity);
    }

    protected void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            currentUri = Crop.getOutput(result);
        } else {
            MyProfile myProfile = MyProfile.getInstance(getApplicationContext());
            if (myProfile != null) {
                currentUri = myProfile.getThumbUri();
            }
        }

        ColorGenerator generator = ColorGenerator.MATERIAL;
        int color = generator.getColor(commonUtils.getMyName(mContext) == null
                ? "W"
                : commonUtils.getMyName(mContext));
        TextDrawable drawable = TextDrawable.builder().buildRound(
                commonUtils.getMyName(mContext) == null
                        ? "W"
                        : commonUtils.getMyName(mContext).substring(0, 1), color);

        Glide.with(this)
                .load(currentUri)
                .asBitmap()
                .error(drawable)
                .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                .thumbnail(0.2f)
                .transform(new CircleTransform(mContext))
                .into(ivPhoto);
    }

    protected long createTimelineMessage(String messageContent) {
        MyProfile myProfile = MyProfile.getInstance(mContext);
        long id = System.currentTimeMillis();

        boolean isImage = messageContent.
                equals(getString(R.string.message_timeline_photo_changed));

        TimelineInfo timelineInfo = new TimelineInfo.Builder(id,
                messageContent,
                myProfile.getIdent(),
                myProfile.getName(),
                TimelineInfo.OUTGOING,
                isImage ? TimelineInfo.MESSAGE_TYPE_PROFILE_CHANGED_PHOTO : TimelineInfo.MESSAGE_TYPE_PROFILE_CHANGED_TEXT,
                false)
                .build();
        appData.getTimelineDataSource().insertTimelineInfo(timelineInfo);

        return id;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) {
            beginCrop(result.getData(), this);
        } else if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, result);
        } else if (resultCode == RESULT_OK && requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            beginCrop(currentUriBeforeCrop, this);
        }
    }

    //TODO: exif utils not respected
    protected Bitmap getBitmap(ImageView imageView, Uri uri, Context cxt) {
        Bitmap bitmap;
        if (imageView.getDrawable() instanceof BitmapDrawable) {
            bitmap = ProfileUtils.getBitmapFromUri(cxt, uri);
        } else {
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
        }
        boolean nullCheck = bitmap == null;
        Log.e(TAG, "Get bitmap returning null ? " + nullCheck);
        return bitmap;
    }

    protected int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }
}
