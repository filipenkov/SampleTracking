/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */

package com.atlassian.jira.issue.subscription;

import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.core.util.DateUtils;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.mail.MailingListCompiler;
import com.atlassian.jira.mail.SubscriptionMailQueueItemFactory;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.template.TemplateManager;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.mail.queue.MailQueue;
import com.atlassian.mail.queue.MailQueueItem;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityConditionList;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.user.ApplicationUsers.getKeyFor;

public class DefaultSubscriptionManager extends MailingListCompiler implements SubscriptionManager
{
    private static final Logger log = Logger.getLogger(DefaultSubscriptionManager.class);
    public static final String SUBSCRIPTION_PREFIX = "SUBSCRIPTION_";
    public static final String SUBSCRIPTION_IDENTIFIER = "SEND_SUBSCRIPTION";
    private final OfBizDelegator delegator;
    private final MailQueue mailQueue;
    private final SubscriptionMailQueueItemFactory subscriptionMailQueueItemFactory;
    private final GroupManager groupManager;

    public DefaultSubscriptionManager(final OfBizDelegator delegator, final MailQueue mailQueue,
            final TemplateManager templateManager, final SubscriptionMailQueueItemFactory subscriptionMailQueueItemFactory,
            final ProjectRoleManager projectRoleManager, final GroupManager groupManager)
    {
        super(templateManager, projectRoleManager);
        this.delegator = delegator;
        this.mailQueue = mailQueue;
        this.subscriptionMailQueueItemFactory = subscriptionMailQueueItemFactory;
        this.groupManager = groupManager;
    }

    @Override
    public boolean hasSubscription(final User user, final Long filterId) throws GenericEntityException
    {
        return !getSubscriptions(user, filterId).isEmpty();
    }

    private GenericValue getSubscription(final Long subId) throws GenericEntityException
    {
        return delegator.findById("FilterSubscription", subId);
    }

    @Override
    public GenericValue getSubscription(final User user, final Long subId) throws GenericEntityException
    {
        return EntityUtil.getOnly(delegator.findByAnd("FilterSubscription", EasyMap.build("username", getKeyFor(user), "id", subId)));
    }

    @Override
    public GenericValue getSubscriptionFromTriggerName(final String triggerName) throws GenericEntityException
    {
        return getSubscription(new Long(triggerName.substring(SUBSCRIPTION_PREFIX.length(), triggerName.length())));
    }

    @Override
    public List<GenericValue> getSubscriptions(final User user, final Long filterId) throws GenericEntityException
    {
        final List<EntityExpr> entityExpressions = new ArrayList<EntityExpr>();
        // Retrieve all subscriptions created by the user
        entityExpressions.add(new EntityExpr("username", EntityOperator.EQUALS, getKeyFor(user)));

        // Group shared subscriptions
        final Iterable<String> groups = groupManager.getGroupNamesForUser(user.getName());
        for (final String group : groups)
        {
            entityExpressions.add(new EntityExpr("group", EntityOperator.EQUALS, group));
        }

        // Get the expression which will return everything owned by the user or shared to one of user's groups
        final EntityCondition ownershipCondition = new EntityConditionList(entityExpressions, EntityOperator.OR);

        final EntityCondition filterCondition = new EntityExpr("filterID", EntityOperator.EQUALS, filterId);

        // Return the results
        return delegator.findByAnd("FilterSubscription", EasyList.build(filterCondition, ownershipCondition));
    }

    @Override
    public Trigger getTriggerFromSubscription(final GenericValue subscription) throws SchedulerException
    {
        return getScheduler().getTrigger(SUBSCRIPTION_PREFIX + subscription.getLong("id"), SUBSCRIPTION_IDENTIFIER);
    }

