package com.atlassian.jira.projectconfig.tab;

import com.atlassian.jira.projectconfig.util.VelocityContextFactory;
import com.atlassian.jira.web.action.util.CalendarResourceIncluder;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.webresource.WebResourceManager;

import java.util.Locale;

/**
 * @since v4.4
 */
public class VersionsTab extends WebPanelTab
{
   public VersionsTab(final WebInterfaceManager webInterfaceManager, VelocityContextFactory factory)
    {
        super(webInterfaceManager, factory, "versions", "view_project_versions");
    }

    public String getTitle(ProjectConfigTabRenderContext context)
    {
        return context.getI18NHelper().getText("admin.project.versions.title", context.getProject().getName());
    }

    @Override
    public void addResourceForProject(ProjectConfigTabRenderContext renderContext)
    {
        final Locale locale = renderContext.getLocale();
        final CalendarResourceIncluder calendarResourceIncluder = new CalendarResourceIncluder();
        calendarResourceIncluder.includeForLocale(locale);

        WebResourceManager manager = renderContext.getResourceManager();
        manager.requireResource("com.atlassian.jira.jira-project-config-plugin:project-config-versions");
    }
}
