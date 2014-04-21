package com.atlassian.jira.webtests.ztests.workflow;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.AbstractTestWorkflowNameEditing;
import com.atlassian.jira.webtests.table.AndCell;
import com.atlassian.jira.webtests.table.LinkCell;
import com.atlassian.jira.webtests.table.NotCell;
import com.atlassian.jira.webtests.table.OrCell;
import com.atlassian.jira.webtests.table.TextCell;
import com.meterware.httpunit.WebTable;
import org.xml.sax.SAXException;

/**
 * Test that editing workflow name and description works in JIRA enterprise
 *
 */
@WebTest ({ Category.FUNC_TEST, Category.WORKFLOW })
public class TestWorkflowNameEditingEnterprise extends AbstractTestWorkflowNameEditing
{
    public TestWorkflowNameEditingEnterprise(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestWorkflowNameEditingEnterprise.xml");
    }

    public void tearDown()
    {
        restoreBlankInstance();
        super.tearDown();
    }

    public void testListWorkflowsOperationLinkVisibility() throws SAXException
    {
        gotoWorkFlow();

        //check the visibility of the Edit and Delete link for each workflow
        WebTable workflowsTable = getDialog().getResponse().getTableWithID("workflows_table");
        assertEquals(5, workflowsTable.getRowCount());
        assertTableRowEquals(workflowsTable, 0, new Object[] { "Name", "Description", "Status", "Schemes", "Number of steps", "Operations" });
        assertTableHasMatchingRowFrom(workflowsTable, 1, new Object[] {
                new TextCell("jira", "(Read-only System Workflow)"), "The default JIRA workflow.", "Active",
                new AndCell(new LinkCell("EditWorkflowSchemeEntities!default.jspa?schemeId=10001", "scheme with editable flow"),
                        new TextCell("Used by projects with no associated workflow scheme and by workflow schemes with unassigned issue types.")), "5",
                new AndCell(
                        new XsrfLinkCell("ViewWorkflowSteps.jspa?workflowMode=live&workflowName=jira", "Steps"),
                        new XsrfLinkCell("ViewWorkflowXml.jspa?workflowMode=live&workflowName=jira", "XML"),
                        new XsrfLinkCell("CloneWorkflow!default.jspa?workflowMode=live&workflowName=jira", "Copy"),
                        new NotCell(new OrCell(new TextCell("Edit"), new TextCell("Delete")))) });

        assertTableHasMatchingRowFrom(workflowsTable, 1, new Object[] { "Active workflow", "This workflow is active", "Active",
                new AndCell(new LinkCell("EditWorkflowSchemeEntities!default.jspa?schemeId=10001", "scheme with editable flow"),
                        new LinkCell("EditWorkflowSchemeEntities!default.jspa?schemeId=10000", "Active Scheme")), "5",
                new AndCell(
                        new XsrfLinkCell("ViewWorkflowSteps.jspa?workflowMode=live&workflowName=Active+workflow", "Steps"),
                        new XsrfLinkCell("ViewWorkflowXml.jspa?workflowMode=live&workflowName=Active+workflow", "XML"),
                        new XsrfLinkCell("CloneWorkflow!default.jspa?workflowMode=live&workflowName=Active+workflow", "Copy"),
                        new NotCell(new OrCell(new TextCell("Edit"), new TextCell("Delete")))) });

        assertTableHasMatchingRowFrom(workflowsTable, 1, new Object[] { "editable with no scheme", "this workflow does not have any scheme or project", "Inactive", "", "1",
                new AndCell(
                        new XsrfLinkCell("ViewWorkflowSteps.jspa?workflowMode=live&workflowName=editable+with+no+scheme", "Steps"),
                        new XsrfLinkCell("ViewWorkflowXml.jspa?workflowMode=live&workflowName=editable+with+no+scheme", "XML"),
                        new XsrfLinkCell("CloneWorkflow!default.jspa?workflowMode=live&workflowName=editable+with+no+scheme", "Copy"),
                        new XsrfLinkCell("EditWorkflow!default.jspa?workflowMode=live&workflowName=editable+with+no+scheme", "Edit"),
                        new XsrfLinkCell("DeleteWorkflow.jspa?workflowMode=live&workflowName=editable+with+no+scheme", "Delete")) });

        assertTableHasMatchingRowFrom(workflowsTable, 1, new Object[] { "editable workflow", "This workflow is editable as it is inactive", "Inactive",
                new LinkCell("EditWorkflowSchemeEntities!default.jspa?schemeId=10001", "scheme with editable flow"), "2",
                new AndCell(
                        new XsrfLinkCell("ViewWorkflowSteps.jspa?workflowMode=live&workflowName=editable+workflow", "Steps"),
                        new XsrfLinkCell("ViewWorkflowXml.jspa?workflowMode=live&workflowName=editable+workflow", "XML"),
                        new XsrfLinkCell("CloneWorkflow!default.jspa?workflowMode=live&workflowName=editable+workflow", "Copy"),
                        new XsrfLinkCell("EditWorkflow!default.jspa?workflowMode=live&workflowName=editable+workflow", "Edit"),
                        new NotCell(new TextCell("Delete"))) });
    }

