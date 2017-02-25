package com.croconaut.tictactoe.utils;


import com.croconaut.tictactoe.model.board.GameSeed;

import org.junit.Test;


public final class AssertionsTest {

    @Test
    public void assertNonNull_not_null_object() {
        Assertions.assertNotNull(new Object(), "notNullObject"); //$NON-NLS-1$
    }

    @Test(expected = AssertionError.class)
    public void assertNonNull_null_object() {
        Assertions.assertNotNull(null, "nullObject"); //$NON-NLS-1$
    }

    @Test
    public void assertIsPositive_possitive_number() {
        Assertions.assertIsPositive(4, "positiveNumber"); //$NON-NLS-1$
    }

    @Test(expected = AssertionError.class)
    public void assertIsPositive_negative_number() {
        Assertions.assertIsPositive(-1, "negativeNumber"); //$NON-NLS-1$
    }

    @Test
    public void assertIsPositive_zero() {
        Assertions.assertIsPositive(0, "zero"); //$NON-NLS-1$
    }

    @Test
    public void assertIsBiggerThen_true() {
        Assertions.assertIsBiggerThan(5, 1, "isBigger"); //$NON-NLS-1$
    }

    @Test(expected = AssertionError.class)
    public void assertIsBiggerThen_false() {
        Assertions.assertIsBiggerThan(1, 5, "isNotBigger"); //$NON-NLS-1$
    }

    @Test
    public void assertIsInRangeInclusive_true() {
        Assertions.assertIsInRange(10, 5, 15, "isInRangeInclusiveTrue"); //$NON-NLS-1$
    }

    @Test(expected = AssertionError.class)
    public void assertIsInRangeInclusive_false() {
        Assertions.assertIsInRange(10, 15, 20, "isInRangeInclusiveFalse"); //$NON-NLS-1$
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertIsInRangeInclusive_wrong_arguments() {
        Assertions.assertIsInRange(10, 15, 5, "isInRangeInclusiveWrongArg"); //$NON-NLS-1$
    }

    @Test
    public void assertIsSeed_blank() {
        Assertions.assertIsSeed(GameSeed.BLANK, "blank"); //$NON-NLS-1$
    }

    @Test
    public void assertIsSeed_cross() {
        Assertions.assertIsSeed(GameSeed.CROSS, "cross"); //$NON-NLS-1$
    }

    @Test
    public void assertIsSeed_nought() {
        Assertions.assertIsSeed(GameSeed.NOUGHT, "nought"); //$NON-NLS-1$
    }

    @Test
    public void assertIsEqual_sameInt() {
        Assertions.assertEquals(1, 1, "intTrue"); //$NON-NLS-1$
    }

    @Test(expected = AssertionError.class)
    public void assertIsEqual_different() {
        Assertions.assertEquals(1, 2, "intFalse"); //$NON-NLS-1$
    }

}
