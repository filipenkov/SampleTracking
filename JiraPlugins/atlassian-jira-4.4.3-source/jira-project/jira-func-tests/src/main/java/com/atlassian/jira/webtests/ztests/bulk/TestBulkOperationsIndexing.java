package com.atlassian.jira.webtests.ztests.bulk;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.JIRAWebTest;

/**
 *
 */
@WebTest ({ Category.FUNC_TEST, Category.BULK_OPERATIONS })
public class TestBulkOperationsIndexing extends JIRAWebTest
{
    public TestBulkOperationsIndexing(String name)
    {
        super(name);
    }


    public void setUp()
    {
        super.setUp();
        restoreData("TestBulkOperationsIndexing.xml");
    }

    public void testBulkEditIndexing()
    {
        findAndVerifyIssuesArePresent(false);

        //lets bulk edit and add a comment
        clickLink("bulkedit_all");
        checkCheckbox("bulkedit_10003", "on");
        checkCheckbox("bulkedit_10002", "on");
        checkCheckbox("bulkedit_10001", "on");
        checkCheckbox("bulkedit_10000", "on");
        submit("Next");
        checkCheckbox("operation", "bulk.edit.operation.name");
        submit("Next");
        checkCheckbox("actions", "comment");
        setFormElement("comment", "whatsminesay");
        submit("Next");
        submit("Confirm");

        //search for all issues with the comment we've just added.
        clickLink("find_link");
        clickLink("new_filter");
        checkCheckbox("body", "true");
        uncheckCheckbox("summary");
        uncheckCheckbox("description");
        setFormElement("query", "whatsminesay");
        submit("show");

        assertTextPresent("HSP-1");
        assertTextPresent("HSP-2");
        assertTextPresent("Test issue 1");
        assertTextPresent("Test Issue 2");
        assertTextPresent("HSP-3");
        assertTextPresent("HSP-4");
        assertTextPresent("Subtask 1");
        assertTextPresent("Subtask 2");
    }

    public void testBulkMoveIndexing()
    {
        findAndVerifyIssuesArePresent(true);

        //lets bulk move, this set includes an issue that is already in the project we are moving to
        clickLink("bulkedit_all");
        checkCheckbox("bulkedit_10003", "on");
        checkCheckbox("bulkedit_10002", "on");
        checkCheckbox("bulkedit_10001", "on");
        checkCheckbox("bulkedit_10000", "on");
        checkCheckbox("bulkedit_10010", "on");
        submit("Next");
        checkCheckbox("operation", "bulk.move.operation.name");
        submit("Next");
        selectOption("10010_1_pid", "monkey");
        submit("Next");
        submit("Next");
        submit("Next");
        submit("Next");
        submit("Next");

        //search for all issues with the comment we've just added.
        clickLink("find_link");
        clickLink("new_filter");

        selectOption("pid", "monkey");
        submit("show");

        assertTextPresent("MKY-1");
        assertTextPresent("MKY-2");
        assertTextPresent("MKY-3");
        assertTextPresent("Test issue 1");
        assertTextPresent("Test Issue 2");
        assertTextPresent("MKY-4");
        assertTextPresent("MKY-5");
        assertTextPresent("Subtask 1");
        assertTextPresent("Subtask 2");
        assertTextPresent("Test Monkey Issue 1");

        // Verify there are no issues left in the old project
        clickLink("find_link");
        clickLink("new_filter");

        selectOption("pid", "homosapien");
        submit("show");
        assertTextPresent("No matching issues found.");
    }

    public void testBulkWorkflowTransitionIndexing()
    {
        findAndVerifyIssuesArePresent(true);

        //lets bulk move, this set includes an issue that is already in the project we are moving to
        clickLink("bulkedit_all");
        checkCheckbox("bulkedit_10003", "on");
        checkCheckbox("bulkedit_10002", "on");
        checkCheckbox("bulkedit_10001", "on");
        checkCheckbox("bulkedit_10000", "on");
        checkCheckbox("bulkedit_10010", "on");
        submit("Next");
        checkCheckbox("operation", "bulk.workflowtransition.operation.name");
        submit("Next");
        checkCheckbox("wftransition", "jira_5_5");
        submit("Next");
        selectOption("resolution", "Fixed");
        submit("Next");
        submit("Next");

        // Search to make sure they have all been put into the right status and resolution
        clickLink("find_link");
        clickLink("new_filter");
        selectOption("resolution", "Fixed");
        selectOption("status", "Resolved");
        submit("show");

        assertTextPresent("HSP-1");
        assertTextPresent("HSP-2");
        assertTextPresent("Test issue 1");
        assertTextPresent("Test Issue 2");
        assertTextPresent("HSP-3");
        assertTextPresent("HSP-4");
        assertTextPresent("Subtask 1");
        assertTextPresent("Subtask 2");
        assertTextPresent("MKY-1");
        assertTextPresent("Test Monkey Issue 1");
    }

    private void findAndVerifyIssuesArePresent(boolean all)
    {
        //Show all issues.
        clickLink("find_link");
        if (!all)
        {
            selectOption("pid", "homosapien");
        }
        submit("show");

        assertTextPresent("HSP-1");
        assertTextPresent("HSP-2");
        assertTextPresent("Test issue 1");
        assertTextPresent("Test Issue 2");
        assertTextPresent("HSP-3");
        assertTextPresent("HSP-4");
        assertTextPresent("Subtask 1");
        assertTextPresent("Subtask 2");
        if (all)
        {
            assertTextPresent("MKY-1");
            assertTextPresent("Test Monkey Issue 1");
        }
    }
}
