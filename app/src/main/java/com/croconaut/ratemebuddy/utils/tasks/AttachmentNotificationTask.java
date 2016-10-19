package com.croconaut.ratemebuddy.utils.tasks;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.croconaut.cpt.data.Communication;
import com.croconaut.ratemebuddy.AppData;
import com.croconaut.ratemebuddy.activities.CommunicationActivity;
import com.croconaut.ratemebuddy.utils.pojo.UIMessage;


public class AttachmentNotificationTask extends AsyncTask<Intent, Void, Intent> {

    private static final String TAG = AttachmentSentTask.class.getName();

    private final AppData appData;

    public AttachmentNotificationTask(AppData appData) {
        this.appData = appData;
    }


    @Override
    protected Intent doInBackground(Intent... params) {
        Intent intent = params[0];

        long messageId = intent.getLongExtra(Communication.EXTRA_MESSAGE_ID, -1);

        Log.e(TAG, "Received AttachmentNotificationTask with ID: " + messageId);
        UIMessage uiMessage = appData.getUiMessageDataSource().getUIMessage(messageId);
        Intent communicationIntent = new Intent(appData, CommunicationActivity.class);
        communicationIntent.putExtra(CommunicationActivity.EXTRA_TARGET_CROCO_ID, uiMessage.getCrocoId());
        communicationIntent.putExtra(CommunicationActivity.EXTRA_SHOW_DIALOG_MSG, messageId);
        communicationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return communicationIntent;
    }

    @Override
    protected void onPostExecute(Intent intent) {
        super.onPostExecute(intent);
        Log.e(TAG, "Starting CommunicationActivity from AttachmentNotificationTask");
        appData.startActivity(intent);
    }
}
