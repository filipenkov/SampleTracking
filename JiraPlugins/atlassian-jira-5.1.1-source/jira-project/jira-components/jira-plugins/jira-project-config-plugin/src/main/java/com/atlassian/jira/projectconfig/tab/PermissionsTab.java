package com.atlassian.jira.projectconfig.tab;

import com.atlassian.jira.projectconfig.util.VelocityContextFactory;
import com.atlassian.plugin.web.WebInterfaceManager;

/**
 * @since v4.4
 */
public class PermissionsTab extends WebPanelTab
{
    public PermissionsTab(final WebInterfaceManager webInterfaceManager, VelocityContextFactory factory)
    {
        super(webInterfaceManager, factory, "permissions", "view_project_permissions");
    }

    @Override
    public String getTitle(ProjectConfigTabRenderContext context)
    {
        return context.getI18NHelper().getText("admin.project.permissions.title", context.getProject().getName());
    }
}
