package com.croconaut.ratemebuddy.activities;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.format.Formatter;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.croconaut.cpt.data.Communication;
import com.croconaut.cpt.network.NetworkHop;
import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.ui.views.transformation.CircleTransform;
import com.croconaut.ratemebuddy.utils.CommonUtils;
import com.croconaut.ratemebuddy.utils.EmoticonSupportHelper;
import com.croconaut.ratemebuddy.utils.pojo.UIMessage;
import com.croconaut.ratemebuddy.utils.pojo.UIMessageAttachment;
import com.croconaut.ratemebuddy.utils.pojo.profiles.IProfile;
import com.croconaut.ratemebuddy.utils.pojo.profiles.MyProfile;
import com.croconaut.ratemebuddy.utils.pojo.profiles.Profile;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@SuppressLint("InflateParams")
public class MessageDetailActivity extends WifonActivity implements CptProcessor {

    public static final String UI_MESSAGE_ID_TAG = "ui_message";
    private static final String TAG = MessageDetailActivity.class.getName();

    private Profile remoteProfile;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createDetail();
    }

    private void createDetail() {
        // reset the view
        setContentView(R.layout.activity_message_detail);

        int messageId = getIntent().getIntExtra(UI_MESSAGE_ID_TAG, 0);
        final UIMessage message = appData.getUiMessageDataSource().getMessageById(messageId);
        dateFormat = new SimpleDateFormat("HH:mm:ss, dd.MM.yyyy", Locale.getDefault());

        remoteProfile = (Profile) profileUtils.findProfile(message.getCrocoId());
        initializeHeaderWithDrawer(remoteProfile.getName(), false);

        List<NetworkHop> hopsData = getNetworkHops(message);

        ListView hops = (ListView) findViewById(R.id.messageDetailHopList);
        assert hops != null;
        hops.setDivider(null);

        final ArrayList<Hop> hopsList = new ArrayList<>();
        for (int i = 0; i < hopsData.size(); i++) {
            NetworkHop hop = hopsData.get(i);
            hopsList.add(new Hop(hop.userName, hop.latitude, hop.longitude, hop.receivedTime, hop.locationTime));
        }

        HopsAdapter adapter = new HopsAdapter(this, 0, hopsList);
        for (Hop h : hopsList) {
            new GetGPSNamesTask(h, adapter).execute();
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        ViewGroup header = (ViewGroup) inflater.inflate(
                R.layout.activity_message_detail_header, hops, false);

        setupExportButton(message, hopsList, header);

        boolean isOutgoing = message.getSendType() != UIMessage.INCOMING;
        boolean isAttachment = message.hasAttachment();
        boolean hasFinishedDownload = isAttachment && message.getUiMessageAttachment().hasFinishedDownload();

        LinearLayout timeContainer = (LinearLayout) header.findViewById(R.id.msgTimesContainer);
        LinearLayout.LayoutParams timesParams = (LinearLayout.LayoutParams) timeContainer.getLayoutParams();
        timesParams.gravity = isOutgoing ? Gravity.RIGHT : Gravity.LEFT;
        timeContainer.setLayoutParams(timesParams);

        View messageDetailTimeOutgoingSent = header.findViewById(R.id.msgTimeOutgoingSent);
        View messageDetailTimeOutgoingAck = header.findViewById(R.id.msgTimeOutgoingAck);
        View messageDetailTimeIncoming = header.findViewById(R.id.msgTimeIncoming);

        messageDetailTimeIncoming.setVisibility(isOutgoing ? View.GONE : View.VISIBLE);
        messageDetailTimeOutgoingSent.setVisibility(isOutgoing ? View.VISIBLE : View.GONE);
        messageDetailTimeOutgoingAck.setVisibility(isOutgoing ? View.VISIBLE : View.GONE);

        TextView messageText = (TextView) header.findViewById(R.id.messageText);
        ImageView messageImage = (ImageView) header.findViewById(R.id.messageImage);

        TextView tvCreationTime = (TextView) header.findViewById(R.id.msgCreationTimeText);
        tvCreationTime.setTypeface(tRegular);
        tvCreationTime.setText(
                getString(
                        R.string.message_written_at,
                        dateFormat.format(new Date(message.getCreationTime()))
                )
        );

        setupUserImageAndMsgBackground(
                header,
                messageText,
                isOutgoing
        );

        if (isOutgoing) {
            setFirstAndLastSentTime(message, header, isAttachment);
            setupAckState(header, message, isAttachment);

            View msgSentServerTimeContainer = header.findViewById(R.id.msgSentServerTimeContainer);
            if (message.getSentToInternetTime() != 0) {
                msgSentServerTimeContainer.setVisibility(View.VISIBLE);
                TextView tvSentServerTime = (TextView) header.findViewById(R.id.msgSentServerTimeText);
                tvSentServerTime.setTypeface(tRegular);

                tvSentServerTime.setText(
                        getString(
                                R.string.message_sent_to_server_at,
                                dateFormat.format(new Date(message.getSentToInternetTime()))
                        )
                );
            } else msgSentServerTimeContainer.setVisibility(View.GONE);

            View msgSentUserTimeContainer = header.findViewById(R.id.msgSentUserTimeContainer);
            if (message.getSentToRecipientTime() != 0) {
                msgSentUserTimeContainer.setVisibility(View.VISIBLE);
                TextView tvSentRecipientTime = (TextView) header.findViewById(R.id.msgSentUserTimeText);
                tvSentRecipientTime.setTypeface(tRegular);

                tvSentRecipientTime.setText(
                        getString(
                                R.string.message_sent_to_user_at,
                                dateFormat.format(new Date(message.getSentToRecipientTime()))
                        )
                );
            } else {
                msgSentUserTimeContainer.setVisibility(View.GONE);
            }

            View msgSentOtherDeviceTimeContainer = header.findViewById(R.id.msgSentOtherDeviceTimeContainer);
            if (message.getSentToOtherDeviceTime() != 0) {
                header.findViewById(R.id.msgSentOtherDeviceTimeContainer).setVisibility(View.VISIBLE);
                TextView tvSentOtherDeviceTime = (TextView) header.findViewById(R.id.msgSentOtherDeviceTimeText);
                tvSentOtherDeviceTime.setTypeface(tRegular);

                tvSentOtherDeviceTime.setText(
                        getString(
                                R.string.message_sent_to_other_device_at,
                                dateFormat.format(new Date(message.getSentToOtherDeviceTime()))
                        )
                );
            } else msgSentOtherDeviceTimeContainer.setVisibility(View.GONE);


        } else {
            header.findViewById(R.id.msgReceivedTimeContainer).setVisibility(View.VISIBLE);
            header.findViewById(R.id.msgCameUpTimeContainer).setVisibility(View.VISIBLE);

            TextView tvReceivedTime = (TextView) header.findViewById(R.id.msgReceivedTimeText);
            TextView tvCameUpTime = (TextView) header.findViewById(R.id.msgCameUpTimeText);

            tvReceivedTime.setTypeface(tRegular);
            tvCameUpTime.setTypeface(tRegular);

            tvReceivedTime.setText(
                    getString(
                            R.string.message_received_time_at,
                            dateFormat.format(
                                    new Date(Math.abs(message.getReceivedTime()))
                            )
                    )
            );

            long cameUpTime = message.getReceivedTime() - message.getCreationTime();

            cameUpTime = getFaultTolerantTime(cameUpTime);

            String cameUpTimeString = getString(
                    R.string.message_came_up_time_at,
                    getReadableTime(Math.abs(cameUpTime))
            );

            if (cameUpTime <= 0) {
                ImageView ivCameUpTimeIcon = (ImageView) header.findViewById(R.id.msgCameUpTimeIcon);
                Glide.with(mContext)
                        .load(R.drawable.ic_msg_error_black)
                        .into(ivCameUpTimeIcon);

                cameUpTimeString += getString(
                        R.string.message_time_error,
                        getReadableTime(Math.abs(cameUpTime))
                );
            }

            tvCameUpTime.setText(cameUpTimeString);


            if (isAttachment) {
                header.findViewById(R.id.msgAttachmentTransferInContainer).setVisibility(View.VISIBLE);

                TextView tvTransferIn = (TextView) header.findViewById(R.id.msgAttachmentTransferInText);
                tvTransferIn.setTypeface(tRegular);

                UIMessageAttachment uiMessageAttachment = message.getUiMessageAttachment();
                long transferInTime =
                        uiMessageAttachment.getDownloadEnded() - uiMessageAttachment.getDownloadStarted();

                String transferInText = getString(
                        R.string.message_attachment_transfer_in,
                        getReadableTime(Math.abs(transferInTime))
                );

                if (transferInTime <= 0) {
                    ImageView ivTransferInIcon = (ImageView) header.findViewById(R.id.msgAttachmentTransferInIcon);
                    Glide.with(mContext)
                            .load(R.drawable.ic_msg_error_black)
                            .into(ivTransferInIcon);

                    transferInText += getString(
                            R.string.message_time_error,
                            getReadableTime(Math.abs(transferInTime))
                    );
                }

                tvTransferIn.setText(transferInText);
            }

            if (isAttachment && !hasFinishedDownload) {
                header.findViewById(R.id.msgAttachmentTransferInContainer).setVisibility(View.GONE);
                header.findViewById(R.id.msgReceivedTimeContainer).setVisibility(View.GONE);
                header.findViewById(R.id.msgCameUpTimeContainer).setVisibility(View.GONE);
            }

        }


        if (isAttachment) {
            initAttachmentInfo(message, header);

            Log.e(TAG, "Message uri: " + message.getFileUri());

            if (message.getFileUri() != null) {
                ContentResolver cr = mContext.getContentResolver();
                String mimeType = cr.getType(message.getFileUri());

                if (mimeType != null && (mimeType.contains("image") || mimeType.contains("video"))) {
                    messageText.setVisibility(View.GONE);
                    messageImage.setVisibility(View.VISIBLE);

                    Glide.with(this).load(message.getFileUri())
                            .asBitmap()
                            .centerCrop()
                            .thumbnail(0.7f)
                            .into(messageImage);


                    messageImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                CommonUtils.openFile(mContext, message.getFileUri());
                            } catch (Exception e) {
                                Log.e(TAG, e.getMessage());
                                Toast.makeText(
                                        mContext,
                                        mRes.getString(R.string.toast_error_open_file),
                                        Toast.LENGTH_LONG)
                                        .show();
                            }
                        }
                    });

                } else {
                    Glide.with(this)
                            .load(CommonUtils.getDrawableForFile(mContext, message.getFileUri()))
                            .asBitmap()
                            .centerCrop()
                            .thumbnail(0.2f)
                            .into(messageImage);

                    messageText.setVisibility(View.GONE);
                    messageImage.setVisibility(View.VISIBLE);
//
//                    setTextViewClickable(message, messageText, message.getContent());
                }
            } else
                messageText.setText(new EmoticonSupportHelper()
                        .getSmiledText(
                                MessageDetailActivity.this,
                                message.getContent()
                        )
                );
        } else {
            messageText.setText(new EmoticonSupportHelper()
                    .getSmiledText(
                            MessageDetailActivity.this,
                            message.getContent()
                    )
            );
        }
        messageText.setMovementMethod(LinkMovementMethod.getInstance());


        hops.addHeaderView(header, null, false);
        hops.setAdapter(adapter);
    }

    private void setTextViewClickable(final UIMessage message, TextView textView, String text) {
        if (message.getFileUri() == null) {
            textView.setText(text);
            return;
        }

        SpannableString ss = new SpannableString(text);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                try {
                    CommonUtils.openFile(mContext, message.getFileUri());
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                    Toast.makeText(
                            mContext,
                            mRes.getString(R.string.toast_error_open_file),
                            Toast.LENGTH_LONG)
                            .show();
                }
            }
        };

        ss.setSpan(
                clickableSpan,
                text.indexOf(message.getContent()),
                text.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        textView.setText(ss);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }


    private void initAttachmentInfo(UIMessage message, ViewGroup header) {
        header.findViewById(R.id.msgDetailFileInfoContainer).setVisibility(View.VISIBLE);
        header.findViewById(R.id.message_detail_file_info_divider).setVisibility(View.VISIBLE);

        UIMessageAttachment uiMessageAttachment = message.getUiMessageAttachment();

        TextView tvFileFullName = (TextView) header.findViewById(R.id.tvFileFullName);
        TextView tvFileSize = (TextView) header.findViewById(R.id.tvFileSize);
        TextView tvFileModificationTime = (TextView) header.findViewById(R.id.tvFileModificationTime);
        TextView tvFileTransferSpeed = (TextView) header.findViewById(R.id.tvFileTransferSpeed);

        tvFileFullName.setTypeface(tRegular);
        tvFileSize.setTypeface(tRegular);
        tvFileModificationTime.setTypeface(tRegular);
        tvFileTransferSpeed.setTypeface(tRegular);

        String textToClick = getString(
                R.string.message_attachment_detail_name,
                uiMessageAttachment.getName()
        );
        setTextViewClickable(message, tvFileFullName, textToClick);

        tvFileSize.setText(
                getString(
                        R.string.message_attachment_detail_size,
                        Formatter.formatShortFileSize(
                                appData,
                                uiMessageAttachment.getLength()
                        )
                )
        );

        if (uiMessageAttachment.getLastModified() != null)
            tvFileModificationTime.setText(
                    getString(
                            R.string.message_attachment_detail_mod_time,
                            dateFormat.format(
                                    new Date(uiMessageAttachment.getLastModified().getTime()
                                    )
                            )
                    )

            );
        else tvFileModificationTime.setVisibility(View.GONE);

        if (uiMessageAttachment.getSpeed() > 0) {
            String transferSpeed = Formatter.formatShortFileSize(
                    mContext,
                    uiMessageAttachment.getSpeed()
            );
            transferSpeed += "/s";
            tvFileTransferSpeed.setText(
                    getString(
                            R.string.message_attachment_detail_transfer_speed,
                            transferSpeed
                    )
            );
        } else tvFileTransferSpeed.setVisibility(View.GONE);
    }

    private void setFirstAndLastSentTime(UIMessage message, ViewGroup header, boolean isAttachment) {
        if (isAttachment) return;

        if (message.getFirstSentTime() != 0) {
            header.findViewById(R.id.msgSentFirstTimeContainer).setVisibility(View.VISIBLE);
            TextView tvSentFirstTime = (TextView) header.findViewById(R.id.msgSentFirstTimeText);
            tvSentFirstTime.setTypeface(tRegular);

            tvSentFirstTime.setText(
                    getString(
                            R.string.message_sent_first_time_at,
                            dateFormat.format(new Date(message.getFirstSentTime()

                            ))
                    )
            );
        }


        if (message.getLastSentTime() != 0) {
            header.findViewById(R.id.msgSentLastTimeContainer).setVisibility(View.VISIBLE);
            TextView tvSentLastTime = (TextView) header.findViewById(R.id.msgSentLastTimeText);
            tvSentLastTime.setTypeface(tRegular);

            tvSentLastTime.setText(
                    getString(
                            R.string.message_sent_last_time_at,
                            dateFormat.format(new Date(message.getLastSentTime()

                            ))
                    )
            );
        }
    }

    private void setupAckState(View header, UIMessage message, boolean isAttachment) {
        if (message.getSeenTime() == 0) return;

        header.findViewById(R.id.msgAckTimeContainer).setVisibility(View.VISIBLE);
        header.findViewById(R.id.msgDeliveredTimeContainer).setVisibility(View.VISIBLE);

        TextView tvAckTime = (TextView) header.findViewById(R.id.msgAckTimeText);
        TextView tvDeliveredTime = (TextView) header.findViewById(R.id.msgDeliveredTimeText);

        tvAckTime.setTypeface(tRegular);
        tvDeliveredTime.setTypeface(tRegular);

        tvAckTime.setText(
                getString(
                        R.string.message_ack_time_at,
                        dateFormat.format(new Date(message.getSeenTime()))
                )
        );

        long deliveredTime = isAttachment
                ? message.getUiMessageAttachment().getTime() - message.getCreationTime()
                : message.getSeenTime() - message.getCreationTime();

        deliveredTime = getFaultTolerantTime(deliveredTime);

        String deliveredTimeText = getString(
                R.string.message_delivered_time_at,
                getReadableTime(
                        Math.abs(deliveredTime)
                )
        );

        if (deliveredTime <= 0) {
            ImageView ivDeliveredAtIcon = (ImageView) header.findViewById(R.id.msgDeliveredTimeIcon);
            Glide.with(mContext)
                    .load(R.drawable.ic_msg_error_black)
                    .into(ivDeliveredAtIcon);

            deliveredTimeText += getString(
                    R.string.message_time_error,
                    getReadableTime(Math.abs(deliveredTime))
            );
        }

        tvDeliveredTime.setText(deliveredTimeText);


        if (isAttachment && !message.getUiMessageAttachment().hasFinishedDownload())
            header.findViewById(R.id.msgDeliveredTimeContainer).setVisibility(View.GONE);


    }

    private long getFaultTolerantTime(long deliveredTime) {
        if (deliveredTime > -5000 && deliveredTime < 5000) {
            deliveredTime = Math.abs(deliveredTime);
        }

        if (deliveredTime > -1000 && deliveredTime < 1000) {
            deliveredTime = 1000;
        }
        return deliveredTime;
    }


    private void setupUserImageAndMsgBackground(View header, TextView messageText, boolean isOutgoing) {
        ImageView messageProfilePhotoIncoming = (ImageView) header.findViewById(R.id.msgProfilePhotoIncoming);
        ImageView messageProfilePhotoOutgoing = (ImageView) header.findViewById(R.id.msgProfilePhotoOutgoing);

        messageProfilePhotoIncoming.setVisibility(isOutgoing ? View.GONE : View.VISIBLE);
        messageProfilePhotoOutgoing.setVisibility(isOutgoing ? View.VISIBLE : View.GONE);

        IProfile profile = isOutgoing ? remoteProfile : MyProfile.getInstance(mContext);

        Glide.with(this)
                .load(profile.getThumbUri())
                .asBitmap()
                .signature(new StringSignature(
                        String.valueOf(profile.getTimeStamp())))
                .thumbnail(0.2f)
                .transform(new CircleTransform(mContext))
                .into(isOutgoing ? messageProfilePhotoOutgoing : messageProfilePhotoIncoming);

        View bubbleLayout = header.findViewById(R.id.msgDetailBubbleLayout);
        @DrawableRes int resId = isOutgoing ? R.drawable.sent_message_item_selector : theme.getIncomingMessageSelector();
        bubbleLayout.setBackgroundResource(
                resId
        );

    }

    private void setupExportButton(UIMessage message, final ArrayList<Hop> hopsList, ViewGroup header) {
        View exportButton = header.findViewById(R.id.earth_button);

        TextView tvHopsTitle = (TextView) header.findViewById(R.id.hops_title);

        if (message.getSendType() == UIMessage.INCOMING || message.getSeenTime() != 0) {
            tvHopsTitle.setText(getString(R.string.hops_title));

            exportButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isEarthInstalled = CommonUtils.isPackageInstalled(
                            "com.google.earth", MessageDetailActivity.this);
                    if (isEarthInstalled) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                showHopsOnEarth(hopsList);
                            }
                        }).start();
                    } else {
                        showEarthInstallDialog();
                    }
                }
            });
        } else {
            header.findViewById(R.id.container_hops_on_earth).setVisibility(View.GONE);
            tvHopsTitle.setText(getString(R.string.hops_title_cannot_show));
        }
    }

    @NonNull
    private List<NetworkHop> getNetworkHops(UIMessage message) {
        List<NetworkHop> hopsData = new ArrayList<>();
        if (message.getHops() != null) {

            Log.d(TAG, "HOPS: " + message.getHops());
            try {
                JSONObject json = new JSONObject(message.getHops());
                JSONArray items = json.optJSONArray(CommonUtils.HOPS_JSON_ARRAY);

                for (int i = 0; i < items.length(); i++) {
                    JSONObject jObj = items.getJSONObject(i);
                    NetworkHop objHop = new NetworkHop(
                            null,
                            jObj.getDouble("latitude"),
                            jObj.getDouble("longitude"),
                            new Date(jObj.getLong("locationTime")),
                            jObj.getString("androidOsVersion"),
                            new Date(jObj.getLong("receivedTime")),
                            jObj.getString("name")
                    );
                    hopsData.add(objHop);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error: ", e);
            }
        } else Log.e(TAG, "Hops are null");
        return hopsData;
    }

    private String getReadableTime(long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long mins = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis));
        long secs = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis));
        String ret = "";
        if (mins > 0) {
            if (hours > 0) {
                ret += String.format("%dh ", hours);
                ret += String.format("%02dm ", mins);
            } else {
                ret += String.format("%dm ", mins);
            }
            ret += String.format("%02ds", secs);
        } else {
            ret += String.format("%ds", secs);
        }

        return ret;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    @Override
    public boolean process(Intent cptIntent) throws IOException, ClassNotFoundException {
        super.process(cptIntent);

        switch (cptIntent.getAction()) {
            case Communication.ACTION_MESSAGE_ACKED:
            case Communication.ACTION_MESSAGE_SENT:
            case Communication.ACTION_MESSAGE_DELETED:
                return processIntentForUpdate(cptIntent);
        }
        return false;
    }

    private boolean processIntentForUpdate(Intent cptIntent) {
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

        createDetail();
        return true;
    }

    private void showEarthInstallDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.hop_installEarth))
                .setPositiveButton(getString(R.string.hop_install),
                        new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(
                                    android.content.DialogInterface dialog,
                                    int which) {
                                Intent marketIntent = new Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("market://details?id=com.google.earth")
                                );
                                startActivity(marketIntent);
                            }
                        })
                .setNegativeButton(getString(R.string.cancel), null)
                .setIcon(R.drawable.ic_launcher)
                .setTitle(getString(R.string.hop_title));
        builder.show();
    }

    private class HopsAdapter extends ArrayAdapter<Hop> {
        LayoutInflater inflater;

        public HopsAdapter(Context context, int resource, List<Hop> objects) {
            super(context, resource, objects);
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = inflater.inflate(R.layout.hop_list_item, null);

            Hop h = getItem(position);

            TextView name = (TextView) v.findViewById(R.id.hop_list_name);
            TextView differ = (TextView) v.findViewById(R.id.hop_list_differ);
            TextView gps = (TextView) v.findViewById(R.id.hop_list_gps);
            TextView date = (TextView) v.findViewById(R.id.hop_list_date);

            name.setText(h.crocoName);
            gps.setText(h.address.length() <= 0 ? mRes
                    .getString(R.string.hops_address_not_found) : h.address);
            date.setText(dateFormat.format(h.receivedTime));

            differ.setTypeface(tSemiBold);
            gps.setTypeface(tRegular);
            date.setTypeface(tRegular);
            name.setTypeface(Typeface.DEFAULT_BOLD);

            return v;
        }
    }

    private class Hop {
        public String crocoName = mRes.getString(R.string.profile_unknown_name);
        public double lat, lon;
        public String address = "";
        public Date receivedTime;
        public Date locationTime;

        public Hop(String name, double lat, double lon, Date receivedTime,
                   Date locationTime) {
            this.crocoName = name;
            this.lat = lat;
            this.lon = lon;
            this.receivedTime = receivedTime;
            this.locationTime = locationTime;
        }
    }

    private class GetGPSNamesTask extends AsyncTask<Void, Void, Void> {

        private final BaseAdapter list;
        private final Hop hop;

        public GetGPSNamesTask(Hop hop, BaseAdapter list) {
            this.hop = hop;
            this.list = list;
        }

        @Override
        protected Void doInBackground(Void... params) {
            String json = getStringFromHttp("http://maps.googleapis.com/maps/api/geocode/json?latlng="
                    + hop.lat + "," + hop.lon + "&sensor=true");

            String loc = "", route = "", number = "";
            try {
                JSONObject obj = new JSONObject(json);
                JSONArray address = obj.getJSONArray("results")
                        .getJSONObject(0).getJSONArray("address_components");
                for (int i = 0; i < address.length(); i++) {
                    JSONObject component = address.getJSONObject(i);
                    JSONArray types = component.getJSONArray("types");

                    for (int j = 0; j < types.length(); j++) {
                        String type = types.getString(j);
                        switch (type) {
                            case "locality":
                                loc = component.getString("long_name");
                                break;
                            case "route":
                                route = component.getString("long_name");
                                break;
                            case "street_number":
                                number = component.getString("long_name");
                                break;
                        }
                    }
                }
                hop.address = loc;
                if (!route.isEmpty()) {
                    hop.address += ", " + route + " " + number;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (list != null)
                list.notifyDataSetChanged();
        }

        public String getStringFromHttp(String url) {
            String response = "";

            try {
                final HttpGet getRequest = new HttpGet(url);
                DefaultHttpClient client = new DefaultHttpClient();
                client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(
                        0, false));

                HttpResponse execute = client.execute(getRequest);
                HttpEntity entity = execute.getEntity();
                response = EntityUtils.toString(entity, HTTP.UTF_8);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

    }

    private void showHopsOnEarth(List<Hop> hops) {
        boolean atLeastOne = false;
        for (Hop hop : hops) {
            if (hop.lat != 0.0 && hop.lon != 0.0) {
                atLeastOne = true;
            }
        }

        if (!atLeastOne) {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MessageDetailActivity.this,
                            mRes.getString(R.string.hops_not_enough_addresses),
                            Toast.LENGTH_LONG).show();
                }
            });
            return;
        }

        // nastav priecinok a vytvor ho ak neexistuje
        File root = new File(Environment.getExternalStorageDirectory()
                .getPath() + "/" + CommonUtils.DIRECTORY_WIFON + "/" + CommonUtils.DIRECTORY_EXPORT_HOPS);
        root.mkdirs();
        File exportFile = null;
        if (root.canWrite()) {
            try {
                exportFile = new File(root, "export.kml");
                FileWriter gpxwriter = new FileWriter(exportFile);
                BufferedWriter out = new BufferedWriter(gpxwriter);
                out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                        + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">"
                        + "<Folder><name>meno</name><open>1</open>");
                out.write("<description>Line</description><Placemark id=\"track1\">"
                        + "<name>frankies path</name><LineString><coordinates>");

                // pridaj ciary medzi pinmi
                for (Hop h : hops) {
                    out.write(h.lon + "," + h.lat + ",0 ");
                }
                out.write("</coordinates></LineString></Placemark>");

                // pridaj piny
                for (Hop h : hops) {
                    out.write("<Placemark>" + "<name>" + h.crocoName
                            + "</name>" + "<description>" + h.address
                            + "</description>" + "<Point><coordinates>" + h.lon
                            + "," + h.lat + ",0"
                            + "</coordinates></Point></Placemark>");
                }
                out.write("</Folder></kml>");
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (exportFile == null) {
            Toast.makeText(this, R.string.hop_export_error, Toast.LENGTH_LONG)
                    .show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(exportFile),
                "application/vnd.google-earth.kml+xml");
        intent.putExtra("com.google.earth.EXTRA.tour_feature_id", "my_track");
        startActivity(intent);
    }
}
