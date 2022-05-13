package com.appme.story.engine.app.commons.downloader;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

/**
 * A special {@link Scheduler} that creates a {@link Handler}
 * from a certain {@link Looper} and executes tasks on the
 * thread associated with it.
 */
class ThreadScheduler implements Scheduler {

    @NonNull private final Handler handler;

    ThreadScheduler(@NonNull Looper looper) {
        handler = new Handler(looper);
    }

    @Override
    public synchronized void execute(@NonNull Runnable command) {
        handler.post(command);
    }
}
