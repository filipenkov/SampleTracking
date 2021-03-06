package com.atlassian.jira.task;

import com.atlassian.jira.util.NotNull;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.util.thread.JiraThreadLocalUtils;
import com.atlassian.multitenant.juc.MultiTenantExecutors;
import org.apache.log4j.Logger;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * An implementation of a {@link ImportTaskManager}. This manager uses an {@link java.util.concurrent.ExecutorService} to run
 * submitted tasks.
 *
 * It should have *no* external dependencies and should *not* be injectable!
 *
 * @since 4.4
 */
public class ImportTaskManagerImpl implements ImportTaskManager
{
    private final static Logger log = Logger.getLogger(ImportTaskManagerImpl.class);

    private final AtomicReference<TaskDescriptorImpl<?>> currentTask = new AtomicReference<TaskDescriptorImpl<?>>();
    private final ExecutorService executorService;

    public ImportTaskManagerImpl()
    {
        this.executorService = MultiTenantExecutors.wrap(Executors.newSingleThreadExecutor(new TaskManagerThreadFactory()));
    }

    public <V> TaskDescriptor<V> submitTask(@NotNull final Callable<V> callable, String taskName)
            throws RejectedExecutionException, AlreadyExecutingException
    {
        notNull("callable", callable);

        TaskProgressAdapter taskProgressAdapter = null;
        if (callable instanceof ProvidesTaskProgress)
        {
            taskProgressAdapter = new TaskProgressAdapter();
        }

        final TaskDescriptorImpl<V> taskDescriptor = new TaskDescriptorImpl<V>(1L, taskName, new NoOpTaskContext(), null, taskProgressAdapter);
        final FutureTask<V> futureTask = new FutureTask<V>(new TaskCallableDecorator<V>(callable, taskDescriptor));
        taskDescriptor.setFuture(futureTask);

        // can they provide progress feed back
        if (callable instanceof ProvidesTaskProgress)
        {
            taskProgressAdapter.setTaskDescriptor(taskDescriptor);
            ((ProvidesTaskProgress) callable).setTaskProgressSink(taskProgressAdapter);
        }
        // do they want task descriptor info
        if (callable instanceof RequiresTaskInformation)
        {
            @SuppressWarnings ("unchecked")
            final RequiresTaskInformation<V> requiresTaskInformation = (RequiresTaskInformation<V>) callable;
            requiresTaskInformation.setTaskDescriptor(taskDescriptor);
        }

        // THREAD SAFETY :
        //the only thing we are worried about is making sure that two tasks with the same
        //Context do not start. There are race conditions here (i.e. a task can complete
        //between the hasLiveTaskWithContext() and taskMap.put) but this does not
        //matter because the user can just retry the task.
        synchronized (this)
        {
            if (currentTask.get() != null)
            {
                throw new AlreadyExecutingException(currentTask.get(), "A task with this context has already been submitted");
            }
            currentTask.set(taskDescriptor);
        }

        // begin execution of the task (soon)
        executorService.submit(futureTask);

        // return an immutable  clone
        return new TaskDescriptorImpl<V>(taskDescriptor);
    }

    public void shutdownNow()
    {
        executorService.shutdownNow();
    }

    @SuppressWarnings ("unchecked")
    public <V> TaskDescriptor<V> getTask()
    {
        return (TaskDescriptor<V>) copy(currentTask.get());
    }

    private static <V> TaskDescriptor<V> copy(final TaskDescriptorImpl<V> input)
    {
        if (input == null)
        {
            return null;
        }
        return new TaskDescriptorImpl<V>(input);
    }


    /**
     * THREAD SAFETY :
     * <p/>
     * This wraps the task Callable and ensures that the TaskDescriptor is updated in regards to start and finish times.
     * It also clears the reference to the original Callable to help with memory cleanup when the Callable is finished
     * and has returned a result.
     */
    private static class TaskCallableDecorator<V> implements Callable<V>
    {
        private final AtomicReference<Callable<V>> actualCallableRef;
        private final TaskDescriptorImpl<V> taskDescriptor;

        private TaskCallableDecorator(final Callable<V> callable, final TaskDescriptorImpl<V> taskDescriptor)
        {
            Assertions.notNull("callable", callable);
            Assertions.notNull("taskDescriptor", taskDescriptor);

            actualCallableRef = new AtomicReference<Callable<V>>(callable);
            this.taskDescriptor = taskDescriptor;
        }

        public V call() throws Exception
        {
            preCallSetup();

            taskDescriptor.setStartedTimestamp();
            try
            {
                //We want the executor to forget about the callable so that it can
                //be garbage collected as we are only interested in the results. This also
                //creates a happens-before edge between the thread that created the task
                //and the thread that will execute it. This will make assignments on the creating
                //thead visible to the executing thread.
                final Callable<V> actualCallable = actualCallableRef.getAndSet(null);
                if (actualCallable != null)
                {
                    return actualCallable.call();
                }
                // really really unlikely in fact we reckon improssible
                throw new IllegalStateException("Callable executed twice.");
            }
            finally
            {
                postCallTearDown();
            }
        }

        private void preCallSetup()
        {
            JiraThreadLocalUtils.preCall();
        }

        private void postCallTearDown()
        {
            taskDescriptor.setFinishedTimestamp();

            JiraThreadLocalUtils.postCall(log, new JiraThreadLocalUtils.ProblemDeterminationCallback()
            {
                public void onOpenTransaction()
                {
                    log.error("The task '" + taskDescriptor.getDescription() + "' has left an open database transaction in play.");
                }
            });
        }
    }

    /**
     * Internal thread factory class for threads created when executing tasks.
     */
    private static class TaskManagerThreadFactory implements ThreadFactory
    {
        private final AtomicLong threadId = new AtomicLong(0);

        public Thread newThread(final Runnable runnable)
        {
            final Thread t = new Thread(runnable, "JiraImportTaskExecutionThread-" + threadId.incrementAndGet());
            t.setDaemon(true);
            return t;
        }
    }


    static class NoOpTaskContext implements TaskContext
    {
        @Override
        public String buildProgressURL(Long taskId)
        {
            return "";
        }
    }
}
