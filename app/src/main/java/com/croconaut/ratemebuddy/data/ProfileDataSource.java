package com.croconaut.ratemebuddy.data;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.croconaut.ratemebuddy.AppData;
import com.croconaut.ratemebuddy.data.pojo.Status;
import com.croconaut.ratemebuddy.utils.CommonUtils;
import com.croconaut.ratemebuddy.utils.pojo.UIMessage;
import com.croconaut.ratemebuddy.utils.pojo.profiles.IProfile;
import com.croconaut.ratemebuddy.utils.pojo.profiles.Profile;
import com.croconaut.ratemebuddy.utils.pojo.profiles.states.ActualState;

import java.util.ArrayList;
import java.util.List;

public class ProfileDataSource {
    private static final String TAG = ProfileDataSource.class.getName();

    private ProfileSQLiteHelper helper;
    private SQLiteDatabase db;
    private String table;
    private AppData appData;

    public ProfileDataSource(AppData appData) {
        helper = new ProfileSQLiteHelper(appData);
        table = ProfileSQLiteHelper.PROFILES_TABLE;
        this.appData = appData;
    }

    public void open() {
        db = helper.getWritableDatabase();
    }

    public void close() {
        helper.close();
    }

    private List<Profile> getProfiles(Cursor cursor) {
        ArrayList<Profile> profiles = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            int crocoIdCol = cursor.getColumnIndex(ProfileSQLiteHelper.COLUMN_CROCO_ID);
            int nameCol = cursor.getColumnIndex(ProfileSQLiteHelper.COLUMN_NAME);
            int statusCol = cursor.getColumnIndex(ProfileSQLiteHelper.COLUMN_STATUS);
            int typeCol = cursor.getColumnIndex(ProfileSQLiteHelper.COLUMN_TYPE);
            int unreadMessCol = cursor.getColumnIndex(ProfileSQLiteHelper.COLUMN_UNREAD_MESS);
            int timeStampCol = cursor.getColumnIndex(ProfileSQLiteHelper.COLUMN_TIMESTAMP);
            int photoThumbCol = cursor.getColumnIndex(ProfileSQLiteHelper.COLUMN_PHOTO_URI);
            int actualCol = cursor.getColumnIndex(ProfileSQLiteHelper.COLUMN_ACTUAL_STATE);
            int unreadCountCol = cursor.getColumnIndex(ProfileSQLiteHelper.COLUMN_UNREAD_COUNT);
            int showUploadCol = cursor.getColumnIndex(ProfileSQLiteHelper.COLUMN_SHOW_UPLOAD_DIALOG);
            int friendsCol = cursor.getColumnIndex(ProfileSQLiteHelper.COLUMN_FRIENDS);
            int profileIdCol = cursor.getColumnIndex(ProfileSQLiteHelper.COLUMN_PROFILE_ID);
            int showInFriendsCol = cursor.getColumnIndex(ProfileSQLiteHelper.COLUMN_ENABLE_SHOW_IN_FRIENDS);

            do {
                String crocoId = cursor.getString(crocoIdCol);
                String name = cursor.getString(nameCol);
                Status status = (Status) CommonUtils.deserializeObject(cursor.getBlob(statusCol));
                ArrayList<UIMessage> unreadMessages = (ArrayList<UIMessage>) CommonUtils.deserializeObject(cursor.getBlob(unreadMessCol));
                List<IProfile> friendsList = (List<IProfile>) CommonUtils.deserializeObject(cursor.getBlob(friendsCol));
                String thumbnail = cursor.getString(photoThumbCol);
                int type = cursor.getInt(typeCol);
                long timeStamp = cursor.getLong(timeStampCol);
                ActualState actualState = (ActualState) CommonUtils.deserializeObject(cursor.getBlob(actualCol));
                int unread = cursor.getInt(unreadCountCol);
                int showUploadDialog = cursor.getInt(showUploadCol);
                int showInFriendsEnabled = cursor.getInt(showInFriendsCol);
                String profileId = cursor.getString(profileIdCol);

                Profile profile = new Profile.Builder(crocoId)
                        .addActualState(actualState)
                        .addProfileId(profileId)
                        .addName(name)
                        .addStatus(status)
                        .addUnreadMessages(unreadMessages)
                        .addUri(thumbnail == null ? null : Uri.parse(thumbnail))
                        .addTimestamp(timeStamp)
                        .addType(type)
                        .addUnread(unread)
                        .showUploadDialog(showUploadDialog)
                        .setFriendsList(friendsList)
                        .enableInFriends(showInFriendsEnabled)
                        .build();
                profiles.add(profile);

            } while (cursor.moveToNext());
        }
        return profiles;
    }

    public Profile getProfileByCrocoId(String crocoId) {
        Cursor cur = db.query(table, null, ProfileSQLiteHelper.COLUMN_CROCO_ID + " = ?", new String[]{crocoId}, null, null, null);
        try {
            List<Profile> messages = getProfiles(cur);
            return !messages.isEmpty() ? messages.get(0) : null;
        } finally {
            if (cur != null && !cur.isClosed()) cur.close();
        }
    }

    public void updateUnread(String crocoId, int count, ArrayList<UIMessage> unreadMessages) {
        Log.e(ProfileDataSource.class.getCanonicalName(), "Updating unread to: " + count);
        ContentValues vals = new ContentValues();
        vals.put(ProfileSQLiteHelper.COLUMN_UNREAD_COUNT, count);
        vals.put(ProfileSQLiteHelper.COLUMN_UNREAD_MESS, CommonUtils.serializeObject(unreadMessages));

        String where = ProfileSQLiteHelper.COLUMN_CROCO_ID + " = ?";
        String[] whereArgs = {crocoId};

        if (db.update(table, vals, where, whereArgs) > 0) {
            appData.syncProfileToNearby(getProfileByCrocoId(crocoId));
        }
    }

    public void removeUnread(String crocoId) {
        updateUnread(crocoId, 0, new ArrayList<UIMessage>());
    }

    public void insertProfile(Profile profile) {
        ContentValues vals = new ContentValues();
        vals.put(ProfileSQLiteHelper.COLUMN_CROCO_ID, profile.getCrocoId());
        vals.put(ProfileSQLiteHelper.COLUMN_NAME, profile.getName());
        vals.put(ProfileSQLiteHelper.COLUMN_TIMESTAMP, profile.getTimeStamp());
        vals.put(ProfileSQLiteHelper.COLUMN_STATUS, CommonUtils.serializeObject(profile.getStatus()));
        vals.put(ProfileSQLiteHelper.COLUMN_UNREAD_MESS, CommonUtils.serializeObject(profile.getUnreadMessages()));
        vals.put(ProfileSQLiteHelper.COLUMN_TYPE, profile.getType());
        vals.put(ProfileSQLiteHelper.COLUMN_PROFILE_ID, profile.getProfileId());
        vals.put(ProfileSQLiteHelper.COLUMN_UNREAD_COUNT, profile.getUnread());
        vals.put(ProfileSQLiteHelper.COLUMN_PHOTO_URI, profile.getThumbUri() == null ? null : profile.getThumbUri().toString());
        vals.put(ProfileSQLiteHelper.COLUMN_ACTUAL_STATE, CommonUtils.serializeObject(profile.getActualState()));
        vals.put(ProfileSQLiteHelper.COLUMN_SHOW_UPLOAD_DIALOG, profile.getShowUploadDialog());
        vals.put(ProfileSQLiteHelper.COLUMN_ENABLE_SHOW_IN_FRIENDS, profile.getShowInFriendsEnabled());
        vals.put(ProfileSQLiteHelper.COLUMN_FRIENDS, CommonUtils.serializeObject(profile.getFriendsList()));

        if (db.insert(table, null, vals) > 0) {
            appData.syncProfileToNearby(profile);
        }
    }

    public void updateProfile(Profile profile) {
        ContentValues vals = new ContentValues();
        if (profile.getName() != null)
            vals.put(ProfileSQLiteHelper.COLUMN_NAME, profile.getName());
        if (profile.getType() != 0)
            vals.put(ProfileSQLiteHelper.COLUMN_TYPE, profile.getType());
        if (profile.getProfileId() != null)
            vals.put(ProfileSQLiteHelper.COLUMN_PROFILE_ID, profile.getProfileId());
        if (profile.getTimeStamp() != 0)
            vals.put(ProfileSQLiteHelper.COLUMN_TIMESTAMP, profile.getTimeStamp());
        if (profile.getStatus() != null) {
            Log.e(TAG, "Updating profile " + profile.getName() + " with status " + profile.getStatus());
            vals.put(ProfileSQLiteHelper.COLUMN_STATUS, CommonUtils.serializeObject(profile.getStatus()));
        }
        if (profile.getThumbUri() != null)
            vals.put(ProfileSQLiteHelper.COLUMN_PHOTO_URI, profile.getThumbUri().toString());
        if (profile.getActualState() != null)
            vals.put(ProfileSQLiteHelper.COLUMN_ACTUAL_STATE, CommonUtils.serializeObject(profile.getActualState()));
        if (profile.getFriendsList() != null)
            vals.put(ProfileSQLiteHelper.COLUMN_FRIENDS, CommonUtils.serializeObject(profile.getFriendsList()));

        if (profile.getShowUploadDialog() != 0)
            vals.put(ProfileSQLiteHelper.COLUMN_SHOW_UPLOAD_DIALOG, profile.getShowUploadDialog());
        if (profile.getShowInFriendsEnabled() != 0)
            vals.put(ProfileSQLiteHelper.COLUMN_ENABLE_SHOW_IN_FRIENDS, profile.getShowInFriendsEnabled());

        String where = ProfileSQLiteHelper.COLUMN_CROCO_ID + " = ?";
        String[] whereArgs = {profile.getCrocoId()};

        Log.e(TAG, "Updating profile: " + profile);
        if (db.update(table, vals, where, whereArgs) > 0) {
            Log.e(TAG, "Profile updated!");
            appData.syncProfileToNearby(getProfileByCrocoId(profile.getCrocoId()));
        } else {
            Log.e(TAG, "Profile not updated");
        }


    }

    public List<Profile> getProfilesByType(int type) {
        Cursor cur = db.query(table, null, ProfileSQLiteHelper.COLUMN_TYPE + " = ?", new String[]{String.valueOf(type)}, null, null, null);
        try {
            return getProfiles(cur);
        } finally {
            if (cur != null && !cur.isClosed()) cur.close();
        }
    }

    public List<Profile> getUnreadProfiles() {
        Cursor cur = db.query(table, null, ProfileSQLiteHelper.COLUMN_UNREAD_COUNT + " > ?", new String[]{String.valueOf(0)}, null, null, null);
        try {
            return getProfiles(cur);
        } finally {
            if (cur != null && !cur.isClosed()) cur.close();
        }
    }

    public boolean isProfileInDB(String crocoId) {
        return getProfileByCrocoId(crocoId) != null;
    }
}
