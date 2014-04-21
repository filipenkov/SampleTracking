package com.atlassian.crowd.integration.seraph.v25;

import java.security.Principal;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.atlassian.core.user.UserUtils;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.integration.http.CrowdHttpAuthenticator;
import com.atlassian.seraph.auth.AuthenticatorException;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.atlassian.seraph.auth.LoginReason;
import com.atlassian.seraph.auth.RoleMapper;
import com.atlassian.seraph.config.SecurityConfig;
import com.atlassian.seraph.elevatedsecurity.NoopElevatedSecurityGuard;
import com.atlassian.seraph.filter.BaseLoginFilter;
import com.atlassian.seraph.service.rememberme.RememberMeService;

import com.opensymphony.user.EntityNotFoundException;
import com.opensymphony.user.User;
import com.opensymphony.user.UserManager;
import com.opensymphony.user.provider.ejb.util.Base64;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * CrowdAuthenticator Tester.
 */
public class CrowdAuthenticatorTest
{
    private static final String USER_NAME = "joe";
    private static final String PASSWORD = "password";
    private static final String EMAIL_ADDRESS = "joe@atlassian.com";

    private static final String FULL_NAME = "Joe Smith";

    private static final String LOGGED_IN_PROPERTY = "test.property.for.logged.in.user";
    
    private CrowdAuthenticator authenticator;

    @Mock private CrowdHttpAuthenticator crowdHttpAuthenticator;
    @Mock private RememberMeService rememberMeService;
    @Mock private RoleMapper roleMapper;

    private MockHttpServletRequest httpServletRequest;
    private MockHttpServletResponse httpServletResponse;

    @Mock private com.atlassian.crowd.model.user.User principal;
    private User user;

    @Before
    public void setUp() throws Exception
    {
        initMocks(this);

        user = UserUtils.createUser(USER_NAME, PASSWORD, EMAIL_ADDRESS, FULL_NAME);

        when(principal.getName()).thenReturn(USER_NAME);

        httpServletRequest = new MockHttpServletRequest();
        httpServletResponse = new MockHttpServletResponse();

        authenticator = new CrowdAuthenticator(crowdHttpAuthenticator)
        {
            @Override
            protected RememberMeService getRememberMeService()
            {
                return rememberMeService;
            }

            @Override
            protected RoleMapper getRoleMapper()
            {
                return roleMapper;
            }
            
            protected void logoutUser(HttpServletRequest request)
            {
                HttpSession session = request.getSession();
                session.removeAttribute(LOGGED_IN_PROPERTY);
            }
            
            @Override
            protected Principal getUser(String username)
            {
                try
                {
                    return UserManager.getInstance().getUser(username);
                }
                catch (EntityNotFoundException e)
                {
                    throw new RuntimeException(e);
                }
            }
        };

        SecurityConfig config = Mockito.mock(SecurityConfig.class);
        when(config.getElevatedSecurityGuard()).thenReturn(NoopElevatedSecurityGuard.INSTANCE);
        authenticator.init(new HashMap<String, String>(), config);
    }

    @After
    public void tearDown() throws Exception
    {
        UserUtils.removeUser(UserUtils.getUser(USER_NAME));
    }

    private void verifyUserIsLoggedIn()
    {
        assertNull(httpServletRequest.getSession().getAttribute(DefaultAuthenticator.LOGGED_OUT_KEY));
        assertEquals(user, httpServletRequest.getSession().getAttribute(DefaultAuthenticator.LOGGED_IN_KEY));
    }

    private void verifyUserIsUnauthorised()
    {
        assertNull(httpServletRequest.getSession().getAttribute(DefaultAuthenticator.LOGGED_OUT_KEY));
        assertNull(httpServletRequest.getSession().getAttribute(DefaultAuthenticator.LOGGED_IN_KEY));
        assertEquals(LoginReason.AUTHORISATION_FAILED, httpServletRequest.getAttribute(LoginReason.REQUEST_ATTR_NAME));
    }

    private void verifyUserIsLoggedOut()
    {
        assertEquals(Boolean.TRUE, httpServletRequest.getSession().getAttribute(DefaultAuthenticator.LOGGED_OUT_KEY));
        assertNull(httpServletRequest.getSession().getAttribute(DefaultAuthenticator.LOGGED_IN_KEY));
        assertEquals(LoginReason.OUT, httpServletRequest.getAttribute(LoginReason.REQUEST_ATTR_NAME));
    }

