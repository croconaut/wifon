package com.croconaut.tictactoe.ui.viewmodel.models;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.croconaut.ratemebuddy.utils.pojo.profiles.Profile;
import com.croconaut.tictactoe.payload.games.Game;
import com.croconaut.tictactoe.payload.moves.Move;
import com.croconaut.tictactoe.ui.viewmodel.views.IGameView;
import com.croconaut.tictactoe.utils.Async;

import java.util.List;

import eu.inloop.viewmodel.AbstractViewModel;

import static com.croconaut.tictactoe.utils.Assertions.assertNotNull;


public final class GameViewModel extends AbstractViewModel<IGameView> {

    public void initGame(@NonNull final String gameId) {
        assertNotNull(gameId, "gameId");

        new GetGameAsync(new Async.AsyncListener<Game>() {
            @Override
            public void onSuccess(@Nullable final Game game) {
                assertNotNull(getView(), "getView");
                getView().updateGame(game);
            }

            @Override
            public void onError(@NonNull final Throwable error) {
                assertNotNull(getView(), "getView");
                getView().error(error.getMessage());
            }
        }, gameId).run();
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

    public void updateMoves(@NonNull final String gameId) {
        assertNotNull(gameId, "gameId");

        new GetMovesAsync(new Async.AsyncListener<List<Move>>() {
            @Override
            public void onSuccess(@NonNull final  List<Move> movesList) {
                assertNotNull(getView(), "getView");
                getView().updateMoves(movesList);
            }

            @Override
            public void onError(@NonNull final Throwable error) {
                assertNotNull(getView(), "getView");
                getView().error(error.getMessage());
            }
        }, gameId).run();
    }

    private class GetGameAsync extends Async<Game> {

        @NonNull
        private final String mGameId;

        /*package*/ GetGameAsync(@NonNull final AsyncListener<Game> listener,
                                 @NonNull final String gameId) {
            super(listener);
            this.mGameId = gameId;
        }

        @Override
        public Game call() throws Throwable {
            assertNotNull(getView(), "getView");
            return getView().getAppData().getGameRepository().getGameByGameId(mGameId);
        }
    }

    private class GetMovesAsync extends Async<List<Move>> {

        @NonNull
        private final String mGameId;

        /*package*/ GetMovesAsync(@NonNull final AsyncListener<List<Move>> listener,
                                  @NonNull final String gameId) {
            super(listener);
            this.mGameId = gameId;
        }

        @Override
        public List<Move> call() throws Throwable {
            assertNotNull(getView(), "getView");
            return getView().getAppData().getGameRepository().getAllMovesByGameId(mGameId);
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
