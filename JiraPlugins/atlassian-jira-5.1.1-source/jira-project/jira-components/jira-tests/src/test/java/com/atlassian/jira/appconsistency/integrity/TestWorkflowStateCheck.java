/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.appconsistency.integrity;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.util.CoreTransactionUtil;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.appconsistency.integrity.check.WorkflowStateCheck;
import com.atlassian.jira.appconsistency.integrity.exception.IntegrityException;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.DefaultOfBizDelegator;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.List;

public class TestWorkflowStateCheck extends LegacyJiraMockTestCase
{
    private GenericDelegator genericDelegator = CoreFactory.getGenericDelegator();
    private OfBizDelegator ofBizDelegator = new DefaultOfBizDelegator(genericDelegator);
    private WorkflowStateCheck wfCheck;

    public TestWorkflowStateCheck(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        CoreTransactionUtil.setUseTransactions(false);
        UtilsForTests.getTestEntity("Project", EasyMap.build("key", "ABC"));

        // Create three issue or which two do no have a project.
        UtilsForTests.getTestEntity("Issue", EasyMap.build("workflowId", new Long(1001), "key", "ABC-1"));
        UtilsForTests.getTestEntity("Issue", EasyMap.build("workflowId", new Long(1002), "key", "ABC-2"));
        UtilsForTests.getTestEntity("Issue", EasyMap.build("workflowId", new Long(1003), "key", "ABC-3"));

        UtilsForTests.getTestEntity("OSWorkflowEntry", EasyMap.build("id", new Long(1001), "state", new Integer(1)));
        UtilsForTests.getTestEntity("OSWorkflowEntry", EasyMap.build("id", new Long(1002), "state", new Integer(0)));
        UtilsForTests.getTestEntity("OSWorkflowEntry", EasyMap.build("id", new Long(1003)));
        wfCheck = new WorkflowStateCheck(ofBizDelegator, 1);

    }

    public void testPreview() throws IntegrityException
    {
        List amendments = wfCheck.preview();
        assertEquals(2, amendments.size());
    }

    public void testCorrect() throws IntegrityException, GenericEntityException
    {
        // This should correct the problem by removing the issues with no project
        List amendments = wfCheck.correct();
        assertEquals(2, amendments.size());

        // This should return no amendments as they have just been corrected.
        amendments = wfCheck.preview();
        assertTrue(amendments.isEmpty());
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        CoreTransactionUtil.setUseTransactions(true);
        this.wfCheck = null;
        this.genericDelegator = null;
        this.ofBizDelegator = null;

    }
}
