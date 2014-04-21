package com.atlassian.jira.plugins.monitor;

import com.atlassian.jira.plugins.monitor.rrd4j.RrdUpdater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TimerTask;

/**
 * SAL task that collects relevant JIRA metrics every 30 seconds.
 *
 * @since v5.0.3
 */
public class MetricsCollectorTask extends TimerTask
{
    public static final Logger log = LoggerFactory.getLogger(MetricsCollectorTask.class);

    private final RrdUpdater rrdUpdater;

    public MetricsCollectorTask(RrdUpdater rrdUpdater)
    {
        this.rrdUpdater = rrdUpdater;
    }

    /**
     * Virtual constructor that creates a new MetricsCollectorTask that is a copy of this one. This is necessary because
     * {@code TimerTask} instances can't be reused, yet we need to cancel/reschedule this.
     *
     * @return a copy of this MetricsCollectorTask
     */
    public MetricsCollectorTask cloneTask()
    {
        return new MetricsCollectorTask(rrdUpdater);
    }

    @Override
    public void run()
    {
        try
        {
            doRun();
        }
        catch (Exception e)
        {
            log.error("Error running task", e);
        }
    }

    protected void doRun() throws Exception
    {
        log.debug("Calling addSample() in: {}", rrdUpdater);
        rrdUpdater.addSample();
    }
}