    private void verifyUserIsAnonymous()
    {
        assertNull(httpServletRequest.getSession().getAttribute(DefaultAuthenticator.LOGGED_OUT_KEY));
        assertNull(httpServletRequest.getSession().getAttribute(DefaultAuthenticator.LOGGED_IN_KEY));
        assertNull(httpServletRequest.getAttribute(LoginReason.REQUEST_ATTR_NAME));
    }

    @Test
    public void testRememberMeLoginToCrowdFailsWhenRememberMeServiceFailsToFindUser()
    {
        when(rememberMeService.getRememberMeCookieAuthenticatedUsername(httpServletRequest, httpServletResponse)).thenReturn(null);

        boolean success = authenticator.rememberMeLoginToCrowd(httpServletRequest, httpServletResponse);

        assertFalse(success);

        verifyZeroInteractions(crowdHttpAuthenticator);

        verifyUserIsAnonymous();
    }

    @Test
    public void testRememberMeLoginToCrowdSucceedsWhenRememberMeServiceFindsUser() throws Exception
    {
        when(rememberMeService.getRememberMeCookieAuthenticatedUsername(httpServletRequest, httpServletResponse)).thenReturn(USER_NAME);
        when(roleMapper.canLogin(Matchers.<Principal>anyObject(), eq(httpServletRequest))).thenReturn(true);
        when(crowdHttpAuthenticator.authenticateWithoutValidatingPassword(httpServletRequest, httpServletResponse, USER_NAME)).thenReturn(principal);

        boolean success = authenticator.rememberMeLoginToCrowd(httpServletRequest, httpServletResponse);

        assertTrue(success);

        verifyUserIsLoggedIn();
    }

    @Test
    public void testRememberMeLoginToCrowdFailsWhenRememberMeServiceFindsUnauthorisedUser() throws Exception
    {
        when(rememberMeService.getRememberMeCookieAuthenticatedUsername(httpServletRequest, httpServletResponse)).thenReturn(USER_NAME);
        when(roleMapper.canLogin(Matchers.<Principal>anyObject(), eq(httpServletRequest))).thenReturn(false);

        boolean success = authenticator.rememberMeLoginToCrowd(httpServletRequest, httpServletResponse);

        assertFalse(success);

        verifyUserIsUnauthorised();
    }

    @Test
    public void testRememberMeLoginToCrowdFailsWhenRememberMeServiceFindsInactiveUser() throws Exception
    {
        when(rememberMeService.getRememberMeCookieAuthenticatedUsername(httpServletRequest, httpServletResponse)).thenReturn(USER_NAME);
        when(roleMapper.canLogin(Matchers.<Principal>anyObject(), eq(httpServletRequest))).thenReturn(true);

        doThrow(InvalidAuthenticationException.newInstanceWithName(USER_NAME)).when(crowdHttpAuthenticator).authenticateWithoutValidatingPassword(httpServletRequest, httpServletResponse, USER_NAME);

        boolean success = authenticator.rememberMeLoginToCrowd(httpServletRequest, httpServletResponse);

        assertFalse(success);
        assertNull(httpServletRequest.getSession().getAttribute(DefaultAuthenticator.LOGGED_IN_KEY));
        assertEquals(Boolean.TRUE, httpServletRequest.getSession().getAttribute(DefaultAuthenticator.LOGGED_OUT_KEY));
    }

    @Test
    public void testGetUserWhereUserIsInSessionAsTrustedAppsCall() throws Exception
    {
        // Return true for the trusted apps call so we are 'authenticated'
        httpServletRequest.setAttribute(BaseLoginFilter.OS_AUTHSTATUS_KEY, BaseLoginFilter.LOGIN_SUCCESS);

        httpServletRequest.getSession().setAttribute(DefaultAuthenticator.LOGGED_OUT_KEY, null);
        httpServletRequest.getSession().setAttribute(DefaultAuthenticator.LOGGED_IN_KEY, user);

        final Principal principal = authenticator.getUser(httpServletRequest, httpServletResponse);

        assertEquals(user, principal);
    }

