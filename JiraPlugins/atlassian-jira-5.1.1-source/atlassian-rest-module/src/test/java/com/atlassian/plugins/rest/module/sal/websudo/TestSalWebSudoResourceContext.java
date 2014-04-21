package com.atlassian.plugins.rest.module.sal.websudo;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.atlassian.plugins.rest.common.sal.websudo.WebSudoResourceContext;
import com.atlassian.plugins.rest.module.servlet.ServletUtils;
import com.atlassian.sal.api.websudo.WebSudoManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;

import javax.servlet.http.HttpServletRequest;

public final class TestSalWebSudoResourceContext
{
    @Mock private WebSudoManager webSudoManager;
    @Mock private HttpServletRequest request;
    
    private WebSudoResourceContext webSudoResourceContext;

    @Before
    public void setUp()
    {
        initMocks(this);
        webSudoResourceContext = new SalWebSudoResourceContext(webSudoManager);
        ServletUtils.setHttpServletRequest(request);
    }

    @After
    public void teardown()
    {
        webSudoManager = null;
        request = null;
        webSudoResourceContext = null;
    }

    @Test
    public void enforceWebSudoProtection()
    {
        when(webSudoManager.canExecuteRequest(request)).thenReturn(false);
        assertTrue(webSudoResourceContext.shouldEnforceWebSudoProtection());
    }

    @Test
    public void dontEnforceWebSudoProtection()
    {
        when(webSudoManager.canExecuteRequest(request)).thenReturn(true);
        assertFalse(webSudoResourceContext.shouldEnforceWebSudoProtection());
    }

    @Test
    public void basicAuthDoesNotRequireWebSudo()
    {
        // From http://www.ietf.org/rfc/rfc2617.txt
        when(request.getHeader("Authorization")).thenReturn("Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==");

        assertFalse(webSudoResourceContext.shouldEnforceWebSudoProtection());
        verify(webSudoManager, never()).canExecuteRequest(Matchers.<HttpServletRequest>anyObject());
    }
}