package com.atlassian.jira.task;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.Functions;
import com.atlassian.jira.util.NotNull;
import com.atlassian.jira.util.Predicate;
import com.atlassian.jira.util.collect.Transformed;
import com.atlassian.jira.util.concurrent.BlockingCounter;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.util.thread.JiraThreadLocalUtils;
import com.atlassian.multitenant.juc.MultiTenantExecutors;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static com.atlassian.jira.util.collect.CollectionUtil.contains;
import static com.atlassian.jira.util.collect.CollectionUtil.filter;
import static com.atlassian.jira.util.collect.CollectionUtil.findFirstMatch;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * An implementation of a {@link com.atlassian.jira.task.TaskManager}. This manager uses an {@link ExecutorService}
 * to run submitted tasks.
 *
 * @since 3.13
 */
public class TaskManagerImpl implements TaskManager
{
    private final static Logger log = Logger.getLogger(TaskManagerImpl.class);

    private static final Function<TaskDescriptorImpl<?>, TaskDescriptor<?>> COPY = new Function<TaskDescriptorImpl<?>, TaskDescriptor<?>>()
    {
        public TaskDescriptor<?> get(final TaskDescriptorImpl<?> input)
        {
            return copy(input);
        }
    };

    private final Map<Long, TaskDescriptorImpl<?>> taskMap = new ConcurrentHashMap<Long, TaskDescriptorImpl<?>>();
    private final AtomicLong taskIdGen = new AtomicLong(0);
    private final ExecutorService executorService;
    private final JiraAuthenticationContext authenticationContext;
    private final BlockingCounter activeThreads = new BlockingCounter();

    public TaskManagerImpl(final JiraAuthenticationContext authenticationContext)
    {
        this(authenticationContext, MultiTenantExecutors.wrap(new ForkedThreadExecutor(5, new TaskManagerThreadFactory())));
    }

    public TaskManagerImpl(final JiraAuthenticationContext authenticationContext, final ExecutorService executorService)
    {
        this.authenticationContext = authenticationContext;
        this.executorService = executorService;
    }

    public <V> TaskDescriptor<V> submitTask(@NotNull final Callable<V> callable, @NotNull final String taskDescription, @NotNull final TaskContext taskContext) throws RejectedExecutionException, AlreadyExecutingException
    {
        notNull("callable", callable);
        notNull("taskContext", taskContext);
        notNull("taskDescription", taskDescription);

        final Long taskId = taskIdGen.incrementAndGet();

        TaskProgressAdapter taskProgressAdapter = null;
        if (callable instanceof ProvidesTaskProgress)
        {
            taskProgressAdapter = new TaskProgressAdapter();
        }

        final TaskDescriptorImpl<V> taskDescriptor = new TaskDescriptorImpl<V>(taskId, taskDescription, taskContext, authenticationContext.getLoggedInUser(),
            taskProgressAdapter);
        final FutureTask<V> futureTask = new FutureTask<V>(new TaskCallableDecorator<V>(callable, taskDescriptor, authenticationContext, activeThreads));
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
            @SuppressWarnings("unchecked")
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
            //
            // check the TaskContext to see if we have any "live" tasks with that context
            final TaskDescriptor<?> testTaskDescriptor = getLiveTask(taskContext);
            if (testTaskDescriptor != null)
            {
                throw new AlreadyExecutingException(testTaskDescriptor, "A task with this context has already been submitted");
            }
            //
            // add the task to out set of known tasks
            taskMap.put(taskId, taskDescriptor);
        }

        //
        // begin execution of the task (soon)
        submitTaskInternal(futureTask);