    public void testEditInactiveWorkflowWithNoScheme() throws SAXException
    {
        gotoWorkFlow();

        WebTable workflowsTable = getDialog().getResponse().getTableWithID("workflows_table");
        assertEquals(5, workflowsTable.getRowCount());
        assertTableRowEquals(workflowsTable, 0, new Object[] { "Name", "Description", "Status", "Schemes", "Number of steps", "Operations" });

        assertTableHasMatchingRowFrom(workflowsTable, 1, new Object[] {
                new TextCell("jira", "(Read-only System Workflow)"), "The default JIRA workflow.", "Active",
                new AndCell(new LinkCell("EditWorkflowSchemeEntities!default.jspa?schemeId=10001", "scheme with editable flow"),
                        new TextCell("Used by projects with no associated workflow scheme and by workflow schemes with unassigned issue types.")), "5",
                new AndCell(new XsrfLinkCell("ViewWorkflowSteps.jspa?workflowMode=live&workflowName=jira", "Steps"),
                        new XsrfLinkCell("ViewWorkflowXml.jspa?workflowMode=live&workflowName=jira", "XML"),
                        new XsrfLinkCell("CloneWorkflow!default.jspa?workflowMode=live&workflowName=jira", "Copy"),
                        new NotCell(new OrCell(new TextCell("Edit"), new TextCell("Delete")))) });
        
        assertTableHasMatchingRowFrom(workflowsTable, 1, new Object[] { "Active workflow", "This workflow is active", "Active",
                new AndCell(new LinkCell("EditWorkflowSchemeEntities!default.jspa?schemeId=10001", "scheme with editable flow"),
                        new LinkCell("EditWorkflowSchemeEntities!default.jspa?schemeId=10000", "Active Scheme")), "5",
                new AndCell(new XsrfLinkCell("ViewWorkflowSteps.jspa?workflowMode=live&workflowName=Active+workflow", "Steps"),
                        new XsrfLinkCell("ViewWorkflowXml.jspa?workflowMode=live&workflowName=Active+workflow", "XML"),
                        new XsrfLinkCell("CloneWorkflow!default.jspa?workflowMode=live&workflowName=Active+workflow", "Copy"),
                        new NotCell(new OrCell(new TextCell("Edit"), new TextCell("Delete")))) });
        
        assertTableHasMatchingRowFrom(workflowsTable, 1, new Object[] { "editable workflow", "This workflow is editable as it is inactive", "Inactive",
                new LinkCell("EditWorkflowSchemeEntities!default.jspa?schemeId=10001", "scheme with editable flow"), "2",
                new AndCell(new XsrfLinkCell("ViewWorkflowSteps.jspa?workflowMode=live&workflowName=editable+workflow", "Steps"),
                        new XsrfLinkCell("ViewWorkflowXml.jspa?workflowMode=live&workflowName=editable+workflow", "XML"),
                        new XsrfLinkCell("CloneWorkflow!default.jspa?workflowMode=live&workflowName=editable+workflow", "Copy"),
                        new XsrfLinkCell("EditWorkflow!default.jspa?workflowMode=live&workflowName=editable+workflow", "Edit"),
                        new NotCell(new TextCell("Delete"))) });
        
        //assert that the workflow is editable with no associated schemes
        assertTableHasMatchingRowFrom(workflowsTable, 1, new Object[] { "editable with no scheme", "this workflow does not have any scheme or project", "Inactive", "", "1",
                new AndCell(new XsrfLinkCell("ViewWorkflowSteps.jspa?workflowMode=live&workflowName=editable+with+no+scheme", "Steps"),
                        new XsrfLinkCell("ViewWorkflowXml.jspa?workflowMode=live&workflowName=editable+with+no+scheme", "XML"),
                        new XsrfLinkCell("CloneWorkflow!default.jspa?workflowMode=live&workflowName=editable+with+no+scheme", "Copy"),
                        new XsrfLinkCell("EditWorkflow!default.jspa?workflowMode=live&workflowName=editable+with+no+scheme", "Edit"),
                        new XsrfLinkCell("DeleteWorkflow.jspa?workflowMode=live&workflowName=editable+with+no+scheme", "Delete")) });

        clickLink("edit_live_editable with no scheme");
        assertFormElementWithNameHasValue("edit-workflow", "newWorkflowName", "editable with no scheme");
        assertFormElementWithNameHasValue("edit-workflow", "description", "this workflow does not have any scheme or project");
        setFormElement("newWorkflowName", "edited with no scheme");
        setFormElement("description", "description has been edited");
        submit("Update");

        workflowsTable = getDialog().getResponse().getTableWithID("workflows_table");
        assertEquals(5, workflowsTable.getRowCount());
        assertTableRowEquals(workflowsTable, 0, new Object[] { "Name", "Description", "Status", "Schemes", "Number of steps", "Operations" });
        assertTableHasMatchingRowFrom(workflowsTable, 1, new Object[] {
                new TextCell("jira", "(Read-only System Workflow)"), "The default JIRA workflow.", "Active",
                new AndCell(
                        new LinkCell("EditWorkflowSchemeEntities!default.jspa?schemeId=10001", "scheme with editable flow"),
                        new TextCell("Used by projects with no associated workflow scheme and by workflow schemes with unassigned issue types.")), "5",
                new AndCell(
                        new XsrfLinkCell("ViewWorkflowSteps.jspa?workflowMode=live&workflowName=jira", "Steps"),
                        new XsrfLinkCell("ViewWorkflowXml.jspa?workflowMode=live&workflowName=jira", "XML"),
                        new XsrfLinkCell("CloneWorkflow!default.jspa?workflowMode=live&workflowName=jira", "Copy"),
                        new NotCell(new OrCell(new TextCell("Edit"), new TextCell("Delete")))) });
        
        assertTableHasMatchingRowFrom(workflowsTable, 1, new Object[] { "Active workflow", "This workflow is active", "Active",
                new AndCell(new LinkCell("EditWorkflowSchemeEntities!default.jspa?schemeId=10001", "scheme with editable flow"),
                        new LinkCell("EditWorkflowSchemeEntities!default.jspa?schemeId=10000", "Active Scheme")), "5",
                new AndCell(new XsrfLinkCell("ViewWorkflowSteps.jspa?workflowMode=live&workflowName=Active+workflow", "Steps"),
                        new XsrfLinkCell("ViewWorkflowXml.jspa?workflowMode=live&workflowName=Active+workflow", "XML"),
                        new XsrfLinkCell("CloneWorkflow!default.jspa?workflowMode=live&workflowName=Active+workflow", "Copy"),
                        new NotCell(new OrCell(new TextCell("Edit"), new TextCell("Delete")))) });
        
        assertTableHasMatchingRowFrom(workflowsTable, 1, new Object[] { "editable workflow", "This workflow is editable as it is inactive", "Inactive",
                new LinkCell("EditWorkflowSchemeEntities!default.jspa?schemeId=10001", "scheme with editable flow"), "2",
                new AndCell(new XsrfLinkCell("ViewWorkflowSteps.jspa?workflowMode=live&workflowName=editable+workflow", "Steps"),
                        new XsrfLinkCell("ViewWorkflowXml.jspa?workflowMode=live&workflowName=editable+workflow", "XML"),
                        new XsrfLinkCell("CloneWorkflow!default.jspa?workflowMode=live&workflowName=editable+workflow", "Copy"),
                        new XsrfLinkCell("EditWorkflow!default.jspa?workflowMode=live&workflowName=editable+workflow", "Edit"),
                        new NotCell(new TextCell("Delete"))) });
        
        //check that the original entry doesnt exist
        assertTableHasNoMatchingRow(workflowsTable, 1, new Object[] { "editable with no scheme", "this workflow does not have any scheme or project", "Inactive", "", "1",
                new AndCell(
                        new XsrfLinkCell("ViewWorkflowSteps.jspa?workflowMode=live&workflowName=editable+with+no+scheme", "Steps"),
                        new XsrfLinkCell("ViewWorkflowXml.jspa?workflowMode=live&workflowName=editable+with+no+scheme", "XML"),
                        new XsrfLinkCell("CloneWorkflow!default.jspa?workflowMode=live&workflowName=editable+with+no+scheme", "Copy"),
                        new XsrfLinkCell("EditWorkflow!default.jspa?workflowMode=live&workflowName=editable+with+no+scheme", "Edit"),
                        new XsrfLinkCell("DeleteWorkflow.jspa?workflowMode=live&workflowName=editable+with+no+scheme", "Delete")) });

        //check that the workflow is edited
        assertTableHasMatchingRowFrom(workflowsTable, 1, new Object[] { "edited with no scheme", "description has been edited", "Inactive", "", "1",
                new AndCell(new XsrfLinkCell("ViewWorkflowSteps.jspa?workflowMode=live&workflowName=edited+with+no+scheme", "Steps"),
                        new XsrfLinkCell("ViewWorkflowXml.jspa?workflowMode=live&workflowName=edited+with+no+scheme", "XML"),
                        new XsrfLinkCell("CloneWorkflow!default.jspa?workflowMode=live&workflowName=edited+with+no+scheme", "Copy"),
                        new XsrfLinkCell("EditWorkflow!default.jspa?workflowMode=live&workflowName=edited+with+no+scheme", "Edit"),
                        new XsrfLinkCell("DeleteWorkflow.jspa?workflowMode=live&workflowName=edited+with+no+scheme", "Delete")) });
    }

