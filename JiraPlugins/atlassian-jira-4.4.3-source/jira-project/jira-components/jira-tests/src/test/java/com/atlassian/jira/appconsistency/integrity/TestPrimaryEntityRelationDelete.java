/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.appconsistency.integrity;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.util.CoreTransactionUtil;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.appconsistency.integrity.check.PrimaryEntityRelationDelete;
import com.atlassian.jira.appconsistency.integrity.exception.IntegrityException;
import com.atlassian.jira.appconsistency.integrity.integritycheck.EntityIntegrityCheckImpl;
import com.atlassian.jira.appconsistency.integrity.amendment.DeleteEntityAmendment;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.DefaultOfBizDelegator;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

public class TestPrimaryEntityRelationDelete extends LegacyJiraMockTestCase
{
    private GenericDelegator genericDelegator;
    private OfBizDelegator ofBizDelegator;
    private GenericValue issue1;
    private PrimaryEntityRelationDelete simpleRelationEntityCheck;

    public TestPrimaryEntityRelationDelete(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        CoreTransactionUtil.setUseTransactions(false);
        GenericValue project = UtilsForTests.getTestEntity("Project", EasyMap.build("key", "ABC"));

        // Create three issue or which two do no have a project.
        issue1 = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(1000), "project", project.getLong("id"), "key", "ABC-1"));
        UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(1001), "project", new Long(-1), "key", "ABC-2"));
        UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(1002), "project", new Long(-1), "key", "ABC-3"));

        genericDelegator = CoreFactory.getGenericDelegator();
        ofBizDelegator  = new DefaultOfBizDelegator(genericDelegator);
        simpleRelationEntityCheck = new PrimaryEntityRelationDelete(ofBizDelegator, 1, "Parent", "Project");

        // Need to create this so the Entity Check has access to the entity it is operating on.
        new EntityIntegrityCheckImpl(1, "Mock", "Issue", simpleRelationEntityCheck);
    }

    public void testPreview() throws IntegrityException, GenericEntityException
    {
        List amendments = simpleRelationEntityCheck.preview();
        assertEquals(2, amendments.size());

        assertAmendments(amendments);
        Collection ids;

        // The preview method should not modify anything so we should have 3 issues in the database
        List issueGVs = genericDelegator.findAll("Issue");
        assertEquals(3, issueGVs.size());

        ids = new ArrayList();
        for (Iterator iterator = issueGVs.iterator(); iterator.hasNext();)
        {
            GenericValue issueGV = (GenericValue) iterator.next();
            ids.add(issueGV.getLong("id"));
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
        // This should correct the problem by removing the issues with no project
        List amendments = simpleRelationEntityCheck.correct();
        assertEquals(2, amendments.size());

        assertAmendments(amendments);

        // There should be one Issue remaining
        List allIssues = genericDelegator.findAll("Issue");
        assertEquals(1, allIssues.size());
        assertEquals(issue1, allIssues.get(0));

        // This should return no amendments as they have just been corrected.
        amendments = simpleRelationEntityCheck.preview();
        assertTrue(amendments.isEmpty());
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        CoreTransactionUtil.setUseTransactions(true);
        this.genericDelegator = null;
        this.ofBizDelegator = null;
        this.simpleRelationEntityCheck = null;
        this.issue1 = null;
    }
}
