/**
 * Atlassian Source Code Template.
 * User: Bobby
 * Date: Apr 10, 2003
 * Time: 2:45:30 PM
 * CVS Revision: $Revision: 1.2 $
 * Last CVS Commit: $Date: 2006/10/09 01:01:38 $
 * Author of last CVS Commit: $Author: bkuo $
 */

package com.atlassian.johnson.filters;

import com.atlassian.johnson.JohnsonEventContainer;
import com.atlassian.johnson.config.JohnsonConfig;
import com.atlassian.johnson.event.Event;
import com.atlassian.johnson.event.EventType;
import com.mockobjects.constraint.IsAnything;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.C;
import com.mockobjects.dynamic.Mock;
import junit.framework.TestCase;
import mock.MockSetupConfig;
import org.easymock.MockControl;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

public class TestJohnsonFilter extends TestCase
{
    JohnsonFilter filter;
    private Mock mockResp;
    private Mock mockFilterChain;
    private Mock mockFilterConfig;
    private Mock mockServletContext;
    private MockControl requestMockCtrl;
    private JohnsonEventContainer container;
    HttpServletRequest request;
    HttpServletResponse response;
    FilterChain filterChain;

    public TestJohnsonFilter(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        MockSetupConfig.IS_SETUP = false;
        JohnsonConfig.setInstance(new JohnsonConfig("test-johnson-config.xml"));
        filter = new JohnsonFilter();
        container = new JohnsonEventContainer();
        request = getRequest();
        response = getResponse();
        filterChain = getFilterChain();
    }

    protected void tearDown() throws Exception
    {
        JohnsonConfig.setInstance(null);
        super.tearDown();
    }

    public void testErrorNotIgnorableURI() throws Exception
    {
        container.addEvent(new Event(EventType.get("database"), "foo")); // add an event
        request.getServletPath();
        requestMockCtrl.setReturnValue("somepage.jsp", 2);
        requestMockCtrl.replay();
        mockResp.expect("sendRedirect", C.args(new IsEqual("/the/error/path.jsp"))); // verify redirect
        filter.doFilter(request, response, filterChain);
        verifyAll();
    }

    public void testErrorsIgnorableUri() throws Exception
    {
        container.addEvent(new Event(EventType.get("export"), "foo")); // add an event
        request.getServletPath();
        requestMockCtrl.setReturnValue("/the/error/path.jsp");
        requestMockCtrl.replay();
        mockFilterChain.expect("doFilter", C.args(new IsAnything(), new IsAnything())); // verify that doFilter is called

        filter.doFilter(request, response, filterChain);
        verifyAll();
    }

    public void testNoErrorsAndNotSetup() throws Exception
    {
        MockSetupConfig.IS_SETUP = false;
        request.getServletPath();
        requestMockCtrl.setReturnValue("somepage.jsp", 2);
        requestMockCtrl.replay();
        mockResp.expect("sendRedirect", C.args(new IsEqual("/the/setup/path.jsp"))); // verify redirect

        filter.doFilter(request, response, filterChain);
        verifyAll();
    }

    public void testNoErrorsSetup() throws Exception
    {
        MockSetupConfig.IS_SETUP = true;
        request.getServletPath();
        requestMockCtrl.setReturnValue("somepage.jsp");
        requestMockCtrl.replay();
        mockFilterChain.expect("doFilter", C.args(new IsAnything(), new IsAnything())); // verify that doFilter is called

        filter.doFilter(request, response, filterChain);
        verifyAll();
    }

    public void testNoErrorsSetupButSetupURI() throws Exception
    {
        MockSetupConfig.IS_SETUP = true;
        request.getServletPath();
        requestMockCtrl.setReturnValue("/setuppage.jsp");
        requestMockCtrl.replay();
        mockFilterChain.expect("doFilter", C.args(new IsAnything(), new IsAnything())); // verify that doFilter is called

        filter.doFilter(request, response, filterChain);
        verifyAll();
    }

    public void testNoErrorsNotSetupButSetupURI() throws Exception
    {
        MockSetupConfig.IS_SETUP = false;
        request.getServletPath();
        requestMockCtrl.setReturnValue("/setup.jsp"); // setup URI as containing 'setup'
        requestMockCtrl.replay();
        mockFilterChain.expect("doFilter", C.args(new IsAnything(), new IsAnything())); // verify that doFilter is called

        filter.doFilter(request, response, filterChain);
        verifyAll();
    }

    public void testNoErrorsNotSetupIgnorableURI() throws Exception
    {
        MockSetupConfig.IS_SETUP = false;
        request.getServletPath();
        requestMockCtrl.setReturnValue("/ignore/path/1.jsp"); // setup URI as ignorable
        requestMockCtrl.replay();
        mockFilterChain.expect("doFilter", C.args(new IsAnything(), new IsAnything())); // verify that doFilter is called

        filter.doFilter(request, response, filterChain);
        verifyAll();
    }

