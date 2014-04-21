package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Comment;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Issue;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.IssueClient;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Response;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Transition;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.TransitionPost;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.TransitionsClient;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestIssueResourceTransitions extends RestFuncTest
{
    private IssueClient issueClient;
    private TransitionsClient transitionsClient;

    public void testTransitionLink() throws Exception
    {
        administration.restoreData("TestWorkflowActions.xml");

        Issue issue = issueClient.get("HSP-1");
        assertEquals(getEnvironmentData().getBaseUrl().toExternalForm() + "/rest/api/2.0.alpha1/issue/HSP-1/transitions", issue.transitions);
    }

    public void testCustomFieldInTransition() throws Exception
    {
        administration.restoreData("TestIssueResourceTransitions.xml");

        final Map<Integer, Transition> transitions = transitionsClient.get("HSP-1");
        final List<Transition.TransitionField> fields = transitions.get(2).fields;
        for (Transition.TransitionField field : fields)
        {
            if (field.id.equals("customfield_10000"))
            {
                assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:float", field.type);
            }
        }
    }

    // JRADEV-3474 JRADEV-3471
    public void testNumberCustomFieldLocalized() throws Exception
    {
        administration.restoreData("TestIssueResourceTransitions.xml");
        navigation.userProfile().changeUserLanguage("French (France)");

        TransitionPost transition = new TransitionPost();
        transition.transition = 2;
        transition.fields.put("resolution", "Duplicate");
        transition.fields.put("customfield_10000", 2.5);

        final Response response = transitionsClient.postResponse("HSP-1", transition);
        assertEquals(204, response.statusCode);
    }

    public void testIssueTransitionDestination() throws Exception
    {
        administration.restoreData("TestIssueResourceTransitions.xml");

        Map<Integer, Transition> transitions = transitionsClient.get("HSP-1");
        assertEquals(3, transitions.size());

        assertEquals(transitions.get(2).transitionDestination, "6");
        assertEquals(transitions.get(4).transitionDestination, "3");
        assertEquals(transitions.get(5).transitionDestination, "5");
    }

    public void testTransitionGET() throws Exception
    {
        administration.restoreData("TestWorkflowActions.xml");

        Map<Integer, Transition> transitions = transitionsClient.get("HSP-1");

        assertEquals(3, transitions.size());

        List<Transition.TransitionField> fields = Arrays.asList(
                new Transition.TransitionField("resolution", true, "com.atlassian.jira.issue.resolution.Resolution"),
                new Transition.TransitionField("fixVersions", false, "com.atlassian.jira.project.version.Version"),
                new Transition.TransitionField("assignee", false, "com.opensymphony.user.User")
        );

        Transition transition = transitions.get(2);
        assertEquals("Close Issue", transition.name);
        final List<Transition.TransitionField> closeIssue = transition.fields;
        assertEquals(3, closeIssue.size());
        assertEquals(fields, closeIssue);

        transition = transitions.get(5);
        assertEquals("Resolve Issue", transition.name);
        final List<Transition.TransitionField> resolveIssue = transition.fields;
        assertEquals(3, resolveIssue.size());
        assertEquals(fields, resolveIssue);

        transition = transitions.get(4);
        assertEquals("Start Progress", transition.name);
        final List<Transition.TransitionField> startProgress = transition.fields;
        assertEquals(0, startProgress.size());
    }

    public void testTransitionPUT_noComment() throws Exception
    {
        administration.restoreData("TestWorkflowActions.xml");
        final String transitionURL = getEnvironmentData().getBaseUrl().toExternalForm() + "/rest/api/2.0.alpha1/issue/HSP-1/transitions";

        TransitionPost transition = new TransitionPost();
        transition.transition = 2; // id for "Close Issue"
        transition.fields.put("resolution", "Won't Fix");
        transition.fields.put("fixVersions", CollectionBuilder.newBuilder("New Version 4", "New Version 5").asCollection());

        final Response response = transitionsClient.postResponse("HSP-1", transition);

        assertEquals(204, response.statusCode);

        Issue issue = issueClient.get("HSP-1");
        assertEquals("Closed", issue.fields.status.value.name);
        assertEquals("Won't Fix", issue.fields.resolution.value.name);
        assertEquals("New Version 4", issue.fields.fixVersions.value.get(0).name);
        assertEquals("New Version 5", issue.fields.fixVersions.value.get(1).name);
    }


    public void testTransitionPOST_invalidRole() throws Exception
    {
        administration.restoreData("TestWorkflowActions.xml");

        final TransitionPost transitionPost = new TransitionPost();
        transitionPost.transition = 2; // id for "Close Issue"
        transitionPost.fields.put("resolution", "Won't Fix");
        final Comment comment = new Comment("My comment", "some-non-existing-role");
        comment.visibility = new Comment.Visibility("ROLE", "NON-EXISTING-ROLE");
        transitionPost.comment = comment;

        Response response = transitionsClient.postResponse("HSP-1", transitionPost);
        assertEquals(400, response.statusCode);
        assertEquals("Invalid role [NON-EXISTING-ROLE]", response.entity.errorMessages.get(0));
    }

    // time tracking doesn't correspond to anything in JiraDataTypes. make sure we don't throw an exception on fields like that.
    public void testBasicTimeTracking() throws Exception
    {
        administration.restoreData("TestRESTTransitionsSimple.xml");
        transitionsClient.get("MKY-1");
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        issueClient = new IssueClient(getEnvironmentData());
        transitionsClient = new TransitionsClient(getEnvironmentData());
    }
}
