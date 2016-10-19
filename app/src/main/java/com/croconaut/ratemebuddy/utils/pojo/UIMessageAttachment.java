package com.croconaut.ratemebuddy.utils.pojo;


import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;

import com.croconaut.ratemebuddy.R;

import java.io.Serializable;
import java.util.Date;

public class UIMessageAttachment implements Serializable {

    private static final long serialVersionUID = 7L;

    public static final int PRIVATE = 0;
    public static final int PUBLIC_IMAGE = 1;
    public static final int PUBLIC_OTHER = 2;
    public static final int UNKNOWN = 3;

    public static final int STATE_WAITING_FOR_CONFIRMATION = 0;
    public static final int STATE_WAITING_FOR_DOWNLOAD = 1;
    public static final int STATE_DOWNLOADING = 2;
    public static final int STATE_DOWNLOAD_FINISHED = 3;
    public static final int STATE_DOWNLOAD_CANCELLED = 4;

    public static final int STATE_UPLOAD_WAITING = 5;
    public static final int STATE_UPLOADING = 6;
    public static final int STATE_UPLOAD_CANCELLED = 7;
    public static final int STATE_UPLOADED_TO_SERVER = 8;
    public static final int STATE_UPLOADED_TO_RECIPIENT = 9;

    private String name;
    private String type;
    private String uri;
    private String sourceUri;

    private Date lastModified;
    private long length;
    private long time;
    private int storageType;
    private int state;
    private int speed;

    private long downloadStarted;
    private long downloadEnded;

    public UIMessageAttachment(Builder builder) {
        this.name = builder.name;
        this.type = builder.type;
        this.uri = builder.uri;
        this.lastModified = builder.lastModified;
        this.length = builder.length;
        this.storageType = builder.storageType;
        this.state = builder.state;
        this.sourceUri = builder.sourceUri;
        this.time = builder.time;
        this.speed = builder.speed;
        this.downloadStarted = builder.downloadStarted;
        this.downloadEnded = builder.downloadEnded;
    }

    public boolean hasFinishedDownload() {
        return this.state == STATE_DOWNLOAD_FINISHED;
    }

    public boolean hasFinishedUpload() {
        return this.state == STATE_UPLOADED_TO_SERVER || this.state == STATE_UPLOADED_TO_RECIPIENT;
    }

    public String getName() {
        return name;
    }

    public String getState(Context context) {
        Resources resources = context.getResources();
        switch (state) {
            case STATE_DOWNLOADING:
                return resources.getString(R.string.message_download_state_downloading);
            case STATE_DOWNLOAD_FINISHED:
                return resources.getString(R.string.message_download_state_finished);
            case STATE_WAITING_FOR_CONFIRMATION:
                return resources.getString(R.string.message_download_state_waiting_for_confirm);
            case STATE_WAITING_FOR_DOWNLOAD:
                return resources.getString(R.string.message_download_state_waiting_for_download);
            case STATE_DOWNLOAD_CANCELLED:
                return resources.getString(R.string.message_download_state_cancelled);

            case STATE_UPLOADED_TO_RECIPIENT:
                return resources.getString(R.string.message_upload_state_uploaded_to_recipient);
            case STATE_UPLOADED_TO_SERVER:
                return resources.getString(R.string.message_upload_state_uploaded_to_server);
            case STATE_UPLOAD_WAITING:
                return resources.getString(R.string.message_upload_state_waiting_for_upload);
            case STATE_UPLOADING:
                return resources.getString(R.string.message_upload_state_uploading);
            case STATE_UPLOAD_CANCELLED:
                return resources.getString(R.string.message_upload_state_cancelled);
        }
        return null;
    }

    public int getState() {
        return state;
    }

    public int getStorageType() {
        return storageType;
    }

    public long getLength() {
        return length;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public String getType() {
        return type;
    }

    public String getSourceUri() {
        return sourceUri;
    }

    public Uri getUri() {
        return uri == null ? null : Uri.parse(uri);
    }

    public long getTime() {
        return time;
    }

    public int getSpeed() {
        return speed;
    }

    public long getDownloadEnded() {
        return downloadEnded;
    }

    public long getDownloadStarted() {
        return downloadStarted;
    }

    public static class Builder {
        private String name;
        private String type;
        private String uri;
        private String sourceUri;

        private Date lastModified;
        private long length;
        private long time;
        private int storageType;
        private int state;
        private int speed;

        private long downloadStarted;
        private long downloadEnded;

        public Builder(UIMessageAttachment uiMessageAttachment) {
            this.name = uiMessageAttachment.name;
            this.type = uiMessageAttachment.type;
            this.uri = uiMessageAttachment.uri;
            this.lastModified = uiMessageAttachment.lastModified;
            this.length = uiMessageAttachment.length;
            this.storageType = uiMessageAttachment.storageType;
            this.state = uiMessageAttachment.state;
            this.sourceUri = uiMessageAttachment.sourceUri;
            this.time = uiMessageAttachment.time;
            this.speed = uiMessageAttachment.speed;
            this.downloadEnded = uiMessageAttachment.downloadEnded;
            this.downloadStarted = uiMessageAttachment.downloadStarted;
        }

        public Builder(String name) {
            this.name = name;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder uri(String uri) {
            this.uri = uri;
            return this;
        }

        public Builder sourceUri(String sourceUri) {
            this.sourceUri = sourceUri;
            return this;
        }

        public Builder lastModified(Date lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        public Builder length(long length) {
            this.length = length;
            return this;
        }

        public Builder downloadEnded(long downloadEnded) {
            this.downloadEnded = downloadEnded;
            return this;
        }

        public Builder downloadStarted(long downloadStarted) {
            this.downloadStarted = downloadStarted;
            return this;
        }

        public Builder time(long time) {
            this.time = time;
            return this;
        }

        public Builder storageType(int storageType) {
            this.storageType = storageType;
            return this;
        }

        public Builder state(int state) {
            this.state = state;
            return this;
        }

        public Builder speed(int speed) {
            this.speed = speed;
            return this;
        }

        public UIMessageAttachment build() {
            return new UIMessageAttachment(this);
        }
    }
}
