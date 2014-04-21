package com.atlassian.jira.webtests.ztests.workflow;

import com.atlassian.jira.functest.framework.assertions.TableAssertions;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;
import com.atlassian.jira.webtests.table.AndCell;
import com.atlassian.jira.webtests.table.LinkCell;
import com.atlassian.jira.webtests.table.StrictTextCell;
import com.atlassian.jira.webtests.table.TextCell;

/**
 *
 * @since v3.13
 */
@WebTest ({ Category.FUNC_TEST, Category.WORKFLOW })
public class TestWorkflowEditor extends JIRAWebTest
{
    private static final String WORKFLOW_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                                               + "<!DOCTYPE workflow PUBLIC \"-//OpenSymphony Group//DTD OSWorkflow 2.8//EN\" \"http://www.opensymphony.com/osworkflow/workflow_2_8.dtd\">\n"
                                               + "<workflow>\n"
                                               + "  <meta name=\"jira.update.author.name\">admin</meta>\n"
                                               + "  <meta name=\"jira.description\"></meta>\n"
                                               + "  <meta name=\"jira.updated.date\">1196830052833</meta>\n"
                                               + "  <initial-actions>\n"
                                               + "    <action id=\"1\" name=\"Create\">\n"
                                               + "      <validators>\n"
                                               + "        <validator name=\"\" type=\"class\">\n"
                                               + "          <arg name=\"class.name\">com.atlassian.jira.workflow.validator.PermissionValidator</arg>\n"
                                               + "          <arg name=\"permission\">Create Issue</arg>\n"
                                               + "        </validator>\n"
                                               + "      </validators>\n"
                                               + "      <results>\n"
                                               + "        <unconditional-result old-status=\"null\" status=\"open\" step=\"1\">\n"
                                               + "          <post-functions>\n"
                                               + "            <function type=\"class\">\n"
                                               + "              <arg name=\"class.name\">com.atlassian.jira.workflow.function.issue.IssueCreateFunction</arg>\n"
                                               + "            </function>\n"
                                               + "            <function type=\"class\">\n"
                                               + "              <arg name=\"class.name\">com.atlassian.jira.workflow.function.issue.IssueReindexFunction</arg>\n"
                                               + "            </function>\n"
                                               + "            <function type=\"class\">\n"
                                               + "              <arg name=\"class.name\">com.atlassian.jira.workflow.function.event.FireIssueEventFunction</arg>\n"
                                               + "              <arg name=\"eventTypeId\">1</arg>\n"
                                               + "            </function>\n"
                                               + "          </post-functions>\n"
                                               + "        </unconditional-result>\n"
                                               + "      </results>\n"
                                               + "    </action>\n"
                                               + "  </initial-actions>\n"
                                               + "  <steps>\n"
                                               + "    <step id=\"1\" name=\"Open\">\n"
                                               + "      <meta name=\"jira.status.id\">1</meta>\n"
                                               + "    </step>\n"
                                               + "  </steps>\n"
                                               + "</workflow>";

    TableAssertions tableAssertions;

    public TestWorkflowEditor(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestWorkflowEditor.xml");
        tableAssertions = new TableAssertions(tester, environmentData);
    }

