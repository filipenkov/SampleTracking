package com.atlassian.upm.rest.async;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response;

import com.atlassian.sal.api.executor.ThreadLocalDelegateExecutorFactory;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.upm.rest.UpmUriBuilder;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapMaker;

import org.springframework.beans.factory.DisposableBean;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.transformValues;

public class AsynchronousTaskManager implements DisposableBean
{
    private static final int NUM_THREADS = 4;

    private final ExecutorService executor;
    private final UpmUriBuilder uriBuilder;
    private final UserManager userManager;

    private final ConcurrentMap<String, TaskFuture<? extends TaskStatus>> tasks;
    private final ConcurrentMap<String, TaskFuture<? extends TaskStatus>> completedTasks;
    private static final int DEFAULT_TTL = 300;

    public AsynchronousTaskManager(ThreadLocalDelegateExecutorFactory factory, UpmUriBuilder uriBuilder, UserManager userManager)
    {
        this(factory, Executors.newFixedThreadPool(NUM_THREADS), uriBuilder, userManager);
    }

    public AsynchronousTaskManager(ThreadLocalDelegateExecutorFactory factory, ExecutorService executor, UpmUriBuilder uriBuilder,
            UserManager userManager)
    {
        this.userManager = checkNotNull(userManager, "userManager");
        this.executor = checkNotNull(factory, "factory").createExecutorService(checkNotNull(executor, "executor"));
        this.uriBuilder = checkNotNull(uriBuilder, "uriBuilder");
        this.tasks = new MapMaker().concurrencyLevel(NUM_THREADS).makeMap();
        this.completedTasks = new MapMaker().concurrencyLevel(NUM_THREADS).expiration(DEFAULT_TTL, TimeUnit.SECONDS).makeMap();
    }

    public <T extends TaskStatus> Response executeAsynchronousTask(final AsynchronousTask<T> task)
    {
        URI uri = uriBuilder.buildAbsolutePendingTaskUri(task.getId());
        task.accept();
        Future<URI> future = executor.submit(new Callable<URI>()
        {
            public URI call() throws Exception
            {
                try
                {
                    return task.call();
                }
                finally
                {
                    completedTasks.put(task.getId(), tasks.get(task.getId()));
                    tasks.remove(task.getId());
                }
            }
        });
        tasks.put(task.getId(), new TaskFuture<T>(task, future));

        AsynchronousTask.Representation<T> representation = task.getRepresentation(uriBuilder);
        return Response.status(Response.Status.ACCEPTED).entity(representation).type(representation.getContentType()).location(uri).build();
    }

    public boolean hasPendingTasks()
    {
        return !getTasks().isEmpty();
    }

    public void destroy()
    {
        executor.shutdown();
    }

    Map<String, AsynchronousTask<? extends TaskStatus>> getTasks()
    {
        return ImmutableMap.copyOf(transformValues(tasks, toTasks));
    }

    private static final Function<TaskFuture<? extends TaskStatus>, AsynchronousTask<? extends TaskStatus>> toTasks = new Function<TaskFuture<? extends TaskStatus>, AsynchronousTask<? extends TaskStatus>>()
    {
        public AsynchronousTask<? extends TaskStatus> apply(TaskFuture<? extends TaskStatus> from)
        {
            return from.task;
        }
    };

    TaskFuture<? extends TaskStatus> getTaskFuture(String taskId)
    {
        TaskFuture<? extends TaskStatus> taskFuture = completedTasks.get(taskId);

        if (taskFuture == null)
        {
            taskFuture = tasks.get(taskId);
        }
        return taskFuture;
    }

    final class TaskFuture<T extends TaskStatus>
    {
        final AsynchronousTask<T> task;
        final Future<URI> future;

        public TaskFuture(AsynchronousTask<T> task, Future<URI> future)
        {
            this.task = task;
            this.future = future;
        }

        public AsynchronousTask.Representation<T> getRepresentation()
        {
            return task.getRepresentation(uriBuilder);
        }

        public URI get() throws InterruptedException, ExecutionException
        {
            return future.get();
        }
    }
}
