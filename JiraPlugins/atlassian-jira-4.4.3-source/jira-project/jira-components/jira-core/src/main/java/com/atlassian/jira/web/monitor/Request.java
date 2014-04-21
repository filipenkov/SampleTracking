package com.atlassian.jira.web.monitor;

/**
 * Represents a web request. An instance of this class normally has a 1-1 mapping to a ServletRequest.
 */
public class Request
{
    /**
     * The time when this request was begun.
     */
    private final long startNanos;

    /**
     * The name of the thread that is servicing the request.
     */
    private final String threadName;

    /**
     * Creates a new Request.
     *
     * @param startNanos the time when this request was started
     * @param threadName a String containing the name of the thread that is servicing the request
     */
    public Request(long startNanos, String threadName)
    {
        this.startNanos = startNanos;
        this.threadName = threadName;
    }

    /**
     * Returns the running time of this request in nanoseconds.
     *
     * @return a long containing the running time of this request in nanoseconds
     */
    public long getRunningTime()
    {
        return System.nanoTime() - startNanos;
    }

    /**
     * Returns the name of the thread that is servicing the request.
     *
     * @return a String containing the name of the thread that is servicing the request
     */
    public String getThreadName()
    {
        return threadName;
    }

    @Override
    public String toString()
    {
        return "Request{threadName='" + threadName + '\'' + '}';
    }
}