    public void testWorkflowListingEnterprise()
    {
        //assert pre-conditions
        gotoWorkFlow();
        assertEquals(5, getWebTableWithID("workflows_table").getRowCount());
        assertTableRowEquals(getWebTableWithID("workflows_table"), 0,
                new Object[] { new TextCell("Name"),
                               new TextCell("Description"),
                               new TextCell("Status"),
                               new TextCell("Schemes"),
                               new TextCell("Number of steps"),
                               new TextCell("Operations")
                });

        assertTableRowEquals(getWebTableWithID("workflows_table"), 1,
                new Object[] { new TextCell("jira", "Read-only System Workflow"),
                               new TextCell("The default JIRA workflow."),
                               new TextCell("Inactive"),
                               new TextCell("Used by projects with no associated workflow scheme and by workflow schemes with unassigned issue types."),
                               new TextCell("5"),
                               new AndCell(
                                       new XsrfLinkCell("ViewWorkflowSteps.jspa?workflowMode=live&workflowName=jira", "Steps"),
                                       new XsrfLinkCell("ViewWorkflowXml.jspa?workflowMode=live&workflowName=jira", "XML"),
                                       new XsrfLinkCell("CloneWorkflow!default.jspa?workflowMode=live&workflowName=jira", "Copy"))
                });
        assertTableRowEquals(getWebTableWithID("workflows_table"), 2,
                new Object[] { new TextCell("Workflow1", "Last modified on"),
                               new StrictTextCell(""),
                               new TextCell("Active"),
                               new LinkCell("EditWorkflowSchemeEntities!default.jspa?schemeId=10010", "WorkflowScheme_Workflow1"),
                               new TextCell("1"),
                               new AndCell(
                                       new XsrfLinkCell("ViewWorkflowSteps.jspa?workflowMode=live&workflowName=Workflow1", "Steps"),
                                       new XsrfLinkCell("ViewWorkflowXml.jspa?workflowMode=live&workflowName=Workflow1", "XML"),
                                       new XsrfLinkCell("CloneWorkflow!default.jspa?workflowMode=live&workflowName=Workflow1", "Copy"),
                                       new XsrfLinkCell("CreateDraftWorkflow.jspa?draftWorkflowName=Workflow1", "Create Draft"))
                });
        assertTableRowEquals(getWebTableWithID("workflows_table"), 3,
                new Object[] { new TextCell("Workflow2", "Last modified on"),
                               new StrictTextCell(""),
                               new TextCell("Inactive"),
                               new StrictTextCell(""),
                               new TextCell("1"),
                               new AndCell(
                                       new XsrfLinkCell("ViewWorkflowSteps.jspa?workflowMode=live&workflowName=Workflow2", "Steps"),
                                       new XsrfLinkCell("ViewWorkflowXml.jspa?workflowMode=live&workflowName=Workflow2", "XML"),
                                       new XsrfLinkCell("CloneWorkflow!default.jspa?workflowMode=live&workflowName=Workflow2", "Copy"),
                                       new XsrfLinkCell("EditWorkflow!default.jspa?workflowMode=live&workflowName=Workflow2", "Edit"),
                                       new XsrfLinkCell("DeleteWorkflow.jspa?workflowMode=live&workflowName=Workflow2", "Delete"))
                });
        assertTableRowEquals(getWebTableWithID("workflows_table"), 4,
                new Object[] { new TextCell("Workflow3", "Last modified on"),
                               new StrictTextCell(""),
                               new TextCell("Inactive"),
                               new StrictTextCell(""),
                               new TextCell("1"),
                               new AndCell(
                                       new XsrfLinkCell("ViewWorkflowSteps.jspa?workflowMode=live&workflowName=Workflow3", "Steps"),
                                       new XsrfLinkCell("ViewWorkflowXml.jspa?workflowMode=live&workflowName=Workflow3", "XML"),
                                       new XsrfLinkCell("CloneWorkflow!default.jspa?workflowMode=live&workflowName=Workflow3", "Copy"),
                                       new XsrfLinkCell("EditWorkflow!default.jspa?workflowMode=live&workflowName=Workflow3", "Edit"),
                                       new XsrfLinkCell("DeleteWorkflow.jspa?workflowMode=live&workflowName=Workflow3", "Delete"))
                });

        //try copying a workflow
        clickLink("copy_Workflow3");
        assertTextPresent("Copy Workflow: Workflow3");
        setFormElement("newWorkflowName", "XX Copy Of Workflow3");
        setFormElement("description", "Description of Workflow 3 copy");
        submit("Update");

        assertTextPresent("View Workflows");
        assertTableRowEquals(getWebTableWithID("workflows_table"), 5,
                new Object[] { new TextCell("XX Copy Of Workflow3"),
                               new TextCell("Description of Workflow 3 copy"),
                               new TextCell("Inactive"),
                               new StrictTextCell(""),
                               new TextCell("1"),
                               new AndCell(
                                       new XsrfLinkCell("ViewWorkflowSteps.jspa?workflowMode=live&workflowName=XX+Copy+Of+Workflow3", "Steps"),
                                       new XsrfLinkCell("ViewWorkflowXml.jspa?workflowMode=live&workflowName=XX+Copy+Of+Workflow3", "XML"),
                                       new XsrfLinkCell("CloneWorkflow!default.jspa?workflowMode=live&workflowName=XX+Copy+Of+Workflow3", "Copy"),
                                       new XsrfLinkCell("EditWorkflow!default.jspa?workflowMode=live&workflowName=XX+Copy+Of+Workflow3", "Edit"),
                                       new XsrfLinkCell("DeleteWorkflow.jspa?workflowMode=live&workflowName=XX+Copy+Of+Workflow3", "Delete"))
                });

        //try deleting a workflow
        clickLink("del_Workflow2");
        assertTextPresent("Delete Workflow");
        assertTextPresent("Confirm that you want to delete the workflow <strong>Workflow2</strong>.");
        assertLinkPresentWithText("do a full backup");
        assertLinkPresentWithText("export");
        submit("Delete");
        assertTextNotPresent("Worfklow2");

        //try XML view of a workflow
        clickLink("xml_Workflow3");
        //should really assert the XML is equal here, but since the ordering of elements is different depending on
        //JDK, its a bit tricky.  XMLUnit doesn't help either, since it's the meta elements (with attributes) that
        //are different in order.
        assertEquals(200, getDialog().getResponse().getResponseCode());

        //try adding a workflow
        gotoWorkFlow();
        setFormElement("newWorkflowName", "ZZ This is a new Workflow!");
        setFormElement("description", "A new Workflow.");
        submit("Add");
        assertTextPresent("View Workflows");
        assertTableRowEquals(getWebTableWithID("workflows_table"), 5,
                new Object[] { new TextCell("ZZ This is a new Workflow!"),
                               new TextCell("A new Workflow."),
                               new TextCell("Inactive"),
                               new StrictTextCell(""),
                               new TextCell("1"),
                               new AndCell(
                                       new XsrfLinkCell("ViewWorkflowSteps.jspa?workflowMode=live&workflowName=ZZ+This+is+a+new+Workflow%21", "Steps"),
                                       new XsrfLinkCell("ViewWorkflowXml.jspa?workflowMode=live&workflowName=ZZ+This+is+a+new+Workflow%21", "XML"),
                                       new XsrfLinkCell("CloneWorkflow!default.jspa?workflowMode=live&workflowName=ZZ+This+is+a+new+Workflow%21", "Copy"),
                                       new XsrfLinkCell("EditWorkflow!default.jspa?workflowMode=live&workflowName=ZZ+This+is+a+new+Workflow%21", "Edit"),
                                       new XsrfLinkCell("DeleteWorkflow.jspa?workflowMode=live&workflowName=ZZ+This+is+a+new+Workflow%21", "Delete"))
                });

        //try importing a workflow via XML.
        clickLinkWithText("import a workflow from XML");
        setFormElement("name", "ZZZ Imported workflow");
        setFormElement("description", "This was imported from XML!");
        setFormElement("workflowXML", "Invalid input");
        submit("Import");
        assertTextPresent("Error parsing workflow XML");
        setFormElement("workflowXML", WORKFLOW_XML);
        submit("Import");

        assertTableRowEquals(getWebTableWithID("workflows_table"), 6,
                new Object[] { new TextCell("ZZZ Imported workflow"),
                               new TextCell("This was imported from XML!"),
                               new TextCell("Inactive"),
                               new StrictTextCell(""),
                               new TextCell("1"),
                               new AndCell(
                                       new XsrfLinkCell("ViewWorkflowSteps.jspa?workflowMode=live&workflowName=ZZZ+Imported+workflow", "Steps"),
                                       new XsrfLinkCell("ViewWorkflowXml.jspa?workflowMode=live&workflowName=ZZZ+Imported+workflow", "XML"),
                                       new XsrfLinkCell("CloneWorkflow!default.jspa?workflowMode=live&workflowName=ZZZ+Imported+workflow", "Copy"),
                                       new XsrfLinkCell("EditWorkflow!default.jspa?workflowMode=live&workflowName=ZZZ+Imported+workflow", "Edit"),
                                       new XsrfLinkCell("DeleteWorkflow.jspa?workflowMode=live&workflowName=ZZZ+Imported+workflow", "Delete"))
                });
    }

