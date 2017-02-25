package com.croconaut.ratemebuddy;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDelegate;

import com.croconaut.ratemebuddy.activities.CptProcessor;
import com.croconaut.ratemebuddy.data.ProfileDataSource;
import com.croconaut.ratemebuddy.data.StatusDataSource;
import com.croconaut.ratemebuddy.data.TimelineDataSource;
import com.croconaut.ratemebuddy.data.UIMessageDataSource;
import com.croconaut.ratemebuddy.utils.pojo.profiles.Profile;
import com.croconaut.tictactoe.communication.GameCommunication;
import com.croconaut.tictactoe.storage.GameRepository;

import java.util.ArrayList;

public class AppData extends Application {
    private static Context mAppContext;

    private ArrayList<Profile> nearbyPeople = new ArrayList<>();

    private StatusDataSource statusDataSource;
    private ProfileDataSource profileDataSource;
    private UIMessageDataSource uiMessageDataSource;
    private TimelineDataSource timelineDataSource;

    private GameCommunication gameCommunication;

    private CptProcessor mCurrentActivity;
    private ActivityLifecycleCallbacks mActivityLifecycleCallbacks = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
        }

        @Override
        public void onActivityResumed(Activity activity) {
            mCurrentActivity = activity instanceof CptProcessor ? (CptProcessor) activity : null;
        }

        @Override
        public void onActivityPaused(Activity activity) {
            mCurrentActivity = null;
        }

        @Override
        public void onActivityStopped(Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        mAppContext = getApplicationContext();
        init();


//        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
//        List<WifiConfiguration> networks = wm.getConfiguredNetworks();
//        if (networks != null) {
//            for (WifiConfiguration wifiConfiguration : networks) {
//                wm.disableNetwork(wifiConfiguration.networkId);
//            }
//        }
    }

    private void init() {
        uiMessageDataSource = new UIMessageDataSource(this);
        uiMessageDataSource.open();

        statusDataSource = new StatusDataSource(this);
        statusDataSource.open();

        profileDataSource = new ProfileDataSource(this);
        profileDataSource.open();

        timelineDataSource = new TimelineDataSource(this);
        timelineDataSource.open();

        gameCommunication = new GameCommunication(this);

        registerActivityLifecycleCallbacks(mActivityLifecycleCallbacks);
    }

    public ArrayList<Profile> getNearbyPeople() {
        return (nearbyPeople == null) ? new ArrayList<Profile>() : nearbyPeople;
    }

    public ProfileDataSource getProfileDataSource() {
        return profileDataSource;
    }

    public StatusDataSource getStatusDataSource() {
        return statusDataSource;
    }

    public UIMessageDataSource getUiMessageDataSource() {
        return uiMessageDataSource;
    }

    public TimelineDataSource getTimelineDataSource() {
        return timelineDataSource;
    }

    @NonNull
    public GameCommunication getGameCommunication() {
        return gameCommunication;
    }


    @NonNull
    public GameRepository getGameRepository() {
        return gameCommunication.getGameRepository();
    }

    public boolean syncProfileToNearby(Profile remoteProfile) {
        if (getNearbyPeople().contains(remoteProfile)) {
            int index = getNearbyPeople().indexOf(remoteProfile);
            getNearbyPeople().remove(index);
            getNearbyPeople().add(remoteProfile);
            return true;
        }
        return false;
    }

    public CptProcessor getCurrentActivity() {
        return mCurrentActivity;
    }

    public static Context getAppContext() {
        return mAppContext;
    }
}
