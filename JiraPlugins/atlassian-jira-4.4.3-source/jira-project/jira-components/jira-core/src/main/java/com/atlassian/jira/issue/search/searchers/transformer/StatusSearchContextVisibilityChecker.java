package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.renderer.StatusSearchRenderer;
import com.atlassian.jira.issue.status.Status;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A {@link com.atlassian.jira.issue.search.searchers.transformer.SearchContextVisibilityChecker} for status fields
 *
 * @since v4.0
 */
public class StatusSearchContextVisibilityChecker implements SearchContextVisibilityChecker
{
    private final StatusSearchRenderer statusSearchRenderer;

    public StatusSearchContextVisibilityChecker(final StatusSearchRenderer statusSearchRenderer)
    {
        this.statusSearchRenderer = statusSearchRenderer;
    }

    public Set<String> FilterOutNonVisibleInContext(final SearchContext searchContext, final Collection<String> ids)
    {
        final Collection<Status> visibleStatuses = statusSearchRenderer.getSelectListOptions(searchContext);
        Set<String> visibleIds = new HashSet<String>();
        for (Status visibleStatus : visibleStatuses)
        {
            String id = visibleStatus.getId();
            if (ids.contains(id))
            {
                visibleIds.add(visibleStatus.getId());
            }
        }
        return visibleIds;
    }
}