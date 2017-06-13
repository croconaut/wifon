package com.croconaut.tictactoe.ui.activities;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;

import com.croconaut.cpt.data.Communication;
import com.croconaut.cpt.data.IncomingMessage;
import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.activities.CommunicationActivity;
import com.croconaut.ratemebuddy.utils.pojo.profiles.Profile;
import com.croconaut.tictactoe.model.board.GameSeed;
import com.croconaut.tictactoe.payload.TicTacToeGame;
import com.croconaut.tictactoe.payload.games.Game;
import com.croconaut.tictactoe.payload.games.GameState;
import com.croconaut.tictactoe.payload.invites.InviteRequest;
import com.croconaut.tictactoe.ui.fragments.GameFragment;
import com.croconaut.tictactoe.ui.notifications.BaseNotificationManager;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import static com.croconaut.ratemebuddy.activities.CommunicationActivity.EXTRA_TARGET_CROCO_ID;
import static com.croconaut.tictactoe.utils.Assertions.assertNotNull;
import static com.croconaut.tictactoe.utils.SeedUtils.getOpposite;
import static com.croconaut.tictactoe.utils.StringUtils.getSeedString;

public final class GameActivity extends BaseActivity
        implements GameFragment.GameListener {

    @NonNull
    private static final String ARG_EXTRA_GAME = "arg_extra_game";

    @NonNull
    private static final String ARG_EXTRA_GAME_IS_PENDING = "arg_extra_game_is_pending";

    @Nullable
    private Game mGame = null;

    @Nullable
    private AlertDialog mAlertDialog = null;

    @NonNull
    @CheckResult
    public static Intent newStartIntent(@NonNull final Context context,
                                        @NonNull final Game game) {
        assertNotNull(context, "context");//$NON-NLS
        assertNotNull(game, "game");//$NON-NLS

        final Intent intent = new Intent(context, GameActivity.class);
        intent.putExtra(GameActivity.ARG_EXTRA_GAME, game);

        return intent;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeHeaderWithDrawer(getString(R.string.action_game_tic_tac_toe), true);

        mGame = ((Game) getIntent().getExtras().getSerializable(ARG_EXTRA_GAME));
        assertNotNull(mGame, "game");

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.fragment_container,
                            GameFragment.newInstance(mGame),
                            GameFragment.TAG)
                    .commit();
        }

        checkIfGameHasInvite();
    }

    private void checkIfGameHasInvite() {
        assertNotNull(mGame, "game");

        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
        }

        final List<Game> gameForInvite = appData.getGameRepository().getAllGamesWithState(
                mGame.getRemoteProfileId(), GameState.PENDING_WAITING_FOR_INVITE_REQUEST);
        if (!gameForInvite.isEmpty()) {
            final Game pendingGame = gameForInvite.get(0);
            final InviteRequest inviteRequest = new InviteRequest(pendingGame);
            final Profile profile = appData.getProfileDataSource()
                    .getProfileByCrocoId(pendingGame.getRemoteProfileId());
            final Resources res = getResources();

            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(
                    res.getString(R.string.tictactoe_game_dialog_invite_message_title));
            builder.setCancelable(false);

            builder.setMessage(
                    res.getString(R.string.tictactoe_game_dialog_invite_message_text,
                            profile.getName(), inviteRequest.getGameSize(),
                            inviteRequest.getGameSize(), getSeedString(inviteRequest.getSeed())));
            builder.setPositiveButton(
                    res.getString(R.string.tictactoe_game_dialog_btn_positive_text),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(@NonNull final DialogInterface dialog,
                                            final int btnId) {
                            dialog.dismiss();

                            final Game acceptedGame = appData.getGameCommunication()
                                    .acceptInvite(pendingGame.getRemoteProfileId(),
                                            pendingGame.getGameId(), getOpposite(pendingGame.getGameSeed()));
                            assertNotNull(acceptedGame, "acceptedGame");

                            BaseNotificationManager.cancelNotification(getApplicationContext(),
                                    acceptedGame.getRemoteProfileId().hashCode());

                            startActivity(
                                    GameActivity.newStartIntent(getApplicationContext(), acceptedGame));
                            finish();
                        }
                    });

            builder.setNegativeButton(
                    res.getString(R.string.tictactoe_game_dialog_btn_negative_text),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(@NonNull final DialogInterface dialog,
                                            final int btnId) {

                            appData.getGameCommunication()
                                    .declineInvite(pendingGame.getRemoteProfileId(),
                                            inviteRequest.getGameId());

                            BaseNotificationManager.cancelNotification(getApplicationContext(),
                                    pendingGame.getRemoteProfileId().hashCode());

                            dialog.dismiss();
                            finish();
                        }
                    });

            mAlertDialog = builder.create();
            mAlertDialog.show();
        }
    }

    @Override
    public void onSurrenderClicked(@NonNull final String gameId) {
        appData.getGameCommunication().sendSurrender(gameId);

        startActivity(MenuActivity.newStartIntent(getApplicationContext()));
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(MenuActivity.newStartIntent(getApplicationContext()));
        finish();
    }

    @Override
    public void onGameEnded(@NonNull final Game game) {
        assertNotNull(game, "game");

        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
        }

        final Resources res = getResources();

        final String gameEndStateString;
        if (game.getGameState() == GameState.DRAW) {
            gameEndStateString = res.getString(R.string.tictactoe_invite_players_game_state_draw_text);
        } else if ((game.getGameState() == GameState.WIN_NOUGHT && game.getGameSeed() == GameSeed.NOUGHT)
                || (game.getGameState() == GameState.WIN_CROSS && game.getGameSeed() == GameSeed.CROSS)) {
            gameEndStateString = res.getString(R.string.tictactoe_invite_players_game_state_won_text);
        } else {
            gameEndStateString = res.getString(R.string.tictactoe_invite_players_game_state_lost_text);
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(gameEndStateString);
        builder.setCancelable(false);

        final List<Game> gameForInvite = appData.getGameRepository().getAllGamesWithState(
                game.getRemoteProfileId(), GameState.PENDING_WAITING_FOR_INVITE_REQUEST);
        if (!gameForInvite.isEmpty()) {
            final InviteRequest inviteRequest = new InviteRequest(gameForInvite.get(0));
            final Profile profile = appData.getProfileDataSource()
                    .getProfileByCrocoId(gameForInvite.get(0).getRemoteProfileId());

            builder.setMessage(
                    res.getString(R.string.tictactoe_game_dialog_invite_message_text,
                            profile.getName(), inviteRequest.getGameSize(),
                            inviteRequest.getGameSize(), getSeedString(inviteRequest.getSeed())));
            builder.setPositiveButton(
                    res.getString(R.string.tictactoe_game_dialog_btn_positive_text),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(@NonNull final DialogInterface dialog,
                                            final int btnId) {
                            dialog.dismiss();

                            final Game acceptedGame = appData.getGameCommunication()
                                    .acceptInvite(game.getRemoteProfileId(),
                                            inviteRequest.getGameId(), getOpposite(inviteRequest.getSeed()));
                            assertNotNull(acceptedGame, "acceptedGame");

                            BaseNotificationManager.cancelNotification(getApplicationContext(),
                                    acceptedGame.getRemoteProfileId().hashCode());

                            startActivity(
                                    GameActivity.newStartIntent(getApplicationContext(), acceptedGame));
                            finish();
                        }
                    });

            builder.setNegativeButton(
                    res.getString(R.string.tictactoe_game_dialog_btn_negative_text),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(@NonNull final DialogInterface dialog,
                                            final int btnId) {
                            appData.getGameCommunication()
                                    .declineInvite(gameForInvite.get(0).getRemoteProfileId(),
                                            inviteRequest.getGameId());

                            BaseNotificationManager.cancelNotification(getApplicationContext(),
                                    gameForInvite.get(0).getRemoteProfileId().hashCode());

                            dialog.dismiss();
                            finish();
                        }
                    });

        } else {
            builder.setMessage(
                    res.getString(R.string.tictactoe_game_dialog_repeat_game_message_text));
            builder.setPositiveButton(
                    res.getString(R.string.tictactoe_game_dialog_btn_positive_text),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(@NonNull final DialogInterface dialog,
                                            final int btnId) {
                            dialog.dismiss();
                            final Game newGame =
                                    appData.getGameCommunication().inviteToGame(
                                            game.getRemoteProfileId(),
                                            game.getGameSeed(),
                                            game.getGameSize());
                            assertNotNull(newGame, "newGame");

                            startActivity(GameActivity.newStartIntent(getApplicationContext(), newGame));
                            finish();
                        }
                    });

            builder.setNegativeButton(
                    res.getString(R.string.tictactoe_game_dialog_btn_negative_text),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(@NonNull final DialogInterface dialog,
                                            final int btnId) {
                            dialog.dismiss();
                            finish();
                        }
                    });
        }

        mAlertDialog = builder.create();
        mAlertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull final Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(@NonNull final Menu menu) {
        super.onPrepareOptionsMenu(menu);
        final MenuItem item = menu.findItem(R.id.menuWriteToRemote);
        item.setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuWriteToRemote:
                final Intent communicationIntent = new Intent(this, CommunicationActivity.class);
                communicationIntent.putExtra(EXTRA_TARGET_CROCO_ID, mGame.getRemoteProfileId());
                startActivity(communicationIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    int getLayoutResId() {
        return R.layout.activity_tictactoe_shared_container;
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
                    final TicTacToeGame ticTacToeGame = ((TicTacToeGame) data);

                    if (mGame != null && ticTacToeGame.getGameId().equals(mGame.getGameId())) {
                        GameFragment gameFragment = (GameFragment) getSupportFragmentManager()
                                .findFragmentByTag(GameFragment.TAG);

                        if (gameFragment != null) {
                            gameFragment.refreshGame();
                            return true;
                        }
                    }
                }
        }

        return false;
    }
}
