package com.atlassian.scheduler;

import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.jira.ofbiz.DefaultOfBizConnectionFactory;
import com.atlassian.util.concurrent.LazyReference;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilMisc;
import org.quartz.Calendar;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobPersistenceException;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.SchedulerConfigException;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.core.SchedulingContext;
import org.quartz.impl.calendar.BaseCalendar;
import org.quartz.simpl.RAMJobStore;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.JobStore;
import org.quartz.spi.SchedulerSignaler;
import org.quartz.spi.TriggerFiredBundle;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This was taken from atlassian-scheduler and placed into its now rightful home of JIRA.
 *
 * @since 4.3
 */
public class OfBizJobStore implements JobStore
{
    private static final Logger log = Logger.getLogger(OfBizJobStore.class);

    private final RAMJobStore memoryStore = new RAMJobStore();
    private boolean memoryStoreInited = false;
    private LazyReference<String> delegatorNameRef;

    public final static String STATE_WAITING = "WAITING";
    public final static String STATE_ACQUIRED = "ACQUIRED";
    public final static String STATE_EXECUTING = "EXECUTING";
    public final static String STATE_COMPLETE = "COMPLETE";
    public final static String STATE_BLOCKED = "BLOCKED";
    public final static String TTYPE_SIMPLE = "SIMPLE";
    public final static String TTYPE_CRON = "CRON";


    public void initialize(ClassLoadHelper loadHelper, SchedulerSignaler signaler) throws SchedulerConfigException
    {
        memoryStore.initialize(loadHelper, signaler);
        delegatorNameRef = new LazyReference<String>()
        {
            @Override
            protected String create() throws Exception
            {
                return new DefaultOfBizConnectionFactory().getDelegatorName();
            }
        };
    }

    private void initialise(SchedulingContext ctxt) throws JobPersistenceException
    {
        Timestamp now = new Timestamp(new Date().getTime());
        memoryStoreInited = true;
        //Initalise the memorystore that does all the work.
        try
        {
            //Retrieve the job and construct object so that it can be placed in the memory store
            List jobDetailGVs = getDelegator().findAll("QRTZJobDetails");
            for (int j = 0; j < jobDetailGVs.size(); j++)
            {
                GenericValue jobDetailGV = (GenericValue) jobDetailGVs.get(j);

                JobDetail jobDetail = createJobDetail(jobDetailGV);

                memoryStore.storeJob(ctxt, jobDetail, true);

                List triggerGVs = jobDetailGV.getRelated("ChildQRTZTriggers");
                for (int i = 0; i < triggerGVs.size(); i++)
                {
                    GenericValue triggerGV = (GenericValue) triggerGVs.get(i);
                    if (triggerGV.getString("triggerType").equals(TTYPE_SIMPLE))
                    {
                        loadSimpleTrigger(ctxt, now, jobDetail, triggerGV);
                    }
                    else if (triggerGV.getString("triggerType").equals(TTYPE_CRON))
                    {
                        loadCronTrigger(ctxt, jobDetail, triggerGV);
                    }
                    else
                    {
                        throw new JobPersistenceException("Unsupported trigger type: '" + triggerGV.getString("triggerType") + "'");
                    }
                }
            }
        }
        catch (GenericEntityException e)
        {
            throw new JobPersistenceException("Error retrieving Job details", e);
        }
    }

    private JobDetail createJobDetail(final GenericValue jobDetailGV)
    {
        final String jobName = jobDetailGV.getString("jobName");
        final String jobGroup = jobDetailGV.getString("jobGroup");
        final String jobClassName = jobDetailGV.getString("className");
        final boolean isDurable = Boolean.valueOf(jobDetailGV.getString("isDurable")).booleanValue();
        final boolean requestRecovery = Boolean.valueOf(jobDetailGV.getString("requestsRecovery")).booleanValue();

        final Class jobClass = loadJobClassResiliently(jobName,jobClassName);

        return new JobDetail(jobName, jobGroup, jobClass, false, isDurable, requestRecovery);
    }


