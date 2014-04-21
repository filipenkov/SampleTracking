package com.atlassian.jira.jelly.tag.projectroles;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.util.ErrorCollection;

/**
 * Tests the {@link com.atlassian.jira.jelly.tag.projectroles.IsProjectRoleNameUnique} class.
 */
public class TestIsProjectRoleNameUnique extends AbstractProjectRolesTest
{

    public TestIsProjectRoleNameUnique(String s)
    {
        super(s);
    }

    public void testIsProjectRoleNameUnique() throws Exception
    {
        swapProjectRoleService(new MockProjectRoleService()
        {
            public boolean isProjectRoleNameUnique(User currentUser, String name, ErrorCollection errorCollection)
            {
                return true;
            }

        });

        // test with no project roles
        runScriptAndAssertTextResultEquals("true", "isunique-projectrole.test-isprojectrolenameunique.jelly");
    }
}
