package com.croconaut.ratemebuddy.data.pojo;

import java.io.Serializable;


public class Message implements Serializable {
    private static final long serialVersionUID = 55L;

    private final String content;
    private final String profileId;
    private final String profileName;

    public Message(final String content, final String profileId, String profileName){
        this.profileId = profileId;
        this.profileName = profileName;
        this.content = content;
    }

    public String getProfileId() {
        return profileId;
    }

    public String getProfileName() {
        return profileName;
    }

    public String getContent() {
        return content;
    }
}
