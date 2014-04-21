/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.subscription;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.DateUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.local.AbstractUsersIndexingTestCase;
import com.atlassian.jira.mail.SubscriptionMailQueueItem;
import com.atlassian.jira.mail.SubscriptionMailQueueItemFactory;
import com.atlassian.jira.ofbiz.DefaultOfBizDelegator;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.template.TemplateManager;
import com.atlassian.mail.queue.MailQueue;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.constraint.IsInstanceOf;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import org.ofbiz.core.entity.GenericEntity;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class TestDefaultSubscriptionManager extends AbstractUsersIndexingTestCase
{
    private final long runAt = new Date().getTime();
    private GenericValue subscrip;
    private User u;
    private Mock mailQueue;
    private GenericEntity job;
    private final Long filterId = new Long(1);
    private Mock groupManager;

    public TestDefaultSubscriptionManager(final String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        //        ManagerFactory.addService(SubscriptionManager.class, ComponentManager.getComponentInstanceOfType(SubscriptionManager.class));
        //        ManagerFactory.addService(SubscriptionManager.class, new DefaultSubscriptionManager(new DefaultOfBizDelegator(CoreFactory.getGenericDelegator()),ManagerFactory.getScheduler(), ManagerFactory.getMailQueue()));

        u = createMockUser("owen", "owen", "owen@atlassian.com");

        final Map columns = EasyMap.build("filterID", filterId, "username", "owen", "group", "group", "lastRun", new Timestamp(runAt));
        columns.put("emailOnEmpty", Boolean.TRUE.toString());
        subscrip = UtilsForTests.getTestEntity("FilterSubscription", columns);

        mailQueue = new Mock(MailQueue.class);
        groupManager = new Mock(GroupManager.class);
    }

    @Override
    protected void tearDown() throws Exception
    {
        ManagerFactory.removeService(Scheduler.class);
        super.tearDown();
    }

    private void setUp2()
    {
        Map columns = EasyMap.build("jobName", "SEND_SUBSCRIPTION", "jobGroup", "SEND_SUBSCRIPTION", "className",
            "com.atlassian.jira.issue.subscription.SendFilterJob", "isDurable", "true", "isStateful", "false", "requestsRecovery", "false");
        job = UtilsForTests.getTestEntity("QRTZJobDetails", columns);

        columns = EasyMap.build("triggerName", "SUBSCRIPTION_" + subscrip.getLong("id"), "triggerGroup", "SEND_SUBSCRIPTION", "job",
            job.getLong("id"), "triggerType", "SIMPLE");

        final GenericValue trigger = UtilsForTests.getTestEntity("QRTZTriggers", columns);

        columns = EasyMap.build("trigger", trigger.getLong("id"), "repeatCount", new Integer(-1), "repeatInterval", new Long(DateUtils.DAY_MILLIS));
        UtilsForTests.getTestEntity("QRTZSimpleTriggers", columns);
    }

    public void testHasSubscription() throws GenericEntityException
    {
        List<String> groupNames = new ArrayList<String>();
        groupManager.expectAndReturn("getGroupNamesForUser", P.args(new IsEqual("owen")), groupNames);

        final SubscriptionManager sm = getSubscriptionManager(null, null, null, null);
        assertTrue(sm.hasSubscription(u, new Long(1)));
    }

    public void testGetSubscription() throws GenericEntityException
    {
        final SubscriptionManager sm = getSubscriptionManager(null, null, null, null);
        final GenericValue subscription = sm.getSubscription(u, new Long(1));
        assertNotNull(subscription);
        assertEquals(subscrip, subscription);
    }

    public void testGetSubscriptions()
            throws GenericEntityException, OperationNotPermittedException, InvalidGroupException, InvalidUserException, InvalidCredentialException
    {
        List<String> groupNames = new ArrayList<String>();

        groupManager.expectAndReturn("getGroupNamesForUser", P.args(new IsEqual("owen")), groupNames);
        
        // Test retrieving group subscriptions for a filter given a user
        final SubscriptionManager sm = getSubscriptionManager(null, null, null, null);

        // Test if the subscription owned by the user is returned
        List subscriptions = sm.getSubscriptions(u, filterId);
        assertNotNull(subscriptions);
        assertEquals(1, subscriptions.size());
        assertEquals(subscrip, subscriptions.iterator().next());

        // Create a subscription that is shared with a group that user does not belong to
        final User testUser = createMockUser("test");
        final Group anotherGroup = createMockGroup("anothergroup");
        final GenericValue anotherSubscription = UtilsForTests.getTestEntity("FilterSubscription", EasyMap.build("filterID", filterId, "username",
            testUser.getName(), "group", anotherGroup.getName(), "lastRun", new Timestamp(runAt)));

        // Ensure that only the old subscription is returned
        subscriptions = sm.getSubscriptions(u, filterId);
        assertNotNull(subscriptions);
        assertEquals(1, subscriptions.size());
        assertEquals(subscrip, subscriptions.iterator().next());

        // Put the user into the group
        groupNames.add("anothergroup");
        groupManager.expectAndReturn("getGroupNamesForUser", P.args(new IsEqual("owen")), groupNames);

        // Now test that both subscriptions come back
        subscriptions = sm.getSubscriptions(u, filterId);
        assertNotNull(subscriptions);
        assertEquals(2, subscriptions.size());
        assertTrue(subscriptions.contains(subscrip));
        assertTrue(subscriptions.contains(anotherSubscription));

        // Create a subscriptioned owned by the user by now shared with a group
        final GenericValue ownedSubscription = UtilsForTests.getTestEntity("FilterSubscription", EasyMap.build("filterID", filterId, "username",
            u.getName(), "group", null, "lastRun", new Timestamp(runAt)));

        // Ensure it is returned as the user owns it
        subscriptions = sm.getSubscriptions(u, filterId);
        assertNotNull(subscriptions);
        assertEquals(3, subscriptions.size());
        assertTrue(subscriptions.contains(subscrip));
        assertTrue(subscriptions.contains(anotherSubscription));
        assertTrue(subscriptions.contains(ownedSubscription));

        // Create another group
        groupManager.expectAndReturn("getGroupNamesForUser", P.args(new IsEqual("owen")), groupNames);

        // And anotehr subscription
        final GenericValue yetAnotherSubscription = UtilsForTests.getTestEntity("FilterSubscription", EasyMap.build("filterID", filterId, "username",
            testUser.getName(), "group", "testGroup", "lastRun", new Timestamp(runAt)));

        // Ensure that the subscription is not returned
        subscriptions = sm.getSubscriptions(u, filterId);
        assertNotNull(subscriptions);
        assertEquals(3, subscriptions.size());
        assertTrue(subscriptions.contains(subscrip));
        assertTrue(subscriptions.contains(anotherSubscription));
        assertTrue(subscriptions.contains(ownedSubscription));

        // Make the user member fo the group
        groupNames.add("testGroup");
        groupManager.expectAndReturn("getGroupNamesForUser", P.args(new IsEqual("owen")), groupNames);

        // Tets that the subscription is returned
        subscriptions = sm.getSubscriptions(u, filterId);
        assertNotNull(subscriptions);
        assertEquals(4, subscriptions.size());
        assertTrue(subscriptions.contains(subscrip));
        assertTrue(subscriptions.contains(anotherSubscription));
        assertTrue(subscriptions.contains(ownedSubscription));
        assertTrue(subscriptions.contains(yetAnotherSubscription));

        // Now remove the user from the groups
        groupNames.clear();
        groupManager.expectAndReturn("getGroupNamesForUser", P.args(new IsEqual("owen")), groupNames);

        // Test that only owned filters are returned
        subscriptions = sm.getSubscriptions(u, filterId);
        assertNotNull(subscriptions);
        assertEquals(2, subscriptions.size());
        assertTrue(subscriptions.contains(subscrip));
        assertTrue(subscriptions.contains(ownedSubscription));

    }

    public void testCreateSubscriptions() throws Exception
    {
        final Mock schedulerMock = new Mock(Scheduler.class);
        ManagerFactory.addService(Scheduler.class, (Scheduler) schedulerMock.proxy());

        schedulerMock.expectVoid("addJob", P.args(new IsInstanceOf(JobDetail.class), new IsEqual(Boolean.TRUE)));

        final SubscriptionManager sm = getSubscriptionManager((Scheduler) schedulerMock.proxy(), null, null, null);
        final GenericValue subscription = sm.createSubscription(u, new Long(1), null, new Long(3600), Boolean.FALSE);
        final List subs = CoreFactory.getGenericDelegator().findAll("FilterSubscription");
        assertTrue(subs.contains(subscription));

        schedulerMock.verify();
    }

    public void testUpdateSubscription() throws Exception
    {
        final Mock schedulerMock = new Mock(Scheduler.class);
        ManagerFactory.addService(Scheduler.class, (Scheduler) schedulerMock.proxy());
        setUp2();

        final SubscriptionManager sm = getSubscriptionManager((Scheduler) schedulerMock.proxy(), null, null, null);
        final SimpleTrigger simpleTrigger = new SimpleTrigger("SUBSCRIPTION_" + subscrip.getLong("id"), "SEND_SUBSCRIPTION", new Date(), null,
            SimpleTrigger.REPEAT_INDEFINITELY, DateUtils.DAY_MILLIS);

        schedulerMock.expectAndReturn("scheduleJob", P.args(new IsEqual(simpleTrigger)), new Date());

        sm.updateSubscription(u, subscrip.getLong("id"), "newgroup", simpleTrigger, true);

        final List subs = CoreFactory.getGenericDelegator().findAll("FilterSubscription");
        assertEquals(1, subs.size());

        schedulerMock.verify();
    }

    public void testDeleteSubscription() throws Exception
    {
        final Mock schedulerMock = new Mock(Scheduler.class);
        ManagerFactory.addService(Scheduler.class, (Scheduler) schedulerMock.proxy());
        setUp2();

        final SubscriptionManager sm = getSubscriptionManager((Scheduler) schedulerMock.proxy(), null, null, null);
        final SimpleTrigger simpleTrigger = new SimpleTrigger("SUBSCRIPTION_" + subscrip.getLong("id"), "SEND_SUBSCRIPTION", new Date(), null,
            SimpleTrigger.REPEAT_INDEFINITELY, DateUtils.DAY_MILLIS);

        schedulerMock.expectAndReturn("unscheduleJob", P.args(new IsEqual(DefaultSubscriptionManager.SUBSCRIPTION_PREFIX + subscrip.getLong("id")),
            new IsEqual(DefaultSubscriptionManager.SUBSCRIPTION_IDENTIFIER)), Boolean.TRUE);
        schedulerMock.expectAndReturn("getTrigger", P.args(new IsEqual(DefaultSubscriptionManager.SUBSCRIPTION_PREFIX + subscrip.getLong("id")),
            new IsEqual(DefaultSubscriptionManager.SUBSCRIPTION_IDENTIFIER)), simpleTrigger);

        sm.deleteSubscription(subscrip.getLong("id"));

        final List subs = CoreFactory.getGenericDelegator().findAll("FilterSubscription");
        assertTrue(subs.isEmpty());

        schedulerMock.verify();
    }

    public void testRunSubscription() throws Exception
    {
        final Timestamp ts = new Timestamp(new Date().getTime());
        mailQueue.expectVoid("addItem", P.args(new IsInstanceOf(SubscriptionMailQueueItem.class)));
        final SubscriptionManager sm = getSubscriptionManager(null, (MailQueue) mailQueue.proxy(), null, null);
        GenericValue sub = sm.getSubscription(u, subscrip.getLong("id"));
        assertTrue(sub.getTimestamp("lastRun").getTime() <= ts.getTime());

        sm.runSubscription(subscrip);
        sub = sm.getSubscription(u, subscrip.getLong("id"));
        assertTrue(sub.getTimestamp("lastRun").getTime() >= ts.getTime());

        mailQueue.verify();
    }

    public void testRunSubscriptionNow() throws Exception
    {
        final Timestamp ts = new Timestamp(new Date().getTime());
        mailQueue.expectVoid("addItem", P.args(new IsInstanceOf(SubscriptionMailQueueItem.class)));
        final SubscriptionManager sm = getSubscriptionManager(null, (MailQueue) mailQueue.proxy(), null, null);
        GenericValue sub = sm.getSubscription(u, subscrip.getLong("id"));
        assertTrue(sub.getTimestamp("lastRun").getTime() <= ts.getTime());

        sm.runSubscription(subscrip);
        sub = sm.getSubscription(u, subscrip.getLong("id"));
        assertTrue(sub.getTimestamp("lastRun").getTime() >= ts.getTime());

        mailQueue.verify();
    }

    private SubscriptionManager getSubscriptionManager(Scheduler scheduler, MailQueue mailQueue, TemplateManager templateManager, SubscriptionMailQueueItemFactory subscriptionMailQueueItemFactory)
    {
        final DefaultOfBizDelegator delegator = new DefaultOfBizDelegator(CoreFactory.getGenericDelegator());
        if (scheduler == null)
        {
            scheduler = ComponentAccessor.getScheduler();
        }
        if (mailQueue == null)
        {
            mailQueue = ManagerFactory.getMailQueue();
        }
        if (templateManager == null)
        {
            templateManager = ComponentManager.getInstance().getTemplateManager();
        }
        if (subscriptionMailQueueItemFactory == null)
        {
            subscriptionMailQueueItemFactory = ComponentManager.getInstance().getSubscriptionMailQueueItemFactory();
        }

        return new DefaultSubscriptionManager(delegator, mailQueue, templateManager, subscriptionMailQueueItemFactory, null, (GroupManager) groupManager.proxy());
    }
}
