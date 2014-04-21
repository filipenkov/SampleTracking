package com.atlassian.jira.security.type;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.mock.MockPermissionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;

/**
 * TestCase for CurrentAssigneeHasAssignablePermission.
 *
 * @since v3.12
 */
public class TestCurrentAssigneeHasAssignablePermission extends ListeningTestCase
{
    private MockPermissionManager permissionManager;

    @Before
    public void setUp() throws Exception
    {
        permissionManager = new MockPermissionManager();
    }

    @Test
    public void testHasProjectPermission()
    {
        CurrentAssigneeHasAssignablePermission currentAssigneeHasAssignablePermission = createCurrentAssigneeHasAssignablePermission();

        permissionManager.setDefaultPermission(true);
        assertTrue(currentAssigneeHasAssignablePermission.hasProjectPermission(null, false, null));
        permissionManager.setDefaultPermission(false);
        assertFalse(currentAssigneeHasAssignablePermission.hasProjectPermission(null, false, null));
    }

    @Test
    public void testIsValidForPermission()
    {
        CurrentAssigneeHasAssignablePermission currentAssigneeHasAssignablePermission = createCurrentAssigneeHasAssignablePermission();
        assertTrue(currentAssigneeHasAssignablePermission.isValidForPermission(Permissions.ASSIGN_ISSUE));
        assertTrue(currentAssigneeHasAssignablePermission.isValidForPermission(Permissions.BROWSE));
        assertTrue(currentAssigneeHasAssignablePermission.isValidForPermission(Permissions.ADMINISTER));
        assertTrue(currentAssigneeHasAssignablePermission.isValidForPermission(Permissions.CREATE_ISSUE));

        // "Assignable" is the only Permission we can't be used in because otherwise we have a circular dependency
        assertFalse(currentAssigneeHasAssignablePermission.isValidForPermission(Permissions.ASSIGNABLE_USER));
    }

    private CurrentAssigneeHasAssignablePermission createCurrentAssigneeHasAssignablePermission()
    {
        return new CurrentAssigneeHasAssignablePermission(null)
        {

            PermissionManager getPermissionManager()
            {
                return permissionManager;
            }
        };
    }

    @After
    public void tearDown() throws Exception
    {
        // Just to make sure we don't use too much memory.
        permissionManager = null;
    }

}