    @Test
    public void testGetUserWhereUserNotInSessionButIsLoggedInViaSSO() throws Exception
    {
        when(crowdHttpAuthenticator.isAuthenticated(httpServletRequest, httpServletResponse)).thenReturn(true);
        when(crowdHttpAuthenticator.getToken(httpServletRequest)).thenReturn("token");
        when(crowdHttpAuthenticator.getUser(httpServletRequest)).thenReturn(principal);
        when(roleMapper.canLogin(Matchers.<Principal>anyObject(), eq(httpServletRequest))).thenReturn(true);

        Principal principal = authenticator.getUser(httpServletRequest, httpServletResponse);

        assertEquals(user, principal);
        assertEquals("token", httpServletRequest.getSession().getAttribute("com.atlassian.crowd.integration.seraph.v25.CrowdAuthenticator#SESSION_TOKEN_KEY"));

        verifyUserIsLoggedIn();
    }

    @Test
    public void testGetUserWhereUserNotInSessionAndIsAlsoNotAuthorisedToLogInViaSSO() throws Exception
    {
        when(crowdHttpAuthenticator.isAuthenticated(httpServletRequest, httpServletResponse)).thenReturn(true);
        when(crowdHttpAuthenticator.getToken(httpServletRequest)).thenReturn("token");
        when(crowdHttpAuthenticator.getUser(httpServletRequest)).thenReturn(principal); 
        when(roleMapper.canLogin(Matchers.<Principal>anyObject(), eq(httpServletRequest))).thenReturn(false);

        Principal principal = authenticator.getUser(httpServletRequest, httpServletResponse);

        assertNull(principal);

        // we do not report this as a log in attempt (as the user may have wanted an anonymous view in JIRA even though they are authenticated in Confluence, for example)
        verifyUserIsAnonymous();
    }

    @Test
    public void testGetUserWithValidSession() throws Exception
    {
        // Session is valid, so return user from the session

        when(crowdHttpAuthenticator.isAuthenticated(httpServletRequest, httpServletResponse)).thenReturn(true);

        httpServletRequest.getSession().setAttribute("com.atlassian.crowd.integration.seraph.v25.CrowdAuthenticator#SESSION_TOKEN_KEY", "token");
        when(crowdHttpAuthenticator.getToken(httpServletRequest)).thenReturn("token");

        httpServletRequest.getSession().setAttribute(DefaultAuthenticator.LOGGED_IN_KEY, user);

        Principal principal = authenticator.getUser(httpServletRequest, httpServletResponse);

        assertEquals(user, principal);
    }


    @Test
    public void testGetUserInSessionWithUnauthenticatedRequest()
    {
        httpServletRequest.getSession();
        final Principal principal = authenticator.getUser(httpServletRequest, httpServletResponse);

        assertNull(principal);

        // if you are not authenticated, then you are logged out
        assertEquals(Boolean.TRUE, httpServletRequest.getSession().getAttribute(DefaultAuthenticator.LOGGED_OUT_KEY));
        assertNull(httpServletRequest.getSession().getAttribute(DefaultAuthenticator.LOGGED_IN_KEY));
    }
    
    @Test
    public void testGetUserNotInSessionWithUnauthenticatedRequest() throws Exception
    {
        final Principal principal = authenticator.getUser(httpServletRequest, httpServletResponse);

        assertNull(principal);

        // No session, not authenticated - getting the user should not result in logging out, nor in the creation of a session
        verify(crowdHttpAuthenticator, never()).logout(httpServletRequest, httpServletResponse);
        assertNull(httpServletRequest.getSession(false));
    }    


    @Test
    public void testLoginSuccess() throws Exception
    {
        when(roleMapper.canLogin(Matchers.<Principal>anyObject(), eq(httpServletRequest))).thenReturn(true);
        when(crowdHttpAuthenticator.authenticate(httpServletRequest, httpServletResponse, USER_NAME, PASSWORD)).thenReturn(principal);

        boolean authenticated = authenticator.login(httpServletRequest, httpServletResponse, USER_NAME, PASSWORD);

        assertTrue(authenticated);

        verify(crowdHttpAuthenticator).logout(httpServletRequest, httpServletResponse);

        verifyUserIsLoggedIn();
    }

