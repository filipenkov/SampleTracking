package com.atlassian.security.auth.trustedapps;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * simple list based implementation. Matches using exact matches or wildcards like: 192.168.*.* or 192.145.372.*
 *
 * TODO: Add support for IPv6 and CIDR
 */
public class DefaultIPMatcher implements IPMatcher
{
    private static class AddressMask
    {
        private final int address;
        private final int mask;

        public AddressMask(final int address, final int mask)
        {
            this.address = address;
            this.mask = mask;
        }

        public boolean matches(final int otherAddress)
        {
            return address == (otherAddress & mask);
        }

        static AddressMask create(final int[] pattern)
        {
            int address = 0;
            int mask = 0;

            for (final int element : pattern)
            {
                address = address << 8;
                mask = mask << 8;

                if (element != -1)
                {
                    address = address | element;
                    mask = mask | 0xFF;
                }
            }

            return new AddressMask(address, mask);
        }
    }

    private static final String WILDCARD = "*";

    private final List<AddressMask> addressMasks;

    /**
     * Main ctor.
     * 
     * @param patterns the Set<String> of allowed pattern Strings
     * @throws IPAddressFormatException if the pattern does not represent a valid IP address
     */
    public DefaultIPMatcher(final Set<String> patterns) throws IPAddressFormatException
    {
        addressMasks = new LinkedList<AddressMask>();
        for (final String patternStr : patterns)
        {
            addressMasks.add(AddressMask.create(parsePatternString(patternStr)));
        }
    }

    public static int[] parsePatternString(final String patternStr)
    {
        final int[] pattern = new int[4];
        final StringTokenizer st = new StringTokenizer(patternStr, ".");
        if (st.countTokens() != 4)
        {
            throw new IPAddressFormatException(patternStr);
        }

        for (int i = 0; i < 4; i++)
        {
            final String token = st.nextToken().trim();
            if (WILDCARD.equals(token))
            {
                pattern[i] = -1;
            }
            else
            {
                try
                {
                    final int value = Integer.valueOf(token).intValue();

                    if ((value < 0) || (value > 255))
                    {
                        throw new IPAddressFormatException(patternStr);
                    }

                    pattern[i] = value;
                }
                catch (final NumberFormatException e)
                {
                    throw new IPAddressFormatException(patternStr);
                }
            }
        }
        return pattern;
    }

    public boolean match(final String ipAddress)
    {
        if (addressMasks.isEmpty())
        {
            return true;
        }

        final int address = toAddress(ipAddress);

        for (final Object element : addressMasks)
        {
            final AddressMask addressMask = (AddressMask) element;
            if (addressMask.matches(address))
            {
                return true;
            }
        }
        return false;
    }

    private int toAddress(final String ipAddress)
    {
        int address = 0;
        final int[] parsedIPAddr = parsePatternString(ipAddress);
        for (final int element : parsedIPAddr)
        {
            address = address << 8;
            address = address | element;
        }
        return address;
    }
}