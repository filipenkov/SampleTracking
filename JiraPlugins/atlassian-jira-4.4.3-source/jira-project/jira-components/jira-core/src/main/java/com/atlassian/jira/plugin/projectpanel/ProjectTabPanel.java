package com.atlassian.jira.plugin.projectpanel;

import com.atlassian.jira.plugin.browsepanel.TabPanel;
import com.atlassian.jira.project.browse.BrowseContext;

/**
 * A Tab panel to be displayed on the Browse Project page.
 */
public interface ProjectTabPanel extends TabPanel<ProjectTabPanelModuleDescriptor, BrowseContext>
{
    
}