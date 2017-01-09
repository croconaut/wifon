package com.croconaut.ratemebuddy.utils;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bumptech.glide.Glide;
import com.croconaut.cpt.data.Communication;
import com.croconaut.cpt.data.LocalAttachment;
import com.croconaut.cpt.data.OutgoingPayload;
import com.croconaut.cpt.data.OutgoingPersistentBroadcastMessage;
import com.croconaut.ratemebuddy.AppData;
import com.croconaut.ratemebuddy.CptReceiver;
import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.activities.SettingsActivity;
import com.croconaut.ratemebuddy.data.pojo.RMBProfile;
import com.croconaut.ratemebuddy.utils.pojo.profiles.IProfile;
import com.croconaut.ratemebuddy.utils.pojo.profiles.MyProfile;
import com.croconaut.ratemebuddy.utils.pojo.profiles.Profile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ProfileUtils {
    private static final String TAG = ProfileUtils.class.getName();
    private static final int THUMBNAIL_SIZE = 50;

    private AppData appData;

    public ProfileUtils(AppData appData) {
        this.appData = appData;
    }

    public void showImage(Context context, Uri uri) {
        final Dialog builder = new Dialog(context);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //nothing;
            }
        });


        builder.setCancelable(true);
        builder.setCanceledOnTouchOutside(true);

        ImageView imageView = new ImageView(context);

        Glide.with(context).load(uri).asBitmap().into(imageView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder.dismiss();
            }
        });

        int px = (int) CommonUtils.dipToPixels(context, 250);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(px, px));

        builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        imageView.getLayoutParams().height = px;
        imageView.getLayoutParams().width = px;

        builder.show();
    }

    public IProfile findProfile(String ident) {
        return findProfile(ident, appData.getResources().getString(R.string.profile_unknown_name));
    }

    public IProfile findProfileByIdOrCrocoId(String crocoId, String profileId, String profileName) {
        MyProfile myProfile = MyProfile.getInstance(appData);
        if (profileId.equals(myProfile.getProfileId())) {
            return myProfile;
        }

        return findProfile(crocoId, profileName);
    }


    public IProfile findProfile(String ident, String name) {
        // check db
        Profile profileDB = appData.getProfileDataSource().getProfileByCrocoId(ident);
        if (profileDB != null) {
            return profileDB;
        }

        MyProfile myProfile = MyProfile.getInstance(appData);
        if (ident.equals(myProfile.getProfileId())) {
            return myProfile;
        }

        if (ident.equals(CommonUtils.CEO_CROCO_ID)) {
            Profile ceoProfile = new Profile.Builder(CommonUtils.CEO_CROCO_ID)
                    .addName(appData.getResources().getString(R.string.ceo_name))
                    .build();

            return ceoProfile;
        }

        Log.e(TAG, "Profile " + name + " not found, ident: " + ident + "... creating new profile");
        // profile was not found
        Profile profile = new Profile.Builder(ident).addName(name).build();
        profile.setType(Profile.CACHED);
        appData.getProfileDataSource().insertProfile(profile);
        return profile;
    }

    public static MyProfile readMyProfile(Context cxt) {
        File defaultDir = new File(cxt.getFilesDir(), CommonUtils.PROFILE_DIRECTORY_MYSELF);

        if (!defaultDir.exists()) {
            Log.e(TAG, "Directory " + defaultDir + " dont exist, returning!");
            return null;
        }

        File file = new File(defaultDir, CommonUtils.FILE_NAME_PROFILE);
        if (!file.exists()) {
            Log.e(TAG, "File " + file + " does not exist, returning!");
            return null;
        }

        FileInputStream fis = null;
        ObjectInputStream is = null;
        try {
            fis = new FileInputStream(file);
            is = new ObjectInputStream(fis);
            MyProfile myProfile = (MyProfile) is.readObject();
            return myProfile;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) fis.close();
                if (is != null) is.close();
            } catch (IOException e) {
                Log.e(TAG, "Exception:", e);
            }
        }

        Log.e(TAG, "MyProfile is null");
        return null;
    }

    public static void createAndSendRmbProfile(AppData appData, MyProfile myProfile, Uri uri) throws IOException {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appData);

        List<IProfile> friendsToSend = new ArrayList<>();
        for (Profile profile : appData.getProfileDataSource().getProfilesByType(Profile.FAVOURITE)) {
            Log.e(TAG, "Adding to friends: " + profile.getName() + " with profileId: " + profile.getProfileId() + " and croco: " + profile.getCrocoId());
            if (profile.isShowInFriendsEnabled())
                friendsToSend.add(
                        new Profile.Builder(profile.getCrocoId())
                                .addName(profile.getName())
                                .addProfileId(profile.getProfileId())
                                .build()
                );
        }

        RMBProfile rmbProfile = new RMBProfile(
                myProfile.getName(),
                myProfile.getThumbUri() == null
                        ? null
                        : myProfile.getThumbUri().toString(),
                myProfile.getProfileId(),
                friendsToSend,
                preferences.getBoolean(SettingsActivity.SHOW_ME_IN_FRIENDS_PREF, true)
        );

        Log.e(TAG, "Sending RMBProfileID:" + rmbProfile.getProfileId());

        OutgoingPayload payload = new OutgoingPayload(rmbProfile);
        LocalAttachment localAttachment = new LocalAttachment(appData, uri);
        payload.addAttachment(localAttachment);

        OutgoingPersistentBroadcastMessage message = new OutgoingPersistentBroadcastMessage(payload, CptReceiver.PROFILE_PERSISTENT_ID);
        Communication.newMessage(appData, message);
    }

    public static boolean writeMyProfileToFile(Context context, MyProfile myProfile) {
        String directory = CommonUtils.PROFILE_DIRECTORY_MYSELF;
        String fileName = CommonUtils.FILE_NAME_PROFILE;

        String externalState = Environment.getExternalStorageState();
        File defaultDir;

        // find out if external storage is available, if not use cache
        if (Environment.MEDIA_MOUNTED.equals(externalState)) {
            // create croconaut directory if did not exist anymore
            File croconautDir = new File(context.getFilesDir(), CommonUtils.DIRECTORY_WIFON);
            if (!croconautDir.exists()) {
                Log.d(TAG, "Croconaut directory does not exist (external storage dir), creating...");
                if (!croconautDir.mkdir()) {
                    Log.e(TAG, "Dir: " + croconautDir.getName() + " cannnot be created!");
                }
            }

            defaultDir = new File(context.getFilesDir(), directory);
        } else {
            // create croconaut directory if did not exist anymore
            File croconautDir = new File(context.getCacheDir(), CommonUtils.DIRECTORY_WIFON);
            if (!croconautDir.exists()) {
                Log.d(TAG, "Croconaut directory does not exist (cache dir), creating...");
                if (!croconautDir.mkdir()) {
                    Log.e(TAG, "Dir: " + croconautDir.getName() + " cannnot be created!");
                }
            }

            defaultDir = new File(context.getCacheDir(), directory);
        }

        // create directory if don't exists already
        if (!defaultDir.exists()) {
            if (!defaultDir.mkdir()) {
                Log.e(TAG, "Dir: " + defaultDir.getName() + " cannot be created!");
            }
            Log.d(TAG, "Creating directory " + defaultDir);
        }

        // delete existing file and create a new one
        File file = new File(defaultDir, fileName);
        if (file.exists()) {
            boolean deleted = file.delete();
            Log.d(TAG, "File existed and was deleted: " + deleted);
        }

        // we already know that file doesn't exist, but just in case
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    Log.d(TAG, "File exists " + file);
                } else {
                    Log.d(TAG, "Creating file " + file);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        // ready to write profile to file
        FileOutputStream fos = null;
        ObjectOutputStream os = null;
        try {
            Log.d(TAG, "Trying to write " + myProfile.getName() + " profile to file");
            fos = new FileOutputStream(file);
            os = new ObjectOutputStream(fos);
            os.writeObject(myProfile);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) fos.close();
                if (os != null) os.close();
            } catch (IOException e) {
                Log.e(TAG, "Exception:", e);
            }
        }
        Log.d(TAG, "Profile of user " + myProfile.getName() + " wrote to file");
        return true;
    }

    public static TextDrawable getTextDrawableForProfile(IProfile profile) {
        ColorGenerator generator = ColorGenerator.MATERIAL;
        int color = generator.getColor(profile.getIdent());
        String text = (profile instanceof Profile && ((Profile) profile).isUnknown()) ?
                "?" : profile.getName().substring(0, 1);
        return TextDrawable.builder().buildRound(text, color);
    }

    public static int getUnreadMessagesCount(List<Profile> profiles) {
        int count = 0;
        for (Profile profile : profiles) {
            count = count + profile.getUnread();
        }
        return count;
    }

    public static Bitmap getBitmapFromUri(Context context, Uri uri) {
        try {
            InputStream input = context.getContentResolver().openInputStream(uri);

            BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
            onlyBoundsOptions.inJustDecodeBounds = true;
            onlyBoundsOptions.inDither = true;//optional
            onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
            BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
            if (input != null)
                input.close();
            if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1))
                return null;

            int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;

            double ratio = (originalSize > 100) ? (originalSize / 100) : 1.0;

            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
            bitmapOptions.inDither = true;//optional
            bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
            input = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
            if (input != null)
                input.close();
            return bitmap;
        } catch (Exception e) {
            Log.e(TAG, "Fatal error:", e);
        }

        return null;
    }


    public static Bitmap getThumbnail(Context context, Uri uri) {
        try {
            InputStream input = context.getContentResolver().openInputStream(uri);

            BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
            onlyBoundsOptions.inJustDecodeBounds = true;
            onlyBoundsOptions.inDither = true;//optional
            onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
            BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
            if (input != null)
                input.close();
            if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1))
                return null;

            int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;

            double ratio = (originalSize > THUMBNAIL_SIZE) ? (originalSize / THUMBNAIL_SIZE) : 1.0;

            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
            bitmapOptions.inDither = true;//optional
            bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
            input = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
            if (input != null)
                input.close();
            return bitmap;
        } catch (Exception e) {
            Log.e(TAG, "Fatal error:", e);
        }

        return null;
    }

    private static int getPowerOfTwoForSampleRatio(double ratio) {
        int k = Integer.highestOneBit((int) Math.floor(ratio));
        if (k == 0) return 1;
        else return k;
    }


    public static Uri getUriById(Context cxt, long fileId) {
        File defaultDir = new File(cxt.getFilesDir(), CommonUtils.PROFILE_DIRECTORY_MYSELF);

        if (!defaultDir.exists()) {
            Log.e(TAG, "Directory " + defaultDir + " dont exist, returning!");
            return null;
        }

        File file = new File(defaultDir, String.valueOf(fileId) + ".jpg");
        if (!file.exists()) {
            Log.e(TAG, "File " + file + " does not exist, returning!");
            return null;
        }

        return Uri.fromFile(file);
    }

    public static boolean writePhotoToFile(Context context, long fileId, Bitmap bitmap) {
        String directory = CommonUtils.PROFILE_DIRECTORY_MYSELF;

        String externalState = Environment.getExternalStorageState();
        File defaultDir;

        // find out if external storage is available, if not use cache
        if (Environment.MEDIA_MOUNTED.equals(externalState)) {
            // create croconaut directory if did not exist anymore
            File croconautDir = new File(context.getFilesDir(), CommonUtils.DIRECTORY_WIFON);
            if (!croconautDir.exists()) {
                Log.e(TAG, "Croconaut directory does not exist (external storage dir), creating...");
                if (!croconautDir.mkdir()) {
                    Log.e(TAG, "Dir: " + croconautDir.getName() + " cannnot be created!");
                }
            }

            defaultDir = new File(context.getFilesDir(), directory);
        } else {
            // create croconaut directory if did not exist anymore
            File croconautDir = new File(context.getCacheDir(), CommonUtils.DIRECTORY_WIFON);
            if (!croconautDir.exists()) {
                Log.e(TAG, "Croconaut directory does not exist (cache dir), creating...");
                if (!croconautDir.mkdir()) {
                    Log.e(TAG, "Dir: " + croconautDir.getName() + " cannnot be created!");
                }
            }

            defaultDir = new File(context.getCacheDir(), directory);
        }

        // create directory if don't exists already
        if (!defaultDir.exists()) {
            if (!defaultDir.mkdir()) {
                Log.e(TAG, "Dir: " + defaultDir.getName() + " cannot be created!");
            }
            Log.e(TAG, "Creating directory " + defaultDir);
        }

        // delete existing file and create a new one
        File file = new File(defaultDir, String.valueOf(fileId) + ".jpg");
        if (file.exists()) {
            boolean deleted = file.delete();
            Log.e(TAG, "File existed and was deleted: " + deleted);
        }

        // we already know that file doesn't exist, but just in case
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    Log.e(TAG, "File exists " + file);
                } else {
                    Log.e(TAG, "Creating file " + file);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        FileOutputStream fos = null;
        ByteArrayOutputStream stream = null;
        try {
            fos = new FileOutputStream(file);
            stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            fos.write(stream.toByteArray());
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (stream != null) stream.close();
                if (fos != null) fos.close();
            } catch (IOException e) {
                Log.e(TAG, "Exception", e);
            }
        }

        Log.e(TAG, "Image wrote to file: " + Uri.fromFile(file));
        return true;
    }

}
