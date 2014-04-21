package com.atlassian.jira.task;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.util.NotNull;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * An implementation of {@link com.atlassian.jira.task.TaskDescriptor}.
 *
 * @since v3.13
 */
class TaskDescriptorImpl<V> implements TaskDescriptor<V>
{
    private final TaskContext taskContext;
    private final Date submittedTime;
    private final Long taskId;
    private final String description;
    private final User user;
    private final String progressURL;
    private final TaskProgressIndicator taskProgressIndicator;

    private volatile Future<V> future;
    private Date startedTimestamp;
    private Date finishedTimestamp;

    TaskDescriptorImpl(@NotNull final Long taskId, @NotNull final String description, @NotNull final TaskContext taskContext, final User user, final TaskProgressIndicator taskProgressIndicator)
    {
        notNull("taskId", taskId);
        notNull("description", description);
        notNull("taskContext", taskContext);

        this.taskContext = taskContext;
        this.description = description;
        this.taskId = taskId;
        this.user = user;
        submittedTime = new Date();
        this.taskProgressIndicator = taskProgressIndicator;
        future = null;
        progressURL = taskContext.buildProgressURL(taskId);

        initialiseTime(null, null);
    }

    /**
     * THREAD: Thread safe copy contructor for TaskDescriptorImpls. Synchronizes on the copied TaskDescriptorImpl to
     * ensure that its mutable fields are not updated during the copy.
     *
     * @param copiedTaskDescriptor the task descriptor to copy
     */

    TaskDescriptorImpl(@NotNull final TaskDescriptorImpl<V> copiedTaskDescriptor)
    {
        notNull("copiedTaskDescriptor", copiedTaskDescriptor);

        synchronized (copiedTaskDescriptor)
        {
            taskContext = copiedTaskDescriptor.getTaskContext();
            description = copiedTaskDescriptor.getDescription();
            taskId = copiedTaskDescriptor.getTaskId();
            user = copiedTaskDescriptor.getUser();
            submittedTime = copiedTaskDescriptor.getSubmittedTimestamp();
            taskProgressIndicator = copiedTaskDescriptor.getTaskProgressIndicator();
            progressURL = copiedTaskDescriptor.getProgressURL();
            future = copiedTaskDescriptor.future;

            initialiseTime(copiedTaskDescriptor.getStartedTimestamp(), copiedTaskDescriptor.getFinishedTimestamp());
        }
    }

    private synchronized void initialiseTime(final Date startedTimestamp, final Date finishedTimestamp)
    {
        this.startedTimestamp = startedTimestamp;
        this.finishedTimestamp = finishedTimestamp;
    }

    /**
     * THREAD: This is only set once in the {@link com.atlassian.jira.task.TaskDescriptorImpl} during task preperation and wont be
     * null afterwards.  It is package level protected for all round goodness. Making it volatile 
     * provides a happens-before edge between the assigning thread and the executing
     * thread.
     *
     * @param future contains the result of executing the callable. 
     */

    void setFuture(final Future<V> future)
    {
        this.future = future;
    }

    public synchronized long getElapsedRunTime()
    {
        if (startedTimestamp == null)
        {
            return 0;
        }
        if (finishedTimestamp == null)
        {
            return System.currentTimeMillis() - startedTimestamp.getTime();
        }
        else
        {
            return finishedTimestamp.getTime() - startedTimestamp.getTime();
        }
    }

    public V getResult() throws ExecutionException, InterruptedException
    {
        return future.get();
    }

    public synchronized boolean isStarted()
    {
        return startedTimestamp != null;
    }

    public synchronized boolean isFinished()
    {
        return finishedTimestamp != null;
    }

    public synchronized Date getFinishedTimestamp()
    {
        if (finishedTimestamp != null)
        {
            return new Date(finishedTimestamp.getTime());
        }
        else
        {
            return null;
        }
    }

    synchronized void setFinishedTimestamp()
    {
        if (startedTimestamp == null)
        {
            throw new IllegalStateException("Task has not yet started.");
        }
        if (finishedTimestamp != null)
        {
            throw new IllegalStateException("Task has already finished.");
        }

        finishedTimestamp = new Date();
    }

    public synchronized Date getStartedTimestamp()
    {
        if (startedTimestamp != null)
        {
            return new Date(startedTimestamp.getTime());
        }
        else
        {
            return null;
        }
    }

    synchronized void setStartedTimestamp()
    {
        if (startedTimestamp != null)
        {
            throw new IllegalStateException("Task has already started.");
        }

        startedTimestamp = new Date();
    }

    public Date getSubmittedTimestamp()
    {
        return new Date(submittedTime.getTime());
    }

    public Long getTaskId()
    {
        return taskId;
    }

    public User getUser()
    {
        return user;
    }

    public String getDescription()
    {
        return description;
    }

    public TaskContext getTaskContext()
    {
        return taskContext;
    }

    public TaskProgressIndicator getTaskProgressIndicator()
    {
        return taskProgressIndicator;
    }

    public String getProgressURL()
    {
        return progressURL;
    }
}
