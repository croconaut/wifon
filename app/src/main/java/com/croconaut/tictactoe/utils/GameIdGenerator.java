package com.croconaut.tictactoe.utils;


import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Size;

import java.util.UUID;

public final class GameIdGenerator {

    public static final int GAME_ID_LENGTH = 36;

    @NonNull
    @CheckResult
    @Size(GAME_ID_LENGTH)
    public static String newGameId() {
        return UUID.randomUUID().toString();
    }


    /**
     * Private constructor prevents instantiation.
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private GameIdGenerator() {
        throw new UnsupportedOperationException("This class is non-instantiable"); //$NON-NLS-1$
    }
}
