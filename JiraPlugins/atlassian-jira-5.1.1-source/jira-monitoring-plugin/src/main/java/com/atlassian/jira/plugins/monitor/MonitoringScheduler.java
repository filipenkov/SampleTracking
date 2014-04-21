package com.atlassian.jira.plugins.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;

import static com.atlassian.jira.plugins.monitor.MonitorConstants.DEFAULT_STEP;

/**
 * @since v5.0.3
 */
public class MonitoringScheduler implements MonitorService
{
    private static final Logger log = LoggerFactory.getLogger(MonitoringScheduler.class);

    private final MetricsCollectorTask metricsCollectorTask;
    private volatile Timer timer;

    public MonitoringScheduler(MetricsCollectorTask metricsCollectorTask)
    {
        this.metricsCollectorTask = metricsCollectorTask;
    }

    @Override
    public void start() throws Exception
    {
        timer = new Timer("jira-monitoring-plugin", true);

        long pollingInterval = DEFAULT_STEP * 1000;
        log.info("Scheduling metrics collector to run every {}ms...", pollingInterval);
        timer.schedule(metricsCollectorTask.cloneTask(), pollingInterval, pollingInterval);
    }

    @Override
    public void stop() throws Exception
    {
        log.info("Unscheduling metrics collector...");
        timer.cancel();
    }
}
