package com.atlassian.jira.plugin.searchrequestview;

import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.plugin.JiraResourcedModuleDescriptor;

/**
 * An search request view allows you to view a search request in different ways (eg XML, Word, PDF, Excel)
 *
 * @see SearchRequestView
 */
public interface SearchRequestViewModuleDescriptor extends JiraResourcedModuleDescriptor<SearchRequestView>
{
    public SearchRequestView getSearchRequestView();

    public String getContentType();

    public String getFileExtension();

    public String getURLWithoutContextPath(SearchRequest searchRequest);

    public boolean isBasicAuthenticationRequired();

    public boolean isExcludeFromLimitFilter();
}

