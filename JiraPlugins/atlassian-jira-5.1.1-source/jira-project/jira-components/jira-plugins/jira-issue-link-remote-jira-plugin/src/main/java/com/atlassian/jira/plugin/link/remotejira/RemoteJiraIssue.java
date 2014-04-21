package com.atlassian.jira.plugin.link.remotejira;

/**
 * A {@link com.atlassian.jira.issue.link.RemoteIssueLink} to an issue in a remote JIRA instance
 *
 * @since v5.0
 */
public class RemoteJiraIssue
{
    private final Long id;
    private final String key;
    private final String summary;
    private final String iconUrl;
    private final String iconTitle;
    private final String statusIconUrl;
    private final String statusIconTitle;
    private final boolean resolved;
    private final String browseUrl;

    public RemoteJiraIssue(final Long id, final String key, final String summary, final String iconUrl, final String iconTitle, final String statusIconUrl, final String statusIconTitle, final boolean resolved, final String browseUrl)
    {
        this.id = id;
        this.key = key;
        this.summary = summary;
        this.iconUrl = iconUrl;
        this.iconTitle = iconTitle;
        this.statusIconUrl = statusIconUrl;
        this.statusIconTitle = statusIconTitle;
        this.resolved = resolved;
        this.browseUrl = browseUrl;
    }

    public Long getId()
    {
        return id;
    }

    public String getKey()
    {
        return key;
    }

    public String getSummary()
    {
        return summary;
    }

    public String getIconUrl()
    {
        return iconUrl;
    }

    public String getIconTitle()
    {
        return iconTitle;
    }

    public String getStatusIconUrl()
    {
        return statusIconUrl;
    }

    public String getStatusIconTitle()
    {
        return statusIconTitle;
    }

    public boolean isResolved()
    {
        return resolved;
    }

    public String getBrowseUrl()
    {
        return browseUrl;
    }
}
