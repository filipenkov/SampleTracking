package com.atlassian.jira.webtests.ztests.workflow;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.WebTable;
import org.xml.sax.SAXException;

/**
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.WORKFLOW })
public class TestDraftWorkflow extends JIRAWebTest
{
    public TestDraftWorkflow(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestDraftWorkflow.xml");
        HttpUnitOptions.setScriptingEnabled(true);
    }

    public void tearDown()
    {
        HttpUnitOptions.setScriptingEnabled(false);
        super.tearDown();
    }

    public void testPublishDraftWorkflowWithFunnyNames() throws Exception
    {
        restoreData("TestDraftWorkflowFunnyNames.xml");

        gotoWorkFlow();
        clickLink("createDraft_Funny&Name");
        assertTextPresent("View Workflow Steps &mdash; Funny&amp;Name (Draft)");
        setFormElement("stepName", "In Progress");
        submit("Add");

        clickLinkWithText("workflows.");
        assertTextNotInTable("workflows_table", "Published draft workflowDude");

        //check that the active workflow does not have the new step.
        clickLink("steps_live_Funny&Name");
        assertTextNotPresent("In Progress");

        clickLinkWithText("workflows.");

        //check that the draft has the step.
        clickLink("steps_draft_Funny&Name");
        assertTextPresent("In Progress");

        clickLink("workflows");

        //ensure cancel publish goes back to the workflow listing
        clickLink("publishDraft_Funny&Name");
        assertTextPresent("Publish Draft Workflow");
        assertTextPresent("You are about to publish the workflow <strong>Funny&amp;Name (Draft)</strong>.  This will overwrite the active workflow <strong>Funny&amp;Name</strong> and remove the draft! Click Publish if you want to continue.");
        clickLink("publish-workflow-cancel");
        assertTextInTable("workflows_table", "Draft");

        //now lets publish.
        clickLink("publishDraft_Funny&Name");
        checkCheckbox("enableBackup", "true");
        //save the draft as a copy.
        setFormElement("newWorkflowName", "overwritten workflowDude");
        submit("Publish");
        assertTextNotInColumn("workflows_table", 2, "Draft");
        assertTextInTable("workflows_table", "overwritten workflowDude");

        //check that the active workflow does now have the new step.
        clickLink("steps_live_Funny&Name");
        assertTextPresent("In Progress");

        //check the copy that was created doesn't have the new step.
        clickLink("workflows");
        clickLink("steps_live_overwritten workflowDude");
        assertTextNotInTable("steps_table", "In Progress");
    }

    public void testPublishDraftWorkflow() throws SAXException
    {
        gotoWorkFlow();

        //lets create draft workflow first.
        clickLink("createDraft_Workflow1");

        //now lets add a step to our draft.
        setFormElement("stepName", "In Progress");
        submit("Add");

        clickLinkWithText("workflows.");
        assertTextNotInTable("workflows_table", "Published draft workflowDude");

        //check that the active workflow does not have the new step.
        clickLink("steps_live_Workflow1");
        assertTextNotPresent("In Progress");

        clickLinkWithText("workflows.");

        //check that the draft has the step.
        clickLink("steps_draft_Workflow1");
        assertTextPresent("In Progress");

        clickLink("workflows");

        //ensure cancel publish goes back to the workflow listing
        clickLink("publishDraft_Workflow1");
        assertTextPresent("Publish Draft Workflow");
        assertTextPresent("You are about to publish the workflow <strong>Workflow1 (Draft)</strong>.  This will overwrite the active workflow <strong>Workflow1</strong> and remove the draft! Click Publish if you want to continue.");
        clickLink("publish-workflow-cancel");
        assertTextInTable("workflows_table", "Draft");

        //now lets publish.
        clickLink("publishDraft_Workflow1");
        checkCheckbox("enableBackup", "true");
        //save the draft as a copy.
        setFormElement("newWorkflowName", "overwritten workflowDude");
        submit("Publish");
        assertTextNotInColumn("workflows_table", 2, "Draft");
        assertTextInTable("workflows_table", "overwritten workflowDude");

        //check that the active workflow does now have the new step.
        clickLink("steps_live_Workflow1");
        assertTextPresent("In Progress");

        //check the copy that was created doesn't have the new step.
        clickLink("workflows");
        clickLink("steps_live_overwritten workflowDude");
        assertTextNotInTable("steps_table", "In Progress");
    }

    public void testPublishDraftWithoutBackup() throws SAXException
    {
        gotoWorkFlow();

        //lets create draft workflow first.
        clickLink("createDraft_Workflow1");

        clickLink("workflows");

        //check we have 6 rows in the table.
        WebTable table = getDialog().getResponse().getTableWithID("workflows_table");
        assertEquals(6, table.getRowCount());

        //publish the draft without creating a backup.
        clickLink("publishDraft_Workflow1");
        //set the enableBackup radio button.
        checkCheckbox("enableBackup", "false");
        submit("Publish");

        //there should now be one less row in the table.
        table = getDialog().getResponse().getTableWithID("workflows_table");
        assertEquals(5, table.getRowCount());
    }

    public void testHideDeleteLink()
    {
        gotoWorkFlow();

        //lets create a draft.
        clickLink("createDraft_Workflow1");

        //shouldn't be able to delete an existing step
        assertLinkNotPresent("delete_step_1");

        //also check the single step view
        navigation.clickLinkWithExactText("Open");
        assertLinkNotPresent("del_step");

        //lets add a new step. This should now display a delete link.
        clickLink("workflows");
        clickLink("steps_draft_Workflow1");
        setFormElement("stepName", "ClosedStep");
        selectOption("stepStatus", "Closed");
        submit("Add");
        assertTextInTable("steps_table", "ClosedStep");
        assertLinkPresent("delete_step_2");

        //also check the single step view
        clickLinkWithText("ClosedStep");
        assertLinkPresent("del_step");

        //lets delete the new step.
        clickLink("workflows");
        clickLink("steps_draft_Workflow1");
        clickLink("delete_step_2");
        assertTextPresent("Delete Workflow Step: ClosedStep");
        submit("Delete");

        assertTextNotInTable("steps_table", "ClosedStep");
    }

    public void testEditStatusDisabled()
    {
        gotoWorkFlow();
        //lets create a draft
        clickLink("createDraft_Workflow1");
        assertTextInTable("steps_table", "Open");
        assertTextNotInTable("steps_table", "EditedStep");

        //lets edit the existing step.  Shouldn't be able to modify the status.
        clickLink("edit_step_1");
        assertTrue(isFormElementDisabled("jiraform", "stepStatus_select"));
        setFormElement("stepName", "EditedStep");
        submit("Update");
        assertTextInTable("steps_table", "EditedStep");

        //lets create a new step and edit its status.
        setFormElement("stepName", "ClosedStep");
        selectOption("stepStatus", "Closed");
        submit("Add");
        assertTextNotInTable("steps_table", "Reopened");
        assertTextNotInTable("steps_table", "ClosedStepEdit");

        clickLink("edit_step_2");
        //this is a new step, so we should be able to edit the status.
        assertFalse(isFormElementDisabled("jiraform", "stepStatus_select"));
        setFormElement("stepName", "ClosedStepEdit");
        selectOption("stepStatus", "Reopened");
        submit("Update");

        assertTextInTable("steps_table", "Reopened");
        assertTextInTable("steps_table", "ClosedStepEdit");
    }

    public void testEditValidation()
    {
        gotoWorkFlow();
        clickLink("createDraft_Workflow1");

        //try to hack a URL where we're changing the stepStatus of an existing step.
        gotoPage(page.addXsrfToken("secure/admin/workflows/EditWorkflowStep.jspa?workflowMode=draft&workflowName=Workflow1&workflowStep=1&stepStatus=Closed&stepName=sweet"));

        assertTextPresent("Cannot change the status of an existing step on a draft workflow");
    }

    public void testDeleteDraftValidation()
    {
        gotoWorkFlow();
        clickLink("createDraft_Workflow1");

        //try to hack a URL where we're changing the stepStatus of an existing step.
        gotoPage("secure/admin/workflows/DeleteWorkflowStep!default.jspa?workflowMode=draft&workflowName=Workflow1&workflowStep=1");
        submit("Delete");

        assertTextPresent("Cannot delete an existing step on a draft workflow.");
    }

    //TODO: this is not testing a draft workflow.  We might want to move this.
    public void testDeleteValidation()
    {
        gotoWorkFlow();

        //try to hack a URL where we're changing the stepStatus of an existing step.
        gotoPage("secure/admin/workflows/DeleteWorkflowStep!default.jspa?workflowMode=live&workflowName=Workflow1&workflowStep=1");
        submit("Delete");

        assertTextPresent("Cannot delete step. This workflow is not editable.");
    }

    public void testWorkflowInfoBox()
    {
        gotoWorkFlow();
        clickLink("steps_live_Workflow1");

        //first check the correct message is shown about creating a draft workflow.
        assertTextPresent("You are viewing an active workflow.");
        assertTextSequence(new String[] { "Create", "a draft workflow" });
        assertTextNotPresent("Steps that exist on the active workflow, can't be deleted from the draft workflow.");

        //also check that the last edited message shows the correct user.
        assertTextPresent("This workflow was last edited by <strong>you</strong> at");

        //give users global admin permission, such that fred, can view the admin section.
        grantGlobalPermission(GLOBAL_ADMIN, "jira-users");

        logout();
        login(FRED_USERNAME, FRED_PASSWORD);
        gotoAdmin();
        clickLink("workflows");
        clickLink("steps_live_Workflow1");
        assertTextPresent("You are viewing an active workflow.");
        assertTextSequence(new String[] { "Create", "a draft workflow." });
        assertTextSequence(new String[] { "This workflow was last edited by", ADMIN_USERNAME });
        assertTextNotPresent("Steps that exist on the active workflow, can't be deleted from the draft workflow.");
        //check the admin link takes us to the user's profile.
        clickLink("workflow_edited_admin");
        assertTextSequence(new String[] { "User Profile", ADMIN_FULLNAME });

        //now lets create a draft and check the info message updated correctly.
        gotoAdmin();
        clickLink("workflows");
        clickLink("steps_live_Workflow1");
        clickLink("create_draft_workflow");
        assertTextSequence(new String[] { "View Workflow Steps", "Workflow1" });
        assertTextPresent("You are editing a draft workflow");
        assertTextSequence(new String[] { "View", "the original workflow or", "publish", "this draft." });
        assertTextPresent("This draft was last edited by <strong>you</strong> at");
        assertTextPresent("Steps that exist on the active workflow, can't be deleted from the draft workflow.");

        //go to the active workflow
        clickLink("view_live_workflow");
        assertTextSequence(new String[] { "View Workflow Steps", "Workflow1" });
        assertTextPresent("You are viewing an active workflow.");
        assertTextSequence(new String[] { "Edit", "the draft workflow." });
        assertTextSequence(new String[] { "This workflow was last edited by", ADMIN_USERNAME });
        assertTextNotPresent("Steps that exist on the active workflow, can't be deleted from the draft workflow.");
        navigation.clickLinkWithExactText("Open");
        assertTextNotPresent("This step exists on the active workflow, which means it can't be deleted from this draft workflow.");

        //go back to the draft.
        clickLink("view_draft_workflow");
        assertTextSequence(new String[] { "View Workflow Steps", "Workflow1" });
        assertTextPresent("You are editing a draft workflow");
        assertTextSequence(new String[] { "View", "the original workflow or", "publish", "this draft." });
        assertTextSequence(new String[] { "This draft was last edited by <strong>you</strong> at" });
        assertTextPresent("Steps that exist on the active workflow, can't be deleted from the draft workflow.");

        // Check the "Open" Workflow Step
        navigation.clickLinkWithExactText("Open");
        assertTextPresent("View Workflow Step &mdash; Open");
        assertTextSequence(new String[] { "You are editing a draft workflow.", "View", "the original workflow or", "publish", "this draft." });
        // On this screen we don't show the audit trail.
        assertTextNotPresent("last edited by");
        assertTextPresent("This step exists on the active workflow, which means it can't be deleted from this draft workflow.");

        // View a transition - "Create"
        clickLink("view_transition_1");
        assertTextSequence(new String[] { "You are editing a draft workflow.", "View", "the original workflow or", "publish", "this draft." });
        // On this screen we don't show the audit trail.
        assertTextNotPresent("last edited by");
        assertTextNotPresent("This step exists on the active workflow, which means it can't be deleted from this draft workflow.");

        //finally ensure the publish link goes to the right page.
        clickLinkWithText("publish");
        assertTextPresent("Publish Draft Workflow");
        assertTextSequence(new String[] { "You are about to publish the workflow", "Workflow1 (Draft)",
                                          "This will overwrite the active workflow", "Workflow1",
                                          "and remove the draft", "Click Publish if you want to continue" });
    }

    public void testWorkflowDraftName()
    {
        gotoWorkFlow();
        //lets create a draft
        clickLink("createDraft_Workflow1");

        //the workflow name should have (Draft appended).
        assertTextSequence(new String[] { "This shows all of the steps for", "Workflow1 (Draft)", "Steps that exist on the active workflow, can't be deleted from the draft workflow." });

        //lets add a step
        setFormElement("stepName", "Dude");
        submit("Add");

        //lets add a transition
        clickLinkWithText("Add Transition");
        setFormElement("transitionName", "testTransition");
        submit("Add");

        //look at the single step view workflow name should have (Draft appended).
        navigation.clickLinkWithExactText("Open");
        assertTextSequence(new String[] { "View", "workflow steps", "of", "Workflow1 (Draft)" });

        //lets look at the testTransition workflow name should have (Draft appended).
        clickLinkWithText("testTransition");
        assertTextPresent("Transition: testTransition");
        assertTextSequence(new String[] { "View", "workflow steps", "of", "Workflow1 (Draft)" });

        //finally lets look at the step's properties
        navigation.clickLinkWithExactText("Open");
        clickLink("view_properties_1");
        assertTextSequence(new String[] { "View", "workflow steps", "of", "Workflow1 (Draft)" });

        //NOW lets check the original workflow.  There shouldn't be (Draft) anywhere.
        clickLink("workflows");
        clickLink("steps_live_Workflow1");
        assertTextNotPresent("(Draft)");
        navigation.clickLinkWithExactText("Open");
        assertTextNotPresent("(Draft)");
        clickLink("view_properties_1");
        assertTextNotPresent("(Draft)");
        clickLinkWithText("workflow steps");
        navigation.clickLinkWithExactText("Open");
        clickLink("view_transition_1");
        assertTextNotPresent("(Draft)");
    }

    protected void _testCreateAndDeleteDraftWorkflow(int operationColumn)
            throws SAXException
    {
        gotoWorkFlow();
        WebTable table = getDialog().getResponse().getTableWithID("workflows_table");
        assertEquals(5, table.getRowCount());
        // Assert that 'Workflow1' is Active and we have an "edit" link in order to create a draft.
        assertTableCellHasText("workflows_table", 2, 0, "Workflow1");
        assertTableCellHasText("workflows_table", 2, 2, "Active");

        assertTableCellHasText("workflows_table", 2, operationColumn, "Create Draft");
        assertTextNotInColumn("workflows_table", 2, "Draft");

        //lets create a draft
        clickLink("createDraft_Workflow1");
        clickLinkWithText("workflows.");

        //check that the workflows table now has a draft workflow.
        // Assert that 'Workflow1' is Active and we do not have a "edit" link
        assertTableCellHasText("workflows_table", 2, 0, "Workflow1");
        assertTableCellHasText("workflows_table", 2, 2, "Active");
        assertTableCellHasNotText("workflows_table", 2, operationColumn, "Create Draft");
        table = getDialog().getResponse().getTableWithID("workflows_table");
        assertEquals(6, table.getRowCount());

        assertTableCellHasText("workflows_table", 3, 0, "Workflow1");
        assertTableCellHasText("workflows_table", 3, 2, "Draft");
        assertTableCellHasText("workflows_table", 3, operationColumn, "Delete");
        assertTableCellHasText("workflows_table", 3, operationColumn, "Publish");

        //now lets try to delete the draft workflow!
        clickLink("del_Workflow1");
        assertTextPresent("Delete Draft Workflow");
        assertTextPresent("Confirm that you want to delete the draft workflow <strong>Workflow1</strong>.");
        submit("Delete");

        assertTableCellHasText("workflows_table", 2, 0, "Workflow1");
        assertTableCellHasText("workflows_table", 2, 2, "Active");
        assertTableCellHasText("workflows_table", 2, operationColumn, "Create Draft");
        assertTextNotInColumn("workflows_table", 2, "Draft");
        table = getDialog().getResponse().getTableWithID("workflows_table");
        assertEquals(5, table.getRowCount());
    }

    public void testDraftWorkflowWithInactiveParent()
    {
        //only way to get into this state is via some db weirdness
        restoreData("TestDraftWorkflowInvalidState.xml");

        gotoWorkFlow();
        assertions.getJiraFormAssertions().assertFormErrMsg("The parent workflow of draft 'Workflow1' is no longer"
                + " active. Please delete the draft. You may wish to copy the draft before deleting it.");
        assertLinkNotPresentWithText("Publish");

        gotoPage("secure/admin/workflows/PublishDraftWorkflow!default.jspa?workflowMode=live&workflowName=Workflow1");
        assertTextPresent("Publish Draft Workflow");
        submit("Publish");
        assertions.getJiraFormAssertions().assertFormErrMsg("The parent workflow of draft 'Workflow1' is no longer"
                + " active. Please delete the draft. You may wish to copy the draft before deleting it.");
    }

    public void testEditParentNameWithInactiveDraft() throws SAXException
    {
        //only way to get into this state is via some db weirdness
        restoreData("TestDraftWorkflowInvalidState.xml");
        gotoWorkFlow();

        assertTableCellHasText("workflows_table", 2, 0, "Workflow1");
        assertTableCellHasText("workflows_table", 3, 0, "Workflow1");

        //lets edit the parent.  This should rename the draft too.
        clickLink("edit_live_Workflow1");
        setFormElement("newWorkflowName", "InvalidWorkflowNow");
        setFormElement("description", "My weird description.");
        submit("Update");

        assertTableCellHasText("workflows_table", 2, 0, "InvalidWorkflowNow");
        assertTableCellHasText("workflows_table", 2, 1, "My weird description.");
        assertTableCellHasText("workflows_table", 3, 0, "InvalidWorkflowNow");
        assertTableCellHasNotText("workflows_table", 3, 1, "My weird description.");

        //now lets change the draft workflow desc
        clickLink("edit_draft_InvalidWorkflowNow");
        setFormElement("description", "dude this is a draft");
        submit("Update");

        assertTableCellHasText("workflows_table", 2, 0, "InvalidWorkflowNow");
        assertTableCellHasText("workflows_table", 2, 1, "My weird description.");
        assertTableCellHasText("workflows_table", 3, 0, "InvalidWorkflowNow");
        assertTableCellHasText("workflows_table", 3, 1, "dude this is a draft");
    }

    public void testDeleteLinkNotShownForInitialTransition()
    {
        gotoWorkFlow();

        //lets create a copy of the default workflow
        administration.workflows().goTo().copyWorkflow("jira", "Copy of jira");

        clickLink("steps_live_Copy of jira");
        navigation.clickLinkWithExactText("Open");
        clickLinkWithText("Start Progress");
        assertTextPresent("Transition: Start Progress");
        //check a normal transition has the delete link.
        assertLinkPresent("delete_transition");

        //an initial transition should NOT have the delete link.
        navigation.clickLinkWithExactText("Open");
        clickLink("view_transition_1");
        assertTextPresent("Transition: Create Issue");
        assertLinkNotPresent("delete_transition");
    }

    public void testAddTransitionForStepWithNoTransitions()
    {
        gotoWorkFlow();

        //lets create a draft with such a step.
        clickLink("createDraft_Workflow1");
        //check the link isn't present.
        assertLinkWithTextNotPresent("Add Transition");
        navigation.clickLinkWithExactText("Open");
        assertTextPresent("The step <strong>Open</strong> has no outgoing transitions in the Active workflow, so you cannot add any outgoing transitions in the Draft workflow.");
        assertLinkWithTextNotPresent("Add Transition");

        //check that if the URL is hacked, the correct error is shown.
        gotoPage("/secure/admin/workflows/AddWorkflowTransition!default.jspa?workflowMode=draft&workflowName=Workflow1&workflowStep=1");
        assertTextPresent("Add Workflow Transition");
        assertions.getJiraFormAssertions().assertFormErrMsg("You are editing a draft workflow. The step 'Open' has no "
                + "outgoing transitions in the Active workflow, so you cannot add any outgoing transitions in the Draft workflow.");
        submit("Add");
        assertTextPresent("Add Workflow Transition");
        assertions.getJiraFormAssertions().assertFormErrMsg("You are editing a draft workflow. The step 'Open' has no "
                + "outgoing transitions in the Active workflow, so you cannot add any outgoing transitions in the Draft workflow.");

        //Finally lets try adding some transitions for a new step.
        gotoWorkFlow();
        //create the step
        clickLink("steps_draft_Workflow1");
        setFormElement("stepName", "Dude");
        submit("Add");

        //add the transition
        clickLinkWithText("Add Transition");
        setFormElement("transitionName", "New Transition");
        submit("Add");

        assertTextSequence(new String[] { "View Workflow Steps", "Workflow1 (Draft)" });
        assertTextPresent("New Transition");
    }

    public void testOverWriteWorkflowWithInvalidTransition()
    {
        restoreData("TestOverwriteInvalidWorkflow.xml");

        gotoWorkFlow();
        clickLink("publishDraft_Workflow1");
        checkCheckbox("enableBackup", "false");
        submit("Publish");
        assertions.getJiraFormAssertions().assertFormErrMsg("You are editing a draft workflow. The step 'Open' has no"
                + " outgoing transitions in the Active workflow, so you cannot add any outgoing transitions in the"
                + " Draft workflow");
    }

}
