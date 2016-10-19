package com.croconaut.ratemebuddy.ui.adapters;


import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.croconaut.ratemebuddy.AppData;
import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.activities.CommentActivity;
import com.croconaut.ratemebuddy.activities.CommunicationActivity;
import com.croconaut.ratemebuddy.activities.EditProfileActivity;
import com.croconaut.ratemebuddy.ui.views.transformation.CircleTransform;
import com.croconaut.ratemebuddy.utils.CommonUtils;
import com.croconaut.ratemebuddy.utils.ProfileUtils;
import com.croconaut.ratemebuddy.utils.ThemeManager;
import com.croconaut.ratemebuddy.utils.pojo.TimelineInfo;
import com.croconaut.ratemebuddy.utils.pojo.profiles.IProfile;
import com.croconaut.ratemebuddy.utils.pojo.profiles.MyProfile;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TimelineListAdapter extends ArrayAdapter<TimelineInfo> {
    private static final String TAG = TimelineListAdapter.class.getName();

    private static final String DAY_FORMAT = "HH:mm";
    private static final String DAY_OF_WEEK_FORMAT = "EEEE dd. MM. yyyy";

    private final List<TimelineInfo> itemsList;
    private final LayoutInflater inflater;
    private final SimpleDateFormat dateFormat;
    private final SimpleDateFormat dayOfWeekFormat;
    private final ThemeManager theme;
    private final Resources mRes;
    private final ProfileUtils profileUtils;

    public TimelineListAdapter(AppData context, List<TimelineInfo> itemsArrayList) {
        super(context, R.layout.activity_main, itemsArrayList);
        inflater = LayoutInflater.from(context);
        dateFormat = new SimpleDateFormat(DAY_FORMAT, Locale.getDefault());
        dayOfWeekFormat = new SimpleDateFormat(DAY_OF_WEEK_FORMAT, Locale.getDefault());
        this.theme = new ThemeManager(context);
        this.itemsList = itemsArrayList;
        this.mRes = context.getResources();
        this.profileUtils = new ProfileUtils(context);
    }

    @Override
    public int getItemViewType(int position) {
        return itemsList.get(position).getSendType() == TimelineInfo.INCOMING ? TimelineInfo.INCOMING : TimelineInfo.OUTGOING;
    }


    @Override
    public int getCount() {
        return itemsList.size();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View cv, ViewGroup parent) {
        final TimelineInfo timeLineInfo = itemsList.get(position);
        final String content = timeLineInfo.getContent();

        Typeface tRegular = Typeface.createFromAsset(getContext().getAssets(), "fonts/regular.ttf");

        // vote time
        Date time = new Date(timeLineInfo.getTime());

        boolean showDateStamp = position == 0;
        final boolean outgoing = timeLineInfo.getSendType() != TimelineInfo.INCOMING;

        // compare time of this and previous vote to show datetime
        if (position > 0) {
            Date prevMessageDate = new Date(itemsList.get(position - 1).getTime());

            Calendar c = Calendar.getInstance();
            c.setTime(time);
            int thisDay = c.get(Calendar.DAY_OF_YEAR);
            int thisYear = c.get(Calendar.YEAR);

            c.setTime(prevMessageDate);
            int prevDay = c.get(Calendar.DAY_OF_YEAR);
            int prevYear = c.get(Calendar.YEAR);

            showDateStamp = thisDay != prevDay || thisYear != prevYear;
        }

        // choose correct layout
        int layoutRes = outgoing ? R.layout.list_view_item_outgoing : R.layout.list_view_item_incoming;

        // holder to hold all references to views in layout
        final ViewHolder holder;

        // converView pattern
        if (cv == null) {
            cv = inflater.inflate(layoutRes, parent, false);
            holder = new ViewHolder();
            holder.voteText = (TextView) cv.findViewById(R.id.messageText);
            holder.voteTime = (TextView) cv.findViewById(R.id.messageTime);
            holder.dateStamp = (TextView) cv.findViewById(R.id.messageDayStamp);
            holder.messageImage = (ImageView) cv.findViewById(R.id.messageImage);
            holder.dateDivider = cv.findViewById(R.id.messageDayDivider);
            holder.bubbleLayout = cv.findViewById(R.id.bubbleLayout);
            holder.votePhoto = (ImageView) cv.findViewById(R.id.messagePhoto);
            holder.voteText.setTypeface(tRegular);
            holder.voteTime.setTypeface(tRegular);
            holder.dateStamp.setTypeface(tRegular);
            holder.dateStamp.setTextColor(ContextCompat.getColor(getContext(), R.color.material_grey_600));
            cv.setTag(holder);
        } else {
            holder = (ViewHolder) cv.getTag();
        }

        // show head with date
        if (showDateStamp) {
            holder.dateStamp.setText(dayOfWeekFormat.format(time).toUpperCase(Locale.getDefault()));
            holder.dateStamp.setVisibility(View.VISIBLE);
            holder.dateDivider.setVisibility(View.VISIBLE);
        } else {
            holder.dateStamp.setVisibility(View.GONE);
            holder.dateDivider.setVisibility(View.GONE);
        }

        // set correct values to views
        holder.voteTime.setText(dateFormat.format(time));

        String bubbleText = null;
        if (outgoing) {
            if (timeLineInfo.getMessageType() == TimelineInfo.MESSAGE_TYPE_VOTE)
                bubbleText = mRes.getQuantityString(R.plurals.mesage_voteup_outgoing, 1, timeLineInfo.getName(), content, content, content);
            else if (timeLineInfo.getMessageType() == TimelineInfo.MESSAGE_TYPE_FILE)
                bubbleText = mRes.getQuantityString(R.plurals.mesage_file_outgoing, 1, content, timeLineInfo.getName(), content, content);
            else if (timeLineInfo.getMessageType() == TimelineInfo.MESSAGE_TYPE_COMMENT)
                bubbleText = mRes.getString(R.string.message_timeline_comment_outgoing, timeLineInfo.getName(), timeLineInfo.getContent());
        } else {
            if (timeLineInfo.getMessageType() == TimelineInfo.MESSAGE_TYPE_VOTE)
                bubbleText = mRes.getQuantityString(R.plurals.mesage_voteup_incoming, 1, timeLineInfo.getName(), content, content, content);
            else if (timeLineInfo.getMessageType() == TimelineInfo.MESSAGE_TYPE_FILE)
                bubbleText = mRes.getString(R.string.message_timeline_file_accepted, content);
            else if (timeLineInfo.getMessageType() == TimelineInfo.MESSAGE_TYPE_INTRO)
                bubbleText = mRes.getString(R.string.message_timeline_intro, timeLineInfo.getName());
            else if (timeLineInfo.getMessageType() == TimelineInfo.MESSAGE_TYPE_COMMENT)
                bubbleText = mRes.getString(R.string.message_timeline_comment_incoming, timeLineInfo.getName(), timeLineInfo.getContent());
        }

        if (timeLineInfo.getMessageType() == TimelineInfo.MESSAGE_TYPE_PROFILE_CHANGED_TEXT
                || timeLineInfo.getMessageType() == TimelineInfo.MESSAGE_TYPE_PROFILE_CHANGED_PHOTO)
            bubbleText = timeLineInfo.getContent();

        if (timeLineInfo.getMessageType() == TimelineInfo.MESSAGE_TYPE_FILE) {
            ContentResolver cr = getContext().getContentResolver();
            String type = cr.getType(timeLineInfo.getFileUri());

            if (type != null && (type.contains("image") || type.contains("video"))) {
                holder.messageImage.setVisibility(View.VISIBLE);
                holder.voteText.setText(bubbleText);

                Glide.with(getContext())
                        .load(timeLineInfo.getFileUri())
                        .asBitmap()
                        .override(300, 400)
                        .centerCrop()
                        .thumbnail(0.2f)
                        .into(holder.messageImage);
            } else {
                holder.messageImage.setVisibility(View.GONE);

                holder.voteText.setVisibility(View.VISIBLE);
                SpannableString ss = new SpannableString(bubbleText);
                ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View textView) {
                        try {
                            CommonUtils.openFile(getContext(), timeLineInfo.getFileUri());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };

                ss.setSpan(clickableSpan, bubbleText.indexOf(timeLineInfo.getContent()),
                        bubbleText.indexOf(timeLineInfo.getContent()) + timeLineInfo.getContent().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.voteText.setText(ss);
                holder.voteText.setMovementMethod(LinkMovementMethod.getInstance());
            }
        } else if (timeLineInfo.getMessageType() == TimelineInfo.MESSAGE_TYPE_PROFILE_CHANGED_PHOTO) {
            holder.messageImage.setVisibility(View.VISIBLE);
            holder.voteText.setText(bubbleText);

            final Uri photoUri = ProfileUtils.getUriById(getContext(), timeLineInfo.getTime());

//            holder.messageImage.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    try {
//                        Intent intent = new Intent();
//                        intent.setAction(Intent.ACTION_VIEW);
//                        Log.e(TAG, "Clicked");
//                        intent.setDataAndType(photoUri, "image/*");
//                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
//                        getContext().startActivity(intent);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            });

            Glide.with(getContext())
                    .load(photoUri)
                    .asBitmap()
                    .override(300, 400)
                    .centerCrop()
                    .thumbnail(0.2f)
                    .into(holder.messageImage);
        } else {
            holder.voteText.setVisibility(View.VISIBLE);
            holder.messageImage.setVisibility(View.GONE);
            holder.voteText.setText(Html.fromHtml(bubbleText));
        }

        final IProfile profile = profileUtils.findProfile(timeLineInfo.getCrocoId(), timeLineInfo.getName());

        @DrawableRes int bubbleRes;
        if (outgoing) {
            bubbleRes = R.drawable.sent_message_item_selector;
        } else {
            bubbleRes = theme.getIncomingMessageSelector(timeLineInfo.getMessageType());
            holder.votePhoto.setVisibility(View.VISIBLE);

            holder.votePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), CommunicationActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(CommunicationActivity.EXTRA_TARGET_CROCO_ID, profile.getIdent());
                    getContext().startActivity(intent);
                }
            });

            Glide.with(getContext())
                    .load(profile.getThumbUri())
                    .asBitmap()
                    .signature(new StringSignature(
                            String.valueOf(profile.getTimeStamp())))
                    .error(ProfileUtils.getTextDrawableForProfile(profile))
                    .thumbnail(0.2f)
                    .transform(new CircleTransform(getContext()))
                    .into(holder.votePhoto);
        }

        holder.bubbleLayout.setBackgroundResource(bubbleRes);

        holder.bubbleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IProfile profile = profileUtils.findProfile(timeLineInfo.getCrocoId(), timeLineInfo.getName());

                if (timeLineInfo.getMessageType() == TimelineInfo.MESSAGE_TYPE_FILE) {
                    try {
                        CommonUtils.openFile(getContext(), timeLineInfo.getFileUri());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }

                if (timeLineInfo.getMessageType() == TimelineInfo.MESSAGE_TYPE_PROFILE_CHANGED_PHOTO
                        || timeLineInfo.getMessageType() == TimelineInfo.MESSAGE_TYPE_PROFILE_CHANGED_TEXT) {
                    Intent intent;

                    if (outgoing) {
                        intent = new Intent(getContext(), EditProfileActivity.class);
                    } else {
                        intent = new Intent(getContext(), CommunicationActivity.class);
                        intent.putExtra(CommunicationActivity.EXTRA_TARGET_CROCO_ID, profile.getIdent());
                    }

                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getContext().startActivity(intent);
                    return;
                }

                Intent intent = null;
                if (outgoing && (timeLineInfo.getMessageType() == TimelineInfo.MESSAGE_TYPE_COMMENT
                        || timeLineInfo.getMessageType() == TimelineInfo.MESSAGE_TYPE_VOTE)) {
                    intent = new Intent(getContext(), CommentActivity.class);
                    intent.putExtra(CommentActivity.EXTRA_CROCO_ID, profile.getIdent());
                } else if (!outgoing && (timeLineInfo.getMessageType() == TimelineInfo.MESSAGE_TYPE_COMMENT
                        || timeLineInfo.getMessageType() == TimelineInfo.MESSAGE_TYPE_VOTE)) {
                    intent = new Intent(getContext(), CommentActivity.class);
                    if (timeLineInfo.getStatusId() != null)
                        intent.putExtra(CommentActivity.EXTRA_OLD_STATUS_ID, timeLineInfo.getStatusId());
                    intent.putExtra(CommentActivity.EXTRA_CROCO_ID, MyProfile.getInstance(getContext()).getIdent());
                } else if (timeLineInfo.getMessageType() == TimelineInfo.MESSAGE_TYPE_FILE) {
                    intent = new Intent(getContext(), CommunicationActivity.class);
                    intent.putExtra(CommunicationActivity.EXTRA_TARGET_CROCO_ID, profile.getIdent());
                    intent.putExtra(CommonUtils.EXTRA_CLEAR_NOTIF_MESS_PROFILES, true);
                }

                if (intent != null) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getContext().startActivity(intent);
                } else Log.e(TAG, "Intent is null");
            }
        });
        return cv;
    }

    // viewholder class for MainAdapater
    private class ViewHolder {
        TextView voteText, voteTime, dateStamp;
        ImageView votePhoto;
        ImageView messageImage;
        View dateDivider, bubbleLayout;
    }
}
