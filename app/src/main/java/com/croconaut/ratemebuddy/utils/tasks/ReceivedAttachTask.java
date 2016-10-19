package com.croconaut.ratemebuddy.utils.tasks;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.croconaut.cpt.data.Communication;
import com.croconaut.cpt.data.DownloadedAttachment;
import com.croconaut.cpt.data.DownloadedAttachmentPreview;
import com.croconaut.cpt.data.MessageAttachment;
import com.croconaut.ratemebuddy.AppData;
import com.croconaut.ratemebuddy.activities.CommunicationActivity;
import com.croconaut.ratemebuddy.activities.CptProcessor;
import com.croconaut.ratemebuddy.activities.MessageDetailActivity;
import com.croconaut.ratemebuddy.activities.TimelineActivity;
import com.croconaut.ratemebuddy.utils.pojo.TimelineInfo;
import com.croconaut.ratemebuddy.utils.pojo.UIMessageAttachment;
import com.croconaut.ratemebuddy.utils.pojo.profiles.Profile;

import java.io.IOException;
import java.util.Date;

public class ReceivedAttachTask extends AsyncTask<Intent, Void, Intent> {

    private final String TAG = ReceivedAttachTask.class.getName();

    private final AppData appData;

    public ReceivedAttachTask(AppData appData) {
        this.appData = appData;
    }

    @Override
    protected Intent doInBackground(Intent... params) {
        Intent intent = params[0];

        long messageId = intent.getLongExtra(Communication.EXTRA_MESSAGE_ID, -1);
        final String crocoId = intent.getStringExtra(Communication.EXTRA_MESSAGE_ATTACHMENT_SOURCE_CROCO_ID);
        final String sourceUri = intent.getStringExtra(Communication.EXTRA_MESSAGE_ATTACHMENT_SOURCE_URI);
        final MessageAttachment messageAttachment = intent.getParcelableExtra(Communication.EXTRA_MESSAGE_ATTACHMENT);

        if (messageAttachment instanceof DownloadedAttachmentPreview) {
            DownloadedAttachmentPreview downloadedAttachmentPreview = (DownloadedAttachmentPreview) messageAttachment;

            appData.getUiMessageDataSource().updateStartDownloadTime(messageId);
            appData.getUiMessageDataSource().updateAttachmentState(
                    messageId,
                    UIMessageAttachment.STATE_DOWNLOADING,
                    crocoId,
                    downloadedAttachmentPreview,
                    appData);

        } else if (messageAttachment instanceof DownloadedAttachment) {
            final DownloadedAttachment downloadedAttachment = (DownloadedAttachment) messageAttachment;
            final Date attachmentTime = (Date) intent.getSerializableExtra(Communication.EXTRA_MESSAGE_ATTACHMENT_TIME);
            final int speed = intent.getIntExtra(Communication.EXTRA_MESSAGE_ATTACHMENT_SPEED, -1);

            Profile profile = appData.getProfileDataSource().getProfileByCrocoId(crocoId);
            TimelineInfo timelineInfo = new TimelineInfo.Builder(System.currentTimeMillis(), messageAttachment.getName(appData),
                    profile.getCrocoId(), profile.getName(), TimelineInfo.INCOMING, TimelineInfo.MESSAGE_TYPE_FILE, false)
                    .fileUri(messageAttachment.getUri().toString())
                    .build();
            appData.getTimelineDataSource().insertTimelineInfo(timelineInfo);

            appData.getUiMessageDataSource().updateEndDownloadTime(messageId);
            appData.getUiMessageDataSource().updateAttachmentStateWithUri(
                    messageId,
                    UIMessageAttachment.STATE_DOWNLOAD_FINISHED,
                    downloadedAttachment,
                    crocoId,
                    appData,
                    attachmentTime.getTime(),
                    speed
            );
        }

        return intent;
    }

    @Override
    protected void onPostExecute(Intent intent) {
        super.onPostExecute(intent);

        CptProcessor cptProcessor = appData.getCurrentActivity();

        if (intent == null || cptProcessor == null) return;

        if (cptProcessor instanceof CommunicationActivity
                || cptProcessor instanceof MessageDetailActivity
                || cptProcessor instanceof TimelineActivity) {
            try {
                cptProcessor.process(intent); //TODO process to show notif and add unread
            } catch (IOException | ClassNotFoundException e) {
                Log.e(TAG, "onPostExecute", e);
            }
        }
    }
}