    /**
     * If a class name in the database in no longer valid, then we hide this from the upstream code.  If we just threw
     * an exception then Quartz initialisation will be stuff and no jobs will be run.  Instead we return a job class
     * that does nothing when run.
     *
     * @param jobName the name of the job being loaded
     * @param jobClassName the class name to attempt to load
     *
     * @return a non null class that will run as a  {@link org.quartz.Job}
     */
    private Class loadJobClassResiliently(final String jobName, final String jobClassName)
    {
        final Class fallbackClass = NoOpQuartzJob.class;
        try
        {
            final Class jobClass = Class.forName(jobClassName);
            if (Job.class.isAssignableFrom(jobClass))
            {
                return jobClass;
            }
            log.error("The Quartz Job class '" + jobClassName + "' does not implement org.quartz.Job!");
        }
        catch (ClassNotFoundException e)
        {
            log.error("The Quartz Job class '" + jobClassName + "' cannot be loaded");
            log.error("The Quartz Job configuration for '" + jobName + "' is invalid!  This can be caused by Plugin JARS no longer being available on the class path.");
        }
        log.error("A NoOp Job class has been returned and hence '" + jobName + "' will not do anything until fixed.");
        return fallbackClass;
    }

    private void loadCronTrigger(final SchedulingContext ctxt, final JobDetail jobDetail, final GenericValue triggerGV)
            throws GenericEntityException, JobPersistenceException
    {
        GenericValue cronTriggerGV = EntityUtil.getOnly(getDelegator().findByAnd("QRTZCronTriggers", UtilMisc.toMap("trigger", triggerGV.getLong("id"))));
        if (cronTriggerGV != null)
        {
            CronTrigger trigger;
            String cronExpression = cronTriggerGV.getString("cronExpression");
            try
            {
                trigger = new CronTrigger(
                        triggerGV.getString("triggerName"),
                        triggerGV.getString("triggerGroup"),
                        jobDetail.getName(),
                        jobDetail.getGroup(),
                        triggerGV.getTimestamp("startTime"),
                        triggerGV.getTimestamp("endTime"),
                        cronExpression);

                trigger.setNextFireTime(triggerGV.getTimestamp("nextFire"));
                memoryStore.storeTrigger(ctxt, trigger, true);
            }
            catch (ParseException e)
            {
                log.error("Could not read cron trigger with cron spec '" + cronExpression + "'", e);
            }
            catch (IllegalArgumentException e)
            {
                log.error("Could not read cron trigger with cron spec '" + cronExpression + "'", e);
            }
        }
        else
        {
            log.warn("Could not find cron trigger with trigger id " + triggerGV.getLong("id") + ".");
        }
    }

    private void loadSimpleTrigger(final SchedulingContext ctxt, final Timestamp now, final JobDetail jobDetail, final GenericValue triggerGV)
            throws GenericEntityException, JobPersistenceException
    {
        GenericValue simpleTriggerGV = EntityUtil.getOnly(getDelegator().findByAnd("QRTZSimpleTriggers", UtilMisc.toMap("trigger", triggerGV.getLong("id"))));
        if (simpleTriggerGV != null)
        {
            SimpleTrigger trigger = new AtlassianSimpleTrigger(
                    triggerGV.getString("triggerName"),
                    triggerGV.getString("triggerGroup"),
                    jobDetail.getName(),
                    jobDetail.getGroup(),
                    triggerGV.getTimestamp("startTime"),
                    triggerGV.getTimestamp("endTime"),
                    simpleTriggerGV.getInteger("repeatCount").intValue(),
                    simpleTriggerGV.getLong("repeatInterval").longValue());
            trigger.setNextFireTime(triggerGV.getTimestamp("nextFire"));
            if (trigger.getNextFireTime() == null || trigger.getNextFireTime().getTime() < now.getTime())
            {
                trigger.setNextFireTime(now);
            }
            memoryStore.storeTrigger(ctxt, trigger, true);
        }
        else
        {
            log.warn("Could not find simple trigger with trigger id " + triggerGV.getLong("id") + ".");
        }
    }

    public void schedulerStarted() throws SchedulerException
    {
    }

    public void shutdown()
    {
        memoryStore.shutdown();
    }

    public boolean supportsPersistence()
    {
        return true;
    }

    public void storeJobAndTrigger(SchedulingContext ctxt, JobDetail newJob, Trigger newTrigger)
            throws ObjectAlreadyExistsException, JobPersistenceException
    {
        storeJob(ctxt, newJob);
        storeTrigger(ctxt, newTrigger);
    }

    public void storeJob(SchedulingContext ctxt, JobDetail newJob)
            throws ObjectAlreadyExistsException, JobPersistenceException
    {
        storeJob(ctxt, newJob, false);
    }

