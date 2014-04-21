package com.atlassian.jira.plugins.monitor;

/**
 * Monitoring constants.
 *
 * @since v5.0.3
 */
public final class MonitorConstants
{
    /**
     * Expect a value every 10s.
     */
    public static final int DEFAULT_STEP = 10;

    /**
     * If we don't get a ping for 20s then that sample is UNKNOWN.
     */
    public static final int DEFAULT_HEARTBEAT = 2*DEFAULT_STEP;

    private MonitorConstants()
    {
    }
}
