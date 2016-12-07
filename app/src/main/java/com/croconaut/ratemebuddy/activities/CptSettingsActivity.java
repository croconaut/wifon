package com.croconaut.ratemebuddy.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.croconaut.cpt.ui.CptController;
import com.croconaut.cpt.ui.LinkLayerMode;
import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.ui.adapters.WifiSettingsArrayAdapter;


@SuppressLint({"NewApi"})
@SuppressWarnings("deprecation")
public class CptSettingsActivity extends PreferenceActivity implements CptProcessor {

    public static final String PREFS_MODE = "prefs_more";
    public static final String PREFS_BATTERY_OPTIMIZATION = "prefs_battery_optimization";
    public static final String PREFS_WIFI_POLICY = "prefs_wifi_policy";
    public static final String PREFS_WORKAROUND = "prefs_workaround";

    private SharedPreferences mPrefs;
    private CptController mCptController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        chooseTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cpt_settings);

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


        mCptController = new CptController(this);

        addPreferencesFromResource(R.xml.advanced_preferences);
        PreferenceManager.setDefaultValues(this, R.xml.advanced_preferences, false);

        final Preference cptModePrefs =  findPreference(PREFS_MODE);
        cptModePrefs.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Dialog.OnClickListener onClickListener =  new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                mCptController.setMode(LinkLayerMode.FOREGROUND);
                                break;
                            case 1:
                                mCptController.setMode(LinkLayerMode.BACKGROUND);
                                break;
                            case 2:
                                mCptController.setMode(LinkLayerMode.OFF);
                                Toast.makeText(CptSettingsActivity.this, getString(R.string.prefs_message_off_warning), Toast.LENGTH_SHORT).show();
                                break;
                        }

                        mPrefs.edit().putString(PREFS_MODE, String.valueOf(which)).apply();

                        refreshModeSummary();
                    }
                };

                displayPreferenceDialog(
                        preference,
                        onClickListener,
                        Integer.valueOf(mPrefs.getString(preference.getKey(), "2")),
                        getResources().getStringArray(R.array.moreSettings)
                );
                return true;
            }
        });

        if (mCptController.isBatteryOptimizationModifiable()) {
            addPreferencesFromResource(R.xml.checkbox_battery_optimization);

            Preference batteryPrefs = findPreference(PREFS_BATTERY_OPTIMIZATION);
            batteryPrefs.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    mCptController.setBatteryOptimizationEnabled((boolean) newValue);
                    refreshBatterySummary();
                    return true;
                }
            });
        } else {
            addPreferencesFromResource(R.xml.preference_battery_optimization);

            Preference batteryPrefs = findPreference(PREFS_BATTERY_OPTIMIZATION);
            batteryPrefs.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(mCptController.getAdvancedWifiIntent());
                    return true;
                }
            });
        }

        addPreferencesFromResource(R.xml.preference_wifi_policy);
        Preference wifiPrefs = findPreference(PREFS_WIFI_POLICY);
        wifiPrefs.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (mCptController.isWifiSleepPolicyModifiable()) {
                    Dialog.OnClickListener onClickListener =  new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    mCptController.setWifiSleepPolicy(CptController.SleepPolicy.WIFI_SLEEP_POLICY_DEFAULT);
                                    break;
                                case 1:
                                    mCptController.setWifiSleepPolicy(CptController.SleepPolicy.WIFI_SLEEP_POLICY_NEVER_WHILE_PLUGGED);
                                    break;
                                case 2:
                                    mCptController.setWifiSleepPolicy(CptController.SleepPolicy.WIFI_SLEEP_POLICY_NEVER);
                                    break;
                            }

                            //write pref
                            mPrefs.edit().putInt(PREFS_WIFI_POLICY, which).apply();

                            //refresh UI
                            refreshWifiPolicySummary();
                        }
                    };

                    displayPreferenceDialog(
                            preference,
                            onClickListener,
                            mPrefs.getInt(preference.getKey(), 0),
                            getResources().getStringArray(R.array.wifiPolicySettings)
                    );
                }else startActivity(mCptController.getAdvancedWifiIntent());
                return true;
            }
        });

        addPreferencesFromResource(R.xml.preferences_workaround);

        CheckBoxPreference workaroundPrefs = (CheckBoxPreference) findPreference(PREFS_WORKAROUND);
        workaroundPrefs.setChecked(mCptController.getDimScreenWorkaroundEnabled());
        workaroundPrefs.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                mCptController.setDimScreenWorkaroundEnabled((boolean) newValue);
                verifyDimScreenWorkaround();
                return true;
            }
        });

        initRefreshWifiPolicyPreferenceValue();
    }

    @Override
    public boolean process(Intent cptIntent) {
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // if we return from Advanced WiFi activity
        refreshBatterySummary();
        refreshWifiPolicySummary();
        refreshModeSummary();
    }

    private void refreshBatterySummary() {
        Preference batteryOptimizationPrefs = findPreference(PREFS_BATTERY_OPTIMIZATION);
        if (batteryOptimizationPrefs instanceof CheckBoxPreference) {
            ((CheckBoxPreference) batteryOptimizationPrefs).setChecked(mCptController.getBatteryOptimizationEnabled());
        }
        if (mCptController.getBatteryOptimizationEnabled()) {
            batteryOptimizationPrefs.setSummary(
                    Html.fromHtml("<font color=\"" + getResources().getColor(R.color.material_red_500) + "\">" +
                            getString(R.string.prefs_battery_enabled) + "</font>")
            );
        } else {
            batteryOptimizationPrefs.setSummary(
                    Html.fromHtml("<font color=\"" + getResources().getColor(R.color.material_light_green_500) + "\">" +
                            getString(R.string.prefs_battery_disabled) + "</font>")
            );
        }
    }

    private void initRefreshWifiPolicyPreferenceValue() {
        switch (mCptController.getWifiSleepPolicy()) {
            case WIFI_SLEEP_POLICY_DEFAULT:
                mPrefs.edit().putInt(PREFS_WIFI_POLICY, 0).apply();
                break;
            case WIFI_SLEEP_POLICY_NEVER_WHILE_PLUGGED:
                mPrefs.edit().putInt(PREFS_WIFI_POLICY, 1).apply();
                break;
            case WIFI_SLEEP_POLICY_NEVER:
                mPrefs.edit().putInt(PREFS_WIFI_POLICY, 2).apply();
                break;
        }
    }

    private void refreshModeSummary(){
        int value = Integer.valueOf(mPrefs.getString(PREFS_MODE, "2"));
        Preference modePrefs = findPreference(PREFS_MODE);

        switch (value) {
            case 2:
                modePrefs.setSummary(
                        Html.fromHtml("<font color=\"" + getResources().getColor(R.color.material_red_500) + "\">" +
                                getResources().getStringArray(R.array.moreSettings)[value] + "</font>")
                );
                break;
            case 1:
                modePrefs.setSummary(
                        Html.fromHtml("<font color=\"" + getResources().getColor(R.color.material_orange_500) + "\">" +
                                getResources().getStringArray(R.array.moreSettings)[value] + "</font>")
                );
                break;
            case 0:
                modePrefs.setSummary(
                        Html.fromHtml("<font color=\"" + getResources().getColor(R.color.material_light_green_500) + "\">" +
                                getResources().getStringArray(R.array.moreSettings)[value] + "</font>")
                );
                break;
        }
    }

    private void refreshWifiPolicySummary() {
        Preference wifiPolicyPrefs = findPreference(PREFS_WIFI_POLICY);
        switch (mCptController.getWifiSleepPolicy()) {
            case WIFI_SLEEP_POLICY_DEFAULT:
                wifiPolicyPrefs.setSummary(
                        Html.fromHtml("<font color=\"" + getResources().getColor(R.color.material_red_500) + "\">" +
                                getString(R.string.prefs_wifi_never) + "</font>")
                );
                break;
            case WIFI_SLEEP_POLICY_NEVER_WHILE_PLUGGED:
                wifiPolicyPrefs.setSummary(
                        Html.fromHtml("<font color=\"" + getResources().getColor(R.color.material_orange_500) + "\">" +
                                getString(R.string.prefs_wifi_if_connected) + "</font>")
                );
                break;
            case WIFI_SLEEP_POLICY_NEVER:
                wifiPolicyPrefs.setSummary(
                        Html.fromHtml("<font color=\"" + getResources().getColor(R.color.material_light_green_500) + "\">" +
                                getString(R.string.prefs_wifi_always) + "</font>")
                );
                break;
        }
    }

    private void verifyDimScreenWorkaround() {
        if (mCptController.isDimScreenWorkaroundRecommended() && !mCptController.getDimScreenWorkaroundEnabled()) {
            Toast.makeText(this, getString(R.string.prefs_message_workaround_recommended), Toast.LENGTH_SHORT).show();
        }
    }

    private void chooseTheme() {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        int themeId = Integer.parseInt(mPrefs.getString(
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

    private void displayPreferenceDialog(Preference preference,
                                         DialogInterface.OnClickListener onClickListener,
                                         int selectedValue,
                                         String[] values) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(CptSettingsActivity.this);
        builderSingle.setIcon(R.drawable.ic_launcher);
        builderSingle.setTitle(preference.getTitle());

        final ArrayAdapter<String> arrayAdapter = new WifiSettingsArrayAdapter(
                CptSettingsActivity.this,
                selectedValue,
                values
        );


        builderSingle.setNegativeButton(
                getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builderSingle.setAdapter(arrayAdapter, onClickListener);
        builderSingle.show();
    }
}
