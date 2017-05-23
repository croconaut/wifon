package com.croconaut.tictactoe.ui.viewmodel.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.croconaut.ratemebuddy.utils.pojo.profiles.Profile;
import com.croconaut.tictactoe.ui.viewmodel.views.IErrorView;
import com.croconaut.tictactoe.utils.Async;

import eu.inloop.viewmodel.AbstractViewModel;

import static com.croconaut.tictactoe.utils.Assertions.assertNotNull;


public final class GameOptionsViewModel extends AbstractViewModel<IErrorView> {

    public void retrievePlayer(@Nullable final String remotePlayerId) {
        if (null == remotePlayerId) {
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
