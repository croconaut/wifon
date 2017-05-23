package com.croconaut.tictactoe.ui.activities;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.croconaut.cpt.data.Communication;
import com.croconaut.cpt.data.IncomingMessage;
import com.croconaut.ratemebuddy.R;
import com.croconaut.tictactoe.model.board.GameSeed;
import com.croconaut.tictactoe.payload.TicTacToeGame;
import com.croconaut.tictactoe.payload.games.Game;
import com.croconaut.tictactoe.payload.games.GameSize;
import com.croconaut.tictactoe.ui.adapter.model.ProfileGameWrapper;
import com.croconaut.tictactoe.ui.fragments.MenuFragment;
import com.croconaut.tictactoe.ui.fragments.PlayersFragment;
import com.croconaut.tictactoe.utils.StateUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import static com.croconaut.tictactoe.utils.Assertions.assertIsSeed;
import static com.croconaut.tictactoe.utils.Assertions.assertNotNull;
import static com.croconaut.tictactoe.utils.StateUtils.PENDING_STATES;

public final class MenuActivity extends BaseActivity
        implements MenuFragment.OnGameConfirmClickListener, PlayersFragment.OnInviteGamePlayerListener {

    @NonNull
    private static final String TAG = MenuActivity.class.getName();

    @NonNull
    @CheckResult
    public static Intent newStartIntent(@NonNull final Context context) {
        assertNotNull(context, "context");//$NON-NLS

        return new Intent(context, MenuActivity.class);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeHeaderWithDrawer(getString(R.string.tictactoe_invite_players_toolbar_text), true);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.fragment_container,
                            PlayersFragment.newInstance(),
                            PlayersFragment.TAG)
                    .commit();
        }
    }

    @Override
    int getLayoutResId() {
        return R.layout.activity_tictactoe_shared_container;
    }

    @Override
    public void onConfirmGameClick(@NonNull final String playerId, @GameSeed final int gameSeed,
                                   @GameSize final int gameSize) {
        assertNotNull(playerId, "playerId");
        assertIsSeed(gameSeed, "gameSeed");

        final List<Game> playerGames = appData.getGameRepository().getGamesByPlayerId(playerId);
        final Game gameInPendingState = StateUtils.isGameInState(PENDING_STATES, playerGames);

        if (gameInPendingState == null) {
            final Game game =
                    appData.getGameCommunication().inviteToGame(playerId, gameSeed, gameSize);
            assertNotNull(game, "game");

            startActivity(GameActivity.newStartIntent(getApplicationContext(), game));
        } else {
            Toast.makeText(
                    getApplicationContext(),
                    getString(R.string.tictactoe_toast_invite_refused_game_already_pending),
                    Toast.LENGTH_LONG
            ).show();
        }
        finish();
    }

    @Override
    public void onConfirmNewGameClick(@NonNull final String playerId) {
        assertNotNull(playerId, "playerId");

        getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragment_container,
                        MenuFragment.newInstance(playerId),
                        MenuFragment.TAG)
                .addToBackStack(MenuFragment.TAG)
                .commit();
    }

    @Override
    public void onConfirmExistingGameClick(@NonNull final ProfileGameWrapper profileGameWrapper) {
        assertNotNull(profileGameWrapper, "profileGameWrapper");

        final Game game = profileGameWrapper.getGameInProgress();
        assertNotNull(game, "game"); //never happens

        startActivity(GameActivity.newStartIntent(getApplicationContext(), game));
        finish();
    }

    @Override
    public boolean process(Intent cptIntent) throws IOException, ClassNotFoundException {
        super.process(cptIntent);

        switch (cptIntent.getAction()) {
            case Communication.ACTION_MESSAGE_ARRIVED:
                final IncomingMessage message
                        = cptIntent.getParcelableExtra(Communication.EXTRA_MESSAGE_ARRIVED);
                Serializable data = message.getPayload().getAppData();

                if (data instanceof TicTacToeGame) {
                    final PlayersFragment fragment = (PlayersFragment) getSupportFragmentManager()
                            .findFragmentByTag(PlayersFragment.TAG);

                    if (fragment != null) {
                        fragment.getViewModel().updatePlayers();
                    }
                }

                break;
            case Communication.ACTION_NEARBY_ARRIVED:
                final PlayersFragment playersFragment = (PlayersFragment) getSupportFragmentManager()
                        .findFragmentByTag(PlayersFragment.TAG);

                if (playersFragment != null) {
                    playersFragment.getViewModel().updatePlayers();
                }
        }

        return false;
    }
}
