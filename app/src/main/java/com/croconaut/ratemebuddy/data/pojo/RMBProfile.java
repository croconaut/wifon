package com.croconaut.ratemebuddy.data.pojo;

import android.net.Uri;

import com.croconaut.ratemebuddy.utils.pojo.profiles.IProfile;

import java.io.Serializable;
import java.util.List;

public class RMBProfile implements Serializable {
    private static final long serialVersionUID = 3L;

    private final String name;
    private final String thumbnail;
    private final String profileId;
    private final List<IProfile> friendProfiles;
    private final boolean showInFriendsEnabled;

    public RMBProfile(final String name, final String thumbnail,
                      final String profileId, final List<IProfile> friendProfiles,
                      final boolean showInFriendsEnabled) {
        this.friendProfiles = friendProfiles;
        this.name = name;
        this.profileId = profileId;
        this.showInFriendsEnabled = showInFriendsEnabled;
        this.thumbnail = thumbnail;
    }

    public List<IProfile> getFriendProfiles() {
        return friendProfiles;
    }

    public String getName() {
        return name;
    }

    public String getProfileId() {
        return profileId;
    }

    public boolean isShowInFriendsEnabled() {
        return showInFriendsEnabled;
    }

    public Uri getThumbnail() {
        return thumbnail == null ? null : Uri.parse(thumbnail);
    }

    @Override
    public String toString() {
        return "RMBProfile{" +
                "name='" + name + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", profileId='" + profileId + '\'' +
                ", friendProfiles=" + friendProfiles +
                '}';
    }
}
