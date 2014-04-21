package com.atlassian.jira.webtests.ztests.workflow;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

@WebTest ({ Category.FUNC_TEST, Category.SCHEMES, Category.WORKFLOW})
public class TestWorkFlowSchemes extends JIRAWebTest
{
    private static final String TAB_NAME = "Tab for Testing";
    private static final String CUSTOM_FIELD_NAME = "Approval Rating";
    private static final String CUSTOM_FIELD_NAME_TWO = "Animal";
    String customFieldId;
    String customFieldId2;
    private static final String UNSHOWN_STATUS = "Unshown Status";

    public TestWorkFlowSchemes(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreBlankInstance();
    }

    public void testWorkFlowSchemes()
    {
        restoreBlankInstance();
        if (workflowSchemeExists(WORKFLOW_SCHEME))
        {
            associateWorkFlowSchemeToProject(PROJECT_HOMOSAP, "Default");
            waitForSuccessfulWorkflowSchemeMigration(PROJECT_HOMOSAP, "Default");
            associateWorkFlowSchemeToProject(PROJECT_MONKEY, "Default");
            waitForSuccessfulWorkflowSchemeMigration(PROJECT_MONKEY, "Default");
            deleteWorkFlowScheme(WORKFLOW_SCHEME);
        }

        if (workflowExists(WORKFLOW_ADDED))
        {
            deleteWorkFlow(WORKFLOW_ADDED);
        }
        if (workflowExists(WORKFLOW_COPIED))
        {
            deleteWorkFlow(WORKFLOW_COPIED);
        }
        if (linkedStatusExists(STATUS_NAME))
        {
            deleteLinkedStatus("10000");
        }

        resetFields();
        removeAllFieldScreens();
        removeAllCustomFields();
        customFieldId = addCustomField(CUSTOM_FIELD_TYPE_TEXTFIELD, CUSTOM_FIELD_NAME);
        customFieldId2 = addCustomField(CUSTOM_FIELD_TYPE_TEXTFIELD, CUSTOM_FIELD_NAME_TWO);
        String issueKey = addIssue(PROJECT_HOMOSAP, PROJECT_HOMOSAP_KEY, "Bug", "test 1", "Minor", null, null, null, ADMIN_FULLNAME, "test environment 1", "test description for workflow schemes", null, null, null);
        addScreen(TEST_FIELD_SCREEN, "");
        addFieldToFieldScreen(TEST_FIELD_SCREEN, CUSTOM_FIELD_NAME);

        addTabToScreen(TEST_FIELD_SCREEN, TAB_NAME);
        addFieldToFieldScreenTab(TEST_FIELD_SCREEN, TAB_NAME, CUSTOM_FIELD_NAME_TWO, "");

        workflowAddScheme();
        workflowAddDuplicateScheme();
        workflowAddInvalidScheme();

        workflowAddWorkflow();
        workflowCopyWorkflow();
        workflowAddDuplicateWorkflow();
        workflowAddInvalidWorkflow();
        workflowAddLinkedStatus();
        workflowAddDuplicateLinkedStatus();
        workflowAddInvalidLinkedStatus();
        workflowAddStep();
        workflowAddTransition();
        workflowAddDuplicateTransition();
        workflowAddInvalidTransition();

        gotoWorkFlowScheme();
        clickLink("workflowscheme_10000");
        assertTextPresent("On this page you can edit the workflows for the \"New Workflow Scheme\" workflow scheme.");
        assertTextNotPresent("The \"New Workflow Scheme\" workflow scheme is active therefore you cannot edit its workflows.");

        workflowAssignWorkflowSchemeToIssueType();
        workflowAssociateWorkflowSchemeToProject();

        gotoWorkFlowScheme();
        clickLink("workflowscheme_10000");
        assertTextNotPresent("On this page you can edit the workflows for the \"New Workflow Scheme\" workflow scheme.");
        assertTextPresent("The \"New Workflow Scheme\" workflow scheme is active therefore you cannot edit its workflows.");

        workflowPerformAction(issueKey);

        _testNoActiveStatus();

        associateWorkFlowSchemeToProject(PROJECT_HOMOSAP, "Default");
        waitForSuccessfulWorkflowSchemeMigration(PROJECT_HOMOSAP, "Default");
        workflowDeleteScheme();

        workflowDeleteTransition();
        workflowDeleteStep();
        workflowDeleteLinkedStatus();

        workflowDeleteWorkflow();

        deleteIssue(issueKey);
        removeAllFieldScreens();
        removeAllCustomFields();
    }

