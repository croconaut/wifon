package com.croconaut.ratemebuddy.utils.tasks;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.croconaut.cpt.data.Communication;
import com.croconaut.cpt.data.IncomingMessage;
import com.croconaut.cpt.data.MessageAttachment;
import com.croconaut.cpt.data.OutgoingMessage;
import com.croconaut.cpt.data.OutgoingPayload;
import com.croconaut.cpt.data.OutgoingPersistentBroadcastMessage;
import com.croconaut.cpt.network.NetworkHop;
import com.croconaut.ratemebuddy.AppData;
import com.croconaut.ratemebuddy.CptReceiver;
import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.activities.CptProcessor;
import com.croconaut.ratemebuddy.activities.notifications.CommentNotification;
import com.croconaut.ratemebuddy.activities.notifications.MessageNotification;
import com.croconaut.ratemebuddy.activities.notifications.Notification;
import com.croconaut.ratemebuddy.activities.notifications.OldStatusNotif;
import com.croconaut.ratemebuddy.activities.notifications.VoteUpNotification;
import com.croconaut.ratemebuddy.data.pojo.Attachment;
import com.croconaut.ratemebuddy.data.pojo.Comment;
import com.croconaut.ratemebuddy.data.pojo.Message;
import com.croconaut.ratemebuddy.data.pojo.RMBProfile;
import com.croconaut.ratemebuddy.data.pojo.VoteUp;
import com.croconaut.ratemebuddy.utils.CommonUtils;
import com.croconaut.ratemebuddy.utils.ProfileUtils;
import com.croconaut.ratemebuddy.utils.XorString;
import com.croconaut.ratemebuddy.utils.pojo.TimelineInfo;
import com.croconaut.ratemebuddy.utils.pojo.UIMessage;
import com.croconaut.ratemebuddy.utils.pojo.UIMessageAttachment;
import com.croconaut.ratemebuddy.utils.pojo.profiles.IProfile;
import com.croconaut.ratemebuddy.utils.pojo.profiles.MyProfile;
import com.croconaut.ratemebuddy.utils.pojo.profiles.Profile;
import com.croconaut.ratemebuddy.utils.pojo.profiles.states.ActualState;
import com.croconaut.tictactoe.payload.TicTacToeGame;
import com.croconaut.tictactoe.payload.invites.Invite;
import com.croconaut.tictactoe.payload.invites.InviteRequest;
import com.croconaut.tictactoe.payload.invites.InviteResponse;
import com.croconaut.tictactoe.payload.moves.Move;
import com.croconaut.tictactoe.payload.moves.Surrender;
import com.croconaut.tictactoe.ui.notifications.GameNotificationManager;
import com.croconaut.tictactoe.ui.notifications.InviteNotificationManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReceivedMessageTask extends AsyncTask<Intent, Void, PostExecuteValue> {
    private static final String TAG = ReceivedMessageTask.class.getName();

    private final CptProcessor cptProcessor;
    private final AppData appData;
    private final ProfileUtils profileUtils;

    public ReceivedMessageTask(AppData appData) {
        this.appData = appData;
        this.profileUtils = new ProfileUtils(appData);
        cptProcessor = appData.getCurrentActivity();
    }

    @Override
    protected PostExecuteValue doInBackground(Intent... params) {
        try {
            Intent intent = params[0];
            final IncomingMessage message = intent.getParcelableExtra(Communication.EXTRA_MESSAGE_ARRIVED);
            final Date date = (Date) intent.getSerializableExtra(Communication.EXTRA_MESSAGE_TIME);

            boolean valid = false;
            Serializable data = message.getPayload().getAppData();
            if (data instanceof RMBProfile) {
                RMBProfile rmbProfile = (RMBProfile) data;
                Log.e(TAG, "Received RMB profile, friends size: " + rmbProfile.getFriendProfiles().size());

                Profile profile = new Profile.Builder(message.getFrom()).addName(rmbProfile.getName())
                        .addTimestamp(message.getId())
                        .setFriendsList(rmbProfile.getFriendProfiles())
                        .addProfileId(rmbProfile.getProfileId())
                        .addUri(rmbProfile.getThumbnail())
                        .enableInFriends(rmbProfile.isShowInFriendsEnabled() ? Profile.DISPLAY_IN_FRIENDS : Profile.NOT_DISPLAY_IN_FRIENDS)
                        .addActualState(new ActualState.Builder().build())
                        .build();

                //only one attachemnt and that is uri
                for (MessageAttachment attachment : message.getPayload().getAttachments()) {
                    profile = new Profile.Builder(profile).addUri(attachment.getUri()).build();
                }
                valid = profileReceived(profile);
            } else if (data instanceof com.croconaut.ratemebuddy.data.pojo.Status) {
                if (message.getFrom() == null) {
                    Log.e(TAG, "Received my status from broadcast after RMB clear with null 'from'");
                    return null;
                }

                com.croconaut.ratemebuddy.data.pojo.Status status = (com.croconaut.ratemebuddy.data.pojo.Status) data;
                status = new com.croconaut.ratemebuddy.data.pojo.Status.Builder(status).crocoId(message.getFrom()).build();
                valid = statusReceived(status, message.getFrom());
            } else if (data instanceof Comment) {
                Comment comment = (Comment) data;

                if (message.getFrom() == null) {
                    return null;
                }

                valid = commentReceived(comment, message.getFrom());
            } else if (data instanceof VoteUp) {
                VoteUp voteUp = (VoteUp) data;

                if (message.getFrom() == null) {
                    return null;
                }

                valid = voteUpReceived(voteUp, message);
            } else if (data instanceof Message) {
                Message rmbPMessage = (Message) data;

                // -Xi- msg decoding modification
                String passB64 = "XiB1cDNyLVQ0am7DqS5IM3NsMCxQcmUvS8OzRG92NG5pZSpTcHLDoXYr"; //Base64.encodeToString(pass.getBytes(), Base64.DEFAULT);
                XorString xs = new XorString();
                String deXoredMsg = xs.xoring(rmbPMessage.getContent(), passB64);
                int flags = Base64.NO_WRAP | Base64.NO_PADDING;
                String origMsg = new String(Base64.decode(deXoredMsg.getBytes(), flags));
                Message rmbPMessageDecoded = new Message(origMsg, rmbPMessage.getProfileId(), rmbPMessage.getProfileName());

                if (message.getFrom() == null) {

                    String hops = null;
                    try {
                        hops = createHops(message);
                    } catch (Exception e) {
                        Log.e(TAG, "Hops cannot be parsed!", e);
                    }

                    UIMessage uiMessage = new UIMessage.Builder(message.getTo(),
                            rmbPMessageDecoded.getContent(),
                            date.getTime(),
                            UIMessage.INCOMING)
                            .receivedTime(message.getId())
                            .hops(hops).build();
                    appData.getUiMessageDataSource().insertMessage(uiMessage);

                    return null;
                }

                if (!rmbPMessage.getContent().isEmpty()) {
                    valid = messageReceived(message,
                            date,
                            rmbPMessageDecoded
                    );
                }
            } else if (data instanceof Attachment
                    || !message.getPayload().getAttachments().isEmpty()) {
                valid = fileReceived(message, date);
            } else if (data instanceof TicTacToeGame) {
                //FIXME: oh my god, this is so SAD to put it here! :(

                if (message.getFrom() != null) {

                    //this is a HACK so we have at least unknown profile inside ProfileDataSource
                    profileUtils.findProfile(message.getFrom());

                    if (data instanceof Invite) {
                        final Invite invite = ((Invite) data);

                        if (invite instanceof InviteRequest) {
                            valid = appData.getGameCommunication().processInviteRequest(
                                    appData, message.getFrom(), ((InviteRequest) invite));
                        } else if (invite instanceof InviteResponse) {
                            valid = appData.getGameCommunication().processInviteResponse(
                                    appData, message.getFrom(), ((InviteResponse) invite));
                        }
                    } else if (data instanceof Move) {
                        final Move move = ((Move) data);
                        valid = appData.getGameCommunication().processMove(move);
                    } else if (data instanceof Surrender) {
                        final Surrender surrender = ((Surrender) data);
                        valid = appData.getGameCommunication().processSurrender(surrender);

                        if (!valid) {
                            InviteNotificationManager
                                    .cancelNotification(appData, message.getFrom().hashCode());
                        }
                    }
                }
            } else {
                Log.e(TAG, "Received BinaryMessagePayload with wrong serialized object!");
            }

            return new PostExecuteValue(valid, intent, message);
        } catch (ClassNotFoundException |
                IOException e)

        {
            Log.e(TAG, "doInBackground", e);
        }

        return new PostExecuteValue(false, params[0], null);

    }

    @Override
    protected void onPostExecute(PostExecuteValue postExecuteValue) {
        super.onPostExecute(postExecuteValue);

        if (postExecuteValue == null) {
            return;
        }

        try {
            Intent intent = postExecuteValue.getIntent();
            Serializable data = postExecuteValue.getMessage().getPayload().getAppData();

            Log.e(TAG, "onPostExecute");

            if (cptProcessor != null && data instanceof RMBProfile && postExecuteValue.isValid()) {
                cptProcessor.process(intent);
                return;
            } else if (cptProcessor != null &&
                    data instanceof com.croconaut.ratemebuddy.data.pojo.Status && postExecuteValue.isValid()) {
                cptProcessor.process(intent);
                return;
            }

            boolean valid = postExecuteValue.isValid();

            if (cptProcessor != null)
                valid = valid && !cptProcessor.process(postExecuteValue.getIntent());
            Log.e(TAG, "onPostExecute - other: " + valid);

            if (cptProcessor != null && data instanceof TicTacToeGame) {
                valid = valid && !cptProcessor.process(intent);
                Log.e(TAG, "onPostExecute - TicTacToeGame: " + valid);
            }

            Log.e(TAG, "onPostExecute - valid: " + valid);

            if (!valid) return;

            if (data instanceof Comment)
                Notification.createOrUpdate(CommentNotification.COMMENT_NOTIF_ID, appData);
            else if (data instanceof VoteUp)
                Notification.createOrUpdate(VoteUpNotification.VOTE_UP_NOTIF_ID, appData);
            else if (data instanceof Message || data instanceof Attachment)
                Notification.createOrUpdate(MessageNotification.MESSAGE_NOTIF_ID, appData);
            else if (data instanceof Move || data instanceof Surrender)
                GameNotificationManager.createNewMoveNotification(appData, ((TicTacToeGame) data));
            else
                Log.e(TAG, "Cannot show notification to this intent!");
        } catch (ClassNotFoundException | IOException e) {
            Log.e(TAG, "onPostExecute", e);
        }
    }


    // send new profile to RBM
    private boolean profileReceived(Profile profile) {

        if (isProfileBlocked(profile)) {
            Log.e(TAG, "Profile " + profile.getName() + " is blocked, returning!");
            return false;
        }

        if (profile.getCrocoId() == null) {
            Log.e(TAG, "Received profile has crocoID as null!");
            return false;
        }

        if (profile.getProfileId() != null) {
            Log.e(TAG, "Not null profile ID: " + (profile.getProfileId()));
            Profile checkNullProfileId = appData.getProfileDataSource().getProfileByCrocoId(profile.getCrocoId());

            if (checkNullProfileId != null && checkNullProfileId.getType() == Profile.FAVOURITE
                    && checkNullProfileId.getTimeStamp() < profile.getTimeStamp()
                    && (checkNullProfileId.getProfileId() == null || !checkNullProfileId.getProfileId().equalsIgnoreCase(profile.getProfileId()))) {
                Log.e(TAG, "Was favourite and has lower timestamp");
                appData.getProfileDataSource().updateProfile(profile);

                try {
                    Log.e(TAG, "sending profile");
                    ProfileUtils.createAndSendRmbProfile(
                            appData,
                            MyProfile.getInstance(appData),
                            MyProfile.getInstance(appData).getThumbUri()
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else Log.e(TAG, "Not sending");
        } else Log.e(TAG, "RMBProfileId is NULL");

        if (profile.getFriendsList() != null) {
            for (IProfile friendProfile : profile.getFriendsList()) {
                if (!appData.getProfileDataSource().isProfileInDB(friendProfile.getIdent())) {
                    appData.getProfileDataSource().insertProfile((Profile) friendProfile);
                } else {
                    Profile profileDb = appData.getProfileDataSource().getProfileByCrocoId(friendProfile.getIdent());
                    if (profileDb != null && isDifferTimestamp((Profile) friendProfile, profileDb)) {
                        appData.getProfileDataSource().updateProfile((Profile) friendProfile);
                    }
                }
            }
        }

        if (appData.getProfileDataSource().isProfileInDB(profile.getCrocoId())) {
            Profile profileDb = appData.getProfileDataSource().getProfileByCrocoId(profile.getCrocoId());
            if (profileDb != null && isDifferTimestamp(profile, profileDb)) {
                appData.getProfileDataSource().updateProfile(profile);
            }
        } else {
            profile.setType(Profile.CACHED);
            appData.getProfileDataSource().insertProfile(profile);
        }

        profile = appData.getProfileDataSource().getProfileByCrocoId(profile.getCrocoId());

        if (appData.getNearbyPeople().contains(profile)) {
            int nearbyProfileIndex = appData.getNearbyPeople().indexOf(profile);
            Profile profileNearby = appData.getNearbyPeople().get(nearbyProfileIndex);
            if (isDifferTimestamp(profile, profileNearby)) {
                appData.getNearbyPeople().remove(nearbyProfileIndex);
                appData.getNearbyPeople().add(profile);
            }
        }


        Log.e(TAG, "Processed accessed and passed: " + profile.getFriendsList().size());
        return true;
    }

    private void sendMyProfile(Profile profile, String oldProfileId) {
        if ((profile.getType() == Profile.FAVOURITE) && !TextUtils.isEmpty(oldProfileId)) {
            try {
                ProfileUtils.createAndSendRmbProfile(
                        appData,
                        MyProfile.getInstance(appData),
                        MyProfile.getInstance(appData).getThumbUri()
                );
            } catch (IOException ex) {
                Log.e(TAG, "Cannot send my updated profile");
            }
        }
    }

    private boolean isDifferTimestamp(Profile p1, Profile p2) {
        return p1.getTimeStamp() > p2.getTimeStamp();
    }

    private boolean commentReceived(Comment comment, String crocoFrom) {
        comment = new Comment.Builder(comment)
                .crocoId(crocoFrom)
                .seen(false)
                .build();

        TimelineInfo timelineInfo;
        Profile remoteProfile = (Profile) profileUtils.findProfile(crocoFrom);
        Log.e(TAG, "Comment received ...: " + comment);

        if (isProfileBlocked(remoteProfile)) {
            Log.e(TAG, "Received comment from blocked profile!");
            return false;
        }

        com.croconaut.ratemebuddy.data.pojo.Status status = appData.getStatusDataSource().getStatusByID(comment.getStatusId());
        if (status != null && !status.getComments().contains(comment)) {
            status.getComments().add(comment);
            appData.getStatusDataSource().updateStatus(status);
            Log.e(TAG, "Comment added ...: " + comment);

            timelineInfo = new TimelineInfo.Builder(System.currentTimeMillis(), status.getContent(),
                    remoteProfile.getIdent(), remoteProfile.getName(), TimelineInfo.INCOMING, TimelineInfo.MESSAGE_TYPE_COMMENT, false)
                    .statusId(comment.getStatusId()).build();
            appData.getTimelineDataSource().insertTimelineInfo(timelineInfo);
        } else {
            Log.e(TAG, "Status not found or we already received this comment!");
            return false;
        }

        MyProfile myProfile = MyProfile.getInstance(appData);
        if (myProfile.getStatus() != null
                && myProfile.getStatus().getStatusID().equals(status.getStatusID())) {
            Log.e(TAG, "Sending status ...");
            myProfile = new MyProfile.Builder(myProfile).status(status).build(appData);
            sendStatus(status);
        } else if (timelineInfo != null) {
            Notification.createOrUpdate(OldStatusNotif.OLD_COMMENT_NOTIF_ID, appData);
        }

        return true;
    }

    protected boolean voteUpReceived(VoteUp voteUp, IncomingMessage msg) {
        Profile profileFrom = (Profile) profileUtils.findProfile(msg.getFrom(), voteUp.getProfileName());

        if (isProfileBlocked(profileFrom)) {
            Log.d(TAG, "Profile " + profileFrom.getName() + " is blocked, returning!");
            return false;
        }

        com.croconaut.ratemebuddy.data.pojo.Status status = appData.getStatusDataSource().getStatusByID(voteUp.getStatusId());
        if (status == null || status.getVotes().contains(voteUp)) {
            Log.e(TAG, "Received voteUp for non existent status or we already received vote!");
            return false;
        }

        voteUp = new VoteUp.Builder(voteUp)
                .seen(false)
                .crocoId(msg.getFrom())
                .build();
        status = new com.croconaut.ratemebuddy.data.pojo.Status.Builder(status)
                .addVote(voteUp)
                .build();

        appData.getStatusDataSource().updateStatus(status);


        TimelineInfo timelineInfo = new TimelineInfo.Builder(System.currentTimeMillis(), status.getContent(),
                voteUp.getCrocoId(), voteUp.getProfileName(), TimelineInfo.INCOMING, TimelineInfo.MESSAGE_TYPE_VOTE, false)
                .statusId(voteUp.getStatusId()).build();
        appData.getTimelineDataSource().insertTimelineInfo(timelineInfo);

        MyProfile myProfile = MyProfile.getInstance(appData);
        if (myProfile.getStatus() != null
                && myProfile.getStatus().getProfileId().equals(status.getProfileId())) {
            myProfile = new MyProfile.Builder(myProfile).status(status).build(appData);
            sendStatus(status);
        } else {
            Notification.createOrUpdate(OldStatusNotif.OLD_COMMENT_NOTIF_ID, appData);
        }

        return true;
    }

    private boolean statusReceived(com.croconaut.ratemebuddy.data.pojo.Status status, String crocoIdFrom) {
        Log.e(TAG, "Status received");

        ArrayList<Comment> comments = status.getComments();

        ArrayList<Comment> commentsToBlock = new ArrayList<>();
        for (Comment comment : comments) {
            if (comment.getCrocoId().equals(status.getProfileId())) {
                comment.setCrocoId(crocoIdFrom);
            }

            for (Profile blockedProfile : appData.getProfileDataSource().getProfilesByType(Profile.BLOCKED)) {
                if (blockedProfile.getCrocoId().equals(comment.getCrocoId())) {
                    commentsToBlock.add(comment);
                }
            }
        }

        comments.removeAll(commentsToBlock);

        status = new com.croconaut.ratemebuddy.data.pojo.Status.Builder(status).crocoId(crocoIdFrom).comments(comments).build();
        Profile profile = (Profile) profileUtils.findProfile(status.getCrocoId(), status.getProfileName());

        //find profile by id
        if (profile == null) {
            Log.e(TAG, "Found profile is null!");
            return false;
        }

        if (isProfileBlocked(profile)) {
            Log.e(TAG, "Received status on blocked profile!");
            return false;
        }

//        MyProfile myProfile = MyProfile.getInstance(appData);
//        com.croconaut.ratemebuddy.data.pojo.Status myStatus = myProfile.getStatus();
//
//        if (myStatus != null) {
//            for (Comment comment : myStatus.getComments()) {
//                if (comment.getCrocoId().equals(crocoIdFrom)) {
//                    checkIfProfileIsUpToDate(crocoIdFrom, profile);
//                    break;
//                }
//            }
//
//            for (VoteUp voteUp : myStatus.getVotes()) {
//                if (voteUp.getCrocoId().equals(crocoIdFrom)) {
//                    checkIfProfileIsUpToDate(crocoIdFrom, profile);
//                    break;
//                }
//            }
//        }


        if (profile.getStatus() != null && !profile.getStatus().getStatusID().equals(status.getStatusID())) {

            if (profile.getType() == Profile.FAVOURITE) {
                TimelineInfo timelineInfo = new TimelineInfo.Builder(System.currentTimeMillis(),
                        appData.getString(R.string.message_timeline_status_changed_remote, profile.getName(), status.getContent()),
                        profile.getIdent(),
                        profile.getName(),
                        TimelineInfo.INCOMING,
                        TimelineInfo.MESSAGE_TYPE_PROFILE_CHANGED_TEXT,
                        false).build();

                appData.getTimelineDataSource().insertTimelineInfo(timelineInfo);
            }

        }


        if (profile.getStatus() == null || !profile.getStatus().getStatusID().equals(status.getStatusID())) {
            Log.e(TAG, "Status received or changed for profile: " + profile.getName());
            if (profile.getActualState() != null) {
                profile.getActualState().clearAndSetActual();
            }
            profile.setStatus(status);
            appData.getProfileDataSource().updateProfile(profile);
        } else if (!profile.isActual()) {
            Log.e(TAG, "Status was not actual");
            for (VoteUp voteUp : status.getVotes()) {
                profile.getActualState().removeVoteIfEquals(voteUp);
            }

            for (Comment comment : status.getComments()) {
                profile.getActualState().removeCommentIfContains(comment);
            }

            if (!profile.isActual()) {
                VoteUp notActualVoteUp = profile.getActualState().getNotActualVote();
                if (notActualVoteUp != null)
                    profile.getStatus().getVotes().add(notActualVoteUp);
                profile.getStatus().getComments().addAll(profile.getActualState().getNotActualComments());
            }

            profile.setStatus(status);
            appData.getProfileDataSource().updateProfile(profile);
        } else if (status.getTimeStamp() >= profile.getStatus().getTimeStamp()) {
            Log.e(TAG, "Status timestamp is bigger/equal (same status but updated)");
            profile.setStatus(status);
            appData.getProfileDataSource().updateProfile(profile);
        } else {
            Log.e(TAG, "Status has not changed!");
            return false;
        }

        return true;
    }

//    private void checkIfProfileIsUpToDate(String crocoIdFrom, Profile profile) {
//        Profile updateProfile = appData.getProfileDataSource().getProfileByCrocoId(crocoIdFrom);
//
//        if (updateProfile != null && updateProfile.getTimeStamp() < profile.getTimeStamp()
//                && (updateProfile.getProfileId() == null || !updateProfile.getProfileId().equalsIgnoreCase(profile.getProfileId()))) {
//            appData.getProfileDataSource().updateProfile(profile);
//
//            sendStatus(appData.getStatusDataSource().getStatusByID(MyProfile.getInstance(appData).getStatus().getStatusID()));
//        } else Log.e(TAG, "Not sending");
//    }

    protected boolean fileReceived(IncomingMessage msg, Date receivedTime) {
        List<? extends MessageAttachment> messageAttachments = msg.getPayload().getAttachments();
        String crocoIdFrom = msg.getFrom();
        long messageId = msg.getId();
        MessageAttachment messageAttachment = messageAttachments.get(0);

        if (appData.getUiMessageDataSource().isMessageInDB(messageId, crocoIdFrom)) {
            Log.e(TAG, "We already received this file, returning!");
            return false;
        }

        Profile fileProfile = (Profile) profileUtils.findProfile(crocoIdFrom);
        if (fileProfile == null) {
            Log.e(TAG, "Profile not found, returning!");
            return false;
        }

        if (isProfileBlocked(fileProfile)) {
            Log.d(TAG, "Profile " + fileProfile.getName() + " is blocked, returning!");
            return false;
        }

        String hops = null;
        try {
            hops = createHops(msg);
        } catch (Exception e) {
            Log.e(TAG, "Hops cannot be parsed!", e);
        }

        // TODO: just a quick, one time, hack
        int storageType;
        if (messageAttachment.getStorageDirectory() == null) {
            storageType = 0;
        } else if (messageAttachment.getStorageDirectory().equals(Environment.DIRECTORY_PICTURES)) {
            storageType = 1;
        } else if (messageAttachment.getStorageDirectory().equals(Environment.DIRECTORY_DOWNLOADS)) {
            storageType = 2;
        } else {
            Log.e(TAG, "Unknown storage directory: " + messageAttachment.getStorageDirectory());
            storageType = 2;    // downloads
        }

        UIMessageAttachment uiMessageAttachment = new UIMessageAttachment.Builder(
                messageAttachment.getName(appData))
                .type(messageAttachment.getType(appData))
                .lastModified(messageAttachment.getLastModified(appData))
                .length(messageAttachment.getLength(appData))
                .sourceUri(messageAttachment.getSourceUri())
                .storageType(storageType)
                .state(UIMessageAttachment.STATE_WAITING_FOR_CONFIRMATION)
                .build();

        UIMessage uiMessage = new UIMessage.Builder(crocoIdFrom, messageAttachment.getName(appData), receivedTime.getTime(), UIMessage.INCOMING)
                .receivedTime(messageId)
                .hops(hops)
                .uiAttachmentId(messageId)
                .uiMessageAttachment(uiMessageAttachment)
                .build();
        appData.getUiMessageDataSource().insertMessage(uiMessage);

        // if profile is not in favourites nor unknown, we need to add it to unknown ...
        if (!(fileProfile.getType() == Profile.FAVOURITE)
                && !(fileProfile.getType() == Profile.UNKNOWN)) {
            fileProfile.setType(Profile.UNKNOWN);
            updateOrInsertProfile(fileProfile);
        }


        boolean valid = MessageNotification.currentProfile == null
                || !fileProfile.equals(MessageNotification.currentProfile);

        if (valid) {
            // set last message and increment unread
            fileProfile.setUnread(fileProfile.getUnread() + 1);
            fileProfile.getUnreadMessages().add(uiMessage);
            appData.getProfileDataSource().updateUnread(
                    fileProfile.getCrocoId(),
                    fileProfile.getUnread(),
                    fileProfile.getUnreadMessages());
        }

        appData.syncProfileToNearby(fileProfile);

        return true;
    }

    private void updateOrInsertProfile(Profile fileProfile) {
        if (appData.getProfileDataSource().isProfileInDB(fileProfile.getCrocoId()))
            appData.getProfileDataSource().updateProfile(fileProfile);
        else appData.getProfileDataSource().insertProfile(fileProfile);
    }

    // send local broadcast to RMB that new message was received
    private boolean messageReceived(IncomingMessage msg, Date deliveredTime, Message message) {
        Profile remoteProfile = (Profile) profileUtils.findProfile(msg.getFrom(), message.getProfileName());

        if (remoteProfile == null || remoteProfile.getType() == Profile.BLOCKED) {
            Log.d(TAG, "Message blocked or profile is null, returning");
            return false;
        }

        if (appData.getUiMessageDataSource().isMessageInDB(msg.getId(), msg.getFrom())) {
            Log.i(TAG, "Message already in DB!");
            return false;
        }

        // if profile is not in favourites nor unknown, we need to add it to unknown ...
        if (!(remoteProfile.getType() == Profile.FAVOURITE) && !(remoteProfile.getType() == Profile.UNKNOWN)) {
            remoteProfile.setType(Profile.UNKNOWN);
            updateOrInsertProfile(remoteProfile);
        }

        String hops = null;
        try {
            hops = createHops(msg);
        } catch (Exception e) {
            Log.e(TAG, "Hops cannot be parsed!", e);
        }

        UIMessage uiMessage = new UIMessage.Builder(
                msg.getFrom(), message.getContent(), msg.getId(), UIMessage.INCOMING)
                .receivedTime(deliveredTime.getTime())
                .hops(hops).build();
        appData.getUiMessageDataSource().insertMessage(uiMessage);


        boolean valid = MessageNotification.currentProfile == null
                || !remoteProfile.equals(MessageNotification.currentProfile);

        if (valid) {
            // set last message and increment unread
            remoteProfile.setUnread(remoteProfile.getUnread() + 1);
            remoteProfile.getUnreadMessages().add(uiMessage);
            appData.getProfileDataSource().updateUnread(
                    remoteProfile.getCrocoId(),
                    remoteProfile.getUnread(),
                    remoteProfile.getUnreadMessages());
        }

        appData.syncProfileToNearby(remoteProfile);

        return true;
    }

    private void sendStatus(com.croconaut.ratemebuddy.data.pojo.Status status) {
        Log.e(TAG, "Sending status");
        try {
            OutgoingMessage serviceMsg = new OutgoingPersistentBroadcastMessage(new OutgoingPayload(status), CptReceiver.STATUS_PERSISTENT_ID)
                    .setIsExpectingSent(false);

            Communication.newMessage(appData, serviceMsg);
        } catch (IOException e) {
            Log.e(TAG, "sendStatus", e);
        }
    }

    private boolean isProfileBlocked(Profile profile) {
        return appData.getProfileDataSource().getProfilesByType(Profile.BLOCKED).contains(profile);
    }


    private String createHops(IncomingMessage msg) throws Exception {
        JSONObject json = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        Log.i(TAG, "Processing hops size: " + msg.getHops().size());
        try {
            for (int i = 0; i < msg.getHops().size(); i++) {
                NetworkHop objHop = msg.getHops().get(i);
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
        } catch (Exception e) {
            Log.e(TAG, "Error: ", e);
        }
        return json.toString();
    }
}
