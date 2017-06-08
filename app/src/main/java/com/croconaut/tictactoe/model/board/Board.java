package com.croconaut.tictactoe.model.board;


import android.support.annotation.CheckResult;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;

import com.croconaut.tictactoe.payload.games.GameSize;
import com.croconaut.tictactoe.payload.games.GameState;
import com.croconaut.tictactoe.payload.moves.Move;

import java.util.ArrayList;
import java.util.List;

import static com.croconaut.tictactoe.utils.Assertions.assertIsBiggerThan;
import static com.croconaut.tictactoe.utils.Assertions.assertIsInRange;
import static com.croconaut.tictactoe.utils.Assertions.assertIsSeed;
import static com.croconaut.tictactoe.utils.Assertions.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * This class represents TicTacToe game playing field and its logic.
 * <p>
 * See also: {@link Cell}, {@link GameState}, {@link GameSeed}
 */
public final class Board {

    /**
     * The minimum size of playing field. Board cannot be 0x0 or 1x1, otherwise the
     * TicTacToe game would be unplayable.
     */
    public static final int BOARD_SIZE_MINIMUM = 2;

    /**
     * Indexing rule for the board. By configuration (to prevent bugs)
     * the board index starts at zero.
     */
    public static final int BOARD_INDEX_MINIMUM = 0;

    @NonNull
    private final TicTacToeBoardListener mListener;

    @NonNull
    private final Cell[][] mBoard;

    private final int mBoardSize;


    /**
     * Creates a new TicTacToe playing field eg. board.
     *
     * @param listener Listener for delegating calls eg. creating callback to the caller.
     * @param size     Requested board size. The minimum board size is {@link #BOARD_INDEX_MINIMUM}.
     */
    public Board(@NonNull final TicTacToeBoardListener listener,
                 @IntRange(from = BOARD_SIZE_MINIMUM) final int size,
                 @NonNull final List<Move> moveList) {
        assertNotNull(listener, "ticTacToeBoardListener"); //$NON-NLS-1$
        assertNotNull(moveList, "moveList"); //$NON-NLS-1$
        assertIsBiggerThan(size, BOARD_SIZE_MINIMUM, "size"); //$NON-NLS-1$

        this.mListener = listener;
        this.mBoardSize = size;
        this.mBoard = new Cell[size][size];

        for (int row = BOARD_INDEX_MINIMUM; row < mBoardSize; row++) {
            for (int col = BOARD_INDEX_MINIMUM; col < mBoardSize; col++) {
                setCellAt(GameSeed.BLANK, row, col);
            }
        }

        for (final Move move : moveList) {
            performMove(move.getSeed(), move.getXPos(), move.getYPos());
        }
    }

    /**
     * Represents the player move in the TicTacToe game.
     *
     * @param seed Players {@link GameSeed}. See {@link GameSeed#CROSS} and {@link GameSeed#NOUGHT}.
     * @param xPos The 'x' coordinate on the {@link Board}. Must be higher then
     *             {@link Board#BOARD_INDEX_MINIMUM} and lower then {@link Board} size.
     * @param yPos The 'y' coordinate on the {@link Board}. Must be higher then
     *             {@link Board#BOARD_INDEX_MINIMUM} and lower then {@link Board} size.
     */
    public void move(@NonNull final String gameId, @GameSeed final int seed, @IntRange(from = BOARD_INDEX_MINIMUM) final int xPos,
                     @IntRange(from = BOARD_INDEX_MINIMUM) final int yPos) {
        assertNotNull(gameId, "gameId");
        assertIsInRange(xPos, BOARD_INDEX_MINIMUM, mBoardSize - 1, "xPos"); //$NON-NLS-1$
        assertIsInRange(yPos, BOARD_INDEX_MINIMUM, mBoardSize - 1, "yPos"); //$NON-NLS-1$
        assertIsSeed(seed, "seed"); //$NON-NLS-1$d

        final boolean movePerfomed = performMove(seed, xPos, yPos);
        @GameState final int newGameState = checkGameState(seed, xPos, yPos);
        final Move move = new Move(gameId, seed, newGameState, xPos, yPos);

        mListener.onGameStateChanged(movePerfomed, move, newGameState);
    }

