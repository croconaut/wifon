package com.croconaut.ratemebuddy.activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.ui.adapters.PeopleRecyclerViewAdapter;
import com.croconaut.ratemebuddy.utils.Comparators;
import com.croconaut.ratemebuddy.utils.ThemeUtils;
import com.croconaut.ratemebuddy.utils.pojo.profiles.Profile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class PeopleFragment extends Fragment {
    private static final String TAG = PeopleFragment.class.getName();
    private static final String ARG_SECTION_NUMBER = "section_number";

    private static final int SORT_BY_RATING = 0;
    private static final int SORT_ALPHABETICALLY = 1;
    private static final int SORT_FAVOURITES = 2;

    // hold the sorting value
    private int sortType;
    // current display type
    private DisplayType type;

    private ArrayList<Profile> mProfiles;
    private RecyclerView rvNearby;
    private PeopleRecyclerViewAdapter rvAdapter;
    private View rootView;
    private RelativeLayout rlEmptyView;

    //instance of PeopleActivity
    private PeopleParentActivityBB activity;

    public PeopleFragment() {
    }


    public static PeopleFragment newInstance(DisplayType displayType) {
        PeopleFragment fragment = new PeopleFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, displayType.ordinal());
        fragment.setArguments(args);
        return fragment;
    }

    public void updateContent() {
        Log.e(TAG, "Updating UI updateContent for type: " + type);
        if(mProfiles == null){
            Log.e(TAG, "mProfiles are null, cannot perform update");
            return;
        }

        mProfiles.clear();
        mProfiles.addAll(getProfiles(type));
        activity.toolbar.setTitle(type.toString(activity, mProfiles.size()));

        if(mProfiles.isEmpty()){
            setEmptyTextAndBackground();
        }

        rvNearby.setVisibility(mProfiles.isEmpty() ? View.GONE : View.VISIBLE);
        rlEmptyView.setVisibility(mProfiles.isEmpty() ? View.VISIBLE : View.GONE);

        Log.e(TAG, "Profiles: " + mProfiles);
        rvAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkArguments();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        updateContent();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity = (PeopleParentActivityBB) getActivity();
        rootView = inflater.inflate(R.layout.fragment_people, container, false);
        initializeViewsAndResources();
        checkArguments();
        setHasOptionsMenu(true);

        sortType = SORT_BY_RATING;

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateContent();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.action_bar_menu, menu);
    }

    private void checkArguments() {
        switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
            case 0:
                type = DisplayType.FAVOURITES;
                break;
            case 2:
                type = DisplayType.UNKNOWN;
                break;
            case 3:
                type = DisplayType.UNREAD;
                break;
            default:
                type = DisplayType.NEARBY;
                break;
        }
    }

    // sort nearby people
    private void sortNearby(int sortType) {
        Log.e(TAG, "Current sort TYPE:" + sortType);
        this.sortType = sortType;
        updateContent();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuSortAlphabetically:
                sortNearby(SORT_ALPHABETICALLY);
                return true;
            case R.id.menuSortRating:
                sortNearby(SORT_BY_RATING);
                return true;
            case R.id.menuSortFavourites:
                sortNearby(SORT_FAVOURITES);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void performSearch(String query) {
        Log.e(TAG, "Performing search");

        ArrayList<Profile> filteredProfiles = new ArrayList<>();
        mProfiles = getProfiles(type);

        for (Profile profile : mProfiles) {
            // Content to search through (in lower case)
            String name = (profile.getName()).toLowerCase();

            if (name.contains(query.toLowerCase())) {
                filteredProfiles.add(profile);
            }
        }


        rvAdapter = new PeopleRecyclerViewAdapter(filteredProfiles, activity, rvNearby);
        rvNearby.setAdapter(rvAdapter);
    }

    //initiliaze all view and resources used in this activity
    private void initializeViewsAndResources() {

        rvNearby = (RecyclerView) rootView.findViewById(R.id.myList);
        rlEmptyView = (RelativeLayout) rootView.findViewById(R.id.emptylistViewNearby);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(activity.getBaseContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvNearby.setLayoutManager(layoutManager);
        mProfiles = getProfiles(type);
        rvAdapter = new PeopleRecyclerViewAdapter(mProfiles, activity, rvNearby);
        rvNearby.setAdapter(rvAdapter);
        rvNearby.setBackgroundColor(ContextCompat.getColor(getContext() ,R.color.material_white));

        if (mProfiles.isEmpty()) {
            rvNearby.setVisibility(View.GONE);
            setEmptyTextAndBackground();
            rlEmptyView.setVisibility(View.VISIBLE);
        }

        activity.toolbar.setTitle(type.toString(activity, mProfiles.size()));
    }

    private void setEmptyTextAndBackground() {
        TextView tvEmptyView = (TextView) rootView.findViewById(R.id.tvEmptylistViewNearby);
        tvEmptyView.setTypeface(activity.tLight);

        switch (type) {
            case FAVOURITES:
                tvEmptyView.setText(activity.mRes.getString(R.string.emptyFavourites));
                break;
            case UNKNOWN:
                tvEmptyView.setText(activity.mRes.getString(R.string.emptyUnknown));
                break;
            case NEARBY:
                tvEmptyView.setText(activity.mRes.getString(R.string.emptyNearby));
                break;
            case UNREAD:
                tvEmptyView.setText(activity.mRes.getString(R.string.emptyUnread));
        }

        Glide.with(this)
                .load(ThemeUtils.getEmptyBgResId(PreferenceManager.getDefaultSharedPreferences(activity.getAppData())))
                .asBitmap()
                .thumbnail(0.4f)
                .into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                        rlEmptyView.setBackground(new BitmapDrawable(bitmap));
                    }
                });
    }

    public ArrayList<Profile> getProfiles(DisplayType type) {
        ArrayList<Profile> profiles = new ArrayList<>();

        switch (type) {
            case FAVOURITES:
                profiles.addAll(activity.appData.getProfileDataSource().getProfilesByType(Profile.FAVOURITE));
                break;
            case UNKNOWN:
                profiles.addAll(activity.appData.getProfileDataSource().getProfilesByType(Profile.UNKNOWN));
                break;
            case NEARBY:
                profiles.addAll(activity.appData.getNearbyPeople());
                break;
            case UNREAD:
                profiles.addAll(activity.appData.getProfileDataSource().getUnreadProfiles());
                break;
        }

        Collections.sort(profiles, getCurrentComparator());
        if (type == DisplayType.NEARBY)
            Log.e(TAG, "Sorted: " + profiles.toString());
        return profiles;
    }

    private Comparator<? super Profile> getCurrentComparator() {
        switch (sortType) {
            case SORT_ALPHABETICALLY:
                return Comparators.profileNameComparator;
            case SORT_BY_RATING:
                return Comparators.profileRatingComparator;
            case SORT_FAVOURITES:
                return Comparators.profileFavouriteComparator;
        }
        return Comparators.profileRatingComparator;
    }

    public enum DisplayType {
        FAVOURITES, NEARBY, UNKNOWN, UNREAD;

        public String toString(Context context, int profilesSize) {
            switch (this) {
                case FAVOURITES:
                    return context.getResources().getString(
                            profilesSize == 0
                                    ? R.string.action_drawer_bb_favourites
                                    : R.string.action_drawer_favourites,
                            profilesSize
                    );
                case NEARBY:
                    return context.getResources().getString(R.string.action_drawer_nearby);
                case UNKNOWN:
                    return context.getResources().getString(R.string.action_drawer_unknown);
                case UNREAD:
                    return context.getResources().getString(R.string.action_drawer_unread);
            }
            return null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateContent();
    }
}
