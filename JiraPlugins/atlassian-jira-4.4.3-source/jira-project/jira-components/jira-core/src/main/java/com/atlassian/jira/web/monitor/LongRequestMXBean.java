package com.atlassian.jira.web.monitor;

/**
 * Interface for JIRA's long request MXB. This bean's properties control what kind of logging is performed for long
 * requests (requests that are not processed within a configurable time threshold).
 *
 * @since v4.3
 */
public interface LongRequestMXBean
{
    /**
     * Returns the log threshold value. Requests that take more than this amount of time will be logged.
     *
     * @return an int containing the log threshold, in milliseconds
     */
    int getLogThreshold();

    /**
     * Sets the log threshold value. Requests that take more than this amount of time will be logged.
     *
     * @param threshold an int containing the log threshold, in milliseconds
     */
    void setLogThreshold(int threshold);

    /**
     * Returns the dump threads threshold value. Requests that take more than this amount of time may cause a JVM thread
     * dump to be generated.
     *
     * @return an int containing the dump threads threshold, in milliseconds
     */
    int getDumpThreadsThreshold();

    /**
     * Sets the dump threads threshold value. Requests that take more than this amount of time may cause a JVM thread
     * dump to be generated.
     *
     * @param threshold an int containing the dump threads threshold, in milliseconds
     */
    void setDumpThreadsThreshold(int threshold);

    /**
     * Returns the directory where thread dumps will be created.
     *
     * @return a String containing the absolute path to a directory
     */
    String getThreadDumpsDir();

    /**
     * Sets the directory where thread dumps will be created.
     *
     * @param directory a String containing the absolute path to a directory
     */
    void setThreadDumpsDir(String directory);
}
