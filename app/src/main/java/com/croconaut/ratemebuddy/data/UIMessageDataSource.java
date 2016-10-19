package com.croconaut.ratemebuddy.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.croconaut.cpt.data.DownloadedAttachment;
import com.croconaut.cpt.data.DownloadedAttachmentPreview;
import com.croconaut.ratemebuddy.AppData;
import com.croconaut.ratemebuddy.utils.CommonUtils;
import com.croconaut.ratemebuddy.utils.pojo.UIMessage;
import com.croconaut.ratemebuddy.utils.pojo.UIMessageAttachment;

import java.util.ArrayList;
import java.util.List;

public class UIMessageDataSource {

    private static final String TAG = UIMessageDataSource.class.getName();

    private UIMessageSQLiteHelper helper;
    private SQLiteDatabase db;
    private String table;

    public UIMessageDataSource(Context context) {
        helper = new UIMessageSQLiteHelper(context);
        table = UIMessageSQLiteHelper.MESSAGES_TABLE;
    }

    public void open() {
        db = helper.getWritableDatabase();
    }

    public void close() {
        helper.close();
    }

    private List<UIMessage> getMessages(Cursor cursor) {
        ArrayList<UIMessage> messages = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            int idCol = cursor.getColumnIndex(UIMessageSQLiteHelper.COLUMN_ID);
            int crocoIdCol = cursor.getColumnIndex(UIMessageSQLiteHelper.COLUMN_CROCO_ID);
            int sendTypeCol = cursor.getColumnIndex(UIMessageSQLiteHelper.COLUMN_SEND_TYPE);
            int contentCol = cursor.getColumnIndex(UIMessageSQLiteHelper.COLUMN_CONTENT);
            int hopsCol = cursor.getColumnIndex(UIMessageSQLiteHelper.COLUMN_HOPS);
            int attachmentCol = cursor.getColumnIndex(UIMessageSQLiteHelper.COLUMN_ATTACHMENT);
            int attachmentIdCol = cursor.getColumnIndex(UIMessageSQLiteHelper.COLUMN_ATTACHMENT_ID);
            int creationTimeCol = cursor.getColumnIndex(UIMessageSQLiteHelper.COLUMN_CREATION_TIME);
            int receivedTimeCol = cursor.getColumnIndex(UIMessageSQLiteHelper.COLUMN_RECEIVED_TIME);
            int firstSentTimeCol = cursor.getColumnIndex(UIMessageSQLiteHelper.COLUMN_FIRST_SENT_TIME);
            int lastSentTimeCol = cursor.getColumnIndex(UIMessageSQLiteHelper.COLUMN_LAST_SENT_TIME);
            int seenTimeCol = cursor.getColumnIndex(UIMessageSQLiteHelper.COLUMN_SEEN_TIME);
            int sentToInternetCol = cursor.getColumnIndex(UIMessageSQLiteHelper.COLUMN_SENT_TO_INTERNET_TIME);
            int sentToRecipientrCol = cursor.getColumnIndex(UIMessageSQLiteHelper.COLUMN_SENT_TO_RECIPIENT_TIME);
            int sentToOtherDeviceCol = cursor.getColumnIndex(UIMessageSQLiteHelper.COLUMN_SENT_TO_OTHER_DEVICE_TIME);

            do {
                int id = cursor.getInt(idCol);
                String crocoId = cursor.getString(crocoIdCol);
                Object obj = CommonUtils.deserializeObject(cursor.getBlob(attachmentCol));
                UIMessageAttachment attachment = obj == null ? null : (UIMessageAttachment) obj;
                long createTime = cursor.getLong(creationTimeCol);
                long receivedTime = cursor.getLong(receivedTimeCol);
                long firstSentTime = cursor.getLong(firstSentTimeCol);
                long lastSentTime = cursor.getLong(lastSentTimeCol);
                long seenTime = cursor.getLong(seenTimeCol);
                long sentToInternet = cursor.getLong(sentToInternetCol);
                long sentToRecipient = cursor.getLong(sentToRecipientrCol);
                long sentToOtherDevice = cursor.getLong(sentToOtherDeviceCol);
                long attachmentId = cursor.getLong(attachmentIdCol);
                int sendType = cursor.getInt(sendTypeCol);
                String content = cursor.getString(contentCol);
                String hops = cursor.getString(hopsCol);

                UIMessage uiMessage = new UIMessage.Builder(crocoId, content, createTime, sendType)
                        .id(id)
                        .uiMessageAttachment(attachment)
                        .uiAttachmentId(attachmentId)
                        .firstSentTime(firstSentTime)
                        .lastSentTime(lastSentTime)
                        .seenTime(seenTime)
                        .sentToInternetTime(sentToInternet)
                        .sentToOtherDeviceTime(sentToOtherDevice)
                        .sentToRecipientTime(sentToRecipient)
                        .receivedTime(receivedTime)
                        .hops(hops).build();

                messages.add(uiMessage);

            } while (cursor.moveToNext());
        }
        return messages;
    }

    public boolean isMessageInDB(long remoteTime, String crocoIdFrom) {
        StringBuilder w = new StringBuilder();
        w.append(UIMessageSQLiteHelper.COLUMN_RECEIVED_TIME + " =? ");
        w.append(" AND ");
        w.append(UIMessageSQLiteHelper.COLUMN_CROCO_ID + " =? ");
        String selection = new String(w);
        String[] selectionArgs = new String[]{String.valueOf(remoteTime), crocoIdFrom};
        Cursor cur = db.query(table, null, selection, selectionArgs, null, null, UIMessageSQLiteHelper.COLUMN_CREATION_TIME);

        try {
            List<UIMessage> list = getMessages(cur);
            return list.size() > 0;
        } finally {
            if (cur != null && !cur.isClosed()) cur.close();
        }
    }

    public UIMessage getMessageById(int id) {
        Cursor cur = db.query(table, null, UIMessageSQLiteHelper.COLUMN_ID + " = ?", new String[]{id + ""}, null, null, null);
        try {
            List<UIMessage> message = getMessages(cur);
            return (message.isEmpty()) ? null : message.get(0);
        } finally {
            if (cur != null && !cur.isClosed()) cur.close();
        }
    }

    public List<UIMessage> getMessagesByUserId(String crocoID) {
        String selection = UIMessageSQLiteHelper.COLUMN_CROCO_ID + " = ?";
        String[] selectionArgs = {crocoID};
        Cursor cur = db.query(table, null, selection, selectionArgs, null, null, UIMessageSQLiteHelper.COLUMN_CREATION_TIME);
        try {
            return getMessages(cur);
        } finally {
            if (cur != null && !cur.isClosed()) cur.close();
        }
    }

    public long insertMessage(UIMessage message) {
        ContentValues vals = new ContentValues();
        vals.put(UIMessageSQLiteHelper.COLUMN_CROCO_ID, message.getCrocoId());
        vals.put(UIMessageSQLiteHelper.COLUMN_CREATION_TIME, message.getCreationTime());
        vals.put(UIMessageSQLiteHelper.COLUMN_RECEIVED_TIME, message.getReceivedTime());
        vals.put(UIMessageSQLiteHelper.COLUMN_FIRST_SENT_TIME, message.getFirstSentTime());
        vals.put(UIMessageSQLiteHelper.COLUMN_LAST_SENT_TIME, message.getLastSentTime());
        vals.put(UIMessageSQLiteHelper.COLUMN_SEEN_TIME, message.getSeenTime());
        vals.put(UIMessageSQLiteHelper.COLUMN_SENT_TO_INTERNET_TIME, message.getSentToInternetTime());
        vals.put(UIMessageSQLiteHelper.COLUMN_SENT_TO_OTHER_DEVICE_TIME, message.getSentToOtherDeviceTime());
        vals.put(UIMessageSQLiteHelper.COLUMN_SENT_TO_RECIPIENT_TIME, message.getSentToRecipientTime());
        vals.put(UIMessageSQLiteHelper.COLUMN_SEND_TYPE, message.getSendType());
        vals.put(UIMessageSQLiteHelper.COLUMN_CONTENT, message.getContent());
        vals.put(UIMessageSQLiteHelper.COLUMN_HOPS, message.getHops());
        vals.put(UIMessageSQLiteHelper.COLUMN_ATTACHMENT_ID, message.getUiAttachmentId());
        vals.put(UIMessageSQLiteHelper.COLUMN_ATTACHMENT, message.getUiMessageAttachment() != null ?
                CommonUtils.serializeObject(message.getUiMessageAttachment()) : null);
        return db.insert(table, null, vals);
    }

    public UIMessage getUIMessage(long messageId) {
        StringBuilder w = new StringBuilder();
        w.append(UIMessageSQLiteHelper.COLUMN_RECEIVED_TIME + " = ? ");
        w.append(" OR ");
        w.append(UIMessageSQLiteHelper.COLUMN_ATTACHMENT_ID + " = ?");
        String selection = new String(w);
        String[] selectionArgs = new String[]{
                String.valueOf(messageId), String.valueOf(messageId)};
        Cursor cur = db.query(table, null, selection, selectionArgs, null,
                null, null);
        try {
            List<UIMessage> message = getMessages(cur);
            return (message.isEmpty()) ? null : message.get(0);
        } finally {
            if (cur != null && !cur.isClosed()) cur.close();
        }
    }

    public UIMessage getUIMessageByRemoteOrAttachmentId(long remoteTime) {
        StringBuilder w = new StringBuilder();
        w.append(UIMessageSQLiteHelper.COLUMN_RECEIVED_TIME + " = ? ");
        w.append(" OR ");
        w.append(UIMessageSQLiteHelper.COLUMN_ATTACHMENT_ID + " = ?");
        w.append(" OR ");
        w.append(UIMessageSQLiteHelper.COLUMN_CREATION_TIME + " = ?");
        String selection = new String(w);
        String[] selectionArgs = new String[]{
                String.valueOf(remoteTime), String.valueOf(remoteTime), String.valueOf(remoteTime)};
        Cursor cur = db.query(table, null, selection, selectionArgs, null,
                null, null);
        try {
            List<UIMessage> message = getMessages(cur);
            return (message.isEmpty()) ? null : message.get(0);
        } finally {
            if (cur != null && !cur.isClosed()) cur.close();
        }
    }

    public void updateAttachmentSent(long attachmentId, int sendType, long attachmentTime, int speed, int attachmentState) {
        String selection = UIMessageSQLiteHelper.COLUMN_ATTACHMENT_ID + " == ?";
        String[] selectionArgs = new String[]{String.valueOf(attachmentId)};

        Cursor cur = null;
        try {
            cur = db.query(table, null, selection, selectionArgs, null, null, null);
            for (UIMessage uiMessage : getMessages(cur)) {
                UIMessageAttachment uiMessageAttachment =
                        new UIMessageAttachment.Builder(uiMessage.getUiMessageAttachment())
                                .time(attachmentTime)
                                .state(attachmentState == -1 ? uiMessage.getUiMessageAttachment().getState() : attachmentState)
                                .speed(speed)
                                .build();

                uiMessage = new UIMessage.Builder(uiMessage)
                        .uiMessageAttachment(uiMessageAttachment)
                        .build();

                updateUiMessageAttachment(uiMessage);
                setMessageType(uiMessage.getCreationTime(), uiMessage.getSendType(), sendType);
            }
        } finally {
            if (cur != null && !cur.isClosed()) cur.close();
        }
    }

    public void updateAttachmentState(long messageId, int state, String sourceUri) {
        StringBuilder w = new StringBuilder();
        w.append(UIMessageSQLiteHelper.COLUMN_ATTACHMENT_ID + " == ?");
        String selection = new String(w);
        String[] selectionArgs = new String[]{String.valueOf(messageId)};
        Cursor cur = db.query(table, null, selection, selectionArgs, null, null, null);

        for (UIMessage uiMessage : getMessages(cur)) {
            UIMessageAttachment uiMessageAttachment =
                    new UIMessageAttachment.Builder(uiMessage.getUiMessageAttachment())
                            .state(state)
                            .sourceUri(sourceUri)
                            .build();

            UIMessage newUiMessage = new UIMessage.Builder(uiMessage)
                    .uiMessageAttachment(uiMessageAttachment)
                    .build();

            updateUiMessageAttachment(newUiMessage);
        }
    }

    public void updateAttachmentState(long messageId, int state, String crocoId, String sourceUri) {
        StringBuilder w = new StringBuilder();
        w.append(UIMessageSQLiteHelper.COLUMN_CROCO_ID + " = ? ");
        w.append(" AND ");
        w.append(UIMessageSQLiteHelper.COLUMN_ATTACHMENT_ID + " == ?");
        String selection = new String(w);
        String[] selectionArgs = new String[]{crocoId, String.valueOf(messageId)};

        Cursor cur = null;
        try {
            cur = db.query(table, null, selection, selectionArgs, null, null, null);
            for (UIMessage uiMessage : getMessages(cur)) {
                UIMessageAttachment uiMessageAttachment =
                        new UIMessageAttachment.Builder(uiMessage.getUiMessageAttachment())
                                .state(state)
                                .sourceUri(sourceUri)
                                .build();

                UIMessage newUiMessage = new UIMessage.Builder(uiMessage)
                        .uiMessageAttachment(uiMessageAttachment)
                        .build();

                updateUiMessageAttachment(newUiMessage);
            }
        } finally {
            if (cur != null && !cur.isClosed()) cur.close();
        }
    }

    public void updateEndDownloadTime(long messageId){
        Log.e(TAG, "Updating download time END");
        StringBuilder w = new StringBuilder();
        w.append(UIMessageSQLiteHelper.COLUMN_ATTACHMENT_ID + " == ?");
        String selection = new String(w);
        String[] selectionArgs = new String[]{String.valueOf(messageId)};

        Cursor cur = null;
        try {
            cur = db.query(table, null, selection, selectionArgs, null, null, null);
            for (UIMessage uiMessage : getMessages(cur)) {
                if(uiMessage.getUiMessageAttachment().getDownloadEnded() != 0) {
                    UIMessageAttachment uiMessageAttachment =
                            new UIMessageAttachment.Builder(uiMessage.getUiMessageAttachment())
                                    .downloadEnded(System.currentTimeMillis())
                                    .build();

                    UIMessage newUiMessage = new UIMessage.Builder(uiMessage)
                            .uiMessageAttachment(uiMessageAttachment)
                            .build();

                    updateUiMessageAttachment(newUiMessage);
                }
            }
        } finally {
            if (cur != null && !cur.isClosed()) cur.close();
        }
    }

    public void updateStartDownloadTime(long messageId){
        Log.e(TAG, "Updating download time START");
        StringBuilder w = new StringBuilder();
        w.append(UIMessageSQLiteHelper.COLUMN_ATTACHMENT_ID + " == ?");
        String selection = new String(w);
        String[] selectionArgs = new String[]{String.valueOf(messageId)};

        Cursor cur = null;
        try {
            cur = db.query(table, null, selection, selectionArgs, null, null, null);
            for (UIMessage uiMessage : getMessages(cur)) {
                if(uiMessage.getUiMessageAttachment().getDownloadStarted() != 0) {
                    UIMessageAttachment uiMessageAttachment =
                            new UIMessageAttachment.Builder(uiMessage.getUiMessageAttachment())
                                    .downloadStarted(System.currentTimeMillis())
                                    .build();

                    UIMessage newUiMessage = new UIMessage.Builder(uiMessage)
                            .uiMessageAttachment(uiMessageAttachment)
                            .build();

                    updateUiMessageAttachment(newUiMessage);
                }
            }
        } finally {
            if (cur != null && !cur.isClosed()) cur.close();
        }
    }

    public void updateAttachmentState(long messageId, int state, String crocoId,
                                      DownloadedAttachmentPreview preview, AppData appData) {
        StringBuilder w = new StringBuilder();
        w.append(UIMessageSQLiteHelper.COLUMN_CROCO_ID + " =? ");
        w.append(" AND ");
        w.append(UIMessageSQLiteHelper.COLUMN_ATTACHMENT_ID + " == ?");
        String selection = new String(w);
        String[] selectionArgs = new String[]{crocoId, String.valueOf(messageId)};

        Cursor cur = null;
        try {
            cur = db.query(table, null, selection, selectionArgs, null, null, null);
            for (UIMessage uiMessage : getMessages(cur)) {
                updateAttachment(state, uiMessage, preview, appData);
            }
        } finally {
            if (cur != null && !cur.isClosed()) cur.close();
        }
    }

    private void updateAttachment(int state, UIMessage uiMessage,
                                  DownloadedAttachmentPreview preview, AppData appData) {
        // TODO: just a quick, one time, hack
        int storageType;
        if (preview.getStorageDirectory() == null) {
            storageType = 0;
        } else if (preview.getStorageDirectory().equals(Environment.DIRECTORY_PICTURES)) {
            storageType = 1;
        } else if (preview.getStorageDirectory().equals(Environment.DIRECTORY_DOWNLOADS)) {
            storageType = 2;
        } else {
            Log.e(TAG, "Unknown storage directory: " + preview.getStorageDirectory());
            storageType = 2;    // downloads
        }

        UIMessageAttachment uiMessageAttachment =
                new UIMessageAttachment.Builder(uiMessage.getUiMessageAttachment())
                        .state(state)
                        .type(preview.getType(appData))
                        .lastModified(preview.getLastModified(appData))
                        .length(preview.getLength(appData))
                        .sourceUri(preview.getSourceUri())
                        .downloadStarted(System.currentTimeMillis())
                        .storageType(storageType)
                        .build();

        UIMessage newUiMessage = new UIMessage.Builder(uiMessage)
                .uiMessageAttachment(uiMessageAttachment)
                .build();

        updateUiMessageAttachment(newUiMessage);
    }

    public void updateAttachmentStateWithUri
            (long messageId, int state, DownloadedAttachment downloadedAttachment,
             String crocoId, AppData appData, long attachmentTime, int speed) {
        StringBuilder w = new StringBuilder();
        w.append(UIMessageSQLiteHelper.COLUMN_CROCO_ID + " =? ");
        w.append(" AND ");
        w.append(UIMessageSQLiteHelper.COLUMN_ATTACHMENT_ID + " == ?");
        String selection = new String(w);
        String[] selectionArgs = new String[]{crocoId, String.valueOf(messageId)};

        Cursor cur = null;
        try {
            cur = db.query(table, null, selection, selectionArgs, null, null, null);
            for (UIMessage uiMessage : getMessages(cur)) {
                UIMessageAttachment newUiMessageAttachment = new UIMessageAttachment.Builder(
                        uiMessage.getUiMessageAttachment())
                        .state(state)
                        .type(downloadedAttachment.getType(appData))
                        .time(attachmentTime)
                        .lastModified(downloadedAttachment.getLastModified(appData))
                        .length(downloadedAttachment.getLength(appData))
                        .sourceUri(downloadedAttachment.getSourceUri())
                        .uri(downloadedAttachment.getUri().toString())
                        .downloadEnded(System.currentTimeMillis())
                        .speed(speed)
                        .build();
                UIMessage newUiMessage = new UIMessage.Builder(uiMessage)
                        .uiMessageAttachment(newUiMessageAttachment)
                        .build();
                updateUiMessageAttachment(newUiMessage);
            }
        } finally {
            if (cur != null && !cur.isClosed()) cur.close();
        }
    }

    public ArrayList<UIMessage> getCurrentAttachments(String crocoId) {
        ArrayList<UIMessage> uiMessagesWithAttachmentNotSend = new ArrayList<>();

        StringBuilder w = new StringBuilder();
        w.append(UIMessageSQLiteHelper.COLUMN_CROCO_ID + " =? ");
        w.append(" AND ");
        w.append(UIMessageSQLiteHelper.COLUMN_ATTACHMENT + " IS NOT NULL");
        String selection = new String(w);
        String[] selectionArgs = new String[]{crocoId};

        Cursor cur = null;
        try {
            cur = db.query(table, null, selection, selectionArgs, null, null, null);
            for (UIMessage uiMessage : getMessages(cur)) {
                if (uiMessage.getSendType() == UIMessage.INCOMING &&                //is outgoing?
                        !uiMessage.getUiMessageAttachment().hasFinishedDownload())  //is finished?
                    uiMessagesWithAttachmentNotSend.add(uiMessage);
            }
        } finally {
            if (cur != null && !cur.isClosed()) cur.close();
        }

        return uiMessagesWithAttachmentNotSend;
    }

    public void updateUiMessageAttachment(UIMessage uiMessage) {
        ContentValues vals = new ContentValues();
        vals.put(UIMessageSQLiteHelper.COLUMN_ATTACHMENT,
                CommonUtils.serializeObject(uiMessage.getUiMessageAttachment()));
        db.update(table, vals, UIMessageSQLiteHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(uiMessage.getId())});
    }


    public int updateMessageRemoteTimeAndHops(UIMessage message, long remoteTime) {
        ContentValues vals = new ContentValues();
        vals.put(UIMessageSQLiteHelper.COLUMN_RECEIVED_TIME, remoteTime);
        vals.put(UIMessageSQLiteHelper.COLUMN_HOPS, message.getHops());
        return db.update(table, vals, UIMessageSQLiteHelper.COLUMN_CREATION_TIME + " = ?", new String[]{String.valueOf(message.getCreationTime())});
    }

    public boolean setMessageType(long localTime, int from, int to) {
        ContentValues vals = new ContentValues();
        vals.put(UIMessageSQLiteHelper.COLUMN_SEND_TYPE, to);

        String where = UIMessageSQLiteHelper.COLUMN_CREATION_TIME + " = ? AND " + UIMessageSQLiteHelper.COLUMN_SEND_TYPE + " == ?";

        String[] whereArgs = {localTime + "", from + ""};
        return db.update(table, vals, where, whereArgs) > 0;
    }

    public boolean setUIMessageSent(long creationTime, int sendTypeFrom, int sendTypeTo, long sentTime,
                                    boolean isFirstTimeSent) {
        Log.e(getClass().getName(), "SendTypeTo: " + sendTypeTo);
        ContentValues vals = new ContentValues();
        vals.put(UIMessageSQLiteHelper.COLUMN_SEND_TYPE, sendTypeTo);

        switch (sendTypeTo) {
            case UIMessage.SENT_TO_INTERNET:
                vals.put(UIMessageSQLiteHelper.COLUMN_SENT_TO_INTERNET_TIME, sentTime);
                break;
            case UIMessage.SENT_TO_OTHER_DEVICE:
                vals.put(UIMessageSQLiteHelper.COLUMN_SENT_TO_OTHER_DEVICE_TIME, sentTime);
                break;
            case UIMessage.SENT_TO_RECIPIENT:
                Log.e(getClass().getName(), "updating recipient");
                vals.put(UIMessageSQLiteHelper.COLUMN_SENT_TO_RECIPIENT_TIME, sentTime);
                break;
        }

        vals.put(UIMessageSQLiteHelper.COLUMN_LAST_SENT_TIME, sentTime);
        if (isFirstTimeSent)
            vals.put(UIMessageSQLiteHelper.COLUMN_FIRST_SENT_TIME, sentTime);
        String where = UIMessageSQLiteHelper.COLUMN_CREATION_TIME + " = ? AND " + UIMessageSQLiteHelper.COLUMN_SEND_TYPE + " == ?";
        String[] whereArgs = {String.valueOf(creationTime), String.valueOf(sendTypeFrom)};
        return db.update(table, vals, where, whereArgs) > 0;
    }

    public boolean setUIMessageSeen(long creationTime, int sendTypeFrom, long seenTime) {
        ContentValues vals = new ContentValues();
        vals.put(UIMessageSQLiteHelper.COLUMN_SEND_TYPE, UIMessage.ACKED);
        vals.put(UIMessageSQLiteHelper.COLUMN_SEEN_TIME, seenTime);

        String where = UIMessageSQLiteHelper.COLUMN_CREATION_TIME + " = ? AND " + UIMessageSQLiteHelper.COLUMN_SEND_TYPE + " == ?";
        String[] whereArgs = {String.valueOf(creationTime), String.valueOf(sendTypeFrom)};
        return db.update(table, vals, where, whereArgs) > 0;
    }

    public boolean setUIMessageDeleted(long creationTime, int sendTypeFrom) {
        ContentValues vals = new ContentValues();
        vals.put(UIMessageSQLiteHelper.COLUMN_SEND_TYPE, UIMessage.DELETED);

        String where = UIMessageSQLiteHelper.COLUMN_CREATION_TIME + " = ? AND " + UIMessageSQLiteHelper.COLUMN_SEND_TYPE + " == ?";

        String[] whereArgs = {String.valueOf(creationTime), String.valueOf(sendTypeFrom)};
        return db.update(table, vals, where, whereArgs) > 0;
    }


    public int deleteMessageByID(int id) {
        return db.delete(table, UIMessageSQLiteHelper.COLUMN_ID + " = ?", new String[]{id + ""});
    }
}
