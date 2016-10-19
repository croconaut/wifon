package com.croconaut.ratemebuddy.activities;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.croconaut.cpt.data.Communication;
import com.croconaut.cpt.data.IncomingMessage;
import com.croconaut.cpt.data.OutgoingMessage;
import com.croconaut.cpt.data.OutgoingPayload;
import com.croconaut.cpt.data.OutgoingPersistentBroadcastMessage;
import com.croconaut.ratemebuddy.CptReceiver;
import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.activities.notifications.CommentNotification;
import com.croconaut.ratemebuddy.activities.notifications.Notification;
import com.croconaut.ratemebuddy.activities.notifications.VoteUpNotification;
import com.croconaut.ratemebuddy.data.pojo.Comment;
import com.croconaut.ratemebuddy.data.pojo.RMBProfile;
import com.croconaut.ratemebuddy.data.pojo.Status;
import com.croconaut.ratemebuddy.data.pojo.VoteUp;
import com.croconaut.ratemebuddy.ui.adapters.CommentsListAdapter;
import com.croconaut.ratemebuddy.ui.adapters.EmoticonsAdapter;
import com.croconaut.ratemebuddy.ui.adapters.GridViewProfileAdapter;
import com.croconaut.ratemebuddy.utils.CommonUtils;
import com.croconaut.ratemebuddy.utils.pojo.profiles.IProfile;
import com.croconaut.ratemebuddy.utils.pojo.profiles.MyProfile;
import com.croconaut.ratemebuddy.utils.pojo.profiles.Profile;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CommentActivity extends WifonActivity implements CptProcessor {

    private static final String TAG = CommentActivity.class.getName();
    public static final String EXTRA_CROCO_ID = "EXTRA_CROCO_ID";
    public static final String EXTRA_OLD_STATUS_ID = "EXTRA_OLD_STATUS_ID";
    private static final int COMMENT_SCROLL_OFFSET = 5;

    private ListView listViewComments;
    private RelativeLayout emoticonsLayout;
    private ImageButton btnSendComment;
    private CommentsListAdapter commentsListAdapter;
    private Status status;
    private ArrayList<Comment> comments;
    private EditText etComment;
    private IProfile remoteProfile;
    private View newCommentHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        checkForExtras(this.getIntent());
        initializeHeaderWithDrawer(remoteProfile.getName(), false);
        initialize();
        clearCommentsAndLikes();
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.setIntent(intent);
        checkForExtras(intent);
        initialize();
        clearCommentsAndLikes();
    }

    private void initialize() {
        TextView tvStatus = (TextView) findViewById(R.id.tvStatus);
        tvStatus.setText(status.getContent());
        tvStatus.setTypeface(tSemiBold);

        initializeLikes();

        listViewComments = (ListView) findViewById(R.id.lvComments);
        listViewComments.setDivider(null);

        etComment = (EditText) findViewById(R.id.etComment);
        etComment.clearFocus();
        btnSendComment = (ImageButton) findViewById(R.id.btnSendComment);
        btnSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (remoteProfile.getIdent().equals(MyProfile.getInstance(mContext).getIdent()) &&
                        !MyProfile.getInstance(mContext).getStatus().getStatusID()
                                .equals(status.getStatusID())) {
                    Toast.makeText(mContext, mRes.getString(R.string.toast_comment_old_status), Toast.LENGTH_LONG).show();
                    btnSendComment.startAnimation(AnimationUtils.loadAnimation(CommentActivity.this, R.anim.shake_error));
                    return;
                }


                String text = etComment.getText().toString();
                if (text.length() == 0) {
                    btnSendComment.startAnimation(AnimationUtils.loadAnimation(CommentActivity.this, R.anim.shake_error));
                } else {
                    MyProfile myProfile = MyProfile.getInstance(mContext);

                    Comment comment = new Comment.Builder(
                            System.currentTimeMillis(),
                            etComment.getText().toString(),
                            status.getStatusID(),
                            myProfile.getProfileId(),
                            myProfile.getName())
                            .crocoId(myProfile.getProfileId())
                            .build();

                    if (remoteProfile instanceof Profile) {
                        ((Profile) remoteProfile).setStatus(status);
                        appData.getProfileDataSource().updateProfile((Profile) remoteProfile);
                        comment = sendComment(comment, (Profile) remoteProfile);
                    } else {
                        OutgoingMessage serviceMsg = new OutgoingPersistentBroadcastMessage(CptReceiver.STATUS_PERSISTENT_ID)
                                .setIsExpectingSent(false);

                        comment = new Comment.Builder(comment)
                                .timestamp(serviceMsg.getCreationDate().getTime())
                                .seen(true).build();
                        status.getComments().add(comment);
                        myProfile = new MyProfile.Builder(myProfile).status(status).build(mContext);
                        appData.getStatusDataSource().updateStatus(myProfile.getStatus());

                        try {
                            serviceMsg.setPayload(new OutgoingPayload(status));
                            Communication.newMessage(CommentActivity.this, serviceMsg);
                        } catch (IOException e) {
                            Log.e(TAG, "sendStatus", e);
                        }
                    }

                    etComment.setText(null);
                    emoticonsLayout.setVisibility(View.GONE);
                    comments = status.getComments();

                    commentsListAdapter.setData(comments);
                    scrollToBottom();

                    CommonUtils.hideKeyboard(CommentActivity.this);
                }
            }
        });

        emoticonsLayout = (RelativeLayout) findViewById(R.id.emoticonsLayout);

        GridView emoticons = (GridView) findViewById(R.id.emoticons);
        assert emoticons != null;
        emoticons.setAdapter(new EmoticonsAdapter(this, etComment));

        ImageButton btnEmoticons = (ImageButton) findViewById(R.id.btnSelectEmoticon);
        assert btnEmoticons != null;
        btnEmoticons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (emoticonsLayout.getVisibility() == View.VISIBLE) {
                    emoticonsLayout.setVisibility(View.GONE);
                } else {
                    emoticonsLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        newCommentHint = findViewById(R.id.newMessageHint);
        TextView tvHint = (TextView) findViewById(R.id.tvHint);
        assert tvHint != null;
        tvHint.setText(getResources().getText(R.string.comment_new_hint));

        newCommentHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newCommentHint.setVisibility(View.GONE);
                listViewComments.smoothScrollToPosition(listViewComments.getCount() - 1);
            }
        });

        listViewComments.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (newCommentHint.getVisibility() == View.VISIBLE && (totalItemCount - visibleItemCount <= firstVisibleItem)) {
                    newCommentHint.setVisibility(View.GONE);
                }
            }
        });

        refreshComments(true);
    }

    private void initializeLikes() {
        String profileId = MyProfile.getInstance(mContext).getProfileId();
        List<IProfile> voteProfiles = new ArrayList<>();

        if (status.getVotes() == null) {
            Log.e(TAG, "Status votes are null, returning");
            return;
        }

        for (VoteUp voteUp : status.getVotes()) {
            voteProfiles.add(profileUtils.findProfile(
                    profileId.equals(voteUp.getProfileId())
                            ? voteUp.getProfileId()
                            : voteUp.getCrocoId(),
                    voteUp.getProfileName()
                    )
            );
        }

        GridView gvLikes = (GridView) findViewById(R.id.gvLikes);
        TextView tvLikesHint = (TextView) findViewById(R.id.likesHint);

        assert gvLikes != null;
        assert tvLikesHint != null;

        GridViewProfileAdapter gridViewProfileAdapter = new GridViewProfileAdapter(
                mContext,
                remoteProfile,
                appData,
                voteProfiles.size() < 5
                        ? voteProfiles
                        : voteProfiles.subList(0, 5),
                voteProfiles.size(),
                DisplayProfilesActivity.DISPLAY_LIKES
        );

        if (voteProfiles.size() > 0) {
            gvLikes.setVisibility(View.VISIBLE);
            tvLikesHint.setVisibility(View.VISIBLE);
            gvLikes.setAdapter(gridViewProfileAdapter);
        } else {
            gvLikes.setVisibility(View.GONE);
            tvLikesHint.setVisibility(View.GONE);
        }
    }

    private void scrollToBottom() {
        Log.e(TAG, "Scrolling to bottom");

        listViewComments.clearFocus();
        listViewComments.post(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "Scrolling to bottom " + commentsListAdapter.getCount());
                listViewComments.setSelection(commentsListAdapter.getCount() - 1);
            }
        });
    }

    private void scrollIfFacingLastFiveMessages() {
        int lastVisiblePosition = listViewComments.getLastVisiblePosition();

        if (lastVisiblePosition + COMMENT_SCROLL_OFFSET > commentsListAdapter.getCount()) {
            listViewComments.setSelection(commentsListAdapter.getCount() - 1);
        } else if (newCommentHint.getVisibility() != View.VISIBLE) {
            newCommentHint.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        CommentNotification.currentStatus = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        clearCommentsAndLikes();
        initializeLikes();
        refreshComments(false);
        CommentNotification.currentStatus = status;
    }

    private void checkForExtras(Intent intent) {
        Log.e(TAG, "Checking extras1");
        Bundle extras = intent.getExtras();

        if (extras == null) {
            Log.e(TAG, "Extras are null");
            return;
        }

        String crocoId = extras.getString(EXTRA_CROCO_ID);
        remoteProfile = profileUtils.findProfile(crocoId);
        status = remoteProfile.getStatus();

        String targetStatusId = extras.getString(EXTRA_OLD_STATUS_ID, null);
        if (targetStatusId != null) {
            status = appData.getStatusDataSource().getStatusByID(targetStatusId);
        }
    }

    @Override
    public boolean process(Intent cptIntent) throws IOException, ClassNotFoundException {
        super.process(cptIntent);

        switch (cptIntent.getAction()) {
            case Communication.ACTION_MESSAGE_ARRIVED:
                final IncomingMessage message = cptIntent.getParcelableExtra(Communication.EXTRA_MESSAGE_ARRIVED);
                Serializable data = message.getPayload().getAppData();

                if (data instanceof Comment || data instanceof VoteUp) {
                    if (remoteProfile instanceof MyProfile) {
                        if (data instanceof Comment) {
                            Comment commentData = (Comment) data;
                            status = appData.getStatusDataSource().getStatusByID(commentData.getStatusId());
                            comments = status.getComments();

                            ArrayList<Comment> commentsToBlock = new ArrayList<>();
                            for (Comment comment : comments) {
                                for (Profile blockedProfile : appData.getProfileDataSource().getProfilesByType(Profile.BLOCKED))
                                    if (blockedProfile.getCrocoId().equals(comment.getCrocoId()))
                                        commentsToBlock.add(comment);
                            }

                            comments.removeAll(commentsToBlock);

                            commentsListAdapter.setData(comments);

                            scrollIfFacingLastFiveMessages();
                        } else {
                            status = appData.getStatusDataSource().getStatusByID(((VoteUp) data).getStatusId());
                            initializeLikes();
                        }
                        return true;
                    }
                } else if (data instanceof Status) {
                    if (message.getFrom() != null
                            && remoteProfile.getIdent() != null
                            && !message.getFrom().equals(remoteProfile.getIdent())) {
                        return false;
                    }

                    remoteProfile = profileUtils.findProfile(remoteProfile.getIdent(), remoteProfile.getName());
                    status = remoteProfile.getStatus();


                    if (status.getContent() == null) {
                        onBackPressed();
                        finish();
                        return true;
                    }

                    if (status.getCrocoId().equals(remoteProfile.getIdent())
                            || (status.getProfileId() != null && status.getProfileId().equals(remoteProfile.getIdent()))) {
                        initialize();
                        return true;
                    }
                } else if (data instanceof RMBProfile) {
                    boolean updated = false;
                    for (Comment comment : comments) {
                        if (updated) break;
                        if (comment.getCrocoId() != null && comment.getCrocoId().equals(message.getFrom())) {

                            int selection = listViewComments.getFirstVisiblePosition();
                            commentsListAdapter = new CommentsListAdapter(appData, comments);
                            listViewComments.setAdapter(commentsListAdapter);
                            listViewComments.setSelection(selection);
                            commentsListAdapter.addSmiles();

                            updated = true;
                        }
                    }

                    for (VoteUp voteUp : status.getVotes()) {
                        if (voteUp.getCrocoId() != null && voteUp.getCrocoId().equals(message.getFrom())) {
                            initializeLikes();
                        }
                    }
                }

                break;
        }
        return false;
    }

    private void refreshComments(final boolean scrollToBottom) {
        comments = status.getComments();

        final int selection = listViewComments.getFirstVisiblePosition();
        ArrayList<Comment> commentsToBlock = new ArrayList<>();
        for (Comment comment : comments) {
            for (Profile blockedProfile : appData.getProfileDataSource().getProfilesByType(Profile.BLOCKED))
                if (blockedProfile.getCrocoId().equals(comment.getCrocoId()))
                    commentsToBlock.add(comment);
        }

        comments.removeAll(commentsToBlock);

        commentsListAdapter = new CommentsListAdapter(appData, comments);
        listViewComments.setAdapter(commentsListAdapter);
        listViewComments.setSelection(selection);
        listViewComments.post(new Runnable() {
            @Override
            public void run() {
                listViewComments.setSelection(scrollToBottom ? comments.size() - 1 : selection);
            }
        });
        commentsListAdapter.addSmiles();
    }

    private void clearCommentsAndLikes() {
        if (remoteProfile instanceof MyProfile) {
            Notification.clearNotification(mContext, VoteUpNotification.VOTE_UP_NOTIF_ID);
            Notification.clearNotification(mContext, CommentNotification.COMMENT_NOTIF_ID);
        }

        Log.e(TAG, "Remote profile: " + remoteProfile.getName());
        status.clearUnseenComments();
        status.clearUnseenVotes();
        if (remoteProfile instanceof MyProfile) {
            Log.e(TAG, "Clear comments and votes on my profile");
            MyProfile myProfile = MyProfile.getInstance(mContext);
            appData.getStatusDataSource().updateStatus(status);
            new MyProfile.Builder(myProfile).status(status).build(mContext);
        } else if (remoteProfile instanceof Profile) {
            Log.e(TAG, "Clear comments on other profile");
            ((Profile) remoteProfile).setStatus(status);
            appData.getProfileDataSource().updateProfile((Profile) remoteProfile);
        }
    }
}
