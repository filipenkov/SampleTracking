/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.service;

/**
 * Proxies calls to JiraService & manages delay between calls.
 *
 * @see JiraService
 */
public interface JiraServiceContainer extends JiraService
{
    Long getId();

    long getDelay();

    void setDelay(long delay);

    long getLastRun();

    /**
     * Record when run.
     */
    void setLastRun();

    /**
     * Is this service due to run at the specified time.
     * @param time the time to check whether it is due.
     * @return true if due.
     */
    boolean isDueAt(long time);

    boolean isRunning();

    String getServiceClass();

    boolean isUsable();
}
