package com.atlassian.jira.plugin.projectpanel.impl;

import com.atlassian.jira.plugin.browsepanel.AbstractFragmentBasedTabPanel;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanel;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanelModuleDescriptor;
import com.atlassian.jira.plugin.projectpanel.fragment.ProjectTabPanelFragment;
import com.atlassian.jira.project.browse.BrowseContext;

/**
 * Abstract child of {@link com.atlassian.jira.plugin.browsepanel.AbstractFragmentBasedTabPanel} that implements
 * {@link ProjectTabPanel}, so that all the concrete tab panels don't have to.
 *
 * @since v4.0
 */
abstract class AbstractFragmentBasedProjectTabPanel
        extends AbstractFragmentBasedTabPanel<ProjectTabPanelModuleDescriptor, BrowseContext, ProjectTabPanelFragment>
        implements ProjectTabPanel
{
}
