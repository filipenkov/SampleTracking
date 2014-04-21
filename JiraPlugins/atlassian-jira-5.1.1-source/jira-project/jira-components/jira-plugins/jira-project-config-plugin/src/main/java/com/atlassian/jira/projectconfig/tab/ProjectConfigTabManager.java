package com.atlassian.jira.projectconfig.tab;

/**
 * @since v4.4
 */
public interface ProjectConfigTabManager
{
    /**
     * Return the tab with the specified ID.
     *
     * @param id the id of the tab to render.
     *
     * @return the tab with the passed id or null if such a tab does not exist.
     */
    public ProjectConfigTab getTabForId(String id);
}
