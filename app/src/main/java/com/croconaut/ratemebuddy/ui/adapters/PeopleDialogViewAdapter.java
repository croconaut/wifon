package com.croconaut.ratemebuddy.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.utils.pojo.profiles.Profile;

import java.util.List;


public class PeopleDialogViewAdapter extends BaseAdapter {

    private final List<Profile> mProfiles;
    private final Context mContext;

    public PeopleDialogViewAdapter(Context context, List<Profile> profiles){
        this.mProfiles = profiles;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return mProfiles.size();
    }

    @Override
    public Object getItem(int position) {
        return mProfiles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Profile profile = mProfiles.get(position);

        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_dialog_choose_people, parent, false);
            holder = new ViewHolder();
            holder.ivImage = (ImageView) convertView.findViewById(R.id.ivPhoto);
            holder.tvName = (TextView) convertView.findViewById(R.id.tvName);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvName.setText(profile.getName());
        Glide.with(mContext)
                .load(profile.getThumbUri())
                .asBitmap()
                .thumbnail(0.2f)
                .into(holder.ivImage);

        return convertView;
    }

    private class ViewHolder {
        TextView tvName;
        ImageView ivImage;
    }

}
