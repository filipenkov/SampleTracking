package com.atlassian.security.cookie;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

/**
 * Test the utility method on ServerCookie which is used by {@link HttpOnlyCookies}.
 */
public final class ServerCookieTest
{
    @Test
    public void testAppendCookieValue() throws Exception
    {
        String cookie = generateCookieString(1, "name", "value", "/path", "example.com", "comment", 2000, false, true);
        assertThat(cookie, startsWith("name=value; Version=1; Comment=comment; " +
            "Domain=example.com; Max-Age=2000; Expires="));
        // Expires is a calculated time that we can't test easily
        assertThat(cookie, endsWith("; Path=/path; HttpOnly"));
    }

    @Test
    public void testValueIsEscaped() throws Exception
    {
        String cookie = generateCookieString("name", "value with \"embedded\" quotes and; semi-colons");
        assertThat(cookie, equalTo("name=\"value with \\\"embedded\\\" quotes and; semi-colons\"; Version=1"));
    }

    @Test
    public void testNameIsNotEscaped() throws Exception
    {
        String cookie = generateCookieString("name=foo; Domain=example.com; Expires", "value");
        assertThat(cookie, equalTo("name=foo; Domain=example.com; Expires=value")); // no escaping
    }

    private static String generateCookieString(String name, String value)
    {
        return generateCookieString(0, name, value, null, null, null, -1, false, false);
    }

    private static String generateCookieString(int version, String name, String value, String path, String domain,
        String comment, int maxAge, boolean isSecure, boolean isHttpOnly)
    {
        StringBuffer buffer = new StringBuffer();
        ServerCookie.appendCookieValue(buffer, version, name, value, path, domain, comment, maxAge, isSecure, isHttpOnly);
        return buffer.toString();
    }
}
