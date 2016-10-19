package com.croconaut.ratemebuddy.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.croconaut.cpt.data.Communication;
import com.croconaut.cpt.data.IncomingMessage;
import com.croconaut.cpt.data.OutgoingMessage;
import com.croconaut.cpt.data.OutgoingPayload;
import com.croconaut.ratemebuddy.AppData;
import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.activities.notifications.MessageNotification;
import com.croconaut.ratemebuddy.activities.notifications.Notification;
import com.croconaut.ratemebuddy.data.pojo.Attachment;
import com.croconaut.ratemebuddy.data.pojo.Message;
import com.croconaut.ratemebuddy.data.pojo.RMBProfile;
import com.croconaut.ratemebuddy.data.pojo.Status;
import com.croconaut.ratemebuddy.data.pojo.VoteUp;
import com.croconaut.ratemebuddy.ui.adapters.CommunicationListAdapter;
import com.croconaut.ratemebuddy.ui.adapters.EmoticonsAdapter;
import com.croconaut.ratemebuddy.ui.adapters.GridViewProfileAdapter;
import com.croconaut.ratemebuddy.ui.adapters.PeopleDialogViewAdapter;
import com.croconaut.ratemebuddy.ui.views.dialogs.DownloadFileDialog;
import com.croconaut.ratemebuddy.ui.views.dialogs.FileDialog;
import com.croconaut.ratemebuddy.ui.views.dialogs.UploadFileDialog;
import com.croconaut.ratemebuddy.ui.views.transformation.CircleTransform;
import com.croconaut.ratemebuddy.utils.CommonUtils;
import com.croconaut.ratemebuddy.utils.EmoticonSupportHelper;
import com.croconaut.ratemebuddy.utils.ProfileUtils;
import com.croconaut.ratemebuddy.utils.ThemeUtils;
import com.croconaut.ratemebuddy.utils.pojo.UIMessage;
import com.croconaut.ratemebuddy.utils.pojo.UIMessageAttachment;
import com.croconaut.ratemebuddy.utils.pojo.profiles.MyProfile;
import com.croconaut.ratemebuddy.utils.pojo.profiles.Profile;
import com.croconaut.ratemebuddy.utils.XorString;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class CommunicationActivity extends WifonActivity implements CptProcessor {

    public static final String EXTRA_TARGET_CROCO_ID = "targetCrocoID";
    public static final String EXTRA_TARGET_MESSAGE_ID = "targetMessageId";
    public static final String EXTRA_SHOW_PREDEFINED_MSG = "showPredefinedMsg";
    public static final String EXTRA_SHOW_DIALOG_MSG = "showDialog";


    private static final int FILE_REQUEST_CODE = 500;
    private static final int IMAGE_REQUEST_CODE = 294;
    private static final int MESSAGE_SCROLL_OFFSET = 5;
    private static final String TAG = CommunicationActivity.class.getName();

    private AppData appData;
    private EditText editTextToSend;
    private CommunicationListAdapter adapter;
    private Profile remoteProfile;
    private ListView messagesListView;
    private TextView tvStatus, tvName, tvLikes, tvFriendsHint;
    private TextView tvComments;

    private RelativeLayout statusContainer;
    private RelativeLayout emoticonsLayout;
    private ImageButton btnSendVote;
    private ImageButton btnComments;
    private ImageButton btnSendMessage;
    private List<UIMessage> adapterData;
    private View header;
    private ImageView ivPhoto, ivCover;
    private GridView mFriends;
    private View newMessageHint;


    private boolean sendingFile = false;
    private Uri fileUri, mSelectedImageUri;
    private boolean fileIsImage;

    private ActionMode mActionMode;
    private int mActionClickPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_communication);
        appData = (AppData) getApplication();

        View iconsLayout = findViewById(R.id.iconsLayout);
        assert iconsLayout != null;
        iconsLayout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.material_white));

        ImageButton btnAttachFile = (ImageButton) findViewById(R.id.btnCommSendFile);
        if (btnAttachFile != null)
            btnAttachFile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setType("*/*");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    if (Build.VERSION.SDK_INT < 19) {
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                    } else {
                        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                    }
                    startActivityForResult(Intent.createChooser(intent, null), FILE_REQUEST_CODE);
                }
            });

        ImageButton btnSendImageGallery = (ImageButton) findViewById(R.id.btnCommSendGallery);
        if (btnSendImageGallery != null)
            btnSendImageGallery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    if (Build.VERSION.SDK_INT < 19) {
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                    } else {
                        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                    }
                    startActivityForResult(Intent.createChooser(intent, null), IMAGE_REQUEST_CODE);
                }
            });

        ImageButton btnSendImagePhoto = (ImageButton) findViewById(R.id.btnSendImage);
        if (btnSendImagePhoto != null)
            btnSendImagePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    mSelectedImageUri = CommonUtils.getOutputMediaFile(mContext);
                    takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mSelectedImageUri);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(takePictureIntent, IMAGE_REQUEST_CODE);
                    }

                }
            });

        remoteProfile = (Profile) profileUtils.findProfile(getIntent().getStringExtra(EXTRA_TARGET_CROCO_ID));

        initializeHeaderWithDrawer(remoteProfile.getName(), false);

        editTextToSend = (EditText) findViewById(R.id.editTextMessageToSend);

        if (getIntent().getIntExtra(EXTRA_TARGET_MESSAGE_ID, 0) != 0) {
            int messageId = getIntent().getIntExtra(EXTRA_TARGET_MESSAGE_ID, 0);
            UIMessage message = appData.getUiMessageDataSource().getMessageById(messageId);

            if (message != null) {
                if (!message.hasAttachment()) {
                    editTextToSend.setText(message.getContent());
                } else {
                    UIMessageAttachment attachment = message.getUiMessageAttachment();
                    editTextToSend.setText(attachment.getName());
                    assignFile(
                            message.getFileUri(),
                            attachment.getStorageType() == UIMessageAttachment.PUBLIC_IMAGE
                    );
                }
            }
        }


        editTextToSend.setTypeface(tRegular);
        editTextToSend.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (sendingFile) {
                    sendingFile = false;
                    editTextToSend.setText("");
                }
            }
        });

        messagesListView = (ListView) findViewById(R.id.messagesListView);
        btnSendMessage = (ImageButton) findViewById(R.id.buttonSendMessage);
        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
                emoticonsLayout.setVisibility(View.GONE);
            }
        });

        emoticonsLayout = (RelativeLayout) findViewById(R.id.emoticonsLayout);

        GridView emoticons = (GridView) findViewById(R.id.emoticons);
        if (emoticons != null)
            emoticons.setAdapter(new EmoticonsAdapter(this, editTextToSend));

        ImageButton btnEmoticons = (ImageButton) findViewById(R.id.btnSelectEmoticon);
        if (btnEmoticons != null)
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

        attachHeader(true);

        newMessageHint = findViewById(R.id.newMessageHint);

        newMessageHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newMessageHint.setVisibility(View.GONE);
                messagesListView.smoothScrollToPosition(messagesListView.getCount() - 1);
            }
        });

        messagesListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (newMessageHint.getVisibility() == View.VISIBLE && (totalItemCount - visibleItemCount <= firstVisibleItem)) {
                    newMessageHint.setVisibility(View.GONE);
                }
            }
        });


