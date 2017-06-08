package com.croconaut.tictactoe.ui.viewmodel.views;


import android.support.annotation.Nullable;

import com.croconaut.ratemebuddy.AppData;
import com.croconaut.ratemebuddy.utils.pojo.profiles.Profile;

import eu.inloop.viewmodel.IView;

/*package*/ interface IAppDataView extends IView {
    AppData getAppData();

    void updateProfile(@Nullable final Profile profile);
}
