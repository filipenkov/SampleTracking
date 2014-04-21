/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.appconsistency.integrity;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.ofbiz.util.CoreTransactionUtil;
import com.atlassian.jira.appconsistency.integrity.check.FilterSubscriptionsTriggerCheck;
import com.atlassian.jira.appconsistency.integrity.exception.IntegrityException;
import com.atlassian.jira.appconsistency.integrity.amendment.DeleteEntityAmendment;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.ofbiz.DefaultOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.core.util.map.EasyMap;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

public class TestSubscriptionsTriggerCheck extends LegacyJiraMockTestCase
{
    private GenericDelegator genericDelegator;
    private FilterSubscriptionsTriggerCheck triggerCheck;

    public TestSubscriptionsTriggerCheck(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        CoreTransactionUtil.setUseTransactions(false);

        // Create 3 subs but only one has a trigger
        GenericValue sub = UtilsForTests.getTestEntity("FilterSubscription", EasyMap.build("id", new Long(1000)));
        UtilsForTests.getTestEntity("QRTZTriggers", EasyMap.build("triggerName", "SUBSCRIPTION_" + sub.getLong("id")));
        UtilsForTests.getTestEntity("FilterSubscription", EasyMap.build("id", new Long(1001)));
        UtilsForTests.getTestEntity("FilterSubscription", EasyMap.build("id", new Long(1002)));


        genericDelegator = CoreFactory.getGenericDelegator();
        OfBizDelegator ofBizDelegator  = new DefaultOfBizDelegator(genericDelegator);
        triggerCheck = new FilterSubscriptionsTriggerCheck(ofBizDelegator, 1);

    }

    public void testPreview() throws IntegrityException, GenericEntityException
    {
        List amendments = triggerCheck.preview();
        assertEquals(2, amendments.size());

        assertAmendments(amendments);
        Collection ids;

        // The preview method should not update the database so there should still be 3 Subscriptions
        List filterSubscriptGVs = genericDelegator.findAll("FilterSubscription");
        assertEquals(3, filterSubscriptGVs.size());

        ids = new ArrayList();
        for (Iterator iterator = filterSubscriptGVs.iterator(); iterator.hasNext();)
        {
            GenericValue filterSubscriptionGV = (GenericValue) iterator.next();
            ids.add(filterSubscriptionGV.getLong("id"));
        }

        assertTrue(ids.contains(new Long(1000)));
        assertTrue(ids.contains(new Long(1001)));
        assertTrue(ids.contains(new Long(1002)));
    }

    private void assertAmendments(List amendments)
    {
        Collection ids = new ArrayList();
        for (Iterator iterator = amendments.iterator(); iterator.hasNext();)
        {
            DeleteEntityAmendment amendment = (DeleteEntityAmendment) iterator.next();
            ids.add(amendment.getEntity().getLong("id"));
        }

        assertTrue(ids.contains(new Long(1001)));
        assertTrue(ids.contains(new Long(1002)));
    }

    public void testCorrect() throws IntegrityException, GenericEntityException
    {
        // This should correct the problem by adding Workflow Entries
        List amendments = triggerCheck.correct();
        assertEquals(2, amendments.size());

        assertAmendments(amendments);

        // There should be 3 workflow entries
        List filterSubscriptionGVs = genericDelegator.findAll("FilterSubscription");
        assertEquals(1, filterSubscriptionGVs.size());
        GenericValue filterSubscriptionGV = (GenericValue) filterSubscriptionGVs.iterator().next();
        assertEquals(new Long(1000), filterSubscriptionGV.getLong("id"));

        // This should return no amendments as they have just been corrected.
        amendments = triggerCheck.preview();
        assertTrue(amendments.isEmpty());
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        CoreTransactionUtil.setUseTransactions(true);
        this.genericDelegator = null;
        this.triggerCheck = null;

    }
}
