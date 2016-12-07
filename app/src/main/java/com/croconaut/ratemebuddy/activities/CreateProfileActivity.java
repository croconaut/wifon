package com.croconaut.ratemebuddy.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bumptech.glide.Glide;
import com.croconaut.cpt.data.Communication;
import com.croconaut.cpt.data.LocalAttachment;
import com.croconaut.cpt.data.OutgoingPayload;
import com.croconaut.cpt.data.OutgoingPersistentBroadcastMessage;
import com.croconaut.ratemebuddy.CptReceiver;
import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.SharedFilesContract;
import com.croconaut.ratemebuddy.data.pojo.RMBProfile;
import com.croconaut.ratemebuddy.data.pojo.Status;
import com.croconaut.ratemebuddy.utils.CommonUtils;
import com.croconaut.ratemebuddy.utils.ProfileUtils;
import com.croconaut.ratemebuddy.utils.ThemeUtils;
import com.croconaut.ratemebuddy.utils.pojo.profiles.IProfile;
import com.croconaut.ratemebuddy.utils.pojo.profiles.MyProfile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.UUID;


public class CreateProfileActivity extends ProfileActivity implements CptProcessor {
    private static final String TAG = CreateProfileActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);
        initiliazeViews();
    }

    @Override
    public boolean process(Intent cptIntent) {
        return false;
    }

    private void initiliazeViews() {
        toolbar = (Toolbar) findViewById(R.id.idToolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitle(mRes.getString(R.string.activity_create_profile));
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);

        etStatus = (EditText) findViewById(R.id.etStatus);
        ivCover = (ImageView) findViewById(R.id.ivCover);
        ivPhoto = (ImageView) findViewById(R.id.ivImage);

        etName = (EditText) findViewById(R.id.etName);
        etName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(NAME_CHECK_MAX_LENGTH)});

        Glide.with(this)
                .load(ThemeUtils.getBgCoverResId(prefs))
                .asBitmap()
                .thumbnail(0.3f)
                .into(ivCover);

        ivPhoto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                createImageDialog(CreateProfileActivity.this);
            }
        });

        etName.setTypeface(tSemiBold);
        etStatus.setTypeface(tSemiBold);


        if (commonUtils.getMyName(mContext) != null) {
            etName.setText(commonUtils.getMyName(mContext));
        }

        initPhoto();

        currentUri = null;

        etName.requestFocus();
    }

    protected void collectInfoAndCreateProfile() {
        String name = etName.getText().toString().trim();

        setCollectClickable = false;
        supportInvalidateOptionsMenu();

        // check name and status
        int nameLength = name.trim().length();
        if (nameLength < CHECK_MIN_LENGTH || nameLength > NAME_CHECK_MAX_LENGTH) {
            toast(mRes.getString(R.string.toast_error_name));
            setCollectClickable = true;
            supportInvalidateOptionsMenu();
            return;
        }


        Bitmap bitmap;
        if (ivPhoto.getDrawable() instanceof TextDrawable) {
            ColorGenerator generator = ColorGenerator.MATERIAL;
            int color = generator.getColor(name);
            TextDrawable drawable = TextDrawable.builder().buildRect(
                    name.trim().substring(0, 1).toUpperCase(),
                    color
            );

            ImageView imageView = new ImageView(mContext);
            imageView.setImageDrawable(drawable);
            bitmap = getBitmap(imageView, currentUri, mContext);
        } else {
            bitmap = getBitmap(ivPhoto, currentUri, mContext);

            try {
                ExifInterface exif = new ExifInterface(currentUri.getPath());
                int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                int rotationInDegrees = exifToDegrees(rotation);

                Matrix matrix = new Matrix();
                if (rotation != 0f) {
                    matrix.preRotate(rotationInDegrees);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                }
            } catch (Exception e) {
                Log.e(TAG, "Cannot rotate picture: ", e);
            }
        }

        ProfileUtils.writePhotoToFile(mContext, System.currentTimeMillis(), bitmap);
        try {
            Uri uri = Uri.withAppendedPath(SharedFilesContract.getThumbnailsUri(this), "myProfile.png");
            OutputStream fos = mContext.getContentResolver().openOutputStream(uri, "w");
            ByteArrayOutputStream stream = null;
            try {
                if (fos != null) {
                    stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    fos.write(stream.toByteArray());
                } else
                    Log.e(TAG, "FOS is null for uri: " + uri);
            } finally {
                try {
                    if (stream != null) stream.close();
                    if (fos != null) fos.close();
                } catch (IOException e) {
                    Log.e(TAG, "Exception", e);
                }
            }

            MyProfile myProfile = new MyProfile.Builder(CommonUtils.getMyId(this))
                    .name(name)
                    .thumbnail(uri)
                    .build(this);

            RMBProfile rmbProfile = new RMBProfile(
                    myProfile.getName(),
                    myProfile.getThumbUri() == null
                            ? null
                            : myProfile.getThumbUri().toString(),
                    myProfile.getProfileId(),
                    Collections.<IProfile>emptyList(),
                    prefs.getBoolean(SettingsActivity.SHOW_ME_IN_FRIENDS_PREF, true)
            );

            OutgoingPayload payload = new OutgoingPayload(rmbProfile);
            LocalAttachment localAttachment = new LocalAttachment(this, uri);
            payload.addAttachment(localAttachment);

            OutgoingPersistentBroadcastMessage message = new OutgoingPersistentBroadcastMessage(payload, CptReceiver.PROFILE_PERSISTENT_ID);
            Communication.register(this, myProfile.getName(), CptReceiver.class);
            Communication.newMessage(this, message);

            String statusContent = etStatus.getText().toString();
            int statusLength = statusContent.trim().length();
            Status status;
            if (statusLength > 0) {
                status = new Status.Builder(System.currentTimeMillis(), UUID.randomUUID().toString(), CommonUtils.getMyId(this), statusContent, myProfile.getName())
                        .build();
                if (status.getContent() != null && !status.getContent().isEmpty()) {
                    appData.getStatusDataSource().insertStatus(status);
                    new MyProfile.Builder(myProfile).status(status).build(this);
                    sendStatus(status);
                }
            } else {
                status = new Status.Builder(System.currentTimeMillis(), UUID.randomUUID().toString(), CommonUtils.getMyId(this), null, myProfile.getName())
                        .build();
                appData.getStatusDataSource().insertStatus(status);
                new MyProfile.Builder(myProfile).status(status).build(this);
                sendStatus(status);
            }

            toast(mRes.getString(R.string.toast_my_profile_changed));

            finish();
            Intent intent = new Intent(this, TimelineActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        } catch (Exception e) {
            Log.e(TAG, "Fatal exception:", e);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        supportInvalidateOptionsMenu();
    }

}
