/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.subscription;

import com.atlassian.jira.ManagerFactory;
import org.ofbiz.core.entity.GenericValue;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class SendFilterJob implements Job
{
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        SubscriptionManager dsm = ManagerFactory.getSubscriptionManager();
        try
        {
            GenericValue subscription = dsm.getSubscriptionFromTriggerName(context.getTrigger().getName());
            if (subscription == null) throw new JobExecutionException("No filter subscription for trigger "+context.getTrigger().getName());
            dsm.runSubscription(subscription);
        }
        catch (Exception e)
        {
            throw new JobExecutionException("Error retrieving schedule entry for this job.", e, false);
        }
    }
}