    @Test
    public void testLoginAuthenticationFailure() throws Exception
    {
        doThrow(InvalidAuthenticationException.newInstanceWithName(USER_NAME)).when(crowdHttpAuthenticator).authenticate(httpServletRequest, httpServletResponse, USER_NAME, PASSWORD);

        boolean authenticated = authenticator.login(httpServletRequest, httpServletResponse, USER_NAME, PASSWORD);

        assertFalse(authenticated);

        verify(crowdHttpAuthenticator).logout(httpServletRequest, httpServletResponse);

        assertNull(httpServletRequest.getSession().getAttribute(DefaultAuthenticator.LOGGED_IN_KEY));
        assertEquals(Boolean.TRUE, httpServletRequest.getSession().getAttribute(DefaultAuthenticator.LOGGED_OUT_KEY));
    }

    @Test
    public void testLogout() throws Exception
    {
        httpServletRequest.getSession().setAttribute(LOGGED_IN_PROPERTY, "present");
        
        boolean success = authenticator.logout(httpServletRequest, httpServletResponse);

        assertTrue(success);

        verify(crowdHttpAuthenticator).logout(httpServletRequest, httpServletResponse);

        // Assert the JIRA attribute is not present in the session, since this is performed by the jira subclass'd authenticator
        assertNull(httpServletRequest.getSession().getAttribute(LOGGED_IN_PROPERTY));

        verifyUserIsLoggedOut();
    }

    // THE FOLLOWING TESTS HAVE BEEN RETAINED FROM THE V2.1 CROWD-SERAPH CONNECTOR

    @Test
    public void testIsAuthenticatedWithAutoLogin() throws Exception
    {
        // We don't have a crowd token
        when(crowdHttpAuthenticator.isAuthenticated(httpServletRequest, httpServletResponse)).thenReturn(false);

        when(rememberMeService.getRememberMeCookieAuthenticatedUsername(httpServletRequest, httpServletResponse)).thenReturn(USER_NAME);
        when(roleMapper.canLogin(Matchers.<Principal>anyObject(), eq(httpServletRequest))).thenReturn(true);
        when(crowdHttpAuthenticator.authenticateWithoutValidatingPassword(httpServletRequest, httpServletResponse, USER_NAME)).thenReturn(principal);

        final boolean authenticated = authenticator.isAuthenticated(httpServletRequest, httpServletResponse);

        assertTrue(authenticated);
    }

    @Test
    public void testIsAuthenticatedWithBasicAuth() throws Exception
    {
        // We don't have a crowd token
        when(crowdHttpAuthenticator.isAuthenticated(httpServletRequest, httpServletResponse)).thenReturn(false);

        when(roleMapper.canLogin(Matchers.<Principal>anyObject(), eq(httpServletRequest))).thenReturn(true);

        when(crowdHttpAuthenticator.authenticate(httpServletRequest, httpServletResponse, USER_NAME, PASSWORD)).thenReturn(principal);

        // Try basic Auth
        httpServletRequest.setQueryString("?os_authType=basic");

        String base64auth = new String(Base64.encode((USER_NAME + ":" + PASSWORD).getBytes()));

        httpServletRequest.addHeader("Authorization", "Basic " + base64auth);

        // Make the basic auth authentication
        final boolean authenticated = authenticator.isAuthenticated(httpServletRequest, httpServletResponse);

        assertTrue(authenticated);
    }


    @Test
    public void testIsAuthenticatedWithCrowd() throws Exception
    {
        // We have a valid crowd token
        when(crowdHttpAuthenticator.isAuthenticated(httpServletRequest, httpServletResponse)).thenReturn(true);

        final boolean authenticated = authenticator.isAuthenticated(httpServletRequest, httpServletResponse);

        assertTrue(authenticated);
    }

    @Test
    public void testIsAuthenticatedWithTrustedApps()
    {
        // We have a trusted apps request
        httpServletRequest.setAttribute(BaseLoginFilter.OS_AUTHSTATUS_KEY, BaseLoginFilter.LOGIN_SUCCESS);

        final boolean authenticated = authenticator.isAuthenticated(httpServletRequest, httpServletResponse);

        assertTrue(authenticated);
    }

    @Test
    public void testIsAuthenticatedWithUnAuthenticatedRequestAndWithSession()
    {
        httpServletRequest.getSession();
        final boolean authenticated = authenticator.isAuthenticated(httpServletRequest, httpServletResponse);

        assertFalse(authenticated);
        assertNull(httpServletRequest.getSession().getAttribute(DefaultAuthenticator.LOGGED_IN_KEY));
        assertTrue((Boolean) httpServletRequest.getSession().getAttribute(DefaultAuthenticator.LOGGED_OUT_KEY));
    }
    
}