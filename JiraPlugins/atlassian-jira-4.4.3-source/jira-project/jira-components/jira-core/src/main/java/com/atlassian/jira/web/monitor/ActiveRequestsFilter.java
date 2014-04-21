package com.atlassian.jira.web.monitor;

import com.atlassian.jira.web.monitor.dump.Dumper;
import com.atlassian.jira.web.monitor.jmx.JMXBean;
import com.atlassian.jira.web.monitor.jmx.JMXException;
import com.atlassian.jira.web.monitor.watcher.ActiveRequestsCallback;
import com.atlassian.jira.web.monitor.watcher.OverdueRequestListener;
import com.atlassian.jira.web.monitor.watcher.RequestWatcher;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * This filter identifies requests that have taken a long time to process, and logs information about the request. This
 * filter is configurable via servlet filter &lt;init-param&gt; properties, and also at runtime via JMX.
 * <p/>
 * Example filter configuration:
 * <pre>
 * &lt;filter&gt;
 *     &lt;filter-name&gt;active-requests-filter&lt;/filter-name&gt;
 *     &lt;filter-class&gt;com.atlassian.jira.web.monitor.ActiveRequestsFilter&lt;/filter-class&gt;
 * <p/>
 *     &lt;!-- register in JMX --&gt;
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;jmx.name&lt;/param-name&gt;
 *         &lt;param-value&gt;com.atlassian.jira:name=Requests&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 * <p/>
 *     &lt;!-- log a INFO entry if a request takes more than this (default: 5s, 0 to disable) --&gt;
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;request.log.threshold&lt;/param-name&gt;
 *         &lt;param-value&gt;5000&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 * <p/>
 *     &lt;!-- create a thread dump if a request takes more than this (default: 30s, 0 to disable) --&gt;
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;request.dumpthreads.threshold&lt;/param-name&gt;
 *         &lt;param-value&gt;30000&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 * <p/>
 *     &lt;!-- save the thread dump in the logs directory --&gt;
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;request.dumpthreads.dir&lt;/param-name&gt;
 *         &lt;param-value&gt;${catalina.base}/logs&lt;/param-value&gt;
 *     &lt;/init-param&gt;
 * &lt;/filter&gt;
 * </pre>
 * <p/>
 * This class is <em>thread-safe</em>.
 *
 * @since v4.3
 */
public class ActiveRequestsFilter implements Filter
{
    /**
     * The name of the init-param used to select the JMX name.
     */
    public static final String JMX_NAME_PARAM = "jmx.name";

    /**
     * The name of the init-param used to enable and disable logging of overdue requests.
     */
    private static final String LOG_THRESHOLD = "request.log.threshold";

    /**
     * The name of the init-param that defines the default threshold in ms.
     */
    private static final String DUMPTHREADS_THRESHOLD = "request.dumpthreads.threshold";

    /**
     * The name of the init-param that contains the directory where thread dumps are to be created.
     */
    private static final String DUMPTHREADS_DIR_PARAM = "request.dumpthreads.dir";

    /**
     * Logger for this ActiveRequestsFilter instance.
     */
    private final Logger log = LoggerFactory.getLogger(ActiveRequestsFilter.class);

    /**
     * The number of ms after which a request will be logged.
     */
    private final AtomicInteger logThreshold = new AtomicInteger(0);

    /**
     * The number of ms after which a request will trigger a thread dump.
     */
    private final AtomicInteger dumpThreadsThreshold = new AtomicInteger(0);

    /**
     * The name of the directory where the thread dumps will be placed.
     */
    private final AtomicReference<String> dumpThreadsDir = new AtomicReference<String>(null);

    /**
     * The current filter state (will either be enabled or disabled).
     */
    private final AtomicReference<FilterFunc> filterFunc = new AtomicReference<FilterFunc>(null);

    /**
     * Reference to the MXBean that was registered, or null if none was registered.
     */
    private final AtomicReference<JMXBean> jmxBean = new AtomicReference<JMXBean>(null);

    /**
     * Creates a new ActiveRequestsFilter.
     */
    public ActiveRequestsFilter()
    {
        // empty
    }

    /**
     * Initialises this filter.
     *
     * @param filterConfig a FilterConfig
     */
    public void init(FilterConfig filterConfig)
    {
        int logThreshold = Integer.valueOf(filterConfig.getInitParameter(LOG_THRESHOLD));
        int dumpThreadsThreshold = Integer.valueOf(filterConfig.getInitParameter(DUMPTHREADS_THRESHOLD));
        String dumpThreadsDir = getDumpDirFromConfig(filterConfig);

        setLogThreshold(logThreshold);
        setDumpThreadsThreshold(dumpThreadsThreshold);
        setDumpThreadsDir(dumpThreadsDir);

        String jmxName = filterConfig.getInitParameter(JMX_NAME_PARAM);
        if (jmxName != null)
        {
            initJMX(jmxName);
        }

        settingsUpdated();
        log.debug("Initialised");
    }

