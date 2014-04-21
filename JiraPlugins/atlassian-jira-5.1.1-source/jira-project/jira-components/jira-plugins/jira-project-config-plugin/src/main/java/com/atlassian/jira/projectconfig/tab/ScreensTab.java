package com.atlassian.jira.projectconfig.tab;

import com.atlassian.jira.projectconfig.util.VelocityContextFactory;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.webresource.WebResourceManager;

/**
 * @since v4.4
 */
public class ScreensTab extends WebPanelTab
{
    public ScreensTab(final WebInterfaceManager webInterfaceManager, VelocityContextFactory factory)
    {
        super(webInterfaceManager, factory, "screens", "view_project_screens");
    }

    @Override
    public String getTitle(ProjectConfigTabRenderContext context)
    {
        return context.getI18NHelper().getText("admin.project.screens.title", context.getProject().getName());
    }

}
