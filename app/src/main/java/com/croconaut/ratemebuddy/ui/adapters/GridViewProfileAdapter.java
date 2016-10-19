package com.croconaut.ratemebuddy.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.croconaut.ratemebuddy.AppData;
import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.activities.CommunicationActivity;
import com.croconaut.ratemebuddy.activities.DisplayProfilesActivity;
import com.croconaut.ratemebuddy.activities.EditProfileActivity;
import com.croconaut.ratemebuddy.ui.views.transformation.CircleTransform;
import com.croconaut.ratemebuddy.utils.ProfileUtils;
import com.croconaut.ratemebuddy.utils.ThemeManager;
import com.croconaut.ratemebuddy.utils.pojo.profiles.IProfile;
import com.croconaut.ratemebuddy.utils.pojo.profiles.MyProfile;
import com.croconaut.ratemebuddy.utils.pojo.profiles.Profile;

import java.util.List;


public class GridViewProfileAdapter extends BaseAdapter {
    private static final String TAG = GridViewProfileAdapter.class.getName();

    private static final int MAX_PROFILES_DISPLAY_COUNT = 4; // +1 0..3

    private final List<IProfile> mProfiles;
    private final Context mContext;
    private final MyProfile mMyProfile;
    private final ProfileUtils mProfileUtils;
    private final IProfile mRemoteProfile;
    private final int mProfileSize;
    private final int mDisplayType;


    public GridViewProfileAdapter(Context context, IProfile profile, AppData appData, List<IProfile> profileList, int totalFriends, int displayType) {
        this.mProfiles = profileList;
        this.mContext = context;
        this.mProfileUtils = new ProfileUtils(appData);
        this.mMyProfile = MyProfile.getInstance(mContext);
        this.mRemoteProfile = profile;
        this.mProfileSize = totalFriends;
        this.mDisplayType = displayType;
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
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        final IProfile profile;

        String identToCompare = mProfiles.get(position) instanceof Profile
                ? ((Profile) mProfiles.get(position)).getProfileId()
                : mMyProfile.getProfileId();
        final boolean isMyProfile = identToCompare != null && identToCompare.equals(mMyProfile.getIdent());

        if (isMyProfile)
            profile = mMyProfile;
        else {
            profile = mProfileUtils.findProfile(
                    mProfiles.get(position).getIdent(),
                    mProfiles.get(position).getName()
            );
        }

        if (convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(
                    (int) mContext.getResources().getDimension(R.dimen.gv_friends_idem_width),
                    (int) mContext.getResources().getDimension(R.dimen.gv_friends_idem_height)));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        boolean showLastProfile = mProfileSize == (MAX_PROFILES_DISPLAY_COUNT + 1);


        mMyProfile.getThumbUri();
        if (position < MAX_PROFILES_DISPLAY_COUNT || showLastProfile)
            Glide.with(mContext)
                    .load(profile.getThumbUri())
                    .asBitmap()
                    .transform(new CircleTransform(mContext))
                    .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                    .error(ProfileUtils.getTextDrawableForProfile(profile))
                    .thumbnail(0.3f)
                    .into(imageView);
        else {
            String displayPlusCount = "+" + (mProfileSize - MAX_PROFILES_DISPLAY_COUNT);

            ThemeManager themeManager = new ThemeManager(mContext);
            Glide.with(mContext)
                    .load("null") // on purpose to force error
                    .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                    .transform(new CircleTransform(mContext))
                    .error(TextDrawable.builder().buildRound(
                            displayPlusCount,
                            themeManager.getCurrentColorHexa()
                    ))
                    .thumbnail(0.3f)
                    .into(imageView);
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;

                if (position < MAX_PROFILES_DISPLAY_COUNT) {
                    if (isMyProfile) {
                        intent = new Intent(mContext, EditProfileActivity.class);
                    } else {
                        intent = new Intent(mContext, CommunicationActivity.class);
                        intent.putExtra(CommunicationActivity.EXTRA_TARGET_CROCO_ID,
                                mProfiles.get(position).getIdent());
                    }
                } else {
                    intent = new Intent(mContext, DisplayProfilesActivity.class);
                    intent.putExtra(DisplayProfilesActivity.EXTRA_REMOTE_PROFILE_ID, mRemoteProfile.getIdent());
                    intent.putExtra(DisplayProfilesActivity.EXTRA_DISPLAY_TYPE, mDisplayType);
                }

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        });

        return imageView;
    }
}
