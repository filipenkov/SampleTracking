package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Comment;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Issue;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.IssueClient;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.IssueLink;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.LinkIssueClient;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.LinkRequest;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Response;

import java.util.List;

/**
 *
 *
 * @since v4.3
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestLinkIssueResource extends RestFuncTest
{
    private LinkIssueClient linkIssueClient;
    private IssueClient issueClient;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        linkIssueClient = new LinkIssueClient(getEnvironmentData());
        issueClient = new IssueClient(getEnvironmentData());
        administration.restoreData("TestLinkIssueResource.xml");
    }

    public void testLinkIssuesWithComment() throws Exception
    {
        LinkRequest linkRequest = new LinkRequest();
        Comment comment = new Comment();
        linkRequest.comment = comment;
        comment.body = "Issue linked via REST!";
        linkRequest.linkType = "Duplicate";
        linkRequest.fromIssueKey = "HSP-1";
        linkRequest.toIssueKey = "MKY-1";
        final Response response = linkIssueClient.linkIssues( linkRequest);
        assertEquals(200, response.statusCode);

        Issue issue = issueClient.get("HSP-1");
        Issue.IssueField<List<Comment>> comments = issue.fields.comment;
        assertEquals("Issue linked via REST!", comments.value.get(0).body);
        Issue.IssueField<List<IssueLink>> issueLinks = issue.fields.links;
        IssueLink issueLink = issueLinks.value.get(0);
        assertEquals("MKY-1", issueLink.issueKey);
        IssueLink.Type type = issueLink.type;
        assertEquals("Duplicate", type.name);
        assertEquals("Duplicates", type.description);
        assertEquals("OUTBOUND", type.direction);

        issue = issueClient.get("MKY-1");
        comments = issue.fields.comment;
        assertEquals(0, comments.value.size());
        issueLinks = issue.fields.links;
        issueLink = issueLinks.value.get(0);
        assertEquals("HSP-1", issueLink.issueKey);
        type = issueLink.type;
        assertEquals("Duplicate", type.name);
        assertEquals("Duplicated by", type.description);
        assertEquals("INBOUND", type.direction);
    }

    public void testLinkIssues() throws Exception
    {
        LinkRequest linkRequest = new LinkRequest();
        linkRequest.linkType = "Duplicate";
        linkRequest.fromIssueKey = "HSP-1";
        linkRequest.toIssueKey = "MKY-1";
        final Response response = linkIssueClient.linkIssues( linkRequest);
        assertEquals(200, response.statusCode);
    }

    public void testLinkIssuesWithInvalidRoleLevelSpecified() throws Exception
    {
        LinkRequest linkRequest = new LinkRequest();
        Comment comment = new Comment();
        linkRequest.comment = comment;
        comment.body = "Issue linked via REST!";
        comment.visibility = new Comment.Visibility("ROLE", "Developers");
        linkRequest.linkType = "Duplicate";
        linkRequest.fromIssueKey = "HSP-1";
        linkRequest.toIssueKey = "MKY-1";
        final Response response = linkIssueClient.linkIssues( linkRequest);
        assertEquals(400, response.statusCode);
        assertEquals("You are currently not a member of the project role: Developers.", response.entity.errors.values().iterator().next());

    }

    public void testLinkIssuesWithGroupLevelSpecified() throws Exception
    {
        LinkRequest linkRequest = new LinkRequest();
        Comment comment = new Comment();
        linkRequest.comment = comment;

        comment.body = "Issue linked via REST!";
        comment.visibility = new Comment.Visibility("GROUP", "jira-administrators");
        linkRequest.linkType = "Duplicate";
        linkRequest.fromIssueKey = "HSP-1";
        linkRequest.toIssueKey = "MKY-1";
        final Response response = linkIssueClient.linkIssues( linkRequest);
        assertEquals(200, response.statusCode);
        final Issue issue = issueClient.get("HSP-1");
        final Issue.IssueField<List<Comment>> comments = issue.fields.comment;
        assertEquals("Issue linked via REST!", comments.value.get(0).body);
    }

    public void testLinkIssuesWithRoleLevelSpecified() throws Exception
    {
        LinkRequest linkRequest = new LinkRequest();
        Comment comment = new Comment();
        linkRequest.comment = comment;
        comment.body = "Issue linked via REST!";
        comment.visibility = new Comment.Visibility("ROLE", "Administrators");
        linkRequest.linkType = "Duplicate";
        linkRequest.fromIssueKey = "HSP-1";
        linkRequest.toIssueKey = "MKY-1";
        final Response response = linkIssueClient.linkIssues( linkRequest);
        assertEquals(200, response.statusCode);
        final Issue issue = issueClient.get("HSP-1");
        final Issue.IssueField<List<Comment>> comments = issue.fields.comment;
        assertEquals("Issue linked via REST!", comments.value.get(0).body);
    }

    public void testLinkIssuesFailedBecauseIssueLinkingDisabled() throws Exception
    {
        oldway_consider_porting.deactivateIssueLinking();
        LinkRequest linkRequest = new LinkRequest();
        Comment comment = new Comment();
        linkRequest.comment = comment;
        comment.body = "Issue linked via REST!";
        linkRequest.linkType = "Duplicate";
        linkRequest.fromIssueKey = "HSP-1";
        linkRequest.toIssueKey = "MKY-1";
        final Response response = linkIssueClient.linkIssues( linkRequest);
        assertEquals(404, response.statusCode);
        assertEquals("Issue linking is currently disabled.", response.entity.errorMessages.get(0));

    }

    public void testLinkIssuesFailedIssueANotVisible() throws Exception
    {
        LinkRequest linkRequest = new LinkRequest();
        Comment comment = new Comment();
        linkRequest.comment = comment;
        comment.body = "Issue linked via REST!";
        linkRequest.linkType = "Duplicate";
        linkRequest.fromIssueKey = "HSP-1";
        linkRequest.toIssueKey = "MKY-1";
        final Response response = linkIssueClient.loginAs("fred").linkIssues( linkRequest);
        assertEquals(404, response.statusCode);
        assertEquals("You do not have the permission to see the specified issue", response.entity.errorMessages.get(0));
    }

    public void testLinkIssuesFailedIssueBNotVisible() throws Exception
    {
        LinkRequest linkRequest = new LinkRequest();
        Comment comment = new Comment();
        linkRequest.comment = comment;
        comment.body = "Issue linked via REST!";
        linkRequest.linkType = "Duplicate";
        linkRequest.fromIssueKey = "HSP-1";
        linkRequest.toIssueKey = "MKY-1";
        final Response response = linkIssueClient.loginAs("bob").linkIssues( linkRequest);
        assertEquals(404, response.statusCode);
        assertEquals("You do not have the permission to see the specified issue", response.entity.errorMessages.get(0));
    }

    public void testLinkIssuesFailedNoLInkIssuePermissionIssueA() throws Exception
    {
        LinkRequest linkRequest = new LinkRequest();
        Comment comment = new Comment();
        linkRequest.comment = comment;
        comment.body = "Issue linked via REST!";
        linkRequest.linkType = "Duplicate";
        linkRequest.fromIssueKey = "HSP-1";
        linkRequest.toIssueKey = "HSP-2";
        final Response response = linkIssueClient.loginAs("bob").linkIssues( linkRequest);
        assertEquals(401, response.statusCode);
        assertEquals("No Link Issue Permission for issue 'HSP-1'", response.entity.errorMessages.get(0));
    }

    public void testLinkIssuesFailedNoLInkIssuePermissionIssueB() throws Exception
    {
        LinkRequest linkRequest = new LinkRequest();
        Comment comment = new Comment();
        linkRequest.comment = comment;
        comment.body = "Issue linked via REST!";
        linkRequest.linkType = "Duplicate";
        linkRequest.fromIssueKey = "MKY-1";
        linkRequest.toIssueKey = "MKY-2";
        final Response response = linkIssueClient.loginAs("fred").linkIssues( linkRequest);
        assertEquals(401, response.statusCode);
        assertEquals("No Link Issue Permission for issue 'MKY-1'", response.entity.errorMessages.get(0));
    }

    public void testLinkIssuesFailedBecauseLinkTypeDoesNotExist() throws Exception
    {
        LinkRequest linkRequest = new LinkRequest();
        Comment comment = new Comment();
        linkRequest.comment = comment;
        comment.body = "Issue linked via REST!";
        linkRequest.linkType = "calculated";
        linkRequest.fromIssueKey = "MKY-1";
        linkRequest.toIssueKey = "MKY-2";
        final Response response = linkIssueClient.linkIssues( linkRequest);
        assertEquals(404, response.statusCode);
        assertEquals("No issue link type with name 'calculated' found.", response.entity.errorMessages.get(0));
    }

    public void testLinkIssuesLinkIssuePermissionForIssueAButNotB() throws Exception
    {
        LinkRequest linkRequest = new LinkRequest();
        Comment comment = new Comment();
        linkRequest.comment = comment;
        comment.body = "Issue linked via REST!";
        linkRequest.linkType = "Duplicate";
        linkRequest.fromIssueKey = "HSP-1";
        linkRequest.toIssueKey = "MKY-1";
        final Response response = linkIssueClient.loginAs("linker").linkIssues( linkRequest);
        assertEquals(200, response.statusCode);
    }

    public void testLinkIssuesNoLinkIssuePermissionForIssueBButB() throws Exception
    {
        LinkRequest linkRequest = new LinkRequest();
        Comment comment = new Comment();
        linkRequest.comment = comment;
        comment.body = "Issue linked via REST!";
        linkRequest.linkType = "Duplicate";
        linkRequest.fromIssueKey = "MKY-1";
        linkRequest.toIssueKey = "HSP-1";
        final Response response = linkIssueClient.loginAs("linker").linkIssues( linkRequest);
        assertEquals(401, response.statusCode);
        assertEquals("No Link Issue Permission for issue 'MKY-1'", response.entity.errorMessages.get(0));
    }

}
