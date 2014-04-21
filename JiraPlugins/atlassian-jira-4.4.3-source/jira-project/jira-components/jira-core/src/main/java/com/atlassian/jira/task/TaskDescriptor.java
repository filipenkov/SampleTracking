package com.atlassian.jira.task;

import com.atlassian.crowd.embedded.api.User;

import java.util.Date;
import java.util.concurrent.ExecutionException;

/**
 * TaskDescriptor decribes the state of a long running task.
 *
 * @param V the result type.
 * @since v3.13
 */
public interface TaskDescriptor<V>
{
    /**
     * This returns the result of the long running task.  WARNING: This will BLOCK if the task has not finished
     * and will not return until it has finished executing.
     *
     * @return the result of the long running task or null if it has not return value.
     * @throws ExecutionException   if an uncaught exception is thrown from the task's callable.
     * @throws InterruptedException if the calling thread is interrupted while waiting for the result.
     */
    V getResult() throws ExecutionException, InterruptedException;

    /**
     * True if the task has been started.
     *
     * @return true if the task has been started.
     */
    boolean isStarted();

    /**
     * Tells if caller if the task has finished running or not.
     *
     * @return true if the task has finished running.
     */
    boolean isFinished();

    /**
     * Reuturn the identifier for this task. This is only unique in the current execution of the JVM.
     *
     * @return The unique id of the task
     */
    Long getTaskId();

    /**
     * Return the date when the task was started.
     *
     * @return the time that task was started. <code>null</code> will be returned if the task has not started executing.
     */
    Date getStartedTimestamp();

    /**
     * Return the date when the task was finished.
     *
     * @return the time that task finished executing. <code>null</code> will be returned if the task has not finished
     *         executing.
     */
    Date getFinishedTimestamp();

    /**
     * Return the date when the task was submitted.
     *
     * @return the time that task was submited to the {@link com.atlassian.jira.task.TaskManager}. A <code>null</code>
     *         value will never be returned as the task will always have a submission time.
     */
    Date getSubmittedTimestamp();

    /**
     * This returns number of milliseconds the task has been running for. Will return zero if the task
     * has not started. When the task has started but not finished, it will return the the difference between
     * the current time and the time was started (i.e. it will change). When the task has finished, it will
     * return the difference between the start time and the end time (i.e. it will not change). 
     *
     * @return the elapsed run time in milliseconds.
     */
    long getElapsedRunTime();

    /**
     * Return the user that started to task.
     *
     * @return the user that caused the task to be submitted.  This may be null.
     */
    User getUser();

    /**
     * Return the description of the task passed when it was created.
     *
     * @return a meaningful description of the task
     */
    String getDescription();

    /**
     * Return he context of task.  Code that starts long running tasks can implement their own variants of this.
     *
     * @return the context of the task. This method will never return <code>null</code> as a task must always
     *         have a context.
     */
    TaskContext getTaskContext();

    /**
     * Returns the URL that displays progress on this task.  It is built using the {@link com.atlassian.jira.task.TaskContext}.
     *
     * @return the URL that displays progress for this task. <code>null</code> cannot be returned.
     */
    String getProgressURL();

    /**
     * Return the {@link TaskProgressIndicator} associated with the task. A task will only have an indictator if its
     * callable implements the {@link com.atlassian.jira.task.ProvidesTaskProgress} interface.
     *
     * @return the {@link TaskProgressIndicator} associated with the task or <code>null</code> if there isn't one.
     */
    TaskProgressIndicator getTaskProgressIndicator();
}
