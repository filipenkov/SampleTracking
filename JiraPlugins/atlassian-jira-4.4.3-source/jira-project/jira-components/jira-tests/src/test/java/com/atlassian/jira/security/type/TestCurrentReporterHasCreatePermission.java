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
 * Tests the CurrentReporterHasCreatePermission class.
 *
 * @since v3.12
 */
public class TestCurrentReporterHasCreatePermission extends ListeningTestCase
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
        CurrentReporterHasCreatePermission currentReporterHasCreatePermission = createCurrentReporterHasCreatePermission();

        permissionManager.setDefaultPermission(true);
        assertTrue(currentReporterHasCreatePermission.hasProjectPermission(null, false, null));
        permissionManager.setDefaultPermission(false);
        assertFalse(currentReporterHasCreatePermission.hasProjectPermission(null, false, null));
    }

    @Test
    public void testIsValidForPermission()
    {
        CurrentReporterHasCreatePermission currentReporterHasCreatePermission = createCurrentReporterHasCreatePermission();
        assertTrue(currentReporterHasCreatePermission.isValidForPermission(Permissions.ASSIGN_ISSUE));
        assertTrue(currentReporterHasCreatePermission.isValidForPermission(Permissions.BROWSE));
        assertTrue(currentReporterHasCreatePermission.isValidForPermission(Permissions.ADMINISTER));

        // Create issue is the only Permission we can't be used in because otherwise we have a circular dependency
        assertFalse(currentReporterHasCreatePermission.isValidForPermission(Permissions.CREATE_ISSUE));
    }

    private CurrentReporterHasCreatePermission createCurrentReporterHasCreatePermission()
    {
        return new CurrentReporterHasCreatePermission(null)
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
