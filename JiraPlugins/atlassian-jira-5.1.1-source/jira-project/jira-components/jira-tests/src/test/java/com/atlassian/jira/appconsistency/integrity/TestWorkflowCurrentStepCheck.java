/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.appconsistency.integrity;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.ofbiz.util.CoreTransactionUtil;
import com.atlassian.jira.appconsistency.integrity.check.WorkflowCurrentStepCheck;
import com.atlassian.jira.appconsistency.integrity.exception.IntegrityException;
import com.atlassian.jira.ofbiz.DefaultOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.opensymphony.workflow.loader.MockStepDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;
import com.opensymphony.workflow.spi.WorkflowStore;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.MockConstantsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestWorkflowCurrentStepCheck extends LegacyJiraMockTestCase
{
    private GenericDelegator genericDelegator = CoreFactory.getGenericDelegator();
    private OfBizDelegator ofBizDelegator = new DefaultOfBizDelegator(genericDelegator);
    private WorkflowCurrentStepCheck wfCheck;
    private MockControl ctrlWFManager;
    private WorkflowManager mockWFManager;
    private MockControl ctrlWF;
    private JiraWorkflow mockWF;
    private MockControl ctrlWFStore;
    private WorkflowStore mockWFStore;
    private MockControl ctrlStepDesc;
    private StepDescriptor mockStepDesc;
    private MockConstantsManager constManager = new MockConstantsManager();
    private GenericValue issue1;
    private GenericValue issue2;
    private GenericValue issue3;
    private GenericValue status;

    public TestWorkflowCurrentStepCheck(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        CoreTransactionUtil.setUseTransactions(false);
        setUpMocks();

        status = UtilsForTests.getTestEntity("Status", EasyMap.build("id", new Long(1)));
        constManager.addStatus(status);

        issue1 = createValue("Issue", EasyMap.build("workflowId", new Long(1001), "key", "ABC-1", "status", "1", "project", new Long(1), "type", "1"));
        issue2 = createValue("Issue", EasyMap.build("workflowId", new Long(1002), "key", "ABC-2", "status", "1", "project", new Long(1), "type", "1"));
        issue3 = createValue("Issue", EasyMap.build("workflowId", new Long(1003), "key", "ABC-3", "status", "1", "project", new Long(1), "type", "1"));

        UtilsForTests.getTestEntity("OSCurrentStep", EasyMap.build("id", new Long(1001), "entryId", new Long(1001), "stepId", new Integer(1)));

        ctrlWFManager.expectAndReturn(mockWFManager.getWorkflow(issue1), mockWF);
        ctrlWFManager.expectAndReturn(mockWFManager.getWorkflow(issue2), mockWF);
        ctrlWFManager.expectAndReturn(mockWFManager.getWorkflow(issue3), mockWF);
        ctrlWF.expectAndReturn(mockWF.getLinkedStep(status), mockStepDesc, 3);

        ctrlStepDesc.expectAndReturn(mockStepDesc.getId(), 1, 3);

        wfCheck = new WorkflowCurrentStepCheck(ofBizDelegator, 1, constManager, mockWFManager);
    }

    private GenericValue createValue(String entity, Map params)
    {
        GenericValue v = UtilsForTests.getTestEntity(entity, params);
        try
        {
            // get this guy from the database because if you don't the tests mysteriously fail for issue3
            // not for issue1 or issue2 though, just issue3...
            return CoreFactory.getGenericDelegator().findByPrimaryKey(v.getPrimaryKey());
        }
        catch (GenericEntityException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void setUpMocks()
    {
        ctrlWFManager = MockControl.createControl(WorkflowManager.class);
        mockWFManager = (WorkflowManager) ctrlWFManager.getMock();
        ctrlWF = MockControl.createControl(JiraWorkflow.class);
        mockWF = (JiraWorkflow) ctrlWF.getMock();
        ctrlStepDesc = MockClassControl.createControl(MockStepDescriptor.class);
        mockStepDesc = (StepDescriptor) ctrlStepDesc.getMock();
        ctrlWFStore = MockControl.createControl(WorkflowStore.class);
        mockWFStore = (WorkflowStore) ctrlWFStore.getMock();
    }

    private void replayMocks()
    {
        ctrlWF.replay();
        ctrlWFManager.replay();
        ctrlStepDesc.replay();
        ctrlWFStore.replay();
    }

    private void verifyMocks()
    {
        ctrlWF.verify();
        ctrlWFManager.verify();
        ctrlStepDesc.verify();
        ctrlWFStore.verify();
    }

    private void resetMocks()
    {
        ctrlWF.reset();
        ctrlWFManager.reset();
        ctrlStepDesc.reset();
        ctrlWFStore.reset();
    }

    private void noProblems() throws Exception
    {
        resetMocks();
        ctrlWFManager.expectAndReturn(mockWFManager.getWorkflow(issue1), mockWF);
        ctrlWFManager.expectAndReturn(mockWFManager.getWorkflow(issue2), mockWF);
        ctrlWFManager.expectAndReturn(mockWFManager.getWorkflow(issue3), mockWF);
        ctrlWF.expectAndReturn(mockWF.getLinkedStep(status), mockStepDesc, 3);

        ctrlStepDesc.expectAndReturn(mockStepDesc.getId(), 1, 3);

        replayMocks();

        List amendments = wfCheck.preview();
        assertEquals(0, amendments.size());

        verifyMocks();
    }

    private void setUpCreate() throws Exception
    {
        List nonEmptyList = new ArrayList();
        nonEmptyList.add("nonEmptyValue");
        ctrlStepDesc.expectAndReturn(mockStepDesc.getActions(), nonEmptyList, 2);
    }

    public void testCreatePreview() throws Exception
    {
        setUpCreate();

        replayMocks();

        List amendments = wfCheck.preview();
        assertEquals(2, amendments.size());

        verifyMocks();
    }


    public void testCreateCorrect() throws Exception
    {
        setUpCreate();
        ctrlWFManager.expectAndReturn(mockWFManager.getStore(), mockWFStore, 2);
        ctrlWFStore.expectAndReturn(mockWFStore.createCurrentStep(1002l, 1, null, null, null, "1", null), null);
        ctrlWFStore.expectAndReturn(mockWFStore.createCurrentStep(1003l, 1, null, null, null, "1", null), null);

        replayMocks();

        // This should correct the problem
        List amendments = wfCheck.correct();
        assertEquals(2, amendments.size());

        verifyMocks();

        UtilsForTests.getTestEntity("OSCurrentStep", EasyMap.build("id", new Long(1002), "entryId", new Long(1002), "stepId", new Integer(1)));
        UtilsForTests.getTestEntity("OSCurrentStep", EasyMap.build("id", new Long(1003), "entryId", new Long(1003), "stepId", new Integer(1)));

        //There should be 3 current steps
        List allIssues = genericDelegator.findAll("OSCurrentStep");
        assertEquals(3, allIssues.size());

        resetMocks();

        // This should return no amendments as they have just been corrected.
        noProblems();
    }

    private void setUpValidate()
    {
        UtilsForTests.getTestEntity("OSCurrentStep", EasyMap.build("id", new Long(1002), "entryId", new Long(1002), "stepId", new Integer(2)));
        UtilsForTests.getTestEntity("OSCurrentStep", EasyMap.build("id", new Long(1003), "entryId", new Long(1003)));
    }

    public void testValidatePreview() throws Exception
    {
        setUpValidate();

        replayMocks();

        List amendments = wfCheck.preview();
        assertEquals(2, amendments.size());

        verifyMocks();
    }

    public void testValidateCorrect() throws Exception
    {
        setUpValidate();

        replayMocks();
        // This should correct the problem
        List amendments = wfCheck.correct();
        assertEquals(2, amendments.size());

        verifyMocks();

        noProblems();
    }

    private void setUpDelete()
    {
        UtilsForTests.getTestEntity("OSCurrentStep", EasyMap.build("id", new Long(1002), "entryId", new Long(1002), "stepId", new Integer(2)));
        UtilsForTests.getTestEntity("OSCurrentStep", EasyMap.build("id", new Long(1003), "entryId", new Long(1003), "stepId", new Integer(1)));
        UtilsForTests.getTestEntity("OSCurrentStep", EasyMap.build("id", new Long(1004), "entryId", new Long(1002), "stepId", new Integer(2)));
        UtilsForTests.getTestEntity("OSCurrentStep", EasyMap.build("id", new Long(1005), "entryId", new Long(1003), "stepId", new Integer(1)));
    }

    public void testDeletePreview() throws IntegrityException
    {
        setUpDelete();

        replayMocks();

        List amendments = wfCheck.preview();
        assertEquals(2, amendments.size());

        verifyMocks();
    }

    public void testDeleteCorrect() throws Exception
    {
        setUpDelete();

        replayMocks();
        // This should correct the problem
        List amendments = wfCheck.correct();
        assertEquals(3, amendments.size());  //only 2 bad issues but should be 3 ammendments as one has a bad step id

        verifyMocks();

        noProblems();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
        CoreTransactionUtil.setUseTransactions(true);
        this.genericDelegator = null;
        this.constManager = null;
        this.ofBizDelegator = null;
        this.ctrlStepDesc = null;
        this.ctrlWF = null;
        this.ctrlWFManager = null;
        this.ctrlWFStore = null;
        this.wfCheck = null;
        this.mockStepDesc = null;
        this.mockWF = null;
        this.mockWFManager = null;
        this.mockWFStore = null;
    }
}
