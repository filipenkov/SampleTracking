package com.atlassian.security.auth.trustedapps;

import org.apache.commons.httpclient.ProxyHost;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the operation of the {@link ProxyHostSelector} class.
 *
 * @since v2.5.2
 */
public class TestProxyHostSelector
{
    private static final URI ATLASSIAN_URI = URI.create("http://atlassian.com");
    private static final InetSocketAddress ADDR_LOCALHOST = new InetSocketAddress("localhost",8099);
    private static final InetSocketAddress ADDR_ATLASSIAN = new InetSocketAddress("atlassian.com",8088);

    private static final ProxyHost PROXY_HOST_ATLASSIAN =
            new ProxyHost(ADDR_ATLASSIAN.getAddress().getHostAddress(), ADDR_ATLASSIAN.getPort());

    /**
     * Tests that the {@link ProxyHostSelector#withDefaultProxySelector} method returns an instance
     * whose proxy selector is the default proxy selector.
     */
    @Test
    public void withDefaultProxySelector()
    {
        assertSame(ProxySelector.getDefault(), ProxyHostSelector.withDefaultProxySelector().getProxySelector());
    }

    /**
     * Tests that, when the proxy selector returns {@link Proxy#NO_PROXY},
     * the {@code select} method returns null
     */
    @Test
    public void noProxy()
    {
        ProxySelector sel = mockProxySelector(Proxy.NO_PROXY);
        assertNull(ProxyHostSelector.withProxySelector(sel).select(ATLASSIAN_URI));
    }

    /**
     * Tests that, when the proxy selector returns a list of <b>one</b> proxy,
     * the {@code select} method returns a proxy host corresponding to that proxy.
     */
    @Test
    public void oneProxy()
    {
        ProxySelector sel = mockProxySelector(new Proxy(Proxy.Type.HTTP, ADDR_ATLASSIAN));
        assertEquals(PROXY_HOST_ATLASSIAN, ProxyHostSelector.withProxySelector(sel).select(ATLASSIAN_URI));
    }

    /**
     * Tests that, when the proxy selector returns a list of <b>two</b> proxies,
     * the {@code select} method returns a proxy host corresponding the first
     * non-socks proxy in that list.
     */
    @Test
    public void twoProxies()
    {
        final Proxy p1 = new Proxy(Proxy.Type.SOCKS, ADDR_LOCALHOST);
        final Proxy p2 = new Proxy(Proxy.Type.HTTP, ADDR_ATLASSIAN);
        ProxySelector sel = mockProxySelector(p1, p2);
        assertEquals(PROXY_HOST_ATLASSIAN, ProxyHostSelector.withProxySelector(sel).select(ATLASSIAN_URI));
    }

    /**
     * Tests that, when the proxy selector returns a list containing only socks proxies,
     * the {@code select} method returns null.
     */
    @Test
    public void onlySocksProxies()
    {
        final Proxy p1 = new Proxy(Proxy.Type.SOCKS, ADDR_LOCALHOST);
        final Proxy p2 = new Proxy(Proxy.Type.SOCKS, ADDR_ATLASSIAN);
        ProxySelector sel = mockProxySelector(p1, p2);

        assertNull(ProxyHostSelector.withProxySelector(sel).select(ATLASSIAN_URI));
    }

    /**
     * Returns a mock proxy selector whose select method will return the given list of proxies.
     *
     * @param proxies the proxies to be returned by the {@code select} method
     *
     * @return a mock proxy selector
     */
    private static ProxySelector mockProxySelector(Proxy... proxies)
    {
        ProxySelector sel = mock(ProxySelector.class);
        when(sel.select(Mockito.<URI>anyObject())).thenReturn(Arrays.asList(proxies));
        return sel;
    }
}
