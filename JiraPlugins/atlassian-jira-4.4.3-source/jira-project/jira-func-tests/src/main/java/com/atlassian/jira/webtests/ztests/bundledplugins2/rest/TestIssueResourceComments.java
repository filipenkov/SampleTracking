package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.admin.GeneralConfiguration;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Comment;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Issue;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.IssueClient;

/**
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestIssueResourceComments extends RestFuncTest
{
    private IssueClient issueClient;

    public void testCommentRendering() throws Exception
    {
        administration.restoreData("TestEditComment.xml");
        Issue hsp1 = issueClient.get("HSP-1");
        assertEquals("I'm a hero!", hsp1.fields.comment.value.get(0).body);

        Issue expandedHsp1 = issueClient.get("HSP-1", Issue.Expand.html);
        assertEquals("I'm a hero!", expandedHsp1.fields.comment.value.get(0).body);
        assertEquals("I&#39;m a hero!", expandedHsp1.html.comment.get(0));
    }

    public void testSystemTextFieldRendering() throws Exception
    {
        administration.restoreData("TestEditComment.xml");
        navigation.issue().setDescription("HSP-1", "I'll have 5<10<15 things?");
        navigation.issue().setEnvironment("HSP-1", "I'll have 5<10<15 things?");

        Issue hsp1 = issueClient.get("HSP-1");
        // wtf is up with the \r\n being prepended here?
        assertEquals("\r\nI'll have 5<10<15 things?", hsp1.fields.description.value);
        assertEquals("I'll have 5<10<15 things?", hsp1.fields.environment.value);

        Issue expandedHsp1 = issueClient.get("HSP-1", Issue.Expand.html);
        assertEquals("\r<br/>\nI&#39;ll have 5&lt;10&lt;15 things?", expandedHsp1.html.description);
        assertEquals("I&#39;ll have 5&lt;10&lt;15 things?", expandedHsp1.html.environment);
    }

    public void testComment() throws Exception
    {
        administration.restoreBlankInstance();
        administration.generalConfiguration().setCommentVisibility(GeneralConfiguration.CommentVisibility.PROJECT_ROLES);
        administration.roles().addProjectRoleForUser("monkey", "Administrators", ADMIN_USERNAME);
        final String key = navigation.issue().createIssue("monkey", "Bug", "First Test Issue");
        navigation.issue().addComment(key, "comment", "Administrators");

        Issue issue = issueClient.get(key);
        assertEquals(1, issue.fields.comment.value.size());

        tester.gotoPage("/rest/api/2.0.alpha1/issue/" + key);
        Comment comment = issue.fields.comment.value.get(0);

        // assert the comment itself.
        assertEquals("comment", comment.body);
        assertEquals("ROLE", comment.visibility.type);
        assertEquals("Administrators", comment.visibility.value);

        assertNotNull(comment.created);
        assertNotNull(comment.updated);

        assertEquals(getEnvironmentData().getBaseUrl() + "/rest/api/2.0.alpha1/comment/10000", comment.self);

        assertEquals(ADMIN_USERNAME, comment.author.name);
        assertEquals(ADMIN_FULLNAME, comment.author.displayName);
        assertEquals(getEnvironmentData().getBaseUrl() + "/rest/api/2.0.alpha1/user?username=admin", comment.author.self);

        assertEquals(ADMIN_USERNAME, comment.updateAuthor.name);
        assertEquals(ADMIN_FULLNAME, comment.updateAuthor.displayName);
        assertEquals(getEnvironmentData().getBaseUrl() + "/rest/api/2.0.alpha1/user?username=admin", comment.updateAuthor.self);
    }

    /**
     * Setup for an actual test
     */
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        issueClient = new IssueClient(getEnvironmentData());
    }
}
