package com.croconaut.tictactoe.model.board;


import android.support.annotation.IntRange;
import android.support.annotation.Size;

import static com.croconaut.tictactoe.model.board.Board.BOARD_INDEX_MINIMUM;
import static com.croconaut.tictactoe.utils.Assertions.assertIsBiggerThan;
import static com.croconaut.tictactoe.utils.Assertions.assertIsSeed;

/**
 * This class represents entry of the {@link Board} game.
 * <p>
 * See also {@link GameSeed}.
 */
public final class Cell {

    @GameSeed
    private final int mSeed;

    private final int mXPos;
    private final int mYPos;

    /**
     * Creates new instance of {@link Cell} class.
     *
     * @param xPos The 'x' coordinate on the {@link Board} game.
     *             Must be higher then {@link Board#BOARD_INDEX_MINIMUM}.
     * @param yPos The 'y' coordinate on the {@link Board} game.
     *             Must be higher then {@link Board#BOARD_INDEX_MINIMUM}.
     * @param seed {@link GameSeed} that represents the game content on the {@code xPos}, {@code yPos}.
     */
    public Cell(@IntRange(from = BOARD_INDEX_MINIMUM) final int xPos,
                @IntRange(from = BOARD_INDEX_MINIMUM) final int yPos,
                @GameSeed final int seed) {
        assertIsSeed(seed, "seed"); //$NON-NLS-1$d
        assertIsBiggerThan(xPos, BOARD_INDEX_MINIMUM, "xPos"); //$NON-NLS-1$
        assertIsBiggerThan(yPos, BOARD_INDEX_MINIMUM, "yPos"); //$NON-NLS-1$

        this.mXPos = xPos;
        this.mYPos = yPos;
        this.mSeed = seed;
    }

    @GameSeed
    public int getSeed() {
        return mSeed;
    }

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

        Cell cell = (Cell) o;

        if (mSeed != cell.mSeed) return false;
        if (mXPos != cell.mXPos) return false;
        return mYPos == cell.mYPos;

    }

    @Override
    public int hashCode() {
        int result = mSeed;
        result = 31 * result + mXPos;
        result = 31 * result + mYPos;
        return result;
    }

    @Override
    public String toString() {
        return "Cell{" +
                "mSeed=" + mSeed +
                ", mXPos=" + mXPos +
                ", mYPos=" + mYPos +
                '}';
    }
}
