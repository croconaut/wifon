package com.croconaut.ratemebuddy.utils;

import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;


public class CustomClickableUrlSpan extends ClickableSpan {
    private static final String TAG = CustomClickableUrlSpan.class.getName();

    public static final String TAG_LONG_CLICK = "TAG_LONG_CLICK";
    public static final String TAG_CLICK_HAS_DIALOG = "TAG_CLICK_HAS_DIALOG";

    private final View.OnClickListener mOnClickListener;

    public CustomClickableUrlSpan(View.OnClickListener onClickListener) {
        super();
        this.mOnClickListener = onClickListener;
    }

    @Override
    public void onClick(View view) {
        if (view.getTag() != null) {
            String tag = view.getTag().toString();
            Log.e(TAG, "TAG:" + tag);
            if (tag.equals(TAG_LONG_CLICK) || tag.equals(TAG_CLICK_HAS_DIALOG)) {
                return;
            }
        }

        Log.e(TAG, "Clicking");
        mOnClickListener.onClick(view);
    }
}
