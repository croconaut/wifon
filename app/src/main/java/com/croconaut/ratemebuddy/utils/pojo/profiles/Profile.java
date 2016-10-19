package com.croconaut.ratemebuddy.utils.pojo.profiles;

import android.net.Uri;
import android.util.Log;

import com.croconaut.ratemebuddy.AppData;
import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.data.pojo.Status;
import com.croconaut.ratemebuddy.utils.pojo.UIMessage;
import com.croconaut.ratemebuddy.utils.pojo.profiles.states.ActualState;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Profile implements Serializable, IProfile {

    private static final long serialVersionUID = 9L;

    public static final int FAVOURITE = 1;
    public static final int BLOCKED = 2;
    public static final int UNKNOWN = 3;
    public static final int CACHED = 4;

    public static final int SHOW_FILE_DIALOG = 1;
    public static final int NOT_SHOW_FILE_DIALOG = 2;

    public static final int DISPLAY_IN_FRIENDS = 1;
    public static final int NOT_DISPLAY_IN_FRIENDS = 2;

    private final String crocoId;

    private String name;
    private Status status;
    private String thumbnailUri;
    private String profileId;
    private ActualState actualState;
    private int type;
    private int unread;
    private long timeStamp;
    private ArrayList<UIMessage> unreadMessages;
    private List<IProfile> friendsList =  Collections.emptyList();

    private int showInFriendsEnabled = DISPLAY_IN_FRIENDS;
    private int showUploadDialog;

    public Profile(Builder builder) {
        this.name = builder.name;
        this.crocoId = builder.crocoId;
        this.status = builder.status;
        this.thumbnailUri = builder.thumbnailUri;
        this.timeStamp = builder.timeStamp;
        this.unreadMessages = builder.unreadMessages;
        this.actualState = builder.actualState;
        this.type = builder.type;
        this.unread = builder.unread;
        this.showUploadDialog = builder.showUploadDialog;
        this.showInFriendsEnabled = builder.showInFriendsEnabled;
        this.friendsList = builder.friendsList;
        this.profileId = builder.profileId;
    }

    public String getProfileId() {
        return profileId;
    }

    @Override
    public String toString() {
        return "Profile{" +
                "name='" + name + '\'' +
                ", crocoId='" + crocoId + '\'' +
                ", profileId='" + profileId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Profile)) return false;

        Profile profile = (Profile) o;

        return getCrocoId().equals(profile.getCrocoId());

    }

    public int getShowInFriendsEnabled() {
        return showInFriendsEnabled;
    }

    public int getShowUploadDialog() {
        return showUploadDialog;
    }

    public List<IProfile> getFriendsList() {
        return friendsList;
    }

    @Override
    public int hashCode() {
        return getCrocoId().hashCode();
    }

    public int getUnread() {
        return unread;
    }

    public void setUnread(int unread) {
        this.unread = unread;
    }

    public void setType(int type) {
        this.type = type;
        this.showUploadDialog = type != FAVOURITE ? SHOW_FILE_DIALOG : NOT_SHOW_FILE_DIALOG;
    }

    public boolean isActual() {
        return actualState == null || actualState.isActual();
    }

    public void setActual() {
        this.actualState.clearAndSetActual();
    }

    @Override
    public String getIdent() {
        return getCrocoId();
    }

    public String getCrocoId() {
        return crocoId;
    }


    @Override
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        Log.e("Profile", "setStatus: " + status.toString() + " for profile " + this.toString());
        this.status = status;
    }

    @Override
    public String getName() {
        return name != null
                ? name
                : AppData.getAppContext().getResources().getString(R.string.profile_unknown_name);
    }

    public boolean isUnknown() {
        return name == null;
    }

    @Override
    public Uri getThumbUri() {
        return thumbnailUri == null ? null : Uri.parse(thumbnailUri);
    }

    public ActualState getActualState() {
        if (actualState == null)
            this.actualState = new ActualState.Builder().build();
        return actualState;
    }

    public int getType() {
        return type;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public ArrayList<UIMessage> getUnreadMessages() {
        return unreadMessages;
    }

    public boolean isShowUploadDialog() {
        return this.showUploadDialog == SHOW_FILE_DIALOG;
    }

    public boolean isShowInFriendsEnabled() {
        return this.showInFriendsEnabled == DISPLAY_IN_FRIENDS;
    }

    public static class Builder {
        private final String crocoId;
        private String name;
        private Status status;
        private String thumbnailUri;
        private String profileId;
        private ActualState actualState;
        private int type;
        private int unread;
        private long timeStamp;
        private int showInFriendsEnabled = DISPLAY_IN_FRIENDS;
        private int showUploadDialog;
        private List<IProfile> friendsList;
        private ArrayList<UIMessage> unreadMessages = new ArrayList<>();

        public Builder(String crocoId) {
            this.crocoId = crocoId;
        }

        public Builder(Profile profile) {
            this.name = profile.name;
            this.crocoId = profile.crocoId;
            this.status = profile.status;
            this.thumbnailUri = profile.thumbnailUri;
            this.timeStamp = profile.timeStamp;
            this.unreadMessages = profile.unreadMessages;
            this.actualState = profile.actualState;
            this.type = profile.type;
            this.unread = profile.unread;
            this.friendsList = profile.friendsList;
            this.showUploadDialog = profile.showUploadDialog;
            this.showInFriendsEnabled = profile.showInFriendsEnabled;
            this.profileId = profile.profileId;
        }

        public Builder enableInFriends(int showInFriendsEnabled) {
            this.showInFriendsEnabled = showInFriendsEnabled;
            return this;
        }

        public Builder addProfileId(String profileId) {
            this.profileId = profileId;
            return this;
        }

        public Builder setFriendsList(List<IProfile> friendsList) {
            this.friendsList = friendsList;
            return this;
        }

        public Builder showUploadDialog(int showUploadDialog) {
            this.showUploadDialog = showUploadDialog;
            return this;
        }

        public Builder addName(String name) {
            this.name = name;
            return this;
        }

        public Builder addStatus(Status status) {
            this.status = status;
            return this;
        }

        public Builder addType(int type) {
            this.type = type;
            this.showUploadDialog = type != FAVOURITE ? SHOW_FILE_DIALOG : NOT_SHOW_FILE_DIALOG;
            return this;
        }

        public Builder addTimestamp(long timeStamp) {
            this.timeStamp = timeStamp;
            return this;
        }

        public Builder addActualState(ActualState actualState) {
            this.actualState = actualState;
            return this;
        }

        public Builder addUnreadMessages(ArrayList<UIMessage> unreadMessages) {
            this.unreadMessages = unreadMessages;
            return this;
        }

        public Builder addUnread(int unread) {
            this.unread = unread;
            return this;
        }

        public Builder addUri(Uri uri) {
            if (uri != null)
                this.thumbnailUri = uri.toString();
            return this;
        }

        public Profile build() {
            return new Profile(this);
        }
    }
}
