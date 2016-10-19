package com.croconaut.ratemebuddy.ui.adapters;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.croconaut.ratemebuddy.R;

public class ChoosePhotoAdapter extends ArrayAdapter<CharSequence> {

    private final Context mContext;
    private final String[] mOptions;

    public ChoosePhotoAdapter(final Context context, String[] options) {
        super(context, R.layout.activity_main, options);
        this.mContext = context;
        this.mOptions = options;
    }

    @Override
    @SuppressLint("ViewHolder")
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
        View row = inflater.inflate(R.layout.list_view_item_choose_photo, parent, false);

        ImageView imageView = (ImageView) row.findViewById(R.id.image);

        int resId;
        if(position == 0){
            resId = R.drawable.ic_camera_alt_grey600_36dp;
        }else{
            resId = R.drawable.ic_gallery;
        }

        Glide.with(mContext)
                .load(resId)
                .into(imageView);

        TextView textView = (TextView) row.findViewById(R.id.tvChoosePhoto);
        textView.setText(mOptions[position]);

        return row;
    }
}
