package com.atlassian.jira.crowd.embedded;

import sun.net.util.IPAddressUtil;

/**
 * Copied from Crowd because they deleted this file in Crowd v2.3.1
 *
 * @since v4.4
 */
public class RemoteAddressHelper
{

    /**
     * Determines if the provided string is a valid IP address
     * @param address IP address
     * @return true iff address is a valid IPv4 or IPv6 address
     */
    public static boolean isValidIPNotation(String address)
    {
        String addr = address;
        if (address.contains("/"))
        {
            String[] addressComponents = address.split("/");
            addr = addressComponents[0];
        }

        // TODO: Replace this when we get Google Guava because it uses sun.net.util.IPAddressUtil
        return (getAddressInBytes(addr) != null);
    }

    /**
     * Converts String representation of valid IP addresses into a byte array.
     * If address is not a valid IP address (eg. a hostname), no conversion will be made.
     * @param address hostname or IP address
     * @return a byte array representing the IP address if a valid IPv4 or IPv6 address has been given.
     * null if a hostname is provided, or an invalid IP address format is provided
     */
    private static byte[] getAddressInBytes(String address)
    {
        boolean ipv6Expected = false;
        // "...literal IPv6 addresses are enclosed in square brackets in such resource identifiers..."
        //  http://en.wikipedia.org/wiki/IPv6_address
        if (address.charAt(0) == '[')
        {
            // This is supposed to be an IPv6 litteral
            if (address.length() > 2 && address.charAt(address.length() - 1) == ']')
            {
                address = address.substring(1, address.length() - 1);
                ipv6Expected = true;
            }
            else
            {
                // This was supposed to be a IPv6 address, but it's not!
                return null;
            }
        }

        // if host is an IP address, we won't do further lookup
        if (Character.digit(address.charAt(0), 16) != -1 || (address.charAt(0) == ':'))
        {
            byte[] addr = null;
            // see if it is IPv4 address
            addr = IPAddressUtil.textToNumericFormatV4(address);
            if (addr == null)
            {
                // see if it is IPv6 address
                // Note: ignoring string zone id
                addr = IPAddressUtil.textToNumericFormatV6(address);
            }
            else if (ipv6Expected)
            {
                // Means an IPv4 literal between brackets!
                return null;
            }
            if (addr != null)
            {
                return addr;
            }
        }
        else if (ipv6Expected)
        {
            // We were expecting an IPv6 literal, but got something else
            return null;
        }

        // The address given did not match formats for either IPv4 or IPv6
        // Therefore it should be a hostname
        return null;
    }

}
