package com.croconaut.ratemebuddy.ui.preferences;

import android.content.Context;
import android.content.res.Resources;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.croconaut.ratemebuddy.R;

public class MyPreferenceCategory extends PreferenceCategory {
    private Context context;

    public MyPreferenceCategory(Context context) {
        super(context);
        this.context = context;
    }

    public MyPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public MyPreferenceCategory(Context context, AttributeSet attrs,
                                int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);

        TextView titleView = (TextView) view.findViewById(android.R.id.title);
        titleView.setTextColor(typedValue.data);
    }


}