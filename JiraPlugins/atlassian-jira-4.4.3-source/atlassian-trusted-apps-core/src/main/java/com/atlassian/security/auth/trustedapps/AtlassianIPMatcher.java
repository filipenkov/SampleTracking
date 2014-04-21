package com.atlassian.security.auth.trustedapps;

import com.atlassian.ip.Subnet;

import java.util.Set;

/**
 * IPMatcher implementation that delegates to Atlassian IP.
 * <p>
 * Supports matching against IPv4 and IPv6 addresses, and subnets in both
 * wildcard (IPv4 only) and CIDR notation. Examples of valid patterns:
 * <pre>
 * 192.168.1.1
 * 192.168.1.0/24
 * 192.168.1.*
 * 0:0:0:1::1
 * 0:0:0:1::/64
 * </pre>
 *
 * @since 2.5
 */
public class AtlassianIPMatcher implements IPMatcher
{
    private final com.atlassian.ip.IPMatcher ipMatcher;

    /**
     * Main ctor.
     *
     * @param patterns the Set<String> of allowed pattern Strings
     * @throws com.atlassian.security.auth.trustedapps.IPAddressFormatException if the pattern does not represent a valid IP address
     */
    public AtlassianIPMatcher(final Set<String> patterns) throws IPAddressFormatException
    {
        if (!patterns.isEmpty()) {
            final com.atlassian.ip.IPMatcher.Builder builder = com.atlassian.ip.IPMatcher.builder();
            for (final String patternStr : patterns)
            {
                builder.addPattern(patternStr);
            }
            ipMatcher = builder.build();
        }
        else
        {
            ipMatcher = null;
        }
    }

    public boolean match(final String ipAddress)
    {
        // Allow all if there were no patterns
        return ipMatcher == null || ipMatcher.matches(ipAddress);
    }

    public static void parsePatternString(String pattern) throws IPAddressFormatException
    {
        if (!Subnet.isValidPattern(pattern))
        {
            throw new IPAddressFormatException(pattern);
        }
    }
}