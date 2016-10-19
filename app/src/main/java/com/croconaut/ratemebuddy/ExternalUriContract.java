package com.croconaut.ratemebuddy;

import android.net.Uri;

public class ExternalUriContract {
    public static final String AUTHORITY = "wifon.sk";

    private static final Uri BASE_URI = new Uri.Builder()
            .scheme("http")
            .authority(AUTHORITY)
            .build();

    public static final Uri PROFILE_URI = BASE_URI.buildUpon()
            .appendPath("profiles")
            .appendPath("profile")
            .build();

    public static final String PARAM_PROFILE_NAME = "name";
    public static final String PARAM_PROFILE_CROCO_ID = "croco_id";
}
