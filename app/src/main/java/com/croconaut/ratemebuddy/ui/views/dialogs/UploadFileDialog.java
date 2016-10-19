package com.croconaut.ratemebuddy.ui.views.dialogs;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.format.Formatter;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.croconaut.cpt.data.Communication;
import com.croconaut.cpt.data.LocalAttachment;
import com.croconaut.cpt.data.OutgoingMessage;
import com.croconaut.cpt.data.OutgoingPayload;
import com.croconaut.ratemebuddy.AppData;
import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.data.pojo.Attachment;
import com.croconaut.ratemebuddy.utils.CommonUtils;
import com.croconaut.ratemebuddy.utils.ThemeUtils;
import com.croconaut.ratemebuddy.utils.pojo.UIMessage;
import com.croconaut.ratemebuddy.utils.pojo.UIMessageAttachment;
import com.croconaut.ratemebuddy.utils.pojo.profiles.Profile;

import java.io.IOException;

public class UploadFileDialog extends FileDialog {
    private static final String TAG = UploadFileDialog.class.getName();

    protected UIMessage mUiMessage;
    protected long mUiMessageId;
    protected OutgoingMessage mServiceMsg;


    public UploadFileDialog(final @NonNull Context context, @NonNull final AppData appData,
                            final @NonNull Profile remoteProfile, final long uiMessageId) {
        this(context, appData, remoteProfile);

        this.mUiMessage = appData.getUiMessageDataSource().getUIMessage(uiMessageId);
        this.mUiMessageId = uiMessageId;
    }

    public UploadFileDialog(final @NonNull Context context, @NonNull final AppData appData,
                            final @NonNull Profile remoteProfile, final boolean isImage,
                            final @NonNull Uri uri) {
        this(context, appData, remoteProfile);

        try {
            createUiMessage(uri, isImage);
        } catch (IOException e) {
            Log.e(TAG, "Cannot create message: ", e);
        }
    }

    private UploadFileDialog(final @NonNull Context context, @NonNull final AppData appData,
                             final @NonNull Profile remoteProfile) {
        super(
                context,
                appData,
                remoteProfile
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_upload_files);
        updateText();

        Button btnUpload = (Button) findViewById(R.id.btnUpload);
        Button btnNotUpload = (Button) findViewById(R.id.btnNotUpload);
        Button btnUploadWifiOnly = (Button) findViewById(R.id.btnUploadOnlyWifi);

        btnUpload.setOnClickListener(this);
        btnNotUpload.setOnClickListener(this);
        btnUploadWifiOnly.setOnClickListener(this);
    }

