package com.atlassian.jira.webtests.ztests.bulk;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.BULK_OPERATIONS })
public class TestBulkTransition extends FuncTestCase
{
    /**
     * Tests for regression of http://jira.atlassian.com/browse/JRA-18359
     */
    public void testBulkTransitionDuplicateWorkflows()
    {
        administration.restoreData("TestBulkTransitionDuplicateWorkflows.xml");
        // Click Link 'Issues' (id='find_link').
        tester.clickLink("find_link");
        tester.assertTextPresent("Issue Navigator");
        tester.submit("show");
        tester.assertTextPresent("Displaying issues <span class=\"results-count-start\">1</span> to 3 of <span class=\"results-count-link\"><strong class=\"results-count-total\">3</strong> matching issues.</span>");
        // Click Link 'all 3 issue(s)' (id='bulkedit_all').
        tester.clickLink("bulkedit_all");
        tester.checkCheckbox("bulkedit_10000", "on");
        tester.checkCheckbox("bulkedit_10001", "on");
        tester.checkCheckbox("bulkedit_10002", "on");
        tester.submit("Next");
        tester.checkCheckbox("operation", "bulk.workflowtransition.operation.name");
        tester.checkCheckbox("operation", "bulk.workflowtransition.operation.name");
        tester.checkCheckbox("operation", "bulk.workflowtransition.operation.name");
        tester.assertTextPresent("Bulk Operation Step 2 of 4: Choose Operation");
        tester.submit("Next");
        tester.assertTextPresent("Bulk Operation Step 3 of 4: Operation Details");
        tester.assertTextPresent("Select the workflow transition to execute on the associated issues.");
        // Assert that we don't have multiple copies of Workflow
        text.assertTextPresentNumOccurences("Workflow: jira", 1);
    }
}
