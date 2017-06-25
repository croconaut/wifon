package com.croconaut.tictactoe.communication;


import android.content.Context;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;
import android.util.Log;

import com.croconaut.cpt.data.Communication;
import com.croconaut.cpt.data.OutgoingMessage;
import com.croconaut.cpt.data.OutgoingPayload;
import com.croconaut.ratemebuddy.AppData;
import com.croconaut.ratemebuddy.utils.pojo.profiles.Profile;
import com.croconaut.tictactoe.model.board.GameSeed;
import com.croconaut.tictactoe.payload.games.Game;
import com.croconaut.tictactoe.payload.games.GameSize;
import com.croconaut.tictactoe.payload.games.GameState;
import com.croconaut.tictactoe.payload.invites.InviteRequest;
import com.croconaut.tictactoe.payload.invites.InviteResponse;
import com.croconaut.tictactoe.payload.moves.Move;
import com.croconaut.tictactoe.payload.moves.Surrender;
import com.croconaut.tictactoe.storage.GameRepository;
import com.croconaut.tictactoe.storage.database.SQLiteDbHelper;
import com.croconaut.tictactoe.storage.repository.GameRepositoryImpl;
import com.croconaut.tictactoe.ui.notifications.InviteNotificationManager;
import com.croconaut.tictactoe.utils.GameIdGenerator;
import com.croconaut.tictactoe.utils.StateUtils;

import java.io.IOException;
import java.util.List;

import static com.croconaut.tictactoe.model.board.Board.BOARD_SIZE_MINIMUM;
import static com.croconaut.tictactoe.payload.games.GameState.WIN_NOUGHT;
import static com.croconaut.tictactoe.utils.Assertions.assertIsBiggerThan;
import static com.croconaut.tictactoe.utils.Assertions.assertIsSeed;
import static com.croconaut.tictactoe.utils.Assertions.assertNotNull;
import static com.croconaut.tictactoe.utils.StateUtils.PENDING_STATES;
import static com.croconaut.tictactoe.utils.StateUtils.isGameInProgress;

public final class GameCommunication {

    @NonNull
    private static final String TAG = GameCommunication.class.getName();

    @NonNull
    private final Context mContext;

    @NonNull
    private final GameRepository mGameRepository;

    public GameCommunication(@NonNull final Context context) {
        assertNotNull(context, "context");

        this.mContext = context;
        this.mGameRepository = new GameRepositoryImpl(new SQLiteDbHelper(mContext));
    }

    @CheckResult
    @Nullable
    public Game inviteToGame(@NonNull @Size(min = 1) final String remotePlayerId,
                             @GameSeed final int seed,
                             @GameSize final int gameSize) {
        assertNotNull(remotePlayerId, "remotePlayerId");
        assertIsBiggerThan(remotePlayerId.length(), 1, "remotePlayerId");
        assertIsBiggerThan(gameSize, BOARD_SIZE_MINIMUM, "gameSize");
        assertIsSeed(seed, "seed");

        try {
            final Game game = new Game(
                    GameIdGenerator.newGameId(), System.currentTimeMillis(),
                    remotePlayerId, seed, gameSize, GameState.PENDING_WAITING_FOR_INVITE_RESPONSE);

            final InviteRequest gameInvite = new InviteRequest(game);

            if (mGameRepository.isInviteLockPresent(remotePlayerId) != null) {
                Log.e(TAG, "INVITE LOCK - UPDATING INVITE LOCK");
                mGameRepository.updateInviteLock(remotePlayerId, gameInvite);
            } else {
                final OutgoingPayload outgoingPayload = new OutgoingPayload(gameInvite);
                final OutgoingMessage outgoingMessage = new OutgoingMessage(remotePlayerId, outgoingPayload);
                Communication.newMessage(mContext, outgoingMessage);
            }

            mGameRepository.insertGame(game);

            return game;
        } catch (IOException ioException) {
            Log.e(TAG, "Invite to game failed.", ioException);
            return null;
        }
    }

