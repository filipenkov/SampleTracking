package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

/**
 * Representation of a search request.
 *
 * @since v4.3
 */
public class SearchRequest
{
    public String jql = "";
    public Integer startAt;
    public Integer maxResults;

    public SearchRequest()
    {
    }

    public SearchRequest jql(String jql)
    {
        this.jql = jql;
        return this;
    }

    public SearchRequest startAt(Integer startAt)
    {
        this.startAt = startAt;
        return this;
    }

    public SearchRequest maxResults(Integer maxResults)
    {
        this.maxResults = maxResults;
        return this;
    }
}
