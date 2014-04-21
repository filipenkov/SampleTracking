package com.atlassian.ip;

import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

public class IPMatcherTest
{
    @Test
    public void testIPv4MatchingSuccess() throws Exception
    {
        final IPMatcher.Builder builder = IPMatcher.builder();
        builder.addPattern("1.1.1.1");
        final IPMatcher ipMatcher = builder.build();
        
        assertTrue(ipMatcher.matches("1.1.1.1"));
    }

    @Test
    public void testIPv4MatchingFailure() throws Exception
    {
        final IPMatcher.Builder builder = IPMatcher.builder();
        builder.addPattern("1.1.1.1");
        final IPMatcher ipMatcher = builder.build();

        assertFalse(ipMatcher.matches("1.1.1.0"));
    }

    @Test
    public void testIPv6MatchingSuccess() throws Exception
    {
        final IPMatcher.Builder builder = IPMatcher.builder();
        builder.addPattern("::2");
        final IPMatcher ipMatcher = builder.build();

        assertTrue(ipMatcher.matches("0:0:0:0:0:0:0:2"));
    }

    @Test
    public void testShortFormIPv6MatchingSuccess() throws Exception
    {
        final IPMatcher.Builder builder = IPMatcher.builder();
        builder.addPattern("::2");
        final IPMatcher ipMatcher = builder.build();

        assertTrue(ipMatcher.matches("::2"));
    }

    @Test
    public void testIPv6MatchingFailure() throws Exception
    {
        final IPMatcher.Builder builder = IPMatcher.builder();
        builder.addPattern("::2");
        final IPMatcher ipMatcher = builder.build();

        assertFalse(ipMatcher.matches("0:0:0:0:0:0:0:1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidPattern() throws Exception
    {
        final IPMatcher.Builder builder = IPMatcher.builder();
        builder.addPattern("192.1.2");
    }

    @Test
    public void testAsteriskMatchingSuccess() throws Exception
    {
        final IPMatcher.Builder builder = IPMatcher.builder();
        builder.addPattern("172.16.*.*");
        final IPMatcher ipMatcher = builder.build();

        assertTrue(ipMatcher.matches("172.16.1.1"));
    }

    @Test
    public void testAsteriskMatchingFailure() throws Exception
    {
        final IPMatcher.Builder builder = IPMatcher.builder();
        builder.addPattern("172.16.*.*");
        final IPMatcher ipMatcher = builder.build();

        assertFalse(ipMatcher.matches("172.15.255.255"));
    }

    @Test
    public void testIPv4CIDRMatchingSuccess() throws Exception
    {
        final IPMatcher.Builder builder = IPMatcher.builder();
        builder.addPattern("192.1.2.3/8");
        final IPMatcher ipMatcher = builder.build();

        assertTrue(ipMatcher.matches("192.1.1.1"));
    }

    @Test
    public void testIPv4CIDRMatchingFailure() throws Exception
    {
        final IPMatcher.Builder builder = IPMatcher.builder();
        builder.addPattern("192.1.2.3/8");
        final IPMatcher ipMatcher = builder.build();

        assertTrue(ipMatcher.matches("192.2.0.0"));
    }

    @Test
    public void testIPv6CIDRMatchingSuccess() throws Exception
    {
        final IPMatcher.Builder builder = IPMatcher.builder();
        builder.addPattern("0:0:0:1::/64");
        final IPMatcher ipMatcher = builder.build();

        assertTrue(ipMatcher.matches("0:0:0:1:ffff:ffff:ffff:ffff"));
    }

    @Test
    public void testIPv6CIDRMatchingFailure() throws Exception
    {
        final IPMatcher.Builder builder = IPMatcher.builder();
        builder.addPattern("0:0:0:1::/64");
        final IPMatcher ipMatcher = builder.build();

        assertFalse(ipMatcher.matches("0:0:0:2::"));
    }
    
    @Test
    public void testSubnetZeroAsteriskMatching() throws Exception
    {
        final IPMatcher.Builder builder = IPMatcher.builder();
        builder.addPattern("*.*.*.*");
        final IPMatcher ipMatcher = builder.build();

        assertTrue(ipMatcher.matches("255.255.255.255"));
    }

    @Test
    public void testIPv4IPv6MatchingFailure() throws Exception
    {
        final IPMatcher.Builder builder = IPMatcher.builder();
        builder.addPattern("0.0.0.1");
        final IPMatcher ipMatcher = builder.build();

        assertFalse(ipMatcher.matches("::1"));
    }

    @Test
    public void testSubnetZeroIPv4CIDRMatching() throws Exception
    {
        final IPMatcher.Builder builder = IPMatcher.builder();
        builder.addPattern("192.1.2.3/0");
        final IPMatcher ipMatcher = builder.build();
        
        assertTrue(ipMatcher.matches("0.0.0.0"));
    }

    @Test
    public void testSubnetZeroIPv6CIDRMatching() throws Exception
    {
        final IPMatcher.Builder builder = IPMatcher.builder();
        builder.addPattern("::1/0");
        final IPMatcher ipMatcher = builder.build();

        assertTrue(ipMatcher.matches("::2"));
    }

    @Test
    public void testAllOnesSubnetIPv4CIDRMatchingSuccess() throws Exception
    {
        final IPMatcher.Builder builder = IPMatcher.builder();
        builder.addPattern("192.1.2.3/32");
        final IPMatcher ipMatcher = builder.build();

        assertTrue(ipMatcher.matches("192.1.2.3"));
    }

    @Test
    public void testAllOnesSubnetIPv4CIDRMatchingFailure() throws Exception
    {
        final IPMatcher.Builder builder = IPMatcher.builder();
        builder.addPattern("192.1.2.3/32");
        final IPMatcher ipMatcher = builder.build();

        assertFalse(ipMatcher.matches("192.1.2.2"));
    }

    @Test
    public void testAllOnesSubnetIPv6CIDRMatchingSuccess() throws Exception
    {
        final IPMatcher.Builder builder = IPMatcher.builder();
        builder.addPattern("::1/128");
        final IPMatcher ipMatcher = builder.build();

        assertTrue(ipMatcher.matches("::1"));
    }

    @Test
    public void testAllOnesSubnetIPv6CIDRMatchingFailure() throws Exception
    {
        final IPMatcher.Builder builder = IPMatcher.builder();
        builder.addPattern("::1/128");
        final IPMatcher ipMatcher = builder.build();

        assertFalse(ipMatcher.matches("::2"));
    }

    @Test
    public void testPatternResolving() throws Exception
    {
        final IPMatcher.Builder builder = spy(IPMatcher.builder());
        doReturn(new InetAddress[]{InetAddress.getByAddress(new byte[]{1, 1, 1, 1})}).when(builder).getAllByName("example.org");
        builder.addPatternOrHost("example.org");
        final IPMatcher ipMatcher = builder.build();

        assertTrue(ipMatcher.matches("1.1.1.1"));
    }

    @Test
    public void testPatternResolvingWithUnknownHost() throws Exception
    {
        final IPMatcher.Builder builder = spy(IPMatcher.builder());
        doThrow(new UnknownHostException("example.org")).when(builder).getAllByName("example.org");
        builder.addPatternOrHost("example.org");
        final IPMatcher ipMatcher = builder.build();

        assertFalse(ipMatcher.matches("1.1.1.1"));
    }

    @Test
    public void testBuilder() throws Exception
    {
        final IPMatcher ipMatcher = IPMatcher.builder()
            .addPatternOrHost("example.org")
            .addPattern("192.168.1.0/24")
            .build();
        assertTrue(ipMatcher.matches("192.168.1.13"));
    }
}