        // return an immutable  clone
        return new TaskDescriptorImpl<V>(taskDescriptor);
    }

    public boolean removeTask(final Long taskId)
    {
        return taskMap.remove(taskId) != null;
    }

    /**
     * Called to submit the task to an ExecutorService.  Made into a package level method
     * to allow for better unit testing
     *
     * @param futureTask the callable future that wraps the task's callable.
     */

    void submitTaskInternal(final FutureTask<?> futureTask)
    {
        executorService.submit(futureTask);
    }

    public boolean shutdownAndWait(final long waitSeconds)
    {
        if (waitSeconds < 0)
        {
            throw new IllegalArgumentException("waitSeconds must be >= 0");
        }

        executorService.shutdown();
        boolean val;
        try
        {
            val = executorService.awaitTermination(waitSeconds, TimeUnit.SECONDS);
        }
        catch (final InterruptedException e)
        {
            val = executorService.isTerminated();
        }

        logRunningTasksOnShutdown();
        return val;
    }

    public void shutdownNow()
    {
        executorService.shutdownNow();
    }

    public boolean awaitUntilActiveTasksComplete(long seconds)
    {
        try
        {
            return activeThreads.await(seconds, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
            return activeThreads.getCount() == 0;
        }
    }

    public <V> TaskDescriptor<V> getLiveTask(@NotNull final TaskContext taskContext)
    {
        notNull("taskContext", taskContext);

        @SuppressWarnings("unchecked")
        final TaskDescriptor<V> result = (TaskDescriptor<V>) findFirstTask(new ActiveMatcher(taskContext));
        return result;
    }

    public <V> TaskDescriptor<V> getTask(final Long taskId)
    {
        if (taskId == null)
        {
            return null;
        }
        @SuppressWarnings("unchecked")
        final TaskDescriptorImpl<V> input = (TaskDescriptorImpl<V>) taskMap.get(taskId);
        return copy(input);
    }

    public boolean hasLiveTaskWithContext(@NotNull final TaskContext taskContext)
    {
        return hasTask(new ActiveMatcher(taskContext));
    }

    public boolean hasTaskWithContext(@NotNull final TaskContext taskContext)
    {
        notNull("taskContext", taskContext);
        return hasTask(new TaskMatcher()
        {
            public boolean match(final TaskDescriptor<?> descriptor)
            {
                return taskContext.equals(descriptor.getTaskContext());
            }
        });
    }

    public TaskDescriptor<?> findFirstTask(@NotNull final TaskMatcher matcher)
    {
        return findFirstMatch(taskMap.values(), new TaskMatcherPredicate(matcher));
    }

    public Collection<TaskDescriptor<?>> findTasks(final TaskMatcher matcher)
    {
        return findTasksInternal(matcher);
    }

    public Collection<TaskDescriptor<?>> getAllTasks()
    {
        return sortIntoIdOrder(Transformed.collection(taskMap.values(), COPY));
    }

    public Collection<TaskDescriptor<?>> getLiveTasks()
    {
        return sortIntoIdOrder(findTasksInternal(new TaskMatcher()
        {
            public boolean match(final TaskDescriptor<?> descriptor)
            {
                return !descriptor.isFinished();
            }
        }));
    }

    private Collection<TaskDescriptor<?>> findTasksInternal(final TaskMatcher matcher)
    {
        notNull("matcher", matcher);
        return Transformed.collection(filter(taskMap.values(), new TaskMatcherPredicate(matcher)),
            Functions.<TaskDescriptor<?>, TaskDescriptorImpl<?>> coerceToSuper());
    }

    private boolean hasTask(final TaskMatcher matcher)
    {
        return contains(taskMap.values(), new TaskMatcherPredicate(matcher));
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
     * Sorts a list of TaskDescriptor objects in taskId order.
     *
     * @param input the list of TaskDescriptor objects
     * @return the list sorted.
     */

    private List<TaskDescriptor<?>> sortIntoIdOrder(final Collection<TaskDescriptor<?>> input)
    {
        final List<TaskDescriptor<?>> result = new ArrayList<TaskDescriptor<?>>(input);
        Collections.sort(result, new Comparator<TaskDescriptor<?>>()
        {
            public int compare(final TaskDescriptor<?> o1, final TaskDescriptor<?> o2)
            {
                return o1.getTaskId().compareTo(o2.getTaskId());
            }
        });
        return result;
    }

    private void logRunningTasksOnShutdown()
    {
        final Collection<TaskDescriptor<?>> liveTasks = getLiveTasks();
        if (!liveTasks.isEmpty())
        {
            log.warn("Shutting down task manager while the following tasks are still executing:");

            for (final TaskDescriptor<?> taskDescriptor : liveTasks)
            {
                final StringBuffer sb = new StringBuffer();
                sb.append("Task Id ");
                sb.append(taskDescriptor.getTaskId());
                final TaskProgressEvent event = taskDescriptor.getTaskProgressIndicator() == null ? null : taskDescriptor.getTaskProgressIndicator().getLastProgressEvent();
                if (event != null)
                {
                    sb.append(" - ");
                    sb.append(event.getTaskProgress());
                    sb.append("% complete");
                }
                sb.append(" - ");
                sb.append(taskDescriptor.getDescription());
                log.warn(sb);
            }
        }
    }

    /**
     * THREAD SAFETY :
     * <p/>
     * This wraps the task Callable and ensures that the TaskDescriptor is updated
     * in regards to start and finish times.  It also clears the reference to the original Callable
     * to help with memory cleanup when the Callable is finished and has returned a result.
     */
    private static class TaskCallableDecorator<V> implements Callable<V>
    {
        private final AtomicReference<Callable<V>> actualCallableRef;
        private final TaskDescriptorImpl<V> taskDescriptor;
        private final JiraAuthenticationContext context;
        private final BlockingCounter counter;

        private TaskCallableDecorator(final Callable<V> callable, final TaskDescriptorImpl<V> taskDescriptor, final JiraAuthenticationContext context, final BlockingCounter counter)
        {
            this.counter = counter;
            Assertions.notNull("callable", callable);
            Assertions.notNull("taskDescriptor", taskDescriptor);
            Assertions.notNull("context", context);

            actualCallableRef = new AtomicReference<Callable<V>>(callable);
            this.taskDescriptor = taskDescriptor;
            this.context = context;
        }

        public V call() throws Exception
        {
            preCallSetup();

            taskDescriptor.setStartedTimestamp();
            counter.up();
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
            context.setLoggedInUser(taskDescriptor.getUser());
        }

        private void postCallTearDown()
        {
            taskDescriptor.setFinishedTimestamp();
            counter.down();

            JiraThreadLocalUtils.postCall(log, new JiraThreadLocalUtils.ProblemDeterminationCallback()
            {
                public void onOpenTransaction()
                {
                    log.error("The task '" + taskDescriptor.getDescription() + "' has left an open database transaction in play.");
                }
            });
        }
    }

    /** Internal matcher that looks for active projects. */

    private static class ActiveMatcher implements TaskMatcher
    {
        private final TaskContext taskContext;

        public ActiveMatcher(final TaskContext taskContext)
        {
            this.taskContext = taskContext;
        }

        public boolean match(final TaskDescriptor<?> descriptor)
        {
            return !descriptor.isFinished() && taskContext.equals(descriptor.getTaskContext());
        }
    }

    /** Internal thread factory class for threads created when executing tasks. */

    private static class TaskManagerThreadFactory implements ThreadFactory
    {
        private final AtomicLong threadId = new AtomicLong(0);

        public Thread newThread(final Runnable runnable)
        {
            final Thread t = new Thread(runnable, "JiraTaskExectionThread-" + threadId.incrementAndGet());
            t.setDaemon(true);
            return t;
        }
    }

    /**
     * Predicate matcher, always synchronises on the TaskDescriptor while matching.
     */
    private final class TaskMatcherPredicate implements Predicate<TaskDescriptor<?>>
    {
        final TaskMatcher matcher;

        TaskMatcherPredicate(@NotNull final TaskMatcher matcher)
        {
            this.matcher = notNull("matcher", matcher);
        }

        public boolean evaluate(final TaskDescriptor<?> input)
        {
            synchronized (input)
            {
                return matcher.match(input);
            }
        }
    }
}
