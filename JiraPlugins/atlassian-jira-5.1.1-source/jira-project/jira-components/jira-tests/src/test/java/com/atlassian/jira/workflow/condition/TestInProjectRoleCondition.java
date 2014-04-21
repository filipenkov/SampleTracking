package com.atlassian.jira.workflow.condition;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.MockProjectManager;
import com.atlassian.jira.mock.MockProjectRoleManager;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.opensymphony.workflow.WorkflowException;
import org.ofbiz.core.entity.GenericValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit test for {@link InProjectRoleCondition}
 */
public class TestInProjectRoleCondition extends LegacyJiraMockTestCase
{
    private GenericValue project;
    private MockIssue issue;
    private User user;

    private static class ConditionResult
    {
        private boolean value = false;
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        project = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(1)));
        issue = new MockIssue();
        issue.setProject(project);
        user = createMockUser("hornblower");
    }

    protected void tearDown() throws Exception
    {
        UtilsForTests.cleanOFBiz();
        issue = null;
        user = null;
        project = null;
        super.tearDown();
    }
    
    public void testPassesCondition() throws Exception
    {
        ConditionResult passesCondition = new ConditionResult();
        InProjectRoleCondition cond = createConditionInvokedWithIssueAndUser(issue, user);

        // mock out ProjectRoleManager and ProjectManager
        installProjectRoleManager(passesCondition, MockProjectRoleManager.PROJECT_ROLE_TYPE_1);
        ManagerFactory.addService(ProjectManager.class, new MockProjectManager());

        Map params = new HashMap();
        params.put(InProjectRoleCondition.KEY_PROJECT_ROLE_ID, "5");

        passesCondition.value = true;
        assertTrue("The condition should pass because the user is in role!", cond.passesCondition(null, params, null));
        passesCondition.value = false;
        assertFalse("The condition should fail because the user is not in role!", cond.passesCondition(null, params, null));
    }

    /**
     * Tests that if the project role is not found by the ProjectRoleManager the
     * Workflow Condition always returns false.
     * @throws Exception
     */
    public void testPassesConditionNoProjectRoleFound() throws Exception
    {
        ConditionResult passesCondition = new ConditionResult();
        // our ProjectRoleManager will return null for the ProjectRole
        installProjectRoleManager(passesCondition, null);
        ManagerFactory.addService(ProjectManager.class, new MockProjectManager());
        Map transientVars = null;
        Map params = new HashMap();
        params.put(InProjectRoleCondition.KEY_PROJECT_ROLE_ID, "123");
        passesCondition.value = false;
        InProjectRoleCondition cond = createConditionInvokedWithIssueAndUser(issue, user);
        assertFalse("When project role doesn't exist condition should always fail", cond.passesCondition(null, params, null));
    }


    public void testPassesConditionWithNoParameters() throws WorkflowException
    {
        InProjectRoleCondition cond = createConditionInvokedWithIssueAndUser(issue, user);
        assertFalse("the condition null should fail when given no params", cond.passesCondition(new HashMap(), new HashMap(), null));
    }

// HELPER METHODS -------------------------------------

    /**
     * Installs a ProjectRoleManager that provides the given ProjectRole
     * for a getProjectRole(id) and returns the result held in the given
     * ConditionResult for the isUserInProjectRole() method. Other methods
     * are not implemented
     * @param userInRoleCondition the delegate for isUserInProjectRole()
     */
    private void installProjectRoleManager(final ConditionResult userInRoleCondition, final ProjectRole projectRole)
    {
        MockProjectRoleManager prm = new MockProjectRoleManager()
        {
            public boolean isUserInProjectRole(User user, ProjectRole projectRole, Project project)
            {
                return userInRoleCondition.value;
            }

            public ProjectRole getProjectRole(Long id)
            {
                return projectRole;
            }
        };
        ManagerFactory.addService(ProjectRoleManager.class, prm);
    }

    /**
     * Creates a subclass of InProjectRoleCondition that has overridden superclass
     * methods getIssue() and getCaller() that are mocked out to return only the
     * given issue and user.
     * @param issue the issue to return from getIssue()
     * @param caller the user to return from getCaller()
     * @return the InProjectRoleCondition with mocked out superclass methods.
     */
    private InProjectRoleCondition createConditionInvokedWithIssueAndUser(final MockIssue issue, final User caller)
    {
        InProjectRoleCondition cond = new InProjectRoleCondition()
        {
            protected Issue getIssue(Map transientVars) throws DataAccessException
            {
                return issue;
            }

            protected User getCaller(Map transientVars, Map args)
            {
                return caller;
            }
        };
        return cond;
    }

}
