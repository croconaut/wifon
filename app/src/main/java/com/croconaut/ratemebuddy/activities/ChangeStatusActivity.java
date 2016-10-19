package com.croconaut.ratemebuddy.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.data.pojo.Status;
import com.croconaut.ratemebuddy.ui.adapters.StatusesRecyclerAdapter;
import com.croconaut.ratemebuddy.utils.CommonUtils;
import com.croconaut.ratemebuddy.utils.ItemClickCallback;
import com.croconaut.ratemebuddy.utils.pojo.profiles.MyProfile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChangeStatusActivity extends WifonActivity implements CptProcessor, ItemClickCallback {

    private static final String TAG = ChangeStatusActivity.class.getName();

    private List<Status> mStatuses;
    private List<Status> mFilteredStatuses;

    private SearchView searchView;
    private ImageButton addStatus;
    private RelativeLayout rlAddStatus;
    private RecyclerView rvStatuses;
    private StatusesRecyclerAdapter rvAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_status);
        initializeViews();
    }

    private void initializeViews() {
        toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setClickable(true);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitle(getString(R.string.activity_change_status));
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });

        searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setIconified(false);
        searchView.setLayoutParams(new Toolbar.LayoutParams(Gravity.RIGHT));
        searchView.setOnQueryTextListener(new SearchHandler());

        addStatus = (ImageButton) findViewById(R.id.addStatus);
        addStatus.setLayoutParams(new Toolbar.LayoutParams(Gravity.RIGHT));
        addStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveStatus(searchView.getQuery().toString());
            }
        });

        rlAddStatus = (RelativeLayout) findViewById(R.id.rlAddStatus);

        rvStatuses = (RecyclerView) findViewById(R.id.rvStatuses);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvStatuses.setLayoutManager(layoutManager);

        mStatuses = appData.getStatusDataSource().getAllMyStatuses();
        ArrayList<Status> toRemove = new ArrayList<>();
        for (Status status : mStatuses) {
            if (status.getContent() == null) toRemove.add(status);
        }
        mStatuses.removeAll(toRemove);

        rvAdapter = new StatusesRecyclerAdapter(mStatuses, mContext, this);
        rvStatuses.setAdapter(rvAdapter);

        if (mStatuses.isEmpty()) {
            rvStatuses.setVisibility(View.GONE);
            RelativeLayout rlNoStatuses = (RelativeLayout) findViewById(R.id.noStatuses);
            rlNoStatuses.setVisibility(View.VISIBLE);
            TextView tvNoStatuses = (TextView) findViewById(R.id.tvNoStatuses);
            tvNoStatuses.setTypeface(tLight);
        }

    }

    @Override
    public boolean process(Intent cptIntent) throws IOException, ClassNotFoundException {
        super.process(cptIntent);
        return false;
    }

    @Override
    public void onItemClick(Status status) {
        Log.e(TAG, "Status: " + status.getStatusID());
        Toast.makeText(mContext, mContext.getString(R.string.toast_status_changed), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(ChangeStatusActivity.this, EditProfileActivity.class);
        intent.putExtra(EditProfileActivity.EXTRA_STATUS_ID, status.getStatusID());
        startActivity(intent);
        finish();
    }

    private class SearchHandler implements SearchView.OnQueryTextListener {

        @Override
        public boolean onQueryTextChange(final String query) {
            //always search through all statuses in case (part of) text was deleted
            mFilteredStatuses = performSearch(appData.getStatusDataSource().getAllMyStatuses(), query);
            ArrayList<Status> toRemove = new ArrayList<>();
            for (Status status : mFilteredStatuses) {
                if (status.getContent() == null) toRemove.add(status);
            }
            mFilteredStatuses.removeAll(toRemove);
            mStatuses.clear();
            mStatuses.addAll(mFilteredStatuses);
            rvAdapter.notifyDataSetChanged();

            if (mFilteredStatuses.isEmpty()) {
                rlAddStatus.setVisibility(View.VISIBLE);
                rlAddStatus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        saveStatus(query);
                    }
                });
            } else {
                rlAddStatus.setVisibility(View.GONE);
            }
            return false;
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

    }

    private List<Status> performSearch(List<Status> statuses, String query) {
        List<Status> filteredStatuses = new ArrayList<>();

        for (Status status : statuses) {
            // Content to search through (in lower case).
            if (status.getContent() != null) {
                String content = (status.getContent()).toLowerCase();

                if (content.contains(query.toLowerCase())) {
                    filteredStatuses.add(status);
                }
            }
        }

        return filteredStatuses;
    }

    private void saveStatus(String query) {
        MyProfile myProfile = MyProfile.getInstance(mContext);
        Status status = null;
        if (!query.trim().isEmpty()) {
            status = new Status
                    .Builder(
                    System.currentTimeMillis(),
                    UUID.randomUUID().toString(),
                    CommonUtils.getMyId(mContext),
                    query,
                    myProfile.getName())
                    .build();
            Toast.makeText(mContext, getString(R.string.toast_status_created), Toast.LENGTH_SHORT).show();
        } else {
            List<Status> statuses = appData.getStatusDataSource().getAllMyStatuses();
            for (Status dbStatus : statuses) {
                if (dbStatus.getContent() == null) {
                    status = dbStatus; //TODO Matej: timestamp ?
                }
            }

            if (status == null)
                status = new Status
                        .Builder(System.currentTimeMillis(), UUID.randomUUID().toString(), CommonUtils.getMyId(mContext), null, myProfile.getName())
                        .build();
        }

        appData.getStatusDataSource().insertStatus(status);

        onItemClick(status);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.menuCheck);
        item.setVisible(true);
        return true;
    }

}
