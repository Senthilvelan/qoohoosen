package com.qoohoosen.recorder;

import android.os.Handler;
import android.os.Looper;

/**
 * A {@code UiThread} is representation of Ui / main thread.
 */
final class UiThread implements ThreadAction {
    private static final Handler handler = new Handler(Looper.getMainLooper());

    /** executes the {@code Runnable} on UI Thread. */
    @Override
    public void execute(Runnable runnable) {
        handler.post(runnable);
    }
}