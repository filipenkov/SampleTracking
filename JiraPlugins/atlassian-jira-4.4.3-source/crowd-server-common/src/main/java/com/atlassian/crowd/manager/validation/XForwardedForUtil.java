package com.atlassian.crowd.manager.validation;

import com.atlassian.crowd.manager.proxy.TrustedProxyManager;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for handling the X-Forwarded-For (XFF) HTTP request header.
 *
 * @since 2.2
 */
public class XForwardedForUtil
{
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";

    private XForwardedForUtil() {}

    /**
     * Returns the originating client address the proxies are forwarding for if the proxies are trusted, otherwise, return the
     * request address.
     *
     * @param trustedProxyManager used to determine if the proxy address is trusted
     * @param request HTTP request
     * @return originating client address if the proxies are trusted, otherwise, the request address is returned
     */
    public static InetAddress getTrustedAddress(TrustedProxyManager trustedProxyManager, HttpServletRequest request)
    {
        final String trustedAddress =  getTrustedAddress(trustedProxyManager, request.getRemoteAddr(), request.getHeader(X_FORWARDED_FOR));
        try
        {
            // Use InetAddress as Guava cannot currently parse IPv6 scopes
            return InetAddress.getByName(trustedAddress);
        }
        catch (UnknownHostException e)
        {
            throw new RuntimeException(e); // This should never happen
        }
    }

    /**
     * Returns the originating client address the proxies are forwarding for if all the proxies are trusted, otherwise,
     * return the request address.
     *
     * @param trustedProxyManager used to determine if the proxy address is trusted
     * @param requestAddress HTTP request address
     * @param xForwardedFor X-Forwarded-For header
     * @return originating client address if the proxies are trusted, otherwise, the request address is returned
     */
    public static String getTrustedAddress(TrustedProxyManager trustedProxyManager, String requestAddress, String xForwardedFor)
    {
        RequestAddressInfo holder = getRequestAddressInfo(requestAddress, xForwardedFor);
        if (!holder.getProxies().isEmpty())
        {
            for (String proxy : holder.getProxies())
            {
                if (!trustedProxyManager.isTrusted(proxy))
                {
                    return holder.getRequestAddress();
                }
            }
            return holder.getClientAddress();
        }
        else
        {
            return holder.getClientAddress();
        }
    }

    /**
     * Returns the client address and proxies.
     *
     * @param requestAddress the HTTP request address
     * @param xForwardedFor the X-Forwarded-For header
     * @return the request address info
     */
    private static RequestAddressInfo getRequestAddressInfo(String requestAddress, String xForwardedFor)
    {
        String clientAddress = requestAddress;
        List<String> proxies = Lists.newArrayList();
        if (StringUtils.isNotBlank(xForwardedFor))
        {
            proxies.addAll(Arrays.asList(StringUtils.split(xForwardedFor, ", ")));
            proxies.add(requestAddress);

            clientAddress = proxies.remove(0);
        }
        return new RequestAddressInfo(requestAddress, clientAddress, proxies);
    }

    /**
     * Class for holding the request address, client address and proxy addresses.
     */
    private static class RequestAddressInfo
    {
        private final String requestAddress;
        private final String clientAddress;
        private final List<String> proxies;

        public RequestAddressInfo(final String requestAddress, final String clientAddress, final List<String> proxies)
        {
            this.requestAddress = requestAddress;
            this.clientAddress = clientAddress;
            this.proxies = ImmutableList.copyOf(proxies);
        }

        /**
         * Returns the address of the HTTP request. This address is the last proxy if a proxy was involved in forwarding
         * the request, or the client address if there are no proxies involved.
         *
         * @return address of the HTTP request
         */
        public String getRequestAddress()
        {
            return requestAddress;
        }

        /**
         * Returns the address of the originating client making the HTTP request.
         *
         * @return address of the originating client making the HTTP request
         */
        public String getClientAddress()
        {
            return clientAddress;
        }

        /**
         * Returns the list of proxies, or an empty list if there are no proxies.
         *
         * @return the list of proxies, or an empty list if there are no proxies.
         */
        public List<String> getProxies()
        {
            return proxies;
        }
    }
}
