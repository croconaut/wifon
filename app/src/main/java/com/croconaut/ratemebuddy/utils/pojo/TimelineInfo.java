package com.croconaut.ratemebuddy.utils.pojo;


import android.net.Uri;

public class TimelineInfo implements Comparable<TimelineInfo> {
    public static final int OUTGOING = 0;
    public static final int INCOMING = 1;

    public static final int MESSAGE_TYPE_FILE = 0;
    public static final int MESSAGE_TYPE_VOTE = 1;
    public static final int MESSAGE_TYPE_INTRO = 2;
    public static final int MESSAGE_TYPE_COMMENT = 3;
    public static final int MESSAGE_TYPE_PROFILE_CHANGED_TEXT = 4;
    public static final int MESSAGE_TYPE_PROFILE_CHANGED_PHOTO = 5;

    private final long time;
    private final String content;
    private final String crocoId;
    private final String name;
    private final int sendType;
    private final int messageType;
    private String fileUri;
    private String statusId;
    private boolean seen;

    public TimelineInfo(Builder builder) {
        this.time = builder.time;
        this.content = builder.content;
        this.crocoId = builder.crocoId;
        this.name = builder.name;
        this.sendType = builder.sendType;
        this.messageType = builder.messageType;
        this.fileUri = builder.fileUri;
        this.statusId = builder.statusId;
        this.seen = builder.seen;
    }

    @Override
    public int compareTo(TimelineInfo another) {
        return Long.valueOf(this.time).compareTo(another.getTime());
    }

    @Override
    public String toString() {
        return "TimelineInfo{" +
                "time=" + time +
                ", content='" + content + '\'' +
                ", crocoId='" + crocoId + '\'' +
                ", name='" + name + '\'' +
                ", sendType=" + sendType +
                ", messageType=" + messageType +
                ", fileUri='" + fileUri + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimelineInfo)) return false;

        TimelineInfo that = (TimelineInfo) o;

        return getTime() == that.getTime();

    }

    @Override
    public int hashCode() {
        return (int) (getTime() ^ (getTime() >>> 32));
    }

    public Uri getFileUri() {
        return fileUri == null ? null : Uri.parse(fileUri);
    }

    public int getMessageType() {
        return messageType;
    }

    public int getSendType() {
        return sendType;
    }

    public String getName() {
        return name;
    }

    public String getCrocoId() {
        return crocoId;
    }

    public String getContent() {
        return content;
    }

    public long getTime() {
        return time;
    }

    public String getStatusId() {
        return statusId;
    }

    public boolean isSeen() {
        return seen;
    }

    public static class Builder {
        private final long time;
        private final String content;
        private final String crocoId;
        private final String name;
        private final int sendType;
        private final int messageType;
        private String fileUri;
        private String statusId;
        private boolean seen;

        public Builder(long time, String content, String crocoId, String name, int sendType, int messageType, boolean seen) {
            this.time = time;
            this.content = content;
            this.crocoId = crocoId;
            this.name = name;
            this.sendType = sendType;
            this.messageType = messageType;
            this.seen = seen;
        }

        public Builder fileUri(String fileUri) {
            this.fileUri = fileUri;
            return this;
        }

        public Builder statusId(String statusId) {
            this.statusId = statusId;
            return this;
        }

        public TimelineInfo build() {
            return new TimelineInfo(this);
        }
    }
}
