package com.atlassian.crowd.manager.directory.monitor.poller;

import com.atlassian.crowd.directory.monitor.poller.DirectoryPoller;
import com.atlassian.crowd.manager.directory.monitor.DirectoryMonitorRegistrationException;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

import java.util.Date;

/**
 * Implementation of DirectoryPollerManager that uses Quartz scheduling without any Spring framework dependency.
 */
public class QuartzDirectoryPollerManager extends AbstractQuartzDirectoryPollerManager
{
    private final Scheduler scheduler;

    public QuartzDirectoryPollerManager(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    @Override
    protected JobDetail buildJobDetail(final DirectoryPoller poller) throws DirectoryMonitorRegistrationException
    {
        JobDetail jobDetail = new JobDetail();
        jobDetail.setName(getJobName(poller.getDirectoryID()));
        jobDetail.setGroup(DIRECTORY_POLLER_JOB_GROUP);
        jobDetail.setJobClass(DirectoryPollerJob.class);
        // Don't need to persist the Job - it will get created again on restart.
        jobDetail.setVolatility(true);
        // Durable must be true for JIRA's Quartz implementation.
        jobDetail.setDurability(true);
        // Add the DirectoryPoller to the JobDataMap. DirectoryPollerJob will extract this out and run pollChanges()
        jobDetail.getJobDataMap().put(DirectoryPollerJob.DIRECTORY_POLLER, poller);
        return jobDetail;
    }

    @Override
    protected Trigger buildTrigger(final DirectoryPoller poller, final JobDetail jobDetail)
            throws DirectoryMonitorRegistrationException
    {
        // create trigger
        SimpleTrigger trigger = new SimpleTrigger();
        trigger.setName(jobDetail.getName());
        trigger.setJobName(jobDetail.getName());
        trigger.setGroup(DIRECTORY_POLLER_JOB_GROUP);
        trigger.setJobGroup(DIRECTORY_POLLER_JOB_GROUP);
        // If the Job is Volatile, then the Trigger must be too (else you can get Exceptions).
        trigger.setVolatility(jobDetail.isVolatile());
        trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        // Convert PollingInterval from seconds to milliseconds
        final long pollingIntervalMillis = poller.getPollingInterval() * 1000;
        trigger.setRepeatInterval(pollingIntervalMillis);
        // Set the first runtime to be in 5 seconds.  If run immediately on start up it
        // breaks some function tests.
        trigger.setStartTime(new Date(System.currentTimeMillis() + 5000));

        return trigger;
    }

    @Override
    protected Scheduler getScheduler()
    {
        return scheduler;
    }
}
