package com.atlassian.jira.projectconfig.tab;

import com.atlassian.jira.projectconfig.util.VelocityContextFactory;
import com.atlassian.jira.web.action.util.CalendarResourceIncluder;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.webresource.WebResourceManager;

import java.util.Locale;

/**
 * @since v4.4
 */
public class PeopleTab extends WebPanelTab
{
    public PeopleTab(final WebInterfaceManager webInterfaceManager, VelocityContextFactory factory)
    {
        super(webInterfaceManager, factory, "people", "view_project_people");
    }

    @Override
    public String getTitle(ProjectConfigTabRenderContext context)
    {
        return context.getI18NHelper().getText("admin.project.people.title", context.getProject().getName());
    }

    @Override
    public void addResourceForProject(ProjectConfigTabRenderContext renderContext)
    {
        WebResourceManager manager = renderContext.getResourceManager();
        manager.requireResource("com.atlassian.jira.jira-project-config-plugin:project-config-people");
    }

}
