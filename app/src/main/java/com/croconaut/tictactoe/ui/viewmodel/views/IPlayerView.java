package com.croconaut.tictactoe.ui.viewmodel.views;


import android.support.annotation.NonNull;

import com.croconaut.ratemebuddy.utils.pojo.profiles.Profile;
import com.croconaut.tictactoe.ui.adapter.model.ProfileGameWrapper;

import java.util.List;
import java.util.Set;

public interface IPlayerView extends IErrorView {
    void showPlayers(@NonNull final Set<ProfileGameWrapper> players);
}
