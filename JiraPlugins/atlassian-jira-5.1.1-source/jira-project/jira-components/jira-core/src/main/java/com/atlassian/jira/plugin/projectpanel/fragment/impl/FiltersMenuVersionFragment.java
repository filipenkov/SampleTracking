package com.atlassian.jira.plugin.projectpanel.fragment.impl;

import com.atlassian.jira.plugin.versionpanel.BrowseVersionContext;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.VersionHelper;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.web.component.webfragment.WebFragmentWebComponent;
import webwork.action.ActionContext;

/**
 * A filters menu fragment that produces a list of preset filters based on a version.
 *
 * @since v4.0
 */
public class FiltersMenuVersionFragment extends FiltersMenuFragment
{
    public FiltersMenuVersionFragment(final WebFragmentWebComponent webFragmentWebComponent)
    {
        super(webFragmentWebComponent);
    }

    /**
     * This changes the helper to be a {@link com.atlassian.jira.plugin.webfragment.model.VersionHelper} instead of a
     * generic {@link JiraHelper}
     *
     * @param ctx This MUST be a BrowseVersionContext
     * @return a new {@link JiraHelper} based on the current context.
     */
    JiraHelper getHelper(BrowseContext ctx)
    {
        try
        {
            return new VersionHelper(ActionContext.getRequest(), ((BrowseVersionContext) ctx));
        }
        catch (ClassCastException e)
        {
            log.error("The supplied context must be of type BrowseVersionContext", e);
            throw new IllegalArgumentException("The supplied context must be of type BrowseVersionContext", e);
        }
    }
}