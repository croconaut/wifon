package com.croconaut.ratemebuddy.utils.tasks;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.croconaut.cpt.data.Communication;
import com.croconaut.ratemebuddy.AppData;
import com.croconaut.ratemebuddy.activities.CommunicationActivity;
import com.croconaut.ratemebuddy.activities.CptProcessor;
import com.croconaut.ratemebuddy.activities.MessageDetailActivity;
import com.croconaut.ratemebuddy.utils.pojo.UIMessageAttachment;

import java.io.IOException;


public class AttachmentStateTask extends AsyncTask<Intent, Void, Intent> {

    private final String TAG = AttachmentStateTask.class.getName();

    private final AppData appData;

    public AttachmentStateTask(AppData appData) {
        this.appData = appData;
    }

    @Override
    protected Intent doInBackground(Intent... params) {
        Intent intent = params[0];

        long messageId = intent.getLongExtra(Communication.EXTRA_MESSAGE_ID, -1);
        final String crocoId = intent.getStringExtra(Communication.EXTRA_MESSAGE_ATTACHMENT_SOURCE_CROCO_ID);
        final String sourceUri = intent.getStringExtra(Communication.EXTRA_MESSAGE_ATTACHMENT_SOURCE_URI);

        int state = -1;
        switch (intent.getAction()) {
            case Communication.ACTION_MESSAGE_ATTACHMENT_DOWNLOAD_CONFIRMED:
                state = UIMessageAttachment.STATE_WAITING_FOR_DOWNLOAD;
                break;
            case Communication.ACTION_MESSAGE_ATTACHMENT_DOWNLOAD_CANCELLED:
                state = UIMessageAttachment.STATE_DOWNLOAD_CANCELLED;
                break;
            case Communication.ACTION_MESSAGE_ATTACHMENT_UPLOAD_CONFIRMED:
                state = UIMessageAttachment.STATE_UPLOAD_WAITING;
                break;
            case Communication.ACTION_MESSAGE_ATTACHMENT_UPLOAD_CANCELLED:
                state = UIMessageAttachment.STATE_UPLOAD_CANCELLED;
                break;
            case Communication.ACTION_MESSAGE_ATTACHMENT_UPLOADING_TO_APP_SERVER:
            case Communication.ACTION_MESSAGE_ATTACHMENT_UPLOADING_TO_RECIPIENT:
                state = UIMessageAttachment.STATE_UPLOADING;
                break;
        }

        if (state == -1) {
            Log.e(TAG, "Received bad state");
            return null;
        }


        if(crocoId != null) {
            appData.getUiMessageDataSource().updateAttachmentState(messageId, state, crocoId, sourceUri);
        }else {
            appData.getUiMessageDataSource().updateAttachmentState(messageId, state, sourceUri);
        }

        return intent;
    }

    @Override
    protected void onPostExecute(Intent intent) {
        super.onPostExecute(intent);

        CptProcessor cptProcessor = appData.getCurrentActivity();
        if (cptProcessor == null) return;

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
