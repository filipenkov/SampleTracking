package com.atlassian.crowd.search.ldap;

import org.springframework.ldap.filter.Filter;

/**
 * Holder representing complete set.
 */
public class EverythingResult implements Filter
{
    public String encode()
    {
        return null;
    }

    public StringBuffer encode(final StringBuffer buf)
    {
        return null;
    }
}
