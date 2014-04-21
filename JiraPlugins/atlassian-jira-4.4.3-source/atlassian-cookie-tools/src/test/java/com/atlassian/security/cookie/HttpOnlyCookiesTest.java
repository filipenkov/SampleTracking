package com.atlassian.security.cookie;

import java.util.Collection;
import java.util.Iterator;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class HttpOnlyCookiesTest
{
    private static final String COOKIE_HEADER = "Set-Cookie";
    private static final String COOKIE_NAME = "myname";
    private static final String COOKIE_VALUE = "myvalue";
    private static final String COOKIE_NAME_2 = COOKIE_NAME + "2";
    private static final String COOKIE_VALUE_2 = COOKIE_VALUE + "2";

    private Response response;
    private Cookie cookie;
    private Cookie cookie2;

    @Before
    public void setUp()
    {
        response = new Response();
        cookie = new Cookie(COOKIE_NAME, COOKIE_VALUE);
        cookie2 = new Cookie(COOKIE_NAME_2, COOKIE_VALUE_2);
    }

    @Test
    public void testAddCookieWithHttpOnly()
    {
        HttpOnlyCookies.addHttpOnlyCookie(response, cookie);
        assertThat(response.getHeader(COOKIE_HEADER), is(equalTo(getCookieText(COOKIE_NAME, COOKIE_VALUE, true))));
    }

    @Test
    public void testAddCookiesWithoutHttpOnly()
    {
        Cookie[] cookies = new Cookie[] { cookie, cookie2 };
        HttpOnlyCookies.addHttpOnlyCookies(response, cookies);
        assertThat(response.getHeader(COOKIE_HEADER), is(equalTo(getCookieText(COOKIE_NAME, COOKIE_VALUE, true) + "; " +
                 getCookieText(COOKIE_NAME_2, COOKIE_VALUE_2, true))));
    }

    /**
     * Get the expected cookie text
     */
    private String getCookieText(String name, String value, boolean httpOnly)
    {
        StringBuffer cookieText = new StringBuffer();
        cookieText.append(name).append("=").append(value);
        if (httpOnly)
        {
            cookieText.append("; HttpOnly");
        }
        return cookieText.toString();
    }

    private static class Response extends HttpServletResponseWrapper
    {
        private MultiMap headers = new MultiValueMap();

        public Response()
        {
            super(mock(HttpServletResponse.class));
        }

        public void addHeader(String name, String value)
        {
            headers.put(name, value);
        }

        public String getHeader(String headerName)
        {
            // return all values, even if there is more than one
            Collection values = (Collection) headers.get(headerName);
            if (values == null)
            {
                return null;
            }
            else if (values.isEmpty())
            {
                return "";
            }
            else
            {
                StringBuffer headerValue = new StringBuffer();
                Iterator<String> iter = values.iterator();
                headerValue.append(iter.next());
                while (iter.hasNext())
                {
                    headerValue.append("; ").append(iter.next());
                }

                return headerValue.toString();
            }
        }
    }
}
