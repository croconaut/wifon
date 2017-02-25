package com.croconaut.tictactoe.ui.adapter;


import android.content.Context;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.croconaut.ratemebuddy.R;
import com.croconaut.tictactoe.model.board.Board;
import com.croconaut.tictactoe.model.board.Cell;
import com.croconaut.tictactoe.model.board.GameSeed;
import com.croconaut.tictactoe.payload.games.Game;
import com.croconaut.tictactoe.payload.games.GameState;
import com.croconaut.tictactoe.payload.moves.Move;
import com.croconaut.tictactoe.ui.listeners.IRecyclerViewOnClickListener;
import com.croconaut.tictactoe.utils.StateUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.croconaut.ratemebuddy.R.id.tvItemGameGrid;
import static com.croconaut.tictactoe.utils.Assertions.assertNotNull;

public final class GameGridViewAdapter
        extends RecyclerView.Adapter<GameGridViewAdapter.ViewHolder>
        implements IRecyclerViewOnClickListener, Board.TicTacToeBoardListener {

    @NonNull
    private static final String TAG = GameGridViewAdapter.class.getName();

    @NonNull
    private List<Cell> mCellList;

    @NonNull
    private Board mBoard;

    @NonNull
    private final OnGameGridItemClickListener mListener;

    @NonNull
    private final Game mGame;

    public GameGridViewAdapter(@NonNull final Game game, @NonNull final List<Move> moveList,
                               @NonNull final OnGameGridItemClickListener onGameItemClickListener) {
        assertNotNull(game, "game");
        assertNotNull(moveList, "moveList");
        assertNotNull(onGameItemClickListener, "onGameItemClickListener");

        this.mGame = game;
        this.mBoard = new Board(this, game.getGameSize(), moveList);
        this.mListener = onGameItemClickListener;
        this.mCellList = mBoard.getCellList();
    }

    @Override
    public GameGridViewAdapter.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent,
                                                             final int viewType) {
        final Context context = parent.getContext();

        final View view = LayoutInflater.from(context)
                .inflate(R.layout.list_item_tictactoe_game_grid, parent, false);

        return new GameGridViewAdapter.ViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull final GameGridViewAdapter.ViewHolder holder,
                                 final int position) {
        holder.setup(mCellList.get(position));
    }

    @Override
    public int getItemCount() {
        return mCellList.size();
    }

    @Override
    public void onClickView(final int position) {
        if (StateUtils.canPerformMove(mGame.getGameState(), mGame.getGameSeed())) {
            final Cell cell = mCellList.get(position);
            mBoard.move(mGame.getGameId(), mGame.getGameSeed(), cell.getXPos(), cell.getYPos());
        } else {
            Log.e(TAG, "Cannot perform move");
        }
    }

    @Override
    public void onGameStateChanged(final boolean movePerformed, @NonNull final Move move,
                                   @GameState final int gameState) {
        mListener.onGameItemClicked(movePerformed, move, gameState);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @NonNull
        private final IRecyclerViewOnClickListener mListener;

        @BindView(tvItemGameGrid)
        ImageView ivGridItem;

        /*package*/ void setup(@NonNull final Cell cell) {
            switch (cell.getSeed()) {
                case GameSeed.BLANK:
                    ivGridItem.setImageResource(R.drawable.vd_empty);
                    ivGridItem.setColorFilter(
                            ivGridItem.getContext().getResources().getColor(R.color.material_black),
                            PorterDuff.Mode.SRC_IN);
                    break;
                case GameSeed.CROSS:
                    ivGridItem.setImageResource(R.drawable.vd_cross);
                    ivGridItem.setColorFilter(
                            ivGridItem.getContext().getResources().getColor(R.color.red_themed_dark),
                            PorterDuff.Mode.SRC_IN);
                    break;
                case GameSeed.NOUGHT:
                    ivGridItem.setImageResource(R.drawable.vd_nought);
                    ivGridItem.setColorFilter(
                            ivGridItem.getContext().getResources().getColor(R.color.blue_themed_dark),
                            PorterDuff.Mode.SRC_IN);
                    break;
            }
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

    public interface OnGameGridItemClickListener {
        void onGameItemClicked(final boolean movePerformed, @NonNull final Move move,
                               final @GameState int gameState);
    }
}
