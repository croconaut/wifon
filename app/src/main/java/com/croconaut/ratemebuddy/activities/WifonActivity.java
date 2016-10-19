package com.croconaut.ratemebuddy.activities;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;

import com.croconaut.cpt.data.Communication;
import com.croconaut.cpt.data.LocalAttachment;
import com.croconaut.cpt.data.OutgoingMessage;
import com.croconaut.cpt.data.OutgoingPayload;
import com.croconaut.cpt.data.OutgoingPersistentBroadcastMessage;
import com.croconaut.cpt.ui.CptController;
import com.croconaut.cpt.ui.LinkLayerMode;
import com.croconaut.ratemebuddy.CptReceiver;
import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.data.pojo.Attachment;
import com.croconaut.ratemebuddy.data.pojo.Comment;
import com.croconaut.ratemebuddy.data.pojo.Status;
import com.croconaut.ratemebuddy.data.pojo.VoteUp;
import com.croconaut.ratemebuddy.utils.pojo.TimelineInfo;
import com.croconaut.ratemebuddy.utils.pojo.UIMessage;
import com.croconaut.ratemebuddy.utils.pojo.UIMessageAttachment;
import com.croconaut.ratemebuddy.utils.pojo.profiles.MyProfile;
import com.croconaut.ratemebuddy.utils.pojo.profiles.Profile;
import com.croconaut.ratemebuddy.utils.pojo.profiles.states.ActualState;

import java.io.IOException;

public abstract class WifonActivity extends ToolbarDrawerActivity {
    private static final String TAG = WifonActivity.class.getName();
    // global variable which gets destroyed on application's removal else stays in memory
    private static boolean mShouldCheckWifiState = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int themeId = Integer.parseInt(prefs.getString(
                SettingsActivity.COLOR_PREF, "1"));
        switch (themeId) {
            case 0:
                setTheme(R.style.AppTheme_Red);
                break;
            case 1:
                setTheme(R.style.AppTheme_Green);
                break;
            case 2:
                setTheme(R.style.AppTheme_Blue);
                break;
            case 3:
                setTheme(R.style.AppTheme_Orange);
                break;
            case 4:
                setTheme(R.style.AppTheme_Pink);
                break;
        }

        //set color of the status bar
        if (Build.VERSION.SDK_INT >= 21) {
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = mContext.getTheme();
            theme.resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
            getWindow().setStatusBarColor(typedValue.data);
        }

        setContentView(R.layout.activity_main);