    @Override
    public void updateSubscription(final User user, final Long subId, final String groupName, final Trigger trigger, final Boolean emailOnEmpty) throws DataAccessException
    {
        try
        {
            getScheduler().unscheduleJob(SUBSCRIPTION_PREFIX + subId, SUBSCRIPTION_IDENTIFIER);

            final GenericValue subscriptionGV = getSubscription(user, subId);
            final Map<String, String> fields = MapBuilder.build("username", getKeyFor(user), "group", groupName, "emailOnEmpty", emailOnEmpty.toString());
            subscriptionGV.setFields(fields);
            subscriptionGV.store();

            if (!getScheduler().isPaused() && !getScheduler().isShutdown())
            {
                populateTriggerFields(trigger, subscriptionGV.getLong("id"));
                getScheduler().scheduleJob(trigger);
            }
            else
            {
                throw new IllegalStateException("The scheduler is paused or shutdown so the subscription was not created.");
            }
        }
        catch (final SchedulerException e)
        {
            throw new DataAccessException(e);
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    private void populateTriggerFields(final Trigger trigger, final Long subscriptionId) throws SchedulerException
    {
        final JobDetail jd = new JobDetail(SUBSCRIPTION_IDENTIFIER, SUBSCRIPTION_IDENTIFIER, SendFilterJob.class, false, true, false);

        trigger.setName(SUBSCRIPTION_PREFIX + subscriptionId);
        trigger.setGroup(SUBSCRIPTION_IDENTIFIER);
        trigger.setJobName(SUBSCRIPTION_IDENTIFIER);
        trigger.setJobGroup(SUBSCRIPTION_IDENTIFIER);

        getScheduler().addJob(jd, true);
    }

    @Override
    public List<GenericValue> getAllSubscriptions(final Long filterId)
    {
        return delegator.findByAnd("FilterSubscription", EasyMap.build("filterID", filterId));
    }

    @Override
    public List<GenericValue> getAllSubscriptions()
    {
        return delegator.findAll("FilterSubscription");
    }

    @Override
    public GenericValue createSubscription(final User user, final Long filterId, final String groupName, final Long period, final Boolean emailOnEmpty)
    {
        final Trigger trigger = new SimpleTrigger(SUBSCRIPTION_PREFIX, SUBSCRIPTION_IDENTIFIER, SUBSCRIPTION_IDENTIFIER, SUBSCRIPTION_IDENTIFIER,
                new Date(), null, SimpleTrigger.REPEAT_INDEFINITELY, (period.longValue() * DateUtils.SECOND_MILLIS));

        return createSubscription(user, filterId, groupName, trigger, emailOnEmpty);
    }

    @Override
    public GenericValue createSubscription(final User user, final Long filterId, String groupName, final Trigger trigger, final Boolean emailOnEmpty)
    {
        // Cannot store empty string here!!! As sybase will screw it up - JRA-8361
        if (!TextUtils.stringSet(groupName))
        {
            groupName = null;
        }

        final Map columns = EasyMap.build("filterID", filterId, "username", getKeyFor(user), "group", groupName, "lastRun", null);
        columns.put("emailOnEmpty", emailOnEmpty.toString());

        GenericValue subscriptionGV = null;
        try
        {
            if (!getScheduler().isPaused() && !getScheduler().isShutdown())
            {
                subscriptionGV = EntityUtils.createValue("FilterSubscription", columns);
                populateTriggerFields(trigger, subscriptionGV.getLong("id"));
                getScheduler().scheduleJob(trigger);
            }
            else
            {
                throw new IllegalStateException("The scheduler is paused or shutdown so the subscription was not created.");
            }
        }
        catch (final SchedulerException e)
        {
            throw new DataAccessException(e);
        }
        return subscriptionGV;

    }

    @Override
    public void deleteSubscription(final Long subId) throws Exception
    {
        final GenericValue subscriptionGV = getSubscription(subId);
        if (getScheduler().getTrigger(SUBSCRIPTION_PREFIX + subscriptionGV.getLong("id"), SUBSCRIPTION_IDENTIFIER) != null)
        {
            getScheduler().unscheduleJob(SUBSCRIPTION_PREFIX + subscriptionGV.getLong("id"), SUBSCRIPTION_IDENTIFIER);
        }
        else
        {
            log.warn("Unable to find a quartz trigger for the subscription: " + subscriptionGV.getLong("id") + " removing the subscription anyway.");
        }
        subscriptionGV.remove();
    }

    @Override
    public void deleteSubscriptionsForUser(final User user) throws Exception
    {
        final List subscriptionGvs = delegator.findByAnd("FilterSubscription", EasyMap.build("username", getKeyFor(user)));
        for (final Iterator iterator = subscriptionGvs.iterator(); iterator.hasNext();)
        {
            final GenericValue subscription = (GenericValue) iterator.next();
            deleteSubscription(subscription.getLong("id"));
        }
    }

    @Override
    public void runSubscription(final GenericValue sub) throws Exception
    {
        //Update the timestamps for the subscription so if it fails it won't get run every minute
        final Timestamp ts = new Timestamp(new Date().getTime());
        sub.set("lastRun", ts);
        sub.store();

        final MailQueueItem item = subscriptionMailQueueItemFactory.getSubscriptionMailQueueItem(sub);
        mailQueue.addItem(item);
    }

    @Override
    public void runSubscription(final User u, final Long subId) throws Exception
    {
        runSubscription(getSubscription(u, subId));
    }

    @Override
    public void deleteSubscriptionsForGroup(final Group group) throws Exception
    {
        final List subscriptionGvs = delegator.findByAnd("FilterSubscription", EasyMap.build("group", group.getName()));
        for (final Iterator iterator = subscriptionGvs.iterator(); iterator.hasNext();)
        {
            final GenericValue subscription = (GenericValue) iterator.next();
            deleteSubscription(subscription.getLong("id"));
        }

    }

    // We need to get the scheduler every time since it's reference can change when we do an import.
    Scheduler getScheduler()
    {
        return ComponentManager.getComponentInstanceOfType(Scheduler.class);
    }

}
