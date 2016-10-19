package com.croconaut.ratemebuddy.utils.tasks;


import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.croconaut.cpt.data.Communication;
import com.croconaut.ratemebuddy.AppData;
import com.croconaut.ratemebuddy.activities.CommunicationActivity;
import com.croconaut.ratemebuddy.activities.CptProcessor;
import com.croconaut.ratemebuddy.activities.MessageDetailActivity;
import com.croconaut.ratemebuddy.data.UIMessageDataSource;
import com.croconaut.ratemebuddy.utils.pojo.UIMessage;

public class MessageDeletedTask extends AsyncTask<Intent, Void, Intent> {

    private static final String TAG = MessageDeletedTask.class.getName();
    private AppData appData;

    public MessageDeletedTask(AppData appData) {
        this.appData = appData;
    }

    @Override
    protected Intent doInBackground(Intent... params) {
        Intent intent = params[0];

        Long id = params[0].getLongExtra(Communication.EXTRA_MESSAGE_ID, -1);
        UIMessageDataSource uiMessageDataSource = appData.getUiMessageDataSource();
        UIMessage uiMessage = uiMessageDataSource.getUIMessage(id);

        if (uiMessage == null) return null;

        boolean isIncoming = uiMessage.getSendType() == UIMessage.INCOMING;
        boolean hasAttachment = uiMessage.getUiMessageAttachment() != null;
        boolean isDelivered = uiMessage.getSendType() == UIMessage.ACKED
                || uiMessage.getSendType() == UIMessage.ATTACHMENT_DELIVERED;

        if (!isIncoming) {
            if (hasAttachment && uiMessage.getSendType() == UIMessage.ATTACHMENT_DELIVERED) { // ak ma prilohu a priloha nie je dorucena
                uiMessageDataSource.setUIMessageDeleted(uiMessage.getCreationTime(), uiMessage.getSendType());
                return intent;
            } else if (!hasAttachment && !isDelivered) { // ak nema prilohu a nie je dorucena
                uiMessageDataSource.setUIMessageDeleted(uiMessage.getCreationTime(), uiMessage.getSendType());
                return intent;
            }
        } else {
            if (hasAttachment && uiMessage.getUiMessageAttachment().hasFinishedDownload()) {   // ak ma prilohu a priloha nie je prijata
                uiMessageDataSource.setUIMessageDeleted(uiMessage.getCreationTime(), uiMessage.getSendType());
                return intent;
            }
        }

        //not processed
        return null;
    }

    @Override
    protected void onPostExecute(Intent intent) {
        super.onPostExecute(intent);

        CptProcessor cptProcessor = appData.getCurrentActivity();

        if (intent == null || cptProcessor == null) return;

        try {
            if (cptProcessor instanceof CommunicationActivity ||
                    cptProcessor instanceof MessageDetailActivity)
                cptProcessor.process(intent);
        } catch (Exception e) {
            Log.e(TAG, "Fatal error: ", e);
        }

    }
}
