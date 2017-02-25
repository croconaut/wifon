package com.croconaut.tictactoe.utils;


import android.support.annotation.CheckResult;

import com.croconaut.tictactoe.model.board.GameSeed;

import static com.croconaut.tictactoe.utils.StringUtils.getSeedString;

public final class SeedUtils {

    @CheckResult
    public static String getOppositeSeed(@GameSeed final int gameSeed){
        return gameSeed == GameSeed.CROSS ? getSeedString(GameSeed.NOUGHT) : getSeedString(GameSeed.CROSS);
    }

    @CheckResult
    @GameSeed
    public static int getOpposite(@GameSeed final int gameSeed){
        return gameSeed == GameSeed.CROSS ? GameSeed.NOUGHT : GameSeed.CROSS;
    }

    /**
     * Private constructor prevents instantiation.
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private SeedUtils() {
        throw new UnsupportedOperationException("This class is non-instantiable"); //$NON-NLS-1$
    }
}
