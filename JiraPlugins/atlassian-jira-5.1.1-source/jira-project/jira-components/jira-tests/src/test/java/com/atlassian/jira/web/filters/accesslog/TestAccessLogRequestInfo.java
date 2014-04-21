package com.atlassian.jira.web.filters.accesslog;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.util.log.Log4jKit;
import mock.servlet.MockHttpServletRequest;
import mock.servlet.MockHttpServletResponse;
import org.apache.log4j.MDC;

import java.util.Hashtable;

/**
 */
public class TestAccessLogRequestInfo extends ListeningTestCase
{
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @Before
    public void setUp() throws Exception
    {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }


    @Test
    public void testCanBeCalledTwice()
    {
        assertEquals(0L, AccessLogRequestInfo.concurrentRequests.get());
        assertNull(request.getAttribute(AccessLogRequestInfo.JIRA_REQUEST_START_MILLIS));
        assertNull(request.getAttribute(AccessLogRequestInfo.JIRA_REQUEST_ID));

        final AccessLogRequestInfo accessLogRequestInfo1 = new AccessLogRequestInfo();
        accessLogRequestInfo1.enterRequest(request, response);

        final Object originalStartTime = request.getAttribute(AccessLogRequestInfo.JIRA_REQUEST_START_MILLIS);
        assertNotNull(originalStartTime);
        assertNotNull(request.getAttribute(AccessLogRequestInfo.JIRA_REQUEST_ID));
        assertEquals(1L, AccessLogRequestInfo.concurrentRequests.get());


        final AccessLogRequestInfo accessLogRequestInfo2 = new AccessLogRequestInfo();
        accessLogRequestInfo2.enterRequest(request, response);

        final Object secondStartTime = request.getAttribute(AccessLogRequestInfo.JIRA_REQUEST_START_MILLIS);
        assertSame(originalStartTime, secondStartTime);
        assertNotNull(request.getAttribute(AccessLogRequestInfo.JIRA_REQUEST_ID));
        assertEquals(1L, AccessLogRequestInfo.concurrentRequests.get());

        accessLogRequestInfo1.exitRequest(request);
        assertEquals(0L, AccessLogRequestInfo.concurrentRequests.get());

        accessLogRequestInfo1.exitRequest(request);
        assertEquals(0L, AccessLogRequestInfo.concurrentRequests.get());
    }

    @Test
    public void testLog4JMDCInteraction()
    {
        // https interaction

        request = new MockHttpServletRequest();
        request.setRemoteAddr("172.45.53.1");
        request.setContextPath("/cntx");
        request.setRequestURL("https://somehostname/cntx/url/path?p=1");

        AccessLogRequestInfo requestInfo = new AccessLogRequestInfo();
        requestInfo.enterRequest(request, response);

        Hashtable context = MDC.getContext();
        assertNotNull(context);
        assertEquals("/url/path?p=1", context.get(Log4jKit.MDC_JIRA_REQUEST_URL));
        assertEquals("172.45.53.1", context.get(Log4jKit.MDC_JIRA_REQUEST_IPADDR));
        assertEquals("anonymous", context.get(Log4jKit.MDC_JIRA_USERNAME));

        // http interaction

        request = new MockHttpServletRequest();
        request.setRemoteAddr("172.45.53.1");
        request.setContextPath("/cntx");
        request.setRequestURL("http://somehostname/cntx/url/path?p=1");

        requestInfo = new AccessLogRequestInfo();
        requestInfo.enterRequest(request, response);


        context = MDC.getContext();
        assertNotNull(context);
        assertEquals("/url/path?p=1", context.get(Log4jKit.MDC_JIRA_REQUEST_URL));
        assertEquals("172.45.53.1", context.get(Log4jKit.MDC_JIRA_REQUEST_IPADDR));
        assertEquals("anonymous", context.get(Log4jKit.MDC_JIRA_USERNAME));

        // bad input interaction

        request = new MockHttpServletRequest();
        request.setContextPath("/Xcntx");
        request.setRequestURL("httpX://somehostname/cntx/url/path?p=1");

        requestInfo = new AccessLogRequestInfo();
        requestInfo.enterRequest(request, response);


        context = MDC.getContext();
        assertNotNull(context);
        assertEquals("httpX://somehostname/cntx/url/path?p=1", context.get(Log4jKit.MDC_JIRA_REQUEST_URL));
        assertEquals("anonymous", context.get(Log4jKit.MDC_JIRA_USERNAME));

    }
}