    public void testNoErrorsNotSetupNotIgnorableURI() throws Exception
    {
        MockSetupConfig.IS_SETUP = false;
        request.getServletPath();
        requestMockCtrl.setReturnValue("/somepage.jsp", 2); // setup URI as not ignorable
        requestMockCtrl.replay();
        mockResp.expect("sendRedirect", C.args(new IsEqual("/the/setup/path.jsp"))); // verify redirect

        filter.doFilter(request, response, filterChain);
        verifyAll();
    }

    /**
     * Check that the app consistency filter is not applied twice to the same request.
     */
    public void testNotAppliedTwice() throws ServletException, IOException
    {
        request.getServletPath();
        requestMockCtrl.setReturnValue("/somepage.jsp", 2);
        requestMockCtrl.replay();
        mockResp.expect("sendRedirect", C.args(new IsEqual("/the/setup/path.jsp"))); // verify redirect

        filter.doFilter(request, response, filterChain);
        verifyAll();

        requestMockCtrl.reset();
        request.getAttribute(JohnsonFilter.ALREADY_FILTERED);
        requestMockCtrl.setReturnValue(Boolean.TRUE);
        requestMockCtrl.replay();

        mockFilterChain.expect("doFilter", C.args(new IsAnything(), new IsAnything())); // verify that doFilter is called

        filter.doFilter(request, response, filterChain);
        verifyAll();
    }

    /**
     * Check that the app consistency filter is not applied twice to the same request.
     */
    public void testEmptyServletPaths() throws ServletException, IOException
    {
        requestMockCtrl.reset();
        request.getServletPath();
        requestMockCtrl.setReturnValue("", 0, 10); // don't care how many calls
        request.getContextPath();
        requestMockCtrl.setReturnValue("", 0, 10);
        request.getRequestURI();
        requestMockCtrl.setReturnValue("/requestpage.jsp", 0, 10);
        request.getPathInfo();
        requestMockCtrl.setReturnValue(null, 0, 10);
        requestMockCtrl.replay();
        mockResp.expect("sendRedirect", C.args(new IsEqual("/the/setup/path.jsp"))); // verify redirect

        filter.doFilter(request, response, filterChain);
        verifyAll();
    }

    public void testGetStringForEvents() {
        JohnsonFilter filter = new JohnsonFilter();
        EventType type = new EventType("mytype", "mydesc");
        Event[] eventsArr = new Event[]{new Event(type, "Error 1"), new Event(type, "Error 2")};
        String stringForEvents = filter.getStringForEvents(Arrays.asList(eventsArr));
        assertEquals("Error 1\nError 2", stringForEvents);
    }

    private FilterChain getFilterChain() throws ServletException
    {
        mockFilterChain = new Mock(FilterChain.class);
        FilterChain filterChain = (FilterChain) mockFilterChain.proxy();

        mockFilterConfig = new Mock(FilterConfig.class);
        FilterConfig filterConfig = (FilterConfig) mockFilterConfig.proxy();

        mockServletContext = new Mock(ServletContext.class);
        ServletContext servletContext = (ServletContext) mockServletContext.proxy();

        mockFilterConfig.expectAndReturn("getServletContext", servletContext);
        container = new JohnsonEventContainer();
        mockServletContext.matchAndReturn("getAttribute", C.args(new IsEqual(JohnsonEventContainer.SERVLET_CONTEXT_KEY)), container);

        filter.init(filterConfig);
        return filterChain;
    }

    private HttpServletResponse getResponse()
    {
        mockResp = new Mock(HttpServletResponse.class);
        HttpServletResponse response = (HttpServletResponse) mockResp.proxy();
        return response;
    }

    private HttpServletRequest getRequest()
    {
        requestMockCtrl = MockControl.createNiceControl(HttpServletRequest.class);

        HttpServletRequest request = (HttpServletRequest) requestMockCtrl.getMock();
        request.getAttribute(JohnsonFilter.ALREADY_FILTERED);
        requestMockCtrl.expectAndReturn(JohnsonFilter.ALREADY_FILTERED, null);
        request.setAttribute(JohnsonFilter.ALREADY_FILTERED, Boolean.TRUE);
        requestMockCtrl.setVoidCallable();
        request.getContextPath();
        requestMockCtrl.setReturnValue("", 0, 1);

        return request;
    }

    private void verifyAll()
    {
        requestMockCtrl.verify();
        mockResp.verify();
        mockFilterChain.verify();
        mockFilterConfig.verify();
        mockServletContext.verify();
    }

}
