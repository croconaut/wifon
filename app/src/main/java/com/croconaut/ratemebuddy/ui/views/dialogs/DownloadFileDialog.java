package com.croconaut.ratemebuddy.ui.views.dialogs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.Formatter;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.croconaut.cpt.data.Communication;
import com.croconaut.ratemebuddy.AppData;
import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.utils.CommonUtils;
import com.croconaut.ratemebuddy.utils.ThemeUtils;
import com.croconaut.ratemebuddy.utils.pojo.UIMessage;
import com.croconaut.ratemebuddy.utils.pojo.profiles.Profile;

import java.util.ArrayList;

public class DownloadFileDialog extends FileDialog implements View.OnClickListener {
    private static final String TAG = DownloadFileDialog.class.getName();

    private final AppData appData;
    private UIMessage uiMessage;
    private final long uiMessageId;
    private final Profile remoteProfile;

    public DownloadFileDialog(final Context context, final AppData appData, final Profile remoteProfile,
                              final long messageId) {
        super(
                context,
                appData,
                remoteProfile
        );
        this.appData = appData;
        this.remoteProfile = remoteProfile;
        this.uiMessage = appData.getUiMessageDataSource().getUIMessage(messageId);
        this.uiMessageId = messageId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_download_files);

        updateText();

        Button btnDownload = (Button) findViewById(R.id.btnDownload);
        Button btnNotDownload = (Button) findViewById(R.id.btnNotDownload);
        Button btnWifiOnly = (Button) findViewById(R.id.btnOnlyWifi);

        btnDownload.setOnClickListener(this);
        btnNotDownload.setOnClickListener(this);
        btnWifiOnly.setOnClickListener(this);
    }

    private ArrayList<UIMessage> getUiMessageAttachments() {
        return appData.getUiMessageDataSource().getCurrentAttachments(remoteProfile.getIdent());
    }

    @Override
    protected void updateText() {
        this.uiMessage = appData.getUiMessageDataSource().getUIMessage(uiMessageId);

        final LinearLayout backgrounLayout = (LinearLayout) findViewById(R.id.fdBackground);
        Glide.with(appData)
                .load(ThemeUtils.getEmptyBgResId(PreferenceManager.getDefaultSharedPreferences(appData)))
                .asBitmap()
                .thumbnail(0.4f)
                .into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                        backgrounLayout.setBackground(new BitmapDrawable(bitmap));
                    }
                });

        ImageView ivMimeType = (ImageView) findViewById(R.id.ivMimeType);
        Glide.with(getContext())
                .load(CommonUtils.getImageTypeRes(
                        uiMessage.getUiMessageAttachment().getType()
                        )
                )
                .into(ivMimeType);

        TextView tvName = (TextView) findViewById(R.id.tvName);
        TextView tvState = (TextView) findViewById(R.id.tvState);
        TextView tvFileSize = (TextView) findViewById(R.id.tvFileSize);
        tvName.setText(uiMessage.getUiMessageAttachment().getName());
        tvState.setText(uiMessage.getUiMessageAttachment().getState(appData));
        tvFileSize.setText(
                Formatter.formatShortFileSize(
                        appData,
                        uiMessage.getUiMessageAttachment().getLength()
                )
        );

        CheckBox cbEvenInFuture = (CheckBox) findViewById(R.id.cbEvenInFuture);
        RadioButton rbOnlyThis = (RadioButton) findViewById(R.id.rbOnlyThis);
        RadioButton rbThisAndOther = (RadioButton) findViewById(R.id.rbThisAndOther);

        cbEvenInFuture.setText(appData.getResources().getString(R.string.dialog_download_rb_all_for_future));
        rbOnlyThis.setText(appData.getResources().getString(R.string.dialog_download_rb_only_this));

        //calculate total length and format output
        ArrayList<UIMessage> currentAttachments = getUiMessageAttachments();
        long totalLength = 0;
        for (UIMessage uiMessage : currentAttachments)
            totalLength += uiMessage.getUiMessageAttachment().getLength();
        rbThisAndOther.setText(
                appData.getResources().getString(R.string.dialog_download_rb_this_and_other,
                        currentAttachments.size(),
                        Formatter.formatShortFileSize(appData, totalLength)));
    }

    @Override
    public void onClick(View v) {
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.rgApplicateOn);
        CheckBox cbEvenInFuture = (CheckBox) findViewById(R.id.cbEvenInFuture);

        boolean rememberForFutureUris = cbEvenInFuture.isChecked();

        ArrayList<UIMessage> uiMessagesWithAttachmentToSend = new ArrayList<>();
        switch (radioGroup.getCheckedRadioButtonId()) {
            case R.id.rbOnlyThis:
                uiMessagesWithAttachmentToSend.add(uiMessage);
                break;
            case R.id.rbThisAndOther:
                uiMessagesWithAttachmentToSend = getUiMessageAttachments();
                break;
        }

        boolean isTrustChanged = false;
        for (UIMessage message : uiMessagesWithAttachmentToSend) {
            String storageDirectory = getStorageDirectory(TAG, message);

            switch (v.getId()) {
                case R.id.btnDownload:
                    if (storageDirectory == null) {
                        Communication.requestPrivateDownload(appData,
                                message.getUiAttachmentId(),
                                remoteProfile.getCrocoId(),
                                message.getUiMessageAttachment().getSourceUri()
                        );
                    } else {
                        Communication.requestPublicDownload(appData,
                                message.getUiAttachmentId(),
                                remoteProfile.getCrocoId(),
                                message.getUiMessageAttachment().getSourceUri(),
                                storageDirectory
                        );
                    }

                    if (!isTrustChanged && rememberForFutureUris) {
                        Communication.changeUserTrustLevel(
                                appData,
                                remoteProfile.getCrocoId(),
                                Communication.USER_TRUST_LEVEL_TRUSTED
                        );
                        isTrustChanged = true;
                    }
                    break;

                case R.id.btnNotDownload:
                    if (storageDirectory == null) {
                        Communication.cancelPrivateDownload(appData,
                                message.getUiAttachmentId(),
                                remoteProfile.getCrocoId(),
                                message.getUiMessageAttachment().getSourceUri()
                        );
                    } else {
                        Communication.cancelPublicDownload(appData,
                                message.getUiAttachmentId(),
                                remoteProfile.getCrocoId(),
                                message.getUiMessageAttachment().getSourceUri(),
                                storageDirectory
                        );
                    }

                    if (!isTrustChanged && rememberForFutureUris) {
                        Communication.changeUserTrustLevel(
                                appData,
                                remoteProfile.getCrocoId(),
                                Communication.USER_TRUST_LEVEL_NORMAL
                        );
                        isTrustChanged = true;
                    }
                    break;

                case R.id.btnOnlyWifi:
                    if (storageDirectory == null) {
                        Communication.requestPrivateDownloadOnWifiOnly(appData,
                                message.getUiAttachmentId(),
                                remoteProfile.getCrocoId(),
                                message.getUiMessageAttachment().getSourceUri()
                        );
                    } else {
                        Communication.requestPublicDownloadOnWifiOnly(appData,
                                message.getUiAttachmentId(),
                                remoteProfile.getCrocoId(),
                                message.getUiMessageAttachment().getSourceUri(),
                                storageDirectory
                        );
                    }

                    if (!isTrustChanged && rememberForFutureUris) {
                        Communication.changeUserTrustLevel(
                                appData,
                                remoteProfile.getCrocoId(),
                                Communication.USER_TRUST_LEVEL_TRUSTED_ON_WIFI
                        );
                        isTrustChanged = true;
                    }
                    break;
            }
        }

        this.cancel();
    }
}
