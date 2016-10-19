package com.croconaut.ratemebuddy.utils.tasks;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.croconaut.cpt.data.Communication;
import com.croconaut.ratemebuddy.AppData;
import com.croconaut.ratemebuddy.activities.CommunicationActivity;
import com.croconaut.ratemebuddy.activities.CptProcessor;
import com.croconaut.ratemebuddy.utils.pojo.UIMessage;

import java.io.IOException;

public class RequestExpiredTask extends AsyncTask<Intent, Void, Intent> {

    private final String TAG = ReceivedAttachTask.class.getName();

    private final AppData appData;

    public RequestExpiredTask(AppData appData) {
        this.appData = appData;
    }

    @Override
    protected Intent doInBackground(Intent... params) {
        Intent intent = params[0];

        long messageId = intent.getLongExtra(Communication.EXTRA_MESSAGE_ID, -1);
        UIMessage uiMessage = appData.getUiMessageDataSource().getUIMessage(messageId);

        if (uiMessage != null && uiMessage.hasAttachment()) {
            appData.getUiMessageDataSource().setUIMessageDeleted(
                    uiMessage.getCreationTime(),
                    uiMessage.getSendType()
            );
        }

        return intent;
    }

    @Override
    protected void onPostExecute(Intent intent) {
        super.onPostExecute(intent);

        CptProcessor cptProcessor = appData.getCurrentActivity();

        if (intent == null || cptProcessor == null) return;

        if (cptProcessor instanceof CommunicationActivity) {
            try {
                cptProcessor.process(intent);
            } catch (IOException | ClassNotFoundException e) {
                Log.e(TAG, "onPostExecute", e);
            }
        }
    }
}
