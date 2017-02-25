package com.croconaut.tictactoe.utils;


import android.support.annotation.CheckResult;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.croconaut.tictactoe.model.board.GameSeed;
import com.croconaut.tictactoe.payload.games.Game;
import com.croconaut.tictactoe.payload.games.GameState;

import java.lang.annotation.Retention;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.croconaut.tictactoe.utils.Assertions.assertNotNull;
import static com.croconaut.tictactoe.utils.StateUtils.ExpectedState.DRAW;
import static com.croconaut.tictactoe.utils.StateUtils.ExpectedState.LOST;
import static com.croconaut.tictactoe.utils.StateUtils.ExpectedState.WON;
import static java.lang.annotation.RetentionPolicy.SOURCE;

public final class StateUtils {

    @NonNull
    public static final List<Integer> PENDING_STATES = Collections.unmodifiableList(
            Arrays.asList(
                    GameState.PENDING_WAITING_FOR_INVITE_REQUEST,
                    GameState.PENDING_WAITING_FOR_INVITE_RESPONSE
            )
    );

    @NonNull
    public static final List<Integer> PLAYING_STATES = Collections.unmodifiableList(
            Arrays.asList(
                    GameState.PLAYING_NEXT_MOVE_CROSS,
                    GameState.PLAYING_NEXT_MOVE_NOUGHT
            )
    );

    @NonNull
    public static final List<Integer> PLAYABLE_STATES = Collections.unmodifiableList(
            Arrays.asList(
                    GameState.PENDING_WAITING_FOR_INVITE_REQUEST,
                    GameState.PENDING_WAITING_FOR_INVITE_RESPONSE,
                    GameState.PLAYING_NEXT_MOVE_CROSS,
                    GameState.PLAYING_NEXT_MOVE_NOUGHT
            )
    );

    @NonNull
    public static final List<Integer> ENDED_STATE = Collections.unmodifiableList(
            Arrays.asList(
                    GameState.WIN_CROSS,
                    GameState.WIN_NOUGHT,
                    GameState.DRAW
            )
    );

    @CheckResult
    public static boolean hasGameEnded(@GameState final int gameState) {
        return ENDED_STATE.contains(gameState);
    }

    @CheckResult
    public static boolean isGameInProgress(@GameState final int gameState) {
        return PLAYABLE_STATES.contains(gameState);
    }

    @CheckResult
    @Nullable
    public static Game isGameInState(@NonNull final List<Integer> gameStates,
                                     @NonNull final List<Game> gamesList) {
        assertNotNull(gamesList, "gameslist");
        assertNotNull(gameStates, "gameStates");

        for (final Game game : gamesList) {
            if (gameStates.contains(game.getGameState())) {
                return game;
            }
        }

        return null;
    }

    @CheckResult
    public static boolean canPerformMove(@GameState final int currentState,
                                         @GameSeed final int currentSeed) {
        switch (currentSeed) {
            case GameSeed.CROSS:
                return currentState == GameState.PLAYING_NEXT_MOVE_CROSS;
            case GameSeed.NOUGHT:
                return currentState == GameState.PLAYING_NEXT_MOVE_NOUGHT;
            case GameSeed.BLANK:
                return false; //cannot happen
            default:
                throw new AssertionError("Unknown seed type");
        }
    }

    @CheckResult
    public static boolean isPlayerWaiting(@NonNull final Game game) {
        @GameSeed final int myGameSeed = game.getGameSeed();
        switch (game.getGameState()) {
            case GameState.PLAYING_NEXT_MOVE_CROSS:
                return myGameSeed != GameSeed.CROSS;
            case GameState.PLAYING_NEXT_MOVE_NOUGHT:
                return myGameSeed != GameSeed.NOUGHT;
            case GameState.PENDING_WAITING_FOR_INVITE_REQUEST:
            case GameState.PENDING_WAITING_FOR_INVITE_RESPONSE:
                return true;
            default:
                return false;
        }
    }

    @CheckResult
    @NonNull
    public static List<Game> getGamesWithState(@ExpectedState final int expectedState,
                                               @NonNull final List<Game> gamesList) {
        assertNotNull(gamesList, "gameslist");

        final List<Game> gamesFound = new ArrayList<>();

        for (final Game game : gamesList) {
            @GameState final int gameStateToCompare;

            switch (expectedState) {
                case ExpectedState.DRAW:
                    gameStateToCompare = GameState.DRAW;
                    break;
                case ExpectedState.LOST:
                    gameStateToCompare
                            = game.getGameSeed() == GameSeed.CROSS ? GameState.WIN_NOUGHT : GameState.WIN_CROSS;
                    break;
                case ExpectedState.WON:
                    gameStateToCompare
                            = game.getGameSeed() == GameSeed.CROSS ? GameState.WIN_CROSS : GameState.WIN_NOUGHT;
                    break;
                default:
                    throw new AssertionError("Wrong @ExpectedState parameter!");
            }

            if (gameStateToCompare == game.getGameState()) {
                gamesFound.add(game);
            }
        }

        return Collections.unmodifiableList(gamesFound);
    }

    /**
     * Private constructor prevents instantiation.
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private StateUtils() {
        throw new UnsupportedOperationException("This class is non-instantiable"); //$NON-NLS-1$
    }

    @Retention(SOURCE)
    @IntDef({WON, LOST, DRAW})
    public @interface ExpectedState {
        int WON = 0;
        int LOST = 1;
        int DRAW = 2;

    }
}
