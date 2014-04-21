package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.IssueType;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.IssueTypeClient;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Response;

/**
 * Func tests for IssueTypeResource.
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestIssueTypeResource extends RestFuncTest
{
    private static final String TASK_ISSUE_TYPE_ID = "3";
    private IssueTypeClient issueTypeClient;

    /**
     * Tests the case where the user can see all available issue types.
     *
     * @throws Exception if anything goes wrong
     */
    public void testIssueTypeVisible() throws Exception
    {
        IssueType issueType = issueTypeClient.get(TASK_ISSUE_TYPE_ID);

        // expected:
        //
        // {
        //   "self": "http://localhost:8090/jira/rest/api/2.0.alpha1/issueType/3",
        //   "description": "A task that needs to be done.",
        //   "iconUrl": "http://localhost:8090/jira/images/icons/task.gif",
        //   "name": "Task",
        //   "subtask": false
        // }
        assertEquals(getBaseUrl() + "/rest/api/2.0.alpha1/issueType/" + TASK_ISSUE_TYPE_ID, issueType.self);
        assertEquals("A task that needs to be done.", issueType.description);
        assertEquals(getBaseUrl() + "/images/icons/task.gif", issueType.iconUrl);
        assertEquals("Task", issueType.name);
        assertEquals(false, issueType.subtask);
    }

    /**
     * Tests the case where not all available types are visible by user.
     *
     * @throws Exception if anything goes wrong
     */
    public void testIssueTypeNotFound() throws Exception
    {
        // the issue is not visible
        Response response = issueTypeClient.loginAs(FRED_USERNAME).getResponse(TASK_ISSUE_TYPE_ID);
        assertTrue(response.entity.errorMessages.contains("The issue type with id '" + TASK_ISSUE_TYPE_ID + "' does not exist"));

        // the issue doesn't exist. should return 404 NOT FOUND
        Response responseZzz = issueTypeClient.loginAs(FRED_USERNAME).getResponse("zzz");
        assertEquals(404, responseZzz.statusCode);
        assertTrue(responseZzz.entity.errorMessages.contains("The issue type with id 'zzz' does not exist"));
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        issueTypeClient = new IssueTypeClient(getEnvironmentData());
        administration.restoreData("TestIssueTypeResource.xml");
    }
}
