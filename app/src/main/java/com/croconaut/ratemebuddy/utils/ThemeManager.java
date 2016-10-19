package com.croconaut.ratemebuddy.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.activities.SettingsActivity;

public class ThemeManager {
    public static final int[] colors = {
            R.color.red_themed,
            R.color.green_themed,
            R.color.blue_themed,
            R.color.orange_themed,
            R.color.pink_themed
    };

    public static final int[] incomingMessageSelector = {
            R.drawable.incoming_message_red_selector,
            R.drawable.incoming_message_green_selector,
            R.drawable.incoming_message_blue_selector,
            R.drawable.incoming_message_orange_selector,
            R.drawable.incoming_message_pink_selector
    };

    public static final int[] incomingCommentSelector = {
            R.drawable.incoming_comment_red_selector,
            R.drawable.incoming_comment_green_selector,
            R.drawable.incoming_comment_blue_selector,
            R.drawable.incoming_comment_orange_selector,
            R.drawable.incoming_comment_pink_selector
    };

    private static final String TAG = ThemeManager.class.getName();
    private static final String DEFAULT_VALUE = "1";

    private int scheme;
    private SharedPreferences prefs;
    private Context context;

    public ThemeManager(Context context) {
        this.context = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        update();
    }

    public void update() {
        try {
            scheme = Integer.parseInt(prefs.getString(SettingsActivity.COLOR_PREF, DEFAULT_VALUE));
        } catch (NumberFormatException e) {
            Log.e(TAG, "NumberFormatException: ", e);
            scheme = Integer.valueOf(DEFAULT_VALUE);
        }
    }


    public int getIncominCommentSelector() {
        update();
        return incomingCommentSelector[scheme];
    }

    public int getIncomingMessageSelector() {
        update();
        return incomingMessageSelector[scheme];
    }

    public int getIncomingMessageSelector(int index) {
        update();
        return incomingMessageSelector[index];
    }

    public int getCurrentColorHexa() {
        update();
        return ContextCompat.getColor(context, colors[scheme]);
    }
}
