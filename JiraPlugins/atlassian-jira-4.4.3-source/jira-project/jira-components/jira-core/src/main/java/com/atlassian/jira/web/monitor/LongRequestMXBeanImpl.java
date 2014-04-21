package com.atlassian.jira.web.monitor;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * This bean represents a ActiveRequestsFilter instance in JMX. This class simply delegates all method implementations
 * to the filter.
 */
class LongRequestMXBeanImpl implements LongRequestMXBean
{
    /**
     * The ActiveRequestsFilter.
     */
    private final ActiveRequestsFilter filter;

    /**
     * Creates a new LongRequestMXBeanImpl.
     *
     * @param filter a LongRequestFilter
     */
    LongRequestMXBeanImpl(ActiveRequestsFilter filter)
    {
        this.filter = notNull("filter", filter);
    }

    public int getLogThreshold()
    {
        return filter.getLogThreshold();
    }

    public void setLogThreshold(int threshold)
    {
        filter.setLogThreshold(threshold);
    }

    public int getDumpThreadsThreshold()
    {
        return filter.getDumpThreadsThreshold();
    }

    public void setDumpThreadsThreshold(int threshold)
    {
        filter.setDumpThreadsThreshold(threshold);
    }

    public String getThreadDumpsDir()
    {
        return filter.getDumpThreadsDir();
    }

    public void setThreadDumpsDir(String directory)
    {
        filter.setDumpThreadsDir(directory);
    }
}
