package com.atlassian.streams.spi;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * A {@code Callable} which will handle cancellation of the task without a Thread interrupt being used.
 *
 * @param <T> the result type of method call
 */
public interface CancellableTask<T> extends Callable<T>
{
    /**
     * Called when {@link Future#cancel(boolean)} is called.  It is the responsibility of the implementor to determine
     * the best strategy for cancelling the task.  If the task can be cancelled, {@link Result#CANCELLED} should be
     * returned.  If the task needs to be interrupted to be cancelled, because it is waiting on IO or doing some other
     * blocking task, {@link Result#INTERRUPT} should be returned and the thread will be interrupted - note that the
     * task is still responsible for detecting the interrupt once the blocking call is interrupted and handling it
     * correctly.  If the task cannot be cancelled normally or through an interrupt, {@link Result#CANNOT_CANCEL} should
     * be returned.
     *
     * @return if the cancel has completed or not or if the thread should be interrupted
     */
    Result cancel();

    /**
     * The result of calling {@link CancellableTask#cancel}, indicating the state of the task.
     */
    enum Result
    {
        /**
         * Returned by {@link CancellableTask#cancel()} if the task can be cancelled normally, without a Thread interrupt.
         */
        CANCELLED,

        /**
         * Returned by {@link CancellableTask#cancel()} if the task cannot be cancelled normally or with a Thread interrupt.
         */
        CANNOT_CANCEL,

        /**
         * Returned by {@link CancellableTask#cancel()} if the task can be cancelled, but is blocking on IO or a lock
         * and needs the Thread to be interrupted.
         */
        INTERRUPT;
    }
}
