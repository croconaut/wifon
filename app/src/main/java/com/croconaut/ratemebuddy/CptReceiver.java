package com.croconaut.ratemebuddy;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

import com.croconaut.cpt.data.Communication;
import com.croconaut.cpt.data.IncomingMessage;
import com.croconaut.cpt.data.MessageAttachment;
import com.croconaut.cpt.data.NearbyUser;
import com.croconaut.cpt.network.NetworkHop;
import com.croconaut.cpt.ui.CptController;
import com.croconaut.cpt.ui.LinkLayerMode;
import com.croconaut.ratemebuddy.activities.CptSettingsActivity;
import com.croconaut.ratemebuddy.utils.pojo.profiles.MyProfile;
import com.croconaut.ratemebuddy.utils.tasks.AckedTask;
import com.croconaut.ratemebuddy.utils.tasks.AttachmentNotificationTask;
import com.croconaut.ratemebuddy.utils.tasks.AttachmentSentTask;
import com.croconaut.ratemebuddy.utils.tasks.AttachmentStateTask;
import com.croconaut.ratemebuddy.utils.tasks.MessageDeletedTask;
import com.croconaut.ratemebuddy.utils.tasks.MessageSentTask;
import com.croconaut.ratemebuddy.utils.tasks.NearbyTask;
import com.croconaut.ratemebuddy.utils.tasks.ReceivedAttachTask;
import com.croconaut.ratemebuddy.utils.tasks.ReceivedMessageTask;
import com.croconaut.ratemebuddy.utils.tasks.RequestExpiredTask;

import java.util.ArrayList;
import java.util.Date;

public class CptReceiver extends com.croconaut.cpt.ui.CptReceiver {
    public static final int STATUS_PERSISTENT_ID = 1;
    public static final int PROFILE_PERSISTENT_ID = 2;

    private static final String TAG = CptReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(CptReceiver.class.getName(), "inside with " + intent.toString());

        AppData appData = (AppData) context.getApplicationContext();

        if(MyProfile.getInstance(context) == null){
            Log.e(TAG, "Received intent while my profile is null, returning!");
            return;
        }

