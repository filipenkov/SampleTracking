package com.atlassian.jira.web.monitor.watcher;

import com.atlassian.jira.web.monitor.Request;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * This class will periodically monitor a map of active requests, and will tell you when one of those requests is
 * overdue.
 * <p/>
 * This class is <em>thread-safe</em>.
 *
 * @see OverdueRequestListener
 * @since v4.3
 */
public class RequestWatcher
{
    /**
     * Logger for this RequestWatcher instance.
     */
    private final Logger log = LoggerFactory.getLogger(RequestWatcher.class);

    /**
     * A reference to the ExecutorService that this class will use to monitor the active requests.
     */
    private final ScheduledExecutorService executorService;

    /**
     * A List of OverdueRequestListener. This list should be updated infrequently, but read frequently.
     */
    private final List<OverdueRequestListener> overdueListeners = new CopyOnWriteArrayList<OverdueRequestListener>();

    /**
     * The future that is currently scheduled, or null if none is scheduled.
     */
    private final AtomicReference<ScheduledFuture<?>> scheduledFuture = new AtomicReference<ScheduledFuture<?>>();

    /**
     * The threshold beyond which a request is deemed to be overdue
     */
    private final long overdueThreshold;

    /**
     * This callback is used to determine the active requests.
     */
    private final ActiveRequestsCallback callback;

    /**
     * Creates a new RequestWatcher.
     *
     * @param overdueThreshold the threshold beyond which a request is deemed overdue, in ms
     * @param callback a RequestCallback
     */
    public RequestWatcher(long overdueThreshold, ActiveRequestsCallback callback)
    {
        this.executorService = newSingleThreadExecutor();
        this.overdueThreshold = overdueThreshold;
        this.callback = callback;
    }

    /**
     * Adds an OverdueRequestListener to this instance's list of listeners.
     *
     * @param overdueRequestListener a OverdueRequestListener
     */
    public void addOverdueRequestListener(OverdueRequestListener overdueRequestListener)
    {
        overdueListeners.add(overdueRequestListener);
        log.debug("[{}] Added overdue request listener: {}", this, overdueRequestListener);
    }

    /**
     * Removes an OverdueRequestListener from this instance's list of listeners.
     *
     * @param overdueRequestListener a OverdueRequestListener
     */
    public void removeOverdueRequestListener(OverdueRequestListener overdueRequestListener)
    {
        overdueListeners.remove(overdueRequestListener);
        log.debug("[{}] Removed overdue request listener: {}", this, overdueRequestListener);
    }

    /**
     * Starts watching the activeRequests for overdue requests.
     */
    public void startWatching()
    {
        long period = overdueThreshold / 2;
        scheduledFuture.set(executorService.scheduleAtFixedRate(new FindOverdueRequests(), period, period, MILLISECONDS));
        log.debug("[{}] started watching, threshold is {}ms", this, overdueThreshold);
    }

    /**
     * Stops watching the activeRequests map for overdue requests.
     */
    public void stopWatching()
    {
        ScheduledFuture<?> future = scheduledFuture.getAndSet(null);
        if (future != null)
        {
            future.cancel(false);
        }

        log.debug("[{}] stopped watching", this);
    }

    /**
     * Frees any resources associated with this RequestWatcher.
     */
    public void close()
    {
        executorService.shutdown();
        log.debug("[{}] closed", this);
    }

    /**
     * Creates a new single-thread executor service.
     *
     * @return an ExecutorService
     */
    protected ScheduledExecutorService newSingleThreadExecutor()
    {
        return Executors.newSingleThreadScheduledExecutor(new ThreadFactory()
        {
            public Thread newThread(Runnable r)
            {
                Thread thread = new Thread(r, "requestwatcher-thread");
                log.debug("[{}] Created watcher thread: {}", this, thread);

                return thread;
            }
        });
    }

    /**
     * Notifies each registered listener that the given requests are overdue.
     *
     * @param overdue a List of Request
     * @param overdueThreshold the number of ms after which a request is deemed to be overdue
     */
    protected void notifyOverdueListeners(List<Request> overdue, long overdueThreshold)
    {
        log.debug("[{}] Overdue requests: {}", this, overdue);
        for (OverdueRequestListener listener : overdueListeners)
        {
            try
            {
                log.trace("[{}] Calling listener: {}", this, listener);
                listener.requestsOverdue(overdue, overdueThreshold);
            }
            catch (RuntimeException e)
            {
                log.error(String.format("[%s] Error in listener", this), e);
            }
        }
    }

    private class FindOverdueRequests implements Runnable
    {
        public void run()
        {
            // try to "freeze" the value set
            List<Request> requests = Lists.newArrayList(callback.get());
            List<Request> overdueRequests = Lists.newArrayList();

            for (Request request : requests)
            {
                // just to be safe... ConcurrentHashMap's weakly-consistent iterator could possibly return null.
                if (request != null)
                {
                    long runningMillis = MILLISECONDS.convert(request.getRunningTime(), NANOSECONDS);
                    if (runningMillis > overdueThreshold)
                    {
                        overdueRequests.add(request);
                    }
                }
            }

            if (!overdueRequests.isEmpty())
            {
                notifyOverdueListeners(overdueRequests, overdueThreshold);
            }
        }
    }
}
