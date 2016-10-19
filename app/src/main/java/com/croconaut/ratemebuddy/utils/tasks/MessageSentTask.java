package com.croconaut.ratemebuddy.utils.tasks;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.croconaut.cpt.data.Communication;
import com.croconaut.ratemebuddy.activities.CptProcessor;
import com.croconaut.ratemebuddy.data.UIMessageDataSource;
import com.croconaut.ratemebuddy.utils.pojo.UIMessage;

import java.io.IOException;
import java.util.Date;

public class MessageSentTask extends AsyncTask<Intent, Void, Intent> {
    private static final String TAG = MessageSentTask.class.getName();

    private final UIMessageDataSource uiMessageDB;
    private final CptProcessor cptProcessor;

    public MessageSentTask(UIMessageDataSource uiMessageDB, CptProcessor cptProcessor) {
        this.uiMessageDB = uiMessageDB;
        this.cptProcessor = cptProcessor;
    }

    @Override
    protected Intent doInBackground(Intent... params) {
        Intent intent = params[0];

        final long id = intent.getLongExtra(Communication.EXTRA_MESSAGE_ID, -1);
        final Date date = (Date) intent.getSerializableExtra(Communication.EXTRA_MESSAGE_TIME);
        final int sentTo = intent.getIntExtra(Communication.EXTRA_MESSAGE_SENT, -1);

        Log.i(TAG, "Message " + id + " sent");

        Log.i(TAG, "sentTo: " + sentTo);
        UIMessage uiMessage = uiMessageDB.getUIMessage(id);    // creationTime is unique

        if (uiMessage == null) {
            Log.e(TAG, "Message " + id + " is not in DB ...");
            return null;
        }

        if (getMessageSendType(sentTo) > uiMessage.getSendType()) {
            uiMessageDB.setUIMessageSent(
                    uiMessage.getCreationTime(),
                    uiMessage.getSendType(),
                    getMessageSendType(sentTo),
                    date.getTime(),
                    uiMessage.getFirstSentTime() == 0
            );
            return intent;
        }else return null;
    }

    private int getMessageSendType(int sentTo) {
        switch (sentTo) {
            case Communication.MESSAGE_SENT_TO_INTERNET:
                return UIMessage.SENT_TO_INTERNET;
            case Communication.MESSAGE_SENT_TO_OTHER_DEVICE:
                return UIMessage.SENT_TO_OTHER_DEVICE;
            case Communication.MESSAGE_SENT_TO_RECIPIENT:
                return UIMessage.SENT_TO_RECIPIENT;
            default:
                return 0;
        }
    }

    @Override
    protected void onPostExecute(Intent intent) {
        super.onPostExecute(intent);
        if (intent != null && cptProcessor != null)
            try {
                cptProcessor.process(intent);
            } catch (IOException | ClassNotFoundException e) {
                Log.e(TAG, "onPostExecute", e);
            }
    }
}
