package com.croconaut.ratemebuddy.ui.adapters;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.croconaut.ratemebuddy.AppData;
import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.activities.CommunicationActivity;
import com.croconaut.ratemebuddy.activities.EditProfileActivity;
import com.croconaut.ratemebuddy.ui.views.transformation.CircleTransform;
import com.croconaut.ratemebuddy.utils.CommonUtils;
import com.croconaut.ratemebuddy.utils.ProfileUtils;
import com.croconaut.ratemebuddy.utils.pojo.profiles.IProfile;
import com.croconaut.ratemebuddy.utils.pojo.profiles.MyProfile;

import java.util.List;

public class DisplayProfileAdapter extends RecyclerView.Adapter<DisplayProfileAdapter.ViewHolder> {
    private List<IProfile> mProfiles;
    private AppData mAppData;
    private Typeface tSemiBold;
    private MyProfile mMyProfile;


    public DisplayProfileAdapter(AppData appData, List<IProfile> profiles) {
        mProfiles = profiles;
        mAppData = appData;
        mMyProfile = MyProfile.getInstance(mAppData);
        tSemiBold = Typeface.createFromAsset(mAppData.getAssets(), "fonts/semibold.ttf");
    }


    @Override
    public DisplayProfileAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                               int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_like_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final IProfile profile = mProfiles.get(position);

        Glide.with(mAppData)
                .load(profile.getThumbUri())
                .asBitmap()
                .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                .error(ProfileUtils.getTextDrawableForProfile(profile))
                .thumbnail(0.2f)
                .transform(new CircleTransform(mAppData))
                .into(holder.ivPhoto);

        holder.tvName.setText(profile.getName());
        holder.tvName.setTypeface(tSemiBold);

        holder.rlLikeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;

                if (profile instanceof MyProfile) {
                    intent = new Intent(mAppData, EditProfileActivity.class);
                } else {
                    intent = new Intent(mAppData, CommunicationActivity.class);
                    intent.putExtra(CommunicationActivity.EXTRA_TARGET_CROCO_ID, profile.getIdent());
                    intent.putExtra(CommonUtils.EXTRA_CLEAR_NOTIF_MESS_PROFILES, true);
                }

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mAppData.startActivity(intent);
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mProfiles.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout rlLikeView;
        private final ImageView ivPhoto;
        private TextView tvName;

        public ViewHolder(View v) {
            super(v);
            rlLikeView = (RelativeLayout) v.findViewById(R.id.rlLikeView);
            ivPhoto = (ImageView) v.findViewById(R.id.ivPhoto);
            tvName = (TextView) v.findViewById(R.id.tvName);
        }
    }

}
