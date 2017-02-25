package com.croconaut.tictactoe.payload.games;

import android.support.annotation.NonNull;
import android.support.annotation.Size;

import com.croconaut.tictactoe.model.board.GameSeed;
import com.croconaut.tictactoe.payload.TicTacToeGame;

import static com.croconaut.tictactoe.model.board.Board.BOARD_SIZE_MINIMUM;
import static com.croconaut.tictactoe.utils.Assertions.assertEquals;
import static com.croconaut.tictactoe.utils.Assertions.assertIsBiggerThan;
import static com.croconaut.tictactoe.utils.Assertions.assertIsSeed;
import static com.croconaut.tictactoe.utils.Assertions.assertNotNull;
import static com.croconaut.tictactoe.utils.GameIdGenerator.GAME_ID_LENGTH;


public final class Game implements TicTacToeGame {

    @NonNull
    private final String mGameId;

    @NonNull
    private final String mRemoteProfileId;

    @GameSeed
    private final int mGameSeed;

    @GameSize
    private final int mGameSize;

    @GameState
    private final int mGameState;

    private final long mGameTimestamp;

    public Game(@NonNull @Size(GAME_ID_LENGTH) final String gameId, final long gameTimestamp,
                @NonNull @Size(min = 1) final String remoteProfileId, @GameSeed final int seed,
                @GameSize final int gameSize, @GameState final int gameState) {
        assertNotNull(remoteProfileId, "remoteProfileId"); //$NON-NLS-1$
        assertIsBiggerThan(remoteProfileId.length(), 1, "remoteProfileIdLength"); //$NON-NLS-1$
        assertNotNull(gameId, "gameId"); //$NON-NLS-1$
        assertEquals(gameId.length(), GAME_ID_LENGTH, "gameIdLength"); //$NON-NLS-1$
        assertIsSeed(seed, "seed"); //$NON-NLS-1$
        assertIsBiggerThan(gameSize, BOARD_SIZE_MINIMUM, "gameSize"); //$NON-NLS-1$

        this.mGameId = gameId;
        this.mRemoteProfileId = remoteProfileId;
        this.mGameSeed = seed;
        this.mGameSize = gameSize;
        this.mGameState = gameState;
        this.mGameTimestamp = gameTimestamp;
    }

    @Override
    @NonNull
    @Size(GAME_ID_LENGTH)
    public String getGameId() {
        return mGameId;
    }

    @GameSeed
    public int getGameSeed() {
        return mGameSeed;
    }

    @GameState
    public int getGameState() {
        return mGameState;
    }

    @NonNull
    @Size(min = 1)
    public String getRemoteProfileId() {
        return mRemoteProfileId;
    }

    @GameSize
    public int getGameSize() {
        return mGameSize;
    }

    public long getGameTimestamp() {
        return mGameTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Game game = (Game) o;

        if (mGameState != game.mGameState) return false;
        if (mGameSeed != game.mGameSeed) return false;
        if (mGameSize != game.mGameSize) return false;
        if (!mGameId.equals(game.mGameId)) return false;
        return mRemoteProfileId.equals(game.mRemoteProfileId);

    }

    @Override
    public int hashCode() {
        int result = mGameId.hashCode();
        result = 31 * result + mRemoteProfileId.hashCode();
        result = 31 * result + mGameState;
        result = 31 * result + mGameSeed;
        result = 31 * result + mGameSize;
        return result;
    }
}
