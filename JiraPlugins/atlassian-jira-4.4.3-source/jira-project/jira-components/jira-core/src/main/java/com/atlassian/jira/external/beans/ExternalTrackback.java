package com.atlassian.jira.external.beans;

import java.util.Date;

/**
 * Used to represent a trackback.
 *
 * @since v3.13
 */
public class ExternalTrackback
{
    private final String id;
    private final String issueId;
    private final String url;
    private final String blogName;
    private final String excerpt;
    private final String title;
    private final Date created;

    public ExternalTrackback(final String id, final String issueId, final String url, final String blogName, final String excerpt, final String title, final Date created)
    {
        this.id = id;
        this.issueId = issueId;
        this.url = url;
        this.blogName = blogName;
        this.excerpt = excerpt;
        this.title = title;
        this.created = created == null ? null : new Date(created.getTime());
    }

    public String getId()
    {
        return id;
    }

    public String getIssueId()
    {
        return issueId;
    }

    public String getUrl()
    {
        return url;
    }

    public String getBlogName()
    {
        return blogName;
    }

    public String getExcerpt()
    {
        return excerpt;
    }

    public String getTitle()
    {
        return title;
    }

    public Date getCreated()
    {
        return created == null ? null : new Date(created.getTime());
    }
}
