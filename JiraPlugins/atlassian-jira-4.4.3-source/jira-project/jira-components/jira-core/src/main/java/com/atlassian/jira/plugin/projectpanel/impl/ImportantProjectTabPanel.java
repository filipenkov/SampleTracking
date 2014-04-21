package com.atlassian.jira.plugin.projectpanel.impl;

import com.atlassian.jira.plugin.projectpanel.ProjectTabPanelModuleDescriptor;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.web.SessionKeys;
import webwork.action.ActionContext;

import java.util.Map;


public class ImportantProjectTabPanel extends AbstractProjectTabPanel
{
    public void init(ProjectTabPanelModuleDescriptor descriptor)
    {
        super.init(descriptor);
    }

    public boolean showPanel(BrowseContext ctx)
    {
        final Map<String, Object> session = ActionContext.getSession();

        final String report = (String) session.get(SessionKeys.PROJECT_BROWSER_CURRENT_TAB);

        return report != null && report.equals(descriptor.getCompleteKey());
    }
}
