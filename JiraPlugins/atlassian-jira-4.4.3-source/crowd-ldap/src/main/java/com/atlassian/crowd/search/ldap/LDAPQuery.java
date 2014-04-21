package com.atlassian.crowd.search.ldap;

import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.Filter;
import org.springframework.ldap.filter.HardcodedFilter;

public class LDAPQuery
{
    private final AndFilter rootFilter;

    public LDAPQuery(final String objectFilter)
    {
        this.rootFilter = new AndFilter();
        rootFilter.and(new HardcodedFilter(objectFilter));
    }

    public void addFilter(Filter filter) 
    {
        rootFilter.append(filter);
    }

    public String encode()
    {
        return rootFilter.encode();
    }
}
