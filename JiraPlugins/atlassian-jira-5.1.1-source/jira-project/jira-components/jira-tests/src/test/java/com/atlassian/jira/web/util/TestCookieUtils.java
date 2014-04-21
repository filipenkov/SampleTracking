package com.atlassian.jira.web.util;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.util.collect.MapBuilder;
import com.mockobjects.servlet.MockHttpServletRequest;
import com.mockobjects.servlet.MockHttpServletResponse;
import mock.servlet.ExtendedMockHttpServletRequest;

import java.util.ArrayList;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * Tests the CookieUtils class.
 */
public class TestCookieUtils extends ListeningTestCase
{

    private static final Cookie COOKIE1 = new Cookie("some", "cookie");
    private static final Cookie COOKIE2 = new Cookie("other", "cookie");

    private static final Cookie HAPPY_COOKIE = new Cookie(CookieUtils.JSESSIONID, "foo");
    private static final Cookie ANOTHER_HAPPY_COOKIE = new Cookie(CookieUtils.JSESSIONID, "another happy");

    private static void assertHasSessionId(boolean expectedResult, Cookie[] cookies)
    {
        assertEquals(expectedResult, CookieUtils.hasSessionId(cookies));
    }

    private static void assertGetSingleSessionIdEquals(String expectedResult, Cookie[] cookies)
    {
        assertEquals(expectedResult, CookieUtils.getSingleSessionId(cookies));
    }

    @Test
    public void testHasSessionIdNullCookies()
    {
        assertHasSessionId(false, null);
    }

    @Test
    public void testGetSingleSessionIdNullCookies()
    {
        assertGetSingleSessionIdEquals(null, null);
    }

    @Test
    public void testHasSessionIdNoCookies()
    {
        assertHasSessionId(false, new Cookie[0]);
    }

    @Test
    public void testHasSessionIdNotValidCookie()
    {
        assertHasSessionId(false, new Cookie[]{COOKIE1});
        assertHasSessionId(false, new Cookie[]{COOKIE1, COOKIE2});
    }

    @Test
    public void testHasSessionIdValidCookie()
    {
        assertHasSessionId(true, new Cookie[]{HAPPY_COOKIE});
        assertHasSessionId(true, new Cookie[]{COOKIE1, HAPPY_COOKIE, COOKIE2});
        assertHasSessionId(true, new Cookie[]{HAPPY_COOKIE, HAPPY_COOKIE, COOKIE2});
        assertHasSessionId(true, new Cookie[]{ANOTHER_HAPPY_COOKIE, HAPPY_COOKIE});
    }

    @Test
    public void testCreateSessionCookie()
    {

        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setupGetContextPath("/mycontext");

        final ArrayList cookies = new ArrayList();

        HttpServletResponse mockResponse = new MockHttpServletResponse()
        {
            public void addCookie(Cookie cookie)
            {
                cookies.add(cookie);
            }
        };

        CookieUtils.setSessionCookie(mockRequest, mockResponse, null);
        assertEquals(0, cookies.size());

        CookieUtils.setSessionCookie(mockRequest, mockResponse, "sessionId");
        assertEquals(1, cookies.size());

        Cookie c = (Cookie) cookies.get(0);
        assertEquals(CookieUtils.JSESSIONID, c.getName());
        assertEquals("sessionId", c.getValue());
        assertEquals("/mycontext", c.getPath());

    }

    @Test
    public void testGetSingleSessionIdNoSession()
    {
        assertGetSingleSessionIdEquals(null, new Cookie[0]);
        assertGetSingleSessionIdEquals(null, new Cookie[]{COOKIE1});
        assertGetSingleSessionIdEquals(null, new Cookie[]{COOKIE1, COOKIE2});
    }

    @Test
    public void testGetSingleSessionIdOneSession()
    {
        assertGetSingleSessionIdEquals(HAPPY_COOKIE.getValue(), new Cookie[]{HAPPY_COOKIE});
        assertGetSingleSessionIdEquals(HAPPY_COOKIE.getValue(), new Cookie[]{HAPPY_COOKIE, COOKIE2});
    }

    @Test
    public void testGetSingleSessionIdMultipleSession()
    {
        assertGetSingleSessionIdEquals(null, new Cookie[]{HAPPY_COOKIE, ANOTHER_HAPPY_COOKIE});
        assertGetSingleSessionIdEquals(null, new Cookie[]{HAPPY_COOKIE, ANOTHER_HAPPY_COOKIE, COOKIE1});
    }

    @Test
    public void testGetCookie()
    {
        ExtendedMockHttpServletRequest mockRequest = new ExtendedMockHttpServletRequest();
        mockRequest.addCookie("testcookie", "testvalue");
        assertEquals("testvalue", CookieUtils.getCookieValue("testcookie", mockRequest));
        assertNull(CookieUtils.getCookieValue("nonexistingcookiename", mockRequest));
    }

