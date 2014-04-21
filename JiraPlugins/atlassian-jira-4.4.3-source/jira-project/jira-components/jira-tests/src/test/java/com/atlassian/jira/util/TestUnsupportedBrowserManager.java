package com.atlassian.jira.util;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import mock.servlet.MockHttpServletRequest;
import org.easymock.EasyMock;

import javax.servlet.http.Cookie;

/**
 * Unit Test for UnsupportedBrowserManager
 *
 * @since v4.2
 */
public class TestUnsupportedBrowserManager extends MockControllerTestCase
{
    private UnsupportedBrowserManager ubm;
    private static UserAgentUtil.Browser ie6 = new UserAgentUtil.Browser(UserAgentUtil.BrowserFamily.MSIE,
            UserAgentUtil.BrowserMajorVersion.MSIE6,
            "MSIE6.0");
    private static UserAgentUtil.Browser firefox = new UserAgentUtil.Browser(UserAgentUtil.BrowserFamily.FIREFOX,
            UserAgentUtil.BrowserMajorVersion.FIREFOX36,
            "Firefox3.6.6");
    private static final String DON_T_USE_IE6 = "browser.ie6.nosupport";
    private ApplicationProperties applicationProperties;


    @Before
    public void setUp()
    {
        applicationProperties = getMock(ApplicationProperties.class);
        ubm = new UnsupportedBrowserManager(applicationProperties);
    }

    //Actual usera agent strings taken from logs

    @Test
    public void testIsUnsupported()
    {
        replay();
        final MockHttpServletRequest ie6Request = new MockHttpServletRequest();
        final MockHttpServletRequest firefoxRequest = new MockHttpServletRequest();

        ie6Request.setHeader("USER-AGENT", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
        assertTrue("IE6 is unsupported", ubm.isUnsupportedBrowser(ie6Request));
        assertTrue("IE6 is unsupported", ubm.isUnsupportedBrowser(ie6));
        assertEquals(DON_T_USE_IE6, DON_T_USE_IE6, ubm.getMessageKey(ie6Request));
        assertEquals(DON_T_USE_IE6, DON_T_USE_IE6, ubm.getMessageKey(ie6));

        firefoxRequest.setHeader("USER-AGENT", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2.6) Gecko/20100625 Firefox/3.6.6");
        assertFalse("Firefox is supported", ubm.isUnsupportedBrowser(firefoxRequest));
        assertFalse("Firefox is supported", ubm.isUnsupportedBrowser(firefox));
        assertNull("Firefox is supported", ubm.getMessageKey(firefoxRequest));
        assertNull("Firefox is supported", ubm.getMessageKey(firefox));
    }

    @Test
    public void testApplicationPropertiesToggle()
    {
        EasyMock.expect(applicationProperties.getDefaultString(APKeys.JIRA_BROWSER_UNSUPPORTED_WARNINGS_DISABLED)).andReturn("true");
        replay();

        assertFalse("Unsupported Browser warnings should be disabled", ubm.isCheckEnabled());
    }

    @Test
    public void testCookieHandling()
    {
        replay();
        final MockHttpServletRequest requestWithCookies = new MockHttpServletRequest();
        requestWithCookies.setCookies(new Cookie[] { new Cookie("UNSUPPORTED_BROWSER_WARNING", "Handled") });
        final MockHttpServletRequest requestWithNoCookies = new MockHttpServletRequest();

        assertTrue("Cookie should be present", ubm.isHandledCookiePresent(requestWithCookies));
        assertFalse("Cookie should be absent", ubm.isHandledCookiePresent(requestWithNoCookies));
        verify();
    }

}
