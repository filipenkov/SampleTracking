package com.atlassian.jira.plugin.projectpanel.fragment.impl;

import com.atlassian.jira.plugin.projectpanel.fragment.MenuFragment;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.ProjectHelper;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.project.browse.BrowseProjectContext;
import com.atlassian.jira.web.component.webfragment.WebFragmentWebComponent;
import org.apache.log4j.Logger;
import webwork.action.ActionContext;

/**
 * Displays a Project Filters dropdown on the browse project page.
 * <p/>
 *
 * @since v4.0
 */
public class FiltersMenuFragment implements MenuFragment
{
    protected static final Logger log = Logger.getLogger(FiltersMenuFragment.class);

    private final WebFragmentWebComponent webFragmentWebComponent;

    private final static String TEMPLATE_PATH = "templates/plugins/jira/projectpanels/fragments/summary/menu/filtersmenu.vm";
    private static final String SYSTEM_PRESET_FILTERS = "system.preset.filters";
    private static final String FILTERS_ID = "filters";

    public FiltersMenuFragment(final WebFragmentWebComponent webFragmentWebComponent)
    {
        this.webFragmentWebComponent = webFragmentWebComponent;
    }

    public String getId()
    {
        return FILTERS_ID;
    }

    public String getHtml(final BrowseContext ctx)
    {
        final JiraHelper jiraHelper = getHelper(ctx);
        return webFragmentWebComponent.getHtml(TEMPLATE_PATH, SYSTEM_PRESET_FILTERS, jiraHelper);
    }

    JiraHelper getHelper(BrowseContext ctx)
    {
        return new ProjectHelper(ActionContext.getRequest(), (BrowseProjectContext) ctx);
    }

    public boolean showFragment(final BrowseContext ctx)
    {
        final JiraHelper jiraHelper = getHelper(ctx);
        return webFragmentWebComponent.hasDisplayableItems(SYSTEM_PRESET_FILTERS, jiraHelper);
    }
}
