package com.atlassian.jira.projectconfig.tab;

import com.atlassian.jira.projectconfig.util.VelocityContextFactory;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.webresource.WebResourceManager;

/**
 * @since v4.4
 */
public class ComponentsTab extends WebPanelTab
{
    public ComponentsTab(final WebInterfaceManager webInterfaceManager, VelocityContextFactory factory)
    {
        super(webInterfaceManager, factory, "components", "view_project_components");
    }

    @Override
    public String getTitle(ProjectConfigTabRenderContext context)
    {
        return context.getI18NHelper().getText("admin.project.components.title", context.getProject().getName());
    }

    @Override
    public void addResourceForProject(ProjectConfigTabRenderContext context)
    {
        WebResourceManager manager = context.getResourceManager();
        manager.requireResource("com.atlassian.jira.jira-project-config-plugin:project-config-components");
    }
}
