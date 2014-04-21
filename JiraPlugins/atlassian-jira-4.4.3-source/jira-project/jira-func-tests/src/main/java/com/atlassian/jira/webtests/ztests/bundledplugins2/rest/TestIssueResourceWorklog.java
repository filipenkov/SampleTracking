package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Issue;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.IssueClient;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.User;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Worklog;

import java.util.List;

/**
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestIssueResourceWorklog extends RestFuncTest
{
    private IssueClient issueClient;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        issueClient = new IssueClient(getEnvironmentData());
        administration.restoreData("TestWorklogAndTimeTracking.xml");
    }

    public void testView() throws Exception
    {
        final Issue json = issueClient.get("HSP-1");

        final List<Worklog> worklogs = json.fields.worklog.value;
        assertEquals(1, worklogs.size());

        final Worklog log = worklogs.get(0);
        assertNotNull(log.self);
        assertEquals("", log.comment);
        assertEquals(120, log.minutesSpent);
        assertEqualDateStrings("2010-05-24T09:52:41.092+1000", log.created);
        assertEqualDateStrings("2010-05-24T09:52:41.092+1000", log.updated);
        assertEqualDateStrings("2010-05-24T09:52:00.000+1000", log.started);
        assertEquals(getBaseUrl() + "/rest/api/2.0.alpha1/issue/HSP-1", log.issue);

        final User author = log.author;
        assertNotNull(author.self);
        assertEquals(ADMIN_USERNAME, author.name);
        assertEquals(ADMIN_FULLNAME, author.displayName);

        final User updateAuthor = log.updateAuthor;
        assertNotNull(updateAuthor.self);
        assertEquals(ADMIN_USERNAME, updateAuthor.name);
        assertEquals(ADMIN_FULLNAME, updateAuthor.displayName);
    }

    /**
     * JRADEV-2313.
     *
     * @throws Exception junit makes me
     */
    public void testViewLoggedByDeletedUser() throws Exception
    {
        final Issue json = issueClient.get("HSP-3");

        final List<Worklog> worklogs = json.fields.worklog.value;
        assertEquals(1, worklogs.size());

        final Worklog log = worklogs.get(0);
        assertNotNull(log.self);
        assertEquals("spent a whole minute on this", log.comment);
        assertEquals(1, log.minutesSpent);
        assertEqualDateStrings("2010-07-12T12:47:39.198+1000", log.created);
        assertEqualDateStrings("2010-07-12T12:47:39.198+1000", log.updated);
        assertEqualDateStrings("2010-07-12T12:47:00.000+1000", log.started);
        assertEquals(getBaseUrl() + "/rest/api/2.0.alpha1/issue/HSP-3", log.issue);

        User author = log.author;
        assertEquals("deleted", author.name);

        User updateAuthor = log.updateAuthor;
        assertEquals("deleted", updateAuthor.name);
    }
}