    /**
     * Adds a request to the #requestsInProgress map when it begins, and removes it once it has finished.
     *
     * @param servletRequest a ServletRequest
     * @param servletResponse a ServletResponse
     * @param filterChain a FilterChain
     * @throws IOException if there is a problem somewhere down the interceptor chain
     * @throws ServletException if there is a problem somewhere down the interceptor chain
     */
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException
    {
        filterFunc.get().doFilter(servletRequest, servletResponse, filterChain);
    }

    /**
     * Destroys this filter, cancelling the associated timer instance.
     */
    public void destroy()
    {
        destroyJMX();
        FilterFunc oldFunc = filterFunc.getAndSet(null);
        if (oldFunc != null)
        {
            oldFunc.close();
        }

        log.debug("Destroyed");
    }

    int getDumpThreadsThreshold()
    {
        return dumpThreadsThreshold.get();
    }

    void setDumpThreadsThreshold(int dumpThreadsThreshold)
    {
        // TODO Impose some minimum threshold, so we don't cause too much overhead.
        this.dumpThreadsThreshold.set(dumpThreadsThreshold);
        settingsUpdated();

        if (dumpThreadsThreshold > 0)
        {
            log.info("Enabled dump threads threshold. Requests that take longer than {}ms will cause a thread dump to be created", dumpThreadsThreshold);
        }
        else
        {
            log.info("Disabled dump threads threshold");
        }
    }

    String getDumpThreadsDir()
    {
        return dumpThreadsDir.get();
    }

    void setDumpThreadsDir(String directory)
    {
        dumpThreadsDir.set(directory);
        log.info("Set thread dump directory to '{}'", directory);
    }   

    int getLogThreshold()
    {
        return logThreshold.get();
    }

    void setLogThreshold(int logThreshold)
    {
        this.logThreshold.set(logThreshold);
        settingsUpdated();

        if (logThreshold > 0)
        {
            log.info("Enabled log threshold. Requests that take longer than {}ms will be logged", logThreshold);
        }
        else
        {
            log.info("Disabled log threshold");
        }
    }

    /**
     * This function gets called when the settings have been updated.
     */
    void settingsUpdated()
    {
        // build up our filter-func chain
        FilterFunc newFunc = new PassToChainFilterFunc();

        // optionally enable request logging
        int logMillis = this.logThreshold.get();
        if (logMillis > 0) { newFunc = new DebugLogFilterFunc(logMillis, newFunc); }

        // optionally enable thread dumps
        int dumpMillis = this.dumpThreadsThreshold.get();
        if (dumpMillis > 0) { newFunc = new WatchRequestsFilterFunc(dumpMillis, newFunc); }

        // activate the new FilterFunc, and release resources associated with the old
        log.debug("Setting filter-func chain: {}", newFunc);
        FilterFunc oldFunc = filterFunc.getAndSet(newFunc);
        if (oldFunc != null)
        {
            oldFunc.close();
        }
    }

    /**
     * Registers the associated bean in JMX.
     *
     * @param objectName the object name to use in JMX
     */
    @SuppressWarnings ("unchecked")
    private void initJMX(String objectName)
    {
        try
        {
            jmxBean.set(new JMXBean(new LongRequestMXBeanImpl(this), objectName).register());
        }
        catch (JMXException e)
        {
            log.error("Error registering in JMX. You will not be able to change settings at runtime", e);
        }
    }

    /**
     * Unregister the associated bean from JMX.
     */
    private void destroyJMX()
    {
        JMXBean beanRef = jmxBean.getAndSet(null);
        if (beanRef != null)
        {
            try
            {
                beanRef.unregister();
            }
            catch (JMXException e)
            {
                log.error("Error unregistering bean in JMX", e);
            }
        }
    }

    /**
     * Returns the dump directory that was configured in this filter's init-params. If no directory was provided,
     * returns the value of the system property <pre>${user.home}</pre>.
     *
     * @param filterConfig a FilterConfig
     * @return a String containing the absolute path of the dump directory
     */
    private String getDumpDirFromConfig(FilterConfig filterConfig)
    {
        String threadDumpDir = filterConfig.getInitParameter(DUMPTHREADS_DIR_PARAM);
        if (threadDumpDir == null)
        {
            threadDumpDir = System.getProperty("user.home");
            log.info("init-param {} not specified, defaulting to home directory", DUMPTHREADS_DIR_PARAM);
        }

        return threadDumpDir;
    }

