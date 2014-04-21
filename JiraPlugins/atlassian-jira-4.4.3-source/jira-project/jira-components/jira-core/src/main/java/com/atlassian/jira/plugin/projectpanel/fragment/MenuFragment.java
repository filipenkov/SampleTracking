package com.atlassian.jira.plugin.projectpanel.fragment;

import com.atlassian.jira.project.browse.BrowseContext;

/**
 * A fragment displayed on a BrowseProject/Component/Version in the appropriate section for menus.
 *
 * @since v4.0
 */
public interface MenuFragment
{
    /**
     * The id of the fragment div
     *
     * @return a valid html id string that represents this menu item/frag.
     */
    String getId();

    /**
     * The Html representation of this menu.  Typically an anchor followed by an unordered list.
     *
     * @param ctx The context under which this menu is being used. Project, component, version...
     * @return valid html for this menu fragment
     */
    String getHtml(BrowseContext ctx);

    /**
     * Whether or not to show this menu fragment.
     *
     * @param ctx The context under which this menu is being used. Project, component, version...
     * @return true is the menu should be shown, false otherwise.
     */
    boolean showFragment(BrowseContext ctx);

}