//        messagesListView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
//            @Override
//            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
//                MenuInflater inflater = getMenuInflater();
//                inflater.inflate(R.menu.message_context_menu, menu);
//            }
//        });

        messagesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                mActionMode = startActionMode(new ActionBarCallBack(position));
                mActionClickPosition = position;
                return true;
            }
        });

        adapterData = appData.getUiMessageDataSource().getMessagesByUserId(remoteProfile.getCrocoId());
        adapter = new CommunicationListAdapter(CommunicationActivity.this, appData, adapterData, remoteProfile);
        messagesListView.setAdapter(adapter);
        messagesListView.setDivider(null);
        adapter.startSmileTask();


        if (adapterData.isEmpty() && remoteProfile.getType() != Profile.FAVOURITE) {
            //TODO Miro: Case 592: Refresh profilu ak je uplne cudzi
            remoteProfile.getIdent(); // <-- CrocoID
        }

        checkForExtras(this.getIntent());
    }


    private void attachHeader(boolean addHeader) {
        if (addHeader) {
            header = getLayoutInflater().inflate(R.layout.profile_list_view_header, null);
            statusContainer = (RelativeLayout) header.findViewById(R.id.statusContainer);
            tvName = (TextView) header.findViewById(R.id.tvName);
            ivPhoto = (ImageView) header.findViewById(R.id.ivImage);
            ivCover = (ImageView) header.findViewById(R.id.ivCover);
            tvStatus = (TextView) header.findViewById(R.id.tvStatus);
            tvLikes = (TextView) header.findViewById(R.id.tvLikes);
            btnSendVote = (ImageButton) header.findViewById(R.id.btnSendVote);
            btnComments = (ImageButton) header.findViewById(R.id.btnComments);
            tvComments = (TextView) header.findViewById(R.id.tvComments);
            mFriends = (GridView) header.findViewById(R.id.gvFriends);
            tvFriendsHint = (TextView) header.findViewById(R.id.tvFriendsHint);
        }

        Glide.with(this)
                .load(ThemeUtils.getBgCoverResId(prefs))
                .asBitmap()
                .thumbnail(0.3f)
                .into(ivCover);

        tvName.setShadowLayer(5, 2, 2, ContextCompat.getColor(mContext, R.color.material_blue_grey_700));
        tvName.setTypeface(tSemiBold);
        tvName.setText(remoteProfile.getName());


        if (remoteProfile.getFriendsList() != null && !remoteProfile.getFriendsList().isEmpty()) {

            tvFriendsHint.setVisibility(View.VISIBLE);
            tvFriendsHint.setText(mRes.getString(
                    R.string.header_friends_count_hint,
                    remoteProfile.getFriendsList().size()
                    )
            );

            mFriends.setVisibility(View.VISIBLE);
            GridViewProfileAdapter friendAdapter = new
                    GridViewProfileAdapter(
                    mContext,
                    remoteProfile,
                    appData,
                    remoteProfile.getFriendsList().size() < 5
                            ? remoteProfile.getFriendsList()
                            : remoteProfile.getFriendsList().subList(0, 5),
                    remoteProfile.getFriendsList().size(),
                    DisplayProfilesActivity.DISPLAY_FRIENDS
            );
            mFriends.setAdapter(friendAdapter);
        } else {
            tvFriendsHint.setVisibility(View.GONE);
            mFriends.setVisibility(View.GONE);
        }

        Glide.with(this)
                .load(remoteProfile.getThumbUri())
                .asBitmap()
                .placeholder(ProfileUtils.getTextDrawableForProfile(remoteProfile))
                .error(ProfileUtils.getTextDrawableForProfile(remoteProfile))
                .thumbnail(0.2f)
                .transform(new CircleTransform(mContext))
                .into(ivPhoto);

        ivPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (remoteProfile.getThumbUri() != null)
                    profileUtils.showImage(mContext, remoteProfile.getThumbUri());
            }
        });


        final Intent commentIntent = new Intent(CommunicationActivity.this, CommentActivity.class);
        commentIntent.putExtra(CommentActivity.EXTRA_CROCO_ID, remoteProfile.getIdent());

        btnComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(commentIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        statusContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(commentIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        if (remoteProfile.getStatus() != null && remoteProfile.getStatus().getContent() != null) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.addRule(RelativeLayout.BELOW, R.id.buttonsContainer);
            tvFriendsHint.setLayoutParams(params);


            tvStatus.setVisibility(View.VISIBLE);
            tvLikes.setVisibility(View.VISIBLE);
            tvComments.setVisibility(View.VISIBLE);
            statusContainer.setVisibility(View.VISIBLE);
            btnSendVote.setVisibility(View.VISIBLE);
            btnComments.setVisibility(View.VISIBLE);

            tvStatus.setText(new EmoticonSupportHelper()
                    .getSmiledText(
                            CommunicationActivity.this,
                            remoteProfile.getStatus().getContent()
                    )
            );

            tvLikes.setText(String.valueOf(remoteProfile.getStatus().getVotes().size()));
            tvComments.setText(String.valueOf(remoteProfile.getStatus().getComments().size()));
        } else {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.addRule(RelativeLayout.BELOW, R.id.ivImage);
            tvFriendsHint.setLayoutParams(params);

            tvStatus.setVisibility(View.GONE);
            tvLikes.setVisibility(View.GONE);
            tvComments.setVisibility(View.GONE);
            statusContainer.setVisibility(View.GONE);
            btnSendVote.setVisibility(View.GONE);
            btnComments.setVisibility(View.GONE);
        }

        tvStatus.setTypeface(tSemiBold);
        tvLikes.setTypeface(tSemiBold);
        tvComments.setTypeface(tSemiBold);

        final MyProfile myProfile = MyProfile.getInstance(this);
        if (remoteProfile.getStatus() != null) {
            for (VoteUp voteUp : remoteProfile.getStatus().getVotes()) {
                if (voteUp.getProfileId().equals(myProfile.getProfileId())) {
                    btnSendVote.setEnabled(false);
                    break;
                }
            }
        }

        btnSendVote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSendVote.setEnabled(false);
                sendVoteUp(remoteProfile);
                attachHeader(false);
            }
        });

        //send vote set to not enabled when we already sended vote
        if (remoteProfile.getStatus() != null
                && remoteProfile.getStatus().isProfileIdPresent(myProfile.getProfileId())) {
            btnSendVote.setEnabled(false);
        } else {
            btnSendVote.setEnabled(true);
        }

        if (addHeader)
            messagesListView.addHeaderView(header);

        if (remoteProfile.getUnreadMessages().size() > 0) {
            // scrollToBottom();
            messagesListView.clearFocus();
            messagesListView.post(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "Scrolling to bottom " + adapter.getCount());
                    messagesListView.setSelection(adapter.getCount() - 1);
                }
            });
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.setIntent(intent);
        checkForExtras(intent);
    }

    private boolean checkForExtras(Intent intent) {
        Log.e(TAG, "Checking extras");
        Bundle extras = intent.getExtras();
        boolean scrolled = false;


        if (extras == null)
            return false;


        UIMessage scrollTo = null;
        if (remoteProfile.getUnreadMessages().size() > 0) {
            scrollTo = remoteProfile.getUnreadMessages().get(0);
            for (UIMessage message : remoteProfile.getUnreadMessages()) {
                if (message.getCreationTime() < scrollTo.getCreationTime())
                    scrollTo = message;
            }
        }

        final UIMessage uiScrollTo = scrollTo;
        if (uiScrollTo != null && appData.getProfileDataSource().getUnreadProfiles().contains(remoteProfile)) {
            messagesListView.clearFocus();
            messagesListView.post(new Runnable() {
                @Override
                public void run() {
                    int position = adapter.getPosition(uiScrollTo);
                    messagesListView.setSelection(position == -1 ? messagesListView.getCount() - 1 : position);
                }
            });
            scrolled = true;
        }


        // do we need to clear notif profiles
        if (extras.getBoolean(CommonUtils.EXTRA_CLEAR_NOTIF_MESS_PROFILES)) {
            appData.getProfileDataSource().updateUnread(remoteProfile.getCrocoId(), 0, new ArrayList<UIMessage>());

            List<Profile> notifMessProfile = appData.getProfileDataSource().getUnreadProfiles();
            if (notifMessProfile.isEmpty()) {
                Notification.clearNotification(this, MessageNotification.MESSAGE_NOTIF_ID);
            } else {
                new MessageNotification(appData, true);
            }

        }

        if (extras.getBoolean(EXTRA_SHOW_PREDEFINED_MSG)) {

            editTextToSend.setText(
                    mRes.getString(
                            R.string.predifined_et_text_content,
                            remoteProfile.getName(),
                            MyProfile.getInstance(mContext).getName()
                    )
            );

            getIntent().removeExtra(EXTRA_SHOW_PREDEFINED_MSG);
        }

        if (intent.hasExtra(EXTRA_SHOW_DIALOG_MSG)) {
            long messageId = intent.getLongExtra(CommunicationActivity.EXTRA_SHOW_DIALOG_MSG, -1);
            Log.e(TAG, "Displaying dialog for ID: " + messageId);
            UIMessage uiMessage = appData.getUiMessageDataSource().getUIMessage(messageId);

            intent.removeExtra(EXTRA_SHOW_DIALOG_MSG);

            if (uiMessage != null) {
                Log.e(TAG, "Displaying dialog");
                boolean outgoing = uiMessage.getSendType() != UIMessage.INCOMING;

                if (outgoing) {
                    UploadFileDialog fileDialog = new UploadFileDialog(
                            CommunicationActivity.this,
                            appData,
                            remoteProfile,
                            uiMessage.getUiAttachmentId()
                    );

                    fileDialog.show();
                } else {
                    DownloadFileDialog downloadFileDialog = new DownloadFileDialog(
                            CommunicationActivity.this,
                            appData,
                            remoteProfile,
                            uiMessage.getUiAttachmentId()
                    );
                    downloadFileDialog.show();
                }
            } else Log.e(TAG, "EXTRA_SHOW_DIALOG_MSG, uiMessage is NULL");
        } else Log.e(TAG, "EXTRA_SHOW_DIALOG_MSG not pres");

        return scrolled;
    }

    @Override
    public boolean process(Intent cptIntent) throws IOException, ClassNotFoundException {
        super.process(cptIntent);
        switch (cptIntent.getAction()) {
            case Communication.ACTION_MESSAGE_ARRIVED:
                final IncomingMessage message = cptIntent.getParcelableExtra(Communication.EXTRA_MESSAGE_ARRIVED);
                Serializable data = message.getPayload().getAppData();

                if (message.getFrom() == null || remoteProfile.getIdent() == null)
                    return false;


                if (data instanceof Status && message.getFrom().equals(remoteProfile.getIdent())) {
                    remoteProfile = (Profile) profileUtils.findProfile(getIntent().getStringExtra(EXTRA_TARGET_CROCO_ID));
                    attachHeader(false);
                    return true;
                } else if (data instanceof RMBProfile && message.getFrom().equals(remoteProfile.getIdent())) {
                    remoteProfile = (Profile) profileUtils.findProfile(getIntent().getStringExtra(EXTRA_TARGET_CROCO_ID));
                    attachHeader(false);
                    initializeHeaderWithDrawer(remoteProfile.getName(), false);

                    int getLastVisiblePosition = messagesListView.getFirstVisiblePosition();
                    adapterData = appData.getUiMessageDataSource().getMessagesByUserId(remoteProfile.getCrocoId());
                    adapter = new CommunicationListAdapter(CommunicationActivity.this, appData, adapterData, remoteProfile);
                    messagesListView.setAdapter(adapter);
                    messagesListView.setDivider(null);
                    adapter.startSmileTask();
                    messagesListView.setSelection(getLastVisiblePosition);
                    return true;
                } else if (data instanceof Message && message.getFrom().equals(remoteProfile.getIdent())) {
                    processIntent(cptIntent, false);
                    scrollIfFacingLastFiveMessages();
                    return true;
                } else if ((data instanceof Attachment || !message.getPayload().getAttachments().isEmpty())
                        && message.getFrom().equals(remoteProfile.getIdent())) {
                    processIntent(cptIntent, false);
                    scrollIfFacingLastFiveMessages();
                    return true;
                }
                break;
            case Communication.ACTION_MESSAGE_ATTACHMENT_DOWNLOAD_CONFIRMED:
            case Communication.ACTION_MESSAGE_ATTACHMENT_DOWNLOAD_CANCELLED:
            case Communication.ACTION_MESSAGE_ATTACHMENT_UPLOAD_CANCELLED:
            case Communication.ACTION_MESSAGE_ATTACHMENT_UPLOAD_CONFIRMED:
                if (adapter != null) {
                    Log.e(TAG, "Updating on Action: " + cptIntent.getAction());
                    adapter.updateDialog();
                }
                break;
            case Communication.ACTION_MESSAGE_ATTACHMENT_DOWNLOADED:
            case Communication.ACTION_MESSAGE_ATTACHMENT_UPLOADED_TO_APP_SERVER:
            case Communication.ACTION_MESSAGE_ATTACHMENT_UPLOADED_TO_RECIPIENT:
                if (adapter != null) adapter.closeDialog();
            case Communication.ACTION_MESSAGE_ATTACHMENT_DOWNLOADING:
            case Communication.ACTION_MESSAGE_ATTACHMENT_UPLOADING_TO_RECIPIENT:
            case Communication.ACTION_MESSAGE_ATTACHMENT_UPLOADING_TO_APP_SERVER:
                if (adapter != null) {
                    Log.e(TAG, "Updating on Action: " + cptIntent.getAction());
                    adapter.updateDialog();
                }
                return processIntent(cptIntent, true);
            case Communication.ACTION_MESSAGE_ACKED:
            case Communication.ACTION_MESSAGE_SENT:
            case Communication.ACTION_MESSAGE_ATTACHMENT_DELIVERED:
            case Communication.ACTION_MESSAGE_DELETED:
            case Communication.ACTION_MESSAGE_ATTACHMENT_REQUEST_EXPIRED:
                processIntent(cptIntent, true);
                break;
        }

        return false;
    }

    private void scrollIfFacingLastFiveMessages() {
        int lastVisiblePosition = messagesListView.getLastVisiblePosition();

        if (lastVisiblePosition + MESSAGE_SCROLL_OFFSET > adapter.getCount()) {
            messagesListView.setSelection(adapter.getCount());
        } else if (newMessageHint.getVisibility() != View.VISIBLE) {
            newMessageHint.setVisibility(View.VISIBLE);
        }
    }

    private void fullRefreshWithPosition(boolean scroll) {
        int getLastVisiblePosition = messagesListView.getLastVisiblePosition();
        adapterData = appData.getUiMessageDataSource().getMessagesByUserId(remoteProfile.getCrocoId());
        adapter = new CommunicationListAdapter(CommunicationActivity.this, appData, adapterData, remoteProfile);
        messagesListView.setAdapter(adapter);
        messagesListView.setDivider(null);
        adapter.startSmileTask();

        if (scroll)
            messagesListView.setSelection(getLastVisiblePosition);
    }

    private boolean processIntent(Intent cptIntent, boolean update) {
        final long id = cptIntent.getLongExtra(Communication.EXTRA_MESSAGE_ID, -1);
        UIMessage uiMessage = appData.getUiMessageDataSource().getUIMessageByRemoteOrAttachmentId(id);


        if (uiMessage == null) {
            Log.e(TAG, "Message " + id + " is not in DB");
            return false;
        }

        if (!uiMessage.getCrocoId().equals(remoteProfile.getCrocoId())) {
            Log.e(TAG, "Message  for another profile!");
            return false;
        }

        if (update)
            updateMessage(uiMessage);
        else
            insertMessage(uiMessage);
        return true;
    }

    private void updateMessage(UIMessage message) {
        adapter.setNotifyOnChange(false);
        adapter.remove(message);
        adapterData.remove(message);
        adapter.setNotifyOnChange(true);

        insertMessage(message);
    }

    private void insertMessage(UIMessage message) {
        adapterData.add(message);
        Collections.sort(adapterData);

        adapter.notifyDataSetChanged();
        adapter.startSmileTask();
    }


    @Override
    protected void onPause() {
        super.onPause();
        MessageNotification.currentProfile = null;
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean scroll = !checkForExtras(this.getIntent());

        remoteProfile = (Profile) profileUtils.findProfile(remoteProfile.getIdent(), remoteProfile.getName());
        List<UIMessage> newData = appData.getUiMessageDataSource().getMessagesByUserId(remoteProfile.getCrocoId());
        if (newData.size() != adapterData.size()) {
            fullRefreshWithPosition(scroll);
        }

        MessageNotification.currentProfile = remoteProfile;
        attachHeader(false);
    }

    public void sendMessage() {
        try {
            String text = editTextToSend.getText().toString();

            if (text.length() == 0) {
                btnSendMessage.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake_error));
                return;
            }

            // -Xi- msg coding modification
            int flags = Base64.NO_WRAP | Base64.NO_PADDING;
            String textB64 = Base64.encodeToString(text.getBytes(), flags);
            String passB64 = "XiB1cDNyLVQ0am7DqS5IM3NsMCxQcmUvS8OzRG92NG5pZSpTcHLDoXYr"; //Base64.encodeToString(pass.getBytes(), Base64.DEFAULT);
            XorString xs = new XorString();
            String codedText = xs.xoring(textB64, passB64);

            UIMessage uiMessage;
            if (sendingFile) {
                if (remoteProfile.isShowUploadDialog()) {
                    FileDialog fileDialog = new UploadFileDialog(
                            CommunicationActivity.this,
                            appData,
                            remoteProfile,
                            fileIsImage,
                            fileUri
                    );


                    fileDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            adapterData = appData.getUiMessageDataSource().getMessagesByUserId(remoteProfile.getCrocoId());
                            adapter = new CommunicationListAdapter(CommunicationActivity.this, appData, adapterData, remoteProfile);
                            messagesListView.setAdapter(adapter);
                            messagesListView.setDivider(null);
                            adapter.startSmileTask();
                            afterMessageSent();
                        }
                    });

                    fileDialog.show();
                } else {
                    uiMessage = sendFile(remoteProfile.getCrocoId(), fileUri, fileIsImage);
                    sendingFile = false;

                    uiMessage = appData.getUiMessageDataSource().getUIMessage(uiMessage.getCreationTime());
                    insertMessage(uiMessage);
                    afterMessageSent();
                }
            } else {
                MyProfile myProfile = MyProfile.getInstance(mContext);
                Message message = new Message(
                        codedText, //text,
                        myProfile.getProfileId(),
                        myProfile.getName()
                );

                OutgoingMessage serviceMsg = new OutgoingMessage(remoteProfile.getCrocoId(),
                        new OutgoingPayload(message));

                uiMessage = new UIMessage.Builder(remoteProfile.getCrocoId(), text,
                        serviceMsg.getCreationDate().getTime(), UIMessage.WAITING)
                        .receivedTime(serviceMsg.getCreationDate().getTime())
                        .build();
                appData.getUiMessageDataSource().insertMessage(uiMessage);

                Communication.newMessage(this, serviceMsg);

                uiMessage = appData.getUiMessageDataSource().getUIMessage(uiMessage.getCreationTime());
                insertMessage(uiMessage);
                afterMessageSent();
            }

            CommonUtils.hideKeyboard(this);
        } catch (IOException e) {
            Log.e(TAG, "sendMessage", e);
        }
    }

    private void afterMessageSent() {
        editTextToSend.setText("");
        scrollToBottom();
        checkForUnknown(remoteProfile);
    }

    private void scrollToBottom() {
//        messagesListView.clearFocus();
//        messagesListView.post(new Runnable() {
//            @Override
//            public void run() {
//                Log.e(TAG, "Scrolling to bottom " + adapter.getCount());
//                messagesListView.setSelection(adapter.getCount() - 1);
//            }
//        });

        if (adapter != null) {
            Log.e(TAG, "Scrolling to bottom " + adapter.getCount());
            messagesListView.setAdapter(adapter);
            messagesListView.setSelection(adapter.getCount() >= 0 ? adapter.getCount() - 1 : 0);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
            case R.id.menuStar:
                if (remoteProfile.getType() != Profile.FAVOURITE) {
                    remoteProfile.setType(Profile.FAVOURITE);
                    updateOrInsertProfile(remoteProfile);
                    menuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_star_white_36dp));

                    Communication.changeUserTrustLevel(
                            mContext,
                            remoteProfile.getCrocoId(),
                            Communication.USER_TRUST_LEVEL_TRUSTED
                    );

                    Toast.makeText(this, mRes.getString(R.string.toast_menu_nearby_favourites, remoteProfile.getName()), Toast.LENGTH_LONG).show();
                } else {
                    remoteProfile.setType(Profile.UNKNOWN);
                    updateOrInsertProfile(remoteProfile);
                    menuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_star_outline_white_36dp));

                    Communication.changeUserTrustLevel(
                            mContext,
                            remoteProfile.getCrocoId(),
                            Communication.USER_TRUST_LEVEL_NORMAL
                    );

                    Toast.makeText(this, mRes.getString(R.string.toast_menu_favourites_removed, remoteProfile.getName()), Toast.LENGTH_LONG).show();
                }

                try {
                    MyProfile myProfile = MyProfile.getInstance(mContext);
                    ProfileUtils.createAndSendRmbProfile(
                            appData,
                            myProfile,
                            myProfile.getThumbUri()
                    );
                } catch (IOException e) {
                    Log.e(TAG, "Cannot create RMPProfile: ", e);
                }

                adapter.setRemoteProfile(remoteProfile);
                adapter.notifyDataSetChanged();
                supportInvalidateOptionsMenu();
                return true;
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    // we really need to remove this value, i mean, really :D
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        return true;
    }

    private ProgressBar mTempProgressBar = null;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.menuStar);

        if (remoteProfile.getProfileId() != null) {
            if (mTempProgressBar != null) mTempProgressBar.setVisibility(View.GONE);
            item.setVisible(true);
            if (remoteProfile.getType() == Profile.FAVOURITE)
                item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_star_white_36dp));
        } else {
            item.setVisible(false);

            if (mTempProgressBar != null) {
                Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.TOP | Gravity.RIGHT);

                mTempProgressBar = new ProgressBar(mContext);
                mTempProgressBar.setIndeterminate(true);

                toolbar.addView(mTempProgressBar, layoutParams);
            }
        }

        return true;
    }

    private void deleteMessage(UIMessage clickedMessage) {
        appData.getUiMessageDataSource().deleteMessageByID(clickedMessage.getId());

        String toastMessage = null;

        switch (clickedMessage.getSendType()) {
            case UIMessage.WAITING:
                Communication.deleteMessage(
                        mContext,
                        clickedMessage.hasAttachment() ? clickedMessage.getUiAttachmentId()
                                : clickedMessage.getCreationTime()
                );

                toastMessage = mRes.getString(R.string.toast_delete);
                break;
            case UIMessage.INCOMING:
                toastMessage = mRes.getString(R.string.toast_delete);
                break;
            case UIMessage.SENT_TO_INTERNET:
                toastMessage = mRes.getString(
                        R.string.toast_delete_already_send,
                        mRes.getString(R.string.toast_delete_server)
                );
                break;
            case UIMessage.SENT_TO_OTHER_DEVICE:
                toastMessage = mRes.getString(
                        R.string.toast_delete_already_send,
                        mRes.getString(R.string.toast_delete_other_device)
                );
                break;
            case UIMessage.SENT_TO_RECIPIENT:
            case UIMessage.ACKED:
                toastMessage = mRes.getString(
                        R.string.toast_delete_already_send,
                        mRes.getString(R.string.toast_delete_recipient)
                );
                break;
        }


        if (toastMessage != null)
            Toast.makeText(
                    CommunicationActivity.this,
                    toastMessage,
                    Toast.LENGTH_LONG
            ).show();

        adapter.remove(clickedMessage);
        adapter.notifyDataSetChanged();
    }

    private void showDialogResend(final UIMessage clickedMessage) {
        List<Profile> profiles =
                appData.getProfileDataSource().getProfilesByType(Profile.FAVOURITE);

        if (profiles.isEmpty()) {
            Snackbar.make(
                    findViewById(R.id.activity_communication_bottom_layout),
                    mRes.getString(R.string.emptyFavourites),
                    Snackbar.LENGTH_LONG
            ).show();

            return;
        }

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(CommunicationActivity.this);
        LayoutInflater mInflater = (LayoutInflater) mContext
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = mInflater.inflate(R.layout.dialog_people_choose, null);


        final ListView listView = (ListView) view.findViewById(R.id.lvDialogList);
        PeopleDialogViewAdapter peopleDialogViewAdapter = new PeopleDialogViewAdapter(
                mContext,
                profiles
        );
        listView.setAdapter(peopleDialogViewAdapter);

        RelativeLayout rlEmptyView = (RelativeLayout) view.findViewById(R.id.emptylistViewNearby);
        TextView tvEmptyView = (TextView) view.findViewById(R.id.tvEmptylistViewNearby);
        tvEmptyView.setTypeface(tLight);
        tvEmptyView.setText(mRes.getString(R.string.emptyFavourites));

        if (profiles.isEmpty()) {
            listView.setVisibility(View.GONE);
            rlEmptyView.setVisibility(View.VISIBLE);
        }

        alertDialog.setCancelable(true);
        alertDialog.setView(view);
        final Dialog dialog = alertDialog.show();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog.cancel();

                Profile profile = (Profile) listView.getItemAtPosition(position);

                Intent intent = new Intent(mContext, CommunicationActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(CommunicationActivity.EXTRA_TARGET_CROCO_ID, profile.getIdent());
                intent.putExtra(CommunicationActivity.EXTRA_TARGET_MESSAGE_ID, clickedMessage.getId());
                appData.startActivity(intent);
            }
        });
    }

    private void assignFile(Uri uri, boolean isImage) {
        fileUri = uri;
        fileIsImage = isImage;
        sendingFile = true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        Log.e(TAG, "Inside on result");
        if (resultCode == RESULT_OK) {
            if (requestCode == FILE_REQUEST_CODE || requestCode == IMAGE_REQUEST_CODE) {
                Uri uri = null;

                if (requestCode == IMAGE_REQUEST_CODE && mSelectedImageUri != null) {
                    uri = getImageContentUri(mContext, new File(mSelectedImageUri.getPath()));
                } else if (result != null) {
                    uri = result.getData();
                }

                //uri is still null
                if (uri == null) {
                    Toast.makeText(
                            mContext,
                            getResources().getString(R.string.toast_image_error),
                            Toast.LENGTH_LONG)
                            .show();
                    return;
                }

                if (Build.VERSION.SDK_INT >= 19) {
                    // All selected documents are returned to the calling application with persistable
                    // read and write permission grants. If you want to maintain access to the documents
                    // across device reboots, you need to explicitly take the persistable permissions
                    // using takePersistableUriPermission(Uri, int).
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }

                Cursor cursor = getContentResolver().query(uri, null, null, null, null, null);
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        String displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                        sendingFile = false;
                        editTextToSend.setText(displayName);
                        assignFile(uri, requestCode == IMAGE_REQUEST_CODE);
                        scrollToBottom();
                    }
                } finally {
                    if (cursor != null) cursor.close();
                    mSelectedImageUri = null;
                }
            }
        } else {
            Log.e(TAG, "Bad result code: " + resultCode);
        }
    }

    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    private class ActionBarCallBack implements ActionMode.Callback {

        private int clickedPosition;

        public ActionBarCallBack(int clickedPosition) {
            this.clickedPosition = clickedPosition;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            final UIMessage clickedMessage = adapter.getItem(
                    mActionClickPosition == 0 ? mActionClickPosition : mActionClickPosition - 1
            );

            switch (item.getItemId()) {
                case R.id.messageMenuDelete:
                    deleteMessage(clickedMessage);
                    mActionMode.finish();
                    return true;
                case R.id.messageMenuDetail:
                    Intent i = new Intent(mContext, MessageDetailActivity.class);
                    i.putExtra(MessageDetailActivity.UI_MESSAGE_ID_TAG, clickedMessage.getId());
                    startActivity(i);
                    mActionMode.finish();
                    return true;
                case R.id.messageMenuCopy:
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                        android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        clipboard.setText(clickedMessage.getContent());
                    } else {
                        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        android.content.ClipData clip = android.content.ClipData.newPlainText("message text", clickedMessage.getContent());
                        clipboard.setPrimaryClip(clip);
                    }
                    Toast.makeText(mContext, R.string.message_menu_copy_toast, Toast.LENGTH_LONG).show();
                    mActionMode.finish();
                    return true;
                case R.id.messageMenuResend:
                    showDialogResend(clickedMessage);
                    mActionMode.finish();
                    return true;
            }
            return false;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.message_context_menu, menu);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            menu.findItem(R.id.messageMenuDetail).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.findItem(R.id.messageMenuDelete).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.findItem(R.id.messageMenuResend).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.findItem(R.id.messageMenuCopy).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            final UIMessage clickedMessage = adapter.getItem(
                    clickedPosition == 0 ? clickedPosition : clickedPosition - 1);

            if (clickedMessage.hasAttachment()) {
                boolean isOutgoing = clickedMessage.getSendType() != UIMessage.INCOMING;

                if ((isOutgoing && !clickedMessage.getUiMessageAttachment().hasFinishedUpload())
                        || (!isOutgoing && !clickedMessage.getUiMessageAttachment().hasFinishedDownload()))
                    setItemsVisibility(menu, false);
                else
                    setItemsVisibility(menu, true);

            } else setItemsVisibility(menu, true);


            mode.setTitle(mRes.getString(R.string.action_back));
            return false;
        }
    }

    private void setItemsVisibility(Menu menu, boolean isVisible) {
        menu.findItem(R.id.messageMenuDetail).setVisible(isVisible);
        menu.findItem(R.id.messageMenuResend).setVisible(isVisible);
        menu.findItem(R.id.messageMenuCopy).setVisible(isVisible);
    }
}
