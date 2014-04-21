package com.atlassian.jira.webtests;

/**
 * Common test cases for professional and enterprise JIRA testing editing workflow names
 */
public abstract class AbstractTestWorkflowNameEditing extends JIRAWebTest
{
    public AbstractTestWorkflowNameEditing(String name)
    {
        super(name);
    }

    public void testDefaultSystemWorkflowIsNotEditable()
    {
        //goto the edit page for jira workflow directly
        gotoPage(page.addXsrfToken("/secure/admin/workflows/EditWorkflow.jspa?workflowMode=live&workflowName=jira"));
        //assert that the name or description cannot be edited
        assertWorkflowIsNotEditable();
    }

    public void testActiveWorkflowsAreNotEditable()
    {
        //goto the edit page for the active workflow
        gotoPage(page.addXsrfToken("/secure/admin/workflows/EditWorkflow.jspa?workflowMode=live&workflowName=Active+workflow"));
        //assert that the name or description cannot be edited
        assertWorkflowIsNotEditable();
    }

    public void testEditWorkflowNameValidation()
    {
        gotoWorkFlow();
        clickLink("edit_live_editable workflow");
        assertFormElementWithNameHasValue("edit-workflow","newWorkflowName", "editable workflow");

        //check name is not null/empty
        setFormElement("newWorkflowName", "");
        submit("Update");
        assertTextPresent("You must specify a workflow name.");

        //check for duplicate name
        setFormElement("newWorkflowName", "Active workflow");
        submit("Update");
        assertTextPresent("A workflow with this name already exists.");

        //check for non-ascii characters
        setFormElement("newWorkflowName", "non-ascii char: \u1234");
        submit("Update");
        assertTextPresent("Please use only ASCII characters for the workflow name.");
    }



    //--------------------------------------------------------------------------------------------------- Helper Methods
    private void assertWorkflowIsNotEditable()
    {
        assertTextPresent("Workflow cannot be edited as it is not editable.");
        submit("Update");
        assertTextPresent("Edit Workflow");
        assertTextPresent("Workflow cannot be edited as it is not editable.");
        setFormElement("newWorkflowName", "name change");
        setFormElement("description", "desc change");
        submit("Update");
        assertTextPresent("Edit Workflow");
        assertTextPresent("Workflow cannot be edited as it is not editable.");
    }

    public void testEditDraftWorkflow()
     {
         gotoWorkFlow();

         //first lets create a draft.
         clickLink("createDraft_Active workflow");
         clickLinkWithText("workflows.");

         //lets edit it.
         clickLink("edit_draft_Active workflow");
         //shouldn't be possible to change the name for a draft!
         assertFormElementNotPresent("newWorkflowName");
         setFormElement("description", "well not really since its a draft");
         submit("Update");

         assertTableCellHasText("workflows_table", 2, 1, "This workflow is active");
         assertTableCellHasText("workflows_table", 3, 1, "well not really since its a draft");
     }
}
