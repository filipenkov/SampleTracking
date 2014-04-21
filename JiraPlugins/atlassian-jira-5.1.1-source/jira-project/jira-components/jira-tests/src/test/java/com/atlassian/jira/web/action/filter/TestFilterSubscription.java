/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.filter;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.DateUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.subscription.SubscriptionManager;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.web.util.OutlookDate;
import com.atlassian.query.QueryImpl;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.mockobjects.servlet.MockHttpServletResponse;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TestFilterSubscription extends AbstractUsersTestCase
{
    private final long runAt = System.currentTimeMillis();
    private User user;
    private SearchRequest searchRequest;
    private GenericValue srgv;

    public TestFilterSubscription(final String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        user = createMockUser("owen");

        JiraTestUtil.loginUser(user);

        searchRequest = ManagerFactory.getSearchRequestManager().create(new SearchRequest(new QueryImpl(), user.getName(), "An SR", null));

        Map columns = EasyMap.build("filterID", searchRequest.getId(), "username", "owen", "group", "group", "lastRun", new Timestamp(runAt));
        columns.put("emailOnEmpty", Boolean.FALSE.toString());
        srgv = UtilsForTests.getTestEntity("FilterSubscription", columns);

        columns = EasyMap.build("jobName", "SEND_SUBSCRIPTION", "jobGroup", "SEND_SUBSCRIPTION", "className",
            "com.atlassian.jira.issue.subscription.SendFilterJob", "isDurable", "true", "isStateful", "false", "requestsRecovery", "false");

        final GenericValue job = UtilsForTests.getTestEntity("QRTZJobDetails", columns);

        columns = EasyMap.build("triggerName", "SUBSCRIPTION_" + srgv.getLong("id"), "triggerGroup", "SEND_SUBSCRIPTION", "job", job.getLong("id"),
            "triggerType", "SIMPLE", "startTime", new Timestamp(new Date().getTime()));

        final GenericValue trigger = UtilsForTests.getTestEntity("QRTZTriggers", columns);

        columns = EasyMap.build("trigger", trigger.getLong("id"), "repeatCount", new Integer(-1), "repeatInterval", new Long(DateUtils.DAY_MILLIS));
        UtilsForTests.getTestEntity("QRTZSimpleTriggers", columns);
    }

    public void testGetsSets()
    {
        final FilterSubscription fs = new FilterSubscription(null, null, null, null, null);
        assertNull(fs.getFilterId());
        fs.setFilterId("1000");
        assertEquals("1000", fs.getFilterId());
        assertNull(fs.getSubId());
        fs.setSubId("100");
        assertEquals("100", fs.getSubId());
        assertNull(fs.getGroupName());
        fs.setGroupName("Group");
        assertEquals("Group", fs.getGroupName());
        assertTrue(!fs.getEmailOnEmpty().booleanValue());
        fs.setEmailOnEmpty(Boolean.TRUE);
        assertTrue(fs.getEmailOnEmpty().booleanValue());

        assertNull(fs.getLastRun());
        fs.setLastRun(null);
        assertNull(fs.getLastRun());
        fs.setLastRun("100000");
        assertNotNull(fs.getLastRun());

        assertNull(fs.getNextRun());
        fs.setNextRun(null);
        assertNull(fs.getNextRun());
        fs.setNextRun("100000");
        assertNotNull(fs.getNextRun());
    }

    public void testGetsSets2()
    {
        final OutlookDate outlookDate = ManagerFactory.getOutlookDateManager().getOutlookDate(Locale.getDefault());
        final FilterSubscription fs = new FilterSubscription(null, null, null, null, null);

        assertNull(fs.getLastRunStr());
        fs.setLastRun(Long.toString(runAt));
        assertEquals(outlookDate.formatDMYHMS(new Date(runAt)), fs.getLastRunStr());

        assertNull(fs.getNextRunStr());
        fs.setNextRun(Long.toString(runAt));
        assertEquals(outlookDate.formatDMYHMS(new Date(runAt)), fs.getNextRunStr());
    }

    public void testDoDelete() throws Exception
    {
        final Mock mockSubManager = new Mock(SubscriptionManager.class);
        final SubscriptionManager subscriptionManager = (SubscriptionManager) ManagerFactory.addService(SubscriptionManager.class,
            (SubscriptionManager) mockSubManager.proxy()).getComponentInstance();

        try
        {
            final SearchRequest sr = ManagerFactory.getSearchRequestManager().create(new SearchRequest(new QueryImpl(), user.getName(), "An SR", null));

            final MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewSubscriptions.jspa?filterId=" + sr.getId());

            final GenericValue subscription = UtilsForTests.getTestEntity("FilterSubscription", new HashMap());
            mockSubManager.expectAndReturn("getSubscription", P.args(new IsEqual(user), new IsEqual(srgv.getLong("id"))), subscription);
            mockSubManager.expectVoid("deleteSubscription", P.args(new IsEqual(srgv.getLong("id"))));

            final FilterSubscription fs = new FilterSubscription(null, null, null, null, null);
            fs.setSubId(srgv.getLong("id").toString());
            fs.setFilterId(sr.getId().toString());

            final String result = fs.doDelete();
            assertEquals(Action.NONE, result);

            response.verify();
            mockSubManager.verify();
        }
        finally
        {
            ManagerFactory.addService(SubscriptionManager.class, subscriptionManager);
        }
    }

    public void testDoSendNow() throws Exception
    {
        final SearchRequest sr = ManagerFactory.getSearchRequestManager().create(new SearchRequest(new QueryImpl(), user.getName(), "An SR", null));
        final MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewSubscriptions.jspa?filterId=" + sr.getId());

        final Mock subscriptionManager = new Mock(SubscriptionManager.class);
        final SubscriptionManager oldSubscriptionManager = (SubscriptionManager) ManagerFactory.addService(SubscriptionManager.class,
            (SubscriptionManager) subscriptionManager.proxy()).getComponentInstance();

        try
        {

            subscriptionManager.setupResult("getSubscription", srgv);

            final Constraint[] constraints = new Constraint[] { new IsEqual(user), new IsEqual(srgv.getLong("id")) };
            subscriptionManager.expectVoid("runSubscription", constraints);

            final FilterSubscription fs = new FilterSubscription(null, null, null, null, null);
            fs.setFilterId(sr.getId().toString());
            fs.setSubId(srgv.getLong("id").toString());

            final String result = fs.doRunNow();
            assertEquals(Action.NONE, result);

            response.verify();
            subscriptionManager.verify();
        }
        finally
        {
            ManagerFactory.addService(SubscriptionManager.class, oldSubscriptionManager);
        }
    }

    public void testHasPermission() throws Exception
    {
        final SearchRequest sr = ManagerFactory.getSearchRequestManager().create(new SearchRequest(new QueryImpl(), user.getName(), "An SR", null));
        final FilterSubscription fs = new FilterSubscription(null, null, null, null, null);
        fs.setFilterId(sr.getId().toString());
        assertFalse(fs.hasGroupPermission());
    }

    public void testGetSubmitName() throws GenericEntityException
    {
        final FilterSubscription fs = new FilterSubscription(null, null, null, null, null);
        assertEquals("Subscribe", fs.getSubmitName());
        fs.setSubId("1");
        assertEquals("Update", fs.getSubmitName());
    }

    public void testGetCancelStr() throws GenericEntityException
    {
        final FilterSubscription fs = new FilterSubscription(null, null, null, null, null);
        assertEquals("ManageFilters.jspa", fs.getCancelStr());
        fs.setSubId("1");
        fs.setFilterId("1");
        assertEquals("ViewSubscriptions.jspa?filterId=1", fs.getCancelStr());
    }

    public void testGetGroups() throws OperationNotPermittedException, InvalidGroupException
    {
        final Group g1 = createMockGroup("group1");
        final Group g2 = createMockGroup("group2");

        addUserToGroup(user, g1);
        addUserToGroup(user, g2);

        final FilterSubscription fs = new FilterSubscription(null, null, null, null, null);
        final Collection groups = fs.getGroups();
        assertEquals(2, groups.size());
        assertTrue(groups.contains("group1"));
        assertTrue(groups.contains("group2"));
    }
}
