package com.croconaut.tictactoe.payload.moves;


import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Size;

import com.croconaut.tictactoe.model.board.Board;
import com.croconaut.tictactoe.model.board.GameSeed;
import com.croconaut.tictactoe.payload.TicTacToeGame;
import com.croconaut.tictactoe.payload.games.GameState;

import static com.croconaut.tictactoe.model.board.Board.BOARD_INDEX_MINIMUM;
import static com.croconaut.tictactoe.utils.Assertions.assertEquals;
import static com.croconaut.tictactoe.utils.Assertions.assertIsBiggerThan;
import static com.croconaut.tictactoe.utils.Assertions.assertIsSeed;
import static com.croconaut.tictactoe.utils.Assertions.assertNotNull;
import static com.croconaut.tictactoe.utils.GameIdGenerator.GAME_ID_LENGTH;

public final class Move implements TicTacToeGame {

    @NonNull
    private final String mGameId;

    @GameSeed
    private final int mSeed;

    @GameState
    private final int mGameState;

    private final int mXPos;
    private final int mYPos;

    public Move(@NonNull @Size(GAME_ID_LENGTH) final String gameId,
                @GameSeed final int seed, @GameState final int gameState,
                @IntRange(from = BOARD_INDEX_MINIMUM) final int xPos,
                @IntRange(from = Board.BOARD_INDEX_MINIMUM) final int yPos) {
        assertNotNull(gameId, "gameId"); //$NON-NLS-1$
        assertEquals(gameId.length(), GAME_ID_LENGTH, "gameIdLength"); //$NON-NLS-1$
        assertIsSeed(seed, "seed"); //$NON-NLS-1$
        assertIsBiggerThan(xPos, BOARD_INDEX_MINIMUM, "xPos"); //$NON-NLS-1$
        assertIsBiggerThan(yPos, BOARD_INDEX_MINIMUM, "yPos"); //$NON-NLS-1$

        this.mGameId = gameId;
        this.mSeed = seed;
        this.mXPos = xPos;
        this.mYPos = yPos;
        this.mGameState = gameState;
    }

    @Override
    @NonNull
    public String getGameId() {
        return mGameId;
    }

    @GameSeed
    public int getSeed() {
        return mSeed;
    }

    @GameState
    public int getGameState() { return  mGameState; }

    @Size(min = BOARD_INDEX_MINIMUM)
    public int getXPos() {
        return mXPos;
    }

    @Size(min = BOARD_INDEX_MINIMUM)
    public int getYPos() {
        return mYPos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Move move = (Move) o;

        if (mSeed != move.mSeed) return false;
        if (mXPos != move.mXPos) return false;
        if (mYPos != move.mYPos) return false;
        return mGameId.equals(move.mGameId);

    }

    @Override
    public int hashCode() {
        int result = mGameId.hashCode();
        result = 31 * result + mSeed;
        result = 31 * result + mXPos;
        result = 31 * result + mYPos;
        return result;
    }

    @Override
    public String toString() {
        return "Move{" +
                "mGameId='" + mGameId + '\'' +
                ", mSeed=" + mSeed +
                ", mGameState=" + mGameState +
                ", mXPos=" + mXPos +
                ", mYPos=" + mYPos +
                '}';
    }
}