    @Override
    protected void updateText() {
        if (mUiMessageId != 0)
            this.mUiMessage = appData.getUiMessageDataSource().getUIMessage(mUiMessageId);

        final UIMessageAttachment uiMessageAttachment = mUiMessage.getUiMessageAttachment();
        final LinearLayout backgroundView = (LinearLayout) findViewById(R.id.fdBackground);

        Glide.with(appData)
                .load(ThemeUtils.getEmptyBgResId(PreferenceManager.getDefaultSharedPreferences(appData)))
                .asBitmap()
                .thumbnail(0.4f)
                .into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                        backgroundView.setBackground(new BitmapDrawable(bitmap));
                    }
                });

        ImageView ivMimeType = (ImageView) findViewById(R.id.ivMimeType);
        Glide.with(getContext())
                .load(uiMessageAttachment.getUri())
                .error(CommonUtils.getImageTypeRes(uiMessageAttachment.getType()))
                .into(ivMimeType);

        TextView tvName = (TextView) findViewById(R.id.tvName);
        TextView tvState = (TextView) findViewById(R.id.tvState);
        TextView tvFileSize = (TextView) findViewById(R.id.tvFileSize);

        String fileName = uiMessageAttachment.getName();
        SpannableString ss = new SpannableString(uiMessageAttachment.getName());

        ss.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                openFile();
            }
        }, 0, fileName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvName.setText(ss);
        tvName.setMovementMethod(LinkMovementMethod.getInstance());

        tvState.setText(uiMessageAttachment.getState(appData));
        tvFileSize.setText(
                Formatter.formatShortFileSize(
                        appData,
                        uiMessageAttachment.getLength()
                )
        );


    }

    private void openFile() {
        try {
            CommonUtils.openFile(appData, mUiMessage.getFileUri());
        } catch (Exception e) {
            Log.e(TAG, "Cannot open file", e);
            Toast.makeText(
                    getContext(),
                    appData.getString(R.string.toast_error_open_file),
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void onClick(View v) {
        CheckBox cbEvenInFuture = (CheckBox) findViewById(R.id.cbEvenInFuture);
        boolean rememberForFuture = cbEvenInFuture.isChecked();
        String storageDirectory = getStorageDirectory(TAG, mUiMessage);

        switch (v.getId()) {
            case R.id.btnUpload:
                updateProfileDialogFlag(rememberForFuture, false);

                sendMessageToCptIfNeeded();

                if (storageDirectory == null) {
                    Communication.requestPrivateUpload(appData,
                            mUiMessage.getUiAttachmentId(),
                            remoteProfile.getCrocoId(),
                            mUiMessage.getUiMessageAttachment().getSourceUri()
                    );
                } else {
                    Communication.requestPublicUpload(appData,
                            mUiMessage.getUiAttachmentId(),
                            remoteProfile.getCrocoId(),
                            mUiMessage.getUiMessageAttachment().getSourceUri(),
                            storageDirectory
                    );
                }

                changeTrustLevel(rememberForFuture, Communication.USER_TRUST_LEVEL_TRUSTED);

                break;
            case R.id.btnUploadOnlyWifi:
                updateProfileDialogFlag(rememberForFuture, false);

                sendMessageToCptIfNeeded();

                if (storageDirectory == null) {
                    Communication.requestPrivateUploadOnWifiOnly(appData,
                            mUiMessage.getUiAttachmentId(),
                            remoteProfile.getCrocoId(),
                            mUiMessage.getUiMessageAttachment().getSourceUri()
                    );
                } else {
                    Communication.requestPublicUploadOnWifiOnly(appData,
                            mUiMessage.getUiAttachmentId(),
                            remoteProfile.getCrocoId(),
                            mUiMessage.getUiMessageAttachment().getSourceUri(),
                            storageDirectory
                    );
                }

                changeTrustLevel(rememberForFuture, Communication.USER_TRUST_LEVEL_TRUSTED_ON_WIFI);

                break;
            case R.id.btnNotUpload:
                updateProfileDialogFlag(rememberForFuture, true);

                if (storageDirectory == null) {
                    Communication.cancelPrivateUpload(appData,
                            mUiMessage.getUiAttachmentId(),
                            remoteProfile.getCrocoId(),
                            mUiMessage.getUiMessageAttachment().getSourceUri()
                    );
                } else {
                    Communication.cancelPublicUpload(appData,
                            mUiMessage.getUiAttachmentId(),
                            remoteProfile.getCrocoId(),
                            mUiMessage.getUiMessageAttachment().getSourceUri(),
                            storageDirectory
                    );
                }

                changeTrustLevel(rememberForFuture, Communication.USER_TRUST_LEVEL_NORMAL);

                break;
        }


        Log.e(TAG, "After - isShow: " + remoteProfile.isShowUploadDialog());
        this.cancel();
    }

    private void changeTrustLevel(boolean rememberForFuture, int userTrustLevel) {
        if (rememberForFuture)
            Communication.changeUserTrustLevel(
                    appData,
                    remoteProfile.getCrocoId(),
                    userTrustLevel
            );
    }

    private void insertMessage() {
        appData.getUiMessageDataSource().insertMessage(this.mUiMessage);
    }

    private void sendMessageToCptIfNeeded() {

        /**
         * If mServiceMsg is NOT null, than this is the first time that we are sending message to CPT.
         *
         * In this case we need to store out UIMessage as well as send message to CPT.
         */
        if (mServiceMsg != null) {
            insertMessage();
            Communication.newMessage(appData, mServiceMsg);
        }
    }

    private void updateProfileDialogFlag(final boolean rememberForFuture, final boolean showDialog) {
        if (rememberForFuture) {
            remoteProfile = new Profile.Builder(remoteProfile)
                    .showUploadDialog(showDialog ? Profile.SHOW_FILE_DIALOG : Profile.NOT_DISPLAY_IN_FRIENDS)
                    .build();

            appData.getProfileDataSource().updateProfile(remoteProfile);
        }
        //else do nothing, dialog flag depends on user interaction ...
    }

    private void createUiMessage(Uri fileUri, boolean fileIsImage) throws IOException {
        LocalAttachment localAttachment = new LocalAttachment(appData,
                fileUri,
                fileIsImage
                        ? Environment.DIRECTORY_PICTURES
                        : Environment.DIRECTORY_DOWNLOADS
        );

        mServiceMsg = new OutgoingMessage(
                remoteProfile.getCrocoId(),
                new OutgoingPayload(new Attachment()).addAttachment(localAttachment)
        );

        UIMessageAttachment messageAttachment = new UIMessageAttachment.Builder(localAttachment.getName(appData))
                .state(UIMessageAttachment.STATE_UPLOAD_WAITING)
                .length(localAttachment.getLength(appData))
                .type(localAttachment.getType(appData))
                .storageType(fileIsImage ? UIMessageAttachment.PUBLIC_IMAGE : UIMessageAttachment.PUBLIC_OTHER)
                .uri(fileUri.toString())
                .build();

        mUiMessage = new UIMessage.Builder(remoteProfile.getCrocoId(),
                localAttachment.getName(appData),
                mServiceMsg.getCreationDate().getTime(),
                UIMessage.WAITING)
                .uiMessageAttachment(messageAttachment)
                .uiAttachmentId(mServiceMsg.getId())
                .build();
    }


}
