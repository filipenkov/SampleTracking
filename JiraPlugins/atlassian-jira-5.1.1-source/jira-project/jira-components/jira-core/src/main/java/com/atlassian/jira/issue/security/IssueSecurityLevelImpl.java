package com.atlassian.jira.issue.security;

import javax.annotation.concurrent.Immutable;

@Immutable
public class IssueSecurityLevelImpl implements IssueSecurityLevel
{
    private final Long id;
    private final String name;
    private final String description;
    private final Long schemeId;

    public IssueSecurityLevelImpl(Long id, String name, String description, Long schemeId)
    {
        this.id = id;
        this.name = (name != null) ? name.intern() : null;
        this.description = (description != null) ? description.intern() : null;
        this.schemeId = schemeId;
    }

    public Long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public Long getSchemeId()
    {
        return schemeId;
    }
}
