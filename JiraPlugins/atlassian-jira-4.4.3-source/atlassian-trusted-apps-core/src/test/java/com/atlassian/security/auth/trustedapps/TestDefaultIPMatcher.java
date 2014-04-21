package com.atlassian.security.auth.trustedapps;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

public class TestDefaultIPMatcher extends TestCase
{
    public void testIpMatcherFailsOutOfRangeNumber() throws Exception
    {
        assertIllegalMatcherInvalidIP("299.299.299.299");
        assertIllegalMatcherInvalidIP("299.1.1.1");
        assertIllegalMatcherInvalidIP("1.299.1.1");
        assertIllegalMatcherInvalidIP("1.1.299.1");
        assertIllegalMatcherInvalidIP("1.1.1.299");
    }

    public void testIpMatcherFailsOutOfRangeNegativeNumber() throws Exception
    {
        assertIllegalMatcherInvalidIP("-2.-2.-2.-2");
        assertIllegalMatcherInvalidIP("-2.1.1.1");
        assertIllegalMatcherInvalidIP("1.-2.1.1");
        assertIllegalMatcherInvalidIP("1.1.-2.1");
        assertIllegalMatcherInvalidIP("1.1.1.-2");
    }

    public void testIpMatcherFailsIllegalWildcard() throws Exception
    {
        assertIllegalMatcherInvalidIP("?.?.?.?");
        assertIllegalMatcherInvalidIP("?.1.1.1");
        assertIllegalMatcherInvalidIP("1.?.1.1");
        assertIllegalMatcherInvalidIP("1.1.?.1");
        assertIllegalMatcherInvalidIP("1.1.1.?");
    }

    private void assertIllegalMatcherInvalidIP(final String dodgyIPAddress)
    {
        try
        {
            final Set<String> patterns = new HashSet<String>(Arrays.asList(dodgyIPAddress));
            new DefaultIPMatcher(patterns);
            fail("Should have thrown InvalidIPAddress: " + dodgyIPAddress);
        }
        catch (final IPAddressFormatException yay)
        {
            assertEquals(dodgyIPAddress, yay.getBadIPAddress());
        }
    }

    public void testDefIPMatcher() throws Exception
    {
        DefaultIPMatcher matcher = new DefaultIPMatcher(new HashSet<String>(Arrays.asList("192.168.*.23", "123.132.*.*", "123.45.67.*",
            "255.255.255.255", "255.255.255.*")));

        assertIPMatch(true, matcher, "192.168.1.23");
        assertIPMatch(true, matcher, "192.168.2.23");
        assertIPMatch(false, matcher, "192.168.1.24");

        assertIPMatch(true, matcher, "123.132.1.24");
        assertIPMatch(true, matcher, "123.132.2.27");
        assertIPMatch(false, matcher, "124.132.1.24");
        assertIPMatch(false, matcher, "123.131.1.24");

        assertIPMatch(true, matcher, "123.45.67.24");
        assertIPMatch(true, matcher, "123.45.67.27");
        assertIPMatch(false, matcher, "124.45.1.24");
        assertIPMatch(false, matcher, "123.45.32.24");

        assertIPMatch(true, matcher, "255.255.255.255");
        assertIPMatch(true, matcher, "255.255.255.251");
        assertIPMatch(false, matcher, "255.255.254.255");

        try
        {
            matcher.match("192.168.1.");
            fail("invalid pattern must fail");
        }
        catch (final IPAddressFormatException e)
        {
            // expected
        }

        try
        {
            matcher = new DefaultIPMatcher(Collections.singleton("192.168.*."));
            fail("invalid pattern must fail");
        }
        catch (final IPAddressFormatException e)
        {
            // expected
        }
    }

    public void testEmptyIpMatcherMatchesEverything() throws Exception
    {
        final DefaultIPMatcher matcher = new DefaultIPMatcher(Collections.<String> emptySet());
        assertIPMatch(true, matcher, "192.168.0.5");
    }

    private void assertIPMatch(final boolean expected, final IPMatcher matcher, final String pattern) throws Exception
    {
        assertEquals(expected, matcher.match(pattern));
    }
}