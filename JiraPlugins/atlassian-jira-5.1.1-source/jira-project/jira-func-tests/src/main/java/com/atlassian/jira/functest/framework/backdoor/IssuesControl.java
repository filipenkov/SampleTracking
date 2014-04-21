package com.atlassian.jira.functest.framework.backdoor;

import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.jira.rest.api.issue.IssueFields;
import com.atlassian.jira.rest.api.issue.IssueUpdateRequest;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Issue;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.IssueClient;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Response;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.rest.api.issue.ResourceRef.withId;
import static com.atlassian.jira.rest.api.issue.ResourceRef.withKey;
import static com.atlassian.jira.rest.api.issue.ResourceRef.withName;

/**
 * Use this class from func/selenium/page-object tests that need to manipulate Issues.
 *
 * @since v5.0
 */
public class IssuesControl extends BackdoorControl<IssuesControl>
{
    // Ids of projects pre-imported from blankprojects.xml; should be present in most tests.
    public static final long HSP_PROJECT_ID = 10000;
    public static final long MKY_PROJECT_ID = 10001;

    private IssueClient issueClient;

    public IssuesControl(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
        issueClient = new IssueClient(environmentData);
    }

    public IssueCreateResponse createIssue(long projectId, String summary)
    {
        return createIssue(projectId, summary, null);
    }

    public IssueCreateResponse createIssue(String projectKey, String summary)
    {
        return createIssue(projectKey, summary, null);
    }

    public IssueCreateResponse createIssue(long projectId, String summary, String assignee)
    {
        IssueFields fields = new IssueFields();
        fields.project(withId("" + projectId)); // 10000
        fields.issueType(withId("1"));  // Bug
        fields.priority(withId("1"));   // Blocker
        if (assignee != null)
        {
            fields.assignee(withName(assignee));
        }
        fields.summary(summary);

        IssueUpdateRequest issue = new IssueUpdateRequest();
        return issueClient.create(issue.fields(fields));
    }

    public IssueCreateResponse createIssue(String projectKey, String summary, String assignee)
    {
        IssueFields fields = new IssueFields();
        fields.project(withKey(projectKey)); // MKY
        fields.issueType(withId("1"));  // Bug
        fields.priority(withId("1"));   // Blocker
        if (assignee != null)
        {
            fields.assignee(withName(assignee));
        }
        fields.summary(summary);

        IssueUpdateRequest issue = new IssueUpdateRequest();
        return issueClient.create(issue.fields(fields));
    }


    public void assignIssue(String issueKey, String username)
    {
        IssueUpdateRequest updateSummaryRequest = new IssueUpdateRequest().fields(new IssueFields()
                .assignee(withName(username))
        );

        issueClient.update(issueKey, updateSummaryRequest);
    }

    public Issue getIssue(String issueKey, Issue.Expand... expand)
    {
        return issueClient.get(issueKey, expand);
    }


    public IssuesControl addLabel(String issueKey, String label)
    {
        final Map<String, String> add = MapBuilder.<String, String>newBuilder().add("add", label).toMap();
        final List<Map<String, String>> addList = Collections.singletonList(add);
        final Map<String, List<Map<String, String>>> labels = MapBuilder.<String, List<Map<String, String>>>newBuilder().add("labels", addList).toMap();
        final Map<String, Map<String, List<Map<String, String>>>> update = MapBuilder.<String, Map<String, List<Map<String, String>>>>newBuilder().add("update", labels).toMap();
        final Response response = issueClient.update(issueKey, update);

        assert(response.statusCode == 204);
        return this;
    }

    public IssuesControl setSummary(String issueKey, String summary)
    {
        IssueUpdateRequest updateSummaryRequest = new IssueUpdateRequest().fields(new IssueFields()
                .summary(summary));

        final Response response = issueClient.updateResponse(issueKey, updateSummaryRequest);

        assert(response.statusCode == 204);
        return this;
    }


    @Override
    public IssuesControl loginAs(String username)
    {
        issueClient.loginAs(username);
        return super.loginAs(username);
    }

    @Override
    public IssuesControl loginAs(String username, String password)
    {
        issueClient.loginAs(username, password);
        return super.loginAs(username, password);
    }
    
    public Issue getIssue(String key)
    {
        return issueClient.get(key);
    }
}