    public void storeJob(SchedulingContext ctxt, JobDetail newJob, boolean replaceExisting)
            throws ObjectAlreadyExistsException, JobPersistenceException
    {
        if (!memoryStoreInited)
        {
            initialise(ctxt);
        }

        if (!newJob.isDurable())
        {
            throw new IllegalArgumentException("Non durable jobs are currently not supported: " + newJob);
        }
        if (newJob.requestsRecovery())
        {
            throw new IllegalArgumentException("Requests Recovery jobs are currently not supported: " + newJob);
        }

        memoryStore.storeJob(ctxt, newJob, replaceExisting);
        if (!newJob.isVolatile())
        {
            try
            {
                GenericValue jobDetail = EntityUtil.getOnly(getDelegator().findByAnd("QRTZJobDetails", UtilMisc.toMap("jobName", newJob.getName(), "jobGroup", newJob.getGroup())));
                Map fields = UtilMisc.toMap(
                        "jobName", newJob.getName(),
                        "jobGroup", newJob.getGroup(),
                        "className", newJob.getJobClass().getName(),
                        "isDurable", Boolean.toString(newJob.isDurable()),
                        "isStateful", Boolean.toString(newJob.isStateful()),
                        "requestsRecovery", Boolean.toString(newJob.requestsRecovery()));

                // We don't support job data,so throw an exception if someone passes some data to us.
                if (newJob.getJobDataMap() != null && !newJob.getJobDataMap().isEmpty())
                {
                    throw new UnsupportedOperationException("Atlassian Scheduler does not support the persistence of a Job Data Map");
                }

                if (jobDetail == null)
                {
                    EntityUtils.createValue("QRTZJobDetails", fields);
                }
                else if (replaceExisting)
                {
                    jobDetail.setFields(fields);
                    jobDetail.store();
                }
                else
                {
                    throw new ObjectAlreadyExistsException(newJob.getName() + " already exists.\n" + ExceptionUtils.getStackTrace(new Throwable()));
                }
            }
            catch (GenericEntityException e)
            {
                throw new JobPersistenceException("Error storing Job", e);
            }
        }
    }

    public boolean removeJob(SchedulingContext ctxt, String jobName, String groupName) throws JobPersistenceException
    {
        if (!memoryStoreInited)
        {
            initialise(ctxt);
        }
        try
        {
            // Remove any associated triggers.
            Trigger[] triggers = getTriggersForJob(ctxt, jobName, groupName);
            for (Trigger trigger : triggers)
            {
                removeTrigger(ctxt, trigger.getName(), trigger.getGroup());
            }
            GenericValue jobDetail = EntityUtil.getOnly(getDelegator().findByAnd("QRTZJobDetails", UtilMisc.toMap("jobName", jobName, "jobGroup", groupName)));
            // If the job has been stored, then remove it.  (Volatile jobs aren't stored)
            if (jobDetail != null)
            {
                jobDetail.remove();
            }
            return memoryStore.removeJob(ctxt, jobName, groupName);
        }
        catch (GenericEntityException e)
        {
            throw new JobPersistenceException("Could not remove job: " + jobName + " in group: " + groupName, e);
        }
    }

    public JobDetail retrieveJob(SchedulingContext ctxt, String jobName, String groupName)
            throws JobPersistenceException
    {
        return memoryStore.retrieveJob(ctxt, jobName, groupName);
    }

    public void storeTrigger(SchedulingContext ctxt, Trigger newTrigger)
            throws ObjectAlreadyExistsException, JobPersistenceException
    {
        storeTrigger(ctxt, newTrigger, false);
    }

    public synchronized void storeTrigger(SchedulingContext ctxt, Trigger newTrigger, boolean replaceExisting)
            throws ObjectAlreadyExistsException, JobPersistenceException
    {
        if (!memoryStoreInited)
        {
            initialise(ctxt);
        }

        memoryStore.storeTrigger(ctxt, newTrigger, replaceExisting);
        persistTrigger(newTrigger, replaceExisting);
    }

