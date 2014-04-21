package com.atlassian.jira.plugin.issuenav.service;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.web.action.JiraWebActionSupport;

import java.util.Map;

/**
 * Kickass interface to searchers
 * @since v5.1
 */
public interface SearcherService
{
    public ServiceOutcome<SearchResults> search(JiraWebActionSupport action, Map<String, String[]> params);

    public ServiceOutcome<SearchResults> searchWithJql(JiraWebActionSupport action, String jql);

    public ServiceOutcome<SearchRendererValueResults> getViewHtml(JiraWebActionSupport action, Map<String, String[]> params);

    public ServiceOutcome<String> getEditHtml(String searcherId, String jqlContext, JiraWebActionSupport action);
    
    public Searchers getSearchers(String jqlContext);
}
