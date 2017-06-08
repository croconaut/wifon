package com.croconaut.tictactoe.ui.viewmodel.views;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.croconaut.tictactoe.payload.games.Game;
import com.croconaut.tictactoe.payload.moves.Move;

import java.util.List;

public interface IGameView extends IErrorView {
    void updateGame(@Nullable final Game game);

    void updateMoves(@NonNull final List<Move> moveList);
}
