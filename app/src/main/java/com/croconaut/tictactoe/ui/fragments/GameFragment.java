package com.croconaut.tictactoe.ui.fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.croconaut.ratemebuddy.AppData;
import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.activities.ToolbarDrawerActivity;
import com.croconaut.ratemebuddy.utils.pojo.profiles.Profile;
import com.croconaut.tictactoe.payload.games.Game;
import com.croconaut.tictactoe.payload.games.GameState;
import com.croconaut.tictactoe.payload.moves.Move;
import com.croconaut.tictactoe.ui.adapter.GameGridViewAdapter;
import com.croconaut.tictactoe.ui.notifications.BaseNotificationManager;
import com.croconaut.tictactoe.ui.viewmodel.models.GameViewModel;
import com.croconaut.tictactoe.ui.viewmodel.views.IGameView;
import com.croconaut.tictactoe.utils.GridDividerDecoration;
import com.croconaut.tictactoe.utils.StateUtils;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import eu.inloop.viewmodel.base.ViewModelBaseFragment;

import static com.croconaut.tictactoe.utils.Assertions.assertNotNull;
import static com.croconaut.tictactoe.utils.StringUtils.getGameInProgressString;
import static com.croconaut.tictactoe.utils.StringUtils.getSeedString;

public final class GameFragment extends ViewModelBaseFragment<IGameView, GameViewModel>
        implements IGameView, GameGridViewAdapter.OnGameGridItemClickListener {

    @NonNull
    private static final String ARG_EXTRA_GAME = "arg_extra_game";

    @NonNull
    public static final String TAG = GameFragment.class.getName();

    @BindView(R.id.horizontal_indeterminate_progress_bar)
    ProgressBar mIndeterminateProgressBar;

    @BindView(R.id.tvGameState)
    TextView mTvGameState;

    @BindView(R.id.tvMySeed)
    TextView mTvGameMySeed;

    @BindView(R.id.rvGameGrid)
    RecyclerView mRvGameGrid;

    @Nullable
    private GameGridViewAdapter mAdapter;

    @Nullable
    private GameListener mCallback;

    private Game mGame;

    private long mLastMoveCreationDate = 0L;

    public GameFragment() {
    }

    @CheckResult
    public static GameFragment newInstance(@NonNull final Game game) {
        assertNotNull(game, "game"); //$NON-NLS

        final GameFragment fragment = new GameFragment();
        final Bundle args = new Bundle();
        args.putSerializable(ARG_EXTRA_GAME, game);
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_tictactoe_game, container, false);
        ButterKnife.bind(this, view);

        final RecyclerView.LayoutManager layoutManager
                = new GridLayoutManager(getContext(), mGame.getGameSize());
        mRvGameGrid.setLayoutManager(layoutManager);
        mRvGameGrid.addItemDecoration(new GridDividerDecoration(getContext()));

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setModelView(this);
        refreshGame();

        mIndeterminateProgressBar.getIndeterminateDrawable().setColorFilter(
                getContext().getResources().getColor(R.color.white_background),
                PorterDuff.Mode.SRC_IN);
    }


    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGame = ((Game) getArguments().getSerializable(ARG_EXTRA_GAME));
        assertNotNull(mGame, "mGame");

        mAdapter = new GameGridViewAdapter(mGame, Collections.<Move>emptyList(), this);

    }

    @Override
    public void onAttach(@NonNull final Context context) {
        super.onAttach(context);

        try {
            mCallback = (GameListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement " + GameFragment.GameListener.class.getName());
        }

    }

    @OnClick(R.id.btnSurrender)
    void onBtnSurrenderClick() {
        assertNotNull(mCallback, "mCallback");

        displaySurrenderDialog();
    }

    public void refreshGame() {
        getViewModel().initGame(mGame.getGameId());
        getViewModel().retrievePlayer(mGame.getRemoteProfileId());
    }

    @Override
    public void error(@NonNull final String errorMsg) {
        Log.e(TAG, errorMsg);
    }

    @Override
    public void updateGame(@Nullable final Game game) {
        if (null == game) {
            getActivity().finish();
            return;
        }

        mTvGameState.setText(getGameInProgressString(getContext(), game));

        if (StateUtils.isPlayerWaiting(game)) {
            mIndeterminateProgressBar.setVisibility(View.VISIBLE);
            mTvGameMySeed.setVisibility(View.GONE);
        } else {
            mIndeterminateProgressBar.setVisibility(View.GONE);

            mTvGameMySeed.setVisibility(View.VISIBLE);
            mTvGameMySeed.setText(getResources()
                    .getString(R.string.tictactoe_game_player_seed, getSeedString(game.getGameSeed()))
            );
        }

        if (StateUtils.hasGameEnded(game.getGameState())) {
            if (mLastMoveCreationDate != 0) {

                Log.e(TAG, "INVITE LOCK - INSERTING INVITE LOCK: " + mLastMoveCreationDate);

                getAppData().getGameRepository()
                        .insertInviteLock(mGame.getRemoteProfileId(), mLastMoveCreationDate);

                mLastMoveCreationDate = 0L;
            }

            mCallback.onGameEnded(game);
            return;
        } else {

            mLastMoveCreationDate = 0L;
        }


        mGame = game;

        BaseNotificationManager
                .cancelNotification(getContext(), mGame.getRemoteProfileId().hashCode());

        getViewModel().updateMoves(game.getGameId());
    }

    @Override
    public void updateMoves(@NonNull final List<Move> moveList) {
        mAdapter = new GameGridViewAdapter(mGame, moveList, this);
        mRvGameGrid.setAdapter(mAdapter);
    }

    @Override
    public void updateProfile(@Nullable final Profile profile) {
        if (null != profile) {
            BaseNotificationManager.cancelNotification(getContext(), profile.getProfileId().hashCode());

            ((ToolbarDrawerActivity) getActivity())
                    .setToolbarTitle(getResources()
                            .getString(R.string.tictactoe_game_toolbar_text, profile.getName()));
        }
    }

    @Override
    public void onGameItemClicked(final boolean movePerformed, @NonNull final Move move,
                                  @GameState final int gameState) {
        if (movePerformed) {
            mLastMoveCreationDate
                    = getAppData().getGameCommunication().sendMove(mGame.getRemoteProfileId(), move);

            getAppData().getGameRepository().updateGameState(mGame, gameState);
            getViewModel().initGame(mGame.getGameId());
            mTvGameState.setText(getGameInProgressString(getContext(), mGame));
        } else {
            Log.e(TAG, "Clicked but move was not performed");
        }
    }

    private void displaySurrenderDialog() {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setTitle(
                getResources().getString(R.string.tictactoe_game_dialog_surrender_game_title_text));
        alertDialogBuilder.setMessage(
                getResources().getString(R.string.tictactoe_game_dialog_surrender_game_message_text));
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(
                getResources().getString(R.string.tictactoe_game_dialog_surrender_game_btn_positive_text),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull final DialogInterface dialog,
                                        final int btnId) {
                        dialog.dismiss();
                        if (mCallback != null) {
                            mCallback.onSurrenderClicked(mGame.getGameId());
                        }
                    }
                });

        alertDialogBuilder.setNegativeButton(
                getResources().getString(R.string.tictactoe_game_dialog_surrender_game_btn_negative_text),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull final DialogInterface dialog,
                                        final int btnId) {
                        dialog.dismiss();
                    }
                });

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    @Override
    public AppData getAppData() {
        return ((AppData) getActivity().getApplication());
    }


    @Nullable
    @Override
    public Class<GameViewModel> getViewModelClass() {
        return GameViewModel.class;
    }

    public interface GameListener {
        void onSurrenderClicked(@NonNull final String gameId);

        void onGameEnded(@NonNull final Game oldGame);
    }
}
