package com.croconaut.ratemebuddy.activities;


import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.ListView;
import android.widget.TextView;

import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.ui.adapters.AttributionsAdapter;

import java.io.IOException;

public class AboutActivity extends WifonActivity implements CptProcessor {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        initializeHeaderWithDrawer(mRes.getString(R.string.action_about), false);

        // find views
        TextView tvAppname = (TextView) findViewById(R.id.tvAppName);
        TextView tvVersion = (TextView) findViewById(R.id.tvVersion);
        TextView tvAbout = (TextView) findViewById(R.id.tvTradeMark);
        TextView tvMail = (TextView) findViewById(R.id.tvMailCroco);

        // typefaces
        tvAppname.setTypeface(tBold);
        tvAbout.setTypeface(tRegular);
        tvMail.setTypeface(tRegular);

        // get package name etc. for version
        StringBuilder sb = new StringBuilder();
        try {
            PackageManager manager = getPackageManager();
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            sb.append(mRes.getString(R.string.dialog_about_version, info.versionName, info.versionCode));
        } catch (PackageManager.NameNotFoundException e) {
            // cannot happen obviously, its OUR package name! :-)
        }

        // set correct values to textviews
        tvVersion.setText(sb.toString());
        tvAbout.setMovementMethod(LinkMovementMethod.getInstance());
        tvAbout.setText(Html.fromHtml(mRes.getString(R.string.dialog_about_trademark)));

        String[] attributionsArray = getResources().getStringArray(R.array.attributions);
        ListView attributions = (ListView) findViewById(R.id.attributions);
        AttributionsAdapter adapter = new AttributionsAdapter(this, attributionsArray);
        attributions.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean process(Intent cptIntent) throws IOException, ClassNotFoundException {
        super.process(cptIntent);
        return false;
    }
}
