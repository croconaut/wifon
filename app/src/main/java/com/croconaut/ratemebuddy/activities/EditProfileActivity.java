package com.croconaut.ratemebuddy.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.croconaut.cpt.data.Communication;
import com.croconaut.cpt.data.IncomingMessage;
import com.croconaut.ratemebuddy.CptReceiver;
import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.SharedFilesContract;
import com.croconaut.ratemebuddy.activities.notifications.CommentNotification;
import com.croconaut.ratemebuddy.activities.notifications.Notification;
import com.croconaut.ratemebuddy.activities.notifications.VoteUpNotification;
import com.croconaut.ratemebuddy.data.pojo.Comment;
import com.croconaut.ratemebuddy.data.pojo.Status;
import com.croconaut.ratemebuddy.data.pojo.VoteUp;
import com.croconaut.ratemebuddy.ui.views.transformation.CircleTransform;
import com.croconaut.ratemebuddy.utils.ProfileUtils;
import com.croconaut.ratemebuddy.utils.ThemeUtils;
import com.croconaut.ratemebuddy.utils.pojo.profiles.MyProfile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;


public class EditProfileActivity extends ProfileActivity implements CptProcessor {

    private static final String TAG = EditProfileActivity.class.getName();
    public static final String EXTRA_STATUS_ID = "extraStatusId";

    private ImageButton btnComments;
    private Status mStatus;
    private View statusStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mStatus = MyProfile.getInstance(mContext).getStatus();

