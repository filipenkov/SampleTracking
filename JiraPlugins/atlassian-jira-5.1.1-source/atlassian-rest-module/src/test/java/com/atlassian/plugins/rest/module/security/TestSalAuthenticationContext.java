package com.atlassian.plugins.rest.module.security;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.plugins.rest.module.servlet.ServletUtils;

import javax.servlet.http.HttpServletRequest;

public class TestSalAuthenticationContext
{
    private SalAuthenticationContext salAuthenticationContext;
    @Mock
    private UserManager mockUserManager;
    @Mock
    private HttpServletRequest request;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        salAuthenticationContext = new SalAuthenticationContext(mockUserManager);
        ServletUtils.setHttpServletRequest(request);
    }

    @Test
    public void isAuthenticatedTrue()
    {
        when(mockUserManager.getRemoteUsername(request)).thenReturn("dusan");
        assertTrue(salAuthenticationContext.isAuthenticated());
    }

    @Test
    public void isAuthenticatedFalse()
    {
        assertFalse(salAuthenticationContext.isAuthenticated());
    }

    @Test
    public void getPrincipal()
    {
        when(mockUserManager.getRemoteUsername(request)).thenReturn("dusan");
        assertEquals("dusan", salAuthenticationContext.getPrincipal().getName());
    }

    @Test
    public void getPrincipalNone()
    {
        assertNull(salAuthenticationContext.getPrincipal());
    }


}
