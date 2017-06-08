package com.croconaut.tictactoe.payload.games;


import android.support.annotation.IntDef;

import com.croconaut.tictactoe.model.board.Board;
import com.croconaut.tictactoe.model.board.GameSeed;

import java.lang.annotation.Retention;

import static com.croconaut.tictactoe.payload.games.GameState.DRAW;
import static com.croconaut.tictactoe.payload.games.GameState.PENDING_WAITING_FOR_INVITE_REQUEST;
import static com.croconaut.tictactoe.payload.games.GameState.PENDING_WAITING_FOR_INVITE_RESPONSE;
import static com.croconaut.tictactoe.payload.games.GameState.PLAYING_NEXT_MOVE_CROSS;
import static com.croconaut.tictactoe.payload.games.GameState.PLAYING_NEXT_MOVE_NOUGHT;
import static com.croconaut.tictactoe.payload.games.GameState.SURRENDER;
import static com.croconaut.tictactoe.payload.games.GameState.WIN_CROSS;
import static com.croconaut.tictactoe.payload.games.GameState.WIN_NOUGHT;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Class that represent game states that occurs in {@link Board} eg. TicTacToe game.
 * <p>
 * Brief explanation of states:
 * {@link #PLAYING_NEXT_MOVE_CROSS}: Represents state that the game is still in the progress and
 * the player playing with {@link GameSeed#CROSS} is supposed to do next move.
 * {@link #PLAYING_NEXT_MOVE_NOUGHT}: Represents state that the game is still in the progress and
 * the player playing with {@link GameSeed#NOUGHT} is supposed to do next move.
 * {@link #DRAW}: Represents state in which the game has ended, but neither player was able to win.
 * {@link #WIN_CROSS}: Represents state in which the player with {@link GameSeed#CROSS} has won the game.
 * {@link #WIN_NOUGHT}: Represents state in which the player with {@link GameSeed#NOUGHT} has won the game.
 * {@link #SURRENDER}: Represents state in which one of the player surrenders.
 */
@Retention(SOURCE)
@IntDef({PLAYING_NEXT_MOVE_CROSS, PLAYING_NEXT_MOVE_NOUGHT, DRAW, WIN_CROSS, WIN_NOUGHT, SURRENDER,
        PENDING_WAITING_FOR_INVITE_RESPONSE, PENDING_WAITING_FOR_INVITE_REQUEST})
public @interface GameState {
    int PLAYING_NEXT_MOVE_CROSS = 0;
    int PLAYING_NEXT_MOVE_NOUGHT = 1;

    int DRAW = 2;
    int WIN_CROSS = 3;
    int WIN_NOUGHT = 4;

    int SURRENDER = 5;

    int PENDING_WAITING_FOR_INVITE_RESPONSE = 6;
    int PENDING_WAITING_FOR_INVITE_REQUEST = 7;
}
