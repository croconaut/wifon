package com.croconaut.ratemebuddy.ui.adapters;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.croconaut.ratemebuddy.AppData;
import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.activities.MessageDetailActivity;
import com.croconaut.ratemebuddy.ui.views.dialogs.DownloadFileDialog;
import com.croconaut.ratemebuddy.ui.views.dialogs.FileDialog;
import com.croconaut.ratemebuddy.ui.views.dialogs.UploadFileDialog;
import com.croconaut.ratemebuddy.utils.CommonUtils;
import com.croconaut.ratemebuddy.utils.CustomClickableUrlSpan;
import com.croconaut.ratemebuddy.utils.EmoticonSupportHelper;
import com.croconaut.ratemebuddy.utils.ThemeManager;
import com.croconaut.ratemebuddy.utils.pojo.UIMessage;
import com.croconaut.ratemebuddy.utils.pojo.UIMessageAttachment;
import com.croconaut.ratemebuddy.utils.pojo.profiles.Profile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CommunicationListAdapter extends ArrayAdapter<UIMessage> {
    private static final String TAG = CommunicationListAdapter.class.getName();

    private final AppData appData;
    private final List<UIMessage> itemsList;
    private final LayoutInflater inflater;
    private final SimpleDateFormat dateFormat;
    private final SimpleDateFormat dayOfWeekFormat;
    private final ThemeManager theme;
    protected Typeface tRegular;
    private Profile remoteProfile;
    private final Context activityCtx;
    private FileDialog fileDialog;

    public CommunicationListAdapter(Context activityCtx, AppData appData, List<UIMessage> itemsArrayList, Profile remoteUser) {
        super(appData, R.layout.contact_list_item, itemsArrayList);

        tRegular = Typeface.createFromAsset(appData.getAssets(), "fonts/regular.ttf");

        inflater = LayoutInflater.from(appData);
        dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        dayOfWeekFormat = new SimpleDateFormat("EEEE dd. MM. yyyy", Locale.getDefault());

        this.theme = new ThemeManager(appData);
        this.appData = appData;
        this.itemsList = itemsArrayList;
        this.remoteProfile = remoteUser;
        this.activityCtx = activityCtx;
    }

    @Override
    public int getItemViewType(int position) {
        int INCOMING_TYPE = 0;
        int OUTGOING_TYPE = 1;
        return itemsList.get(position).getSendType() == UIMessage.INCOMING ? INCOMING_TYPE : OUTGOING_TYPE;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View cv, ViewGroup parent) {
        final UIMessage message = itemsList.get(position);

        boolean showDateStamp = position == 0;
        boolean outgoing = message.getSendType() != UIMessage.INCOMING;

        // cas spravy
        Date time = new Date(message.getCreationTime());
        String bracketTime = "";
        long t = outgoing ? message.getCreationTime() - message.getReceivedTime()
                : message.getReceivedTime() - message.getCreationTime();

        switch (message.getSendType()) {
            case UIMessage.ACKED:
                if (!message.hasAttachment()) {
                    t = -t;
                    bracketTime = getReadableTime(t);
                } else {
                    bracketTime = appData.getResources().getString(
                            R.string.attachment_seen_at,
                            dateFormat.format(message.getUiMessageAttachment().getTime())
                    );
                }
                break;
            case UIMessage.INCOMING:
                if (t <= -5000) {
                    Log.d(TAG, "Displaying error symbol.");
                } else {
                    if (t > -5000 && t < 5000) {
                        t = Math.abs(t);
                    }

                    //avoid 0s
                    if (t > -1000 && t < 1000) {
                        t = 1000;
                    }
                    bracketTime = getReadableTime(t);
                }

                break;
            case UIMessage.ATTACHMENT_DELIVERED:
                bracketTime = appData.getResources().getString(
                        R.string.attachment_delivered_at,
                        dateFormat.format(message.getUiMessageAttachment().getTime())
                );
                break;
            default:
                t = -t;
                bracketTime = appData.getString(R.string.message_not_delivered);
                break;
        }

        // porovnaj cas tejto a predchadzajucej spravy
        // podla toho sa zobrazi datestamp
        if (position > 0) {

            Date prevMessageDate = new Date(itemsList.get(position - 1).getCreationTime());

            Calendar c = Calendar.getInstance();
            c.setTime(time);
            int thisDay = c.get(Calendar.DAY_OF_YEAR);
            int thisYear = c.get(Calendar.YEAR);

            c.setTime(prevMessageDate);
            int prevDay = c.get(Calendar.DAY_OF_YEAR);
            int prevYear = c.get(Calendar.YEAR);

            showDateStamp = thisDay != prevDay || thisYear != prevYear;
        }

        // vyber layout podla odosielatela
        int layoutRes = outgoing ? R.layout.communication_list_item_outgoing : R.layout.communication_list_item_incoming;

        // holder na referencie vsetkych view v iteme
        final ViewHolder holder;

        // zrychlenie UI recyklaciou view
        if (cv == null) {
            cv = inflater.inflate(layoutRes, parent, false);
            holder = new ViewHolder();
            holder.messageText = (TextView) cv.findViewById(R.id.messageText);
            holder.messageTime = (TextView) cv.findViewById(R.id.messageTime);
            holder.messageState = (ImageView) cv.findViewById(R.id.messageState);
            holder.dateDivider = cv.findViewById(R.id.messageDayDivider);
            holder.dateStamp = (TextView) cv.findViewById(R.id.messageDayStamp);
            holder.bubbleLayout = cv.findViewById(R.id.bubbleLayout);
            holder.messagePhoto = (ImageView) cv.findViewById(R.id.messagePhoto);
            holder.errorSign = (ImageView) cv.findViewById(R.id.messageError);
            holder.dateBar = cv.findViewById(R.id.bottomDateBar);
            holder.messageText.setTypeface(tRegular);
            holder.messageTime.setTypeface(tRegular);
            holder.dateStamp.setTypeface(tRegular);
            holder.dateStamp.setTextColor(ContextCompat.getColor(getContext(), R.color.material_grey_600));

            //preview
            holder.preview = cv.findViewById(R.id.messagePreviewFile);
            holder.previewFilename = (TextView) cv.findViewById(R.id.tvName);
            holder.previewFileSize = (TextView) cv.findViewById(R.id.tvFileSize);
            holder.previewImage = (ImageView) cv.findViewById(R.id.ivMimeType);

            //file layout
            holder.fileLayout = cv.findViewById(R.id.fileLayout);
            holder.fileImage = (ImageView) cv.findViewById(R.id.fileImage);
            holder.filePreviewImage = (ImageView) cv.findViewById(R.id.filePreviewImage);
            holder.fileText = (TextView) cv.findViewById(R.id.fileText);

            cv.setTag(holder);
        } else {
            holder = (ViewHolder) cv.getTag();
        }

        // zobraz hlavicku s datumom
        if (showDateStamp) {
            holder.dateStamp.setText(dayOfWeekFormat.format(time).toUpperCase(Locale.getDefault()));
            holder.dateStamp.setVisibility(View.VISIBLE);
            holder.dateDivider.setVisibility(View.VISIBLE);
        } else {
            holder.dateStamp.setVisibility(View.GONE);
            holder.dateDivider.setVisibility(View.GONE);
        }


        if (message.hasAttachment()) {
            final UIMessageAttachment uiMessageAttachment = message.getUiMessageAttachment();

            Log.e(TAG, "Has attachment: " + uiMessageAttachment.getName() + " and isOutgoing: " + outgoing);
            Log.e(TAG, "Messate type is: " + message.getSendType());
            if (!uiMessageAttachment.hasFinishedDownload() && !outgoing) {
                holder.fileLayout.setVisibility(View.GONE);
                holder.messageText.setVisibility(View.GONE);
                holder.preview.setVisibility(View.VISIBLE);

                Glide.with(getContext())
                        .load(CommonUtils.getImageTypeRes(uiMessageAttachment.getType()))
                        .into(holder.previewImage);

                holder.previewFileSize.setText(
                        Formatter.formatShortFileSize(
                                appData,
                                uiMessageAttachment.getLength()
                        )
                );

                holder.previewFilename.setText(
                        getDisplayName(uiMessageAttachment)
                );

            } else {
                holder.fileLayout.setVisibility(View.VISIBLE);

                holder.messageText.setVisibility(View.GONE);
                holder.preview.setVisibility(View.GONE);

                holder.fileText.setText(
                        getDisplayName(uiMessageAttachment)
                );


                ContentResolver cr = appData.getContentResolver();
                String type = message.getFileUri() == null ?
                        null : cr.getType(message.getFileUri());

                if (type != null && (type.contains("image") || type.contains("video"))) {
                    try {
                        holder.fileImage.setVisibility(View.VISIBLE);
                        holder.filePreviewImage.setVisibility(View.GONE);

                        Glide.with(appData)
                                .load(message.getFileUri())
                                .asBitmap()
                                .centerCrop()
                                .thumbnail(0.2f)
                                .into(holder.fileImage);

                        holder.fileImage.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                openFile(message);
                            }
                        });

                        holder.fileImage.setOnLongClickListener(new OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                v.showContextMenu();
                                return true;
                            }
                        });

                    } catch (Exception e) {
                        Log.e(TAG, "Fatal error:", e);
                    }
                } else {
                    holder.fileImage.setVisibility(View.GONE);
                    holder.filePreviewImage.setVisibility(View.VISIBLE);

                    Glide.with(appData)
                            .load(CommonUtils.getDrawableForFile(getContext(), uiMessageAttachment.getUri()))
                            .asBitmap()
                            .centerCrop()
                            .thumbnail(0.2f)
                            .into(holder.filePreviewImage);

                    holder.filePreviewImage.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            openFile(message);
                        }
                    });

                    holder.filePreviewImage.setOnLongClickListener(new OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            v.showContextMenu();
                            return true;
                        }
                    });
                }
            }
        } else {
            holder.preview.setVisibility(View.GONE);
            holder.fileLayout.setVisibility(View.GONE);

            holder.messageText.setVisibility(View.VISIBLE);
            holder.messageText.setGravity(outgoing ? Gravity.RIGHT : Gravity.LEFT);

            holder.messageText.setText(
                    message.getSmileText() != null
                            ? message.getSmileText()
                            : message.getContent()
            );
        }


        holder.dateBar.setVisibility(bracketTime == null ? View.GONE : View.VISIBLE);

        if (bracketTime != null && bracketTime.isEmpty()) {
            holder.errorSign.setVisibility(View.VISIBLE);
        } else {
            holder.errorSign.setVisibility(View.GONE);
            bracketTime = bracketTime != null
                    ? "(" + bracketTime + ")"
                    : null;

        }

        if (bracketTime != null) {
            holder.messageTime.setVisibility(View.VISIBLE);
            String messageTimeString;
            if ((message.hasAttachment() && !outgoing)
                    || (outgoing && message.hasAttachment() && message.getSendType() < UIMessage.ACKED))
                messageTimeString = dateFormat.format(time);
            else
                messageTimeString = dateFormat.format(time) + bracketTime;

            holder.messageTime.setText(messageTimeString);
        } else holder.messageTime.setVisibility(View.GONE);


        UIMessage nextMessage = null;
        boolean nextOutGoing = false;
        try {
            nextMessage = itemsList.get(position + 1);
            nextOutGoing = nextMessage.getSendType() != UIMessage.INCOMING;
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }

        int bubbleRes;

        if (nextMessage != null && outgoing == nextOutGoing) {
            if (outgoing) {
                bubbleRes = R.drawable.sent_message_item_selector;
            } else {
                bubbleRes = theme.getIncomingMessageSelector();
                holder.messagePhoto.setVisibility(View.INVISIBLE);
            }
            holder.bubbleLayout.setBackgroundResource(bubbleRes);
        } else {
            if (outgoing) {
                bubbleRes = R.drawable.sent_message_item_selector;
            } else {
                bubbleRes = theme.getIncomingMessageSelector();
                holder.messagePhoto.setVisibility(View.VISIBLE);
                Glide.with(getContext())
                        .load(remoteProfile.getThumbUri())
                        .asBitmap()
                        .into(holder.messagePhoto);
            }

            holder.bubbleLayout.setBackgroundResource(bubbleRes);
        }

        holder.messageText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setTag(null);
                onViewClicked(message);
            }
        });

        // po kliknuti na spravu zobraz detaily
        holder.bubbleLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onViewClicked(message);
            }
        });

        holder.messageText.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                v.setTag(CustomClickableUrlSpan.TAG_LONG_CLICK);
                v.showContextMenu();
                return true;
            }
        });

        holder.bubbleLayout.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                v.showContextMenu();
                return true;
            }
        });


        if (outgoing) {
            int stateRes;
            switch (message.getSendType()) {
                case UIMessage.ACKED:
                    stateRes = !message.hasAttachment()
                            ? R.drawable.ic_done_all_white_18dp
                            : R.drawable.ic_icon_eye;
                    break;
                case UIMessage.SENT_TO_RECIPIENT:
                    stateRes = R.drawable.ic_done_white_18dp;
                    break;
                case UIMessage.SENT_TO_OTHER_DEVICE:
                    stateRes = R.drawable.sent;
                    break;
                case UIMessage.SENT_TO_INTERNET:
                    stateRes = R.drawable.ic_sent_to_server;
                    break;
                case UIMessage.DELETED:
                    stateRes = R.drawable.ic_message_deleted;
                    break;
                case UIMessage.ATTACHMENT_DELIVERED:
                    stateRes = R.drawable.ic_done_all_white_18dp;
                    break;
                default:
                    stateRes = R.drawable.time;
                    break;
            }
            Glide.with(getContext()).load(stateRes).into(holder.messageState);
        }

        return cv;
    }

    private String getDisplayName(UIMessageAttachment uiMessageAttachment) {
        if (uiMessageAttachment.getName().length() > 14)
            return uiMessageAttachment.getName().substring(0, 10) + "...";
        else return uiMessageAttachment.getName();
    }

    private void onViewClicked(UIMessage message) {
        boolean outgoing = message.getSendType() != UIMessage.INCOMING;

        if (message.hasAttachment()) {
            UIMessageAttachment uiMessageAttachment = message.getUiMessageAttachment();

            if (outgoing) {
                if (uiMessageAttachment.getState() <= UIMessageAttachment.STATE_UPLOAD_CANCELLED) {
                    fileDialog = new UploadFileDialog(
                            activityCtx,
                            appData,
                            remoteProfile,
                            message.getUiAttachmentId()
                    );
                    fileDialog.show();
                } else {
                    openFile(message);
                }

                return;
            }


            if (!message.getUiMessageAttachment().hasFinishedDownload()) {
                fileDialog = new DownloadFileDialog(
                        activityCtx,
                        appData,
                        remoteProfile,
                        message.getUiAttachmentId()
                );
                fileDialog.show();
            } else {
                openFile(message);
            }
        } else {
            Intent i = new Intent(appData, MessageDetailActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra(MessageDetailActivity.UI_MESSAGE_ID_TAG, message.getId());
            appData.startActivity(i);
        }
    }

    private void openFile(UIMessage message) {
        try {
            CommonUtils.openFile(appData, message.getFileUri());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            Toast.makeText(
                    getContext(),
                    appData.getString(R.string.toast_error_open_file),
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

    public void updateDialog() {
        if (fileDialog != null) {
            Log.e(TAG, "fileDialog not null, updating dialog");
            fileDialog.updateDialog();
        }

    }

    public void closeDialog() {
        if (fileDialog != null && fileDialog.isShowing()) {
            Log.e(TAG, "fileDialog not null, canceling dialog");
            fileDialog.cancel();
        }
    }

    private String getReadableTime(long millis) {
        long hours = Math.abs(TimeUnit.MILLISECONDS.toHours(millis));
        long mins = Math.abs(TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)));
        long secs = Math.abs(TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        String ret = "";
        if (mins > 0) {
            if (hours > 0) {
                ret += String.format("%dh ", hours);
                ret += String.format("%02dmin ", mins);
            } else {
                ret += String.format("%dmin ", mins);
            }
            ret += String.format("%02ds", secs);
        } else {
            ret += String.format("%ds", secs);
        }
        return ret;
    }

    private class ViewHolder {
        TextView messageText, messageTime, dateStamp;
        ImageView messagePhoto, messageState, errorSign;
        View dateDivider, bubbleLayout, dateBar;

        //File preview
        View preview;
        ImageView previewImage;
        TextView previewFilename, previewFileSize;

        //File layout
        View fileLayout;
        ImageView fileImage, filePreviewImage;
        TextView fileText;
    }

    public void setRemoteProfile(Profile profile) {
        this.remoteProfile = profile;
    }

    public void startSmileTask() {
        new AddSmilesTask().execute();
    }

    @Override
    public int getCount() {
        return itemsList.size();
    }

    private class AddSmilesTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            ArrayList<UIMessage> messageArrayList = new ArrayList<UIMessage>();
            messageArrayList.addAll(itemsList);
            EmoticonSupportHelper emoticonSupportHelper = new EmoticonSupportHelper();
            for (UIMessage message : messageArrayList) {
                if (!message.hasAttachment()) {
                    message.setSmileText(
                            emoticonSupportHelper.getSmiledText(
                                    getContext(),
                                    message.getContent()
                            )
                    );
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            notifyDataSetChanged();
        }
    }


}
