package com.croconaut.ratemebuddy.utils.tasks;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.croconaut.cpt.data.Communication;
import com.croconaut.ratemebuddy.AppData;
import com.croconaut.ratemebuddy.activities.CommunicationActivity;
import com.croconaut.ratemebuddy.activities.CptProcessor;
import com.croconaut.ratemebuddy.activities.MessageDetailActivity;
import com.croconaut.ratemebuddy.utils.ProfileUtils;
import com.croconaut.ratemebuddy.utils.pojo.TimelineInfo;
import com.croconaut.ratemebuddy.utils.pojo.UIMessage;
import com.croconaut.ratemebuddy.utils.pojo.UIMessageAttachment;
import com.croconaut.ratemebuddy.utils.pojo.profiles.Profile;

import java.io.IOException;
import java.util.Date;


public class AttachmentSentTask extends AsyncTask<Intent, Void, Intent> {

    private static final String TAG = AttachmentSentTask.class.getName();

    private final AppData appData;

    public AttachmentSentTask(AppData appData) {
        this.appData = appData;
    }

    @Override
    protected Intent doInBackground(Intent... params) {
        Intent intent = params[0];

        final long messageId = intent.getLongExtra(Communication.EXTRA_MESSAGE_ID, -1);
        final Date attachmentTime = (Date) intent.getSerializableExtra(Communication.EXTRA_MESSAGE_ATTACHMENT_TIME);
        final int speed = intent.getIntExtra(Communication.EXTRA_MESSAGE_ATTACHMENT_SPEED, -1);

        int sendType = UIMessage.SENT_TO_INTERNET;
        int attachmentState = -1;
        switch (intent.getAction()) {
            case Communication.ACTION_MESSAGE_ATTACHMENT_UPLOADED_TO_APP_SERVER:
                sendType = UIMessage.SENT_TO_INTERNET;
                attachmentState = UIMessageAttachment.STATE_UPLOADED_TO_SERVER;
                createOrUpdateTimelineInfo(messageId);
                break;
            case Communication.ACTION_MESSAGE_ATTACHMENT_DELIVERED:
                sendType = UIMessage.ATTACHMENT_DELIVERED;
                break;
            case Communication.ACTION_MESSAGE_ATTACHMENT_UPLOADED_TO_RECIPIENT:
                sendType = UIMessage.SENT_TO_RECIPIENT;
                attachmentState = UIMessageAttachment.STATE_UPLOADED_TO_RECIPIENT;
                createOrUpdateTimelineInfo(messageId);
                break;
        }

        appData.getUiMessageDataSource().updateAttachmentSent(
                messageId,
                sendType,
                attachmentTime.getTime(),
                speed,
                attachmentState
        );

        return intent;
    }

    private void createOrUpdateTimelineInfo(long messageId){
        UIMessage uiMessage = appData.getUiMessageDataSource().getUIMessage(messageId);

        if(uiMessage != null && uiMessage.hasAttachment() && uiMessage.getSendType() != UIMessage.INCOMING) {
            Profile profile = (Profile) new ProfileUtils(appData).findProfile(uiMessage.getCrocoId());

            TimelineInfo existingInfo = appData.getTimelineDataSource().getTimelineInfoByFileUri(uiMessage.getFileUri().toString());
            if(existingInfo != null){
                Log.e(TAG, "Message timeline info already send, returning!");
                return;
            }

            UIMessageAttachment uiMessageAttachment = uiMessage.getUiMessageAttachment();
            TimelineInfo timelineInfo = new TimelineInfo.Builder(System.currentTimeMillis(), uiMessageAttachment.getName(),
                    profile.getCrocoId(), profile.getName(), TimelineInfo.OUTGOING, TimelineInfo.MESSAGE_TYPE_FILE, false)
                    .fileUri(uiMessageAttachment.getUri().toString())
                    .build();
            appData.getTimelineDataSource().insertTimelineInfo(timelineInfo);
        }else
            Log.e(TAG, "Cannot create timeline info");
    }

    @Override
    protected void onPostExecute(Intent intent) {
        super.onPostExecute(intent);

        CptProcessor cptProcessor = appData.getCurrentActivity();
        if (intent == null || cptProcessor == null) return;

        if (cptProcessor instanceof CommunicationActivity
                || cptProcessor instanceof MessageDetailActivity) {
            try {
                cptProcessor.process(intent);
            } catch (IOException | ClassNotFoundException e) {
                Log.e(TAG, "onPostExecute", e);
            }
        }
    }

}
