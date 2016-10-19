package com.croconaut.ratemebuddy.utils.tasks;

import android.content.Intent;

import com.croconaut.cpt.data.IncomingMessage;


public class PostExecuteValue {
    private final boolean valid;
    private final Intent intent;
    private final IncomingMessage message;

    public PostExecuteValue(boolean valid, Intent intent, IncomingMessage message){
        this.valid = valid;
        this.intent = intent;
        this.message = message;
    }

    public Intent getIntent() {
        return intent;
    }

    public boolean isValid() {
        return valid;
    }

    public IncomingMessage getMessage() {
        return message;
    }
}
