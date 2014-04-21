package com.atlassian.crowd.manager.directory.monitor.poller;

import com.atlassian.crowd.directory.monitor.poller.DirectoryPoller;
import com.atlassian.crowd.manager.directory.SynchronisationMode;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * The Quartz Job that runs {@link DirectoryPoller#pollChanges(com.atlassian.crowd.manager.directory.SynchronisationMode)} on trigger.
 */
public class DirectoryPollerJob implements Job
{
    public static final String DIRECTORY_POLLER = "DIRECTORY_POLLER";
    public static final String SYNC_MODE = "SYNC_MODE";

    public void execute(final JobExecutionContext context) throws JobExecutionException
    {
        // Get the DirectoryPoller instance from the JobDataMap
        // Don't call context.getMergedJobDataMap() - this was introduced in Quartz v1.5 and JIRA are still on v1.4
        DirectoryPoller directoryPoller = (DirectoryPoller) context.getJobDetail().getJobDataMap().get(DIRECTORY_POLLER);
        if (directoryPoller == null)
        {
            throw new IllegalStateException("No DirectoryPoller found in the JobDataMap.");
        }

        // get the syncMode too, if it's not there then default to delta sync
        SynchronisationMode syncMode = (SynchronisationMode) context.getJobDetail().getJobDataMap().get(SYNC_MODE);
        if (syncMode == null)
        {
            syncMode = SynchronisationMode.INCREMENTAL;
        }

        directoryPoller.pollChanges(syncMode);
    }
}
