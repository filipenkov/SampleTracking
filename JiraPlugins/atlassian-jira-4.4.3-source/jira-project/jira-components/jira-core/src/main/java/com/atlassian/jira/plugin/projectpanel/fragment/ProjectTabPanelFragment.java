package com.atlassian.jira.plugin.projectpanel.fragment;

import com.atlassian.jira.project.browse.BrowseContext;

/**
 * Piece of HTML that is rendered portlet-like in a
 * {@link com.atlassian.jira.plugin.browsepanel.TabPanel}. Typically shows information specific to a
 * Project.
 *
 * @since v4.0
 */
public interface ProjectTabPanelFragment
{
    /**
     * Returns fragmet's ID.
     *
     * @return id of the fragment, never null
     */
    // NOTE: We need to ensure that all IDs are unique (as required by HTML specification).
    // When implementing ProjectTabPanelFragment ensure you add it to TestProjectTabPanelFragment test!
    String getId();

    /**
     * Renders the fragment.
     *
     * @param ctx The context that this fragment is being rendered in.
     * @return The escaped HTML to include.
     */
    String getHtml(BrowseContext ctx);

    /**
     * Whether or not to include a fragment.  E.g. check permissios or if there is no contnent to render
     *
     * @param ctx The context that this fragment is being rendered in.
     * @return true if the fragment should be shown, false otherwise
     */
    boolean showFragment(BrowseContext ctx);
}
