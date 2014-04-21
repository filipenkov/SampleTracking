package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import java.util.List;

/**
 * Representation of a search result in the JIRA REST API.
 *
 * @since v4.3
 */
public class SearchResult
{
    public Integer startAt;
    public Integer maxResults;
    public Integer total;
    public List<Issue> issues;

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        SearchResult that = (SearchResult) o;

        if (issues != null ? !issues.equals(that.issues) : that.issues != null) { return false; }
        if (maxResults != null ? !maxResults.equals(that.maxResults) : that.maxResults != null) { return false; }
        if (startAt != null ? !startAt.equals(that.startAt) : that.startAt != null) { return false; }
        if (total != null ? !total.equals(that.total) : that.total != null) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = startAt != null ? startAt.hashCode() : 0;
        result = 31 * result + (maxResults != null ? maxResults.hashCode() : 0);
        result = 31 * result + (total != null ? total.hashCode() : 0);
        result = 31 * result + (issues != null ? issues.hashCode() : 0);
        return result;
    }
}
