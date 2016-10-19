package com.croconaut.ratemebuddy.data;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.croconaut.ratemebuddy.AppData;
import com.croconaut.ratemebuddy.utils.pojo.TimelineInfo;
import com.croconaut.ratemebuddy.utils.pojo.profiles.Profile;

import java.util.ArrayList;
import java.util.List;

public class TimelineDataSource {

    private TimelineSQLiteHelper helper;
    private SQLiteDatabase db;
    private String table;

    public TimelineDataSource(Context context) {
        helper = new TimelineSQLiteHelper(context);
        table = TimelineSQLiteHelper.TIMELINE_TABLE;
    }

    public void open() {
        db = helper.getWritableDatabase();
    }

    public void close() {
        helper.close();
    }

    private ArrayList<TimelineInfo> getTimelineInfos(Cursor cursor) {
        ArrayList<TimelineInfo> timelineInfos = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            int timeCol = cursor.getColumnIndex(TimelineSQLiteHelper.COLUMN_TIME);
            int contentCol = cursor.getColumnIndex(TimelineSQLiteHelper.COLUMN_CONTENT);
            int crocoCol = cursor.getColumnIndex(TimelineSQLiteHelper.COLUMN_CROCO_ID);
            int messCol = cursor.getColumnIndex(TimelineSQLiteHelper.COLUMN_MESS_TYPE);
            int sendCol = cursor.getColumnIndex(TimelineSQLiteHelper.COLUMN_SEND_TYPE);
            int nameCol = cursor.getColumnIndex(TimelineSQLiteHelper.COLUMN_NAME);
            int fileCol = cursor.getColumnIndex(TimelineSQLiteHelper.COLUMN_FILE_URI);
            int statusIdCol = cursor.getColumnIndex(TimelineSQLiteHelper.COLUMN_STATUS_ID);
            int seenCol = cursor.getColumnIndex(TimelineSQLiteHelper.COLUMN_SEEN);

            do {
                long time = cursor.getLong(timeCol);
                String crocoId = cursor.getString(crocoCol);
                String name = cursor.getString(nameCol);
                String content = cursor.getString(contentCol);
                String file = cursor.getString(fileCol);
                String statusID = cursor.getString(statusIdCol);
                int messType = cursor.getInt(messCol);
                int sendType = cursor.getInt(sendCol);
                boolean seen = cursor.getInt(seenCol) == 0;

                TimelineInfo timelineInfo = new TimelineInfo.Builder(time, content, crocoId, name, sendType, messType, seen)
                        .fileUri(file).statusId(statusID).build();
                timelineInfos.add(timelineInfo);
            } while (cursor.moveToNext());
        }
        return timelineInfos;
    }

    public long insertTimelineInfo(TimelineInfo timelineInfo) {
        ContentValues vals = new ContentValues();
        vals.put(TimelineSQLiteHelper.COLUMN_TIME, timelineInfo.getTime());
        vals.put(TimelineSQLiteHelper.COLUMN_CROCO_ID, timelineInfo.getCrocoId());
        vals.put(TimelineSQLiteHelper.COLUMN_CONTENT, timelineInfo.getContent());
        vals.put(TimelineSQLiteHelper.COLUMN_NAME, timelineInfo.getName());
        vals.put(TimelineSQLiteHelper.COLUMN_MESS_TYPE, timelineInfo.getMessageType());
        vals.put(TimelineSQLiteHelper.COLUMN_SEND_TYPE, timelineInfo.getSendType());
        vals.put(TimelineSQLiteHelper.COLUMN_SEEN, timelineInfo.isSeen() ? 0 : 1);
        if (timelineInfo.getFileUri() != null)
            vals.put(TimelineSQLiteHelper.COLUMN_FILE_URI, timelineInfo.getFileUri().toString());
        if (timelineInfo.getStatusId() != null)
            vals.put(TimelineSQLiteHelper.COLUMN_STATUS_ID, timelineInfo.getStatusId());
        return db.insert(table, null, vals);
    }


    public int clearSeen() {
        ContentValues vals = new ContentValues();
        vals.put(TimelineSQLiteHelper.COLUMN_SEEN, 1);
        return db.update(table, vals, TimelineSQLiteHelper.COLUMN_SEEN + " = ?", new String[]{String.valueOf(0)});
    }

    public ArrayList<TimelineInfo> getAllTimeLineInfos(AppData appData) {
        Cursor cur = db.query(table, null, null, null, null, null, null);
        try {
            List<Profile> blockedProfiles = appData.getProfileDataSource().getProfilesByType(Profile.BLOCKED);
            ArrayList<TimelineInfo> infos = getTimelineInfos(cur);
            ArrayList<TimelineInfo> remove = new ArrayList<>();
            for (TimelineInfo info : infos) {
                Profile profile = appData.getProfileDataSource().getProfileByCrocoId(info.getCrocoId());
                if (profile != null && blockedProfiles.contains(profile)) {
                    remove.add(info);
                }
            }
            infos.removeAll(remove);

            return infos;
        } finally {
            if (cur != null && !cur.isClosed()) cur.close();
        }
    }

    public TimelineInfo getTimelineInfoByFileUri(String fileUri) {
        if (fileUri == null) return null;

        StringBuilder w = new StringBuilder();
        w.append(TimelineSQLiteHelper.COLUMN_FILE_URI + " = ? ");
        String selection = new String(w);
        String[] selectionArgs = new String[]{String.valueOf(fileUri)};
        Cursor cur = db.query(table, null, selection, selectionArgs, null,
                null, null);
        try {
            List<TimelineInfo> timelineInfo = getTimelineInfos(cur);
            return (timelineInfo.isEmpty()) ? null : timelineInfo.get(0);
        } finally {
            if (cur != null && !cur.isClosed()) cur.close();
        }
    }
}
