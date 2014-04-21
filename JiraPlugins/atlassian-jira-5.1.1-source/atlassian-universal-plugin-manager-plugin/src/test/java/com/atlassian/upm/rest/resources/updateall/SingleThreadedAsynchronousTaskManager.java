package com.atlassian.upm.rest.resources.updateall;

import java.net.URI;
import java.util.concurrent.Executors;

import javax.ws.rs.core.Response;

import com.atlassian.sal.api.executor.ThreadLocalDelegateExecutorFactory;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.upm.rest.UpmUriBuilder;
import com.atlassian.upm.rest.async.AsynchronousTask;
import com.atlassian.upm.rest.async.AsynchronousTaskManager;
import com.atlassian.upm.rest.async.TaskStatus;

public class SingleThreadedAsynchronousTaskManager extends AsynchronousTaskManager
{
    private AsynchronousTask<? extends TaskStatus> currentlyRunning;
    private boolean wasTaskSubmitted = false;

    public SingleThreadedAsynchronousTaskManager(ThreadLocalDelegateExecutorFactory factory, UpmUriBuilder uriBuilder, UserManager userManager)
    {
        super(factory, Executors.newSingleThreadExecutor(), uriBuilder, userManager);
    }

    @Override
    public <T extends TaskStatus> Response executeAsynchronousTask(final AsynchronousTask<T> task)
    {
        wasTaskSubmitted = true;
        task.accept();
        AsynchronousTask<T> realTask = new AsynchronousTask<T>(task.getType(), task.getUsername())
        {
            public URI call() throws Exception
            {
                currentlyRunning = task;
                return task.call();
            }

            public void accept()
            {
                task.accept();
            }

            public Representation<T> getRepresentation(UpmUriBuilder uriBuilder)
            {
                return task.getRepresentation(uriBuilder);
            }
        };
        return super.executeAsynchronousTask(realTask);
    }

    public AsynchronousTask<? extends TaskStatus> getCurrentlyRunningTask()
    {
        return currentlyRunning;
    }

    public boolean wasTaskSubmitted()
    {
        return wasTaskSubmitted;
    }
}
