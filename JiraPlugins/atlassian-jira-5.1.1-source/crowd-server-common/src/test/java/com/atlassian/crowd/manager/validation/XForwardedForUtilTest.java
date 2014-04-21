package com.atlassian.crowd.manager.validation;

import com.atlassian.crowd.manager.proxy.TrustedProxyManager;
import com.google.common.base.Joiner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link XForwardedForUtil}.
 *
 * @since 2.2
 */
public class XForwardedForUtilTest
{
    private final static String REMOTE_ADDRESS = "1.2.3.4";
    private final static String PROXY1_ADDRESS = "5.6.7.8";
    private final static String PROXY2_ADDRESS = "9.10.11.12";
    private final static String PROXY3_ADDRESS = "13.14.15.16";
    private String xffHeaderValue = null;

    @Mock
    private TrustedProxyManager trustedProxyManager;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        xffHeaderValue = Joiner.on(", ").join(REMOTE_ADDRESS, PROXY1_ADDRESS, PROXY2_ADDRESS);
    }

    @After
    public void tearDown() throws Exception
    {
        trustedProxyManager = null;
    }

    /**
     * Tests {@link XForwardedForUtil#getTrustedAddress(com.atlassian.crowd.manager.proxy.TrustedProxyManager, String, String)}
     * returns the remote address if there are no X-Forwarded-For headers.
     */
    @Test
    public void testGetTrustedAddress_NoXFF() throws Exception
    {
        String remoteAddress = XForwardedForUtil.getTrustedAddress(trustedProxyManager, REMOTE_ADDRESS, null);
        assertEquals(REMOTE_ADDRESS, remoteAddress);
        verify(trustedProxyManager, never()).isTrusted(anyString());
    }

    /**
     * Tests {@link XForwardedForUtil#getTrustedAddress(com.atlassian.crowd.manager.proxy.TrustedProxyManager, String, String)}
     * returns the remote address if all the proxies are trusted.
     */
    @Test
    public void testGetTrustedAddress_XFFWithAllTrustedProxies() throws Exception
    {
        when(trustedProxyManager.isTrusted(PROXY1_ADDRESS)).thenReturn(true);
        when(trustedProxyManager.isTrusted(PROXY2_ADDRESS)).thenReturn(true);
        when(trustedProxyManager.isTrusted(PROXY3_ADDRESS)).thenReturn(true);
        String remoteAddress = XForwardedForUtil.getTrustedAddress(trustedProxyManager, PROXY3_ADDRESS, xffHeaderValue);
        assertEquals(REMOTE_ADDRESS, remoteAddress);
        verify(trustedProxyManager).isTrusted(PROXY1_ADDRESS);
        verify(trustedProxyManager).isTrusted(PROXY2_ADDRESS);
        verify(trustedProxyManager).isTrusted(PROXY3_ADDRESS);
    }

    /**
     * Tests {@link XForwardedForUtil#getTrustedAddress(com.atlassian.crowd.manager.proxy.TrustedProxyManager, String, String)}
     * returns the last proxy address if none of the proxies are trusted.
     */
    @Test
    public void testGetTrustedAddress_XFFWithNoTrustedProxies() throws Exception
    {
        when(trustedProxyManager.isTrusted(PROXY1_ADDRESS)).thenReturn(false);
        when(trustedProxyManager.isTrusted(PROXY2_ADDRESS)).thenReturn(false);
        when(trustedProxyManager.isTrusted(PROXY3_ADDRESS)).thenReturn(false);
        String remoteAddress = XForwardedForUtil.getTrustedAddress(trustedProxyManager, PROXY3_ADDRESS, xffHeaderValue);
        assertEquals(PROXY3_ADDRESS, remoteAddress);
    }

    /**
     * Tests {@link XForwardedForUtil#getTrustedAddress(com.atlassian.crowd.manager.proxy.TrustedProxyManager, String, String)}
     * returns the last proxy address if even one of the proxies is untrusted.
     */
    @Test
    public void testGetTrustedAddress_XFFWithOneUntrustedProxies() throws Exception
    {
        when(trustedProxyManager.isTrusted(PROXY1_ADDRESS)).thenReturn(true);
        when(trustedProxyManager.isTrusted(PROXY2_ADDRESS)).thenReturn(false);
        when(trustedProxyManager.isTrusted(PROXY3_ADDRESS)).thenReturn(true);
        String remoteAddress = XForwardedForUtil.getTrustedAddress(trustedProxyManager, PROXY3_ADDRESS, xffHeaderValue);
        assertEquals(PROXY3_ADDRESS, remoteAddress);
    }
}