    public void testActiveWorkflowCantBeEdited()
    {
        gotoWorkFlow();
        //First check that none of the links are present.
        clickLink("steps_live_Workflow1");
        assertTextSequence(new String[] { "View Workflow Steps", "Workflow1" });
        assertLinkNotPresentWithText("Add Transition");
        assertLinkNotPresentWithText("edit_step_1");
        assertTextNotPresent("Add New Step");

        //check on properties too
        clickLinkWithText("View Properties");
        assertTextPresent("View Workflow Step Properties: Open");
        assertTextNotPresent("Add New Property");

        //now lets go to the single step view
        navigation.clickLinkWithExactText("Open");
        assertTextSequence(new String[] { "View Workflow Step", "Open" });
        assertLinkNotPresentWithText("Add");
        assertLinkNotPresentWithText("edit_step");

        // the step properties too
        clickLink("view_properties_1");
        assertTextPresent("View Workflow Step Properties: Open");
        assertTextNotPresent("Add New Property");

        //finally lets go to a transition.
        clickLinkWithText("workflow steps");
        navigation.clickLinkWithExactText("Open");
        clickLink("view_transition_1");

        assertTextPresent("Transition: Create");
        assertLinkNotPresentWithText("edit_transition");
        assertLinkNotPresentWithText("Delete");
        clickLinkWithText("properties of this transition");
        assertTextPresent("View Workflow Transition Properties: Create");
        assertTextNotPresent("Add New Property");

        //Then try hacking some URLs!

        //Edit the workflow
        gotoPage("secure/admin/workflows/EditWorkflow!default.jspa?workflowMode=live&workflowName=Workflow1");
        setFormElement("newWorkflowName", "HackingTheActiveWorkflow");
        submit("Update");
        assertTextPresent("Workflow cannot be edited as it is not editable.");
        gotoWorkFlow();
        assertTextNotPresent("HackingTheActiveWorkflow");

        //add a transition
        gotoPage("secure/admin/workflows/AddWorkflowTransition!default.jspa?workflowMode=live&workflowName=Workflow1&workflowStep=1");
        assertTextPresent("Add Workflow Transition");
        setFormElement("transitionName", "New transition");
        submit("Add");
        assertTextPresent("The workflow you are trying to update is not editable.");
        gotoWorkFlow();
        clickLink("steps_live_Workflow1");
        assertTextNotPresent("New transition");

        //Delete a Transition. Need to create an active workflow with a transition first. bah.
        gotoWorkFlow();
        clickLink("createDraft_Workflow1");
        setFormElement("stepName", "New Step");
        submit("Add");
        clickLinkWithText("Add Transition");
        setFormElement("transitionName", "Dude Transition");
        submit("Add");
        gotoWorkFlow();
        clickLink("publishDraft_Workflow1");
        checkCheckbox("enableBackup", "false");
        submit("Publish");

        gotoPage("secure/admin/workflows/DeleteWorkflowTransitions!confirm.jspa?workflowMode=live&workflowName=Workflow1&transitionIds=11&workflowStep=2");
        assertTextPresent("Delete Workflow Transitions");
        submit("Delete");
        assertTextPresent("The workflow you are trying to update is not editable.");
        gotoWorkFlow();
        clickLink("steps_live_Workflow1");
        assertTextPresent("Dude Transition");

        //Edit a step
        gotoPage("secure/admin/workflows/EditWorkflowStep!default.jspa?workflowMode=live&workflowName=Workflow1&workflowStep=1");
        setFormElement("stepName", "EditedWorkflowStep");
        submit("Update");
        assertTextPresent("The workflow you are trying to update is not editable.");
        gotoWorkFlow();
        clickLink("steps_live_Workflow1");
        assertTextNotPresent("EditedWorkflowStep");

        //Delete a step
        gotoPage("secure/admin/workflows/DeleteWorkflowStep!default.jspa?workflowMode=live&workflowName=Workflow1&workflowStep=1");
        submit("Delete");
        assertTextPresent("Cannot delete step. This workflow is not editable.");
        //view the step and make sure it's still there!
        gotoPage("secure/admin/workflows/ViewWorkflowStep.jspa?workflowMode=live&workflowName=Workflow1&workflowStep=1");
        assertTextSequence(new String[] { "View Workflow Step", "Open" });
        assertTextSequence(new String[] { "This page shows the details of the", "Open", "step." });
    }

