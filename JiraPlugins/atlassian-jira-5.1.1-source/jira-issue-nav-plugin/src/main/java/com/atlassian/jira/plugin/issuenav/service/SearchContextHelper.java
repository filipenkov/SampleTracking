package com.atlassian.jira.plugin.issuenav.service;

import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.transport.FieldValuesHolder;

/**
 * Utilities for generating search context
 * TODO: copied from com.atlassian.jira.web.action.issue.SearchDescriptionEnabledAction; should merge into jira core
 * @since v5.1
 */
public interface SearchContextHelper
{
    public static class SearchContextWithFieldValues
    {
        public final SearchContext searchContext;
        public final FieldValuesHolder fieldValuesHolder;

        public SearchContextWithFieldValues(SearchContext searchContext, FieldValuesHolder fieldValuesHolder)
        {
            this.searchContext = searchContext;
            this.fieldValuesHolder = fieldValuesHolder;
        }
    }

    public SearchContextWithFieldValues getSearchContextWithFieldValuesFromJqlString(final String query);
    
    public SearchContext getSearchContextFromJqlString(final String query);
}
