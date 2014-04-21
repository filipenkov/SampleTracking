package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import java.util.Arrays;
import java.util.List;

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
    public List<String> fields;
    public List<String> expand;

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

    public SearchRequest fields(String... fields)
    {
        this.fields = fields != null ? Arrays.asList(fields) : null;
        return this;
    }

    public SearchRequest fields(List<String> fields)
    {
        this.fields = fields;
        return this;
    }

    public SearchRequest expand(String... expand)
    {
        this.expand = expand != null ? Arrays.asList(expand) : null;
        return this;
    }

    public SearchRequest expand(List<String> expand)
    {
        this.expand = expand;
        return this;
    }
}
