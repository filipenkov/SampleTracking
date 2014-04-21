package com.atlassian.jira.bc.issue.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.MockProjectRoleManager;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TestDefaultVisibilityValidatorForWorklogs extends ListeningTestCase
{
    private JiraAuthenticationContext jiraAuthenticationContext;


    @Before
    public void setUp() throws Exception
    {
        jiraAuthenticationContext = new MockSimpleAuthenticationContext(null);
    }

    private User createMockUser()
    {
        return new MockUser("fred");
    }

    @Test
    public void testAnonymousUser()
    {
        Mock mockIssue = new Mock(Issue.class);
        DefaultVisibilityValidator visibilityValidator = new DefaultVisibilityValidator(null, jiraAuthenticationContext, null, null)
        {

            public boolean isProjectRoleVisiblityEnabled()
            {
                return false;
            }
        };
        ErrorCollection errorCollection = new SimpleErrorCollection();
        assertFalse(visibilityValidator.isValidVisibilityData(new JiraServiceContextImpl(null, errorCollection), "worklog", (Issue) mockIssue.proxy(), null, "12345"));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("You cannot add a comment for specific groups or roles, as your session has expired. Please log in and try again.", errorCollection.getErrorMessages().iterator().next());
    }

    @Test
    public void testIsValidVisibilityDataRoleVisibilityDisabled()
    {
        Mock mockIssue = new Mock(Issue.class);
        DefaultVisibilityValidator visibilityValidator = new DefaultVisibilityValidator(null, jiraAuthenticationContext, null, null)
        {

            public boolean isProjectRoleVisiblityEnabled()
            {
                return false;
            }
        };
        ErrorCollection errorCollection = new SimpleErrorCollection();
        assertFalse(visibilityValidator.isValidVisibilityData(new JiraServiceContextImpl(createMockUser(), errorCollection), "worklog", (Issue) mockIssue.proxy(), null, "12345"));
        assertEquals(1, errorCollection.getErrors().size());
        assertEquals("Project Role level visibility has been disabled.", errorCollection.getErrors().get("commentLevel"));
    }

    @Test
    public void testIsValidVisibilityDataGroupVisibilityDisabled()
    {
        Mock mockIssue = new Mock(Issue.class);
        DefaultVisibilityValidator visibilityValidator = new DefaultVisibilityValidator(null, jiraAuthenticationContext, null, null)
        {

            public boolean isGroupVisiblityEnabled()
            {
                return false;
            }
        };
        ErrorCollection errorCollection = new SimpleErrorCollection();
        assertFalse(visibilityValidator.isValidVisibilityData(new JiraServiceContextImpl(createMockUser(), errorCollection), "worklog", (Issue) mockIssue.proxy(), "testGroup", null));
        assertEquals(1, errorCollection.getErrors().size());
        assertEquals("Group level visibility has been disabled.", errorCollection.getErrors().get("commentLevel"));
    }

    @Test
    public void testIsValidVisibilityDataBothGroupAndRoleProvided()
    {
        Mock mockIssue = new Mock(Issue.class);
        DefaultVisibilityValidator visibilityValidator = new DefaultVisibilityValidator(null, jiraAuthenticationContext, null, null);
        ErrorCollection errorCollection = new SimpleErrorCollection();
        assertFalse(visibilityValidator.isValidVisibilityData(new JiraServiceContextImpl(createMockUser(), errorCollection), "worklog", (Issue) mockIssue.proxy(), "testGroup", "12345"));
        assertEquals(1, errorCollection.getErrors().size());
        assertEquals("Selecting worklog visibility can be for group or role, not both!", errorCollection.getErrors().get("commentLevel"));
    }

    @Test
    public void testIsValidVisibilityDataNullIssue()
    {
        DefaultVisibilityValidator visibilityValidator = new DefaultVisibilityValidator(null, jiraAuthenticationContext, null, null);
        ErrorCollection errorCollection = new SimpleErrorCollection();
        assertFalse(visibilityValidator.isValidVisibilityData(new JiraServiceContextImpl(null, errorCollection), "worklog", null, null, null));
        assertEquals(1, errorCollection.getErrorMessages().size());
        assertEquals("Can not modify a worklog without an issue specified.", errorCollection.getErrorMessages().iterator().next());
    }

    @Test
    public void testIsRoleLevelValidRoleDoesNotExist()
    {
        Mock mockRoleManager = new Mock(ProjectRoleManager.class);
        mockRoleManager.expectAndReturn("getProjectRole", P.ANY_ARGS, null);
        DefaultVisibilityValidator visibilityValidator = new DefaultVisibilityValidator(null, jiraAuthenticationContext, (ProjectRoleManager) mockRoleManager.proxy(), null);
        ErrorCollection errorCollection = new SimpleErrorCollection();
        assertFalse(visibilityValidator.isRoleLevelValid(new JiraServiceContextImpl(null, errorCollection), "worklog", "1234", null));
        assertEquals(1, errorCollection.getErrors().size());
        assertEquals("Role with id: 1234 does not exist.", errorCollection.getErrors().get("commentLevel"));
    }

    @Test
    public void testIsRoleLevelValidUserNotInRole()
    {
        Mock mockIssue = new Mock(Issue.class);
        mockIssue.expectAndReturn("getProjectObject", P.ANY_ARGS, null);
        Mock mockRoleManager = new Mock(ProjectRoleManager.class);
        mockRoleManager.expectAndReturn("getProjectRole", P.ANY_ARGS, new MockProjectRoleManager.MockProjectRole(1234, "Test Role", "Test Desc"));
        mockRoleManager.expectAndReturn("isUserInProjectRole", P.ANY_ARGS, Boolean.FALSE);
        DefaultVisibilityValidator visibilityValidator = new DefaultVisibilityValidator(null, jiraAuthenticationContext, (ProjectRoleManager) mockRoleManager.proxy(), null);
        ErrorCollection errorCollection = new SimpleErrorCollection();
        assertFalse(visibilityValidator.isRoleLevelValid(new JiraServiceContextImpl(null, errorCollection), "worklog", "1234", (Issue) mockIssue.proxy()));
        assertEquals(1, errorCollection.getErrors().size());
        assertEquals("You are currently not a member of the project role: Test Role.", errorCollection.getErrors().get("commentLevel"));
    }

    @Test
    public void testIsRoleLevelValidRoleIdNotNumber()
    {
        DefaultVisibilityValidator visibilityValidator = new DefaultVisibilityValidator(null, jiraAuthenticationContext, null, null);
        ErrorCollection errorCollection = new SimpleErrorCollection();
        assertFalse(visibilityValidator.isRoleLevelValid(new JiraServiceContextImpl(null, errorCollection), "worklog", "1234abc", null));
        assertEquals(1, errorCollection.getErrors().size());
        assertEquals("Role ID must be a number!", errorCollection.getErrors().get("commentLevel"));
    }
}
