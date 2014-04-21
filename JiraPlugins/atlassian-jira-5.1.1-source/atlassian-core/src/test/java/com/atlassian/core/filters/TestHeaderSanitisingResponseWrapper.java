package com.atlassian.core.filters;

import com.mockobjects.constraint.IsEqual;
import com.mockobjects.constraint.IsNull;
import com.mockobjects.constraint.IsAnything;
import com.mockobjects.dynamic.FullConstraintMatcher;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.servlet.MockHttpServletResponse;
import junit.framework.TestCase;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * @since v4.2
 */
public class TestHeaderSanitisingResponseWrapper extends TestCase
{
    public void testCleanString() throws Exception
    {
        final MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        HeaderSanitisingResponseWrapper wrapper = new HeaderSanitisingResponseWrapper(mockHttpServletResponse);
        assertEquals("This is a normal string", wrapper.cleanString("This is a normal string"));

        String badString = new String(new char[] { '\r', '\n', 'n', 'o', 'r', 'm', 'a', 'l' });
        assertEquals("  normal", wrapper.cleanString(badString));

        badString          = "this is a normal string";
        String replacement = "t!is!is!!!norm!l!strin!";
        wrapper = new HeaderSanitisingResponseWrapper(mockHttpServletResponse, new char[] {' ', 'a', 'g', 'h'}, '!');
        assertEquals(replacement, wrapper.cleanString(badString));

        assertNull(wrapper.cleanString(null));

        assertEquals("", wrapper.cleanString(""));
    }

    public void testAddCookie() throws Exception
    {
        // only values are cleaned as name is immutable
        final Cookie cookie         = new MyCookie("GOOD NAME", "BAD VALUE");
        final Cookie expectedCookie = new MyCookie("GOOD NAME", "BAD.VALUE");

        Mock mockResponse = new Mock(HttpServletResponse.class);
        mockResponse.expect("addCookie", new FullConstraintMatcher(new IsEqual(expectedCookie)));

        HeaderSanitisingResponseWrapper wrapper = createWrapper(mockResponse);
        wrapper.addCookie(cookie);

        mockResponse.verify();
    }

    public void testAddCookieNull() throws Exception
    {
        Mock mockResponse = new Mock(HttpServletResponse.class);
        mockResponse.expect("addCookie", new FullConstraintMatcher(new IsNull()));

        HeaderSanitisingResponseWrapper wrapper = createWrapper(mockResponse);
        wrapper.addCookie(null);

        mockResponse.verify();
    }

    public void testSetContentType() throws Exception
    {
        // only values are cleaned as name is immutable
        final String contentType         = "BAD VALUE";
        final String expectedContentType = "BAD.VALUE";

        Mock mockResponse = new Mock(HttpServletResponse.class);
        mockResponse.expect("setContentType", new FullConstraintMatcher(new IsEqual(expectedContentType)));

        HeaderSanitisingResponseWrapper wrapper = createWrapper(mockResponse);
        wrapper.setContentType(contentType);

        mockResponse.verify();
    }

    public void testSetDateHeader() throws Exception
    {
        // only values are cleaned as name is immutable
        final String dateHeader         = "BAD VALUE";
        final String expectedDateHeader = "BAD.VALUE";

        Mock mockResponse = new Mock(HttpServletResponse.class);
        mockResponse.expect("setDateHeader", new FullConstraintMatcher(new IsEqual(expectedDateHeader), new IsAnything()));

        HeaderSanitisingResponseWrapper wrapper = createWrapper(mockResponse);
        wrapper.setDateHeader(dateHeader, 1L);

        mockResponse.verify();
    }

    public void testAddDateHeader() throws Exception
    {
        // only values are cleaned as name is immutable
        final String dateHeader         = "BAD VALUE";
        final String expectedDateHeader = "BAD.VALUE";

        Mock mockResponse = new Mock(HttpServletResponse.class);
        mockResponse.expect("addDateHeader", new FullConstraintMatcher(new IsEqual(expectedDateHeader), new IsAnything()));

        HeaderSanitisingResponseWrapper wrapper = createWrapper(mockResponse);
        wrapper.addDateHeader(dateHeader, 1L);

        mockResponse.verify();
    }

    public void testSetHeader() throws Exception
    {
        // only values are cleaned as name is immutable
        final String headerName          = "BAD NAME";
        final String expectedHeaderName  = "BAD.NAME";
        final String headerValue         = "BAD VALUE";
        final String expectedHeaderValue = "BAD.VALUE";

        Mock mockResponse = new Mock(HttpServletResponse.class);
        mockResponse.expect("setHeader", new FullConstraintMatcher(new IsEqual(expectedHeaderName), new IsEqual(expectedHeaderValue)));

        HeaderSanitisingResponseWrapper wrapper = createWrapper(mockResponse);
        wrapper.setHeader(headerName, headerValue);

        mockResponse.verify();
    }

