package com.atlassian.jira.scheduler;

import com.atlassian.multitenant.MultiTenantContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * Factory for locating a JIRA scheduler
 *
 * @since v4.3
 */
public class JiraSchedulerFactory
{
    public Scheduler getScheduler() throws SchedulerException
    {
        try
        {
            // Load the quartz properties, override the scheduler name, otherwise it will reuse the same scheduler
            // for each tenant
            Properties props = new Properties();
            props.load(getClass().getResourceAsStream("/quartz.properties"));
            props.setProperty(StdSchedulerFactory.PROP_SCHED_INSTANCE_NAME, MultiTenantContext.getTenantReference().get().getName() + "-Scheduler");
            return new StdSchedulerFactory(props).getScheduler();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Error loading quartz properties", e);
        }
    }
}
