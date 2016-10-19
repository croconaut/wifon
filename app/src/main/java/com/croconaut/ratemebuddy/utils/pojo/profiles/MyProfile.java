package com.croconaut.ratemebuddy.utils.pojo.profiles;

import android.content.Context;
import android.net.Uri;

import com.croconaut.ratemebuddy.data.pojo.Status;
import com.croconaut.ratemebuddy.utils.ProfileUtils;

import java.io.Serializable;

public class MyProfile implements Serializable, IProfile {

    private static final long serialVersionUID = 8L;

    private static MyProfile mInstance = null;

    private final String profileId;
    private String name;
    private String thumbnailUri;
    private Status status;

    private long timeStamp;

    public static MyProfile getInstance(Context context) {
        if (mInstance == null) {
            synchronized (MyProfile.class) {
                if (mInstance == null) {
                    mInstance = ProfileUtils.readMyProfile(context);
                }
            }
        }
        return mInstance;
    }

    private MyProfile(Builder builder) {
        this.name = builder.name;
        this.thumbnailUri = builder.thumbnailUri;
        this.status = builder.status;
        this.profileId = builder.profileId;
        this.timeStamp = builder.timeStamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MyProfile)) return false;

        MyProfile myProfile = (MyProfile) o;

        if (!getProfileId().equals(myProfile.getProfileId())) return false;
        if (!getName().equals(myProfile.getName())) return false;
        if (thumbnailUri != null ? !thumbnailUri.equals(myProfile.thumbnailUri) : myProfile.thumbnailUri != null)
            return false;
        return !(getStatus() != null ? !getStatus().equals(myProfile.getStatus()) : myProfile.getStatus() != null);

    }

    @Override
    public int hashCode() {
        int result = getProfileId().hashCode();
        result = 31 * result + getName().hashCode();
        result = 31 * result + (thumbnailUri != null ? thumbnailUri.hashCode() : 0);
        result = 31 * result + (getStatus() != null ? getStatus().hashCode() : 0);
        return result;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getProfileId() {
        return profileId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Uri getThumbUri() {
        return thumbnailUri != null ? Uri.parse(thumbnailUri) : null;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public String getIdent() {
        return getProfileId();
    }

    public static class Builder {
        private final String profileId;
        private String name;
        private String thumbnailUri;
        private Status status;

        private long timeStamp;

        public Builder(MyProfile myProfile) {
            this.name = myProfile.name;
            this.thumbnailUri = myProfile.thumbnailUri;
            this.status = myProfile.status;
            this.profileId = myProfile.profileId;
            this.timeStamp = myProfile.timeStamp;
        }

        public Builder(String profileId) {
            this.profileId = profileId;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder thumbnail(Uri uri) {
            if (uri != null)
                this.thumbnailUri = uri.toString();
            return this;
        }

        public Builder status(Status status) {
            this.status = status;
            return this;
        }

        public MyProfile build(Context context) {
            timeStamp = System.currentTimeMillis();
            mInstance = new MyProfile(this);
            ProfileUtils.writeMyProfileToFile(context, mInstance);
            return mInstance;
        }
    }
}
