package com.croconaut.ratemebuddy.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.croconaut.cpt.data.Communication;
import com.croconaut.cpt.data.DownloadedAttachment;
import com.croconaut.cpt.data.IncomingMessage;
import com.croconaut.cpt.data.MessageAttachment;
import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.data.pojo.Comment;
import com.croconaut.ratemebuddy.data.pojo.RMBProfile;
import com.croconaut.ratemebuddy.data.pojo.Status;
import com.croconaut.ratemebuddy.data.pojo.VoteUp;
import com.croconaut.ratemebuddy.ui.adapters.TimelineListAdapter;
import com.croconaut.ratemebuddy.utils.ThemeUtils;
import com.croconaut.ratemebuddy.utils.pojo.TimelineInfo;
import com.croconaut.ratemebuddy.utils.pojo.profiles.MyProfile;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

public class TimelineActivity extends WifonActivity implements CptProcessor {
    private static final String TAG = TimelineActivity.class.getName();

    private TimelineListAdapter lvVotesAdapter;
    private ListView lvTimeline;
    private ArrayList<TimelineInfo> timelineInfos = new ArrayList<>();
    private boolean mForceScrollAtStart = false;

    //YOLOc
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        new File(getFilesDir(), "thumbnails").mkdirs();

        // first check for CPT
        if (!checkForMyProfile()) {
            return;
        }

        initializeViewsAndResources();

        //appData.getTimelineDataSource().clearSeen();

        updateHistory(true);
        mForceScrollAtStart = true;
    }

    private boolean checkForMyProfile() {
        if (MyProfile.getInstance(this) == null) {
            finish();
            Intent createProfileIntent = new Intent(this, CreateProfileActivity.class);
            createProfileIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(createProfileIntent);
            return false;
        }
        return true;
    }

    private void initializeViewsAndResources() {
        initializeHeaderWithDrawer(mRes.getString(R.string.action_bar_title_timeline), true);

        timelineInfos = appData.getTimelineDataSource().getAllTimeLineInfos(appData);
        lvVotesAdapter = new TimelineListAdapter(appData, timelineInfos);

        lvTimeline = (ListView) findViewById(R.id.lvVoteUps);
        lvTimeline.setAdapter(lvVotesAdapter);
        lvTimeline.setDivider(null);


        if (!timelineInfos.isEmpty()) {
            LinearLayout layout = (LinearLayout) findViewById(R.id.backgroundLayout);
            layout.setBackgroundColor(ContextCompat.getColor(mContext, R.color.material_white));
        } else {
            final LinearLayout emptyListView = (LinearLayout) findViewById(R.id.emptylistViewHistory);
            lvTimeline.setEmptyView(emptyListView);

            Glide.with(this)
                    .load(ThemeUtils.getEmptyBgResId(prefs))
                    .asBitmap()
                    .thumbnail(0.4f)
                    .into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                        @Override
                        public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                            emptyListView.setBackground(new BitmapDrawable(bitmap));
                        }
                    });


            TextView tutorial = (TextView) findViewById(R.id.tvTutorial);
            assert tutorial != null;
            tutorial.setTypeface(tLight);
        }
    }

    @Override
    public boolean process(Intent cptIntent) throws IOException, ClassNotFoundException {
        super.process(cptIntent);

        switch (cptIntent.getAction()) {
            case Communication.ACTION_MESSAGE_ATTACHMENT_DOWNLOADED:
                final MessageAttachment messageAttachment = cptIntent.getParcelableExtra(Communication.EXTRA_MESSAGE_ATTACHMENT);
                if (messageAttachment != null && messageAttachment instanceof DownloadedAttachment) {
                    updateHistory(true);
                }
                break;
            case Communication.ACTION_MESSAGE_ARRIVED:
                final IncomingMessage message = cptIntent.getParcelableExtra(Communication.EXTRA_MESSAGE_ARRIVED);
                final Serializable data = message.getPayload().getAppData();

                if (data instanceof Comment || data instanceof VoteUp) {
                    updateHistory(true);
                } else if (data instanceof RMBProfile) {
                    updateHistory(false);
                } else if (data instanceof Status) {
                    if (appData.getTimelineDataSource().getAllTimeLineInfos(appData).size() != timelineInfos.size())
                        updateHistory(true);
                }
        }
        return false;
    }

    // updates the history of RMB votes
    private void updateHistory(boolean scrollToBottom) {
        if (lvVotesAdapter != null) {
//            int firstVisible = lvTimeline.getFirstVisiblePosition();

            timelineInfos.clear();
            timelineInfos.addAll(appData.getTimelineDataSource().getAllTimeLineInfos(appData));
            lvVotesAdapter.notifyDataSetChanged();
            lvTimeline.setSelection(timelineInfos.size() - 1);

            //lvTimeline.setSelection(scrollToBottom ? lvVotesAdapter.getCount() - 1 : firstVisible);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        updateHistory(false);

        if(mForceScrollAtStart) {
            mForceScrollAtStart = false;
            lvTimeline.setSelection(lvVotesAdapter.getCount() - 1);
        }
    }

}
