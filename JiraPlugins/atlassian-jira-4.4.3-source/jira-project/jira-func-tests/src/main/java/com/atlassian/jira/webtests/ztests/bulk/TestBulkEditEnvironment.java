package com.atlassian.jira.webtests.ztests.bulk;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.table.HtmlTable;
import com.meterware.httpunit.WebTable;

/**
 * Tests Bulk Edit of the Environment Field.
 *  See JRA-17215.
 *
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.BULK_OPERATIONS })
public class TestBulkEditEnvironment extends FuncTestCase
{
    @Override
    protected void setUpTest()
    {
        administration.restoreData("TestBulkEditEnvironment.xml");
    }

    public void testHappyPath() throws Exception
    {
        // Check initial Environment values
        navigation.issue().viewIssue("RAT-1");
        assertions.getViewIssueAssertions().assertEnvironmentEquals("DOS 4, 80286");
        navigation.issue().viewIssue("RAT-2");
        assertions.getViewIssueAssertions().assertEnvironmentEquals("");
        navigation.issue().viewIssue("COW-18");
        assertions.getViewIssueAssertions().assertEnvironmentEquals("");

        navigation.issueNavigator().displayAllIssues();
        tester.assertTextPresent("Issue Navigator");
        tester.clickLinkWithText("Tools");
        // Click Link 'all 9 issue(s)' (id='bulkedit_all').
        tester.clickLink("bulkedit_all");
        tester.assertTextPresent("Bulk Operation Step 1 of 4: Choose Issues");
        tester.checkCheckbox("bulkedit_10000", "on");
        tester.checkCheckbox("bulkedit_10020", "on");
        tester.submit("Next");
        tester.assertTextPresent("Bulk Operation Step 2 of 4: Choose Operation");
        tester.checkCheckbox("operation", "bulk.edit.operation.name");
        tester.submit("Next");
        tester.assertTextPresent("Bulk Operation Step 3 of 4: Operation Details");
        // We should be able to Bulk Edit the Environment
        tester.assertTextPresent("Change Environment");
        tester.checkCheckbox("actions", "environment");
        tester.setFormElement("environment", "DOS 5, 80386");
        tester.submit("Next");
        tester.assertTextPresent("Bulk Operation Step 4 of 4: Confirmation");
        tester.assertTextPresent("Updated Fields");
        // Assert the table 'updatedfields'
        // Assert the cells in table 'updatedfields'.
        WebTable updatedfields = tester.getDialog().getWebTableBySummaryOrId("updatedfields");
        // Assert row 0: |Environment|DOS|
        assertEquals("Cell (0, 0) in table 'updatedfields' should be 'Environment'.", "Environment", updatedfields.getCellAsText(0, 0).trim());
        assertEquals("Cell (0, 1) in table 'updatedfields' should be 'DOS 5, 80386'.", "DOS 5, 80386", updatedfields.getCellAsText(0, 1).trim());
        // Confirm changes and finish the Bulk Edit.
        tester.submit("Confirm");

        // Check final Environment values
        navigation.issue().viewIssue("RAT-1");
        assertions.getViewIssueAssertions().assertEnvironmentEquals("DOS 5, 80386");
        navigation.issue().viewIssue("RAT-2");
        assertions.getViewIssueAssertions().assertEnvironmentEquals("");
        navigation.issue().viewIssue("COW-15");
        assertions.getViewIssueAssertions().assertEnvironmentEquals("DOS 5, 80386");
    }

    public void testHiddenInOneProject() throws Exception
    {
        // Hide Environment for COW project
        tester.gotoPage("/plugins/servlet/project-config/" + "COW" + "/fields");
        tester.clickLink("project-config-fields-scheme-change");

        // Select 'Small Field Configuration Scheme' from select box 'schemeId'.
        tester.selectOption("schemeId", "Small Field Configuration Scheme");
        tester.submit("Associate");

        // Now try to bulk edit Environment
        navigation.issueNavigator().displayAllIssues();
        tester.assertTextPresent("Issue Navigator");
        tester.clickLinkWithText("Tools");
        // Click Link 'all 9 issue(s)' (id='bulkedit_all').
        tester.clickLink("bulkedit_all");
        tester.assertTextPresent("Bulk Operation Step 1 of 4: Choose Issues");
        tester.checkCheckbox("bulkedit_10000", "on");
        tester.checkCheckbox("bulkedit_10020", "on");
        tester.submit("Next");
        tester.assertTextPresent("Bulk Operation Step 2 of 4: Choose Operation");
        tester.checkCheckbox("operation", "bulk.edit.operation.name");
        tester.submit("Next");
        tester.assertTextPresent("Bulk Operation Step 3 of 4: Operation Details");

        // We should NOT be able to Bulk Edit the Environment
        tester.assertTextInElement("unavailableActionsTable", "Change Environment");
    }

    public void testDifferentRenderers() throws Exception
    {
        // user Wiki renderer for Cow project
        tester.gotoPage("/plugins/servlet/project-config/" + "COW" + "/fields");
        tester.clickLink("project-config-fields-scheme-edit");

        // Click Link 'Big Field Configuration' (id='configure_fieldlayout').
        tester.clickLink("configure_fieldlayout");
        // Click Link 'Renderers' (id='renderer_environment').
        tester.clickLink("renderer_environment");
        // Select 'Wiki Style Renderer' from select box 'selectedRendererType'.
        tester.selectOption("selectedRendererType", "Wiki Style Renderer");
        tester.submit("Update");
        tester.assertTextPresent("Edit Field Renderer Confirmation: Environment");
        tester.submit("Update");
        
        // Now try to bulk edit Environment
        navigation.issueNavigator().displayAllIssues();
        tester.assertTextPresent("Issue Navigator");
        tester.clickLinkWithText("Tools");
        // Click Link 'all 9 issue(s)' (id='bulkedit_all').
        tester.clickLink("bulkedit_all");
        tester.assertTextPresent("Bulk Operation Step 1 of 4: Choose Issues");
        tester.checkCheckbox("bulkedit_10000", "on");
        tester.checkCheckbox("bulkedit_10020", "on");
        tester.submit("Next");
        tester.assertTextPresent("Bulk Operation Step 2 of 4: Choose Operation");
        tester.checkCheckbox("operation", "bulk.edit.operation.name");
        tester.submit("Next");
        tester.assertTextPresent("Bulk Operation Step 3 of 4: Operation Details");

        // We should NOT be able to Bulk Edit the Environment
        tester.assertTextInElement("unavailableActionsTable", "Change Environment");
    }

}