    public void testEditInactiveWorkflowWithScheme() throws SAXException
    {
        //goto the workflow scheme associated with the inactive workflow
        gotoPage("/secure/admin/EditWorkflowSchemeEntities!default.jspa?schemeId=10001");
        assertLinkWithTextUrlContains("editable workflow", new String[]{"secure/admin/workflows/ViewWorkflowSteps.jspa", "workflowMode=live", "workflowName=editable+workflow"});
        assertTextNotPresent("edited with a scheme");

        gotoWorkFlow();

        WebTable workflowsTable = getDialog().getResponse().getTableWithID("workflows_table");
        assertEquals(5, workflowsTable.getRowCount());
        assertTableRowEquals(workflowsTable, 0, new Object[] { "Name", "Description", "Status", "Schemes", "Number of steps", "Operations" });
        assertTableHasMatchingRowFrom(workflowsTable, 1, new Object[] {
                new TextCell("jira", "(Read-only System Workflow)"), "The default JIRA workflow.", "Active",
                new AndCell(new LinkCell("EditWorkflowSchemeEntities!default.jspa?schemeId=10001", "scheme with editable flow"),
                        new TextCell("Used by projects with no associated workflow scheme and by workflow schemes with unassigned issue types.")), "5",
                new AndCell(
                        new XsrfLinkCell("ViewWorkflowSteps.jspa?workflowMode=live&workflowName=jira", "Steps"),
                        new XsrfLinkCell("ViewWorkflowXml.jspa?workflowMode=live&workflowName=jira", "XML"),
                        new XsrfLinkCell("CloneWorkflow!default.jspa?workflowMode=live&workflowName=jira", "Copy"),
                        new NotCell(new OrCell(new TextCell("Edit"), new TextCell("Delete")))) });

        assertTableHasMatchingRowFrom(workflowsTable, 1, new Object[] { "Active workflow", "This workflow is active", "Active",
                new AndCell(new LinkCell("EditWorkflowSchemeEntities!default.jspa?schemeId=10001", "scheme with editable flow"),
                        new LinkCell("EditWorkflowSchemeEntities!default.jspa?schemeId=10000", "Active Scheme")), "5",
                new AndCell(
                        new XsrfLinkCell("ViewWorkflowSteps.jspa?workflowMode=live&workflowName=Active+workflow", "Steps"),
                        new XsrfLinkCell("ViewWorkflowXml.jspa?workflowMode=live&workflowName=Active+workflow", "XML"),
                        new XsrfLinkCell("CloneWorkflow!default.jspa?workflowMode=live&workflowName=Active+workflow", "Copy"),
                        new NotCell(new OrCell(new TextCell("Edit"), new TextCell("Delete")))) });
        
        assertTableHasMatchingRowFrom(workflowsTable, 1, new Object[] { "editable with no scheme", "this workflow does not have any scheme or project", "Inactive", "", "1",
                new AndCell(
                        new XsrfLinkCell("ViewWorkflowSteps.jspa?workflowMode=live&workflowName=editable+with+no+scheme", "Steps"),
                        new XsrfLinkCell("ViewWorkflowXml.jspa?workflowMode=live&workflowName=editable+with+no+scheme", "XML"),
                        new XsrfLinkCell("CloneWorkflow!default.jspa?workflowMode=live&workflowName=editable+with+no+scheme", "Copy"),
                        new XsrfLinkCell("EditWorkflow!default.jspa?workflowMode=live&workflowName=editable+with+no+scheme", "Edit"),
                        new XsrfLinkCell("DeleteWorkflow.jspa?workflowMode=live&workflowName=editable+with+no+scheme", "Delete")) });
        
        //assert that the workflow is editable with an associated scheme
        assertTableHasMatchingRowFrom(workflowsTable, 1, new Object[] { "editable workflow", "This workflow is editable as it is inactive", "Inactive",
                new LinkCell("EditWorkflowSchemeEntities!default.jspa?schemeId=10001", "scheme with editable flow"), "2",
                new AndCell(
                        new XsrfLinkCell("ViewWorkflowSteps.jspa?workflowMode=live&workflowName=editable+workflow", "Steps"),
                        new XsrfLinkCell("ViewWorkflowXml.jspa?workflowMode=live&workflowName=editable+workflow", "XML"),
                        new XsrfLinkCell("CloneWorkflow!default.jspa?workflowMode=live&workflowName=editable+workflow", "Copy"),
                        new XsrfLinkCell("EditWorkflow!default.jspa?workflowMode=live&workflowName=editable+workflow", "Edit"),
                        new NotCell(new TextCell("Delete"))) });

        clickLink("edit_live_editable workflow");
        assertFormElementWithNameHasValue("edit-workflow", "newWorkflowName", "editable workflow");
        assertFormElementWithNameHasValue("edit-workflow", "description", "This workflow is editable as it is inactive");
        setFormElement("newWorkflowName", "edited with a scheme");
        setFormElement("description", "description is edited");
        submit("Update");

        workflowsTable = getDialog().getResponse().getTableWithID("workflows_table");
        assertEquals(5, workflowsTable.getRowCount());
        assertTableRowEquals(workflowsTable, 0, new Object[] { "Name", "Description", "Status", "Schemes", "Number of steps", "Operations" });
        assertTableHasMatchingRowFrom(workflowsTable, 1, new Object[] {
                new TextCell("jira", "(Read-only System Workflow)"), "The default JIRA workflow.", "Active",
                new AndCell(
                        new LinkCell("EditWorkflowSchemeEntities!default.jspa?schemeId=10001", "scheme with editable flow"),
                        new TextCell("Used by projects with no associated workflow scheme and by workflow schemes with unassigned issue types.")), "5",
                new AndCell(
                        new XsrfLinkCell("ViewWorkflowSteps.jspa?workflowMode=live&workflowName=jira", "Steps"),
                        new XsrfLinkCell("ViewWorkflowXml.jspa?workflowMode=live&workflowName=jira", "XML"),
                        new XsrfLinkCell("CloneWorkflow!default.jspa?workflowMode=live&workflowName=jira", "Copy"),
                        new NotCell(new OrCell(new TextCell("Edit"), new TextCell("Delete")))) });
        
        assertTableHasMatchingRowFrom(workflowsTable, 1, new Object[] { "Active workflow", "This workflow is active", "Active",
                new AndCell(
                        new LinkCell("EditWorkflowSchemeEntities!default.jspa?schemeId=10001", "scheme with editable flow"),
                        new LinkCell("EditWorkflowSchemeEntities!default.jspa?schemeId=10000", "Active Scheme")), "5",
                new AndCell(
                        new XsrfLinkCell("ViewWorkflowSteps.jspa?workflowMode=live&workflowName=Active+workflow", "Steps"),
                        new XsrfLinkCell("ViewWorkflowXml.jspa?workflowMode=live&workflowName=Active+workflow", "XML"),
                        new XsrfLinkCell("CloneWorkflow!default.jspa?workflowMode=live&workflowName=Active+workflow", "Copy"),
                        new NotCell(new OrCell(new TextCell("Edit"), new TextCell("Delete")))) });
        
        assertTableHasMatchingRowFrom(workflowsTable, 1, new Object[] { "editable with no scheme", "this workflow does not have any scheme or project", "Inactive", "", "1",
                new AndCell(
                        new XsrfLinkCell("ViewWorkflowSteps.jspa?workflowMode=live&workflowName=editable+with+no+scheme", "Steps"),
                        new XsrfLinkCell("ViewWorkflowXml.jspa?workflowMode=live&workflowName=editable+with+no+scheme", "XML"),
                        new XsrfLinkCell("CloneWorkflow!default.jspa?workflowMode=live&workflowName=editable+with+no+scheme", "Copy"),
                        new XsrfLinkCell("EditWorkflow!default.jspa?workflowMode=live&workflowName=editable+with+no+scheme", "Edit"),
                        new XsrfLinkCell("DeleteWorkflow.jspa?workflowMode=live&workflowName=editable+with+no+scheme", "Delete")) });
        
        //check that the original entry doesnt exist
        assertTableHasNoMatchingRow(workflowsTable, 1, new Object[] { "editable workflow", "This workflow is editable as it is inactive", "Inactive",
                new LinkCell("EditWorkflowSchemeEntities!default.jspa?schemeId=10001", "scheme with editable flow"), "2",
                new AndCell(
                        new XsrfLinkCell("ViewWorkflowSteps.jspa?workflowMode=live&workflowName=editable+workflow", "Steps"),
                        new XsrfLinkCell("ViewWorkflowXml.jspa?workflowMode=live&workflowName=editable+workflow", "XML"),
                        new XsrfLinkCell("CloneWorkflow!default.jspa?workflowMode=live&workflowName=editable+workflow", "Copy"),
                        new XsrfLinkCell("EditWorkflow!default.jspa?workflowMode=live&workflowName=editable+workflow", "Edit"),
                        new NotCell(new TextCell("Delete"))) });
        //check that the workflow is edited
        assertTableHasMatchingRowFrom(workflowsTable, 1, new Object[] { "edited with a scheme", "description is edited", "Inactive",
                new LinkCell("EditWorkflowSchemeEntities!default.jspa?schemeId=10001", "scheme with editable flow"), "2",
                new AndCell(
                        new XsrfLinkCell("ViewWorkflowSteps.jspa?workflowMode=live&workflowName=edited+with+a+scheme", "Steps"),
                        new XsrfLinkCell("ViewWorkflowXml.jspa?workflowMode=live&workflowName=edited+with+a+scheme", "XML"),
                        new XsrfLinkCell("CloneWorkflow!default.jspa?workflowMode=live&workflowName=edited+with+a+scheme", "Copy"),
                        new XsrfLinkCell("EditWorkflow!default.jspa?workflowMode=live&workflowName=edited+with+a+scheme", "Edit"),
                        new NotCell(new TextCell("Delete"))) });

        //goto the associated schemes and verify that the workflow name has changed
        gotoPage("/secure/admin/EditWorkflowSchemeEntities!default.jspa?schemeId=10001");
        assertLinkWithTextUrlContains("edited with a scheme", new String[]{"secure/admin/workflows/ViewWorkflowSteps.jspa", "workflowMode=live", "workflowName=edited+with+a+scheme"});
        assertTextNotPresent("editable workflow");
        gotoPage("/secure/admin/EditWorkflowSchemeEntities!default.jspa?schemeId=10010");
        assertLinkWithTextUrlContains("edited with a scheme", new String[]{"secure/admin/workflows/ViewWorkflowSteps.jspa", "workflowMode=live", "workflowName=edited+with+a+scheme"});
        assertTextNotPresent("editable workflow");

        //verify that the workflow name edit is still valid by associating the schemes with projects and verify a single transition works.
        associateWorkFlowSchemeToProject(PROJECT_MONKEY, "scheme with editable flow");
        waitForSuccessfulWorkflowSchemeMigration(PROJECT_MONKEY, "scheme with editable flow");
        associateWorkFlowSchemeToProject(PROJECT_NEO, "scheme with renamed workflow");
        waitForSuccessfulWorkflowSchemeMigration(PROJECT_NEO, "scheme with renamed workflow");

        //for project monkey, check that the renamed workflow only effects unassigned issue types (ie. task and new feature)
        assertAddingIssueForModifiedWorkflow(PROJECT_MONKEY, PROJECT_MONKEY_KEY, ISSUE_TYPE_NEWFEATURE, "MONKEY new feature issue of the workflow that was renamed");
        assertAddingIssueForModifiedWorkflow(PROJECT_MONKEY, PROJECT_MONKEY_KEY, ISSUE_TYPE_TASK, "MONKEY task issue of the workflow that was renamed");
        //also check that the other assigned issue types are not affected (ie. improvement and bug)
        assertAddingIssueForUnModifiedWorkflow(PROJECT_MONKEY, PROJECT_MONKEY_KEY, ISSUE_TYPE_BUG, "MONKEY bug issue of the workflow that was NOT renamed");
        assertAddingIssueForUnModifiedWorkflow(PROJECT_MONKEY, PROJECT_MONKEY_KEY, ISSUE_TYPE_IMPROVEMENT, "MONKEY improvement issue of the workflow that was NOT renamed");

        //for project neanderthal, check that the renamed workflow affects all unassigned issue types (ie. all the issues types)
        assertAddingIssueForModifiedWorkflow(PROJECT_NEO, PROJECT_NEO_KEY, ISSUE_TYPE_BUG, "NEO bug issue of the workflow that was renamed");
        assertAddingIssueForModifiedWorkflow(PROJECT_NEO, PROJECT_NEO_KEY, ISSUE_TYPE_IMPROVEMENT, "NEO improvement issue of the workflow that was renamed");
        assertAddingIssueForModifiedWorkflow(PROJECT_NEO, PROJECT_NEO_KEY, ISSUE_TYPE_NEWFEATURE, "NEO new feature issue of the workflow that was renamed");
        assertAddingIssueForModifiedWorkflow(PROJECT_NEO, PROJECT_NEO_KEY, ISSUE_TYPE_TASK, "NEO task issue of the workflow that was renamed");
    }