    private void _testNoActiveStatus()
    {
        logSection("Workflow Schemes: Non-active status should not be displayed if it's not active");

        addLinkedStatus(UNSHOWN_STATUS, "This status should not be shown in the issue navigator");
        assertTextPresent(UNSHOWN_STATUS);

        displayAllIssues();

        assertTextPresent(STATUS_NAME);
        assertTextNotPresent(UNSHOWN_STATUS);

        deleteLinkedStatus("10001");
        assertTextNotPresent(UNSHOWN_STATUS);
    }

    public void workflowAddScheme()
    {
        log("Workflow Schemes: Create a workflow scheme");
        addWorkFlowScheme(WORKFLOW_SCHEME, "New workflow scheme for testing");
        assertTextPresent("Workflow Schemes");
        assertTextPresent(WORKFLOW_SCHEME);
    }

    public void workflowDeleteScheme()
    {
        log("Workflow Schemes: Delete a workflow scheme");
        deleteWorkFlowScheme(WORKFLOW_SCHEME);
        assertTextPresent("Workflow Schemes");
        assertTextNotPresent(WORKFLOW_SCHEME);
    }

    public void workflowAddDuplicateScheme()
    {
        log("Workflow Schemes: Add a workflow scheme with a duplicate name");
        addWorkFlowScheme(WORKFLOW_SCHEME, "");
        assertTextPresent("Add Workflow Scheme");
        assertTextPresent("A Scheme with this name already exists.");
    }

    public void workflowAddInvalidScheme()
    {
        log("Workflow Schemes: Add a workflow scheme with a invalid name");
        addWorkFlowScheme("", "");
        assertTextPresent("Add Workflow Scheme");
        assertTextPresent("Please specify a name for this Scheme.");
    }

    public void workflowAddWorkflow()
    {
        log("Workflow Schemes: Create a workflow");
        addWorkFlow(WORKFLOW_ADDED, "New workflow for testing");
        assertTextPresent(WORKFLOW_ADDED);
    }


    public void workflowDeleteWorkflow()
    {
        log("Workflow Schemes: Delete a workflow");
        deleteWorkFlow(WORKFLOW_ADDED);
        assertTextNotPresent(WORKFLOW_ADDED);
        deleteWorkFlow(WORKFLOW_COPIED);
        assertTextNotPresent(WORKFLOW_COPIED);
    }


    public void workflowCopyWorkflow()
    {
        log("Workflow Schemes: Copy a workflow");
        copyWorkFlow("jira", WORKFLOW_COPIED, "Workflow copied from JIRA default");
        assertTextPresent(WORKFLOW_COPIED);
    }

    public void workflowAddDuplicateWorkflow()
    {
        log("Workflow Schemes: Add a workflow with a duplicate name");
        addWorkFlow(WORKFLOW_COPIED, "");
        assertTextPresent("A workflow with this name already exists.");
    }

    public void workflowAddInvalidWorkflow()
    {
        log("Workflow Schemes: Add a workflow with an invalid name");
        addWorkFlow("", "");
        assertTextPresent("You must specify a workflow name.");
    }

    public void workflowAddLinkedStatus()
    {
        log("Workflow Schemes: Add a linked status");
        addLinkedStatus(STATUS_NAME, "The resolution of this issue has been approved");
        assertTextPresent(STATUS_NAME);
    }

    public void workflowDeleteLinkedStatus()
    {
        log("Workflow Schemes: Delete a linked status");
        deleteLinkedStatus("10000");
        assertTextNotPresent(STATUS_NAME);
    }

    public void workflowAddDuplicateLinkedStatus()
    {
        log("Workflow Scheme: Add a linked status with a duplicate name");
        addLinkedStatus(STATUS_NAME, "");
        assertTextPresent("A status with that name already exists.");
    }

    public void workflowAddInvalidLinkedStatus()
    {
        log("Workflow Scheme: Add a linked status with a invalid name");
        addLinkedStatus("", "");
        assertTextPresent("You must specify a name for the status to be added.");
    }

    public void workflowAddStep()
    {
        log("Workflow Scheme: Add a step");
        addStep(WORKFLOW_COPIED, STEP_NAME, STATUS_NAME);
        assertLinkPresentWithText(STEP_NAME);
        assertFormElementNotPresent("stepName");
        assertFormElementNotPresent("stepStatus");
    }

    public void workflowDeleteStep()
    {
        log("Workflow Scheme: delete a step");
        deleteStep(WORKFLOW_COPIED, STEP_NAME);
        assertLinkNotPresent(STEP_NAME);
        assertFormElementPresent("stepName");
        assertFormElementPresent("stepStatus");
    }

