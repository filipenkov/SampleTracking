package com.atlassian.jira.web.action.util;

import com.atlassian.jira.issue.search.SearchRequest;

public class SimpleSearchRequestDisplay
{
    private final Long id;
    private final String name;
    private final String ownerName;

    public SimpleSearchRequestDisplay(final SearchRequest searchRequest)
    {
        id = searchRequest.getId();
        name = searchRequest.getName();
        ownerName = searchRequest.getOwnerUserName();
    }

    public Long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getOwnerUserName()
    {
        return ownerName;
    }
}
