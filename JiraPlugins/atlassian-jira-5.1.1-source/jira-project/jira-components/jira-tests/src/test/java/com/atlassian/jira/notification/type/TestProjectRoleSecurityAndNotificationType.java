package com.atlassian.jira.notification.type;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.MockProjectRoleManager;
import com.atlassian.jira.security.JiraAuthenticationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 */
public class TestProjectRoleSecurityAndNotificationType extends LegacyJiraMockTestCase
{
    private ProjectRoleSecurityAndNotificationType projectRoleSecurityAndNotificationType = null;

    protected void setUp() throws Exception
    {
        super.setUp();
        MockProjectRoleManager projectRoleManager = new MockProjectRoleManager();
        JiraAuthenticationContext jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
        projectRoleSecurityAndNotificationType = new ProjectRoleSecurityAndNotificationType(jiraAuthenticationContext, projectRoleManager, null);
    }

    public void testGetUsersForProjectAndRoleId()
    {
        // Test that when the projectRoleActors can not be resolved we return an empty set
        Set users = projectRoleSecurityAndNotificationType.getUsersFromRole(null, "1");
        assertTrue(users.isEmpty());
    }

    public void testDoValidationOld()
    {
        Map<String, String> paramMap = new HashMap<String, String>();
        String key = "test";

        // The value in this map is the name of a ProjectRole contained in the MockProjectRoleManager
        paramMap.put(key, "1");
        assertTrue(projectRoleSecurityAndNotificationType.doValidation(key, paramMap));

        paramMap.put(key, "20");
        assertFalse(projectRoleSecurityAndNotificationType.doValidation(key, paramMap));
    }

    public void testGetArgumentDisplayName()
    {
        // Testing that getName is called on the returned project role, the mock returns this as the project role with id 1
        assertEquals("Project Administrator", projectRoleSecurityAndNotificationType.getArgumentDisplay("1"));
    }
}
