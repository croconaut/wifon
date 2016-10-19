package com.croconaut.ratemebuddy.ui.adapters;


import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.croconaut.cpt.data.Communication;
import com.croconaut.ratemebuddy.AppData;
import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.utils.pojo.profiles.Profile;

import java.util.List;

public class BlockedUsersAdapter extends ArrayAdapter<Profile> {

    private final Typeface tLight;
    private final AppData appData;
    private final List<Profile> mProfiles;

    public BlockedUsersAdapter(AppData appData, List<Profile> profiles) {
        super(appData, R.layout.activity_main, profiles);
        this.appData = appData;
        this.mProfiles = profiles;
        tLight = Typeface.createFromAsset(appData.getAssets(), "fonts/light.ttf");
    }

    @SuppressLint({"ViewHolder", "InflateParams"})
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(appData);
        View v = inflater.inflate(R.layout.blocked_users_list_item, parent, false);

        final Profile profile = mProfiles.get(position);
        TextView text = (TextView) v.findViewById(R.id.blocked_user_item_text);

        text.setText(profile.getName());
        text.setTypeface(tLight);

        ImageButton button = (ImageButton) v.findViewById(R.id.blocked_user_item_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Communication.changeUserTrustLevel(getContext(), profile.getCrocoId(), Communication.USER_TRUST_LEVEL_NORMAL);
                profile.setType(Profile.UNKNOWN);
                appData.getProfileDataSource().updateProfile(profile);
                Toast.makeText(
                        appData,
                        appData.getResources().getString(R.string.toast_menu_blocked_removed, profile.getName()),
                        Toast.LENGTH_LONG).show();

                remove(profile);
                mProfiles.remove(profile);
                notifyDataSetChanged();
            }
        });

        return v;
    }
}
