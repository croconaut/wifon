package com.croconaut.tictactoe.utils;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.croconaut.tictactoe.model.board.GameSeed;

import java.util.Locale;

/**
 * Runtime assertions checks.
 */
public final class Assertions {

    /**
     * @param actual Value to be compared against {@code value}.
     * @param value Value that the lower limit of {@code actual}.
     * @param name The string representation of {@code actual}.
     *
     * @throws AssertionError If {@code actual} is lower than {@code value}.
     */
    public static void assertIsBiggerThan(final int actual, final int value,
                                          @NonNull final String name) {
        if (actual < value) {
            throw new AssertionError(String.format(Locale.US,
                    "%s cannot be lower then %d currently is %d",  //$NON-NLS-1$
                    name, value, actual));
        }
    }

    /**
     * @param actual Value that will be checked whether it is bigger or equal than zero.
     * @param name The string representation of {@code actual}.
     *
     * @throws AssertionError If {@code actual} is lower then zero.
     */
    public static void assertIsPositive(final int actual, @NonNull final String name) {
        if (actual < 0) {
            throw new AssertionError(String.format(Locale.US,
                    "%s cannot be negative and is: %d", name, actual)); //$NON-NLS-1$
        }
    }

    /**
     * @param object Object that will be checked whether it is null.
     * @param name The string representation of {@code object}.
     *
     * @throws AssertionError If {@code object} is null.
     */
    public static void assertNotNull(@Nullable final Object object, @NonNull final String name) {
        if (null == object) {
            throw new AssertionError(
                    String.format(Locale.US, "%s cannot be null", name));//$NON-NLS-1$
        }
    }

    /**
     *
     * @param actual Value that will be checked whether it is inside {@code minValue}, {@code maxValue}.
     * @param minValue Lower limit for {@code value}.
     * @param maxValue Upper limit for {@code value}.
     * @param name The string representation of {@code actual}.
     *
     * @throws IllegalArgumentException If {@code minValue} is bigger then {@code maxValue}.
     * @throws AssertionError If {@code actual} isn't in interval of {@code minValue},{@code maxValue}
     */
    public static void assertIsInRange(final int actual, final int minValue,
                                       final int maxValue, @NonNull final String name) {
        if (minValue > maxValue) {
            throw new IllegalArgumentException("maxValue has to be bigger then minValue"); //$NON-NLS-1$
        }

        if (actual < minValue || actual > maxValue) {
            throw new AssertionError(String.format(Locale.US,
                    "%s has value %d that is not in range of: %d,%d", name, actual, minValue, //$NON-NLS-1$
                    maxValue));
        }
    }

    /**
     * @param seed Represents value that will be checked whether is is truly {@link GameSeed}.
     * @param name The string representation of {@code seed}.
     *
     *  @throws AssertionError If {@code seed} is not {@link GameSeed}.
     */
    public static void assertIsSeed(@GameSeed final int seed, @NonNull final String name) {
        final boolean isSeed = seed == GameSeed.BLANK || seed == GameSeed.CROSS || seed == GameSeed.NOUGHT;
        if (!isSeed) {
            throw new AssertionError(String.format(Locale.US,
                    "%s is not seed, value found: %d", name, seed));//$NON-NLS-1$
        }
    }

    /**
     *
     * @param actual Integer that needs to be compared to {@code value}.
     * @param value Value to compare.
     * @param name  The string representation of {@code actual}.
     *
     * @throws AssertionError If {@code value} and {@code value} are not equals.
     */
    public static void assertEquals(final int actual, final int value, @NonNull final String name) {
        if (actual != value) {
            throw new AssertionError(
                    String.format(Locale.US, "%s is not equals, %d != %d", name, actual, value)); //$NON-NLS-1$
        }
    }

    /**
     * Private constructor prevents instantiation.
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private Assertions() {
        throw new UnsupportedOperationException("This class is non-instantiable"); //$NON-NLS-1$
    }
}
