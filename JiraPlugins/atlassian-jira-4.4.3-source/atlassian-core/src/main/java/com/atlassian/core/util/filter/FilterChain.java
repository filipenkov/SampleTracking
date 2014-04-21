package com.atlassian.core.util.filter;

import com.atlassian.core.util.filter.Filter;

import java.util.List;
import java.util.ArrayList;

/**
 * Composite Filter that applies a series of filters in turn.
 *
 * By default, the composite filter lets all objects through, so 
 * a composite filter with no filters added,
 */
public class FilterChain implements Filter
{
    private List filters = new ArrayList();

    public FilterChain() {}

    public void addFilter(Filter filter)
    {
        filters.add(filter);
    }

    public boolean isIncluded(Object o)
    {
        for (int i = 0; i < filters.size(); i++)
        {
            if (!((Filter)filters.get(i)).isIncluded(o))
                return false;
        }

        return true;
    }
}