    private void assertAddingIssueForUnModifiedWorkflow(String project, String projectKey, String issueType, String summary)
    {
        addIssue(project, projectKey, issueType, summary);
        assertTextNotPresent("RESOLVE WITH EDITED WO...");
        assertLinkPresentWithText("Start Progress");
        assertLinkPresentWithText("Resolve Issue");
        assertLinkPresentWithText("Close Issue");
        assertTextPresent("Open");
        assertTextNotPresent("In Progress");
        clickLink("action_id_4");//start progress
        assertTextPresent("In Progress");
        assertTextNotPresent("Open");
    }

    private void assertAddingIssueForModifiedWorkflow(String project, String projectKey, String issueType, String summary)
    {
        addIssue(project, projectKey, issueType, summary);
        assertLinkPresentWithText("RESOLVE WITH EDITED WO...");
        assertTextPresent("Open");
        assertTextNotPresent("Resolved");
        clickLink("action_id_11");//resolve the issue by clicking on the workflow transition
        assertTextPresent("Resolved");
        assertTextNotPresent("Open");
    }

    private class XsrfLinkCell extends com.atlassian.jira.webtests.table.LinkCell
    {
        private XsrfLinkCell(final String url, final String label)
        {
            super(page.addXsrfToken(url), label);
        }
    }
}
