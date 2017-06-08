package com.croconaut.tictactoe.payload.moves;


import com.croconaut.tictactoe.utils.GameIdGenerator;

import org.junit.Test;

import java.util.Random;

import static com.croconaut.tictactoe.model.board.Board.BOARD_INDEX_MINIMUM;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class MoveTest {

    @Test
    public void newInstance_valid() {
        final String gameId = GameIdGenerator.newGameId();
        final Move move = new Move(gameId, 1, 2);

        assertThat(move.getGameId(), is(gameId));
        assertThat(move.getXPos(), is(1));
        assertThat(move.getYPos(), is(2));
    }

    @Test(expected = AssertionError.class)
    public void newInstance_wrongGameId() {
        final String gameId = String.valueOf("not_long_enough"); //$NON-NLS-1$
        new Move(gameId, 1, 2);
    }

    @Test(expected = AssertionError.class)
    public void newInstance_wrongXPos() {
        final String gameId = GameIdGenerator.newGameId();
        final int wrongXPos = BOARD_INDEX_MINIMUM
                - (BOARD_INDEX_MINIMUM + 1 + new Random().nextInt(BOARD_INDEX_MINIMUM + 1));
        new Move(gameId, wrongXPos, 2);
    }

    @Test(expected = AssertionError.class)
    public void newInstance_wrongYPos() {
        final String gameId = GameIdGenerator.newGameId();
        final int wrongYPos =BOARD_INDEX_MINIMUM
                - (BOARD_INDEX_MINIMUM + 1 + new Random().nextInt(BOARD_INDEX_MINIMUM + 1));
        new Move(gameId, 0, wrongYPos);
    }
}
