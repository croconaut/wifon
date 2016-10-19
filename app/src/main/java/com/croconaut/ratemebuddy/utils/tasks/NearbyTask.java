package com.croconaut.ratemebuddy.utils.tasks;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.croconaut.cpt.data.Communication;
import com.croconaut.cpt.data.NearbyUser;
import com.croconaut.ratemebuddy.AppData;
import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.activities.CptProcessor;
import com.croconaut.ratemebuddy.activities.notifications.NearbyNotification;
import com.croconaut.ratemebuddy.utils.pojo.profiles.Profile;

import java.io.IOException;
import java.util.ArrayList;


public class NearbyTask extends AsyncTask<Intent, Void, Intent> {
    private static final String TAG = NearbyTask.class.getName();

    private final AppData appData;

    public NearbyTask(AppData appData) {
        this.appData = appData;
    }

    @Override
    protected Intent doInBackground(Intent... params) {
        Intent intent = params[0];


        final ArrayList<NearbyUser> nearbyUsers = intent.getParcelableArrayListExtra(Communication.EXTRA_NEARBY_ARRIVED);

        final ArrayList<Profile> nearbyProfiles = new ArrayList<>();
        for (NearbyUser nearbyUser : nearbyUsers) {
            Profile nearByProfile = new Profile.Builder(nearbyUser.crocoId)
                    .addName(nearbyUser.username)
                    .build();

            nearbyProfiles.add(nearByProfile);
        }

        boolean nearbyChanged = appData.getNearbyPeople().retainAll(nearbyProfiles);

        for (NearbyUser nearbyUser : nearbyUsers) {
            Profile nearByProfile = new Profile.Builder(nearbyUser.crocoId)
                    .addName(nearbyUser.username)
                    .build();
            boolean hasName = nearbyUser.username != null && !nearbyUser.username.equals("null");

            if (isProfileBlocked(nearByProfile)) return null;

            Profile profileDB = appData.getProfileDataSource().getProfileByCrocoId(nearByProfile.getCrocoId());
            if (profileDB != null) {
                if (hasName && !profileDB.getName().equals(nearByProfile.getName())) {
                    profileDB = new Profile.Builder(profileDB).addName(nearByProfile.getName()).build();
                    appData.getProfileDataSource().updateProfile(profileDB);
                    nearbyChanged = appData.syncProfileToNearby(profileDB);
                }

                if (!appData.getNearbyPeople().contains(profileDB)) {
                    nearbyChanged = appData.getNearbyPeople().add(profileDB);
                }

                checkForNearbyNotif(profileDB);
            } else {
                if (!appData.getNearbyPeople().contains(nearByProfile)) {
                    nearbyChanged = appData.getNearbyPeople().add(nearByProfile);
                    checkForNearbyNotif(nearByProfile);
                } else {
                    int index = appData.getNearbyPeople().indexOf(nearByProfile);
                    Profile fromNearby = appData.getNearbyPeople().get(index);

                    if (hasName && !nearByProfile.getName().equals(fromNearby.getName())) {
                        fromNearby = new Profile.Builder(fromNearby)
                                .addName(nearByProfile.getName())
                                .build();
                        nearbyChanged = appData.syncProfileToNearby(fromNearby);
                        checkForNearbyNotif(fromNearby);
                    } else nearbyChanged = false;

                }
            }
        }

        return nearbyChanged ? intent : null;
    }

    private void checkForNearbyNotif(Profile notifProfile) {
        Log.e(TAG, "Profile name: " + notifProfile.getName());
        //check if we should display nearby notification, if yes -> show it
        if (!notifProfile.getName().equals(appData.getResources().getString(R.string.profile_unknown_name))
                && NearbyNotification.showNearbyNotif(appData))
            new NearbyNotification(appData, notifProfile);
    }

    @Override
    protected void onPostExecute(Intent intent) {
        super.onPostExecute(intent);

        CptProcessor cptProcessor = appData.getCurrentActivity();

        if (intent == null || cptProcessor == null) return;

        try {
            cptProcessor.process(intent);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private boolean isProfileBlocked(Profile profile) {
        return appData.getProfileDataSource().getProfilesByType(Profile.BLOCKED).contains(profile);
    }
}
