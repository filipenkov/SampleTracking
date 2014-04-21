package com.atlassian.security.auth.trustedapps;

/**
 * Filter that is supposed to prevent attacks on cross-application trust feature by restricting a range of IP addresses
 * such request can originate from.
 */
public interface IPMatcher
{
    /**
     * check if this IP is allowed to perform trusted calls.
     * 
     * @throws IPAddressFormatException if the ip is not of the correct format.
     */
    boolean match(String ipAddress) throws IPAddressFormatException;
}