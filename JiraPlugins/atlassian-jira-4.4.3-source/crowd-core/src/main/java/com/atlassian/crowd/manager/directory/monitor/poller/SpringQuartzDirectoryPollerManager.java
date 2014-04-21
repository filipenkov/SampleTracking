package com.atlassian.crowd.manager.directory.monitor.poller;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.directory.monitor.poller.DirectoryPoller;
import com.atlassian.crowd.manager.directory.monitor.DirectoryMonitorRegistrationException;
import org.apache.commons.lang.math.NumberUtils;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.SimpleTriggerBean;

import java.text.ParseException;
import java.util.Map;

/**
 * Implementation of DirectoryPollerManager that uses Quartz scheduling from Spring framework ApplicationContext.
 */
public class SpringQuartzDirectoryPollerManager extends AbstractQuartzDirectoryPollerManager implements ApplicationContextAware
{
    private Scheduler scheduler;
    protected ApplicationContext applicationContext;

    // ApplicationContextAware
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    }

    @Override
    protected JobDetail buildJobDetail(final DirectoryPoller poller) throws DirectoryMonitorRegistrationException
    {
        JobDetail jobDetail = new JobDetail();
        jobDetail.setName(getJobName(poller.getDirectoryID()));
        jobDetail.setGroup(DIRECTORY_POLLER_JOB_GROUP);
        jobDetail.setJobClass(DirectoryPollerJobBean.class);

        // Don't need to persist the Job - it will get created again on restart.
        jobDetail.setVolatility(true);

        jobDetail.setJobDataMap(new JobDataMap(EasyMap.build(DirectoryPollerJob.DIRECTORY_POLLER,poller)));
        return jobDetail;
    }

    @Override
    protected Trigger buildTrigger(final DirectoryPoller poller, final JobDetail jobDetail)
            throws DirectoryMonitorRegistrationException
    {
        SimpleTriggerBean trigger = new SimpleTriggerBean();
        trigger.setName(getJobName(poller.getDirectoryID()));
        trigger.setGroup(DIRECTORY_POLLER_JOB_GROUP);

        // If the Job is Volatile, then the Trigger must be too (else you can get Exceptions).
        trigger.setVolatility(jobDetail.isVolatile());

        trigger.setJobDetail(jobDetail);
        // Need to make sure we wait a little while before we start just to make sure hibernate has flushed the world
        // before our job tries to read it.  Esp a newly created directory may not arrive in the DB immediately.
        trigger.setStartDelay(NumberUtils.toLong(System.getProperty("crowd.polling.startdelay"), 5 * 1000));
        trigger.setRepeatInterval(poller.getPollingInterval() * 1000);
        try
        {
            trigger.afterPropertiesSet();
        }
        catch (ParseException e)
        {
            throw new DirectoryMonitorRegistrationException(e);
        }
        return trigger;
    }

    @Override
    protected Scheduler getScheduler()
    {
        if (scheduler == null)
        {
            @SuppressWarnings("unchecked")
            final Map<String, Scheduler> schedulers = applicationContext.getBeansOfType(Scheduler.class);
            if (schedulers.isEmpty())
            {
                throw new IllegalStateException("Could not find any scheduler in Spring context.");
            }
            else if (schedulers.size() > 1)
            {
                throw new IllegalStateException("Found more than one scheduler in Spring context. I am confused!");
            }
            else
            {
                scheduler = schedulers.values().iterator().next();
            }
        }
        return scheduler;
    }

}
