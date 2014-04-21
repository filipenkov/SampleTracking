package com.atlassian.jira.projectconfig.tab;

import com.atlassian.jira.projectconfig.util.VelocityContextFactory;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.webresource.WebResourceManager;

/**
 * @since v4.4
 */
public class IssueTypesTab extends WebPanelTab
{
    public IssueTypesTab(final WebInterfaceManager webInterfaceManager, VelocityContextFactory factory)
    {
        super(webInterfaceManager, factory, "issuetypes", "view_project_issuetypes");
    }

    @Override
    public String getTitle(ProjectConfigTabRenderContext context)
    {
        return context.getI18NHelper().getText("admin.project.issuetypes.title", context.getProject().getName());
    }

}
