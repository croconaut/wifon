package com.croconaut.tictactoe.model.board;


import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class CellTest {

    @Test
    public void cell_newInstance() {
        final Cell cell = new Cell(1, 2, GameSeed.BLANK);

        assertThat(cell.getXPos(), is(1));
        assertThat(cell.getYPos(), is(2));
        assertThat(cell.getSeed(), is(GameSeed.BLANK));
    }

    @Test
    public void cell_toString() {
        final Cell cell = new Cell(1, 2, GameSeed.BLANK);

        assertThat(cell.toString(), containsString(String.valueOf(cell.getXPos())));
        assertThat(cell.toString(), containsString(String.valueOf(cell.getYPos())));
        assertThat(cell.toString(), containsString(String.valueOf(cell.getSeed())));
    }
}
