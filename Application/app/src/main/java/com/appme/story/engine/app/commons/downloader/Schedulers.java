package com.appme.story.engine.app.commons.downloader;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * A class of default {@link Scheduler} provided
 * for use with {@link Stream}, {@link Single},
 * and {@link Completable}.
 * <p>
 * If the options available here are not sufficient,
 * implement {@link Scheduler} and create your own.
 */
@SuppressWarnings("WeakerAccess")
public final class Schedulers {

    @Nullable private static Scheduler mainScheduler;
    @Nullable private static Scheduler workerScheduler;
    @Nullable private static Scheduler ioScheduler;
    @Nullable private static Scheduler immediateScheduler;

    private Schedulers() {
        throw new UnsupportedOperationException("This class is not instantiable");
    }

    /**
     * A worker scheduler. Backed by a fixed
     * thread pool containing 4 thread.
     */
    private static class WorkerScheduler implements Scheduler {

        private final Executor worker = Executors.newFixedThreadPool(4);

        @Override
        public void execute(@NonNull Runnable command) {
            worker.execute(command);
        }
    }

    /**
     * A single threaded scheduler. Backed by a
     * single thread in an executor.
     */
    private static class SingleThreadedScheduler implements Scheduler {

        private final Executor singleThreadExecutor = Executors.newSingleThreadExecutor();

        @Override
        public void execute(@NonNull Runnable command) {
            singleThreadExecutor.execute(command);
        }
    }

    /**
     * A scheduler backed by an executor.
     */
    private static class ExecutorScheduler implements Scheduler {

        @NonNull
        private final Executor backingExecutor;

        public ExecutorScheduler(@NonNull Executor executor) {
            backingExecutor = executor;
        }

        @Override
        public void execute(@NonNull Runnable command) {
            backingExecutor.execute(command);
        }
    }

    /**
     * A scheduler that executes tasks immediately.
     */
    private static class ImmediateScheduler implements Scheduler {
        @Override
        public void execute(@NonNull Runnable runnable) {
            runnable.run();
        }
    }

    /**
     * Creates a scheduler from an executor instance.
     *
     * @param executor the executor to use to create
     *                 the Scheduler.
     * @return a valid Scheduler backed by an executor.
     */
    @NonNull
    public static Scheduler from(@NonNull Executor executor) {
        return new ExecutorScheduler(executor);
    }

    /**
     * Creates a scheduler from a handler. The scheduler
     * will post tasks to the looper associated with the
     * handler.
     *
     * @param handler the handler used to create the Scheduler.
     * @return a valid Scheduler backed by a handler.
     */
    @NonNull
    public static Scheduler from(@NonNull Handler handler) {
        return new ThreadScheduler(handler.getLooper());
    }

    /**
     * A scheduler that executes tasks synchronously
     * on the calling thread. If you use this scheduler
     * as the subscribe-on scheduler, then the work will
     * execute synchronously on the current thread. If
     * you use this scheduler as the observe-on thread,
     * then you will receive events on whatever thread
     * was used as that subscribe-on thread.
     *
     * @return a synchronous scheduler.
     */
    @NonNull
    public static Scheduler immediate() {
        if (immediateScheduler == null) {
            immediateScheduler = new ImmediateScheduler();
        }
        return immediateScheduler;
    }

    /**
     * Creates a new Scheduler that
     * creates a new thread and does
     * all work on it.
     *
     * @return a scheduler associated
     * with a new single thread.
     */
    @NonNull
    public static Scheduler newSingleThreadedScheduler() {
        return new SingleThreadedScheduler();
    }

    /**
     * The worker thread Scheduler, will
     * execute work on any one of multiple
     * threads.
     *
     * @return a non-null Scheduler.
     */
    @NonNull
    public static Scheduler worker() {
        if (workerScheduler == null) {
            workerScheduler = new WorkerScheduler();
        }
        return workerScheduler;
    }

    /**
     * The main thread. All work will
     * be done on the single main thread.
     *
     * @return a non-null Scheduler that does work on the main thread.
     */
    @NonNull
    public static Scheduler main() {
        if (mainScheduler == null) {
            mainScheduler = new ThreadScheduler(Looper.getMainLooper());
        }
        return mainScheduler;
    }

    /**
     * The io scheduler. All work will be
     * done on a single thread.
     *
     * @return a non-null Scheduler that does
     * work on a single thread off the main thread.
     */
    @NonNull
    public static Scheduler io() {
        if (ioScheduler == null) {
            ioScheduler = new SingleThreadedScheduler();
        }
        return ioScheduler;
    }
}
