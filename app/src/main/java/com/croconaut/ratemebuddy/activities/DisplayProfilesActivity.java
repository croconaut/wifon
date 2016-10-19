package com.croconaut.ratemebuddy.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.croconaut.cpt.data.Communication;
import com.croconaut.cpt.data.IncomingMessage;
import com.croconaut.ratemebuddy.AppData;
import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.data.pojo.RMBProfile;
import com.croconaut.ratemebuddy.data.pojo.VoteUp;
import com.croconaut.ratemebuddy.ui.adapters.DisplayProfileAdapter;
import com.croconaut.ratemebuddy.utils.pojo.profiles.IProfile;
import com.croconaut.ratemebuddy.utils.pojo.profiles.MyProfile;
import com.croconaut.ratemebuddy.utils.pojo.profiles.Profile;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DisplayProfilesActivity extends WifonActivity implements CptProcessor {

    private static final String TAG = DisplayProfilesActivity.class.getName();

    public static String EXTRA_REMOTE_PROFILE_ID = "remoteProfile";
    public static String EXTRA_DISPLAY_TYPE = "displayType";

    public static int DISPLAY_FRIENDS = 0;
    public static int DISPLAY_LIKES = 1;

    private IProfile remoteProfile;
    private int displayType;
    private DisplayProfileAdapter rvAdapter;
    private List<IProfile> mProfiles;
    private MyProfile mMyProfile;
    private RecyclerView rvPhotos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_profiles);

        if (getIntent() != null) {
            remoteProfile = profileUtils.findProfile(getIntent().getStringExtra(EXTRA_REMOTE_PROFILE_ID));
            displayType = getIntent().getIntExtra(EXTRA_DISPLAY_TYPE, DISPLAY_FRIENDS);
        }

        mMyProfile = MyProfile.getInstance(appData);

        initializeHeaderWithDrawer(getDisplayTitle(), false);
        initializeViews();
    }

    private String getDisplayTitle() {
        return displayType == DISPLAY_FRIENDS
                ? getString(R.string.activity_display_title_friends)
                : getString(R.string.activity_display_title_likes);
    }

    private List<IProfile> getDataSet() {
        return displayType == DISPLAY_FRIENDS
                ? getFriendsProfiles()
                : getVoteProfiles();
    }

    //oh my god, so bad ... just a quick fix
    private List<IProfile> getFriendsProfiles() {
        List<IProfile> friendsProfiles = new ArrayList<>();


        if (!(remoteProfile instanceof MyProfile)) {

            for (IProfile profile : ((Profile) remoteProfile).getFriendsList()) {

                String identToCompare = profile instanceof Profile
                        ? ((Profile) profile).getProfileId()
                        : mMyProfile.getProfileId();
                final boolean isMyProfile = identToCompare != null && identToCompare.equals(mMyProfile.getIdent());

                if (isMyProfile)
                    friendsProfiles.add(mMyProfile);
                else {
                    friendsProfiles.add(profileUtils.findProfile(
                            profile.getIdent(),
                            profile.getName())
                    );
                }
            }

            return friendsProfiles;
        }

        List<Profile> sourceList =
                appData.getProfileDataSource().getProfilesByType(Profile.FAVOURITE);

        for (IProfile profile : sourceList) {
            if (profile.getIdent() != null &&
                    profile.getIdent().equals(mMyProfile.getProfileId()))
                friendsProfiles.add(mMyProfile);
            else
                friendsProfiles.add(profileUtils.findProfile(
                        profile.getIdent(),
                        profile.getName())
                );
        }

        return friendsProfiles;
    }

    private List<IProfile> getVoteProfiles() {
        List<IProfile> voteUpProfiles = new ArrayList<>();
        for (VoteUp voteUp : remoteProfile.getStatus().getVotes()) {
            if (voteUp.getProfileId().equals(mMyProfile.getProfileId()))
                voteUpProfiles.add(mMyProfile);
            else
                voteUpProfiles.add(profileUtils.findProfileByIdOrCrocoId(
                        voteUp.getCrocoId(),
                        voteUp.getProfileId(),
                        voteUp.getProfileName())
                );
        }
        return voteUpProfiles;
    }


    private void initializeViews() {
        rvPhotos = (RecyclerView) findViewById(R.id.rvPhotos);
        if (remoteProfile != null && rvPhotos != null) {
            final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            rvPhotos.setLayoutManager(layoutManager);


            mProfiles = getDataSet();
            rvAdapter = new DisplayProfileAdapter(appData, mProfiles);
            rvPhotos.setAdapter(rvAdapter);
        }
    }

    public AppData getAppData() {
        return appData;
    }

    @Override
    public boolean process(Intent cptIntent) throws IOException, ClassNotFoundException {
        super.process(cptIntent);

        switch (cptIntent.getAction()) {
            case Communication.ACTION_MESSAGE_ARRIVED:
                Log.e(TAG, "ACTION_MESSAGE_ARRIVED");

                final IncomingMessage message = cptIntent.getParcelableExtra(Communication.EXTRA_MESSAGE_ARRIVED);
                Serializable data = message.getPayload().getAppData();

                if (data instanceof RMBProfile) {
                    Log.e(TAG, "UPDATING");
                    mProfiles = getDataSet();
                    rvAdapter = new DisplayProfileAdapter(appData, mProfiles);
                    rvPhotos.setAdapter(rvAdapter);
                }
                break;
        }
        return false;
    }
}
