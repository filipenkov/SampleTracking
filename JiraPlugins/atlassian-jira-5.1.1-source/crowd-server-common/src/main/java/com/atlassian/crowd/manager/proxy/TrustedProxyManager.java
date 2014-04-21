package com.atlassian.crowd.manager.proxy;

import java.util.Set;

/**
 * Manages the list of trusted proxies.
 */
public interface TrustedProxyManager
{
    /**
     * Returns <tt>true</tt> if the address represents a configured trusted proxy. False otherwise.
     *
     * @param remoteAddress address of the proxy server
     * @return <tt>true</tt> if the address is a trusted proxy
     */
    boolean isTrusted(String remoteAddress);

    /**
     * Returns the addresses of all the trusted proxy servers, or an empty set if there are none.
     *
     * @return addresses of all the trusted proxy servers
     */
    Set<String> getAddresses();

    /**
     * Adds a remote address to the list of those trusted to act as proxies.
     *
     * @param remoteAddress address of a trusted proxy
     * @return <tt>true</tt> if the {@code remoteAddress} was added, false otherwise.
     */
    boolean addAddress(String remoteAddress);

    /**
     * Removes the address of the proxy from the list of trusted proxies.
     *
      * @param remoteAddress address of the proxy
     */
    void removeAddress(String remoteAddress);
}
