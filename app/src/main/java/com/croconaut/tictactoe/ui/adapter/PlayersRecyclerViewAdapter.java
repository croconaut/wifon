package com.croconaut.tictactoe.ui.adapter;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.croconaut.ratemebuddy.R;
import com.croconaut.ratemebuddy.ui.views.transformation.CircleTransform;
import com.croconaut.ratemebuddy.utils.ProfileUtils;
import com.croconaut.tictactoe.ui.adapter.model.ProfileGameWrapper;
import com.croconaut.tictactoe.ui.listeners.IRecyclerViewOnClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.croconaut.tictactoe.utils.Assertions.assertNotNull;
import static com.croconaut.tictactoe.utils.StateUtils.ExpectedState.DRAW;
import static com.croconaut.tictactoe.utils.StateUtils.ExpectedState.LOST;
import static com.croconaut.tictactoe.utils.StateUtils.ExpectedState.WON;
import static com.croconaut.tictactoe.utils.StateUtils.getGamesWithState;
import static com.croconaut.tictactoe.utils.StringUtils.getGameInProgressString;

public final class PlayersRecyclerViewAdapter
        extends RecyclerView.Adapter<PlayersRecyclerViewAdapter.ViewHolder>
        implements IRecyclerViewOnClickListener {

    @NonNull
    private final Context mContext;

    @NonNull
    private final OnPlayerClickedListener mListener;

    @NonNull
    private List<ProfileGameWrapper> mPlayerList;

    public PlayersRecyclerViewAdapter(@NonNull final Context context,
                                      @NonNull final OnPlayerClickedListener listener,
                                      @NonNull final List<ProfileGameWrapper> playerList) {
        assertNotNull(context, "context");
        assertNotNull(listener, "listener");
        assertNotNull(playerList, "playerList");

        this.mContext = context;
        this.mPlayerList = playerList;
        this.mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final Context context = parent.getContext();

        final View view = LayoutInflater.from(context)
                .inflate(R.layout.list_item_tictactoe_people, parent, false);

        return new ViewHolder(view, this);
    }

    @Override
    public void onClickView(final int position) {
        mListener.onPlayerClicked(mPlayerList.get(position));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setup(mContext, mPlayerList.get(position));
    }

    @Override
    public int getItemCount() {
        return mPlayerList.size();
    }

    public void updatePlayers(@NonNull final Set<ProfileGameWrapper> playersList) {
        mPlayerList = new ArrayList<>(playersList);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @NonNull
        private final IRecyclerViewOnClickListener mListener;

        @BindView(R.id.ivPlayerPhoto)
        ImageView mPlayerPhoto;

        @BindView(R.id.tvPlayerName)
        TextView mPlayerName;

        @BindView(R.id.tvGameState)
        TextView mGameState;

        @BindView(R.id.tvTotalGames)
        TextView mTotalGames;

        @BindView(R.id.tvGameRatio)
        TextView mGameRatio;

        /*package*/ void setup(@NonNull final Context context,
                               @NonNull final ProfileGameWrapper wrapper) {
            mPlayerName.setText(wrapper.getProfile().getName());
            mTotalGames.setText(
                    context.getString(R.string.tictactoe_invite_players_item_games_total,
                            wrapper.getGameList().size()));
            mGameRatio.setText(Html.fromHtml(
                    context.getString(R.string.tictactoe_invite_players_item_games_ratio,
                            getGamesWithState(WON, wrapper.getGameList()).size(),
                            getGamesWithState(DRAW, wrapper.getGameList()).size(),
                            getGamesWithState(LOST, wrapper.getGameList()).size()
                    ))
            );
            mGameState.setText(getGameInProgressString(context, wrapper.getGameInProgress()));

            Glide.with(context)
                    .load(wrapper.getProfile().getThumbUri())
                    .asBitmap()
                    .signature(new StringSignature(
                            String.valueOf(wrapper.getProfile().getTimeStamp())))
                    .error(ProfileUtils.getTextDrawableForProfile(wrapper.getProfile()))
                    .thumbnail(0.2f)
                    .transform(new CircleTransform(context))
                    .into(mPlayerPhoto);
        }

        @Override
        public void onClick(@NonNull final View v) {
            mListener.onClickView(getAdapterPosition());
        }

        public ViewHolder(@NonNull final View view,
                          @NonNull final IRecyclerViewOnClickListener listener) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
            this.mListener = listener;
        }
    }

    public interface OnPlayerClickedListener {
        void onPlayerClicked(@NonNull final ProfileGameWrapper profileGameWrapper);
    }
}
