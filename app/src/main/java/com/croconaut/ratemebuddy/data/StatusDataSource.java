package com.croconaut.ratemebuddy.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.croconaut.ratemebuddy.data.pojo.Comment;
import com.croconaut.ratemebuddy.data.pojo.Status;
import com.croconaut.ratemebuddy.data.pojo.VoteUp;
import com.croconaut.ratemebuddy.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

public class StatusDataSource {

    private StatusSQLiteHelper helper;
    private SQLiteDatabase db;
    private String table;

    public StatusDataSource(Context context) {
        helper = new StatusSQLiteHelper(context);
        table = StatusSQLiteHelper.STATUS_TABLE;
    }

    public void open() {
        db = helper.getWritableDatabase();
    }

    public void close() {
        helper.close();
    }

    private List<Status> getStatuses(Cursor cursor) {
        ArrayList<Status> statuses = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            int statusIdCol = cursor.getColumnIndex(StatusSQLiteHelper.COLUMN_STATUS_ID);
            int timeStampCol = cursor.getColumnIndex(StatusSQLiteHelper.COLUMN_TIME_STAMP);
            int statusContentCol = cursor.getColumnIndex(StatusSQLiteHelper.COLUMN_STATUS_CONTENT);
            int votesCol = cursor.getColumnIndex(StatusSQLiteHelper.COLUMN_VOTES);
            int commentsCol = cursor.getColumnIndex(StatusSQLiteHelper.COLUMN_COMMENTS);
            int crocoCol = cursor.getColumnIndex(StatusSQLiteHelper.COLUMN_CROCO_ID);
            int appCol = cursor.getColumnIndex(StatusSQLiteHelper.COLUMN_PROFILE_ID);
            int profileNameCol = cursor.getColumnIndex(StatusSQLiteHelper.COLUMN_PROFILE_NAME);

            do {
                long timeStamp = cursor.getLong(timeStampCol);
                String statusId = cursor.getString(statusIdCol);
                String content = cursor.getString(statusContentCol);
                String crocoId = cursor.getString(crocoCol);
                String profileName = cursor.getString(profileNameCol);
                String profileId = cursor.getString(appCol);

                ArrayList<Comment> comments = (ArrayList<Comment>) CommonUtils.deserializeObject(cursor.getBlob(commentsCol));
                ArrayList<VoteUp> votes = (ArrayList<VoteUp>) CommonUtils.deserializeObject(cursor.getBlob(votesCol));

                Status status = new Status.Builder(timeStamp, statusId, profileId, content, profileName).crocoId(crocoId)
                        .comments(comments)
                        .votes(votes).build();

                statuses.add(status);
            } while (cursor.moveToNext());
        }
        return statuses;
    }

    public Status getStatusByID(String statusId) {
        Cursor cur = db.query(table, null, StatusSQLiteHelper.COLUMN_STATUS_ID + " = ?", new String[]{statusId}, null, null, null);
        try {
            List<Status> statuses = getStatuses(cur);
            return !statuses.isEmpty() ? statuses.get(0) : null;
        } finally {
            if (cur != null && !cur.isClosed()) cur.close();
        }
    }

    public List<Status> getAllMyStatuses() {
        Cursor cur = db.query(table, null, null, null, null, null, null);
        try {
            return getStatuses(cur);
        } finally {
            if (cur != null && !cur.isClosed()) cur.close();
        }
    }

    public long insertStatus(Status status) {
        ContentValues vals = new ContentValues();
        vals.put(StatusSQLiteHelper.COLUMN_TIME_STAMP, status.getTimeStamp());
        vals.put(StatusSQLiteHelper.COLUMN_STATUS_ID, status.getStatusID());
        vals.put(StatusSQLiteHelper.COLUMN_PROFILE_ID, status.getProfileId());
        vals.put(StatusSQLiteHelper.COLUMN_PROFILE_NAME, status.getProfileName());
        vals.put(StatusSQLiteHelper.COLUMN_CROCO_ID, status.getCrocoId());
        vals.put(StatusSQLiteHelper.COLUMN_STATUS_CONTENT, status.getContent());
        vals.put(StatusSQLiteHelper.COLUMN_COMMENTS, CommonUtils.serializeObject(status.getComments()));
        vals.put(StatusSQLiteHelper.COLUMN_VOTES, CommonUtils.serializeObject(status.getVotes()));
        return db.insert(table, null, vals);
    }

    public boolean updateStatus(Status status) {
        ContentValues vals = new ContentValues();
        vals.put(StatusSQLiteHelper.COLUMN_TIME_STAMP, status.getTimeStamp());
        if (status.getStatusID() != null)
            vals.put(StatusSQLiteHelper.COLUMN_STATUS_ID, status.getStatusID());
        if (status.getProfileId() != null)
            vals.put(StatusSQLiteHelper.COLUMN_PROFILE_ID, status.getProfileId());
        if (status.getCrocoId() != null)
            vals.put(StatusSQLiteHelper.COLUMN_CROCO_ID, status.getCrocoId());
        if (status.getProfileName() != null)
            vals.put(StatusSQLiteHelper.COLUMN_PROFILE_NAME, status.getProfileName());

        vals.put(StatusSQLiteHelper.COLUMN_STATUS_CONTENT, status.getContent());

        if (status.getVotes() != null)
            vals.put(StatusSQLiteHelper.COLUMN_VOTES, CommonUtils.serializeObject(status.getVotes()));
        if (status.getComments() != null)
            vals.put(StatusSQLiteHelper.COLUMN_COMMENTS, CommonUtils.serializeObject(status.getComments()));

        String where = StatusSQLiteHelper.COLUMN_STATUS_ID + " == ?";
        String[] whereArgs = {status.getStatusID()};
        return db.update(table, vals, where, whereArgs) > 0;
    }
}
