package com.atlassian.gadgets.renderer.internal.http;

import java.net.URI;

import com.atlassian.gadgets.opensocial.spi.Whitelist;
import com.atlassian.gadgets.renderer.internal.http.DelegatingWhitelist;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DelegatingWhitelistTest
{
    @Test
    public void uriIsRejectedWhenNoOptionalWhitelistsArePresentAndDelegateRejectsUri()
    {
        Whitelist whitelist = new DelegatingWhitelist(alwaysReject(), ImmutableList.<Whitelist>of());
        assertFalse(whitelist.allows(URI.create("http://example.com")));
    }
    
    @Test
    public void uriIsAllowedWhenNoOptionalWhitelistsArePresentAndDelegateAllowsUri()
    {
        Whitelist whitelist = new DelegatingWhitelist(alwaysAllow(), ImmutableList.<Whitelist>of());
        assertTrue(whitelist.allows(URI.create("http://example.com")));
    }
    
    @Test
    public void uriIsRejectedWhenAllDelegatesRejectTheUri()
    {
        Whitelist whitelist = new DelegatingWhitelist(alwaysReject(), ImmutableList.of(alwaysReject(), alwaysReject(), alwaysReject()));
        assertFalse(whitelist.allows(URI.create("http://example.com")));
    }

    @Test
    public void uriIsAllowedWhenAnMainDelegateAllowsTheUriAndOptionalDelegatesAllReject()
    {
        Whitelist whitelist = new DelegatingWhitelist(alwaysAllow(), ImmutableList.of(alwaysReject(), alwaysAllow(), alwaysReject()));
        assertTrue(whitelist.allows(URI.create("http://example.com")));
    }

    @Test
    public void uriIsAllowedWhenAnOptionalDelegateAllowsTheUri()
    {
        Whitelist whitelist = new DelegatingWhitelist(alwaysReject(), ImmutableList.of(alwaysReject(), alwaysAllow(), alwaysReject()));
        assertTrue(whitelist.allows(URI.create("http://example.com")));
    }
    
    private Whitelist alwaysAllow()
    {
        return AlwaysAllowWhitelist.INSTANCE;
    }

    enum AlwaysAllowWhitelist implements Whitelist
    {
        INSTANCE;

        public boolean allows(URI uri)
        {
            return true;
        }
    }

    private Whitelist alwaysReject()
    {
        return AlwaysRejectWhitelist.INSTANCE;
    }
    
    enum AlwaysRejectWhitelist implements Whitelist
    {
        INSTANCE;

        public boolean allows(URI uri)
        {
            return false;
        }
    }
}
