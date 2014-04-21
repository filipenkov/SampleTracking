package com.atlassian.crowd.search.ldap;

import org.springframework.ldap.filter.Filter;

/**
 * Holder representing empty set.
 */
public class NothingResult implements Filter
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
