package com.atlassian.crowd.manager.directory.monitor.poller;

import com.atlassian.crowd.directory.monitor.poller.DirectoryPoller;
import com.atlassian.crowd.manager.directory.SynchronisationMode;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 */
public class DirectoryPollerJobBean extends QuartzJobBean
{
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException
    {
        DirectoryPoller directoryPoller = (DirectoryPoller) context.getJobDetail().getJobDataMap().get(DirectoryPollerJob.DIRECTORY_POLLER);
        if (directoryPoller == null)
        {
            throw new IllegalStateException("No DirectoryPoller found in the JobDataMap.");
        }

        // get the syncMode too, if it's not there then default to delta sync
        SynchronisationMode syncMode = (SynchronisationMode) context.getJobDetail().getJobDataMap().get(DirectoryPollerJob.SYNC_MODE);
        if (syncMode == null)
        {
            syncMode = SynchronisationMode.INCREMENTAL;
        }

        directoryPoller.pollChanges(syncMode);
    }
}