    /**
     * Defines operations on the filter state.
     */
    abstract static class FilterFunc
    {
        abstract void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
                throws IOException, ServletException;

        void close()
        {
            // empty
        }
    }

    /**
     * This FilterFunc simply delegates down the filter chain.
     */
    final static class PassToChainFilterFunc extends FilterFunc
    {
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
                throws IOException, ServletException
        {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    /**
     * This FilterFunc keeps a map of active requests, and has a RequestWatcher that monitors the active requests.
     */
    final class WatchRequestsFilterFunc extends FilterFunc
    {
        /**
         * The threshold after which a request will cause a thread dump to be created.
         */
        private final int threshold;

        /**
         * The delegate FilterFunc.
         */
        private final FilterFunc next;

        /**
         * This concurrent map holds the currently running requests.
         */
        private final ConcurrentMap<Request, Request> activeRequests = new ConcurrentHashMap<Request, Request>();

        /**
         * The RequestWatcher that will monitor the active requests.
         */
        private final RequestWatcher watcher;

        /**
         * Creates a new TrackRequestsFilterFunc, and starts watching the active requests.
         *
         * @param threshold the threshold, in ms
         * @param next a FilterFunc
         */
        WatchRequestsFilterFunc(int threshold, FilterFunc next)
        {
            this.threshold = threshold;
            this.next = next;
            watcher = new RequestWatcher(threshold, new ActiveRequestsCallback()
            {
                public Collection<Request> get()
                {
                    return activeRequests.values();
                }
            });
            watcher.addOverdueRequestListener(new MyOverdueRequestListener());
            watcher.startWatching();
        }

        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
                throws IOException, ServletException
        {
            Request request = new Request(System.nanoTime(), Thread.currentThread().getName());
            activeRequests.put(request, request);
            log.trace("Added to active requests: {}", request);
            try
            {
                next.doFilter(servletRequest, servletResponse, filterChain);
            }
            finally
            {
                activeRequests.remove(request);
                log.trace("Removed from active requests: {}", request);
            }
        }

        @Override
        void close()
        {
            watcher.stopWatching();
            watcher.close();
            super.close();
            next.close();
        }

        @Override
        public String toString()
        {
            return "WatchRequestsFilterFunc{threshold=" + threshold + ", next=" + next + '}';
        }
    }

    /**
     * This filter func generates wrapper WARN messages when a request exceeds the threshold.
     */
    final class DebugLogFilterFunc extends FilterFunc
    {
        /**
         * The threshold after which a request will be logged.
         */
        private final int threshold;

        /**
         * The delegate FilterFunc.
         */
        private final FilterFunc next;

        /**
         * Creates a new WarnFilterFuncWrapper that wraps the given FilterFunc.
         *
         * @param threshold the log threshold in ms
         * @param next a FilterFunc
         */
        public DebugLogFilterFunc(int threshold, FilterFunc next)
        {
            this.threshold = threshold;
            this.next = notNull("delegate", next);
        }

        @Override
        void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
                throws IOException, ServletException
        {
            long start = System.nanoTime();
            try
            {
                next.doFilter(servletRequest, servletResponse, filterChain);
            }
            finally
            {
                long timeTaken = MILLISECONDS.convert(System.nanoTime() - start, NANOSECONDS);
                if (timeTaken > threshold)
                {
                    log.debug("Request took longer than {}ms (completed in {}ms)", threshold, timeTaken);
                }
            }
        }

        @Override
        void close()
        {
            super.close();
            next.close();
        }

        @Override
        public String toString()
        {
            return "LogWarnFilterFunc{threshold=" + threshold + ", next=" + next + '}';
        }
    }

    /**
     * This listeners simply dumps the thread information to a file.
     */
    private class MyOverdueRequestListener implements OverdueRequestListener
    {
        final ThreadNameAndRuntime formatter = new ThreadNameAndRuntime();

        public void requestsOverdue(List<Request> requests, long overdueThreshold)
        {
            // TODO throttle this!
            String dir = dumpThreadsDir.get();
            String dumpFile = new Dumper().dumpThreads(dir);

            log.warn(format("Threads have been running for more than %dms, wrote thread dump to '%s': %s",
                    overdueThreshold,
                    dumpFile != null ? dumpFile : "System.err",
                    StringUtils.join(Lists.transform(requests, formatter), ", ")
            ));
        }
    }

    /**
     * Function that extracts the thread name from a Request object.
     */
    @Immutable
    private static class ThreadNameAndRuntime implements Function<Request, String>
    {
        @Override
        public String apply(@Nullable Request from)
        {
            return String.format("%s (%dms)", from.getThreadName(), MILLISECONDS.convert(from.getRunningTime(), NANOSECONDS));
        }
    }
}
