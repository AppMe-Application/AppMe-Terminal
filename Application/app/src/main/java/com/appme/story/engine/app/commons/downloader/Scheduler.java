package com.appme.story.engine.app.commons.downloader;

import android.support.annotation.NonNull;

/**
 * The interface definition for the threading
 * scheme used by Bonsai. Classes implementing
 * this interface are expected to correctly
 * execute {@link Runnable} on a thread of
 * their choosing.
 */
@SuppressWarnings("WeakerAccess")
public interface Scheduler {

    /**
     * Run the {@link Runnable} on the thread
     * defined by the class implementing this
     * class.
     *
     * @param runnable the task to execute.
     */
    void execute(@NonNull Runnable runnable);

}
