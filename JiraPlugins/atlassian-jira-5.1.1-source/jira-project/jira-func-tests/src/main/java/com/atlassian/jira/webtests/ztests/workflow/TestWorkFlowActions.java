package com.atlassian.jira.webtests.ztests.workflow;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.Groups;
import com.atlassian.jira.webtests.JIRAWebTest;

@WebTest ({ Category.FUNC_TEST, Category.WORKFLOW })
public class TestWorkFlowActions extends JIRAWebTest
{
    public static final String issueKey = "HSP-1";
    private static final String DODGY_WORKFLOW_NAME = "'><script>altert('hello')</script>";

    public TestWorkFlowActions(String name)
    {
        super(name);
    }

    public void setUp()
    {
        super.setUp();
        restoreData("TestWorkflowActions.xml");
    }

    public void testCopyWorkflowWithXSS()
    {
        administration.workflows().goTo().addWorkflow(DODGY_WORKFLOW_NAME, "Some desc");

        administration.workflows().goTo();

        tester.clickLink("copy_" + DODGY_WORKFLOW_NAME);
        tester.assertTextPresent("&#39;&gt;&lt;script&gt;altert(&#39;hello&#39;)&lt;/script&gt;");
        tester.setFormElement("newWorkflowName", "Copy of " + DODGY_WORKFLOW_NAME);
        tester.submit("Update");

        tester.assertTextPresent("Copy of &#39;&gt;&lt;script&gt;altert(&#39;hello&#39;)&lt;/script&gt;");
    }

    public void testWorkFlowActions()
    {
        try
        {
            getBackdoor().darkFeatures().enableForSite("no.frother.assignee.field");
            assignIssue(issueKey);
        }
        finally
        {
            getBackdoor().darkFeatures().disableForSite("no.frother.assignee.field");
        }
        resolveIssue(issueKey);
        closeAndReopenIssue(issueKey);
        closeIssueFromOpen(issueKey);

        navigation.issue().deleteIssue(issueKey);
    }

    public void testInvalidWorkflowAction() throws Exception
    {
        navigation.issue().viewIssue(issueKey);
        tester.gotoPage("/secure/WorkflowUIDispatcher.jspa?id=10000&action=3&atl_token=" + page.getXsrfToken());
        tester.assertTextPresent("Workflow Action Invalid");

        // check the action name is present
        tester.assertTextPresent("Reopen Issue");

        tester.assertLinkPresent("refreshIssue");
    }

    public void testInvalidWorkflowActionDoesNotBreakThePage() throws Exception
    {
        administration.restoreData("TestWorkflowActionsDodgyWorkflow.xml");
        navigation.issue().viewIssue("TST-1");
        tester.assertTextPresent("Details");
        tester.assertLinkNotPresentWithText("Close Issue");
        tester.assertLinkNotPresentWithText("Resolve Issue");
    }

    //Test for JRA-18745
    public void testWorkflowWithReturnUrl()
    {
        //Try to resolve an issue.
        tester.gotoPage("/secure/WorkflowUIDispatcher.jspa?id=10000&action=5&atl_token=" + page.getXsrfToken() + "&returnUrl=%2Fsecure%2FIssueNavigator.jspa%3Freset%3Dtrue%26jqlQuery%3Dproject%2B%3D%2BHSP%26selectedIssueId%3D10000");
        tester.setWorkingForm("issue-workflow-transition");
        tester.submit("Transition");

        //Ensure that after resolving the issue we end up at the correct URL as given in the returnUrl parameter from the previous link.
        final String currentUrl = tester.getDialog().getResponse().getURL().toExternalForm();
        assertTrue("Did not get redirected to the issue navigator.", currentUrl.endsWith("/secure/IssueNavigator.jspa?reset=true&jqlQuery=project+=+HSP&selectedIssueId=10000"));
    }

    /** Put an issue through work flow */
    public void assignIssue(String issueKey)
    {
        administration.usersAndGroups().addUser(BOB_USERNAME, BOB_PASSWORD, BOB_FULLNAME, BOB_EMAIL);
        // Add user to the jira-developers group. so that he can be assigned issues
        try
        {
            administration.usersAndGroups().addUserToGroup(BOB_USERNAME, Groups.DEVELOPERS);
        }
        catch (Throwable t)
        {
            log(BOB_USERNAME + " is already part of " + Groups.DEVELOPERS);
        }

        navigation.issue().assignIssue(issueKey, "issue assigned", BOB_FULLNAME);

        // Remove user from group
        administration.usersAndGroups().removeUserFromGroup(BOB_USERNAME, Groups.DEVELOPERS);

        // Re-assign issue to user with 'ASsignable User' permission
        navigation.issue().assignIssue(issueKey, "issue assigned", ADMIN_FULLNAME);
    }

    public void resolveIssue(String issueKey)
    {
        assertIndexedFieldCorrect("//item", EasyMap.build("status", "Open", "resolution", "Unresolved", "key", issueKey), null, issueKey);
        progressAndResolve(issueKey, 5, "issue resolved");
        //check that resolving issue updates the index
        assertIndexedFieldCorrect("//item", EasyMap.build("status", "Resolved", "resolution", "Fixed", "key", issueKey), null, issueKey);
    }

    public void closeAndReopenIssue(String issueKey)
    {
        assertIndexedFieldCorrect("//item", EasyMap.build("status", "Resolved", "resolution", "Fixed", "key", issueKey, "version", "New Version 1"), null, issueKey);
        progressWorkflow(issueKey, 701, "issue closed");
        //check that the workflow action has updated the index
        assertIndexedFieldCorrect("//item", EasyMap.build("status", "Closed", "key", issueKey), null, issueKey);
        progressWorkflow(issueKey, 3, "issue reopened");
        assertIndexedFieldCorrect("//item", EasyMap.build("status", "Reopened", "key", issueKey), null, issueKey);
    }

    public void closeIssueFromOpen(String issueKey)
    {
        progressAndResolve(issueKey, 2, "issue resolved and closed");
        progressWorkflow(issueKey, 3, "issue reopened");
    }
}