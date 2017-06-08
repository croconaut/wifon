package com.croconaut.tictactoe.ui.activities;


import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;

import com.croconaut.ratemebuddy.activities.WifonActivity;

public abstract class BaseActivity extends WifonActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());
    }

    @LayoutRes
    abstract int getLayoutResId();
}
