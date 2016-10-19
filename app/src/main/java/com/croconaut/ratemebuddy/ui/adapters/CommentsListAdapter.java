package com.croconaut.ratemebuddy.ui.adapters;


import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
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
import com.croconaut.ratemebuddy.activities.CommunicationActivity;
import com.croconaut.ratemebuddy.activities.EditProfileActivity;
import com.croconaut.ratemebuddy.data.pojo.Comment;
import com.croconaut.ratemebuddy.ui.views.transformation.CircleTransform;
import com.croconaut.ratemebuddy.utils.CommonUtils;
import com.croconaut.ratemebuddy.utils.EmoticonSupportHelper;
import com.croconaut.ratemebuddy.utils.ProfileUtils;
import com.croconaut.ratemebuddy.utils.ThemeManager;
import com.croconaut.ratemebuddy.utils.pojo.profiles.IProfile;
import com.croconaut.ratemebuddy.utils.pojo.profiles.MyProfile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentsListAdapter extends ArrayAdapter<Comment> {
    public static final String TAG = CommentsListAdapter.class.getName();

    private static final String DAY_FORMAT = "HH:mm";
    private static final String DAY_OF_WEEK_FORMAT = "EEEE dd. MM. yyyy";

    private List<Comment> itemsList;
    private final LayoutInflater inflater;
    private final SimpleDateFormat dateFormat;
    private final SimpleDateFormat dayOfWeekFormat;
    private final ThemeManager theme;
    private final AppData appData;
    private final ProfileUtils profileUtils;
    private final Typeface tRegular;

    public CommentsListAdapter(AppData appData, List<Comment> commentsList) {
        super(appData, R.layout.activity_main, commentsList);
        inflater = LayoutInflater.from(appData);
        dateFormat = new SimpleDateFormat(DAY_FORMAT, Locale.getDefault());
        dayOfWeekFormat = new SimpleDateFormat(DAY_OF_WEEK_FORMAT, Locale.getDefault());
        this.theme = new ThemeManager(appData);
        this.itemsList = commentsList;
        this.appData = appData;
        this.profileUtils = new ProfileUtils(this.appData);
        tRegular = Typeface.createFromAsset(appData.getAssets(), "fonts/regular.ttf");
    }

    public void setData(List<Comment> commentList){
        this.itemsList = commentList;
        Collections.sort(itemsList);
        notifyDataSetChanged();
        addSmiles();
    }

    @Override
    public View getView(int position, View cv, ViewGroup parent) {
        final Comment comment = itemsList.get(position);
        Date time = new Date(comment.getTimeStamp());

        boolean showDateStamp = position == 0;
        // compare time of this and previous vote to show datetime
        if (position > 0) {
            Date prevMessageDate = new Date(itemsList.get(position - 1).getTimeStamp());

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
        int layoutRes = R.layout.list_view_item_commets;

        // holder to hold all references to views in layout
        final ViewHolder holder;

        // converView pattern
        if (cv == null) {
            cv = inflater.inflate(layoutRes, parent, false);
            holder = new ViewHolder();
            holder.voteText = (TextView) cv.findViewById(R.id.messageText);
            holder.voteTime = (TextView) cv.findViewById(R.id.messageTime);
            holder.dateStamp = (TextView) cv.findViewById(R.id.messageDayStamp);
            holder.dateDivider = cv.findViewById(R.id.messageDayDivider);
            holder.bubbleLayout = cv.findViewById(R.id.bubbleLayout);
            holder.votePhoto = (ImageView) cv.findViewById(R.id.messagePhoto);
            holder.voteText.setTypeface(tRegular);
            holder.voteTime.setTypeface(tRegular);
            holder.dateStamp.setTypeface(tRegular);
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

        String bubleText = comment.getComment();


        holder.voteText.setText(
                comment.getSmileText() != null
                        ? comment.getSmileText()
                        : comment.getComment()
        );

        final IProfile remoteProfile = profileUtils.findProfileByIdOrCrocoId(
                comment.getCrocoId(),
                comment.getProfileId(),
                comment.getProfileName()
        );

        int bubbleRes = theme.getIncominCommentSelector();

        holder.votePhoto.setVisibility(View.VISIBLE);


        Glide.with(getContext())
                .load(remoteProfile.getThumbUri())
                .asBitmap()
                .signature(new StringSignature(
                        String.valueOf(remoteProfile.getTimeStamp())))
                .error(ProfileUtils.getTextDrawableForProfile(remoteProfile))
                .thumbnail(0.2f)
                .transform(new CircleTransform(getContext()))
                .into(holder.votePhoto);

        holder.votePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (remoteProfile instanceof MyProfile) {
                    Intent intent = new Intent(appData, EditProfileActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    appData.startActivity(intent);
                } else {
                    Intent intent = new Intent(appData, CommunicationActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(CommunicationActivity.EXTRA_TARGET_CROCO_ID, remoteProfile.getIdent());
                    intent.putExtra(CommonUtils.EXTRA_CLEAR_NOTIF_MESS_PROFILES, true);
                    appData.startActivity(intent);
                }
            }
        });

        holder.bubbleLayout.setBackgroundResource(bubbleRes);

        // after click on buble, show profile if is available
        holder.bubbleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (remoteProfile instanceof MyProfile) {
                    Intent intent = new Intent(appData, EditProfileActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    appData.startActivity(intent);
                } else {
                    Intent intent = new Intent(appData, CommunicationActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(CommunicationActivity.EXTRA_TARGET_CROCO_ID, remoteProfile.getIdent());
                    intent.putExtra(CommonUtils.EXTRA_CLEAR_NOTIF_MESS_PROFILES, true);
                    appData.startActivity(intent);
                }
            }
        });
        return cv;
    }

    @Override
    public int getCount() {
        return itemsList.size();
    }

    // viewholder class for MainAdapater
    private class ViewHolder {
        TextView voteText, voteTime, dateStamp;
        ImageView votePhoto;
        View dateDivider, bubbleLayout;
    }

    public void addSmiles(){
        new AddSmilesTask().execute();
    }


    private class AddSmilesTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            ArrayList<Comment> messageArrayList = new ArrayList<>();
            messageArrayList.addAll(itemsList);
            EmoticonSupportHelper emoticonSupportHelper = new EmoticonSupportHelper();
            for (Comment comment : messageArrayList) {
                comment.setSmileText(
                        emoticonSupportHelper.getSmiledText(
                                getContext(),
                                comment.getComment()
                        )
                );
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
