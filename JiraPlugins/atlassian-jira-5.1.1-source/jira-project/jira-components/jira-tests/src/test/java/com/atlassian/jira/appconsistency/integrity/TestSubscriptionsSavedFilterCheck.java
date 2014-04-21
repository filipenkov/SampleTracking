/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.appconsistency.integrity;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.ofbiz.util.CoreTransactionUtil;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.appconsistency.integrity.amendment.DeleteEntityAmendment;
import com.atlassian.jira.appconsistency.integrity.check.FilterSubscriptionsSavedFilterCheck;
import com.atlassian.jira.appconsistency.integrity.exception.IntegrityException;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.ofbiz.DefaultOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class TestSubscriptionsSavedFilterCheck extends AbstractUsersTestCase
{
    private GenericDelegator genericDelegator;
    private FilterSubscriptionsSavedFilterCheck filterCheck;

    public TestSubscriptionsSavedFilterCheck(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        CoreTransactionUtil.setUseTransactions(false);

        // Create 3 subs but only one has a search request
        createMockUser("nick");
        UtilsForTests.getTestEntity("SearchRequest", EasyMap.build("id", new Long(1), "name", "my search request", "project", new Long(1), "author", "nick", "request", ""));
        UtilsForTests.getTestEntity("FilterSubscription", EasyMap.build("id", new Long(1), "filterID", new Long(1), "username", "nick"));

        UtilsForTests.getTestEntity("FilterSubscription", EasyMap.build("id", new Long(2), "filterID", new Long(2), "username", "nick"));
        UtilsForTests.getTestEntity("FilterSubscription", EasyMap.build("id", new Long(3), "filterID", new Long(3), "username", "nick"));


        genericDelegator = CoreFactory.getGenericDelegator();
        OfBizDelegator ofBizDelegator = new DefaultOfBizDelegator(genericDelegator);
        filterCheck = new FilterSubscriptionsSavedFilterCheck(ofBizDelegator, 1);

    }

    public void testPreview() throws IntegrityException, GenericEntityException
    {
        List amendments = filterCheck.preview();
        assertEquals(2, amendments.size());

        assertAmendments(amendments);
        Collection ids;

        // The preview method should not modify the database. Therefore we should still have 3 Filter Subscriptions left
        List filterSubscriptionGVs = genericDelegator.findAll("FilterSubscription");
        assertEquals(3, filterSubscriptionGVs.size());

        ids = new ArrayList();
        for (Iterator iterator = filterSubscriptionGVs.iterator(); iterator.hasNext();)
        {
            GenericValue filterSubscriptionGV = (GenericValue) iterator.next();
            ids.add(filterSubscriptionGV.getLong("id"));
        }

        assertTrue(ids.contains(new Long(1)));
        assertTrue(ids.contains(new Long(2)));
        assertTrue(ids.contains(new Long(3)));
    }

    private void assertAmendments(List amendments)
    {
        Collection ids = new ArrayList();
        for (Iterator iterator = amendments.iterator(); iterator.hasNext();)
        {
            DeleteEntityAmendment amendment = (DeleteEntityAmendment) iterator.next();
            ids.add(amendment.getEntity().getLong("id"));
        }

        assertTrue(ids.contains(new Long(2)));
        assertTrue(ids.contains(new Long(3)));
    }

    public void testCorrect() throws IntegrityException, GenericEntityException
    {
        // This should correct the problem by adding Workflow Entries
        List amendments = filterCheck.correct();
        assertEquals(2, amendments.size());

        assertAmendments(amendments);

        // There should be 3 workflow entries
        List filterSubscriptionGVs = genericDelegator.findAll("FilterSubscription");
        assertEquals(1, filterSubscriptionGVs.size());
        GenericValue filterSubscriptionGV = (GenericValue) filterSubscriptionGVs.iterator().next();
        assertEquals(new Long(1), filterSubscriptionGV.getLong("id"));

        // This should return no amendments as they have just been corrected.
        amendments = filterCheck.preview();
        assertTrue(amendments.isEmpty());
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
        CoreTransactionUtil.setUseTransactions(true);
        this.genericDelegator = null;
        this.filterCheck = null;

    }
}
