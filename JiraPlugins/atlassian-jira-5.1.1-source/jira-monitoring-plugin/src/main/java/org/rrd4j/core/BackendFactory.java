package org.rrd4j.core;

import com.atlassian.jira.plugins.monitor.MonitorService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

/**
 * @since v5.3
 */
public class BackendFactory extends RrdFileBackendFactory implements MonitorService
{
    /**
     * The default sync period, in seconds.
     */
    public static final int DEFAULT_SYNC_PERIOD = 300;

    /**
     * The default thread pool size.
     */
    public static final int DEFAULT_THREAD_POOL_SIZE = 4;

    /**
     * The executor service that will do the writing to/from disk.
     */
    private volatile ScheduledExecutorService executorService;

    /**
     * Creates the executor service.
     */
    @Override
    public void start()
    {
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setDaemon(true)
                .setPriority(Thread.NORM_PRIORITY)
                .setNameFormat("RRD4J Sync Thread %d")
                .setThreadFactory(new ThreadGroupPreservingThreadFactory())
                .build();

        executorService = Executors.newScheduledThreadPool(DEFAULT_THREAD_POOL_SIZE, threadFactory);
    }

    /**
     * Shuts down the executor service.
     */
    @Override
    public void stop()
    {
        if (executorService != null)
        {
            executorService.shutdown();
            executorService = null;
        }
    }

    public String getName()
    {
        return "JIRA_NIO";
    }

    @Override
    protected RrdBackend open(String path, boolean readOnly) throws IOException
    {
        return new RrdNioBackend(path, readOnly, executorService, DEFAULT_SYNC_PERIOD);
    }

    /**
     * Creates threads under the SecurityManager's threadGroup, or under the threadGroup of the thread that creates
     * this ThreadFactory.
     */
    private static class ThreadGroupPreservingThreadFactory implements ThreadFactory
    {
        private final ThreadGroup threadGroup;

        private ThreadGroupPreservingThreadFactory()
        {
            SecurityManager s = System.getSecurityManager();
            threadGroup = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        }

        @Override
        public Thread newThread(Runnable r)
        {
            return new Thread(threadGroup, r);
        }
    }
}
