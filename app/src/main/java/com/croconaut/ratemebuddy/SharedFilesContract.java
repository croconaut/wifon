package com.croconaut.ratemebuddy;

import android.content.Context;
import android.net.Uri;

import com.croconaut.cpt.provider.Contract;

@SuppressWarnings("FinalStaticMethod")
public final class SharedFilesContract extends Contract {
    public static final Uri getThumbnailsUri(Context context) {
        return Uri.withAppendedPath(getFilesDirUri(context), "thumbnails");
    }

    public static final Uri getStatusUri(Context context) {
        return Uri.withAppendedPath(getFilesDirUri(context), "status");
    }
}