    public void workflowAddTransition()
    {
        log("Workflow Scheme: Add a transition to a step");
        addTransition(WORKFLOW_COPIED, "Resolved", TRANSIION_NAME_APPROVE, "", STEP_NAME, TEST_FIELD_SCREEN);
        assertLinkPresentWithText(TRANSIION_NAME_APPROVE);
        addTransition(WORKFLOW_COPIED, STEP_NAME, TRANSIION_NAME_REOPEN, "", "Open", ASSIGN_FIELD_SCREEN);
        addTransition(WORKFLOW_COPIED, STEP_NAME, TRANSIION_NAME_CLOSE, "", "Closed", null);
    }

    public void workflowDeleteTransition()
    {
        log("Workflow Scheme: Delete a transition from a Step");
        deleteTransition(WORKFLOW_COPIED, "Resolved", TRANSIION_NAME_APPROVE);
        assertLinkNotPresentWithText(TRANSIION_NAME_APPROVE);
        deleteTransition(WORKFLOW_COPIED, STEP_NAME, TRANSIION_NAME_REOPEN);
        deleteTransition(WORKFLOW_COPIED, STEP_NAME, TRANSIION_NAME_CLOSE);
    }

    public void workflowAddDuplicateTransition()
    {
        log("Workflow Scheme: Add a transition with a duplicate nane");
        addTransition(WORKFLOW_COPIED, STEP_NAME, TRANSIION_NAME_REOPEN, "", "Open", null);
        assertTextPresent("Add Workflow Transition");
        assertTextPresent("Transition with this name already exists for Approved step.");
    }

    public void workflowAddInvalidTransition()
    {
        log("Workflow Scheme: Add a transition with an invalid name");
        addTransition(WORKFLOW_COPIED, STEP_NAME, "", "", "Open", null);
        assertTextPresent("Add Workflow Transition");
        assertTextPresent("You must enter a valid name.");
    }

    public void workflowAssignWorkflowSchemeToIssueType()
    {
        log("Workflow Scheme: Assign a workflow scheme to an issue type");
        assignWorkflowScheme(WORKFLOW_SCHEME, "Bug", WORKFLOW_COPIED);
        assertTextPresent("Edit Workflows for New Workflow Scheme");
        assertTextPresent(WORKFLOW_COPIED);
        assertTextPresent("Bug");
    }

    public void workflowActivateWorkflow()
    {
        activateWorkflow(WORKFLOW_COPIED);
        waitForSuccessfulWorkflowActivation(WORKFLOW_COPIED);
        assertLinkPresent("activate_jira");
    }

    public void workflowUnassignWorkflowScheme()
    {
        log("Workflow Scheme: Unassign a workflow scheme to an issue type");
        unassignWorkflowScheme(WORKFLOW_SCHEME, "Bug", WORKFLOW_COPIED);
        assertTextPresent("Edit Workflows for New Workflow Scheme");
        assertTextNotPresent(WORKFLOW_COPIED);
    }

    public void workflowAssociateWorkflowSchemeToProject()
    {
        log("Workflow Scheme; Associate a workflow scheme with a project");
        associateWorkFlowSchemeToProject(PROJECT_HOMOSAP, WORKFLOW_SCHEME);
        waitForSuccessfulWorkflowSchemeMigration(PROJECT_HOMOSAP, WORKFLOW_SCHEME);
        assertTextPresent(WORKFLOW_SCHEME);
    }

    /* Perform workflow actions using the customised workflow/workflow scheme */
    public void workflowPerformAction(String issueKey)
    {
        log("Perform workflow actions using the customised workflow/workflow scheme");
        gotoIssue(issueKey);

        clickLinkWithText("Resolve Issue");
        setWorkingForm("issue-workflow-transition");
        submit("Transition");
        assertTextPresent("Resolved");

        clickLinkWithText(TRANSIION_NAME_APPROVE);
        setWorkingForm("issue-workflow-transition");
        setFormElement(CUSTOM_FIELD_PREFIX + customFieldId, "High");
        clickLinkWithText(TAB_NAME);
        setFormElement(CUSTOM_FIELD_PREFIX + customFieldId2, "Whale");
        submit("Transition");
        assertTextPresent("Approved");

        clickLinkWithText(TRANSIION_NAME_REOPEN);
        setWorkingForm("issue-workflow-transition");
        submit("Transition");
        assertTextPresent("Open");
    }
}
