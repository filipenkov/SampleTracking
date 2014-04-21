package com.atlassian.jira.projectconfig.tab;

import com.atlassian.jira.projectconfig.util.VelocityContextFactory;
import com.atlassian.plugin.web.WebInterfaceManager;

/**
 * @since v4.4
 */
public class IssuePermissionsTab extends WebPanelTab
{
    public IssuePermissionsTab(final WebInterfaceManager webInterfaceManager, VelocityContextFactory factory)
    {
        super(webInterfaceManager, factory, "issuepermissions", "view_issue_permissions");
    }

    @Override
    public String getTitle(ProjectConfigTabRenderContext context)
    {
        return context.getI18NHelper().getText("admin.project.issuepermissions.title", context.getProject().getName());
    }
}
