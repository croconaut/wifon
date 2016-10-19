package com.croconaut.ratemebuddy.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.croconaut.cpt.ui.CptController;
import com.croconaut.ratemebuddy.AppData;
import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.ui.adapters.BlockedUsersAdapter;
import com.croconaut.ratemebuddy.ui.adapters.ChangeColorAdapter;
import com.croconaut.ratemebuddy.utils.ProfileUtils;
import com.croconaut.ratemebuddy.utils.pojo.profiles.MyProfile;
import com.croconaut.ratemebuddy.utils.pojo.profiles.Profile;

import java.io.IOException;

@SuppressLint({"NewApi"})
@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity implements CptProcessor {
    private static final String TAG = SettingsActivity.class.getName();

    public static final String COLOR_PREF = "pref_color";
    public static final String VIB_PREF = "pref_vib";
    public static final String SOUND_PREF = "pref_sound";
    public static final String SOUND_RING_TONE_PREF = "pref_ringtone";
    public static final String NOTIFICATION_PREF = "pref_notification";
    public static final String NEARBY_NOTIF_PREF = "pref_nearby_notif";
    public static final String ADVANCED_SETTINGS_PREF = "pref_advanced_settings";
    public static final String INTERNET_SERVER_PREF = "pref_internet_server";
    public static final String DISPLAY_INFO_PREF = "pref_display_info";
    public static final String SHOW_ME_IN_FRIENDS_PREF = "pref_show_me_as_friend";
    public static final String BLOCKED_USERS_PREF = "pref_blocked_users";

    private SharedPreferences mPrefs;
    private AppData appData;
    private CptController cptController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        chooseTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        View root = findViewById(R.id.settingsRoot);
        root.setBackgroundColor(ContextCompat.getColor(this, R.color.material_white));

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        appData = (AppData) getApplication();
        cptController = new CptController(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setClickable(true);
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });

        addPreferencesFromResource(R.xml.preferences);

        CheckBoxPreference serverPref = (CheckBoxPreference) findPreference(INTERNET_SERVER_PREF);
        serverPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean notifEnabled = getPreferenceManager().getSharedPreferences().getBoolean(preference.getKey(), true);
                cptController.setInternetEnabled(notifEnabled);
                return true;
            }
        });

        CheckBoxPreference displayInfo = (CheckBoxPreference) findPreference(DISPLAY_INFO_PREF);
        displayInfo.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean notifEnabled = getPreferenceManager().getSharedPreferences().getBoolean(preference.getKey(), true);
                cptController.setTrackingEnabled(notifEnabled);
                return true;
            }
        });

        CheckBoxPreference showMeInFriends = (CheckBoxPreference) findPreference(SHOW_ME_IN_FRIENDS_PREF);
        showMeInFriends.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                MyProfile myProfile = MyProfile.getInstance(appData);
                try {
                    ProfileUtils.createAndSendRmbProfile(
                            appData,
                            myProfile,
                            myProfile.getThumbUri()
                    );
                }catch (IOException e){
                    Log.e(TAG, "Cannot send profile", e);
                }

                return true;
            }
        });

        Preference advancedPref = findPreference(ADVANCED_SETTINGS_PREF);
        advancedPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(SettingsActivity.this, CptSettingsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                return true;
            }
        });

        CheckBoxPreference notifPref = (CheckBoxPreference) findPreference(NOTIFICATION_PREF);
        notifPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean notifEnabled = getPreferenceManager().getSharedPreferences().getBoolean(preference.getKey(), true);
                getPreferenceScreen().findPreference(VIB_PREF).setEnabled(notifEnabled);
                getPreferenceScreen().findPreference(SOUND_PREF).setEnabled(notifEnabled);
                getPreferenceScreen().findPreference(SOUND_RING_TONE_PREF).setEnabled(notifEnabled);
                getPreferenceScreen().findPreference(NEARBY_NOTIF_PREF).setEnabled(notifEnabled);
                return true;
            }
        });


        CheckBoxPreference soundPref = (CheckBoxPreference) findPreference(SOUND_PREF);
        soundPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                boolean notifEnabled = getPreferenceManager().getSharedPreferences().getBoolean(preference.getKey(), true);
                getPreferenceScreen().findPreference(SOUND_RING_TONE_PREF).setEnabled(notifEnabled);
                return true;
            }
        });

        final Preference colorPref = findPreference(COLOR_PREF);
        colorPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Dialog.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPrefs.edit().putString(
                                COLOR_PREF,
                                String.valueOf(which)
                        ).apply();

                        TaskStackBuilder.create(SettingsActivity.this)
                                .addNextIntent(new Intent(SettingsActivity.this, TimelineActivity.class))
                                .addNextIntent(SettingsActivity.this.getIntent())
                                .startActivities();
                    }
                };

                displayPreferenceDialog(
                        preference,
                        onClickListener,
                        Integer.valueOf(mPrefs.getString(COLOR_PREF, "1")),
                        getResources().getStringArray(R.array.colorNames)
                );
                return false;
            }
        });

        final Preference blockedPref = findPreference(BLOCKED_USERS_PREF);
        blockedPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(SettingsActivity.this);
                builderSingle.setIcon(R.drawable.ic_launcher);
                builderSingle.setTitle(preference.getTitle());

                View customView = getLayoutInflater().inflate(R.layout.blocked_users_dialog, null);
                ListView listView = (ListView) customView.findViewById(R.id.blocked_users_list);

                TextView text = (TextView) customView.findViewById(R.id.blocked_users_dialog_empty);
                text.setTypeface(Typeface.createFromAsset(appData.getAssets(), "fonts/light.ttf"));
                listView.setEmptyView(text);

                builderSingle.setNegativeButton(
                        getResources().getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                ArrayAdapter<Profile> blockArrayAdapter = new BlockedUsersAdapter(
                        appData,
                        appData.getProfileDataSource().getProfilesByType(Profile.BLOCKED)
                );
                listView.setAdapter(blockArrayAdapter);


                builderSingle.setView(customView);
                builderSingle.show();

                return false;
            }
        });

    }

    private void chooseTheme() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int themeId = Integer.parseInt(prefs.getString(
                SettingsActivity.COLOR_PREF, "1"));
        switch (themeId) {
            case 0:
                setTheme(R.style.AppTheme_Red);
                break;
            case 1:
                setTheme(R.style.AppTheme_Green);
                break;
            case 2:
                setTheme(R.style.AppTheme_Blue);
                break;
            case 3:
                setTheme(R.style.AppTheme_Orange);
                break;
            case 4:
                setTheme(R.style.AppTheme_Pink);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void displayPreferenceDialog(Preference preference,
                                         DialogInterface.OnClickListener onClickListener,
                                         int selectedValue,
                                         String[] values) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(SettingsActivity.this);
        builderSingle.setIcon(R.drawable.ic_launcher);
        builderSingle.setTitle(preference.getTitle());


        //future usage
        switch (preference.getKey()) {
            case COLOR_PREF:
                ArrayAdapter<CharSequence> changeColorAdapter = new ChangeColorAdapter(
                        SettingsActivity.this,
                        R.layout.list_view_item_pref_category,
                        values,
                        selectedValue
                );
                builderSingle.setAdapter(changeColorAdapter, onClickListener);
                break;
        }


        builderSingle.setNegativeButton(
                getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builderSingle.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    }

    @Override
    public boolean process(Intent cptIntent) {
        return false;
    }
}
