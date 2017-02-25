package com.croconaut.tictactoe.ui.viewmodel.views;

import android.support.annotation.NonNull;


public interface IErrorView extends IAppDataView {
    void error(@NonNull String errorMsg);
}
