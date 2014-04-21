package com.atlassian.jira.issue.search.quicksearch;

import java.util.List;

/**
 * Helper class to extract projects from the search result context.
 *
 * @since v3.13
 */
public interface ProjectAwareQuickSearchHandler
{
    /**
     * Retrieve the related project generic values from the search result
     *
     * @param searchResult search result to extract related project information from
     * @return all projects that may be related to this search
     */
    List/*<GenericValue>*/getProjects(QuickSearchResult searchResult);
}