        if (MyProfile.getInstance(mContext) != null)
            Communication.register(this, CptReceiver.class);
    }


    protected Comment sendComment(Comment comment, Profile targetProfile) {
        try {
            OutgoingMessage serviceMsg = new OutgoingMessage(targetProfile.getCrocoId(), new OutgoingPayload(comment))
                    .setIsExpectingSent(false)
                    .setIsExpectingAck(false);

            comment = new Comment.Builder(comment)
                    .timestamp(serviceMsg.getCreationDate().getTime())
                    .build();

            Communication.newMessage(this, serviceMsg);

            ActualState actualState = new ActualState.Builder(targetProfile.getActualState())
                    .addComment(comment).build();
            targetProfile.getStatus().getComments().add(comment);

            targetProfile = new Profile.Builder(targetProfile).addActualState(actualState).build();
            appData.getProfileDataSource().updateProfile(targetProfile);

            Status status = targetProfile.getStatus();

            TimelineInfo timelineInfo = new TimelineInfo.Builder(serviceMsg.getId(), status.getContent(),
                    targetProfile.getCrocoId(), targetProfile.getName(), TimelineInfo.OUTGOING, TimelineInfo.MESSAGE_TYPE_COMMENT, false)
                    .statusId(comment.getStatusId()).build();
            appData.getTimelineDataSource().insertTimelineInfo(timelineInfo);

        } catch (IOException e) {
            Log.e(TAG, "sendComment", e);
        }

        return comment;
    }

    protected void checkForUnknown(Profile profile) {
        if (!(profile.getType() == Profile.FAVOURITE) && !(profile.getType() == Profile.UNKNOWN)) {
            profile.setType(Profile.UNKNOWN);
            updateOrInsertProfile(profile);
        }
    }

    protected void updateOrInsertProfile(Profile profile) {
        if (appData.getProfileDataSource().isProfileInDB(profile.getCrocoId()))
            appData.getProfileDataSource().updateProfile(profile);
        else appData.getProfileDataSource().insertProfile(profile);
    }

    protected long sendStatus(Status status) {
        Log.e(TAG, "Sending status");
        try {
            OutgoingMessage serviceMsg = new OutgoingPersistentBroadcastMessage(new OutgoingPayload(status), CptReceiver.STATUS_PERSISTENT_ID)
                    .setIsExpectingSent(false);

            Communication.newMessage(this, serviceMsg);
            return serviceMsg.getCreationDate().getTime();
        } catch (IOException e) {
            Log.e(TAG, "sendStatus", e);
            return System.currentTimeMillis();
        }
    }

    protected UIMessage sendFile(String targetCrocoId, Uri fileUri,
                                 boolean isFileImage) throws IOException {
        LocalAttachment localAttachment = new LocalAttachment(this,
                fileUri,
                isFileImage ? Environment.DIRECTORY_PICTURES : Environment.DIRECTORY_DOWNLOADS);

        OutgoingMessage serviceMsg = new OutgoingMessage(
                targetCrocoId,
                new OutgoingPayload(new Attachment()).addAttachment(localAttachment));

        Profile profile = (Profile) profileUtils.findProfile(targetCrocoId);

        TimelineInfo timelineInfo = new TimelineInfo.Builder(serviceMsg.getCreationDate().getTime(), localAttachment.getName(this),
                profile.getCrocoId(), profile.getName(), TimelineInfo.OUTGOING, TimelineInfo.MESSAGE_TYPE_FILE, false)
                .fileUri(fileUri.toString())
                .build();
        appData.getTimelineDataSource().insertTimelineInfo(timelineInfo);

        UIMessageAttachment messageAttachment = new UIMessageAttachment.Builder(localAttachment.getName(this))
                .state(UIMessageAttachment.STATE_UPLOAD_WAITING)
                .length(localAttachment.getLength(mContext))
                .type(localAttachment.getType(mContext))
                .storageType(isFileImage ? UIMessageAttachment.PUBLIC_IMAGE : UIMessageAttachment.PUBLIC_OTHER)
                .uri(fileUri.toString())
                .build();

        UIMessage uiMessage = new UIMessage.Builder(targetCrocoId,
                localAttachment.getName(this),
                serviceMsg.getCreationDate().getTime(),
                UIMessage.WAITING)
                .uiMessageAttachment(messageAttachment)
                .uiAttachmentId(serviceMsg.getId())
                .build();
        Log.e(TAG, "Sending file with id inside uiMessage: " + uiMessage.getUiAttachmentId());
        appData.getUiMessageDataSource().insertMessage(uiMessage);

        Log.e(TAG, "Sending file with id from serviceMsg: " + serviceMsg.getId());
        Communication.newMessage(this, serviceMsg);
        return uiMessage;
    }

    // send voteUp to targetCrocoId
    protected void sendVoteUp(Profile targetProfile) {
        try {
            MyProfile myProfile = MyProfile.getInstance(this);
            VoteUp voteUp = new VoteUp.Builder(myProfile.getName(), targetProfile.getStatus().getStatusID(), myProfile.getProfileId())
                    .build();


            OutgoingMessage serviceMsg = new OutgoingMessage(targetProfile.getCrocoId(), new OutgoingPayload(voteUp))
                    .setIsExpectingSent(false)
                    .setIsExpectingAck(false);
            Communication.newMessage(this, serviceMsg);

            TimelineInfo timelineInfo = new TimelineInfo.Builder(serviceMsg.getCreationDate().getTime(), targetProfile.getStatus().getContent(),
                    targetProfile.getCrocoId(), targetProfile.getName(), TimelineInfo.OUTGOING, TimelineInfo.MESSAGE_TYPE_VOTE, false)
                    .statusId(voteUp.getStatusId()).build();
            appData.getTimelineDataSource().insertTimelineInfo(timelineInfo);

            ActualState actualState = new ActualState.Builder(targetProfile.getActualState())
                    .setVote(voteUp).build();
            targetProfile = new Profile.Builder(targetProfile).addActualState(actualState).build();
            targetProfile.getStatus().getVotes().add(voteUp);

            appData.getProfileDataSource().updateProfile(targetProfile);
            Toast.makeText(this, mRes.getString(R.string.toast_profile_show_vote_send), Toast.LENGTH_LONG).show();

            checkForUnknown(targetProfile);
        } catch (IOException e) {
            Log.e(TAG, "sendVoteUp", e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(appData.getCurrentActivity() instanceof CreateProfileActivity)
            return;

        checkWifi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        theme.update();
        supportInvalidateOptionsMenu();

        checkWifi();

        if (MyProfile.getInstance(mContext) == null) return;
        // not very nice...
        CptController cptController = new CptController(this);
        if (PreferenceManager.getDefaultSharedPreferences(this).getString(CptSettingsActivity.PREFS_MODE, "0").equals("0")) {
            // foreground
            cptController.setMode(LinkLayerMode.FOREGROUND);
        } else if (PreferenceManager.getDefaultSharedPreferences(this).getString(CptSettingsActivity.PREFS_MODE, "0").equals("1")) {
            // background
            cptController.setMode(LinkLayerMode.BACKGROUND);
        } else {
            // off
            cptController.setMode(LinkLayerMode.OFF);
        }
    }

    private boolean mIsAlreadyCheckingWifi;

    private void checkWifi() {
        Log.v(TAG, "check wifi");
        if (mShouldCheckWifiState && !mIsAlreadyCheckingWifi
                && MyProfile.getInstance(mContext) != null) {
            mIsAlreadyCheckingWifi = true;
            final WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
            if (wm.getWifiState() != WifiManager.WIFI_STATE_ENABLING && wm.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
                DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                wm.setWifiEnabled(true);
                                mShouldCheckWifiState = true;
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                mShouldCheckWifiState = false;
                        }
                        mIsAlreadyCheckingWifi = false;
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder
                        .setIcon(R.drawable.ic_launcher)
                        .setTitle(mRes.getString(R.string.dialog_wifon_title))
                        .setMessage(mRes.getString(R.string.dialog_wifon_messege))
                        .setNegativeButton(mRes.getString(R.string.dialog_wifon_negative), dialogListener)
                        .setPositiveButton(mRes.getString(R.string.dialog_wifon_positive), dialogListener)
                        .show()
                ;
            } else {
                mIsAlreadyCheckingWifi = false;
                Log.d(TAG, "wifi is enabled => ok");
            }
        } else {
            Log.d(TAG, "wifi check skipped");
        }
    }

}


