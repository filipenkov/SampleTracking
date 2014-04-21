package com.atlassian.crowd.manager.directory.monitor.poller;

import com.atlassian.crowd.directory.monitor.poller.DirectoryPoller;
import com.atlassian.crowd.manager.directory.SynchronisationMode;
import com.atlassian.crowd.manager.directory.monitor.DirectoryMonitorRegistrationException;
import com.atlassian.crowd.manager.directory.monitor.DirectoryMonitorUnregistrationException;
import org.apache.log4j.Logger;
import org.quartz.*;

public abstract class AbstractQuartzDirectoryPollerManager implements DirectoryPollerManager
{
    private static final Logger LOG = Logger.getLogger(AbstractQuartzDirectoryPollerManager.class);

    public static final String DIRECTORY_POLLER_JOB_GROUP = "DirectoryPoller";

    public void addPoller(DirectoryPoller poller) throws DirectoryMonitorRegistrationException
    {
        try
        {
            // Build JobDetail instance.
            JobDetail jobDetail = buildJobDetail(poller);
            // create trigger
            Trigger trigger = buildTrigger(poller, jobDetail);
            // add to quartz scheduler
            getScheduler().scheduleJob(jobDetail, trigger);
        }
        catch (SchedulerException e)
        {
            throw new DirectoryMonitorRegistrationException(e);
        }
        catch (RuntimeException e)
        {
            throw new DirectoryMonitorRegistrationException(e);
        }
    }

    protected abstract JobDetail buildJobDetail(final DirectoryPoller poller) throws DirectoryMonitorRegistrationException;

    protected abstract Trigger buildTrigger(final DirectoryPoller poller, final JobDetail jobDetail)
            throws DirectoryMonitorRegistrationException;

    public boolean hasPoller(long directoryID)
    {
        boolean registered = false;

        try
        {
            JobDetail jobDetail = getScheduler().getJobDetail(getJobName(directoryID), DIRECTORY_POLLER_JOB_GROUP);
            if (jobDetail != null)
            {
                registered = true;
            }
        }
        catch (SchedulerException e)
        {
            // do nothing
        }

        return registered;
    }

    public void triggerPoll(long directoryID, SynchronisationMode synchronisationMode)
    {
        try
        {
            JobDetail jobDetail = getScheduler().getJobDetail(getJobName(directoryID), DIRECTORY_POLLER_JOB_GROUP);
            if (jobDetail != null)
            {
                // we used to always the manual synchronise in Full mode (not incremental),
                // but for now we will run it the same as the scheduled job.
                // Part of the reason for this is that JIRA is still using Quartz 1.4 which doesn't support JobDataMap on Triggers.
                getScheduler().triggerJobWithVolatileTrigger(getJobName(directoryID), DIRECTORY_POLLER_JOB_GROUP);
            }
            else
            {
                LOG.info("Cannot trigger manual poll as directory [" + directoryID + "] does not have a poller registered");
            }
        }
        catch (SchedulerException e)
        {
            LOG.error("Could not trigger manual poll on directory [" + directoryID + "]: " + e.getMessage(), e);
        }
    }

    public boolean removePoller(long directoryID) throws DirectoryMonitorUnregistrationException
    {
        try
        {
            return getScheduler().deleteJob(getJobName(directoryID), DIRECTORY_POLLER_JOB_GROUP);
        }
        catch (SchedulerException e)
        {
            throw new DirectoryMonitorUnregistrationException(e);
        }
        catch (RuntimeException e)
        {
            throw new DirectoryMonitorUnregistrationException(e);
        }
    }

    public void removeAllPollers()
    {
        try
        {
            String[] directoryIDs = getScheduler().getJobNames(DIRECTORY_POLLER_JOB_GROUP);
            for (String directoryID : directoryIDs)
            {
                try
                {
                    removePoller(Long.parseLong(directoryID));
                }
                catch (DirectoryMonitorUnregistrationException e)
                {
                    // skip
                }
            }
        }
        catch (SchedulerException e)
        {
            // can't do much
        }
    }

    protected String getJobName(long directoryID)
    {
        return Long.toString(directoryID);
    }

    protected abstract Scheduler getScheduler();
}