    public void testViewWorkflowSteps()
    {
        gotoWorkFlow();

        //lets got to Workflow3 and assert all the pre-conditions
        clickLink("steps_live_Workflow3");
        assertTextSequence(new String[] { "View Workflow Steps", "Workflow3" });
        text.assertTextPresent(locator.page(), "This shows all of the steps for Workflow3.");
        assertLinkPresentWithText("workflows.");
        assertLinkPresentWithText("statuses.");

        assertEquals(2, getWebTableWithID("steps_table").getRowCount());
        assertTableRowEquals(getWebTableWithID("steps_table"), 0,
                new Object[] { new TextCell("Step Name", "(id)"),
                               new TextCell("Linked Status"),
                               new TextCell("Transitions", "(id)"),
                               new TextCell("Operations"),
                });
        assertTableRowEquals(getWebTableWithID("steps_table"), 1,
                new Object[] { new AndCell(new XsrfLinkCell("ViewWorkflowStep.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=1", "Open"), new TextCell("(1)")),
                               new TextCell("Open"),
                               new StrictTextCell(""),
                               new AndCell(new XsrfLinkCell("AddWorkflowTransition!default.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=1", "Add Transition"),
                                       new XsrfLinkCell("EditWorkflowStep!default.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=1", "Edit"),
                                       new XsrfLinkCell("ViewWorkflowStepMetaAttributes.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=1", "View Properties"))
                });

        //lets add a Step
        setFormElement("stepName", "");
        submit("Add");
        assertTextPresent("Step name must be specified.");

        setFormElement("stepName", "Resolved");
        selectOption("stepStatus", "Resolved");
        submit("Add");

        assertTableRowEquals(getWebTableWithID("steps_table"), 2,
                new Object[] { new AndCell(new XsrfLinkCell("ViewWorkflowStep.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "Resolved"), new TextCell("(2)")),
                               new TextCell("Resolved"),
                               new StrictTextCell(""),
                               new AndCell(new XsrfLinkCell("AddWorkflowTransition!default.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "Add Transition"),
                                       new XsrfLinkCell("EditWorkflowStep!default.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "Edit"),
                                       new XsrfLinkCell("ViewWorkflowStepMetaAttributes.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "View Properties"),
                                       new XsrfLinkCell("DeleteWorkflowStep!default.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "Delete Step"))
                });

        //Add a Transition
        clickLink("add_trans_2");
        setFormElement("transitionName", "Re-open");
        setFormElement("description", "This transition re-opens a resolved issue.");
        selectOption("view", "Default Screen");
        submit("Add");
        assertTextSequence(new String[] { "View Workflow Steps", "Workflow3" });

        assertTableRowEquals(getWebTableWithID("steps_table"), 2,
                new Object[] { new AndCell(new XsrfLinkCell("ViewWorkflowStep.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "Resolved"), new TextCell("(2)")),
                               new TextCell("Resolved"),                                                         
                               new AndCell(new XsrfLinkCell("ViewWorkflowTransition.jspa?workflowMode=live&workflowName=Workflow3&workflowTransition=11&workflowStep=2", "Re-open"), new TextCell("(11)", "Open")),
                               new AndCell(new XsrfLinkCell("AddWorkflowTransition!default.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "Add Transition"),
                                       new XsrfLinkCell("DeleteWorkflowTransitions!default.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "Delete Transitions"),
                                       new XsrfLinkCell("EditWorkflowStep!default.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "Edit"),
                                       new XsrfLinkCell("ViewWorkflowStepMetaAttributes.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "View Properties"),
                                       new XsrfLinkCell("DeleteWorkflowStep!default.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "Delete Step"))
                });

        //edit the step.
        clickLink("edit_step_2");
        assertTextPresent("Update Workflow Step");
        assertTextPresent("This page allows you to update the <b>Resolved</b> step.");
        setFormElement("stepName", "Actually Closed");
        selectOption("stepStatus", "Closed");
        submit("Update");

        assertTableRowEquals(getWebTableWithID("steps_table"), 2,
                new Object[] { new AndCell(new XsrfLinkCell("ViewWorkflowStep.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "Actually Closed"), new TextCell("(2)")),
                               new TextCell("Closed"),
                               new AndCell(new XsrfLinkCell("ViewWorkflowTransition.jspa?workflowMode=live&workflowName=Workflow3&workflowTransition=11&workflowStep=2", "Re-open"), new TextCell("(11)", "Open")),
                               new AndCell(new XsrfLinkCell("AddWorkflowTransition!default.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "Add Transition"),
                                       new XsrfLinkCell("DeleteWorkflowTransitions!default.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "Delete Transitions"),
                                       new XsrfLinkCell("EditWorkflowStep!default.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "Edit"),
                                       new XsrfLinkCell("ViewWorkflowStepMetaAttributes.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "View Properties"),
                                       new XsrfLinkCell("DeleteWorkflowStep!default.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "Delete Step"))
                });

        //Add another transition
        clickLink("add_trans_2");
        assertTextPresent("Add Workflow Transition");
        assertTextPresent("Create a transition from <b>Actually Closed</b> to another step.");
        setFormElement("transitionName", "Closed it again");
        setFormElement("description", "");
        selectOption("destinationStep", "Actually Closed");
        submit("Add");

        assertTableRowEquals(getWebTableWithID("steps_table"), 2,
                new Object[] { new AndCell(new XsrfLinkCell("ViewWorkflowStep.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "Actually Closed"), new TextCell("(2)")),
                               new TextCell("Closed"),
                               new AndCell(
                                       new XsrfLinkCell("ViewWorkflowTransition.jspa?workflowMode=live&workflowName=Workflow3&workflowTransition=11&workflowStep=2", "Re-open"), new TextCell("(11)", "Open"),
                                       new XsrfLinkCell("ViewWorkflowTransition.jspa?workflowMode=live&workflowName=Workflow3&workflowTransition=21&workflowStep=2", "Closed it again"), new TextCell("21", "Actually Closed")),
                               new AndCell(
                                       new XsrfLinkCell("AddWorkflowTransition!default.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "Add Transition"),
                                       new XsrfLinkCell("DeleteWorkflowTransitions!default.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "Delete Transitions"),
                                       new XsrfLinkCell("EditWorkflowStep!default.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "Edit"),
                                       new XsrfLinkCell("ViewWorkflowStepMetaAttributes.jspa?workflowMode=live&workflowName=Workflow3&workflowStep=2", "View Properties"))
                });

        //Now lets delete a transition
        clickLink("del_trans_2");
        assertTextPresent("Delete Workflow Transitions");
        assertTextPresent("Please select transitions to delete from the <b>Actually Closed</b> step.");
        selectOption("transitionIds", "Closed it again");
        submit("Delete");

        assertTextSequence(new String[] { "View Workflow Steps", "Workflow3" });
        assertTextNotPresent("Closed it again");
    }

    public void testStepProperties()
    {
        gotoWorkFlow();

        clickLink("steps_live_Workflow2");
        clickLinkWithText("View Properties");
        assertTextPresent("View Workflow Step Properties: Open");
        assertLinkPresentWithText("workflow steps");
        assertTextPresent("There are currently no defined properties.");
        assertTextPresent("Add New Property");

        //add without key
        submit("Add");
        assertTextPresent("Attribute key must be set.");

        //add without value
        setFormElement("attributeKey", "test.key");
        setFormElement("attributeValue", "");
        submit("Add");

        assertTextNotPresent("There are currently no defined properties.");

        assertEquals(2, getWebTableWithID("metas_table").getRowCount());
        tableAssertions.assertTableRowEquals(getWebTableWithID("metas_table"), 0,
                new Object[] { new TextCell("Property Key"),
                               new TextCell("Property Value"),
                               new TextCell("Operations")
                });
        tableAssertions.assertTableRowEquals(getWebTableWithID("metas_table"), 1,
                new Object[] { new TextCell("test.key"),
                               new StrictTextCell(""),
                               new XsrfLinkCell("RemoveWorkflowStepMetaAttribute.jspa?workflowName=Workflow2&workflowMode=live&workflowStep=1&attributeKey=test.key", "Delete")
                });

        //add without value
        setFormElement("attributeKey", "another.key");
        setFormElement("attributeValue", "This is a value.");
        submit("Add");

        assertTextNotPresent("There are currently no defined properties.");

        assertEquals(3, getWebTableWithID("metas_table").getRowCount());
        assertTableHasMatchingRow(getWebTableWithID("metas_table"),
                new Object[] { new TextCell("another.key"),
                               new StrictTextCell("This is a value."),
                               new XsrfLinkCell("RemoveWorkflowStepMetaAttribute.jspa?workflowName=Workflow2&workflowMode=live&workflowStep=1&attributeKey=another.key", "Delete")
                });

        //try adding a duplicate.
        setFormElement("attributeKey", "another.key");
        submit("Add");
        assertions.getJiraFormAssertions().assertFieldErrMsg("Attribute key 'another.key' already exists.");

        //Delete
        clickLink("del_meta_another.key");
        assertEquals(2, getWebTableWithID("metas_table").getRowCount());
        assertTextNotPresent("another.key");
        assertTextNotPresent("This is a value.");
    }

    public void testEditSingleStepAndTransition()
    {
        gotoWorkFlow();

        clickLink("steps_live_Workflow2");
        //add another step.
        setFormElement("stepName", "Resolved");
        selectOption("stepStatus", "Resolved");
        submit("Add");

        navigation.clickLinkWithExactText("Open");
        assertTextSequence(new String[] { "View Workflow Step", "Open" });
        assertTextPresent("This page shows the details of the <b>Open</b> step.");
        assertTextSequence(new String[] { "The step is linked to status:", "Open" });
        assertTextPresent("Workflow Browser");

        //assert the WorkflowBrowser is correct.
        assertTableRowEquals(getWebTableWithID("inbound_trans"), 0,
                new Object[] { new AndCell(new XsrfLinkCell("ViewWorkflowTransition.jspa?workflowMode=live&workflowName=Workflow2&workflowTransition=1", "Create"), new TextCell("(1)")) });
        assertTableCellHasText("workflow_browser", 0, 1, "Open");
        assertTableRowEquals(getWebTableWithID("outgoing_trans"), 0,
                new Object[] { null,
                               new TextCell("No Transitions") });

        //lets add an outgoing transition
        clickLinkWithText("Add");
        setFormElement("transitionName", "Resolve");
        selectOption("destinationStep", "Resolved");
        submit("Add");
        assertTextSequence(new String[] { "View Workflow Step", "Open" });

        //assert the WorkflowBrowser is correct.
        assertTableRowEquals(getWebTableWithID("inbound_trans"), 0,
                new Object[] { new AndCell(new XsrfLinkCell("ViewWorkflowTransition.jspa?workflowMode=live&workflowName=Workflow2&workflowTransition=1", "Create"), new TextCell("(1)")) });
        assertTableCellHasText("workflow_browser", 0, 1, "Open");
        assertTableRowEquals(getWebTableWithID("outgoing_trans"), 0,
                new Object[] { null,
                               new AndCell(new XsrfLinkCell("ViewWorkflowTransition.jspa?workflowMode=live&workflowName=Workflow2&workflowTransition=11&workflowStep=1", "Resolve"), new TextCell("(11)")) });

        //lets add another outgoing transition
        clickLinkWithText("Add");
        setFormElement("transitionName", "resolveitsomemore");
        selectOption("destinationStep", "Resolved");
        submit("Add");

        //assert the WorkflowBrowser is correct.
        assertTableRowEquals(getWebTableWithID("inbound_trans"), 0,
                new Object[] {
                        new AndCell(
                                new XsrfLinkCell("ViewWorkflowTransition.jspa?workflowMode=live&workflowName=Workflow2&workflowTransition=1", "Create"), new TextCell("(1)")) });
        assertTableCellHasText("workflow_browser", 0, 1, "Open");
        assertTableRowEquals(getWebTableWithID("outgoing_trans"), 0,
                new Object[] { null,
                               new AndCell(
                                       new XsrfLinkCell("ViewWorkflowTransition.jspa?workflowMode=live&workflowName=Workflow2&workflowTransition=11&workflowStep=1", "Resolve"), new TextCell("(11)")) });
        assertTableRowEquals(getWebTableWithID("outgoing_trans"), 1,
                new Object[] { null,
                               new AndCell(new XsrfLinkCell("ViewWorkflowTransition.jspa?workflowMode=live&workflowName=Workflow2&workflowTransition=21&workflowStep=1", "resolveitsomemore"), new TextCell("(21)")) });

        //add an incoming transition
        clickLinkWithText("Add");
        setFormElement("transitionName", "opensomemore");
        submit("Add");

        //assert the WorkflowBrowser is correct.
        assertTableRowEquals(getWebTableWithID("inbound_trans"), 0,
                new Object[] { new AndCell(new XsrfLinkCell("ViewWorkflowTransition.jspa?workflowMode=live&workflowName=Workflow2&workflowTransition=1", "Create"), new TextCell("(1)")) });
        assertTableRowEquals(getWebTableWithID("inbound_trans"), 1,
                new Object[] { new AndCell(new XsrfLinkCell("ViewWorkflowTransition.jspa?workflowMode=live&workflowName=Workflow2&workflowTransition=31&workflowStep=1", "opensomemore"), new TextCell("(31)")) });
        assertTableCellHasText("workflow_browser", 0, 1, "Open");
        assertTableRowEquals(getWebTableWithID("outgoing_trans"), 0,
                new Object[] { null,
                               new AndCell(
                                       new XsrfLinkCell("ViewWorkflowTransition.jspa?workflowMode=live&workflowName=Workflow2&workflowTransition=11&workflowStep=1", "Resolve"), new TextCell("(11)")) });
        assertTableRowEquals(getWebTableWithID("outgoing_trans"), 1,
                new Object[] { null,
                               new AndCell(new XsrfLinkCell("ViewWorkflowTransition.jspa?workflowMode=live&workflowName=Workflow2&workflowTransition=21&workflowStep=1", "resolveitsomemore"), new TextCell("(21)")) });
        assertTableRowEquals(getWebTableWithID("outgoing_trans"), 2,
                new Object[] { null,
                               new AndCell(new XsrfLinkCell("ViewWorkflowTransition.jspa?workflowMode=live&workflowName=Workflow2&workflowTransition=31&workflowStep=1", "opensomemore"), new TextCell("(31)")) });

        //lets view a transition
        clickLinkWithText("resolveitsomemore");
        assertTextPresent("Transition: resolveitsomemore");
        assertTextSequence(new String[] { "Transition View", "None", "it will happen instantly" });
        assertTextPresent("Workflow Browser");

        //assert the WorkflowBrowser is correct.
        assertTableRowEquals(getWebTableWithID("orig_steps"), 0,
                new Object[] { new XsrfLinkCell("ViewWorkflowStep.jspa?workflowMode=live&workflowName=Workflow2&workflowStep=1", "Open") });
        assertTableCellHasText("workflow_browser", 0, 1, "resolveitsomemore");
        assertTableCellHasText("workflow_browser", 0, 1, "(21)");
        assertTableRowEquals(getWebTableWithID("dest_steps"), 0,
                new Object[] { null,
                               new XsrfLinkCell("ViewWorkflowStep.jspa?workflowMode=live&workflowName=Workflow2&workflowStep=2", "Resolved") });

        //edit the transition
        clickLink("edit_transition");
        setFormElement("transitionName", "resolveitalittleless");
        submit("Update");

        //assert the WorkflowBrowser is correct.
        assertTableRowEquals(getWebTableWithID("orig_steps"), 0,
                new Object[] { new XsrfLinkCell("ViewWorkflowStep.jspa?workflowMode=live&workflowName=Workflow2&workflowStep=1", "Open") });
        assertTableCellHasText("workflow_browser", 0, 1, "resolveitalittleless");
        assertTableCellHasText("workflow_browser", 0, 1, "(21)");
        assertTableRowEquals(getWebTableWithID("dest_steps"), 0,
                new Object[] { null,
                               new XsrfLinkCell("ViewWorkflowStep.jspa?workflowMode=live&workflowName=Workflow2&workflowStep=2", "Resolved") });

        //lets look at a 'loop-back transition'
        navigation.clickLinkWithExactText("Open");
        clickLinkWithText("opensomemore");
        //assert the WorkflowBrowser is correct.
        assertTableRowEquals(getWebTableWithID("orig_steps"), 0,
                new Object[] { new XsrfLinkCell("ViewWorkflowStep.jspa?workflowMode=live&workflowName=Workflow2&workflowStep=1", "Open") });
        assertTableCellHasText("workflow_browser", 0, 1, "opensomemore");
        assertTableCellHasText("workflow_browser", 0, 1, "(31)");
        assertTableRowEquals(getWebTableWithID("dest_steps"), 0,
                new Object[] { null,
                               new XsrfLinkCell("ViewWorkflowStep.jspa?workflowMode=live&workflowName=Workflow2&workflowStep=1", "Open") });

        //finally try to delete the transition
        clickLinkWithText("Delete");
        assertTextPresent("Delete Workflow Transitions");
        assertTextSequence(new String[] { "Confirm that you want to delete", "opensomemore", "transition(s)." });
        submit("Delete");
        //we should come back to the view workflows table.
        assertTextSequence(new String[] { "View Workflow Steps", "Workflow2" });

        //just to be safe, make sure the transitions we added do/don't show up here.
        assertTextNotPresent("opensomemore");
        assertLinkPresentWithText("Resolve");
        assertLinkPresentWithText("resolveitalittleless");
    }

    public void testValidatorsConditionsAndPostFunctions()
    {
        gotoWorkFlow();

        //lets add a transition
        clickLink("steps_live_Workflow2");
        clickLink("add_trans_1");
        setFormElement("transitionName", "testtransition");
        submit("Add");
        clickLinkWithText("testtransition");

        assertTextPresent("Transition: testtransition");

        assertLinkPresentWithText("Validators");
        assertLinkPresentWithText("Post Functions");
        assertLinkPresentWithText("All");
        assertLinkPresentWithText("Add");

        //lets add a condition.
        clickLinkWithText("Add");
        assertTextPresent("Add Condition To Transition");

        assertTableHasMatchingRow(getWebTableWithID("descriptors_table"), new Object[] { null, new TextCell("Only Assignee Condition"), new TextCell("Condition to allow only the assignee to execute a transition.") });
        assertTableHasMatchingRow(getWebTableWithID("descriptors_table"), new Object[] { null, new TextCell("Only Reporter Condition"), new TextCell("Condition to allow only the reporter to execute a transition.") });
        assertTableHasMatchingRow(getWebTableWithID("descriptors_table"), new Object[] { null, new TextCell("Permission Condition"), new TextCell("Condition to allow only users with a certain permission to execute a transition.") });
        assertTableHasMatchingRow(getWebTableWithID("descriptors_table"), new Object[] { null, new TextCell("Sub-Task Blocking Condition"), new TextCell("Condition to block parent issue transition depending on sub-task status.") });
        assertTableHasMatchingRow(getWebTableWithID("descriptors_table"), new Object[] { null, new TextCell("User Is In Group"), new TextCell("Condition to allow only users in a given group to execute a transition.") });
        assertTableHasMatchingRow(getWebTableWithID("descriptors_table"), new Object[] { null, new TextCell("User Is In Group Custom Field"), new TextCell("Condition to allow only users in a custom field-specified group to execute a transition.") });
        assertTableHasMatchingRow(getWebTableWithID("descriptors_table"), new Object[] { null, new TextCell("User Is In Project Role"), new TextCell("Condition to allow only users in a given project role to execute a transition.") });

        //lets add a condition
        checkCheckbox("type", "com.atlassian.jira.plugin.system.workflow:onlyassignee-condition");
        submit("Add");
        assertTextPresent("Only the <b>assignee</b> of the issue can execute this transition.");
        clickLinkWithText("Add");
        checkCheckbox("type", "com.atlassian.jira.plugin.system.workflow:isuserinprojectrole-condition");
        submit("Add");
        assertTextPresent("Add Parameters To Condition");
        assertTextPresent("Add required parameters to the Condition.");
        selectOption("jira.projectrole.id", "Developers");
        submit("Add");
        assertTextPresent("Only users in project role <b>Developers</b> can execute this transition.");

        //try the AND OR toggle.
        assertTextSequence(new String[] { "Only the <b>assignee</b> of the issue can execute this transition.",
                                          "AND", "Switch to OR",
                                          "Only users in project role <b>Developers</b> can execute this transition." });
        clickLinkWithText("Switch to OR");

        assertTextSequence(new String[] { "Only the <b>assignee</b> of the issue can execute this transition.",
                                          "OR", "Switch to AND",
                                          "Only users in project role <b>Developers</b> can execute this transition." });

        //test the grouping functionality.  This isn't a great test, but better than nothing
        clickLinkWithText("Add grouped condition");
        checkCheckbox("type", "com.atlassian.jira.plugin.system.workflow:onlyassignee-condition");
        submit("Add");

        assertTextSequence(new String[] { "Only the <b>assignee</b> of the issue can execute this transition.",
                                          "AND", "Switch to OR",
                                          "Only the <b>assignee</b> of the issue can execute this transition.",
                                          "OR", "Switch to AND",
                                          "Only users in project role <b>Developers</b> can execute this transition." });

        clickLink("view_all_trans");
        //lets take a look at the 'ALL' view
        assertTextSequence(new String[] {
                "Conditions",
                "Validators",
                "Post Functions"
        });

        assertTextSequence(new String[] {
                "Conditions",
                "Add", "a new condition to restrict when this transition can be performed.",
                "Only the <b>assignee</b> of the issue can execute this transition.",
                "Add grouped condition", "Delete",
                "AND", "Add condition to group", "Switch to OR",
                "Only the <b>assignee</b> of the issue can execute this transition.",
                "Add grouped condition", "Delete",
                "OR", "Add condition to group", "Switch to AND",
                "Only users in project role <b>Developers</b> can execute this transition.",
                "Add grouped condition", "Edit", "Delete" });

        assertTextSequence(new String[] {
                "Validators",
                "Add", "a new validator to check the input parameters before this transition is executed.",
                "No input parameters checks will be done before this transition is executed." });

        assertTextSequence(new String[] {
                "Post Functions",
                "Add", "a new post function to the unconditional result of the transition.",
                "Set issue status to the linked status of the destination workflow step.",
                "THEN",
                "Add a comment to an issue if one is entered during a transition.",
                "THEN",
                "Update change history for an issue and store the issue in the database.",
                "THEN",
                "Re-index an issue to keep indexes in sync with the database.",
                "THEN",
                "Fire a", "Generic Event", "event that can be processed by the listeners.",
                "Edit"
        });

        //lets add some validators
        clickLinkWithText("Validators");
        clickLinkWithText("Add");

        assertTextPresent("Add Validator To Transition");
        assertTableHasMatchingRow(getWebTableWithID("descriptors_table"), new Object[] { null, new TextCell("Permission Validator"), new TextCell("Validates that the user has a permission.") });
        assertTableHasMatchingRow(getWebTableWithID("descriptors_table"), new Object[] { null, new TextCell("User Permission Validator"), new TextCell("Validates that the user has a permission, where the OSWorkflow variable holding the username is configurable. Obsolete.") });

        checkCheckbox("type", "com.atlassian.jira.plugin.system.workflow:permission-validator");
        submit("Add");
        assertTextPresent("Add Parameters To Validator");
        assertTextPresent("Add required parameters to the Validator.");
        selectOption("permission", "Assignable User");
        submit("Add");

        assertTextSequence(new String[] {
                "Add", "a new validator to check the input parameters before this transition is executed.",
                "Only users with <b>Assignable User</b> permission can execute this transition.",
                "Edit", "Delete" });

        // Finally lets try some post functions
        clickLinkWithText("Post Functions");
        clickLinkWithText("Add");
        assertTextPresent("Add Post Function To Transition");
        assertTableHasMatchingRow(getWebTableWithID("descriptors_table"), new Object[] { null, new TextCell("Assign to Current User"), new TextCell("Assigns the issue to the current user if the current user has the 'Assignable User' permission.") });
        assertTableHasMatchingRow(getWebTableWithID("descriptors_table"), new Object[] { null, new TextCell("Assign to Lead Developer"), new TextCell("Assigns the issue to the project/component lead developer") });
        assertTableHasMatchingRow(getWebTableWithID("descriptors_table"), new Object[] { null, new TextCell("Assign to Reporter"), new TextCell("Assigns the issue to the reporter") });
        assertTableHasMatchingRow(getWebTableWithID("descriptors_table"), new Object[] { null, new TextCell("Update Issue Field"), new TextCell("Updates a simple issue field to a given value.") });

        checkCheckbox("type", "com.atlassian.jira.plugin.system.workflow:assigntocurrentuser-function");
        submit("Add");

        assertTextSequence(new String[] {
                "Add", "a new post function to the unconditional result of the transition.",
                "Set issue status to the linked status of the destination workflow step.",
                "THEN",
                "Assign the issue to the current user. Please note that the issue will only be assigned to the current user if the current user has the 'Assignable User' permission.",
                "Move Up", "Move Down", "Delete",
                "THEN",
                "Add a comment to an issue if one is entered during a transition.",
                "THEN",
                "Update change history for an issue and store the issue in the database.",
                "THEN",
                "Re-index an issue to keep indexes in sync with the database.",
                "THEN",
                "Fire a", "Generic Event", "event that can be processed by the listeners.",
                "Edit"
        });

        //lets move it to the top
        clickLinkWithText("Move Up");
        assertLinkWithTextNotPresent("Move Up");
        assertTextSequence(new String[] {
                "Add", "a new post function to the unconditional result of the transition.",
                "Assign the issue to the current user. Please note that the issue will only be assigned to the current user if the current user has the 'Assignable User' permission.",
                "Move Down", "Delete",
                "THEN",
                "Set issue status to the linked status of the destination workflow step.",
                "THEN",
                "Add a comment to an issue if one is entered during a transition.",
                "THEN",
                "Update change history for an issue and store the issue in the database.",
                "THEN",
                "Re-index an issue to keep indexes in sync with the database.",
                "THEN",
                "Fire a", "Generic Event", "event that can be processed by the listeners.",
                "Edit"
        });

        //lets move it to the bottom
        while (tester.getDialog().isLinkPresentWithText("Move Down"))
        {
            clickLinkWithText("Move Down");
        }

        assertLinkNotPresentWithText("Move Down");
        assertTextSequence(new String[] {
                "Add", "a new post function to the unconditional result of the transition.",
                "Set issue status to the linked status of the destination workflow step.",
                "THEN",
                "Add a comment to an issue if one is entered during a transition.",
                "THEN",
                "Update change history for an issue and store the issue in the database.",
                "THEN",
                "Re-index an issue to keep indexes in sync with the database.",
                "THEN",
                "Fire a", "Generic Event", "event that can be processed by the listeners.",
                "Edit",
                "THEN",
                "Assign the issue to the current user. Please note that the issue will only be assigned to the current user if the current user has the 'Assignable User' permission.",
                "Move Up", "Delete"
        });
    }

    private class XsrfLinkCell extends com.atlassian.jira.webtests.table.LinkCell
    {
        private XsrfLinkCell(final String url, final String label)
        {
            super(page.addXsrfToken(url), label);
        }
    }

}