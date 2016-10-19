package com.croconaut.ratemebuddy.utils.pojo;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.Spannable;

import java.io.Serializable;

public class UIMessage implements Serializable, Comparable<UIMessage> {

    private static final long serialVersionUID = 6L;

    //numbers according to their priority
    public static final int WAITING = 0;
    public static final int INCOMING = 1;
    public static final int DELETED = 2;

    public static final int SENT_TO_OTHER_DEVICE = 3;
    public static final int SENT_TO_INTERNET = 4;
    public static final int SENT_TO_RECIPIENT = 5;
    public static final int ACKED = 6;
    public static final int ATTACHMENT_DELIVERED = 7;

    private final String crocoId;
    private final String content;

    //common|sent + received)
    private long creationTime;

    //send
    private long sentToInternetTime;
    private long sentToOtherDeviceTime;
    private long sentToRecipientTime;

    private long firstSentTime;
    private long lastSentTime;
    private long seenTime;

    //received
    private long receivedTime;

    private final int sendType;

    private String hops;
    private long uiAttachmentId;
    private int id;
    private UIMessageAttachment uiMessageAttachment;

    private transient Spannable smileText;

    private UIMessage(Builder builder) {
        this.content = builder.content;
        this.crocoId = builder.crocoId;
        this.sendType = builder.sendType;
        this.hops = builder.hops;
        this.id = builder.id;
        this.uiMessageAttachment = builder.uiMessageAttachment;
        this.uiAttachmentId = builder.uiAttachmentId;

        this.firstSentTime = builder.firstSentTime;
        this.lastSentTime = builder.lastSentTime;
        this.seenTime = builder.seenTime;
        this.sentToInternetTime = builder.sentToInternetTime;
        this.sentToOtherDeviceTime = builder.sentToOtherDeviceTime;
        this.sentToRecipientTime = builder.sentToRecipientTime;
        this.creationTime = builder.creationTime;
        this.receivedTime = builder.receivedTime;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UIMessage)) return false;

        UIMessage uiMessage = (UIMessage) o;
        return getId() == uiMessage.getId();
    }

    @Override
    public int hashCode() {
        return getId();
    }

    @Override
    public String toString() {
        return "NewUIMessage{" +
                "crocoId='" + crocoId + '\'' +
                ", content='" + content + '\'' +
                ", sendType=" + sendType +
                ", hops='" + hops + '\'' +
                ", id=" + id +
                '}';
    }

    @Override
    public int compareTo(@NonNull UIMessage uiMessage) {
        long localTime1 = this.getCreationTime();
        long localTime2 = uiMessage.getCreationTime();
        if (localTime1 == localTime2)
            return 0;
        else if (localTime1 > localTime2)
            return 1;
        else return -1;
    }

    public long getSentToOtherDeviceTime() {
        return sentToOtherDeviceTime;
    }

    public long getSentToRecipientTime() {
        return sentToRecipientTime;
    }

    public long getSentToInternetTime() {
        return sentToInternetTime;
    }


    public String getContent() {
        return content;
    }

    public String getCrocoId() {
        return crocoId;
    }

    public int getId() {
        return id;
    }

    public boolean hasAttachment() {
        return uiMessageAttachment != null;
    }

    public Uri getFileUri() {
        return uiMessageAttachment == null ? null : uiMessageAttachment.getUri();
    }

    public long getUiAttachmentId() {
        return this.uiAttachmentId;
    }

    public UIMessageAttachment getUiMessageAttachment() {
        return uiMessageAttachment;
    }

    public String getHops() {
        return hops;
    }

    public int getSendType() {
        return sendType;
    }

    public void setSmileText(Spannable smileText) {
        this.smileText = smileText;
    }

    public Spannable getSmileText() {
        return smileText;
    }

    public long getFirstSentTime() {
        return firstSentTime;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public long getLastSentTime() {
        return lastSentTime;
    }

    public long getSeenTime() {
        return seenTime;
    }

    public long getReceivedTime() {
        return receivedTime;
    }

    public static class Builder {
        private final String crocoId;
        private final String content;
        private final int sendType;

        private String hops;
        private long uiAttachmentId;
        private int id;
        private UIMessageAttachment uiMessageAttachment;

        //common|sent + received)
        private long creationTime;

        //send
        private long sentToInternetTime;
        private long sentToOtherDeviceTime;
        private long sentToRecipientTime;
        private long firstSentTime;
        private long lastSentTime;
        private long seenTime;

        //received
        private long receivedTime;


        public Builder(String crocoId, String content, long creationTime, int sendType) {
            this.crocoId = crocoId;
            this.content = content;
            this.sendType = sendType;
            this.creationTime = creationTime;
        }

        public Builder sentToRecipientTime(long sentToRecipientTime) {
            this.sentToRecipientTime = sentToRecipientTime;
            return this;
        }

        public Builder sentToOtherDeviceTime(long sentToOtherDeviceTime) {
            this.sentToOtherDeviceTime = sentToOtherDeviceTime;
            return this;
        }

        public Builder sentToInternetTime(long sentToInternetTime) {
            this.sentToInternetTime = sentToInternetTime;
            return this;
        }

        public Builder firstSentTime(long firstSentTime) {
            this.firstSentTime = firstSentTime;
            return this;
        }

        public Builder lastSentTime(long lastSentTime) {
            this.lastSentTime = lastSentTime;
            return this;
        }

        public Builder seenTime(long seenTime) {
            this.seenTime = seenTime;
            return this;
        }

        public Builder receivedTime(long receivedTime) {
            this.receivedTime = receivedTime;
            return this;
        }


        public Builder uiMessageAttachment(UIMessageAttachment uiMessageAttachment) {
            this.uiMessageAttachment = uiMessageAttachment;
            return this;
        }

        public Builder uiAttachmentId(long uiAttachmentId) {
            this.uiAttachmentId = uiAttachmentId;
            return this;
        }

        public Builder hops(String hops) {
            this.hops = hops;
            return this;
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder(UIMessage uiMessage) {
            this.content = uiMessage.content;
            this.crocoId = uiMessage.crocoId;
            this.sendType = uiMessage.sendType;
            this.hops = uiMessage.hops;
            this.id = uiMessage.id;
            this.uiMessageAttachment = uiMessage.uiMessageAttachment;
            this.uiAttachmentId = uiMessage.uiAttachmentId;

            this.firstSentTime = uiMessage.firstSentTime;
            this.lastSentTime = uiMessage.lastSentTime;
            this.seenTime = uiMessage.seenTime;

            this.sentToInternetTime = uiMessage.sentToInternetTime;
            this.sentToOtherDeviceTime = uiMessage.sentToOtherDeviceTime;
            this.sentToRecipientTime = uiMessage.sentToRecipientTime;

            this.creationTime = uiMessage.creationTime;
            this.receivedTime = uiMessage.receivedTime;
        }

        public UIMessage build() {
            return new UIMessage(this);
        }
    }
}
