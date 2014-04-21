package com.atlassian.ip;

import org.junit.Test;

import java.net.InetAddress;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SubnetTest
{
    @Test
    public void testInvalidHostNamePattern() throws Exception
    {
        assertFalse(Subnet.isValidPattern("ssdf"));
    }

    @Test
    public void testInvalidPostCIDRPattern() throws Exception
    {
        assertFalse(Subnet.isValidPattern("192.168.1.0/"));
    }

    @Test
    public void testInvalidPreCIDRPattern() throws Exception
    {
        assertFalse(Subnet.isValidPattern("/24"));
    }

    @Test
    public void testInvalidWildcardPattern() throws Exception
    {
        assertFalse(Subnet.isValidPattern("*.168.1.0"));
    }

    @Test
    public void testValidPattern() throws Exception
    {
        assertTrue(Subnet.isValidPattern("0.0.0.0"));
    }

    @Test
    public void testIPv4PatternParsing() throws Exception
    {
        Subnet subnet = Subnet.forPattern("100.100.100.100");
        assertEquals(32, subnet.getMask());
        assertArrayEquals(new byte[]{100, 100, 100, 100}, subnet.getAddress());
    }

    @Test
    public void testIPv6PatternParsing() throws Exception
    {
        Subnet subnet = Subnet.forPattern("::1");
        assertEquals(128, subnet.getMask());
        assertArrayEquals(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}, subnet.getAddress());
    }

    @Test
    public void testIPv4WildcardPatternParsing() throws Exception
    {
        Subnet subnet = Subnet.forPattern("100.100.*.*");
        assertEquals(16, subnet.getMask());
        assertArrayEquals(new byte[]{100, 100, 0, 0}, subnet.getAddress());
    }

    @Test
    public void testIPV4WildcardSubnetZeroPatternParsing() throws Exception
    {
        Subnet subnet = Subnet.forPattern("*.*.*.*");
        assertEquals(0, subnet.getMask());
    }

    @Test
    public void testIPv4CIDRPatternParsing() throws Exception
    {
        Subnet subnet = Subnet.forPattern("100.100.100.100/16");
        assertEquals(16, subnet.getMask());
        assertArrayEquals(new byte[]{100, 100, 100, 100}, subnet.getAddress());
    }

    @Test
    public void testIPv4CIDRSubnetZeroPatternParsing() throws Exception
    {
        Subnet subnet = Subnet.forPattern("100.100.100.100/0");
        assertEquals(0, subnet.getMask());
    }

    @Test
    public void testIPv6CIDRPatternParsing() throws Exception
    {
        Subnet subnet = Subnet.forPattern("::1/64");
        assertEquals(64, subnet.getMask());
        assertArrayEquals(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}, subnet.getAddress());
    }

    @Test
    public void testIPv4AddressParsing() throws Exception
    {
        Subnet subnet = Subnet.forAddress(InetAddress.getByName("1.0.0.1"));
        assertEquals(32, subnet.getMask());
        assertArrayEquals(new byte[]{1, 0, 0, 1}, subnet.getAddress());
    }

    @Test
    public void testIPv6AddressParsing() throws Exception
    {
        Subnet subnet = Subnet.forAddress(InetAddress.getByName("1::1"));
        assertEquals(128, subnet.getMask());
        assertArrayEquals(new byte[]{0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}, subnet.getAddress());
    }
}
