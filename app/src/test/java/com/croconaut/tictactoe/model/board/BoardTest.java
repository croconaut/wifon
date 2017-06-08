package com.croconaut.tictactoe.model.board;


import android.support.annotation.NonNull;

import com.croconaut.tictactoe.payload.games.GameState;

import org.junit.Test;

import java.util.Random;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class BoardTest {

    @Test(expected = AssertionError.class)
    public void boardNewInstance_nullTicTacToeListener() {
        new Board(null, FIXTURE_BOARD_SIZE);
    }

    @Test(expected = AssertionError.class)
    public void boardNewInstance_incorrectSize() {
        final Random lowerThenTwo = new Random();
        new Board(LISTENER_EMPTY, lowerThenTwo.nextInt(2) - 2);
    }

    @Test
    public void boardNewInstance_correctSize() {
        new Board(LISTENER_EMPTY, FIXTURE_BOARD_SIZE);
    }

    @Test
    public void boardNewInstance_areSeedsEmpty() {
        final Board board = new Board(LISTENER_EMPTY, FIXTURE_BOARD_SIZE);

        for (int row = 0; row < FIXTURE_BOARD_SIZE; row++) {
            for (int col = 0; col < FIXTURE_BOARD_SIZE; col++) {
                assertThat(board.getBoard()[row][col].getSeed(), is(GameSeed.BLANK));
            }
        }
    }

    @Test(expected = AssertionError.class)
    public void boardMoveTo_incorrectCoordinates() {
        final Board board = new Board(LISTENER_EMPTY, FIXTURE_BOARD_SIZE);
        board.move(GameSeed.CROSS, 10, 10);
    }

    @Test
    public void boardMoveTo_correctCoordinates() {
        final Board board = new Board(LISTENER_EMPTY, FIXTURE_BOARD_SIZE);
        board.move(GameSeed.CROSS, 1, 1);
    }

    @Test
    public void board_correctMove() {
        final Board.TicTacToeBoardListener listener = new Board.TicTacToeBoardListener() {
            @Override
            public void onGameStateChanged(@GameState int gameState) {
                assertThat(gameState, is(GameState.PLAYING));
            }

            @Override
            public void onMovePerformed(boolean movePerformed) {
                assertThat(movePerformed, is(true));
            }
        };

        final Board board = new Board(listener, FIXTURE_BOARD_SIZE);

        board.move(GameSeed.CROSS, 0, 0);
    }

    @Test
    public void board_sameMoveTwoTimesSamePosition() {
        final Board.TicTacToeBoardListener listener = new Board.TicTacToeBoardListener() {
            @Override
            public void onGameStateChanged(@GameState int gameState) {
                assertThat(gameState, is(GameState.PLAYING));
            }

            @Override
            public void onMovePerformed(boolean movePerformed) {
                assertThat(movePerformed, is(false));
            }
        };

        final Board board = new Board(listener, FIXTURE_BOARD_SIZE);
        board.getBoard()[0][0] = new Cell(0, 0, GameSeed.CROSS);

        board.move(GameSeed.CROSS, 0, 0);
    }

    @Test
    public void boardEndGame_winCrossByRow() {
        final Board board = new Board(LISTENER_WON_CROSS, FIXTURE_BOARD_SIZE);
        board.getBoard()[0][0] = new Cell(0, 0, GameSeed.CROSS);
        board.getBoard()[0][1] = new Cell(0, 1, GameSeed.CROSS);

        board.move(GameSeed.CROSS, 0, 2);
    }

    @Test
    public void boardEndGame_winCrossByColumn() {
        final Board board = new Board(LISTENER_WON_CROSS, FIXTURE_BOARD_SIZE);
        board.getBoard()[0][0] = new Cell(0, 0, GameSeed.CROSS);
        board.getBoard()[1][0] = new Cell(1, 0, GameSeed.CROSS);

        board.move(GameSeed.CROSS, 2, 0);
    }

    @Test
    public void boardEndGame_winCrossByDiagonal() {
        final Board board = new Board(LISTENER_WON_CROSS, FIXTURE_BOARD_SIZE);
        board.getBoard()[0][0] = new Cell(0, 0, GameSeed.CROSS);
        board.getBoard()[1][1] = new Cell(1, 1, GameSeed.CROSS);

        board.move(GameSeed.CROSS, 2, 2);
    }

    @Test
    public void boardEndGame_winCrossByAntiDiagonal() {
        final Board board = new Board(LISTENER_WON_CROSS, FIXTURE_BOARD_SIZE);
        board.getBoard()[0][2] = new Cell(0, 2, GameSeed.CROSS);
        board.getBoard()[1][1] = new Cell(1, 1, GameSeed.CROSS);

        board.move(GameSeed.CROSS, 2, 0);
    }

    @Test
    public void boardEndGame_winNoughtByRow() {
        final Board board = new Board(LISTENER_WON_NOUGHT, FIXTURE_BOARD_SIZE);
        board.getBoard()[0][0] = new Cell(0, 0, GameSeed.NOUGHT);
        board.getBoard()[0][1] = new Cell(0, 1, GameSeed.NOUGHT);

        board.move(GameSeed.NOUGHT, 0, 2);
    }

    @Test
    public void boardEndGame_winNoughtByDiagonal() {
        final Board board = new Board(LISTENER_WON_NOUGHT, FIXTURE_BOARD_SIZE);
        board.getBoard()[0][0] = new Cell(0, 0, GameSeed.NOUGHT);
        board.getBoard()[1][1] = new Cell(1, 1, GameSeed.NOUGHT);

        board.move(GameSeed.NOUGHT, 2, 2);
    }

    @Test
    public void boardEndGame_winNoughtByAntiDiagonal() {
        final Board board = new Board(LISTENER_WON_NOUGHT, FIXTURE_BOARD_SIZE);
        board.getBoard()[0][2] = new Cell(0, 2, GameSeed.NOUGHT);
        board.getBoard()[1][1] = new Cell(1, 1, GameSeed.NOUGHT);

        board.move(GameSeed.NOUGHT, 2, 0);
    }

    @Test
    public void boardEndGame_winNoughtByColumn() {
        final Board board = new Board(LISTENER_WON_NOUGHT, FIXTURE_BOARD_SIZE);
        board.getBoard()[0][0] = new Cell(0, 0, GameSeed.NOUGHT);
        board.getBoard()[1][0] = new Cell(1, 0, GameSeed.NOUGHT);

        board.move(GameSeed.NOUGHT, 2, 0);
    }


    @Test
    public void boardEndGame_draw() {
        final Board.TicTacToeBoardListener listener = new Board.TicTacToeBoardListener() {
            @Override
            public void onGameStateChanged(@GameState int gameState) {
                assertThat(gameState, is(GameState.DRAW));
            }

            @Override
            public void onMovePerformed(boolean movePerformed) {
                assertThat(movePerformed, is(true));
            }
        };

        final Board board = new Board(listener, FIXTURE_BOARD_SIZE);
        board.getBoard()[0][0] = new Cell(0, 0, GameSeed.NOUGHT);
        board.getBoard()[0][1] = new Cell(0, 1, GameSeed.NOUGHT);
        board.getBoard()[0][2] = new Cell(0, 2, GameSeed.CROSS);

        board.getBoard()[1][0] = new Cell(1, 0, GameSeed.CROSS);
        board.getBoard()[1][1] = new Cell(1, 1, GameSeed.CROSS);
        board.getBoard()[1][2] = new Cell(1, 2, GameSeed.NOUGHT);

        board.getBoard()[2][0] = new Cell(2, 0, GameSeed.NOUGHT);
        board.getBoard()[2][1] = new Cell(2, 1, GameSeed.NOUGHT);

        board.move(GameSeed.CROSS, 2, 2);
    }




    /* ---------------------------------------- CONSTANTS ----------------------------------------*/

    private static final int FIXTURE_BOARD_SIZE = 3;

    private static final Board.TicTacToeBoardListener LISTENER_WON_NOUGHT
            = new Board.TicTacToeBoardListener() {
        @Override
        public void onGameStateChanged(@GameState int gameState) {
            assertThat(gameState, is(GameState.WIN_NOUGHT));
        }

        @Override
        public void onMovePerformed(boolean movePerformed) {
            assertThat(movePerformed, is(true));
        }
    };

    private static final Board.TicTacToeBoardListener LISTENER_WON_CROSS
            = new Board.TicTacToeBoardListener() {
        @Override
        public void onGameStateChanged(@GameState int gameState) {
            assertThat(gameState, is(GameState.WIN_CROSS));
        }

        @Override
        public void onMovePerformed(boolean movePerformed) {
            assertThat(movePerformed, is(true));
        }
    };

    @NonNull
    private static final Board.TicTacToeBoardListener LISTENER_EMPTY
            = new Board.TicTacToeBoardListener() {
        @Override
        public void onGameStateChanged(@GameState int gameState) {

        }

        @Override
        public void onMovePerformed(boolean movePerformed) {

        }
    };
    /* ---------------------------------------- CONSTANTS ----------------------------------------*/

}
