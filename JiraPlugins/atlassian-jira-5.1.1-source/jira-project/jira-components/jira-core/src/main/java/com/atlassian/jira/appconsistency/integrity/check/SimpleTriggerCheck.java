package com.atlassian.jira.appconsistency.integrity.check;

import com.atlassian.jira.appconsistency.integrity.amendment.Amendment;
import com.atlassian.jira.appconsistency.integrity.exception.IntegrityException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.scheduler.cron.SimpleToCronTriggerConverter;
import com.atlassian.jira.util.SimpleToCronUtil;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

import java.util.ArrayList;
import java.util.List;

/**
 * Integrity checker to verify that no Quartz SimpleTriggers for subscriptions exist in the
 * database, that are not attached to any subscriptions.
 * <p/>
 * The checker will scan the 'qrtz_triggers' table for simple triggers with a trigger_group matching
 * "SEND_SUBSCRIPTION" without a matching subscription entry.
 */
public class SimpleTriggerCheck extends CheckImpl
{
    private static final Logger log = Logger.getLogger(SimpleTriggerCheck.class);
    private static final String SEND_SUBSCRIPTION = "SEND_SUBSCRIPTION";

    public SimpleTriggerCheck(OfBizDelegator ofBizDelegator, int id)
    {
        super(ofBizDelegator, id);
    }

    public String getDescription()
    {
        return getI18NBean().getText("admin.integrity.check.filter.subscriptions.simple.trigger.desc");
    }

    public List preview() throws IntegrityException
    {
        return doCheck(false);
    }

    public List correct() throws IntegrityException
    {
        return doCheck(true);
    }

    private List doCheck(boolean correct)
    {
        Scheduler scheduler = ComponentAccessor.getScheduler();
        List result = new ArrayList();
        SimpleToCronUtil simpleToCronUtil = new SimpleToCronUtil(scheduler, new SimpleToCronTriggerConverter());

        boolean restartScheduler = false;

        try
        {
            String[] triggerNames = scheduler.getTriggerNames(SEND_SUBSCRIPTION);
            if (triggerNames != null)
            {
                if (correct)
                {
                    restartScheduler = simpleToCronUtil.pauseScheduler();
                }

                for (int i = 0; i < triggerNames.length; i++)
                {
                    String triggerName = triggerNames[i];
                    Trigger trigger = scheduler.getTrigger(triggerName, SEND_SUBSCRIPTION);
                    if (trigger instanceof SimpleTrigger)
                    {
                        String subscriptionId = triggerName.substring("SUBSCRIPTION_".length());
                        GenericValue subscriptionGV = ofBizDelegator.findById("FilterSubscription", new Long(subscriptionId));
                        final SimpleTrigger simpleTrigger = (SimpleTrigger) trigger;
                        if (subscriptionGV == null)
                        {
                            if (correct)
                            {
                                simpleToCronUtil.unscheduleJob(simpleTrigger);
                                result.add(new DeleteTriggerAmendmentImpl(Amendment.CORRECTION, getI18NBean().getText("admin.integrity.check.filter.subscriptions.simple.trigger.message", triggerName), simpleTrigger));
                            }
                            else
                            {
                                result.add(new DeleteTriggerAmendmentImpl(Amendment.ERROR, getI18NBean().getText("admin.integrity.check.filter.subscriptions.simple.trigger.preview", triggerName), simpleTrigger));
                            }
                        }
                        else
                        {
                            // If we do find a SimpleTrigger linked to an existing subscription, we should try to convert it to a CronTrigger.
                            if (correct)
                            {
                                simpleToCronUtil.convertSimpleToCronTrigger(simpleTrigger);
                                result.add(new DeleteTriggerAmendmentImpl(Amendment.CORRECTION, getI18NBean().getText("admin.integrity.check.filter.subscriptions.simple.trigger.with.subscription.message", triggerName), simpleTrigger));
                            }
                            else
                            {
                                result.add(new DeleteTriggerAmendmentImpl(Amendment.ERROR, getI18NBean().getText("admin.integrity.check.filter.subscriptions.simple.trigger.with.subscription.preview", triggerName), simpleTrigger));
                            }

                        }
                    }
                }
            }
        }
        catch (SchedulerException e)
        {
            log.error("Error retrieving all triggers!", e);
        }
        finally
        {
            simpleToCronUtil.restartScheduler(restartScheduler);
        }

        return result;
    }

    public boolean isAvailable()
    {
        return true;
    }

    public String getUnavailableMessage()
    {
        return "";
    }
}
