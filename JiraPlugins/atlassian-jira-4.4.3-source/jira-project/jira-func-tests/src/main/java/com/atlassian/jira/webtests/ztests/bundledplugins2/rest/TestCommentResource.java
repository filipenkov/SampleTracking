package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.admin.GeneralConfiguration;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Comment;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.CommentClient;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Response;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpNotFoundException;

/**
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestCommentResource extends RestFuncTest
{
    private CommentClient commentClient;

    public void testViewCommentNotFound() throws Exception
    {
        administration.restoreData("TestEditComment.xml");

        // {"errorMessages":["Can not find a comment for the id: 1."],"errors":[]}
        Response content1 = commentClient.getResponse("1");
        assertEquals(404, content1.statusCode);
        assertEquals(1, content1.entity.errorMessages.size());
        assertTrue(content1.entity.errorMessages.contains("Can not find a comment for the id: 1."));

        // {"errorMessages":["Can not find a comment for the id: piolho."],"errors":[]}
        Response contentPiolho = commentClient.getResponse("piolho");
        assertEquals(404, contentPiolho.statusCode);
        assertEquals(1, contentPiolho.entity.errorMessages.size());
        assertTrue(contentPiolho.entity.errorMessages.contains("Can not find a comment for the id: piolho."));
    }

    public void testAnonymousComment() throws Exception
    {
        administration.restoreData("TestRESTAnonymous.xml");
        final Comment comment = commentClient.get("10000");
        assertNull(comment.author);
    }

    public void testAnonymous() throws Exception
    {
        // first add a comment that only Administrators can see
        administration.restoreBlankInstance();
        administration.generalConfiguration().setCommentVisibility(GeneralConfiguration.CommentVisibility.PROJECT_ROLES);
        administration.roles().addProjectRoleForUser("monkey", "Administrators", ADMIN_USERNAME);
        final String key = navigation.issue().createIssue("monkey", "Bug", "First Test Issue");
        navigation.issue().addComment(key, "comment", "Administrators");

        navigation.logout();

        try
        {
            String url = getEnvironmentData().getBaseUrl() + "/rest/api/2.0.alpha1/comment/10000";
            tester.getDialog().getWebClient().sendRequest(new GetMethodWebRequest(url));
            fail();
        }
        catch (HttpNotFoundException expected)
        {
            // ignored
        }
    }

    public void testCommentJson() throws Exception
    {
        // first add a comment that only Administrators can see
        administration.restoreBlankInstance();
        administration.generalConfiguration().setCommentVisibility(GeneralConfiguration.CommentVisibility.PROJECT_ROLES);
        administration.roles().addProjectRoleForUser("monkey", "Administrators", ADMIN_USERNAME);
        final String key = navigation.issue().createIssue("monkey", "Bug", "First Test Issue");
        navigation.issue().addComment(key, "comment", "Administrators");

        Comment json = commentClient.get("10000");

        // we don't want to try verifying the actual timestamp because testing time is a path of madness, ask Andreas.
        assertNotNull(json.created);
        assertNotNull(json.updated);

        assertEquals(getEnvironmentData().getBaseUrl() + "/rest/api/2.0.alpha1/comment/10000", json.self);
        assertEquals("comment", json.body);
        assertEquals("ROLE", json.visibility.type);
        assertEquals("Administrators", json.visibility.value);

        assertEquals(ADMIN_USERNAME, json.author.name);
        assertEquals(ADMIN_FULLNAME, json.author.displayName);
        assertEquals(getEnvironmentData().getBaseUrl() + "/rest/api/2.0.alpha1/user?username=admin", json.author.self);

        assertEquals(ADMIN_USERNAME, json.updateAuthor.name);
        assertEquals(ADMIN_FULLNAME, json.updateAuthor.displayName);
        assertEquals(getEnvironmentData().getBaseUrl() + "/rest/api/2.0.alpha1/user?username=admin", json.updateAuthor.self);
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        commentClient = new CommentClient(getEnvironmentData());
    }
}