    private synchronized void persistTrigger(Trigger newTrigger, boolean replaceExisting)
            throws JobPersistenceException, ObjectAlreadyExistsException
    {
        if (!newTrigger.isVolatile())
        {
            try
            {
                GenericValue trigger = EntityUtil.getOnly(getDelegator().findByAnd("QRTZTriggers", UtilMisc.toMap("triggerName", newTrigger.getName(), "triggerGroup", newTrigger.getGroup())));
                GenericValue jobDetail = EntityUtil.getOnly(getDelegator().findByAnd("QRTZJobDetails", UtilMisc.toMap("jobName", newTrigger.getJobName(), "jobGroup", newTrigger.getJobGroup())));
                Map fields = UtilMisc.toMap(
                        "triggerName", newTrigger.getName(),
                        "triggerGroup", newTrigger.getGroup(),
                        "job", jobDetail.getLong("id"),
                        "triggerState", STATE_WAITING);
                if (newTrigger.getNextFireTime() != null)
                {
                    fields.put("nextFire", new Timestamp(newTrigger.getNextFireTime().getTime()));
                }
                Map subFields;
                String subEntity;
                if (newTrigger instanceof SimpleTrigger)
                {
                    fields.put("triggerType", TTYPE_SIMPLE);
                    SimpleTrigger simpleTrig = (SimpleTrigger) newTrigger;
                    subFields = UtilMisc.toMap("repeatCount", new Integer(simpleTrig.getRepeatCount()),
                            "repeatInterval", new Long(simpleTrig.getRepeatInterval()),
                            "timesTriggered", new Integer(simpleTrig.getTimesTriggered()));
                    subEntity = "QRTZSimpleTriggers";

                }
                else if (newTrigger instanceof CronTrigger)
                {
                    fields.put("triggerType", TTYPE_CRON);
                    CronTrigger cronTrigger = (CronTrigger) newTrigger;
                    subFields = UtilMisc.toMap("cronExpression", cronTrigger.getCronExpression());
                    subEntity = "QRTZCronTriggers";
                }
                else
                {
                    throw new JobPersistenceException("Unsupported trigger type: '" + newTrigger.getClass().getName() + "'");
                }

                if (newTrigger.getStartTime() != null)
                {
                    fields.put("startTime", new Timestamp(newTrigger.getStartTime().getTime()));
                }
                if (newTrigger.getEndTime() != null)
                {
                    fields.put("endTime", new Timestamp(newTrigger.getEndTime().getTime()));
                }
                fields.put("calendarName", newTrigger.getCalendarName());
                fields.put("misfireInstr", new Integer(newTrigger.getMisfireInstruction()));

                // We don't support job data,so throw an exception if someone passes some data to us.
                if (newTrigger.getJobDataMap() != null && !newTrigger.getJobDataMap().isEmpty())
                {
                    throw new UnsupportedOperationException("Atlassian Scheduler does not support the persistence of a Job Data Map");
                }

                if (trigger == null)
                {
                    GenericValue createdTrigger = EntityUtils.createValue("QRTZTriggers", fields);
                    subFields.put("trigger", createdTrigger.getLong("id"));
                    EntityUtils.createValue(subEntity, subFields);
                }
                else if (replaceExisting == true)
                {
                    GenericValue subTrigger = EntityUtil.getOnly(getDelegator().findByAnd(subEntity, UtilMisc.toMap("trigger", trigger.getLong("id"))));
                    trigger.setFields(fields);
                    subTrigger.setFields(subFields);
                    trigger.store();
                    subTrigger.store();
                }
                else
                {
                    throw new ObjectAlreadyExistsException(newTrigger.getName() + " already exists");
                }
            }
            catch (GenericEntityException e)
            {
                throw new JobPersistenceException("Error storing Job", e);
            }
        }
    }

