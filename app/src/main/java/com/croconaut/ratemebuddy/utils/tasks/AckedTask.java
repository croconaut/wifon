package com.croconaut.ratemebuddy.utils.tasks;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.croconaut.cpt.data.Communication;
import com.croconaut.cpt.network.NetworkHop;
import com.croconaut.ratemebuddy.activities.CptProcessor;
import com.croconaut.ratemebuddy.data.UIMessageDataSource;
import com.croconaut.ratemebuddy.utils.CommonUtils;
import com.croconaut.ratemebuddy.utils.pojo.UIMessage;
import com.croconaut.ratemebuddy.utils.pojo.UIMessageAttachment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class AckedTask extends AsyncTask<Intent, Void, Intent> {
    private static final String TAG = AckedTask.class.getName();

    private final UIMessageDataSource uiMessageDB;
    private final CptProcessor cptProcessor;

    public AckedTask(UIMessageDataSource uiMessageDB, CptProcessor cptProcessor) {
        this.uiMessageDB = uiMessageDB;
        this.cptProcessor = cptProcessor;
    }

    @Override
    protected Intent doInBackground(Intent... params) {
        final Intent intent = params[0];

        final long id = intent.getLongExtra(Communication.EXTRA_MESSAGE_ID, -1);
        final Date date = (Date) intent.getSerializableExtra(Communication.EXTRA_MESSAGE_TIME);
        final ArrayList<NetworkHop> hops = intent.getParcelableArrayListExtra(Communication.EXTRA_MESSAGE_ACKED);

        Log.e(TAG, "Message " + id + " acked");
        UIMessage uiMessage = uiMessageDB.getUIMessage(id);

        if (uiMessage == null) {
            Log.e(TAG, "Message " + id + " is not in DB ...");
            return null;
        }

        String hopsStr = null;
        try {
            hopsStr = createHops(hops);
        } catch (Exception e) {
            Log.e(TAG, "Hops cannot be parsed!", e);
        }

        if (uiMessage.hasAttachment() && uiMessage.getSendType() == UIMessage.ATTACHMENT_DELIVERED) {
            Log.e(TAG, "Received ACKED for DELIVERED attachment UI message, returning with null");
            return null;
        }

        if (uiMessage.hasAttachment()) {
            final Date attachmentTime = (Date) intent.getSerializableExtra(Communication.EXTRA_MESSAGE_ATTACHMENT_TIME);

            UIMessageAttachment attachment = new UIMessageAttachment.Builder(uiMessage.getUiMessageAttachment())
                    .time(attachmentTime.getTime())
                    .build();

            uiMessage = new UIMessage.Builder(uiMessage).uiMessageAttachment(attachment)
                    .hops(hopsStr)
                    .build();

            uiMessageDB.updateUiMessageAttachment(uiMessage);
        }


        uiMessage = new UIMessage.Builder(uiMessage).seenTime(date.getTime()).hops(hopsStr).build();

        if (uiMessageDB.updateMessageRemoteTimeAndHops(uiMessage, date.getTime()) == 1)
            Log.w(TAG, "Remote time UPDATED");
        else
            Log.w(TAG, "Remote time NOT UPDATED");


        if (uiMessageDB.setUIMessageSeen(
                uiMessage.getCreationTime(),
                uiMessage.getSendType(),
                date.getTime()
        )) {
            Log.i(TAG, "Message " + id + " updated to ACKED");
        }


        return intent;
    }

    @Override
    protected void onPostExecute(Intent intent) {
        if (intent != null && cptProcessor != null)
            try {
                cptProcessor.process(intent);
            } catch (IOException | ClassNotFoundException e) {
                Log.e(TAG, "onPostExecute", e);
            }
    }

    private String createHops(ArrayList<NetworkHop> hops) throws Exception {
        JSONObject json = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        Log.i(TAG, "Processing hops size: " + hops.size());
        for (int i = 0; i < hops.size(); i++) {
            NetworkHop objHop = hops.get(i);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("latitude", objHop.latitude);
            jsonObject.put("longitude", objHop.longitude);
            jsonObject.put("locationTime", objHop.locationTime.getTime());
            jsonObject.put("androidOsVersion", objHop.androidOsVersion);
            jsonObject.put("receivedTime", objHop.receivedTime.getTime());
            if (objHop.userName == null) {
                jsonObject.put("name", "_Mobile" + i);
            } else {
                jsonObject.put("name", objHop.userName);
            }
            jsonArray.put(jsonObject);
        }
        json.put(CommonUtils.HOPS_JSON_ARRAY, jsonArray);

        return json.toString();
    }
}
