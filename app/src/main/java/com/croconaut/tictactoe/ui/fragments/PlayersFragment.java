package com.croconaut.tictactoe.ui.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.croconaut.ratemebuddy.AppData;
import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.activities.ToolbarDrawerActivity;
import com.croconaut.ratemebuddy.utils.pojo.profiles.Profile;
import com.croconaut.tictactoe.ui.adapter.PlayersRecyclerViewAdapter;
import com.croconaut.tictactoe.ui.adapter.model.ProfileGameWrapper;
import com.croconaut.tictactoe.ui.notifications.BaseNotificationManager;
import com.croconaut.tictactoe.ui.viewmodel.models.PlayersRecyclerViewModel;
import com.croconaut.tictactoe.ui.viewmodel.views.IPlayerView;

import java.util.Collections;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.inloop.viewmodel.base.ViewModelBaseFragment;

import static com.croconaut.tictactoe.utils.Assertions.assertNotNull;

public final class PlayersFragment extends ViewModelBaseFragment<IPlayerView, PlayersRecyclerViewModel>
        implements IPlayerView, PlayersRecyclerViewAdapter.OnPlayerClickedListener {

    @BindView(R.id.rvPeopleToPlay)
    RecyclerView mRecyclerView;

    @Nullable
    private OnInviteGamePlayerListener mCallback;

    @Nullable
    private PlayersRecyclerViewAdapter mAdapter;

    @NonNull
    public static final String TAG = PlayersFragment.class.getName();

    public PlayersFragment() {
    }

    @CheckResult
    public static PlayersFragment newInstance() {
        return new PlayersFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_tictactoe_people, container, false);
        ButterKnife.bind(this, view);

        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView.setAdapter(mAdapter);

        setModelView(this);
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new PlayersRecyclerViewAdapter(getContext(), this, Collections.<ProfileGameWrapper>emptyList());
    }

    @Override
    public void onAttach(@NonNull final Context context) {
        super.onAttach(context);

        try {
            mCallback = (OnInviteGamePlayerListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement " + OnInviteGamePlayerListener.class.getName());
        }

    }

    @Override
    public void onPlayerClicked(@NonNull final ProfileGameWrapper profileGameWrapper) {
        assertNotNull(profileGameWrapper, "profileGameWrapper");
        assertNotNull(mCallback, "mCallback");

        if (profileGameWrapper.getGameInProgress() != null) {
            mCallback.onConfirmExistingGameClick(profileGameWrapper);
        } else {
            final String playerId = profileGameWrapper.getProfile().getCrocoId();
            mCallback.onConfirmNewGameClick(playerId);
        }
    }

    @Override
    public void updateProfile(@Nullable Profile profile) {
        ((ToolbarDrawerActivity) getActivity())
                .setToolbarTitle(getResources()
                        .getString(R.string.tictactoe_invite_players_toolbar_text));
    }

    @Override
    public void error(@NonNull final String errorMsg) {
        Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
    }

    @Override
    public AppData getAppData() {
        return ((AppData) getActivity().getApplication());
    }

    @Override
    public void showPlayers(@NonNull final Set<ProfileGameWrapper> players) {
        assertNotNull(mAdapter, "mAdapter");

        mAdapter.updatePlayers(players);
    }

    @Override
    public void onResume() {
        super.onResume();

        getViewModel().updatePlayers();
        getViewModel().retrievePlayer(null);
    }

    @Nullable
    @Override
    public Class<PlayersRecyclerViewModel> getViewModelClass() {
        return PlayersRecyclerViewModel.class;
    }

    public interface OnInviteGamePlayerListener {
        void onConfirmNewGameClick(@NonNull final String playerId);

        void onConfirmExistingGameClick(@NonNull final ProfileGameWrapper profileGameWrapper);
    }
}
