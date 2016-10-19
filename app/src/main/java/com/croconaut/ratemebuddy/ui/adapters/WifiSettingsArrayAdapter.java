package com.croconaut.ratemebuddy.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;

public class WifiSettingsArrayAdapter extends ArrayAdapter<String> {

    private final int mSelectedIndex;

    public WifiSettingsArrayAdapter(Context context, int selectedIndex, String[] values) {
        super(
                context,
                android.R.layout.simple_list_item_single_choice,
                android.R.id.text1,
                values
        );
        this.mSelectedIndex = selectedIndex;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
         View v = super.getView(position, convertView, parent);

        ((CheckedTextView)v.findViewById(android.R.id.text1)).setChecked(position == mSelectedIndex);

        return v;
    }
}
