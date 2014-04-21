package com.atlassian.security.auth.trustedapps;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import junit.framework.TestCase;

public class TestDefaultURLMatcher extends TestCase
{
    public void testDefaultURLMatcher() throws Exception
    {
        final DefaultURLMatcher matcher = new DefaultURLMatcher(new HashSet<String>(Arrays.asList("/docs", "/admin/info", "/logs")));

        assertURLMatch(true, matcher, "/admin/info.html");
        assertURLMatch(true, matcher, "/docs/index.html");
        assertURLMatch(false, matcher, "/");
        assertURLMatch(false, matcher, "/doc");
        assertURLMatch(false, matcher, "/admin");
        assertURLMatch(false, matcher, "/admin/deleteTask");
    }

    public void testDefaultURLMatcherAgainstEmptySet() throws Exception
    {
        final DefaultURLMatcher matcher = new DefaultURLMatcher(Collections.<String> emptySet());

        assertURLMatch(true, matcher, "/admin/info.html");
        assertURLMatch(true, matcher, "/docs/index.html");
        assertURLMatch(true, matcher, "/");
        assertURLMatch(true, matcher, "/doc");
        assertURLMatch(true, matcher, "/admin");
        assertURLMatch(true, matcher, "/admin/deleteTask");
    }

    private void assertURLMatch(final boolean expected, final DefaultURLMatcher matcher, final String path) throws Exception
    {
        assertEquals(expected, matcher.match(path));
    }
}