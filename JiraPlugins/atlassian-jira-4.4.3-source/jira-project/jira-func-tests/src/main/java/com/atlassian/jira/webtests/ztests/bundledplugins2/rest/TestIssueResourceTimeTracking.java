package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Issue;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.IssueClient;

import java.io.IOException;

/**
 * Tests the time tracking field.
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestIssueResourceTimeTracking extends RestFuncTest
{
    private IssueClient issueClient;

    public void testTimeTrackingDisabled() throws Exception
    {
        restoreData(false);

        Issue issue = issueClient.get("FUNC-3");
        assertNull("Time tracking shouldn't be in response when time tracking is disabled", issue.fields.timetracking);
    }

    public void testIssueWithNoTimeTracking() throws Exception
    {
        Issue issue = issueClient.get("FUNC-1");
        assertNull("Time tracking shouldn't exist for FUNC-1", issue.fields.timetracking);
    }

    public void testIssueWithOriginalEstimate() throws Exception
    {
        restoreData(true);

        Issue issue = issueClient.get("FUNC-3");
        assertNotNull(issue.fields.timetracking);
        assertEquals(1440L, issue.fields.timetracking.value.timeoriginalestimate);
    }

    public void testIssueWithTimeSpent() throws Exception
    {
        restoreData(true);

        Issue issue = issueClient.get("FUNC-3");
        assertNotNull(issue.fields.timetracking);
        assertEquals(480L, issue.fields.timetracking.value.timespent);
    }

    public void testIssueWithTimeRemaining() throws Exception
    {
        restoreData(true);

        Issue issue = issueClient.get("FUNC-3");
        assertNotNull(issue.fields.timetracking);
        assertEquals(960L, issue.fields.timetracking.value.timeestimate);
    }

    protected void restoreData(boolean timeTrackingEnabled) throws IOException
    {
        administration.restoreData("TestIssueResourceTimeTracking.xml");
        if (!timeTrackingEnabled)
        {
            administration.timeTracking().disable();
        }
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        issueClient = new IssueClient(getEnvironmentData());
    }
}
