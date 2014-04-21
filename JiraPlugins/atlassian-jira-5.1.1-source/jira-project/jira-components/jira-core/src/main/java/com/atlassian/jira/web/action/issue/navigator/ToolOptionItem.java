package com.atlassian.jira.web.action.issue.navigator;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.0
 */
public class ToolOptionItem
{
    private final String id;
    private final String link;
    private final String label;
    private final String title;


    public ToolOptionItem(final String id, final String label, final String link, final String title)
    {
        this.link = link;
        this.label = label;
        this.id = id;
        this.title = title;
    }

    public String getLabel()
    {
        return label;
    }

    public String getId()
    {
        return id;
    }

    public String getLink()
    {
        return link;
    }

    public String getTitle()
    {
        return title;
    }
}
