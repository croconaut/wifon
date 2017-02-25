package com.croconaut.tictactoe.ui.adapter.model;


import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.croconaut.ratemebuddy.utils.pojo.profiles.Profile;
import com.croconaut.tictactoe.payload.games.Game;
import com.croconaut.tictactoe.utils.StateUtils;

import java.util.List;

import static com.croconaut.tictactoe.utils.StateUtils.PLAYABLE_STATES;
import static com.croconaut.tictactoe.utils.StateUtils.PLAYING_STATES;

public final class ProfileGameWrapper implements Comparable<ProfileGameWrapper> {

    @NonNull
    private Profile mProfile;

    @NonNull
    private List<Game> mGameList;

    public ProfileGameWrapper(@NonNull final Profile profile, @NonNull final List<Game> gameList) {
        this.mGameList = gameList;
        this.mProfile = profile;
    }

    @NonNull
    public Profile getProfile() {
        return mProfile;
    }

    @NonNull
    public List<Game> getGameList() {
        return mGameList;
    }

    @CheckResult
    @Nullable
    public Game getGameInProgress() {
        return StateUtils.isGameInState(PLAYABLE_STATES, mGameList);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ProfileGameWrapper that = (ProfileGameWrapper) o;

        if (!mProfile.equals(that.mProfile)) return false;
        return mGameList.equals(that.mGameList);

    }

    @Override
    public int hashCode() {
        int result = mProfile.hashCode();
        result = 31 * result + mGameList.hashCode();
        return result;
    }

    @Override
    public int compareTo(@NonNull ProfileGameWrapper other) {
        final boolean hasPlayer1GameInProgress =
                StateUtils.isGameInState(PLAYING_STATES, this.getGameList()) != null;
        final boolean hasPlayer2GameInProgress =
                StateUtils.isGameInState(PLAYING_STATES, other.getGameList()) != null;

        if (hasPlayer1GameInProgress && !hasPlayer2GameInProgress) {
            return -1;
        } else if (!hasPlayer1GameInProgress && hasPlayer2GameInProgress) {
            return 1;
        }

        final boolean hasPlayer1PendingGame =
                StateUtils.isGameInState(PLAYABLE_STATES, this.getGameList()) != null;
        final boolean hasPlayer2PendingGame =
                StateUtils.isGameInState(PLAYABLE_STATES, other.getGameList()) != null;

        final int gamesPlayer1 = this.getGameList().size();
        final int gamesPlayer2 = other.getGameList().size();

        if (hasPlayer1PendingGame && hasPlayer2PendingGame) {
            if (gamesPlayer1 == gamesPlayer2) {
                return this.getProfile().getName().compareTo(other.getProfile().getName());
            } else {
                return gamesPlayer2 - gamesPlayer1;
            }
        } else if (!hasPlayer1PendingGame && hasPlayer2PendingGame) {
            return 1;
        } else if (hasPlayer1PendingGame) {
            return -1;
        } else {
            if (gamesPlayer1 == gamesPlayer2) {
                return this.getProfile().getName().compareTo(other.getProfile().getName());
            } else {
                return gamesPlayer2 - gamesPlayer1;
            }
        }
    }

}
