package com.atlassian.jira.plugins.monitor;

/**
 * Interface for services that need to be started and stopped.
 *
 * @since v5.1
 */
public interface MonitorService
{

    /**
     * Start monitoring.
     *
     * @throws Exception
     */
    void start() throws Exception;

    /**
     * Stop monitoring.
     *
     * @throws Exception
     */
    void stop() throws Exception;
}
