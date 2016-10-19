package com.croconaut.ratemebuddy.data.pojo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class Status implements Serializable {
    private static final long serialVersionUID = 4L;

    private final long timeStamp;
    private final String statusID;
    private final String profileId;
    private final String profileName;
    private final String content;
    private String crocoId;

    private ArrayList<Comment> comments = new ArrayList<>();
    private ArrayList<VoteUp> votes = new ArrayList<>();

    private Status(Builder builder) {
        this.timeStamp = builder.timeStamp;
        this.statusID = builder.statusID;
        this.profileId = builder.profileId;
        this.crocoId = builder.crocoId;
        this.content = builder.content;
        this.comments = builder.comments;
        this.votes = builder.votes;
        this.profileName = builder.profileName;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Status)) return false;

        Status status = (Status) o;

        if (getTimeStamp() != status.getTimeStamp()) return false;
        if (!getStatusID().equals(status.getStatusID())) return false;
        if (!getProfileId().equals(status.getProfileId())) return false;
        if (getContent() != null ? !getContent().equals(status.getContent()) : status.getContent() != null)
            return false;
        if (getComments() != null ? !getComments().equals(status.getComments()) : status.getComments() != null)
            return false;
        return !(getVotes() != null ? !getVotes().equals(status.getVotes()) : status.getVotes() != null);

    }

    @Override
    public int hashCode() {
        int result = (int) (getTimeStamp() ^ (getTimeStamp() >>> 32));
        result = 31 * result + getStatusID().hashCode();
        result = 31 * result + getProfileId().hashCode();
        result = 31 * result + (getContent() != null ? getContent().hashCode() : 0);
        result = 31 * result + (getComments() != null ? getComments().hashCode() : 0);
        result = 31 * result + (getVotes() != null ? getVotes().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Status{" +
                "timeStamp=" + timeStamp +
                ", statusID='" + statusID + '\'' +
                ", profileId='" + profileId + '\'' +
                ", content='" + content + '\'' +
                ", crocoId='" + crocoId + '\'' +
                ", comments=" + comments +
                ", votes=" + votes +
                '}';
    }

    public String getProfileName() {
        return profileName;
    }

    public boolean isProfileIdPresent(String profileId) {
        for (VoteUp voteUp : votes) {
            if (voteUp.getProfileId().equals(profileId)) return true;
        }
        return false;
    }

    public ArrayList<VoteUp> getUnseenVotes() {
        ArrayList<VoteUp> unseenVotes = new ArrayList<>();
        for (VoteUp voteUp : votes) {
            if (!voteUp.isSeen()) unseenVotes.add(voteUp);
        }
        return unseenVotes;
    }

    public ArrayList<Comment> getUnseenComments() {
        ArrayList<Comment> unseenComments = new ArrayList<>();
        for (Comment comment : comments) {
            if (!comment.isSeen()) unseenComments.add(comment);
        }
        return unseenComments;
    }

    public void clearUnseenVotes() {
        for (VoteUp voteUp : votes) {
            if (!voteUp.isSeen()) voteUp.setSeen(true);
        }
    }

    public void clearUnseenComments() {
        for (Comment comment : comments) {
            if (!comment.isSeen()) comment.setSeen(true);
        }
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getStatusID() {
        return statusID;
    }

    public String getProfileId() {
        return profileId;
    }

    public String getContent() {
        return content;
    }

    public String getCrocoId() {
        return crocoId;
    }

    public ArrayList<Comment> getComments() {
        return comments;
    }

    public ArrayList<VoteUp> getVotes() {
        return votes;
    }

    public static class Builder {
        private final long timeStamp;
        private final String statusID;
        private final String profileId;
        private final String content;
        private final String profileName;
        private String crocoId;

        private ArrayList<Comment> comments = new ArrayList<>();
        private ArrayList<VoteUp> votes = new ArrayList<>();

        public Builder(long timeStamp, String statusID, String profileId, String content, String profileName) {
            this.timeStamp = timeStamp;
            this.statusID = statusID;
            this.profileId = profileId;
            this.content = content;
            this.profileName = profileName;
        }

        public Builder(Status status) {
            this.timeStamp = status.timeStamp;
            this.statusID = status.statusID;
            this.profileId = status.profileId;
            this.crocoId = status.crocoId;
            this.content = status.content;
            this.comments = status.comments;
            this.votes = status.votes;
            this.profileName = status.profileName;
        }

        public Builder comments(ArrayList<Comment> comments) {
            this.comments = comments;
            return this;
        }

        public Builder votes(ArrayList<VoteUp> votes) {
            this.votes = votes;
            return this;
        }

        public Builder crocoId(String crocoId) {
            this.crocoId = crocoId;
            return this;
        }

        public Builder addVote(VoteUp voteUp) {
            this.votes.add(voteUp);
            return this;
        }

        public Status build() {
            Collections.sort(comments);
            return new Status(this);
        }
    }
}
