package com.atlassian.crowd.plugin.rest.service.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link com.atlassian.crowd.plugin.rest.service.util.AuthenticatedApplicationUtil}.
 */
public class AuthenticatedApplicationUtilTest
{
    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() throws Exception
    {
        request = null;
    }

    @Test
    public void testGetAuthenticatedApplication() throws Exception
    {
        final String APPLICATION_NAME = "applicationName";
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(anyString())).thenReturn(APPLICATION_NAME);
        String appName = AuthenticatedApplicationUtil.getAuthenticatedApplication(request);
        assertEquals(APPLICATION_NAME, appName);
    }

    @Test
    public void testGetAuthenticatedApplication_NoSession() throws Exception
    {
        when(request.getSession(false)).thenReturn(null);
        assertNull(AuthenticatedApplicationUtil.getAuthenticatedApplication(request));
    }

    @Test
    public void testGetAuthenticatedApplication_NotName() throws Exception
    {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(anyString())).thenReturn(null);
        assertNull(AuthenticatedApplicationUtil.getAuthenticatedApplication(request));
    }

    @Test
    public void testSetAuthenticatedApplication() throws Exception
    {
        final String APPLICATION_NAME = "applicationName";
        when(request.getSession()).thenReturn(session);
        AuthenticatedApplicationUtil.setAuthenticatedApplication(request, APPLICATION_NAME);
        verify(session).setAttribute(anyString(), eq(APPLICATION_NAME));
    }
}
