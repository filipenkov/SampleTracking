package com.atlassian.upm;

import java.util.Date;
import java.util.Map;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.atlassian.sal.api.scheduling.PluginJob;
import com.atlassian.sal.api.scheduling.PluginScheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Base class to implement common functionality for UPM-related scheduled tasks.
 * The implemented task will be scheduled upon system initialization and will run every 24 hours following.
 *
 * If this instance is being loaded during a UPM self-update, then this will execute the task a single time
 * but will not schedule it any further times (UPM-1676).
 */
public abstract class UpmScheduler implements LifecycleAware, DisposableBean
{
    private static final Logger log = LoggerFactory.getLogger(UpmScheduler.class);

    protected static final Long ONE_DAY = 24L * 60 * 60 * 1000;
    private static final String UPM_KEY = "com.atlassian.upm.atlassian-universal-plugin-manager-plugin";

    private final PluginScheduler pluginScheduler;
    private final EventPublisher eventPublisher;

    public UpmScheduler(PluginScheduler pluginScheduler, EventPublisher eventPublisher)
    {
        this.pluginScheduler = checkNotNull(pluginScheduler, "pluginScheduler");
        this.eventPublisher = checkNotNull(eventPublisher, "eventPublisher");
    }

    /**
     * Handles the event when a plugin (namely, the UPM) is enabled.
     *
     * @param event the event
     */
    @EventListener
    public void onPluginEnabled(PluginEnabledEvent event)
    {
        if (UPM_KEY.equals(event.getPlugin().getKey()))
        {
            getPluginJob().execute(getJobData());
        }
    }

    /**
     * Occurs on product startup/initialization. Does NOT happen upon subsequent plugin initializations.
     */
    @Override
    public void onStart()
    {
        eventPublisher.register(this);
        scheduleJob();
    }

    @Override
    public void destroy() throws Exception
    {
        eventPublisher.unregister(this);
        unschedulePreviouslyScheduledJob();
    }

    /**
     * Returns the plugin job.
     * @return the plugin job.
     */
    public abstract PluginJob getPluginJob();

    /**
     * Returns the data to be passed into the plugin job.
     * @return the data to be passed into the plugin job.
     */
    public abstract Map<String, Object> getJobData();

    protected String getJobName()
    {
        return getPluginJob().getClass().getSimpleName() + "-job";
    }

    private void scheduleJob()
    {
        try
        {
            unschedulePreviouslyScheduledJob();

            //schedule the UPM job to execute every 24 hours
            pluginScheduler.scheduleJob(
                getJobName(),
                getPluginJob().getClass(),
                getJobData(),
                new Date(),
                ONE_DAY);
        }
        catch (Exception e)
        {
            log.error("Error while scheduling job: " + getJobName(), e);
        }
    }

    /**
     * If UPM is self-updating, it will try to schedule the job even though it has already been scheduled.
     * This conflict will cause UPM to fail to load. this is to work around that problem.
     */
    private void unschedulePreviouslyScheduledJob()
    {
        try
        {
            pluginScheduler.unscheduleJob(getJobName());
        }
        catch (IllegalArgumentException e)
        {
            //don't worry about this exception. just means that the job hadn't yet been added to the scheduler.
        }
    }
}
