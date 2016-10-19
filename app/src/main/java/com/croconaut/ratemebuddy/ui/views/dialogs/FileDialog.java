package com.croconaut.ratemebuddy.ui.views.dialogs;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.croconaut.ratemebuddy.AppData;
import com.croconaut.ratemebuddy.utils.pojo.UIMessage;
import com.croconaut.ratemebuddy.utils.pojo.profiles.Profile;

public abstract class FileDialog extends Dialog implements View.OnClickListener {

    protected final AppData appData;
    protected Profile remoteProfile;

    public FileDialog(final @NonNull Context context, final @NonNull AppData appData,
                      final @NonNull Profile remoteProfile) {
        super(context);
        this.appData = appData;
        this.remoteProfile = remoteProfile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    @UiThread
    abstract protected void updateText();

    @UiThread
    @CallSuper
    public void updateDialog(){
        updateText();
    };

    protected String getStorageDirectory(String TAG, UIMessage uiMessage) {
        switch (uiMessage.getUiMessageAttachment().getStorageType()) {
            case 0:
                return null;
            case 1:
                return Environment.DIRECTORY_PICTURES;
            case 2:
                return Environment.DIRECTORY_DOWNLOADS;
            default:
                Log.e(TAG, "Invalid storage type", new IllegalArgumentException());
        }

        return null;
    }
}