    private boolean performMove(@GameSeed final int changeToSeed, final int xPos, final int yPos) {
        @GameSeed final int seed = getSeedAt(xPos, yPos);

        if (seed == GameSeed.BLANK) {
            setCellAt(changeToSeed, xPos, yPos);
            return true;
        }

        return false;
    }

    @GameState
    @CheckResult
    private int checkGameState(@GameSeed final int seed, final int xPos, final int yPos) {

        //check columns
        for (int sizeIndex = BOARD_INDEX_MINIMUM; sizeIndex < mBoardSize; sizeIndex++) {
            if (getSeedAt(xPos, sizeIndex) != seed) {
                break;
            }

            if (reportWin(sizeIndex)) {
                return reportGameState(seed);
            }
        }

        //check rows
        for (int sizeIndex = BOARD_INDEX_MINIMUM; sizeIndex < mBoardSize; sizeIndex++) {
            if (getSeedAt(sizeIndex, yPos) != seed) {
                break;
            }

            if (reportWin(sizeIndex)) {
                return reportGameState(seed);
            }
        }

        //check diagonals
        if (xPos == yPos) {
            for (int sizeIndex = BOARD_INDEX_MINIMUM; sizeIndex < mBoardSize; sizeIndex++) {
                if (getSeedAt(sizeIndex, sizeIndex) != seed) {
                    break;
                }

                if (reportWin(sizeIndex)) {
                    return reportGameState(seed);
                }
            }
        }

        //check anti-diagonals
        if (xPos + yPos == mBoardSize - 1) {
            for (int sizeIndex = BOARD_INDEX_MINIMUM; sizeIndex < mBoardSize; sizeIndex++) {
                if (getSeedAt(sizeIndex, (mBoardSize - 1) - sizeIndex) != seed) {
                    break;
                }

                if (reportWin(sizeIndex)) {
                    return reportGameState(seed);
                }
            }
        }

        if (isDraw()) {
            return GameState.DRAW;
        }

        return seed == GameSeed.CROSS ? GameState.PLAYING_NEXT_MOVE_NOUGHT : GameState.PLAYING_NEXT_MOVE_CROSS;
    }

    @GameState
    private int reportGameState(@GameSeed final int seed) {
        return seed == GameSeed.CROSS ? GameState.WIN_CROSS : GameState.WIN_NOUGHT;
    }

    @CheckResult
    private boolean isDraw() {
        for (int row = BOARD_INDEX_MINIMUM; row < mBoardSize; row++) {
            for (int col = BOARD_INDEX_MINIMUM; col < mBoardSize; col++) {
                if (getSeedAt(row, col) == GameSeed.BLANK) {
                    return false;
                }
            }
        }
        return true;
    }

    @CheckResult
    private boolean reportWin(final int lastSizeIndex) {
        return lastSizeIndex == (mBoardSize - 1);
    }

    @GameSeed
    @CheckResult
    private int getSeedAt(final int xPos, final int yPos) {
        return mBoard[xPos][yPos].getSeed();
    }

    private void setCellAt(@GameSeed final int seed, final int xPos, final int yPos) {
        mBoard[xPos][yPos] = new Cell(xPos, yPos, seed);
    }

    @NonNull
    public List<Cell> getCellList() {
        final List<Cell> cellList = new ArrayList<>();
        for (int row = BOARD_INDEX_MINIMUM; row < mBoardSize; row++) {
            for (int col = BOARD_INDEX_MINIMUM; col < mBoardSize; col++) {
                cellList.add(mBoard[row][col]);
            }
        }
        return cellList;
    }


    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    @RestrictTo(RestrictTo.Scope.TESTS)
    /*package*/ Cell[][] getBoard() {
        return mBoard;
    }

    @Override
    public String toString() {
        return getCellList().toString();
    }


    /**
     * Listener for {@link Board} delegating calls eg. creating callback to the caller.
     * <p>
     * See also {@link Board}.
     */
    public interface TicTacToeBoardListener {
        void onGameStateChanged(final boolean movePerformed, @NonNull final Move move,
                                @GameState final int gameState);
    }
}