    @CheckResult
    public boolean processInviteRequest(@NonNull final AppData appData,
                                        @NonNull @Size(min = 1) final String remotePlayerId,
                                        @NonNull final InviteRequest inviteRequest) {
        assertNotNull(inviteRequest, "inviteRequest");
        assertNotNull(remotePlayerId, "remotePlayerId");
        assertIsBiggerThan(remotePlayerId.length(), 1, "remotePlayerId");

        final Profile profile = appData.getProfileDataSource().getProfileByCrocoId(remotePlayerId);
        final List<Game> playerGames = mGameRepository.getGamesByPlayerId(remotePlayerId);

        final Game gameInPendingState = StateUtils.isGameInState(PENDING_STATES, playerGames);

        if (null != gameInPendingState) {
            if (gameInPendingState.getGameTimestamp() > inviteRequest.getGameTimestamp()) {
                insertPendingGame(remotePlayerId, inviteRequest);

                mGameRepository.deleteGame(gameInPendingState);
                InviteNotificationManager
                        .createInviteRequestNotification(appData, profile, inviteRequest, true);
            }
        } else {
            insertPendingGame(remotePlayerId, inviteRequest);

            InviteNotificationManager
                    .createInviteRequestNotification(appData, profile, inviteRequest, false);
        }

        return true;
    }

    private void insertPendingGame(@NonNull @Size(min = 1) String remotePlayerId,
                                   @NonNull InviteRequest inviteRequest) {
        final @GameState int gameState = GameState.PENDING_WAITING_FOR_INVITE_REQUEST;

        final @GameSeed int gameSeed = inviteRequest.getSeed() == GameSeed.CROSS
                ? GameSeed.NOUGHT
                : GameSeed.CROSS;

        final Game game = new Game(
                inviteRequest.getGameId(), inviteRequest.getGameTimestamp(), remotePlayerId,
                gameSeed, inviteRequest.getGameSize(), gameState);

        mGameRepository.insertGame(game);

        Log.e(TAG, "INVITE NEW processInviteRequest: "
                + remotePlayerId + " for game " + inviteRequest.getGameId());
    }

    @CheckResult
    public boolean processInviteResponse(@NonNull final AppData appData,
                                         @NonNull @Size(min = 1) final String remotePlayerId,
                                         @NonNull final InviteResponse inviteResponse) {
        assertNotNull(inviteResponse, "inviteResponse");
        assertNotNull(remotePlayerId, "remotePlayerId");
        assertIsBiggerThan(remotePlayerId.length(), 1, "remotePlayerId");

        boolean valid = false;

        final Profile profile = appData.getProfileDataSource().getProfileByCrocoId(remotePlayerId);
        final Game game = mGameRepository.getGameByGameId(inviteResponse.getGameId());

        if (null != game) {
            valid = true;

            if (!StateUtils.isGameInProgress(game.getGameState())) {
                return valid;
            }

            if (inviteResponse.isGameAccepted()) {
                @GameState final int newGameState = game.getGameSeed() == GameSeed.CROSS
                        ? GameState.PLAYING_NEXT_MOVE_CROSS
                        : GameState.PLAYING_NEXT_MOVE_NOUGHT;
                mGameRepository.updateGameState(game, newGameState);
            } else {
                Log.e(TAG, "Game was rejected by remote player");
                mGameRepository.deleteGame(game);
            }

            InviteNotificationManager
                    .createInviteResponseNotification(appData, profile, inviteResponse);
        }

        return valid;
    }

    @CheckResult
    @Nullable
    public Game acceptInvite(@NonNull @Size(min = 1) final String remotePlayerId,
                             @NonNull final String gameId, @GameSeed final int gameSeed) {
        assertNotNull(gameId, "gameId");
        assertNotNull(remotePlayerId, "remotePlayerId");
        assertIsBiggerThan(remotePlayerId.length(), 1, "remotePlayerId");

        try {
            final @GameState int gameState = gameSeed == GameSeed.CROSS
                    ? GameState.PLAYING_NEXT_MOVE_CROSS
                    : GameState.PLAYING_NEXT_MOVE_NOUGHT;

            final Game game = mGameRepository.getGameByGameId(gameId);
            assertNotNull(game, "game");

            mGameRepository.updateGameState(game, gameState);

            final InviteResponse inviteResponse = new InviteResponse(game.getGameId(), true);
            final OutgoingPayload outgoingPayload = new OutgoingPayload(inviteResponse);
            final OutgoingMessage outgoingMessage = new OutgoingMessage(remotePlayerId, outgoingPayload);
            Communication.newMessage(mContext, outgoingMessage);

            return game;
        } catch (IOException ioException) {
            Log.e(TAG, "Accept invite failed", ioException);
            return null;
        }
    }

