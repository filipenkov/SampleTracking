package com.atlassian.jira.projectconfig.tab;

/**
 * Repesents a tab on the project configuration page.
 *
 * @since v4.4
 */
public interface ProjectConfigTab
{
    /**
     * Get the ID for the panel.
     *
     * @return the ID of the panel.
     */
    public String getId();

    /**
     * Return the ID of the tab link that opens this tab.
     *
     * @return the ID of the tab link that opens this tab.
     */
    public String getLinkId();

    /**
     * Return the tab content.
     *
     * @param context contains information that may be useful for tab renderering.
     *
     * @return the HTML of the tab.
     */
    public String getTab(ProjectConfigTabRenderContext context);

    /**
     * Return the title to display in the browser when the tab is displayed.
     *
     * @param context contains information that may be useful for tab renderering.
     *
     * @return the title to display on the screen.
     */
    public String getTitle(ProjectConfigTabRenderContext context);

    /**
     * Add any webresource that the tab needs to render itself.
     *
     * @param context contains information that may be useful for tab renderering.
     */
    public void addResourceForProject(ProjectConfigTabRenderContext context);
}
