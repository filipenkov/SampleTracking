package com.atlassian.scheduler;

import org.apache.log4j.Category;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import java.util.Iterator;
import java.util.Map;

/**
 * This class exists to do the logic of starting, initializing and stopping the scheduler. It is here to decouple the
 * logic from the ServletContextListner.
 */
public class SchedulerUtil
{
    private static final Category log = Category.getInstance(SchedulerUtil.class);

    public void initializeAndStart(Scheduler scheduler)
    {
        SchedulerConfig config = new SchedulerConfig();

        try
        {
            Map jobs = config.getJobs();

            for (Iterator iterator = config.getTriggers().iterator(); iterator.hasNext();)
            {
                Trigger trigger = (Trigger) iterator.next();
                JobDetail jobDetail = null;
                try
                {
                    jobDetail = (JobDetail) jobs.get(trigger.getFullJobName());
                    scheduler.scheduleJob(jobDetail, trigger);
                }
                catch (Exception e)
                {
                    log.error("Unable to schedule job: " + ((jobDetail != null) ? jobDetail.getFullName() : ""), e);
                }
            }

            scheduler.start();
            log.info("The scheduler has been launched.");
        }
        catch (SchedulerException e)
        {
            log.error(e, e);
        }
    }

    public void shutdownScheduler(Scheduler scheduler)
    {
        try
        {
            if (!scheduler.isShutdown())
            {
                log.debug("Started shutting down scheduler.");
                scheduler.shutdown();
                log.debug("Finished shutting down scheduler.");
            }
        }
        catch (Exception e)
        {
            log.error(e, e);
        }
    }
}