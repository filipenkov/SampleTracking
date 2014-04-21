package com.atlassian.upm;

import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.atlassian.sal.api.scheduling.PluginScheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Starting in UPM 2.0.3, the tasks "upmPluginNotificationJob" and "upmPluginLicenseExpiryCheckSchedulerJob"
 * were unscheduled upon UPM being shut down (including during a UPM update). However, users updating directly
 * from UPM 2.0.0-2.0.2 to UPM 2.1.x+ will have these two jobs scheduled but then never unscheduled. They
 * are no longer in use in the current version of UPM as they have since been renamed.
 *
 * Let's unschedule them upon the system starting up. It would have been ideal to implement this as a
 * {@code PluginUpgradeTask} such that it only executed once, but we can't do so because we also need this to
 * be {@code LifecycleAware}. It also would have been ideal to implement this to work upon plugin startup
 * instead of system startup (in the case of UPM self-updates), but PluginScheduler does not play
 * nicely with {@code InitializingBean}.
 *
 */
public class UpmSchedulerUpgradeTask implements LifecycleAware
{
    private static final Logger log = LoggerFactory.getLogger(UpmSchedulerUpgradeTask.class);

    private final PluginScheduler pluginScheduler;

    public UpmSchedulerUpgradeTask(PluginScheduler pluginScheduler)
    {
        this.pluginScheduler = checkNotNull(pluginScheduler, "pluginScheduler");
    }

    @Override
    public void onStart()
    {
        unscheduleJob("upmPluginNotificationJob");
        unscheduleJob("upmPluginLicenseExpiryCheckSchedulerJob");
    }

    private void unscheduleJob(String jobName)
    {
        try
        {
            pluginScheduler.unscheduleJob(jobName);
        }
        catch (IllegalArgumentException e)
        {
            // don't worry about this exception. just means that the job either hadn't yet been added to the scheduler
            // or had already been unscheduled.
            log.debug("Could not unschedule job '" + jobName + "'. This is a harmless error if the job had previously been unscheduled.", e);
        }
    }
}
