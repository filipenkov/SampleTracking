package com.atlassian.jira.web.filters;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @since v4.0
 */
public class TestJiraCachingStrategy extends MockControllerTestCase
{
    private JiraCachingFilter.JiraCachingStrategy strategy;
    private HttpServletRequest servletRequest;

    @Before
    public void setUp() throws Exception
    {
        strategy = new JiraCachingFilter.JiraCachingStrategy();
        servletRequest = mockController.getMock(HttpServletRequest.class);
    }

    //JSPA should not be cached.
    @Test
    public void testMatchesJSPA()
    {
        servletRequest.getServletPath();
        mockController.setReturnValue("/secure/Dashboard.jspa");
        servletRequest.isSecure();
        mockController.setReturnValue(false);
        servletRequest.getRequestURI();
        mockController.setReturnValue("/secure/Dashboard.jspa");

        mockController.replay();

        final boolean nonCachableUri = strategy.matches(servletRequest);
        assertTrue(nonCachableUri);
    }

    //JSP should also not be cached.
    @Test
    public void testMatchesJSP()
    {
        servletRequest.getServletPath();
        mockController.setReturnValue("/secure/viewissue.jsp");
        servletRequest.isSecure();
        mockController.setReturnValue(false);
        servletRequest.getRequestURI();
        mockController.setReturnValue("/secure/viewissue.jsp");

        mockController.replay();

        final boolean nonCachableUriJsp = strategy.matches(servletRequest);
        assertTrue(nonCachableUriJsp);
    }

    //attachments over secure should not add no-cache headers
    @Test
    public void testMatchesAttachmentSecure()
    {
        servletRequest.getServletPath();
        mockController.setReturnValue("/attachment/");
        servletRequest.isSecure();
        mockController.setReturnValue(true);

        mockController.replay();

        final boolean nonCachableUriAttachmentSecure = strategy.matches(servletRequest);
        assertFalse(nonCachableUriAttachmentSecure);
    }

    //attachments over non-secure should not add no cache headers
    @Test
    public void testMatchesAttachment()
    {
        servletRequest.getServletPath();
        mockController.setReturnValue("/attachment/");
        servletRequest.isSecure();
        mockController.setReturnValue(false);
        servletRequest.getRequestURI();
        mockController.setReturnValue("/attachment/");

        mockController.replay();

        final boolean nonCachableUriAttachment = strategy.matches(servletRequest);
        assertFalse(nonCachableUriAttachment);
    }

    //lazyLoader should add no cache headers
    @Test
    public void testMatchesLazyLoader()
    {
        servletRequest.getServletPath();
        mockController.setReturnValue("/lazyLoader");
        servletRequest.isSecure();
        mockController.setReturnValue(false);

        mockController.replay();

        final boolean nonCachableUricached = strategy.matches(servletRequest);
        assertTrue(nonCachableUricached);
    }

    //Browse should add no-cache headers
    @Test
    public void testMatchesBrowse()
    {
        servletRequest.getServletPath();
        mockController.setReturnValue("/browse/JRA-123123");
        servletRequest.isSecure();
        mockController.setReturnValue(false);

        mockController.replay();

        final boolean nonCachableUriBrowse = strategy.matches(servletRequest);
        assertTrue(nonCachableUriBrowse);
    }

     //Browse should add no-cache headers
    @Test
    public void testMatchesOther()
    {
        servletRequest.getServletPath();
        mockController.setReturnValue("/someotherurl");
        servletRequest.isSecure();
        mockController.setReturnValue(false);
        servletRequest.getRequestURI();
        mockController.setReturnValue("/someotherurl");

        mockController.replay();

        final boolean nonCachableUriBrowse = strategy.matches(servletRequest);
        assertFalse(nonCachableUriBrowse);
    }
}
