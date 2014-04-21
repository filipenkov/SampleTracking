package com.atlassian.jira.webtests.ztests.issue.clone;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

/**
 * Tests the Issue Linking that happens in a Clone operation.
 * See http://jira.atlassian.com/browse/JRA-17222
 */
@WebTest ({ Category.FUNC_TEST, Category.ISSUES })
public class TestCloneIssueLinking extends FuncTestCase
{
    protected void setUpTest()
    {
        administration.restoreData("TestCloneIssueLinking.xml");
    }

    public void testCloneNoLinksNoSubtasks() throws Exception
    {
        navigation.issue().viewIssue("RAT-1");
        // Clone the issue
        tester.clickLink("clone-issue");
        tester.setFormElement("summary", "CLONE - Library attempts HTTP communications with URLs that are in the Trackback Filter.");
        // Links is off by default
        tester.uncheckCheckbox("cloneSubTasks");
        tester.submit("Create");

        tester.assertTextPresent("RAT-4");
        // Shouldn't copy the links
        assertions.getViewIssueAssertions().assertOutwardLinkNotPresent("COW-16");
        assertions.getViewIssueAssertions().assertOutwardLinkNotPresent("RAT-3");
        // Shouldn't create subtask
        tester.assertTextNotPresent("Sub-Tasks");
        tester.assertTextNotPresent("Design Solution");
    }

    public void testCloneLinksNoSubtasks() throws Exception
    {
        navigation.issue().viewIssue("RAT-1");
        // Clone the issue
        tester.clickLink("clone-issue");
        tester.setFormElement("summary", "CLONE - Library attempts HTTP communications with URLs that are in the Trackback Filter.");
        tester.checkCheckbox("cloneLinks", "true");
        tester.uncheckCheckbox("cloneSubTasks");
        tester.submit("Create");

        tester.assertTextPresent("RAT-4");
        // Should copy the links
        assertions.getViewIssueAssertions().assertOutwardLinkPresent("COW-16");
        // Because we aren't cloning the subtasks, we link back to the original subtask.
        assertions.getViewIssueAssertions().assertOutwardLinkPresent("RAT-3");
        // Shouldn't create subtask
        tester.assertTextNotPresent("Sub-Tasks");
        tester.assertTextNotPresent("Design Solution");
    }

    public void testCloneNoLinksSubtasks() throws Exception
    {
        navigation.issue().viewIssue("RAT-1");
        // Clone the issue
        tester.clickLink("clone-issue");
        tester.setFormElement("summary", "CLONE - Library attempts HTTP communications with URLs that are in the Trackback Filter.");
        // Links is off by default
        // SubTasks is on by default
        tester.submit("Create");

        tester.assertTextPresent("RAT-4");
        // Shouldn't copy the link to COW-16
        assertions.getViewIssueAssertions().assertOutwardLinkNotPresent("COW-16");
        assertions.getViewIssueAssertions().assertOutwardLinkNotPresent("RAT-3");
        // Should create subtasks
        tester.assertTextPresent("Sub-Tasks");
        tester.assertTextPresent("Design Solution");
    }

    public void testCloneLinks() throws Exception
    {
        navigation.issue().viewIssue("RAT-1");
        // Clone the issue and include Links (and subtasks)
        tester.clickLink("clone-issue");
        tester.setFormElement("summary", "CLONE - Library attempts HTTP communications with URLs that are in the Trackback Filter.");
        tester.checkCheckbox("cloneLinks", "true");
        // SubTasks is on by default
        tester.submit("Create");

        // We have just cloned Rat-1 as Rat-4
        tester.assertTextPresent("RAT-4");
        // New Parent Issue should copy the original link
        assertions.getViewIssueAssertions().assertOutwardLinkPresent("COW-16");
        // Now we have cloned the subtask RAT-3, so we only want to make a link to the new subtask RAT-6
        assertions.getViewIssueAssertions().assertOutwardLinkNotPresent("RAT-3");
        assertions.getViewIssueAssertions().assertOutwardLinkPresent("RAT-6");

        // Check the first new subtask
        navigation.issue().viewIssue("RAT-5");
        tester.assertTextPresent("Design Solution");
        // New subtask should have a link to the other new subtask, and a link to an "external" issue, but no link to the other OLD subtask.
        assertions.getViewIssueAssertions().assertOutwardLinkPresent("RAT-6");
        assertions.getViewIssueAssertions().assertOutwardLinkNotPresent("RAT-3");
        assertions.getViewIssueAssertions().assertInwardLinkPresent("COW-18");

        // Now check the other subtask
        navigation.issue().viewIssue("RAT-6");
        tester.assertTextPresent("Create Estimate");
        // New subtask should have a link to the new parent and other new subtask, and a link to an "external" issue,
        // but no link to the OLD parent or other OLD subtask.
        assertions.getViewIssueAssertions().assertOutwardLinkPresent("COW-17");
        assertions.getViewIssueAssertions().assertInwardLinkNotPresent("RAT-1");
        assertions.getViewIssueAssertions().assertInwardLinkNotPresent("RAT-2");
        assertions.getViewIssueAssertions().assertInwardLinkPresent("RAT-4");
        assertions.getViewIssueAssertions().assertInwardLinkPresent("RAT-5");
    }
}
