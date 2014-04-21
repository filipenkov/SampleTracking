/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.appconsistency.integrity;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.ofbiz.util.CoreTransactionUtil;
import com.atlassian.jira.appconsistency.integrity.amendment.CreateEntityAmendment;
import com.atlassian.jira.appconsistency.integrity.check.PrimaryEntityRelationCreate;
import com.atlassian.jira.appconsistency.integrity.exception.IntegrityException;
import com.atlassian.jira.appconsistency.integrity.integritycheck.EntityIntegrityCheckImpl;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.ofbiz.DefaultOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.core.util.map.EasyMap;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class TestPrimaryEntityRelationCreate extends LegacyJiraMockTestCase
{
    private GenericDelegator genericDelegator;
    private PrimaryEntityRelationCreate simpleRelationEntityCheck;

    public TestPrimaryEntityRelationCreate(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        GenericValue project = UtilsForTests.getTestEntity("Project", EasyMap.build("key", "ABC"));

        // Create 3 Issues but only one has a workflow entry
        GenericValue wfe = UtilsForTests.getTestEntity("OSWorkflowEntry", EasyMap.build("id", new Long(2000), "name", "jira", "state", new Integer(0)));
        UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(1000), "project", project.getLong("id"), "key", "ABC-1", "workflowId", wfe.getLong("id")));
        UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(1001), "project", project.getLong("id"), "key", "ABC-2"));
        UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(1002), "project", project.getLong("id"), "key", "ABC-3"));

        genericDelegator = CoreFactory.getGenericDelegator();
        OfBizDelegator ofBizDelegator = new DefaultOfBizDelegator(genericDelegator);
        simpleRelationEntityCheck = new PrimaryEntityRelationCreate(ofBizDelegator, 1, "Related", "OSWorkflowEntry", "workflowId", EasyMap.build("name", "jira", "state", new Integer(0)));

        // Need to create this so the Entity Check has access to the entity it is operating on.
        new EntityIntegrityCheckImpl(1, "Mock", "Issue", simpleRelationEntityCheck);
    }

    public void testPreview() throws IntegrityException, GenericEntityException
    {
        List amendments = simpleRelationEntityCheck.preview();
        assertEquals(2, amendments.size());

        assertAmendments(amendments);

        // The preview method should not modify anything so there should only be one entry in the database
        List workflowEntries = genericDelegator.findAll("OSWorkflowEntry");
        assertEquals(1, workflowEntries.size());

        GenericValue workflowEntry = (GenericValue) workflowEntries.iterator().next();
        assertEquals(new Long(2000), workflowEntry.getLong("id"));
        assertWorkflowEntry(workflowEntry);
    }

    private void assertAmendments(List amendments)
    {
        Collection ids = new ArrayList();
        for (Iterator iterator = amendments.iterator(); iterator.hasNext();)
        {
            CreateEntityAmendment amendment = (CreateEntityAmendment) iterator.next();
            ids.add(amendment.getEntity().getLong("id"));
        }

        assertTrue(ids.contains(new Long(1001)));
        assertTrue(ids.contains(new Long(1002)));
    }

    public void testCorrect() throws IntegrityException, GenericEntityException
    {
        // This should correct the problem by adding Workflow Entries
        List amendments = simpleRelationEntityCheck.correct();
        assertEquals(2, amendments.size());

        assertAmendments(amendments);

        // There should be 3 workflow entries as 2 missing ones got created
        List allIssues = genericDelegator.findAll("OSWorkflowEntry");
        assertEquals(3, allIssues.size());

        List issueGVs = genericDelegator.findAll("Issue");
        assertEquals(3, issueGVs.size());

        // Test 1st Issue
        GenericValue issueGV = genericDelegator.findByPrimaryKey("Issue", EasyMap.build("id", new Long(1000)));
        Long workflowId = issueGV.getLong("workflowId");
        assertEquals(new Long(2000), workflowId);
        GenericValue workflowEntry = genericDelegator.findByPrimaryKey("OSWorkflowEntry", EasyMap.build("id", workflowId));
        assertNotNull(workflowEntry);
        assertWorkflowEntry(workflowEntry);

        // Test 2nd Issue
        issueGV = genericDelegator.findByPrimaryKey("Issue", EasyMap.build("id", new Long(1001)));
        workflowId = issueGV.getLong("workflowId");
        assertNotNull(workflowId);
        assertFalse(workflowId.equals(new Long(2000)));
        workflowEntry = genericDelegator.findByPrimaryKey("OSWorkflowEntry", EasyMap.build("id", workflowId));
        assertNotNull(workflowEntry);
        assertWorkflowEntry(workflowEntry);

        // Test 3rd Issue
        issueGV = genericDelegator.findByPrimaryKey("Issue", EasyMap.build("id", new Long(1002)));
        workflowId = issueGV.getLong("workflowId");
        assertNotNull(workflowId);
        assertFalse(workflowId.equals(new Long(2000)));
        workflowEntry = genericDelegator.findByPrimaryKey("OSWorkflowEntry", EasyMap.build("id", workflowId));
        assertNotNull(workflowEntry);
        assertWorkflowEntry(workflowEntry);

        // This should return no amendments as they have just been corrected.
        amendments = simpleRelationEntityCheck.preview();
        assertTrue(amendments.isEmpty());
    }

    private void assertWorkflowEntry(GenericValue workflowEntry)
    {
        assertEquals("jira", workflowEntry.getString("name"));
        assertEquals(new Integer(0), workflowEntry.getInteger("state"));
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
        CoreTransactionUtil.setUseTransactions(true);
        this.genericDelegator = null;
        this.simpleRelationEntityCheck = null;
    }
}
