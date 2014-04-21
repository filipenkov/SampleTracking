package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.jira.issue.search.SearchContext;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A {@link com.atlassian.jira.issue.search.searchers.transformer.SearchContextVisibilityChecker} that always returns true
 *
 * @since v4.0
 */
public class AlwaysVisibleSearchContextVisibilityChecker implements SearchContextVisibilityChecker
{

    ///CLOVER:OFF
    public Set<String> FilterOutNonVisibleInContext(final SearchContext searchContext, final Collection<String> ids)
    {
        return new HashSet<String>(ids);
    }
    ///CLOVER:ON
}
