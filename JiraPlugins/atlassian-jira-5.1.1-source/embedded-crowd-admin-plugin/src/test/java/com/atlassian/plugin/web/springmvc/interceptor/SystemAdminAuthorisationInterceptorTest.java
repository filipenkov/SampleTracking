package com.atlassian.plugin.web.springmvc.interceptor;

import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.net.URI;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SystemAdminAuthorisationInterceptorTest
{
    @Mock
    private UserManager userManager;
    @Mock
    private LoginUriProvider loginUriProvider;
    @Mock
    private ApplicationProperties applicationProperties;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpSession session;
    @Mock
    private HttpServletResponse response;

    private SystemAdminAuthorisationInterceptor interceptor;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        when(request.getSession()).thenReturn(session);
        when(request.getRequestURI()).thenReturn("/confluence/plugins/servlet/embedded-crowd/directories/list");
        when(request.getContextPath()).thenReturn("/confluence");
        when(userManager.getRemoteUsername(request)).thenReturn("username");

        interceptor = new SystemAdminAuthorisationInterceptor(userManager, loginUriProvider, applicationProperties);
    }

    @Test
    public void testAbsoluteLoginRedirectUrl() throws Exception
    {
        when(userManager.isSystemAdmin("username")).thenReturn(false);
        when(applicationProperties.getBaseUrl()).thenReturn("http://example.com:8080/confluence/");
        when(loginUriProvider.getLoginUri(Matchers.<URI>any())).thenReturn(new URI("http://example.com:8080/confluence/login.action?os_destination=foo"));

        boolean result = interceptor.preHandle(request, response, null);

        assertFalse(result);
        verify(session).setAttribute("seraph_originalurl", "/plugins/servlet/embedded-crowd/directories/list");
        verify(response).sendRedirect("/confluence/login.action?os_destination=foo");
    }

    @Test
    public void testRelativeLoginRedirect() throws Exception
    {
        when(userManager.isSystemAdmin("username")).thenReturn(false);
        when(applicationProperties.getBaseUrl()).thenReturn("http://example.com:8080/confluence/");
        when(loginUriProvider.getLoginUri(Matchers.<URI>any())).thenReturn(new URI("/confluence/login.action?os_destination=foo"));

        boolean result = interceptor.preHandle(request, response, null);

        assertFalse(result);
        verify(session).setAttribute("seraph_originalurl", "/plugins/servlet/embedded-crowd/directories/list");
        verify(response).sendRedirect("/confluence/login.action?os_destination=foo");
    }

    @Test
    public void testLoginRedirectUrlHandlesBaseUrlWithoutTrailingSlash() throws Exception
    {
        when(userManager.isSystemAdmin("username")).thenReturn(false);
        when(applicationProperties.getBaseUrl()).thenReturn("http://example.com:8080/confluence"); // <-- no trailing slash
        when(loginUriProvider.getLoginUri(Matchers.<URI>any())).thenReturn(new URI("http://example.com:8080/confluence/login.action?os_destination=foo"));

        boolean result = interceptor.preHandle(request, response, null);

        assertFalse(result);
        verify(session).setAttribute("seraph_originalurl", "/plugins/servlet/embedded-crowd/directories/list");
        verify(response).sendRedirect("/confluence/login.action?os_destination=foo");
    }

    @Test
    public void testSuccessfulPermissionCheck() throws Exception
    {
        when(userManager.isSystemAdmin("username")).thenReturn(true);

        boolean result = interceptor.preHandle(request, response, null);

        assertTrue(result);
        verifyZeroInteractions(response);
    }
}