    public synchronized boolean removeTrigger(SchedulingContext ctxt, String triggerName, String groupName)
            throws JobPersistenceException
    {
        if (!memoryStoreInited)
        {
            initialise(ctxt);
        }
        try
        {
            GenericValue trigger = EntityUtil.getOnly(getDelegator().findByAnd("QRTZTriggers", UtilMisc.toMap("triggerName", triggerName, "triggerGroup", groupName)));
            if (trigger != null)
            {
                if (trigger.getString("triggerType").equals(TTYPE_SIMPLE))
                {
                    GenericValue simpleTriggerGV = EntityUtil.getOnly(getDelegator().findByAnd("QRTZSimpleTriggers", UtilMisc.toMap("trigger", trigger.getLong("id"))));
                    if (simpleTriggerGV != null)
                    {
                        simpleTriggerGV.remove();
                    }
                }
                else if (trigger.getString("triggerType").equals(TTYPE_CRON))
                {
                    GenericValue cronTriggerGV = EntityUtil.getOnly(getDelegator().findByAnd("QRTZCronTriggers", UtilMisc.toMap("trigger", trigger.getLong("id"))));
                    if (cronTriggerGV != null)
                    {
                        cronTriggerGV.remove();
                    }
                }
                else
                {
                    throw new JobPersistenceException("Unsupported trigger type: '" + trigger.getClass().getName() + "'");
                }

                trigger.remove();
            }
            return memoryStore.removeTrigger(ctxt, triggerName, groupName);
        }
        catch (GenericEntityException e)
        {
            throw new JobPersistenceException("Could not retrieve trigger: " + triggerName + " in group: " + groupName, e);
        }
    }

    public Trigger retrieveTrigger(SchedulingContext ctxt, String triggerName, String groupName)
            throws JobPersistenceException
    {
        if (!memoryStoreInited)
        {
            initialise(ctxt);
        }
        return memoryStore.retrieveTrigger(ctxt, triggerName, groupName);
    }

    public boolean replaceTrigger(SchedulingContext ctxt, String triggerName, String groupName, Trigger newTrigger)
            throws JobPersistenceException
    {
        throw new UnsupportedOperationException();
    }

    public int getTriggerState(SchedulingContext ctxt, String triggerName, String triggerGroup)
            throws JobPersistenceException
    {
        if (!memoryStoreInited)
        {
            initialise(ctxt);
        }

        return memoryStore.getTriggerState(ctxt, triggerName, triggerGroup);
    }

    public void storeCalendar(SchedulingContext ctxt, String name, Calendar calendar)
            throws ObjectAlreadyExistsException, JobPersistenceException
    {
        throw new UnsupportedOperationException();
    }

    public void storeCalendar(SchedulingContext ctxt, String name, Calendar calendar, boolean replaceExisting)
            throws ObjectAlreadyExistsException, JobPersistenceException
    {
        throw new UnsupportedOperationException();
    }

    public void storeCalendar(SchedulingContext ctxt, String name, Calendar calendar, boolean replaceExisting, boolean updateTriggers)
            throws ObjectAlreadyExistsException, JobPersistenceException
    {
        throw new UnsupportedOperationException();
    }

    public boolean removeCalendar(SchedulingContext ctxt, String calName) throws JobPersistenceException
    {
        throw new UnsupportedOperationException();
    }

    public Calendar retrieveCalendar(SchedulingContext ctxt, String calName) throws JobPersistenceException
    {
        return new BaseCalendar();
    }

    public int getNumberOfJobs(SchedulingContext ctxt) throws JobPersistenceException
    {
        if (!memoryStoreInited)
        {
            initialise(ctxt);
        }
        return memoryStore.getNumberOfJobs(ctxt);
    }

    public int getNumberOfTriggers(SchedulingContext ctxt) throws JobPersistenceException
    {
        if (!memoryStoreInited)
        {
            initialise(ctxt);
        }
        return memoryStore.getNumberOfTriggers(ctxt);
    }

    public int getNumberOfCalendars(SchedulingContext ctxt) throws JobPersistenceException
    {
        throw new UnsupportedOperationException();
    }

    public String[] getJobNames(SchedulingContext ctxt, String groupName) throws JobPersistenceException
    {
        if (!memoryStoreInited)
        {
            initialise(ctxt);
        }

        return memoryStore.getJobNames(ctxt, groupName);
    }

    public String[] getTriggerNames(SchedulingContext ctxt, String groupName) throws JobPersistenceException
    {
        if (!memoryStoreInited)
        {
            initialise(ctxt);
        }

        return memoryStore.getTriggerNames(ctxt, groupName);
    }

    public String[] getJobGroupNames(SchedulingContext ctxt) throws JobPersistenceException
    {
        if (!memoryStoreInited)
        {
            initialise(ctxt);
        }

        return memoryStore.getJobGroupNames(ctxt);
    }

    public String[] getTriggerGroupNames(SchedulingContext ctxt) throws JobPersistenceException
    {
        if (!memoryStoreInited)
        {
            initialise(ctxt);
        }

        return memoryStore.getTriggerGroupNames(ctxt);
    }

