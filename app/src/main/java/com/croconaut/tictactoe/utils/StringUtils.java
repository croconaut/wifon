package com.croconaut.tictactoe.utils;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.croconaut.ratemebuddy.R;
import com.croconaut.tictactoe.model.board.GameSeed;
import com.croconaut.tictactoe.payload.games.Game;
import com.croconaut.tictactoe.payload.games.GameState;

import static com.croconaut.tictactoe.utils.Assertions.assertNotNull;

public final class StringUtils {

    @NonNull
    public static String getSeedString(@GameSeed final int seed) {
        switch (seed) {
            case GameSeed.BLANK:
                return "-";
            case GameSeed.CROSS:
                return "X";
            case GameSeed.NOUGHT:
                return "O";
            default:
                throw new AssertionError("Unknown seed type");
        }
    }

    @NonNull
    public static String getGameInProgressString(@NonNull final Context context,
                                                 @Nullable final Game game) {
        assertNotNull(context, "context");

        if (null == game) {
            return context.getString(R.string.tictactoe_invite_players_game_state_not_in_progress);
        }

        @GameSeed final int myGameSeed = game.getGameSeed();
        switch (game.getGameState()) {
            case GameState.PENDING_WAITING_FOR_INVITE_RESPONSE:
                return context
                        .getString(R.string.tictactoe_invite_players_game_state_in_progress_pending_response);
            case GameState.PENDING_WAITING_FOR_INVITE_REQUEST:
                return context
                        .getString(R.string.tictactoe_invite_players_game_state_in_progress_pending_request);
            case GameState.PLAYING_NEXT_MOVE_CROSS:
                return myGameSeed == GameSeed.CROSS
                        ? context.getString(R.string.tictactoe_invite_players_game_state_in_progress_your_move)
                        : context.getString(R.string.tictactoe_invite_players_game_state_in_progress_opponent_move);
            case GameState.PLAYING_NEXT_MOVE_NOUGHT:
                return myGameSeed == GameSeed.NOUGHT
                        ? context.getString(R.string.tictactoe_invite_players_game_state_in_progress_your_move)
                        : context.getString(R.string.tictactoe_invite_players_game_state_in_progress_opponent_move);
            case GameState.DRAW:
                return context.getString(R.string.tictactoe_invite_players_game_state_draw_text);
            case GameState.SURRENDER:
                return context.getString(R.string.tictactoe_invite_players_game_state_game_end_lost);
            case GameState.WIN_CROSS:
                return myGameSeed == GameSeed.NOUGHT
                        ? context.getString(R.string.tictactoe_invite_players_game_state_game_end_lost)
                        : context.getString(R.string.tictactoe_invite_players_game_state_game_end_won);
            case GameState.WIN_NOUGHT:
                return myGameSeed == GameSeed.NOUGHT
                        ? context.getString(R.string.tictactoe_invite_players_game_state_game_end_won)
                        : context.getString(R.string.tictactoe_invite_players_game_state_game_end_lost);
        }

        return "";
    }

    /**
     * Private constructor prevents instantiation.
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private StringUtils() {
        throw new UnsupportedOperationException("This class is non-instantiable"); //$NON-NLS-1$
    }
}
