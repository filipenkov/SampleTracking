package com.atlassian.jira.web.filters.accesslog;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.local.ListeningTestCase;
import mock.servlet.MockHttpServletRequest;

import static com.atlassian.jira.web.filters.accesslog.AccessLogRequestInfo.JIRA_REQUEST_ASESSIONID;
import static com.atlassian.jira.web.filters.accesslog.AccessLogRequestInfo.JIRA_REQUEST_ID;
import static com.atlassian.jira.web.filters.accesslog.AccessLogRequestInfo.JIRA_REQUEST_START_MILLIS;

/**
 * Unit test case for {@link com.atlassian.jira.web.filters.accesslog.AccessLogImprinter}.
 */
public class TestAccessLogImprinter extends ListeningTestCase
{

    @Test
    public void testNoAttributes()
    {
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();

        AccessLogImprinter imprinter = new AccessLogImprinter(httpServletRequest);

        String value = imprinter.imprintHTMLComment();
        assertNotNull(value);
        assertTrue(value, value.startsWith("\n<!--"));
        assertTrue(value, value.endsWith("\n-->"));
        assertTrue(value, value.contains("REQUEST ID : -"));
        assertTrue(value, value.contains("REQUEST TIMESTAMP : -"));
        assertTrue(value, value.contains("REQUEST TIME : -"));
        assertTrue(value, value.contains("ASESSIONID : -"));
    }

    @Test
    public void testValuesSet()
    {
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();

        AccessLogImprinter imprinter = new AccessLogImprinter(httpServletRequest);

        httpServletRequest.setAttribute(JIRA_REQUEST_START_MILLIS, new Long(System.currentTimeMillis()));
        String value = imprinter.imprintHTMLComment();
        assertNotNull(value);
        assertTrue(value, !value.contains("REQUEST TIMESTAMP : -"));

        httpServletRequest.setAttribute(JIRA_REQUEST_ID, "requestId");
        value = imprinter.imprintHTMLComment();
        assertNotNull(value);
        assertTrue(value, value.contains("REQUEST ID : requestId"));

        httpServletRequest.setAttribute(JIRA_REQUEST_ASESSIONID, "ABCDEF1234");
        value = imprinter.imprintHTMLComment();
        assertNotNull(value);
        assertTrue(value, value.contains("ASESSIONID : ABCDEF1234"));
    }

    @Test
    public void testEscaping()
    {
        MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();

        AccessLogImprinter imprinter = new AccessLogImprinter(httpServletRequest);

        httpServletRequest.setAttribute(JIRA_REQUEST_ID, "a man smoking a pipe <!-- in it");
        String value = imprinter.imprintHTMLComment();
        assertNotNull(value);
        assertTrue(value, !value.contains("REQUEST ID : \"a man smoking a pipe  <!-: comment in it\""));
    }
}
