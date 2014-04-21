package com.atlassian.jira.web.action.issue.navigator;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.0
 */
public class ToolOptionGroup
{
    private List<ToolOptionItem> items = new ArrayList<ToolOptionItem>();

    private String label;

    public ToolOptionGroup()
    {
    }

    public ToolOptionGroup(final String label)
    {
        this.label = label;
    }

    public void addItem(ToolOptionItem item)
    {
        items.add(item);
    }

    public String getLabel()
    {
        return label;
    }

    public List<ToolOptionItem> getItems()
    {
        return items;
    }

}
