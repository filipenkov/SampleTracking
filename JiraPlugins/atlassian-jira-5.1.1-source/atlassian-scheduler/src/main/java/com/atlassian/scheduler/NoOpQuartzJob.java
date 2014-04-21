package com.atlassian.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * This {@link org.quartz.Job} class does nothing.  It can be used to provide a valid Job class when one is not
 * available.
 */
public class NoOpQuartzJob implements Job
{
    public void execute(final JobExecutionContext context) throws JobExecutionException
    {
    }
}
