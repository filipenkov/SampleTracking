package com.atlassian.event.config;

import java.util.concurrent.TimeUnit;

/**
 * A configuration object for thread pools used by asynchronous event dispatchers
 */
public interface EventThreadPoolConfiguration
{
    int getCorePoolSize();

    int getMaximumPoolSize();

    long getKeepAliveTime();

    TimeUnit getTimeUnit();
}