    public void declineInvite(@NonNull @Size(min = 1) final String remotePlayerId,
                              @NonNull final String gameId) {
        assertNotNull(remotePlayerId, "remotePlayerId");
        assertNotNull(gameId, "gameId");

        try {
            final Game game = mGameRepository.getGameByGameId(gameId);
            assertNotNull(game, "game");
            mGameRepository.deleteGame(game);

            final InviteResponse inviteResponse = new InviteResponse(gameId, false);
            final OutgoingPayload outgoingPayload = new OutgoingPayload(inviteResponse);
            final OutgoingMessage outgoingMessage = new OutgoingMessage(remotePlayerId, outgoingPayload);
            Communication.newMessage(mContext, outgoingMessage);
        } catch (IOException ioException) {
            Log.e(TAG, "Decline invite failed", ioException);
        }
    }

    @CheckResult
    public long sendMove(@NonNull @Size(min = 1) final String remotePlayerId,
                         @NonNull final Move move) {
        assertNotNull(remotePlayerId, "remotePlayerId");
        assertNotNull(move, "move");

        try {
            final Game game = mGameRepository.getGameByGameId(move.getGameId());
            assertNotNull(game, "game");

            final OutgoingPayload outgoingPayload = new OutgoingPayload(move);
            final OutgoingMessage outgoingMessage = new OutgoingMessage(remotePlayerId, outgoingPayload);
            Communication.newMessage(mContext, outgoingMessage);

            mGameRepository.insertMove(move);

            return outgoingMessage.getCreationDate().getTime();
        } catch (IOException ioException) {
            Log.e(TAG, "Send move failed.", ioException);
            return 0;
        }
    }

    @CheckResult
    public boolean processMove(@NonNull final Move move) {
        assertNotNull(move, "move");

        boolean valid = false;

        final Game game = mGameRepository.getGameByGameId(move.getGameId());

        if (null != game && isGameInProgress(game.getGameState())) {
            final List<Move> allMoves = mGameRepository.getAllMovesByGameId(game.getGameId());

            if (!allMoves.contains(move)) {
                valid = true;
                mGameRepository.insertMove(move);
                mGameRepository.updateGameState(game, move.getGameState());
            } else {
                Log.e(TAG, "Already received: " + move);
            }

        } else {
            Log.e(TAG, "No game found for ID: " + move.getGameId());
        }

        return valid;
    }

    public void sendSurrender(@NonNull final String gameId) {
        assertNotNull(gameId, "game");

        try {
            final Game game = mGameRepository.getGameByGameId(gameId);
            assertNotNull(game, "game"); //should not happen

            final Surrender surrender = new Surrender(game.getGameId());

            final OutgoingPayload outgoingPayload = new OutgoingPayload(surrender);
            final OutgoingMessage outgoingMessage
                    = new OutgoingMessage(game.getRemoteProfileId(), outgoingPayload);

            Communication.newMessage(mContext, outgoingMessage);

            mGameRepository.updateGameState(
                    game, game.getGameSeed() == GameSeed.CROSS ? WIN_NOUGHT : GameState.WIN_CROSS);
        } catch (IOException ioException) {
            Log.e(TAG, "Send Surrender failed.", ioException);
        }
    }

    @CheckResult
    public boolean processSurrender(@NonNull final Surrender surrender) {
        assertNotNull(surrender, "surrender");

        boolean valid = false;

        final Game game = mGameRepository.getGameByGameId(surrender.getGameId());

        if (null != game) {
            if (StateUtils.PLAYING_STATES.contains(game.getGameState())) {
                mGameRepository.updateGameState(
                        game, game.getGameSeed() == GameSeed.CROSS ? GameState.WIN_CROSS : GameState.WIN_NOUGHT);
                valid = true;
            } else if (StateUtils.PENDING_STATES.contains(game.getGameState())) {
                mGameRepository.deleteGame(game);
            }
        } else {
            Log.e(TAG, "No game found for ID: " + surrender.getGameId());
        }

        return valid; //could be game != null
    }

    public GameRepository getGameRepository() {
        return mGameRepository;
    }
}
