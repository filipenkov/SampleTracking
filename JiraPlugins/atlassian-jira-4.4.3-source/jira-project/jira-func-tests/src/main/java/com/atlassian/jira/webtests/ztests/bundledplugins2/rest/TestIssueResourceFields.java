package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Component;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Issue;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.IssueClient;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.IssueType;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Priority;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Project;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Resolution;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Status;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.User;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Version;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Vote;

import java.util.List;

/**
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestIssueResourceFields extends RestFuncTest
{
    private IssueClient issueClient;

    public void testExpandos() throws Exception
    {
        administration.restoreData("TestIssueResourceFields.xml");
        final Issue json = issueClient.get("HSP-1");
        assertTrue(json.expand.equals("html"));
        // ensure that the expando has nothing in it
        assertNull(json.html);

        final Issue expanded = issueClient.get("HSP-1", Issue.Expand.html);
        assertEquals(4, expanded.html.length());
        assertNotNull(expanded.html.environment);
        assertNotNull(expanded.html.description);
        assertEquals(1, expanded.html.comment.size());
        assertEquals(1, expanded.html.worklog.size());
    }

    public void testSystemFields() throws Exception
    {
        administration.restoreData("TestIssueResourceFields.xml");
        final Issue json = issueClient.get("HSP-1", Issue.Expand.html);

        assertEquals("HSP-1", json.key);
        assertEquals(getBaseUrl() + "/rest/api/2.0.alpha1/issue/HSP-1", json.self);

        assertNotNull(json.fields);
        assertNotNull(json.transitions);
        assertNotNull(json.html);

        assertEquals(getBaseUrl() + "/rest/api/2.0.alpha1/issue/HSP-1/transitions", json.transitions);

        Issue.Fields fields = json.fields;

        // first the "simple" fields...the ones that aren't JSONObject or JSONArray

        assertNotNull(fields.summary);
        assertEquals("Donec posuere tellus nulla; vitae pellentesque.", fields.summary.value);

        Vote votes = fields.votes.value;
        assertEquals(0, votes.votes);
        assertEquals(false, votes.hasVoted);
        assertEquals(getBaseUrl() + "/rest/api/2.0.alpha1/issue/HSP-1/votes", votes.self);

        assertNotNull(fields.security);
        assertEquals("Insecure", fields.security.value);

        assertNotNull(fields.resolutiondate);
        assertEqualDateStrings("2010-06-11T12:19:10.488+1000", fields.resolutiondate.value);

        assertNotNull(fields.environment);
        assertEquals("Curabitur bibendum molestie eros vel pretium.<br/>\n", json.html.environment);

        assertNotNull(fields.updated);
        assertEqualDateStrings("2010-06-11T12:25:16.265+1000", fields.updated.value);

        assertNotNull(fields.created);
        assertEqualDateStrings("2010-06-11T12:17:45.383+1000", fields.created.value);

        assertNotNull(fields.description);
        assertEquals("Suspendisse a mi augue. Donec quis.<br/>\n", json.html.description);

        assertNotNull(fields.duedate);
        assertEquals("2010-06-23", fields.duedate.value);

        // things are that just arrays of strings
        checkLabels(fields);

        // there are already separate tests for timetracking so we'll leave this as a simple assertion
        assertNotNull(fields.timetracking);

        checkIssueType(fields);
        checkStatus(fields);
        checkAssignee(fields);
        checkReporter(fields);
        checkResolution(fields);
        checkProject(fields);
        checkPriority(fields);

        // these things are the most complicated...arrays of JSONObjects
        checkComponents(fields);
        checkFixVersions(fields);
        checkVersions(fields);
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        issueClient = new IssueClient(getEnvironmentData());
    }

    private void checkFixVersions(final Issue.Fields fields)
    {
        assertNotNull(fields.fixVersions);
        final List<Version> versions = fields.fixVersions.value;
        assertEquals(getBaseUrl() + "/rest/api/2.0.alpha1/version/10000", versions.get(0).self);
        assertEquals("Test Version Description 1", versions.get(0).description);
        assertEquals("New Version 1", versions.get(0).name);
        assertFalse(versions.get(0).archived);
        assertFalse(versions.get(0).released);

        assertEquals(getRestApiUrl("version/10002"), versions.get(1).self);
        assertEquals("Test Version Description 5", versions.get(1).description);
        assertEquals("New Version 5", versions.get(1).name);
        assertFalse(versions.get(1).archived);
        assertFalse(versions.get(1).released);
    }

    private void checkVersions(final Issue.Fields fields)
    {
        assertNotNull(fields.versions);
        final List<Version> versions = fields.versions.value;
        assertEquals(getRestApiUrl("version/10000"), versions.get(0).self);
        assertEquals("Test Version Description 1", versions.get(0).description);
        assertEquals("New Version 1", versions.get(0).name);
        assertFalse(versions.get(0).archived);
        assertFalse(versions.get(0).released);

        assertEquals(getRestApiUrl("version/10002"), versions.get(1).self);
        assertEquals("Test Version Description 5", versions.get(1).description);
        assertEquals("New Version 5", versions.get(1).name);
        assertFalse(versions.get(1).archived);
        assertFalse(versions.get(1).released);
    }

    private void checkComponents(final Issue.Fields fields)
    {
        assertNotNull(fields.components);
        final List<Component> components = fields.components.value;
        assertEquals(getBaseUrl() + "/rest/api/2.0.alpha1/component/10001", components.get(0).self);
        assertEquals("New Component 2", components.get(0).name);

        assertEquals(getBaseUrl() + "/rest/api/2.0.alpha1/component/10002", components.get(1).self);
        assertEquals("New Component 3", components.get(1).name);
    }

    private void checkPriority(final Issue.Fields fields)
    {
        assertNotNull(fields.priority);
        final Priority priority = fields.priority.value;
        assertEquals(getBaseUrl() + "/rest/api/2.0.alpha1/priority/3", priority.self);
        assertEquals("Major", priority.name);
    }

    private void checkProject(final Issue.Fields fields)
    {
        assertNotNull(fields.project);
        final Project project = fields.project.value;
        assertEquals(getBaseUrl() + "/rest/api/2.0.alpha1/project/HSP", project.self);
        assertEquals("HSP", project.key);
    }

    private void checkResolution(final Issue.Fields fields)
    {
        assertNotNull(fields.resolution);
        final Resolution resolution = fields.resolution.value;
        assertEquals(getBaseUrl() + "/rest/api/2.0.alpha1/resolution/1", resolution.self);
        assertEquals("Fixed", resolution.name);
    }

    private void checkAssignee(final Issue.Fields fields)
    {
        assertNotNull(fields.assignee);
        final User user = fields.assignee.value;
        assertEquals(getBaseUrl() + "/rest/api/2.0.alpha1/user?username=admin", user.self);
        assertEquals(ADMIN_USERNAME, user.name);
        assertEquals(ADMIN_FULLNAME, user.displayName);
    }

    private void checkReporter(final Issue.Fields fields)
    {
        assertNotNull(fields.reporter);
        final User user = fields.reporter.value;
        assertEquals(getBaseUrl() + "/rest/api/2.0.alpha1/user?username=admin", user.self);
        assertEquals(ADMIN_USERNAME, user.name);
        assertEquals(ADMIN_FULLNAME, user.displayName);
    }

    private void checkStatus(final Issue.Fields fields)
    {
        assertNotNull(fields.status);
        final Status status = fields.status.value;
        assertEquals(getBaseUrl() + "/rest/api/2.0.alpha1/status/5", status.self);
        assertEquals("Resolved", status.name);
    }

    private void checkIssueType(final Issue.Fields fields)
    {
        assertNotNull(fields.issuetype);
        final IssueType issueType = fields.issuetype.value;
        assertEquals(getBaseUrl() + "/rest/api/2.0.alpha1/issueType/1", issueType.self);
        assertEquals("Bug", issueType.name);
        assertFalse(issueType.subtask);
    }

    private void checkLabels(final Issue.Fields fields)
    {
        assertNotNull(fields.labels);
        final List<String> labels = fields.labels.value;
        assertEquals(3, labels.size());
        assertEquals("bad", labels.get(0));
        assertEquals("big", labels.get(1));
        assertEquals("wolf", labels.get(2));
    }
}
