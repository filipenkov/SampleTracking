package com.atlassian.jira.appconsistency.integrity.check;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.appconsistency.integrity.amendment.Amendment;
import com.atlassian.jira.appconsistency.integrity.amendment.DeleteEntityAmendment;
import com.atlassian.jira.appconsistency.integrity.exception.IntegrityException;
import com.atlassian.jira.issue.subscription.DefaultSubscriptionManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

/**
 * Checks and fixes the case where we have a FilterSubscription that has no corresponding
 * Quartz trigger.
 */
public class FilterSubscriptionsTriggerCheck extends BaseFilterSubscriptionsCheck
{

    public FilterSubscriptionsTriggerCheck(OfBizDelegator ofBizDelegator, int id)
    {
        super(ofBizDelegator, id);
    }

    public String getDescription()
    {
        return getI18NBean().getText("admin.integrity.check.filter.subscriptions.trigger.desc");
    }

    // Ensure that the filter subscriptions table does not contain references to search requests that have been deleted.
    protected void doRealCheck(boolean correct, GenericValue subscription, List messages) throws IntegrityException
    {
        // try to find the related quartz trigger, if null then flag
        GenericValue trigger = getTriggerGV(subscription);

        if (trigger == null)
        {
            if (correct)
            {
                // flag the current subscription for deletion
                messages.add(new DeleteEntityAmendment(Amendment.CORRECTION, getI18NBean().getText("admin.integrity.check.filter.subscriptions.trigger.message", subscription.getString("id")), subscription));
            }
            else
            {
                messages.add(new DeleteEntityAmendment(Amendment.ERROR, getI18NBean().getText("admin.integrity.check.filter.subscriptions.trigger.preview", subscription.getString("id")), subscription));
            }
        }
    }

    private GenericValue getTriggerGV(GenericValue subscription)
    {
        List vals = ofBizDelegator.findByAnd("QRTZTriggers", EasyMap.build("triggerName", DefaultSubscriptionManager.SUBSCRIPTION_PREFIX + subscription.getString("id")));
        if (vals != null && vals.size() == 1) {
            return (GenericValue) vals.get(0);
        }
        return null;
    }

}
