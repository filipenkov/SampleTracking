package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Issue;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.IssueClient;
import com.meterware.httpunit.WebResponse;

import java.io.IOException;

import static java.util.Collections.singletonMap;

/**
 * Func tests for IssueResource.
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestIssueResource extends RestFuncTest
{
    private IssueClient issueClient;

    public void testViewIssueNotFound() throws Exception
    {
        WebResponse resp123 = GET("rest/api/2.0.alpha1/issue/1");
        assertEquals(404, resp123.getResponseCode());
        assertNoLongerExistsError(resp123);

        WebResponse resp123Xml = GET("rest/api/2.0.alpha1/issue/1", singletonMap("Accept", "application/xml;q=0.9,*/*;q=0.8"));
        assertEquals(404, resp123Xml.getResponseCode());
        assertNoLongerExistsError(resp123Xml);

        WebResponse resp415 = GET("rest/api/2.0.alpha1/issue/1", singletonMap("Accept", "application/xml;q=0.9"));
        assertEquals(406, resp415.getResponseCode());
    }

    // ensure we get a 401 and not a 404 if the issue actually exists but we are not logged in.
    public void testNotLoggedIn_IssueExists() throws Exception
    {
        final String key = navigation.issue().createIssue("monkey", "Bug", "test bug");
        navigation.logout();

        WebResponse response = GET("/rest/api/2.0.alpha1/issue/" + key);
        assertEquals(401, response.getResponseCode());
    }

    public void testUnassignedIssueHasNoValue() throws Exception
    {
        administration.generalConfiguration().setAllowUnassignedIssues(true);
        final String key = navigation.issue().createIssue("monkey", "Bug", "test bug");
        navigation.issue().assignIssue(key, "my comment", "Unassigned");

        final Issue issue = issueClient.get(key);
        assertNull(issue.fields.assignee.value);
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        administration.restoreBlankInstance();
        issueClient = new IssueClient(getEnvironmentData());
    }

    private void assertNoLongerExistsError(WebResponse resp123) throws JSONException, IOException
    {
        // {"errorMessages":["The issue no longer exists."],"errors":[]}
        JSONObject content = new JSONObject(resp123.getText());
        assertEquals(1, content.getJSONArray("errorMessages").length());
        assertEquals("Issue Does Not Exist", content.getJSONArray("errorMessages").getString(0));
    }
}
