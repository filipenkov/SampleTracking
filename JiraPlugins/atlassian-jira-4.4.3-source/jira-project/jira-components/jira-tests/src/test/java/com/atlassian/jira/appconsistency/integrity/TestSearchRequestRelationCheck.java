/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.appconsistency.integrity;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.ofbiz.util.CoreTransactionUtil;
import com.atlassian.jira.appconsistency.integrity.check.SearchRequestRelationCheck;
import com.atlassian.jira.appconsistency.integrity.exception.IntegrityException;
import com.atlassian.jira.appconsistency.integrity.amendment.Amendment;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.ofbiz.DefaultOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.core.util.map.EasyMap;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

public class TestSearchRequestRelationCheck extends LegacyJiraMockTestCase
{
    private GenericDelegator genericDelegator = CoreFactory.getGenericDelegator();
    private OfBizDelegator ofBizDelegator  = new DefaultOfBizDelegator(genericDelegator);
    private SearchRequestRelationCheck srCheck;

    public TestSearchRequestRelationCheck(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        CoreTransactionUtil.setUseTransactions(false);


        Long id = new Long(1000);
        UtilsForTests.getTestEntity("SearchRequest", EasyMap.build("id", id, "project", new Long(1000), "name", "Test Request " + id));
        id = new Long(1001);
        UtilsForTests.getTestEntity("SearchRequest", EasyMap.build("id", id, "project", new Long(1001), "name", "Test Request " + id));
        id = new Long(1002);
        UtilsForTests.getTestEntity("SearchRequest", EasyMap.build("id", id, "project", new Long(1002), "name", "Test Request " + id));

        UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(1000)));


        srCheck = new SearchRequestRelationCheck(ofBizDelegator, 1);

    }

    public void testPreview() throws IntegrityException, GenericEntityException
    {
        List amendments = srCheck.preview();
        assertEquals(2, amendments.size());

        for (Iterator iterator = amendments.iterator(); iterator.hasNext();)
        {
            Amendment amendment = (Amendment) iterator.next();
            assertTrue(amendment.isError());
            assertTrue(amendment.getMessage().equals("Test Request 1001 - this search request references an invalid project.") ||
                       amendment.getMessage().equals("Test Request 1002 - this search request references an invalid project.") ) ;
        }

        // There should still be 3 Search Requests lefft as the preview method should not modify the database
        // There should be 1 entry
        List allRequests = genericDelegator.findAll("SearchRequest");
        assertEquals(3, allRequests.size());

        List ids = new ArrayList();
        for (Iterator iterator = allRequests.iterator(); iterator.hasNext();)
        {
            GenericValue genericValue = (GenericValue) iterator.next();
            ids.add(genericValue.getLong("id"));

        }

        assertTrue(ids.contains(new Long(1000)));
        assertTrue(ids.contains(new Long(1001)));
        assertTrue(ids.contains(new Long(1002)));
    }

    public void testCorrect() throws IntegrityException, GenericEntityException
    {
        // This should correct the problem
        List amendments = srCheck.correct();
        assertEquals(2, amendments.size());

        for (Iterator iterator = amendments.iterator(); iterator.hasNext();)
        {
            Amendment amendment = (Amendment) iterator.next();
            assertTrue(amendment.isError());
            assertTrue(amendment.getMessage().equals("Test Request 1001 - removed this search request as it referenced an invalid project.") ||
                       amendment.getMessage().equals("Test Request 1002 - removed this search request as it referenced an invalid project.") ) ;
        }

        // There should be 1 entry
        List allRequests = genericDelegator.findAll("SearchRequest");
        assertEquals(1, allRequests.size());
        GenericValue requestGV = (GenericValue) allRequests.iterator().next();
        assertEquals(new Long(1000), requestGV.getLong("id"));

        // This should return no amendments as they have just been corrected.
        amendments = srCheck.preview();
        assertTrue(amendments.isEmpty());
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        CoreTransactionUtil.setUseTransactions(true);
        this.genericDelegator = null;
        this.ofBizDelegator = null;
        this.srCheck = null;
    }
}
