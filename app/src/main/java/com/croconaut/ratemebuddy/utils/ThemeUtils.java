package com.croconaut.ratemebuddy.utils;

import android.content.SharedPreferences;

import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.activities.SettingsActivity;

public class ThemeUtils {

    public static int getEmptyBgResId(SharedPreferences preferences) {
        int themeId = Integer.parseInt(preferences.getString(
                SettingsActivity.COLOR_PREF, "1"));

        switch (themeId) {
            case 0:
                return R.drawable.timeline_bgr_red;
            case 1:
                return R.drawable.timeline_bgr_green;
            case 2:
                return R.drawable.timeline_bgr_blue;
            case 3:
                return R.drawable.timeline_bgr_orange;
            case 4:
                return R.drawable.timeline_bgr_pink;
            default:
                return R.drawable.timeline_bgr_green;
        }
    }

    public static int getBgCoverResId(SharedPreferences preferences) {
        int themeId = Integer.parseInt(preferences.getString(
                SettingsActivity.COLOR_PREF, "1"));

        switch (themeId) {
            case 0:
                return R.drawable.theme_bgr_red;
            case 1:
                return R.drawable.theme_bgr_green;
            case 2:
                return R.drawable.theme_bgr_blue;
            case 3:
                return R.drawable.theme_bgr_orange;
            case 4:
                return R.drawable.theme_bgr_pink;
            default:
                return R.drawable.theme_bgr_green;
        }
    }
}
