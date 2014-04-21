package com.atlassian.crowd.integration.http.util;

import org.junit.Test;

import static org.junit.Assert.assertNull;

import static com.atlassian.crowd.integration.http.util.CrowdHttpValidationFactorExtractorImpl.remoteAddrWithoutIpv6ZoneId;
import static org.junit.Assert.assertEquals;

public class CrowdHttpValidationFactorExtractorImplTest
{
    @Test
    public void removesZoneFromIpv6Address()
    {
        assertEquals("0:0:0:0:0:0:0:1", remoteAddrWithoutIpv6ZoneId("0:0:0:0:0:0:0:1%0"));
    }

    @Test
    public void leavesUnzonedAddressAlone()
    {
        assertEquals("0:0:0:0:0:0:0:1", remoteAddrWithoutIpv6ZoneId("0:0:0:0:0:0:0:1"));
    }

    @Test
    public void removesZoneAsString()
    {
        assertEquals("fe80::3", remoteAddrWithoutIpv6ZoneId("fe80::3%eth0"));
    }

    @Test
    public void leavesIpv4Unaffected()
    {
        assertEquals("192.168.1.1", remoteAddrWithoutIpv6ZoneId("192.168.1.1"));
    }

    @Test
    public void passesNulls()
    {
        assertNull(remoteAddrWithoutIpv6ZoneId(null));
    }
}
