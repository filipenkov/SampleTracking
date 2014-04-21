package com.atlassian.jira.bc.security.login;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.user.MockCrowdService;
import com.atlassian.jira.user.util.MockUserManager;
import com.atlassian.jira.user.util.UserManager;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.login.LoginManager;
import com.opensymphony.user.User;

import javax.servlet.http.HttpServletRequest;

/**
 */
public class TestLoginServiceImpl extends MockControllerTestCase
{
    private LoginManager loginManager;
    private User user;

    @Before
    public void setUp() throws Exception
    {
        loginManager = getMock(LoginManager.class);
        user = new User("username", new MockProviderAccessor(), new MockCrowdService());
    }

    @Test
    public void testGetLoginInfo()
    {
        LoginInfo loginInfo = new LoginInfoImpl(1L, 2L, 3L, 4L, 5L, 6L, 7L, true);

        expect(loginManager.getLoginInfo("userName")).andReturn(loginInfo);

        final LoginService loginService = instantiate(LoginServiceImpl.class);
        final LoginInfo actualLoginInfo = loginService.getLoginInfo("userName");
        assertSame(loginInfo, actualLoginInfo);
    }

    @Test
    public void testIsElevatedSecurityCheckAlwaysShown()
    {
        expect(loginManager.isElevatedSecurityCheckAlwaysShown()).andReturn(true);

        final LoginService loginService = instantiate(LoginServiceImpl.class);
        assertTrue(loginService.isElevatedSecurityCheckAlwaysShown());
    }


    @Test
    public void testResetFailedLoginCount()
    {
        loginManager.resetFailedLoginCount(user);
        expectLastCall();

        final LoginService loginService = instantiate(LoginServiceImpl.class);
        loginService.resetFailedLoginCount(user);
    }

    @Test
    public void testAuthenticate()
    {
        LoginResult loginResult = new LoginResultImpl(LoginReason.OK, null, "username");
        expect(loginManager.authenticate(user, "password")).andReturn(loginResult);

        final LoginService loginService = instantiate(LoginServiceImpl.class);
        assertSame(loginResult, loginService.authenticate(user, "password"));

    }

    @Test
    public void testGetLoginPropertiesFail()
    {
        final LoginService loginService = instantiate(LoginServiceImpl.class);
        try
        {
            LoginProperties loginProperties = loginService.getLoginProperties(null, null);
            fail("Should have thrown exception due to request being null");
        }
        catch (Exception e)
        {
            //awesome!
        }
    }
    
    @Test
    public void testGetLoginProperties()
    {
        _testLoginProperties(false, false, false, false);
    }

    @Test
    public void testGetLoginPropertiesCookies()
    {
        _testLoginProperties(true, false, false, false);
    }

    @Test
    public void testGetLoginPropertiesExternalUser()
    {
        _testLoginProperties(false, true, false, false);
    }

    @Test
    public void testGetLoginPropertiesElevatedSecurityCheck()
    {
        _testLoginProperties(false, false, true, false);
    }

    @Test
    public void testGetLoginPropertiesPublic()
    {
        _testLoginProperties(false, false, false, true);
    }

    private void _testLoginProperties(boolean allowCookies, boolean externalUser, boolean elevatedSecurityCheck, final boolean isPublic)
    {
        final HttpServletRequest mockRequest = getMock(HttpServletRequest.class);
        final ApplicationProperties applicationProperties = getMock(ApplicationProperties.class);
        final UserManager userManager = new MockUserManager();
        expect(applicationProperties.getOption("jira.option.allowcookies")).andReturn(allowCookies);
        expect(applicationProperties.getOption("jira.option.user.externalmanagement")).andReturn(externalUser);
        expect(mockRequest.getAttribute("com.atlassian.jira.security.login.LoginManager.LoginResult")).andReturn(new LoginResultImpl(null, null, null));
        expect(mockRequest.getAttribute("os_authstatus")).andReturn(null);
        expect(mockRequest.getAttribute("auth_error_type")).andReturn(null);
        expect(loginManager.isElevatedSecurityCheckAlwaysShown()).andReturn(elevatedSecurityCheck);

        replay(applicationProperties, mockRequest, loginManager);
        final LoginService loginService = new LoginServiceImpl(loginManager, applicationProperties, userManager)
        {
            @Override
            boolean isPublicMode()
            {
                return isPublic;
            }
        };
        final LoginProperties loginProperties = loginService.getLoginProperties(null, mockRequest);
        assertFalse(loginProperties.getLoginFailedByPermissions());
        assertFalse(loginProperties.isCaptchaFailure());
        assertFalse(loginProperties.isLoginSucceeded());

        if(allowCookies)
        {
            assertTrue(loginProperties.isAllowCookies());
        }
        else
        {
            assertFalse(loginProperties.isAllowCookies());
        }
        if(externalUser)
        {
            assertTrue(loginProperties.isExternalUserManagement());
        }
        else
        {
            assertFalse(loginProperties.isExternalUserManagement());
        }
        if(elevatedSecurityCheck)
        {
            assertTrue(loginProperties.isElevatedSecurityCheckShown());
        }
        else
        {
            assertFalse(loginProperties.isElevatedSecurityCheckShown());
        }
        if(isPublic)
        {
            assertTrue(loginProperties.isPublicMode());
        }
        else
        {
            assertFalse(loginProperties.isPublicMode());
        }

        verify(applicationProperties, mockRequest, loginManager);
    }
}
