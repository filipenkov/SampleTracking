package com.atlassian.jira.plugin.projectpanel.fragment.impl;

import com.atlassian.jira.plugin.componentpanel.BrowseComponentContext;
import com.atlassian.jira.plugin.webfragment.model.ComponentHelper;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.web.component.webfragment.WebFragmentWebComponent;
import webwork.action.ActionContext;

/**
 * A filters menu fragment that produces a list of preset filters based on a component.
 *
 * @since v4.0
 */
public class FiltersMenuComponentFragment extends FiltersMenuFragment
{

    public FiltersMenuComponentFragment(final WebFragmentWebComponent webFragmentWebComponent)
    {
        super(webFragmentWebComponent);
    }

    /**
     * This changes the helper to be a componenthelper instead of a generic JiraHelper
     *
     * @param ctx This MUST be a BrowseComponentContext
     * @return a new {@link com.atlassian.jira.plugin.componentpanel.BrowseComponentContext} containing the current component.
     */
    JiraHelper getHelper(BrowseContext ctx)
    {
        try
        {
            return new ComponentHelper(ActionContext.getRequest(), (BrowseComponentContext) ctx);
        }
        catch (ClassCastException e)
        {
            log.error("The supplied context must be of type BrowseComponentContext", e);
            throw new IllegalArgumentException("The supplied context must be of type BrowseComponentContext", e);
        }
    }
}
