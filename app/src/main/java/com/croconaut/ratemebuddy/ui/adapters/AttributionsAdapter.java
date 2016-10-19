package com.croconaut.ratemebuddy.ui.adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.croconaut.ratemebuddy.R;

/**
 * Created by Juraj on 18.11.2015.
 */
public class AttributionsAdapter extends ArrayAdapter<String> {

    private Context mContext;
    private String[] mAttributions;

    public AttributionsAdapter(Context context, String[] attributions) {
        super(context, -1, attributions);
        mAttributions = attributions;
        mContext = context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.attribution_item, parent, false);
        TextView tvAttribution = (TextView) rowView.findViewById(R.id.tvAttribution);
        tvAttribution.setText(Html.fromHtml(mAttributions[position]));

        return rowView;
    }
}