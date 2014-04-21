package com.atlassian.jira.security;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.login.LoginManager;
import com.atlassian.jira.user.MockUser;

import javax.servlet.http.HttpServletRequest;

public class TestJiraRoleMapper extends MockControllerTestCase
{
    private HttpServletRequest httpServletRequest;
    private PermissionManager permissionManager;
    private LoginManager loginManager;
    private JiraRoleMapper jiraRoleMapper;
    private User fred;

    @Before
    public void setUp() throws Exception
    {
        fred = new MockUser("fred");
        httpServletRequest = getMock(HttpServletRequest.class);
        permissionManager = getMock(PermissionManager.class);
        loginManager = getMock(LoginManager.class);
        jiraRoleMapper = new JiraRoleMapper()
        {
            @Override
            LoginManager getLoginManager()
            {
                return loginManager;
            }

            @Override
            PermissionManager getPermissionManager()
            {
                return permissionManager;
            }
        };
    }

    @Test
    public void testHasRole_FAIL()
    {
        expect(permissionManager.hasPermission(Permissions.USE, fred)).andReturn(false);
        replay();
        assertFalse(jiraRoleMapper.hasRole(fred, httpServletRequest, "use"));
    }

    @Test
    public void testHasRole_FAIL_RubbishRoleName()
    {
        replay();

        try
        {
            jiraRoleMapper.hasRole(fred, httpServletRequest, "rubbish");
            fail("how can we cope with rubbish?");
        }
        catch (IllegalArgumentException expected)
        {
        }
    }

    @Test
    public void testHasRole_OK()
    {
        expect(permissionManager.hasPermission(Permissions.USE, fred)).andReturn(true);
        replay();
        assertTrue(jiraRoleMapper.hasRole(fred, httpServletRequest, "use"));
    }

    @Test
    public void testCanLogin_FAIL()
    {
        expect(loginManager.authorise(fred, httpServletRequest)).andReturn(false);
        replay();
        assertFalse(jiraRoleMapper.canLogin(fred, httpServletRequest));
    }

    @Test
    public void testCanLogin_OK()
    {
        expect(loginManager.authorise(fred, httpServletRequest)).andReturn(true);
        replay();
        assertTrue(jiraRoleMapper.canLogin(fred, httpServletRequest));
    }
}
