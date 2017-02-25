package com.croconaut.tictactoe.utils;


import org.junit.Test;

import static com.croconaut.tictactoe.utils.GameIdGenerator.GAME_ID_LENGTH;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public final class GameIdGeneratorTest {

    @Test
    public void gameIdConstant_length36(){
        assertThat(GAME_ID_LENGTH, is(36));
    }

    @Test
    public void newGameId_length(){
        final String gameId = GameIdGenerator.newGameId();
        assertThat(gameId.length(), is(GAME_ID_LENGTH));
    }
}
