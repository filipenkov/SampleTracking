package com.atlassian.security.auth.trustedapps;

import junit.framework.TestCase;

import java.util.Iterator;

/**
 *
 * @since v2.2
 */
public class TestRequestConditions extends TestCase
{
    public void testBuilder()
    {
        final RequestConditions rules = RequestConditions
                .builder()
                .setCertificateTimeout(10L)
                .addIPPattern("192.168.0.*")
                .build();

        assertEquals(10L, rules.getCertificateTimeout());

        final Iterator patterns = rules.getIPPatterns().iterator();
        assertEquals("192.168.0.*", patterns.next());
        assertFalse(patterns.hasNext());

        assertFalse(rules.getURLPatterns().iterator().hasNext());

        final IPMatcher ipMatcher = rules.getIPMatcher();
        assertTrue(ipMatcher.match("192.168.0.1"));
        assertTrue(ipMatcher.match("192.168.0.255"));
        assertFalse(ipMatcher.match("10.10.10.1"));

        final URLMatcher urlMatcher = rules.getURLMatcher();
        assertTrue(urlMatcher.match("/"));
        assertTrue(urlMatcher.match("/foo/bar"));
    }

    public void testBuilderWithIPv4CIDR()
    {
        final RequestConditions rules = RequestConditions
        .builder()
        .addIPPattern("192.168.2.0/24")
        .build();

        final IPMatcher ipMatcher = rules.getIPMatcher();
        assertTrue(ipMatcher.match("192.168.2.100"));
    }

    public void testBuilderWithIPv6CIDR()
    {
        final RequestConditions rules = RequestConditions
        .builder()
        .addIPPattern("0:0:0:3::/64")
        .build();

        final IPMatcher ipMatcher = rules.getIPMatcher();
        assertTrue(ipMatcher.match("0:0:0:3::1"));
    }

    public void testInputValidation()
    {
        assertInvalid("");
        assertInvalid("192");
        assertInvalid("192.168.0");
        assertInvalid("256.0.0.0");
        assertInvalid("1.2.3.4.5");
        assertInvalid("foo");
        assertInvalid("*");

        try
        {
            RequestConditions
                .builder()
                    .setCertificateTimeout(-1L);
            fail();
        }
        catch (IllegalArgumentException e) {}
    }

    private void assertInvalid(String ipPattern)
    {
        try
        {
            RequestConditions
                .builder()
                .addIPPattern(ipPattern);
            fail();
        }
        catch (IPAddressFormatException e) {}
    }
}
