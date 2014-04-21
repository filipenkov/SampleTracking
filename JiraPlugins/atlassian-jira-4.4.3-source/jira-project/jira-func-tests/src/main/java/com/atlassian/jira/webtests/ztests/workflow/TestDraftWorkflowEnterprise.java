package com.atlassian.jira.webtests.ztests.workflow;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.meterware.httpunit.WebResponse;
import org.xml.sax.SAXException;

import java.util.Collections;

/**
 *
 * @since v3.13
 */@WebTest ({ Category.FUNC_TEST, Category.WORKFLOW })

public class TestDraftWorkflowEnterprise extends TestDraftWorkflow
{

    public TestDraftWorkflowEnterprise(String name)
    {
        super(name);
    }

    public void testCreateAndDeleteDraftWorkflow() throws SAXException
    {
        _testCreateAndDeleteDraftWorkflow(5);
    }

    public void testWorkflowCachingHeaders()
    {
        gotoWorkFlow();
        tester.clickLink("xml_jira");
        final WebResponse response = getDialog().getResponse();
        // JRA-17446: check that headers are compatible with IE over SSL
        assertEquals("", response.getHeaderField("Pragma"));
        final String cacheControl = response.getHeaderField("Cache-Control");
        assertTrue(cacheControl.indexOf("private") > -1);
        assertTrue(cacheControl.indexOf("must-revalidate") > -1);
        assertTrue(cacheControl.indexOf("max-age") > -1);
        assertResponseCanBeCached();
    }

    public void testInstructions()
    {
        gotoWorkFlow();
        assertTextPresent("All workflows have one of the following statuses");
        assertTextSequence(new String[] { "Active", "currently assigned to at least one scheme which is associated with one or more projects." });
        assertTextSequence(new String[] { "Draft", "a draft version of an active workflow, which can be published to apply all changes" });

        assertTextSequence(new String[] { "Inactive", "not assigned to any scheme, or assigned to schemes which are not associated with any projects." });
        assertTextPresent("To delete a workflow, you must first unassign it from any workflow schemes.");
    }

