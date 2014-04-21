package com.atlassian.jira.projectconfig.tab;

import com.atlassian.jira.projectconfig.util.VelocityContextFactory;
import com.atlassian.plugin.web.WebInterfaceManager;

/**
 * @since v4.4
 */
public class IssueSecurityTab extends WebPanelTab
{
    public IssueSecurityTab(final WebInterfaceManager webInterfaceManager, VelocityContextFactory factory)
    {
        super(webInterfaceManager, factory, "issuesecurity", "view_project_issuesecurity");
    }

    @Override
    public String getTitle(ProjectConfigTabRenderContext context)
    {
        return context.getI18NHelper().getText("admin.project.issuesecurity.title", context.getProject().getName());
    }

}