    public void testAddHeader() throws Exception
    {
        // only values are cleaned as name is immutable
        final String headerName          = "BAD NAME";
        final String expectedHeaderName  = "BAD.NAME";
        final String headerValue         = "BAD VALUE";
        final String expectedHeaderValue = "BAD.VALUE";

        Mock mockResponse = new Mock(HttpServletResponse.class);
        mockResponse.expect("addHeader", new FullConstraintMatcher(new IsEqual(expectedHeaderName), new IsEqual(expectedHeaderValue)));

        HeaderSanitisingResponseWrapper wrapper = createWrapper(mockResponse);
        wrapper.addHeader(headerName, headerValue);

        mockResponse.verify();
    }

    public void testSetIntHeader() throws Exception
    {
        // only values are cleaned as name is immutable
        final String intHeader         = "BAD VALUE";
        final String expectedIntHeader = "BAD.VALUE";

        Mock mockResponse = new Mock(HttpServletResponse.class);
        mockResponse.expect("setIntHeader", new FullConstraintMatcher(new IsEqual(expectedIntHeader), new IsAnything()));

        HeaderSanitisingResponseWrapper wrapper = createWrapper(mockResponse);
        wrapper.setIntHeader(intHeader, 1);

        mockResponse.verify();
    }

    public void testAddIntHeader() throws Exception
    {
        // only values are cleaned as name is immutable
        final String intHeader         = "BAD VALUE";
        final String expectedIntHeader = "BAD.VALUE";

        Mock mockResponse = new Mock(HttpServletResponse.class);
        mockResponse.expect("addIntHeader", new FullConstraintMatcher(new IsEqual(expectedIntHeader), new IsAnything()));

        HeaderSanitisingResponseWrapper wrapper = createWrapper(mockResponse);
        wrapper.addIntHeader(intHeader, 1);

        mockResponse.verify();
    }

    public void testSendRedirect() throws Exception
    {
        // only values are cleaned as name is immutable
        final String redirect         = "BAD VALUE";
        final String expectedRedirect = "BAD.VALUE";

        Mock mockResponse = new Mock(HttpServletResponse.class);
        mockResponse.expect("sendRedirect", new FullConstraintMatcher(new IsEqual(expectedRedirect)));

        HeaderSanitisingResponseWrapper wrapper = createWrapper(mockResponse);
        wrapper.sendRedirect(redirect);

        mockResponse.verify();
    }

    public void testSendError() throws Exception
    {
        // only values are cleaned as name is immutable
        final String message         = "BAD VALUE";
        final String expectedMessage = "BAD.VALUE";

        Mock mockResponse = new Mock(HttpServletResponse.class);
        mockResponse.expect("sendError", new FullConstraintMatcher(new IsAnything(), new IsEqual(expectedMessage)));

        HeaderSanitisingResponseWrapper wrapper = createWrapper(mockResponse);
        wrapper.sendError(500, message);

        mockResponse.verify();
    }

    public void testSetStatus() throws Exception
    {
        // only values are cleaned as name is immutable
        final String message         = "BAD VALUE";
        final String expectedMessage = "BAD.VALUE";

        Mock mockResponse = new Mock(HttpServletResponse.class);
        mockResponse.expect("setStatus", new FullConstraintMatcher(new IsAnything(), new IsEqual(expectedMessage)));

        HeaderSanitisingResponseWrapper wrapper = createWrapper(mockResponse);
        wrapper.setStatus(500, message);

        mockResponse.verify();
    }

    private HeaderSanitisingResponseWrapper createWrapper(final Mock mockResponse)
    {
        return new HeaderSanitisingResponseWrapper((HttpServletResponse) mockResponse.proxy(), new char[] {' '}, '.');
    }

    /**
     * Test cookie that actually implements {@link #equals(Object)}.
     */
    private static class MyCookie extends Cookie
    {
        private String name;
        private String value;

        public MyCookie(String s, String s1)
        {
            super(s, s1);
            this.name = s;
            this.value = s1;
        }

        public void setValue(final String s)
        {
            this.value = s;
            super.setValue(s);
        }

        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            final MyCookie myCookie = (MyCookie) o;

            if (name != null ? !name.equals(myCookie.name) : myCookie.name != null)
            {
                return false;
            }
            if (value != null ? !value.equals(myCookie.value) : myCookie.value != null)
            {
                return false;
            }

            return true;
        }

        public int hashCode()
        {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (value != null ? value.hashCode() : 0);
            return result;
        }
    }
}
