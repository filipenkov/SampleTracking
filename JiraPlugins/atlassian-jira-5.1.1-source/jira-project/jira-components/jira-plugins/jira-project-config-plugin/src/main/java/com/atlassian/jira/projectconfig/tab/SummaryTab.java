package com.atlassian.jira.projectconfig.tab;

import com.atlassian.jira.projectconfig.util.VelocityContextFactory;
import com.atlassian.plugin.web.WebInterfaceManager;

/**
 * @since v4.4
 */
public class SummaryTab extends WebPanelTab implements ProjectConfigTab
{
    public static final String NAME = "summary";
    public static final String LINK_ID = "view_project_summary";

    public SummaryTab(final WebInterfaceManager webInterfaceManager, VelocityContextFactory contextFactory)
    {
        super(webInterfaceManager, contextFactory, NAME, LINK_ID);
    }

    public String getTitle(ProjectConfigTabRenderContext context)
    {
        return context.getI18NHelper().getText("admin.project.config.summary.title", context.getProject().getName());
    }

    @Override
    public void addResourceForProject(ProjectConfigTabRenderContext context)
    {
        context.getResourceManager().requireResource("com.atlassian.jira.jira-project-config-plugin:project-config-summary");
    }
}
