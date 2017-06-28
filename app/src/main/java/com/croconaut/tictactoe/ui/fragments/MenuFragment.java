package com.croconaut.tictactoe.ui.fragments;


import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.croconaut.ratemebuddy.AppData;
import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.activities.ToolbarDrawerActivity;
import com.croconaut.ratemebuddy.utils.ThemeManager;
import com.croconaut.ratemebuddy.utils.pojo.profiles.Profile;
import com.croconaut.tictactoe.model.board.GameSeed;
import com.croconaut.tictactoe.payload.games.GameSize;
import com.croconaut.tictactoe.ui.notifications.BaseNotificationManager;
import com.croconaut.tictactoe.ui.viewmodel.models.GameOptionsViewModel;
import com.croconaut.tictactoe.ui.viewmodel.views.IErrorView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import eu.inloop.viewmodel.base.ViewModelBaseFragment;

import static com.croconaut.tictactoe.payload.games.GameSize.BIG;
import static com.croconaut.tictactoe.payload.games.GameSize.HUGE;
import static com.croconaut.tictactoe.payload.games.GameSize.NORMAL;
import static com.croconaut.tictactoe.utils.Assertions.assertNotNull;

public final class MenuFragment extends ViewModelBaseFragment<IErrorView, GameOptionsViewModel>
        implements IErrorView {

    @NonNull
    public static final String TAG = MenuFragment.class.getName();

    @NonNull
    private static final String ARG_EXTRA_PLAYER_ID = "arg_extra_player_id";

    @Nullable
    private String mPlayerId;

    @Nullable
    private OnGameConfirmClickListener mCallback;

    @GameSize
    private int mGameSize = -1;

    @GameSeed
    private int mGameSeed = -1;

    @BindView(R.id.tvSizeNormal)
    TextView mTvSizeNormal;

    @BindView(R.id.tvSizeBig)
    TextView mTvSizeBig;

    @BindView(R.id.tvSizeHuge)
    TextView mTvSizeHuge;

    @BindView(R.id.ivNought)
    ImageView mIvNought;

    @BindView(R.id.ivCross)
    ImageView mIvCross;

    public MenuFragment() {
    }

    @CheckResult
    public static MenuFragment newInstance(@NonNull final String playerId) {
        assertNotNull(playerId, "playerId");

        final MenuFragment fragment = new MenuFragment();
        final Bundle args = new Bundle();
        args.putString(ARG_EXTRA_PLAYER_ID, playerId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPlayerId = getArguments().getString(ARG_EXTRA_PLAYER_ID);
    }

    @Override
    public void onAttach(@NonNull final Context context) {
        super.onAttach(context);

        try {
            mCallback = (OnGameConfirmClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement " + OnGameConfirmClickListener.class.getName());
        }

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setModelView(this);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_tictactoe_menu, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @OnClick({R.id.tvSizeNormal, R.id.tvSizeBig, R.id.tvSizeHuge})
    public void onSizeClick(@NonNull final View view) {
        switch (view.getId()) {
            case R.id.tvSizeNormal:
                mGameSize = NORMAL;
                break;
            case R.id.tvSizeBig:
                mGameSize = BIG;
                break;
            case R.id.tvSizeHuge:
                mGameSize = HUGE;
                break;
        }


        final ThemeManager themeManager = new ThemeManager(getContext());
        mTvSizeBig.setTextColor(mGameSize == BIG
                ? themeManager.getCurrentColorHexa()
                : getResources().getColor(R.color.material_black));
        mTvSizeHuge.setTextColor(mGameSize == HUGE
                ? themeManager.getCurrentColorHexa()
                : getResources().getColor(R.color.material_black));
        mTvSizeNormal.setTextColor(mGameSize == NORMAL
                ? themeManager.getCurrentColorHexa()
                : getResources().getColor(R.color.material_black));

        mTvSizeBig.setTextSize(mGameSize == BIG ? 27 : 25);
        mTvSizeHuge.setTextSize(mGameSize == HUGE ? 27 : 25);
        mTvSizeNormal.setTextSize(mGameSize == NORMAL ? 27 : 25);
    }

    @OnClick({R.id.ivNought, R.id.ivCross})
    public void onGameSeedClick(@NonNull final View view) {
        switch (view.getId()) {
            case R.id.ivNought:
                mGameSeed = GameSeed.NOUGHT;
                break;
            case R.id.ivCross:
                mGameSeed = GameSeed.CROSS;
                break;
        }

        if (mGameSeed == GameSeed.CROSS) {
            mIvCross.setColorFilter(
                    getResources().getColor(R.color.red_themed_dark), PorterDuff.Mode.SRC_IN);
            mIvNought.setColorFilter(null);
        } else {
            mIvCross.setColorFilter(null);
            mIvNought.setColorFilter(
                    getResources().getColor(R.color.blue_themed_dark), PorterDuff.Mode.SRC_IN);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getViewModel().retrievePlayer(mPlayerId);

        BaseNotificationManager.cancelNotification(getContext(), mPlayerId.hashCode());
    }

    @OnClick(R.id.btnInvite)
    public void onConfirmGameClick() {
        assertNotNull(mCallback, "mCallback");
        assertNotNull(mPlayerId, "mPlayerId");

        //noinspection WrongConstant
        if (mGameSeed == -1 || mGameSize == -1) {
            Toast.makeText(getContext(), getResources()
                            .getString(R.string.tictactoe_toast_create_game_empty_size_or_seed),
                    Toast.LENGTH_LONG).show();
            return;
        }

        mCallback.onConfirmGameClick(mPlayerId, mGameSeed, mGameSize);
    }

    @Override
    public void error(@NonNull String errorMsg) {
        Log.e(TAG, errorMsg);
    }

    @Override
    public AppData getAppData() {
        return ((AppData) getActivity().getApplication());
    }

    @Override
    public void updateProfile(@Nullable final Profile profile) {
        if (null != profile) {
            ((ToolbarDrawerActivity) getActivity())
                    .setToolbarTitle(getResources()
                            .getString(R.string.tictactoe_invite_players_menu_toolbar_text, profile.getName()));
        }
    }

    @Nullable
    @Override
    public Class<GameOptionsViewModel> getViewModelClass() {
        return GameOptionsViewModel.class;
    }

    public interface OnGameConfirmClickListener {
        void onConfirmGameClick(@NonNull final String playerId, @GameSeed final int gameSeed,
                                @GameSize final int gameSize);
    }

}