    @Test
    public void testCreateCookieWithContextPath()
    {
        ExtendedMockHttpServletRequest mockRequest = new ExtendedMockHttpServletRequest();
        mockRequest.setupGetContextPath("/some/path");
        Cookie newCookie = CookieUtils.createCookie("name", "value", mockRequest);
        assertEquals("name", newCookie.getName());
        assertEquals("value", newCookie.getValue());
        assertEquals("/some/path", newCookie.getPath());
    }

    @Test
    public void testCreateCookieWithoutContextPath()
    {
        ExtendedMockHttpServletRequest mockRequest = new ExtendedMockHttpServletRequest();
        mockRequest.setupGetContextPath(null);
        Cookie newCookie = CookieUtils.createCookie("name", "value", mockRequest);
        assertEquals("name", newCookie.getName());
        assertEquals("value", newCookie.getValue());
        assertEquals("/", newCookie.getPath());
    }

    @Test
    public void testCreateCookieWithEmptyContextPath()
    {
        ExtendedMockHttpServletRequest mockRequest = new ExtendedMockHttpServletRequest();
        mockRequest.setupGetContextPath("");
        Cookie newCookie = CookieUtils.createCookie("name", "value", mockRequest);
        assertEquals("name", newCookie.getName());
        assertEquals("value", newCookie.getValue());
        assertEquals("/", newCookie.getPath());
    }

    @Test
    public void testcreateConglomerateCookieWithOneValue()
    {
        ExtendedMockHttpServletRequest mockRequest = new ExtendedMockHttpServletRequest();
        Cookie cookie = CookieUtils.createConglomerateCookie("cong", MapBuilder.newBuilder("twixi-blocks", "#comment-13822").toMap(), mockRequest);

        assertEquals("twixi-blocks%3D%23comment-13822", cookie.getValue());
    }

    @Test
    public void testcreateConglomerateCookieWithEmptyValue()
    {
        ExtendedMockHttpServletRequest mockRequest = new ExtendedMockHttpServletRequest();
        Cookie cookie = CookieUtils.createConglomerateCookie("cong", MapBuilder.newBuilder("twixi-blocks", "").toMap(), mockRequest);

        assertEquals("", cookie.getValue());
    }

    @Test
    public void testcreateConglomerateCookieWithTwoValues()
    {
        ExtendedMockHttpServletRequest mockRequest = new ExtendedMockHttpServletRequest();
        Map<String, String> map = MapBuilder.newBuilder("twixi-blocks", "#comment-13822").add("#foo", "bar").toLinkedHashMap();
        Cookie cookie = CookieUtils.createConglomerateCookie("cong", map, mockRequest);

        assertEquals("twixi-blocks%3D%23comment-13822|%23foo%3Dbar", cookie.getValue());
    }
    @Test
    public void testParseConglomerateCookieWithNoCookie()
    {
        ExtendedMockHttpServletRequest mockRequest = new ExtendedMockHttpServletRequest();
        Map<String,String> map = CookieUtils.parseConglomerateCookie("cong", mockRequest);
        assertNotNull(map);
        assertTrue(map.isEmpty());
    }

    @Test
    public void testParseConglomerateCookieWithEmptyCookie()
    {
        ExtendedMockHttpServletRequest mockRequest = new ExtendedMockHttpServletRequest();
        mockRequest.addCookie("cong", "");
        Map<String,String> map = CookieUtils.parseConglomerateCookie("cong", mockRequest);
        assertNotNull(map);
        assertTrue(map.isEmpty());
    }

    @Test
    public void testParseConglomerateCookieWithOneValue()
    {
        ExtendedMockHttpServletRequest mockRequest = new ExtendedMockHttpServletRequest();
        mockRequest.addCookie("cong", "twixi-blocks%3D%23comment-13822");
        Map<String,String> map = CookieUtils.parseConglomerateCookie("cong", mockRequest);
        assertNotNull(map);
        assertEquals(1, map.size());
        assertEquals("#comment-13822", map.get("twixi-blocks"));
    }

    @Test
    public void testParseConglomerateCookieWithTwoValue()
    {
        ExtendedMockHttpServletRequest mockRequest = new ExtendedMockHttpServletRequest();
        mockRequest.addCookie("cong", "twixi-blocks%3D%23comment-13822|%23foo%3dbar");
        Map<String,String> map = CookieUtils.parseConglomerateCookie("cong", mockRequest);
        assertNotNull(map);
        assertEquals(2, map.size());
        assertEquals("#comment-13822", map.get("twixi-blocks"));
        assertEquals("bar", map.get("#foo"));
    }
}
