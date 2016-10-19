package com.croconaut.ratemebuddy.ui.adapters;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.utils.ThemeManager;

public class ChangeColorAdapter extends ArrayAdapter<CharSequence> {

    private int mSelectedValue;
    private final Context mContext;

    public ChangeColorAdapter(final Context context, int textViewResourceId,
                                    CharSequence[] objects, int selectedValue) {
        super(context, textViewResourceId, objects);
        this.mSelectedValue = selectedValue;
        this.mContext = context;
    }

    @Override
    @SuppressLint("ViewHolder")
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
        View row = inflater.inflate(R.layout.list_view_item_pref_category, parent, false);
        ImageView imageView = (ImageView) row.findViewById(R.id.image);
        imageView.setTag(position);

        int color = ContextCompat.getColor(mContext,ThemeManager.colors[position]);
        TextDrawable drawable = TextDrawable.builder().buildRect("", color);
        imageView.setImageDrawable(drawable);

        CheckedTextView checkedTextView = (CheckedTextView) row.findViewById(R.id.check);
        checkedTextView.setText(getItem(position));
        if (position == mSelectedValue)
            checkedTextView.setChecked(true);

        return row;
    }
}
