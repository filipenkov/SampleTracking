package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Issue;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.IssueClient;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.IssueLink;

import java.util.List;

/**
 * Func tests for parent/subtask linking in REST API.
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestIssueResourceSubtasks extends RestFuncTest
{
    private IssueClient issueClient;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        issueClient = new IssueClient(getEnvironmentData());
        administration.restoreData("TestIssueResourceSubtasks.xml");
    }

    /**
     * Verifies that the link to a subtask has all the required information.
     *
     * @throws Exception if anything goes wrong
     */
    public void testSubtaskLink() throws Exception
    {
        Issue issue = issueClient.get("HSP-1");

        // make sure the subtask info is correct
        List<IssueLink> subtasks = issue.fields.subtasks.value;
        assertEquals(1, subtasks.size());

        final String baseUrl = getEnvironmentData().getBaseUrl().toExternalForm();

        IssueLink lnkHsp2 = subtasks.get(0);
        assertEquals("HSP-2", lnkHsp2.issueKey);
        assertEquals(baseUrl + "/rest/api/2.0.alpha1/issue/HSP-2", lnkHsp2.issue);
        assertEquals("Sub-Task", lnkHsp2.type.name);
    }

    /**
     * Verifies that the link to the parent has all the required information.
     *
     * @throws Exception if anything goes wrong
     */
    public void testParentLink() throws Exception
    {
        Issue issue = issueClient.get("HSP-2");

        final String baseUrl = getEnvironmentData().getBaseUrl().toExternalForm();

        IssueLink parent = issue.fields.parent.value;
        assertEquals("HSP-1", parent.issueKey);
        assertEquals(baseUrl + "/rest/api/2.0.alpha1/issue/HSP-1", parent.issue);
        assertEquals("Parent", parent.type.name);
    }
}
