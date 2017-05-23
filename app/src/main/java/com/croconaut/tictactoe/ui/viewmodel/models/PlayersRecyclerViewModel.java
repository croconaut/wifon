package com.croconaut.tictactoe.ui.viewmodel.models;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.croconaut.ratemebuddy.data.ProfileDataSource;
import com.croconaut.ratemebuddy.utils.pojo.profiles.Profile;
import com.croconaut.tictactoe.storage.GameRepository;
import com.croconaut.tictactoe.ui.adapter.model.ProfileGameWrapper;
import com.croconaut.tictactoe.ui.viewmodel.views.IPlayerView;
import com.croconaut.tictactoe.utils.Async;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import eu.inloop.viewmodel.AbstractViewModel;

import static com.croconaut.tictactoe.utils.Assertions.assertNotNull;

public final class PlayersRecyclerViewModel extends AbstractViewModel<IPlayerView> {

    @Override
    public void onBindView(@NonNull IPlayerView view) {
        super.onBindView(view);
        updatePlayers();
    }

    public void retrievePlayer(@Nullable final String remotePlayerId) {
        if(null == remotePlayerId){
            assertNotNull(getView(), "getView");

            getView().updateProfile(null);
            return;
        }

        new GetRemotePlayerAsync(new Async.AsyncListener<Profile>() {
            @Override
            public void onSuccess(@NonNull final Profile profile) {
                assertNotNull(getView(), "getView");
                getView().updateProfile(profile);
            }

            @Override
            public void onError(@NonNull final Throwable error) {
                assertNotNull(getView(), "getView");
                getView().error(error.getMessage());
            }
        }, remotePlayerId).run();
    }

    public void updatePlayers() {
        new GetPlayersAsync(new Async.AsyncListener<Set<ProfileGameWrapper>>() {
            @Override
            public void onSuccess(@NonNull final Set<ProfileGameWrapper> profileList) {
                if(getView() != null) {
                    getView().showPlayers(profileList);
                }else {
                    Log.e("PlayersRecyclerViewMode", "GetView is null");
                }
            }

            @Override
            public void onError(@NonNull final Throwable error) {
                assertNotNull(getView(), "getView");
                getView().error(error.getMessage());
            }
        }).run();
    }

    private class GetPlayersAsync extends Async<Set<ProfileGameWrapper>> {

        /*package*/ GetPlayersAsync(@NonNull final AsyncListener<Set<ProfileGameWrapper>> listener) {
            super(listener);
        }

        @Override
        public Set<ProfileGameWrapper> call() throws Throwable {
            if (getView() != null) {
                final ProfileDataSource profileDataSource
                        = getView().getAppData().getProfileDataSource();

                final List<ProfileGameWrapper> allProfiles = new ArrayList<>();
                allProfiles.addAll(getProfileGameWrapperList(getView().getAppData().getNearbyPeople()));
                allProfiles.addAll(getProfileGameWrapperList(profileDataSource.getProfilesByType(Profile.FAVOURITE)));
                allProfiles.addAll(getProfileGameWrapperList(profileDataSource.getProfilesByType(Profile.UNKNOWN)));
                allProfiles.addAll(getProfileGameWrapperList(profileDataSource.getProfilesByType(Profile.CACHED)));

                Set<ProfileGameWrapper> set = new TreeSet<>(allProfiles);
                return Collections.unmodifiableSet(set);
            } else {
                return Collections.emptySet();
            }
        }

        @NonNull
        private List<ProfileGameWrapper> getProfileGameWrapperList(@NonNull List<Profile> profiles) {
            if (getView() != null) {
                final GameRepository gameRepository = getView().getAppData().getGameRepository();

                final List<ProfileGameWrapper> profileGameWrapperList = new ArrayList<>(profiles.size());

                for (final Profile profile : profiles) {
                    final ProfileGameWrapper profileGameWrapper =
                            new ProfileGameWrapper(
                                    profile,
                                    gameRepository.getGamesByPlayerId(profile.getCrocoId())
                            );
                    profileGameWrapperList.add(profileGameWrapper);
                }

                return profileGameWrapperList;
            } else {
                return Collections.emptyList();
            }
        }
    }

    private class GetRemotePlayerAsync extends Async<Profile> {

        @NonNull
        private final String mRemoteProfileId;

        /*package*/ GetRemotePlayerAsync(@NonNull final AsyncListener<Profile> listener,
                                         @NonNull final String remoteProfileId) {
            super(listener);
            this.mRemoteProfileId = remoteProfileId;
        }

        @Override
        public Profile call() throws Throwable {
            assertNotNull(getView(), "getView");
            return getView().getAppData()
                    .getProfileDataSource().getProfileByCrocoId(mRemoteProfileId);
        }
    }
}
