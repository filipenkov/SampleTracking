package com.atlassian.jira.util;

import com.atlassian.jira.scheduler.cron.ConversionResult;
import com.atlassian.jira.scheduler.cron.SimpleToCronTriggerConverter;
import org.apache.log4j.Logger;
import org.quartz.CronTrigger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

import java.text.ParseException;
import java.util.Date;

/**
 * Helper class to carry out various cron conversion tasks.
 */
public class SimpleToCronUtil
{
    private static final Logger log = Logger.getLogger(SimpleToCronUtil.class);
    private final Scheduler scheduler;
    private final SimpleToCronTriggerConverter simpleToCronTriggerConverter;

    public SimpleToCronUtil(Scheduler scheduler, SimpleToCronTriggerConverter simpleToCronTriggerConverter)
    {
        this.scheduler = scheduler;
        this.simpleToCronTriggerConverter = simpleToCronTriggerConverter;
    }

    /**
     * Convert the provided SimpleTrigger to a CronTrigger.  This method also schedules the new trigger and removes the
     * old trigger.
     *
     * @param simpleTrigger The trigger to be converted.
     * @return The new CronTrigger (or null if conversion failed).
     */
    public CronTrigger convertSimpleToCronTrigger(SimpleTrigger simpleTrigger)
    {
        CronTrigger result = null;
        try
        {
            ConversionResult conversionResult = convertToCronString(simpleTrigger);
            result = createCronTrigger(simpleTrigger, conversionResult);
        }
        catch (ParseException e)
        {
            log.error("Subscription with interval: " + simpleTrigger.getRepeatInterval() + " and nextFireTime: " + simpleTrigger.getNextFireTime() +
                    " failed during parsing", e);

            //notify users of errors and unschedule their subscription.
            unscheduleJob(simpleTrigger);
        }
        catch (SchedulerException e)
        {
            log.error("Un/Scheduling subscription failed with name: " + simpleTrigger.getName(), e);
        }

        return result;
    }

    /**
     * Creates a new CronTrigger, using the details from the SimpleTrigger, and the cron expression provided in the
     * ConversionResult.
     *
     * @param triggerFromSubscription The old SimpleTrigger.
     * @param conversionResult The ConversionResult from converting triggerFromSubscription
     * @return The new CronTrigger
     * @throws ParseException If the new Trigger is not valid
     * @throws SchedulerException If the new Trigger could not be scheduled.
     */
    public CronTrigger createCronTrigger(SimpleTrigger triggerFromSubscription, ConversionResult conversionResult)
            throws ParseException, SchedulerException
    {
        // Update the subscription with the CronTrigger we create with the
        // converted cron string
        CronTrigger cronTrigger = new CronTrigger(triggerFromSubscription.getName(),
                triggerFromSubscription.getGroup(),
                triggerFromSubscription.getJobName(),
                triggerFromSubscription.getJobGroup(),
                conversionResult.cronString);

        //extra validation
        Date nextFireTime = cronTrigger.getFireTimeAfter(null);
        if (nextFireTime == null)
        {
            throw new ParseException("CronTrigger [" + conversionResult.cronString + "] will never be run", 0);
        }

        //remove old trigger and add new
        unscheduleJob(triggerFromSubscription);
        scheduler.scheduleJob(cronTrigger);
        return cronTrigger;
    }

    /**
     * Converts the simpleTrigger to a cronExpression (stored in the ConversionResult).
     *
     * @param triggerFromSubscription The old SimpleTrigger.
     * @return A {@link com.atlassian.jira.scheduler.cron.ConversionResult}.
     */
    public ConversionResult convertToCronString(SimpleTrigger triggerFromSubscription)
    {
        // Run the conversion tool with the nextFireTime and interval from the trigger
        return simpleToCronTriggerConverter.convertToCronString(
                triggerFromSubscription.getNextFireTime(),
                triggerFromSubscription.getRepeatInterval());
    }

    /**
     * Unschedules a trigger.
     *
     * @param triggerFromSubscription The trigger about to be removed.
     */
    public void unscheduleJob(Trigger triggerFromSubscription)
    {
        try
        {
            if (scheduler.getTrigger(triggerFromSubscription.getName(), triggerFromSubscription.getGroup()) != null)
            {
                scheduler.unscheduleJob(triggerFromSubscription.getName(), triggerFromSubscription.getGroup());
            }
        }
        catch (SchedulerException e)
        {
            log.error("Un/Scheduling subscription failed with name: " + triggerFromSubscription.getName(), e);
        }
    }

    /**
     * Pauses the scheduler if it isn't already paused.
     *
     * @return true if the schedulre was paused.
     */
    public boolean pauseScheduler()
    {
        try
        {
            //ensure that the scheduler isn't running when we re-do all the triggers.
            if (!scheduler.isShutdown() && !scheduler.isPaused())
            {
                scheduler.pause();
                return true;
            }
        }
        catch (SchedulerException e)
        {
            log.warn("Unable to pause the scheduler before running the integrity checker", e);
            //this is bad, but we should still attempt the conversion below.
        }
        return false;
    }

    /**
     * Restarts the scheduler if the provided flag is true.
     *
     * @param restartScheduler flag to restart the scheduler
     */
    public void restartScheduler(boolean restartScheduler)
    {
        //kick off the scheduler again.
        try
        {
            if (restartScheduler)
            {
                scheduler.start();
            }
        }
        catch (SchedulerException e)
        {
            log.warn("Error restarting the scheduler after running the integrity checker.", e);
        }
    }

}
