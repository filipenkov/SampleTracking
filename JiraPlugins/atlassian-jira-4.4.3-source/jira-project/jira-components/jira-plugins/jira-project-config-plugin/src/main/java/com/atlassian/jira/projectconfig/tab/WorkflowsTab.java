package com.atlassian.jira.projectconfig.tab;

import com.atlassian.jira.projectconfig.util.VelocityContextFactory;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.webresource.WebResourceManager;

/**
 * @since v4.4
 */
public class WorkflowsTab extends WebPanelTab
{
    public WorkflowsTab(final WebInterfaceManager webInterfaceManager, VelocityContextFactory factory)
    {
        super(webInterfaceManager, factory, "workflows", "view_project_workflows");
    }

    @Override
    public String getTitle(ProjectConfigTabRenderContext context)
    {
        return context.getI18NHelper().getText("admin.project.workflow.title", context.getProject().getName());
    }

    @Override
    public void addResourceForProject(ProjectConfigTabRenderContext context)
    {
        WebResourceManager manager = context.getResourceManager();
        manager.requireResource("com.atlassian.jira.jira-project-config-plugin:project-config-workflows");
    }

}
