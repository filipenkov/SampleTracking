package com.atlassian.jira.sharing.type;

import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.user.MockCrowdService;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharePermissionImpl;
import com.atlassian.jira.sharing.type.ShareType.Name;

import com.opensymphony.user.User;

/**
 * Test for {@link com.atlassian.jira.sharing.type.GlobalShareTypePermissionChecker}.
 * 
 * @since v3.13
 */
public class TestGlobalShareTypePermissionChecker extends ListeningTestCase
{
    protected User user;
    private static final SharePermission GLOBAL_PERM = new SharePermissionImpl(GlobalShareType.TYPE, null, null);
    private static final SharePermission INVALID_PERM = new SharePermissionImpl(new Name("group"), "developers", null);

    @Before
    public void setUp() throws Exception
    {
        final MockProviderAccessor mpa = new MockProviderAccessor();
        user = new User("admin", mpa, new MockCrowdService());
    }

    @After
    public void tearDown() throws Exception
    {
        user = null;
    }

    @Test
    public void testHasPermissionValidSharePermission()
    {
        final ShareTypePermissionChecker validator = new GlobalShareTypePermissionChecker();
        assertTrue(validator.hasPermission(user, TestGlobalShareTypePermissionChecker.GLOBAL_PERM));

    }

    @Test
    public void testHasPermissionValidSharePermissionAndNullUser()
    {
        final ShareTypePermissionChecker validator = new GlobalShareTypePermissionChecker();
        assertTrue(validator.hasPermission(null, TestGlobalShareTypePermissionChecker.GLOBAL_PERM));
    }

    @Test
    public void testHasPermissionInvalidShareType()
    {
        final ShareTypePermissionChecker validator = new GlobalShareTypePermissionChecker();

        try
        {
            validator.hasPermission(user, TestGlobalShareTypePermissionChecker.INVALID_PERM);
            fail("Permission checker should only accept global permissions.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    @Test
    public void testHasPermissionNullPermission()
    {
        final GlobalShareTypePermissionChecker validator = new GlobalShareTypePermissionChecker();

        try
        {
            validator.hasPermission(user, null);
            fail("hasPermission should not accept null permission");
        }
        catch (final IllegalArgumentException e)
        {
            // expected
        }
    }
}