        switch (intent.getAction()) {
            case Communication.ACTION_OPEN_CPT_SETTINGS: {
                Intent closeIntent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                context.sendBroadcast(closeIntent);
                context.startActivity(new Intent(context, CptSettingsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
            }

            case Communication.ACTION_REQUEST_CPT_MODE: {
                int mode = intent.getIntExtra(Communication.EXTRA_REQUEST_CPT_MODE, LinkLayerMode.OFF);
                if (mode == LinkLayerMode.FOREGROUND) {
                    PreferenceManager.getDefaultSharedPreferences(context).edit().putString(CptSettingsActivity.PREFS_MODE, "0").apply();
                } else if (mode == LinkLayerMode.BACKGROUND) {
                    PreferenceManager.getDefaultSharedPreferences(context).edit().putString(CptSettingsActivity.PREFS_MODE, "1").apply();
                } else {
                    PreferenceManager.getDefaultSharedPreferences(context).edit().putString(CptSettingsActivity.PREFS_MODE, "2").apply();
                }

                CptController cptController = new CptController(context);
                cptController.setMode(mode);
                break;
            }

            case Communication.ACTION_MESSAGE_SENT: {
                MessageSentTask messageSentTask = new MessageSentTask(appData.getUiMessageDataSource(), appData.getCurrentActivity());
                messageSentTask.execute(intent);
                break;
            }

            case Communication.ACTION_MESSAGE_ACKED: {
                AckedTask ackedTask = new AckedTask(appData, appData.getCurrentActivity());
                ackedTask.execute(intent);
                break;
            }

            case Communication.ACTION_MESSAGE_ARRIVED: {
                ReceivedMessageTask receivedMessageTask = new ReceivedMessageTask(appData);
                receivedMessageTask.execute(intent);
                break;
            }

            case Communication.ACTION_NEARBY_ARRIVED: {
                NearbyTask nearbyTask = new NearbyTask(appData);
                nearbyTask.execute(intent);
                break;
            }

            case Communication.ACTION_MESSAGE_DELETED: {
                MessageDeletedTask messageDeletedTask = new MessageDeletedTask(appData);
                messageDeletedTask.execute(intent);
                break;
            }

            case Communication.ACTION_MESSAGE_ATTACHMENT_UPLOADING_TO_RECIPIENT:
            case Communication.ACTION_MESSAGE_ATTACHMENT_UPLOADING_TO_APP_SERVER:
            case Communication.ACTION_MESSAGE_ATTACHMENT_UPLOAD_CONFIRMED:
            case Communication.ACTION_MESSAGE_ATTACHMENT_UPLOAD_CANCELLED:
            case Communication.ACTION_MESSAGE_ATTACHMENT_DOWNLOAD_CONFIRMED:
            case Communication.ACTION_MESSAGE_ATTACHMENT_DOWNLOAD_CANCELLED: {
                long messageId = intent.getLongExtra(Communication.EXTRA_MESSAGE_ID, -1);
                Log.e(TAG, intent.getAction() + ", " + messageId);
                AttachmentStateTask attachmentStateTask = new AttachmentStateTask(appData);
                attachmentStateTask.execute(intent);
                break;
            }

            case Communication.ACTION_MESSAGE_ATTACHMENT_DOWNLOADING:
            case Communication.ACTION_MESSAGE_ATTACHMENT_DOWNLOADED: {
                ReceivedAttachTask receivedAttachTask = new ReceivedAttachTask(appData);
                receivedAttachTask.execute(intent);
                break;
            }

            case Communication.ACTION_MESSAGE_ATTACHMENT_UPLOADED_TO_APP_SERVER:
            case Communication.ACTION_MESSAGE_ATTACHMENT_DELIVERED:
            case Communication.ACTION_MESSAGE_ATTACHMENT_UPLOADED_TO_RECIPIENT: {
                AttachmentSentTask attachmentSentTask = new AttachmentSentTask(appData);
                attachmentSentTask.execute(intent);
                break;
            }

            case Communication.ACTION_MESSAGE_ATTACHMENT_NOTIFICATION:
                AttachmentNotificationTask notificationTask = new AttachmentNotificationTask(appData);
                notificationTask.execute(intent);
                break;

            case Communication.ACTION_MESSAGE_ATTACHMENT_REQUEST_EXPIRED:
                RequestExpiredTask requestExpiredTask = new RequestExpiredTask(appData);
                requestExpiredTask.execute(intent);
                break;

            case Communication.ACTION_SEND_CPT_LOGS:
                onSendLogs(context);
                break;

            default:
                Log.e(TAG, "Received unknown intent!");
        }
    }

    // this is very sad but we don't have time/energy to switch
    @Override
    protected void onMessageAttachmentDownloadConfirmed(Context context, long messageId, String sourceUri, String storageDirectory, String from) {
    }
    @Override
    protected void onMessageAttachmentDownloadCancelled(Context context, long messageId, String sourceUri, String storageDirectory, String from) {
    }
    @Override
    protected void onMessageAttachmentDownloading(Context context, long messageId, String sourceUri, String storageDirectory, String from, MessageAttachment messageAttachment) {
    }
    @Override
    protected void onMessageAttachmentDownloaded(Context context, long messageId, String sourceUri, String storageDirectory, String from, MessageAttachment messageAttachment, Date downloadedTime, int downloadedBytesPerSecond) {
    }
    @Override
    protected void onMessageAttachmentDownloadExpired(Context context, long messageId, String sourceUri, String storageDirectory, String from) {
    }
    @Override
    protected void onMessageAttachmentUploadConfirmed(Context context, long messageId, String sourceUri, String storageDirectory, String to) {
    }
    @Override
    protected void onMessageAttachmentUploadCancelled(Context context, long messageId, String sourceUri, String storageDirectory, String to) {
    }
    @Override
    protected void onMessageAttachmentUploadingToRecipient(Context context, long messageId, String sourceUri, String storageDirectory, String to) {
    }
    @Override
    protected void onMessageAttachmentUploadingToAppServer(Context context, long messageId, String sourceUri, String storageDirectory, String to) {
    }
    @Override
    protected void onMessageAttachmentUploadedToRecipient(Context context, long messageId, String sourceUri, String storageDirectory, String to, Date uploadedTime, int uploadedBytesPerSecond) {
    }
    @Override
    protected void onMessageAttachmentUploadedToAppServer(Context context, long messageId, String sourceUri, String storageDirectory, String to, Date uploadedTime, int uploadedBytesPerSecond) {
    }
    @Override
    protected void onMessageAttachmentDelivered(Context context, long messageId, String sourceUri, String storageDirectory, String to, Date deliveredTime) {
    }
    @Override
    protected void onNewMessage(Context context, long messageId, Date receivedTime, IncomingMessage incomingMessage) {
    }
    @Override
    protected void onMessageSentToRecipient(Context context, long messageId, Date sentTime) {
    }
    @Override
    protected void onMessageSentToAppServer(Context context, long messageId, Date sentTime) {
    }
    @Override
    protected void onMessageSentToOtherDevice(Context context, long messageId, Date sentTime) {
    }
    @Override
    protected void onMessageAcked(Context context, long messageId, Date deliveredTime, ArrayList<NetworkHop> hops) {
    }
    @Override
    protected void onMessageDeleted(Context context, long messageId) {
    }
    @Override
    protected void onNearbyPeers(Context context, ArrayList<NearbyUser> nearbyUsers) {
    }
    @Override
    protected void onCptNotificationTapped(Context context) {
    }
    @Override
    protected void onDownloadNotificationTapped(Context context, long messageId, String sourceUri, String storageDirectory, String from) {
    }
    @Override
    protected void onUploadNotificationTapped(Context context, long messageId, String sourceUri, String storageDirectory, String to) {
    }
}