    public String[] getCalendarNames(SchedulingContext ctxt) throws JobPersistenceException
    {
        throw new UnsupportedOperationException();
    }

    public Trigger[] getTriggersBeforeDate(SchedulingContext ctxt, Date fireBeforeDate) throws JobPersistenceException
    {
        throw new UnsupportedOperationException();
    }

    public Trigger[] getTriggersAfterDate(SchedulingContext ctxt, Date fireAfterDate) throws JobPersistenceException
    {
        throw new UnsupportedOperationException();
    }

    public Trigger[] getTriggersDuringDateRange(SchedulingContext ctxt, Date fireAfterDate, Date fireBeforeDate)
            throws JobPersistenceException
    {
        throw new UnsupportedOperationException();
    }

    public Trigger[] getTriggersForJob(SchedulingContext ctxt, String jobName, String groupName)
            throws JobPersistenceException
    {
        if (!memoryStoreInited)
        {
            initialise(ctxt);
        }

        return memoryStore.getTriggersForJob(ctxt, jobName, groupName);
    }

    public void pauseTrigger(SchedulingContext ctxt, String triggerName, String groupName)
            throws JobPersistenceException
    {
        throw new UnsupportedOperationException();
    }

    public void pauseTriggerGroup(SchedulingContext ctxt, String groupName) throws JobPersistenceException
    {
        throw new UnsupportedOperationException();
    }

    public Set getPausedTriggerGroups(SchedulingContext ctxt) throws JobPersistenceException
    {
        throw new UnsupportedOperationException();
    }

    public void pauseJob(SchedulingContext ctxt, String jobName, String groupName) throws JobPersistenceException
    {
        throw new UnsupportedOperationException();
    }

    public void pauseJobGroup(SchedulingContext ctxt, String groupName) throws JobPersistenceException
    {
        throw new UnsupportedOperationException();
    }

    public void resumeTrigger(SchedulingContext ctxt, String triggerName, String groupName)
            throws JobPersistenceException
    {
        throw new UnsupportedOperationException();
    }

    public void resumeTriggerGroup(SchedulingContext ctxt, String groupName) throws JobPersistenceException
    {
        throw new UnsupportedOperationException();
    }

    public void resumeJob(SchedulingContext ctxt, String jobName, String groupName) throws JobPersistenceException
    {
        throw new UnsupportedOperationException();
    }

    public void resumeJobGroup(SchedulingContext ctxt, String groupName) throws JobPersistenceException
    {
        throw new UnsupportedOperationException();
    }

    public void pauseAll(SchedulingContext ctxt) throws JobPersistenceException
    {
        throw new UnsupportedOperationException();
    }

    public void resumeAll(SchedulingContext ctxt) throws JobPersistenceException
    {
        throw new UnsupportedOperationException();
    }

    public Trigger acquireNextTrigger(final SchedulingContext ctxt, final long noLaterThan)
            throws JobPersistenceException
    {
        if (!memoryStoreInited)
        {
            initialise(ctxt);
        }
        return memoryStore.acquireNextTrigger(ctxt, noLaterThan);
    }

    public void releaseAcquiredTrigger(SchedulingContext ctxt, Trigger trigger) throws JobPersistenceException
    {
        if (!memoryStoreInited)
        {
            initialise(ctxt);
        }
        memoryStore.releaseAcquiredTrigger(ctxt, trigger);
    }

    public TriggerFiredBundle triggerFired(SchedulingContext ctxt, Trigger trigger) throws JobPersistenceException
    {
        if (!memoryStoreInited)
        {
            initialise(ctxt);
        }
        TriggerFiredBundle toReturn = memoryStore.triggerFired(ctxt, trigger);
        persistTrigger(trigger, true);
        return toReturn;
    }

    public void triggeredJobComplete(SchedulingContext ctxt, Trigger trigger, JobDetail jobDetail, int triggerInstCode)
            throws JobPersistenceException
    {
        if (!memoryStoreInited)
        {
            initialise(ctxt);
        }
        memoryStore.triggeredJobComplete(ctxt, trigger, jobDetail, triggerInstCode);
    }

    private GenericDelegator getDelegator()
    {
        // We look up the delegator by name because this class is often called directly from a Quartz thread with no
        // tenant context, so getDelegator() won't work.
        return GenericDelegator.getGenericDelegator(delegatorNameRef.get());
    }
}
