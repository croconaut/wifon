package com.croconaut.tictactoe.utils;

import android.os.AsyncTask;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

public abstract class Async<T> extends AsyncTask<Void, Void, Async.Result<T>> {

    @NonNull
    private final AsyncListener<T> listener;

    public Async(@NonNull final AsyncListener<T> listener) {
        this.listener = listener;
    }

    @Override
    protected Result<T> doInBackground(Void... params) {
        Result<T> result = new Result<>();
        try {
            result.result = call();
        } catch (Throwable e) {
            result.error = e;
        }
        return result;
    }

    @CheckResult
    public abstract T call() throws Throwable;

    @Override
    protected void onPostExecute(Result<T> result) {
        if (result.error != null) {
            callErrorListener(result.error);
        } else {
            callSuccessListener(result.getResult());
        }
    }

    private void callSuccessListener(@NonNull final T body) {
        listener.onSuccess(body);
    }

    private void callErrorListener(@NonNull final Throwable error) {
        listener.onError(error);
    }

    public void run() {
        execute();
    }

    /*package*/ static class Result<T> {
        /*package*/ T result;
        /*package*/ Throwable error;

        /*package*/ T getResult() {
            return result;
        }

        /*package*/ Throwable getError() {
            return error;
        }
    }

    public interface AsyncListener<T> {
        void onSuccess(T result);

        void onError(Throwable error);
    }

}