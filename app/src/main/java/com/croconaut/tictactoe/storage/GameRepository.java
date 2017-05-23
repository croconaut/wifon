package com.croconaut.tictactoe.storage;


import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.croconaut.tictactoe.payload.games.Game;
import com.croconaut.tictactoe.payload.games.GameState;
import com.croconaut.tictactoe.payload.invites.InviteRequest;
import com.croconaut.tictactoe.payload.moves.Move;
import com.croconaut.tictactoe.storage.utils.models.InviteLockWrapper;

import java.util.List;

public interface GameRepository {

    @WorkerThread
    @CheckResult
    @NonNull
    List<Move> getAllMovesByGameId(@NonNull final String gameId);

    @WorkerThread
    @CheckResult
    @NonNull
    List<Game> getAllGamesWithState(@NonNull final String remoteProfileId, @GameState final int gameState);

    @WorkerThread
    @CheckResult
    @Nullable
    Game getGameByGameId(@NonNull final String gameId);

    @WorkerThread
    @CheckResult
    @NonNull
    List<Game> getGamesByPlayerId(@NonNull final String playerId);

    @WorkerThread
    void insertGame(@NonNull final Game game);

    @WorkerThread
    void deleteGame(@NonNull final Game game);

    @WorkerThread
    void updateGameState(@NonNull final Game game, @GameState final int newGameState);

    @WorkerThread
    void insertMove(@NonNull final Move move);

    @WorkerThread
    void insertInviteLock(@NonNull final String playerId, final long creationTime);

    @WorkerThread
    void deleteInviteLock(final long creationTime);

    @WorkerThread
    void updateInviteLock(@NonNull final String playerId, @NonNull final InviteRequest inviteRequest);

    @WorkerThread
    InviteLockWrapper isInviteLockPresent(@NonNull final String playerId);

    @WorkerThread
    InviteLockWrapper isInviteLockPresent(final long creationTime);
}
