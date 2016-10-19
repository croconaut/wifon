package com.croconaut.ratemebuddy.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.croconaut.cpt.data.Communication;
import com.croconaut.ratemebuddy.AppData;
import com.croconaut.ratemebuddy.ExternalUriContract;
import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.activities.notifications.NearbyNotification;
import com.croconaut.ratemebuddy.activities.notifications.Notification;
import com.croconaut.ratemebuddy.utils.ProfileUtils;
import com.croconaut.ratemebuddy.utils.ThemeManager;
import com.croconaut.ratemebuddy.utils.pojo.profiles.MyProfile;
import com.croconaut.ratemebuddy.utils.pojo.profiles.Profile;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.BottomBarTab;
import com.roughike.bottombar.OnTabSelectListener;


import java.io.IOException;


public class PeopleParentActivityBB extends WifonActivity implements CptProcessor {
    private static final String TAG = PeopleParentActivityBB.class.getName();

    public static final String EXTRA_SCROLL_TO_PAGE = "EXTRA_SCROLL_TO_PAGE";

    private BottomBar mBottomBar;
    private PeopleFragment mCurrentFragment;
    private PeopleFragment.DisplayType mCurrentType = null;
    private SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people_bb);

        initializeHeaderWithDrawer(getString(R.string.drawer_item_people), true);
        addSearchView();

        mBottomBar = (BottomBar) findViewById(R.id.bottomBar);

        mBottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                PeopleFragment.DisplayType displayType = null;
                switch (tabId) {
                    case R.id.bb_menu_favorites:
                        displayType = PeopleFragment.DisplayType.FAVOURITES;
                        break;
                    case R.id.bb_menu_nearby:
                        displayType = PeopleFragment.DisplayType.NEARBY;
                        break;
                    case R.id.bb_menu_unknown:
                        displayType = PeopleFragment.DisplayType.UNKNOWN;
                        break;
                    case R.id.bb_menu_unread:
                        displayType = PeopleFragment.DisplayType.UNREAD;
                        break;
                }

                replaceFragment(PeopleFragment.newInstance(displayType), displayType);

                if (mSearchView != null) {
                    mSearchView.setQuery("", false);
                    mSearchView.setIconified(true);
                }
            }
        });


        setBottomBarBadges();

        checkExtras(this.getIntent());
    }

    @Override
    public boolean process(Intent cptIntent) throws IOException, ClassNotFoundException {
        super.process(cptIntent);

        if (mCurrentFragment != null)
            mCurrentFragment.updateContent();

        updateBadges();
        return false;
    }


    private void updateBadges() {
        setBottomBarBadges();
    }

    private void setBottomBarBadges() {
        final int nearbyCount = appData.getNearbyPeople().size();
        mBottomBar.getTabWithId(R.id.bb_menu_nearby)
                .setBadgeCount(nearbyCount);

        final int unreadCount = ProfileUtils.getUnreadMessagesCount(
                appData.getProfileDataSource().getUnreadProfiles());
        mBottomBar.getTabWithId(R.id.bb_menu_unread).setBadgeCount(unreadCount);
    }

    private void replaceFragment(@NonNull PeopleFragment peopleFragment, PeopleFragment.DisplayType type) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (mCurrentType == null || mCurrentType.ordinal() > type.ordinal()) {
            transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        } else if (mCurrentType.ordinal() < type.ordinal()) {
            transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        }
        transaction.replace(R.id.fragment_container, peopleFragment);
        transaction.commit();

        mCurrentFragment = peopleFragment;
        mCurrentType = type;

        updateBadges();
    }

    private void addSearchView() {
        mSearchView = (SearchView) getLayoutInflater().inflate(R.layout.search_view, null);
        toolbar.addView(mSearchView);
        mSearchView.setLayoutParams(new Toolbar.LayoutParams(Gravity.END));
        mSearchView.setOnQueryTextListener(new SearchHandler());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        MenuItem item = menu.findItem(R.id.menuSort);
        item.setVisible(true);
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        drawerLayout.closeDrawer(Gravity.LEFT);
    }

    @Override
    public void onResume() {
        super.onResume();

        Notification.clearNotification(mContext, NearbyNotification.NEARBY_NOTIF_ID);
        if (mCurrentFragment != null && mCurrentFragment.isVisible())
            mCurrentFragment.updateContent();

        updateBadges();
    }

    @Override
    public void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null)
            return;

        checkExtras(intent);
    }

    private void checkExtras(Intent intent) {
        this.setIntent(intent);

        if (intent.getExtras().isEmpty()) {
            Log.e(TAG, "Extras are null");
            return;
        } else {
            Log.e(TAG, "Continue check extra ");
        }

        if (intent.hasExtra(EXTRA_SCROLL_TO_PAGE)) {
            int scrollTo = intent.getIntExtra(EXTRA_SCROLL_TO_PAGE,
                    PeopleFragment.DisplayType.NEARBY.ordinal());
            intent.removeExtra(EXTRA_SCROLL_TO_PAGE);

            PeopleFragment.DisplayType displayType;
            switch (scrollTo) {
                case 0:
                    displayType = PeopleFragment.DisplayType.FAVOURITES;
                    break;
                case 1:
                    displayType = PeopleFragment.DisplayType.NEARBY;
                    break;
                case 2:
                    displayType = PeopleFragment.DisplayType.UNKNOWN;
                    break;
                case 3:
                    displayType = PeopleFragment.DisplayType.UNREAD;
                    break;
                default:
                    displayType = PeopleFragment.DisplayType.FAVOURITES;
            }

            mCurrentFragment = PeopleFragment.newInstance(displayType);
            mCurrentType = displayType;

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
            transaction.replace(R.id.fragment_container, mCurrentFragment);
            transaction.commitAllowingStateLoss();

            mBottomBar.selectTabAtPosition(displayType.ordinal());
        }

        checkStartFromShare();
    }

    public AppData getAppData() {
        return appData;
    }

    public void sendLike(Profile profile) {
        sendVoteUp(profile);
    }

    public void updateCurrentFragment() {
        if (mCurrentFragment != null) mCurrentFragment.updateContent();
    }


    /**
     * Util classes and methods
     */

    private void checkStartFromShare() {
        Uri data = getIntent().getData();

        if (data == null) {
            Log.e(TAG, "String data is null => not SMS start");
            return;
        }

        if (data.getAuthority().equals(ExternalUriContract.AUTHORITY)) {
            mCurrentFragment = PeopleFragment.newInstance(PeopleFragment.DisplayType.FAVOURITES);

            String name = data.getQueryParameter(ExternalUriContract.PARAM_PROFILE_NAME);
            final String crocoId = data.getQueryParameter(ExternalUriContract.PARAM_PROFILE_CROCO_ID);

            final Profile remoteProfile =
                    (Profile) profileUtils.findProfile(crocoId, name);


            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:

                            remoteProfile.setType(Profile.FAVOURITE);


                            if (appData.getProfileDataSource().isProfileInDB(crocoId))
                                appData.getProfileDataSource().updateProfile(remoteProfile);
                            else appData.getProfileDataSource().insertProfile(remoteProfile);

                            try {
                                MyProfile myProfile = MyProfile.getInstance(mContext);
                                ProfileUtils.createAndSendRmbProfile(
                                        appData,
                                        myProfile,
                                        myProfile.getThumbUri()
                                );
                            }catch (IOException e){
                                Log.e(TAG, "Cannot create RMPProfile: ", e);
                            }

                            Communication.changeUserTrustLevel(
                                    mContext,
                                    remoteProfile.getCrocoId(),
                                    Communication.USER_TRUST_LEVEL_TRUSTED
                            );

                            Intent intent = new Intent(mContext, CommunicationActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra(CommunicationActivity.EXTRA_SHOW_PREDEFINED_MSG, true);
                            intent.putExtra(CommunicationActivity.EXTRA_TARGET_CROCO_ID, crocoId);
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                            startActivity(intent);
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            dialog.cancel();
                            break;
                    }
                }
            };


            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

            builder.setIcon(R.drawable.ic_launcher)
                    .setTitle(mRes.getString(R.string.dialog_invite_profile_title))
                    .setMessage(mRes.getString(R.string.dialog_invite_profile_text, remoteProfile.getName()))
                    .setPositiveButton(mRes.getString(R.string.dialog_invite_profile_possitive), dialogClickListener)
                    .setNegativeButton(mRes.getString(R.string.dialog_invite_profile_negative), dialogClickListener)
                    .show();

        }
    }


    private class SearchHandler implements SearchView.OnQueryTextListener {

        @Override
        public boolean onQueryTextChange(final String query) {
            Log.e(TAG, "Current query: " + query);
            if (!TextUtils.isEmpty(query))
                mCurrentFragment.performSearch(query);
            else Log.e(TAG, "Empty query, not performing search!");
            return false;
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

    }
}