        initializeViews();
        loadProfileFromFile(true);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.hasExtra(EXTRA_STATUS_ID)) {
            String statusId = intent.getStringExtra(EXTRA_STATUS_ID);
            mStatus = appData.getStatusDataSource().getStatusByID(statusId);
            loadMyStatus(mStatus);
        }
    }

    private void loadMyStatus(final Status status) {
        if (status != null && status.getContent() != null) {
            statusStats.setVisibility(View.VISIBLE);
            btnComments.setVisibility(View.VISIBLE);
            tvStatus.setText(status.getContent());
            nrOfLikes.setText(String.valueOf(status.getVotes().size()));
            nrOfComments.setText(String.valueOf(status.getComments().size()));
        } else {
            btnComments.setVisibility(View.GONE);
            statusStats.setVisibility(View.GONE);
            tvStatus.setText(mRes.getString(R.string.profile_et_status_hint));
        }
    }

    // if its requested, load profile from file
    private void loadProfileFromFile(boolean updatePhoto) {
        final MyProfile myProfile = MyProfile.getInstance(mContext);
        if (myProfile == null) {
            Log.e(TAG, "Profile file not found, returning!");
            return;
        }

        etName.setText(myProfile.getName());
        etName.setSelection(etName.getText().length());
        etName = (EditText) findViewById(R.id.etName);
        etName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(NAME_CHECK_MAX_LENGTH)});


        loadMyStatus(appData.getStatusDataSource().getStatusByID(mStatus.getStatusID()));

        if (updatePhoto) {
            Glide.with(this)
                    .load(myProfile.getThumbUri())
                    .asBitmap()
                    .signature(new StringSignature(
                            String.valueOf(MyProfile.getInstance(this).getTimeStamp())))
                    .thumbnail(0.2f)
                    .transform(new CircleTransform(mContext))
                    .into(ivPhoto);
            currentUri = myProfile.getThumbUri();
        }
    }

    private void initializeViews() {
        initializeHeaderWithDrawer(mRes.getString(R.string.action_bar_title_my_profile), false);

        etName = (EditText) findViewById(R.id.etName);
        tvStatus = (TextView) findViewById(R.id.tvStatus);
        nrOfLikes = (TextView) findViewById(R.id.likesNumber);
        nrOfComments = (TextView) findViewById(R.id.commentsNumber);
        ivCover = (ImageView) findViewById(R.id.ivCover);
        RelativeLayout statusContainer = (RelativeLayout) findViewById(R.id.statusContainer);
        btnComments = (ImageButton) findViewById(R.id.btnComments);
        statusStats = findViewById(R.id.statusStats);
        ivPhoto = (ImageView) findViewById(R.id.ivImage);
        ivPhoto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                createImageDialog(EditProfileActivity.this);
            }
        });


        Glide.with(this)
                .load(ThemeUtils.getBgCoverResId(prefs))
                .asBitmap()
                .thumbnail(0.3f)
                .into(ivCover);

        etName.setTypeface(tSemiBold);
        tvStatus.setTypeface(tSemiBold);

        etName.clearFocus();
        tvStatus.clearFocus();

        statusContainer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EditProfileActivity.this, ChangeStatusActivity.class);
                startActivity(intent);
            }
        });

        statusStats.setOnClickListener(openComments);
        btnComments.setOnClickListener(openComments);
    }

    View.OnClickListener openComments = new View.OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(EditProfileActivity.this, CommentActivity.class);
            intent.putExtra(CommentActivity.EXTRA_CROCO_ID, MyProfile.getInstance(mContext).getIdent());
            startActivity(intent);
        }
    };

    @Override
    public boolean process(Intent cptIntent) throws IOException, ClassNotFoundException {
        super.process(cptIntent);

        switch (cptIntent.getAction()) {
            case Communication.ACTION_MESSAGE_ARRIVED:
                final IncomingMessage message = cptIntent.getParcelableExtra(Communication.EXTRA_MESSAGE_ARRIVED);
                Serializable data = message.getPayload().getAppData();

                if (data instanceof Comment) {
                    Comment comment = (Comment) data;
                    if (comment.getStatusId().equals(mStatus.getStatusID())) {
                        MyProfile myProfile = MyProfile.getInstance(mContext);
                        mStatus = myProfile.getStatus();
                        nrOfComments.setText(String.valueOf(MyProfile.getInstance(mContext).getStatus().getComments().size()));
                        return true;
                    }
                } else if (data instanceof VoteUp) {
                    VoteUp voteUp = (VoteUp) data;
                    if (voteUp.getStatusId().equals(mStatus.getStatusID())) {
                        MyProfile myProfile = MyProfile.getInstance(mContext);
                        mStatus = myProfile.getStatus();
                        nrOfLikes.setText(String.valueOf(MyProfile.getInstance(mContext).getStatus().getVotes().size()));
                        return true;
                    }
                }
        }
        return false;
    }


    // checks if all variebles are filled, if yes wrote profile to file
    protected void collectInfoAndCreateProfile() {
        String name = etName.getText().toString().trim();
        setCollectClickable = false;
        supportInvalidateOptionsMenu();
        // check name and mStatus
        int nameLength = name.trim().length();
        if (nameLength < CHECK_MIN_LENGTH || nameLength > NAME_CHECK_MAX_LENGTH) {
            toast(mRes.getString(R.string.toast_error_name));
            setCollectClickable = true;
            supportInvalidateOptionsMenu();
            return;
        }

        MyProfile myProfile = MyProfile.getInstance(mContext);
        boolean sameName = myProfile.getName().equals(name);
        boolean samePhoto = myProfile.getThumbUri().equals(currentUri);
        boolean sameProfile = sameName && samePhoto;
        boolean sameStatus = myProfile.getStatus().equals(mStatus);

        if (!sameStatus) {
            Log.e(TAG, "Status has changed, sending status");
            myProfile = new MyProfile.Builder(myProfile).status(mStatus).build(mContext);
            sendStatus(myProfile.getStatus());
            createTimelineMessage(getString(R.string.message_timeline_status_changed, mStatus.getContent()));
        }

        if (sameProfile) {
            Log.e(TAG, "Profile is the same, returning");
            setCollectClickable = true;
            finish();
            lunchTimeline();
            return;
        } else Log.e(TAG, "Profile has changed, proceeding");

        try {
            Bitmap bitmap = getBitmap(ivPhoto, currentUri, mContext);

            ExifInterface exif = new ExifInterface(currentUri.getPath());
            int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int rotationInDegrees = exifToDegrees(rotation);

            Matrix matrix = new Matrix();
            if (rotation != 0f) {
                matrix.preRotate(rotationInDegrees);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }

            Uri uri = Uri.withAppendedPath(SharedFilesContract.getThumbnailsUri(this), "myProfile.png");
            OutputStream fos = mContext.getContentResolver().openOutputStream(uri);
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


            myProfile = new MyProfile.Builder(MyProfile.getInstance(this))
                    .name(name)
                    .thumbnail(uri)
                    .build(this);

            ProfileUtils.createAndSendRmbProfile(appData, myProfile, uri);

            Communication.register(this, myProfile.getName(), CptReceiver.class);

            if(!sameName)
                createTimelineMessage(getString(R.string.message_timeline_name_changed, name));

            if(!samePhoto) {
                long id = createTimelineMessage(getString(R.string.message_timeline_photo_changed));
                ProfileUtils.writePhotoToFile(mContext, id, bitmap);
            }

            finish();
            lunchTimeline();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Exception:", e);
        }
    }

    private void lunchTimeline() {
        Intent intent = new Intent(this, TimelineActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    protected void onPause() {
        super.onPause();
        drawerLayout.closeDrawer(Gravity.LEFT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        supportInvalidateOptionsMenu();
        Notification.clearNotification(mContext, VoteUpNotification.VOTE_UP_NOTIF_ID);
        Notification.clearNotification(mContext, CommentNotification.COMMENT_NOTIF_ID);
        loadProfileFromFile(false);
    }

}