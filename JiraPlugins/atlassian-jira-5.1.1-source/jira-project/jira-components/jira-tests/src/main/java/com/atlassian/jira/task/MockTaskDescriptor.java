package com.atlassian.jira.task;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.task.TaskContext;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.task.TaskProgressIndicator;

import java.util.Date;
import java.util.concurrent.ExecutionException;

/**
* @since v5.1
*/
public class MockTaskDescriptor<V> implements TaskDescriptor<V>
{
    private V result;
    private Date submittedTime;
    private Date startedTime;
    private Date finishedTime;
    private TaskProgressIndicator taskProgressIndicator;
    private String description;
    private Long taskId;
    private long elapsedRunTime;
    private TaskContext taskContext;
    private User user;

    public MockTaskDescriptor()
    {
        clear();
    }

    public void clear()
    {
        elapsedRunTime = 0;
        taskId = null;
        result = null;
        submittedTime = null;
        startedTime = null;
        finishedTime = null;
        taskProgressIndicator = null;
        description = null;
        taskContext = null;
        user = null;
    }

    public V getResult() throws ExecutionException, InterruptedException
    {
        return result;
    }

    public boolean isStarted()
    {
        return startedTime != null;
    }

    public boolean isFinished()
    {
        return finishedTime != null;
    }

    public Long getTaskId()
    {
        return taskId;
    }

    public Date getStartedTimestamp()
    {
        return startedTime;
    }

    public Date getFinishedTimestamp()
    {
        return finishedTime;
    }

    public Date getSubmittedTimestamp()
    {
        return submittedTime;
    }

    public long getElapsedRunTime()
    {
        return elapsedRunTime;
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

    public String getProgressURL()
    {
        return "/userUrl?user=";
    }

    public TaskProgressIndicator getTaskProgressIndicator()
    {
        return taskProgressIndicator;
    }

    public void setResult(final V result)
    {
        this.result = result;
    }

    public void setSubmittedTime(final Date submittedTime)
    {
        this.submittedTime = submittedTime;
    }

    public void setStartedTime(final Date startedTime)
    {
        this.startedTime = startedTime;
    }

    public void setFinishedTime(final Date finishedTime)
    {
        this.finishedTime = finishedTime;
    }

    public void setTaskProgressIndicator(final TaskProgressIndicator taskProgressIndicator)
    {
        this.taskProgressIndicator = taskProgressIndicator;
    }

    public void setDescription(final String description)
    {
        this.description = description;
    }

    public void setTaskId(final Long taskId)
    {
        this.taskId = taskId;
    }

    public void setElapsedRunTime(final long elapsedRunTime)
    {
        this.elapsedRunTime = elapsedRunTime;
    }

    public void setTaskContext(final TaskContext taskContext)
    {
        this.taskContext = taskContext;
    }

    public void setUser(final User user)
    {
        this.user = user;
    }
}