    public void testDeactivateDraftWorkflow() throws SAXException, InterruptedException
    {
        restoreData("TestDeactivateDraftWorkflowEnterprise.xml");

        gotoWorkFlow();
        //assert pre-conditions
        assertTableCellHasText("workflows_table", 2, 0, "Workflow1");
        assertTableCellHasText("workflows_table", 2, 2, "Active");
        assertTableCellHasText("workflows_table", 2, 3, "WorkflowScheme_Workflow1");
        assertTableCellHasText("workflows_table", 2, 3, "A second scheme");

        assertTableCellHasText("workflows_table", 3, 0, "Workflow1");
        assertTableCellHasText("workflows_table", 3, 2, "Draft");
        assertTableCellHasText("workflows_table", 3, 3, "WorkflowScheme_Workflow1");
        assertTableCellHasText("workflows_table", 3, 3, "A second scheme");

        assertTableCellHasText("workflows_table", 5, 0, "Workflow3");
        assertTableCellHasText("workflows_table", 5, 2, "Active");
        assertTableCellHasNotText("workflows_table", 5, 3, "WorkflowScheme_Workflow1");
        assertTableCellHasText("workflows_table", 5, 3, "A second scheme");

        assertTableCellHasText("workflows_table", 6, 0, "Workflow3");
        assertTableCellHasText("workflows_table", 6, 2, "Draft");
        assertTableCellHasNotText("workflows_table", 6, 3, "WorkflowScheme_Workflow1");
        assertTableCellHasText("workflows_table", 6, 3, "A second scheme");

        clickLink("steps_live_Workflow1");
        assertTextNotPresent("NewDraftStep");
        gotoWorkFlow();
        clickLink("steps_draft_Workflow1");
        assertTextPresent("NewDraftStep");
        gotoWorkFlow();
        clickLink("steps_live_Workflow3");
        assertTextNotPresent("AnotherNewDraftStep");
        gotoWorkFlow();
        clickLink("steps_draft_Workflow3");
        assertTextPresent("AnotherNewDraftStep");

        //now lets migrate project monkey to the default scheme.  This should cause one of the drafts to be copied and deleted
        associateWorkFlowSchemeToProject("monkey", "Default");

        //assert only one draft left and one copy present
        gotoWorkFlow();
        assertTableCellHasText("workflows_table", 2, 0, "Copy of Workflow3");
        assertTableCellHasText("workflows_table", 2, 1, "(This copy was automatically generated from a draft, when workflow 'Workflow3' was made inactive.)");
        assertTableCellHasText("workflows_table", 2, 2, "Inactive");
        assertTableCellHasNotText("workflows_table", 2, 3, "WorkflowScheme_Workflow1");
        assertTableCellHasNotText("workflows_table", 2, 3, "A second scheme");
        clickLink("steps_live_Copy of Workflow3");
        assertTextPresent("AnotherNewDraftStep");
        gotoWorkFlow();
        clickLink("steps_live_Workflow3");
        assertTextNotPresent("AnotherNewDraftStep");
        gotoWorkFlow();

        assertTableCellHasText("workflows_table", 3, 0, "Workflow1");
        assertTableCellHasText("workflows_table", 3, 2, "Active");
        assertTableCellHasText("workflows_table", 3, 3, "WorkflowScheme_Workflow1");
        assertTableCellHasText("workflows_table", 3, 3, "A second scheme");

        assertTableCellHasText("workflows_table", 4, 0, "Workflow1");
        assertTableCellHasText("workflows_table", 4, 2, "Draft");
        assertTableCellHasText("workflows_table", 4, 3, "WorkflowScheme_Workflow1");
        assertTableCellHasText("workflows_table", 4, 3, "A second scheme");

        assertTableCellHasText("workflows_table", 6, 0, "Workflow3");
        assertTableCellHasText("workflows_table", 6, 2, "Inactive");
        assertTableCellHasNotText("workflows_table", 6, 3, "WorkflowScheme_Workflow1");
        assertTableCellHasText("workflows_table", 6, 3, "A second scheme");

        // now lets migrate project homo to the default scheme.  This should cause the remaining draft to be copied and deleted.
        // Try to migrate the project again
        associateWorkFlowSchemeToProject("homosapien", "Default", Collections.EMPTY_MAP);

        // Ensure that the project has been associated with the Destination Workflow scheme
        waitForSuccessfulWorkflowSchemeMigration("homosapien", "Default");

        //assert no drafts left, and two copies present.
        gotoWorkFlow();
        assertTableCellHasText("workflows_table", 2, 0, "Copy of Workflow1");
        assertTableCellHasText("workflows_table", 2, 1, "(This copy was automatically generated from a draft, when workflow 'Workflow1' was made inactive.)");
        assertTableCellHasText("workflows_table", 2, 2, "Inactive");
        assertTableCellHasNotText("workflows_table", 2, 3, "WorkflowScheme_Workflow1");
        assertTableCellHasNotText("workflows_table", 2, 3, "A second scheme");

        assertTableCellHasText("workflows_table", 3, 0, "Copy of Workflow3");
        assertTableCellHasText("workflows_table", 3, 1, "(This copy was automatically generated from a draft, when workflow 'Workflow3' was made inactive.)");
        assertTableCellHasText("workflows_table", 3, 2, "Inactive");
        assertTableCellHasNotText("workflows_table", 3, 3, "WorkflowScheme_Workflow1");
        assertTableCellHasNotText("workflows_table", 3, 3, "A second scheme");

        clickLink("steps_live_Copy of Workflow3");
        assertTextPresent("AnotherNewDraftStep");
        gotoWorkFlow();
        clickLink("steps_live_Workflow3");
        assertTextNotPresent("AnotherNewDraftStep");
        gotoWorkFlow();
        clickLink("steps_live_Copy of Workflow1");
        assertTextPresent("NewDraftStep");
        gotoWorkFlow();
        clickLink("steps_live_Workflow1");
        assertTextNotPresent("NewDraftStep");
        gotoWorkFlow();

        assertTableCellHasText("workflows_table", 4, 0, "Workflow1");
        assertTableCellHasText("workflows_table", 4, 2, "Inactive");
        assertTableCellHasText("workflows_table", 4, 3, "WorkflowScheme_Workflow1");
        assertTableCellHasText("workflows_table", 4, 3, "A second scheme");

        assertTableCellHasText("workflows_table", 6, 0, "Workflow3");
        assertTableCellHasText("workflows_table", 6, 2, "Inactive");
        assertTableCellHasNotText("workflows_table", 6, 3, "WorkflowScheme_Workflow1");
        assertTableCellHasText("workflows_table", 6, 3, "A second scheme");
    }
}
