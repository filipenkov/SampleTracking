package com.atlassian.jira.plugin.componentpanel.impl;

import com.atlassian.jira.plugin.browsepanel.AbstractFragmentBasedTabPanel;
import com.atlassian.jira.plugin.componentpanel.BrowseComponentContext;
import com.atlassian.jira.plugin.componentpanel.ComponentTabPanel;
import com.atlassian.jira.plugin.componentpanel.ComponentTabPanelModuleDescriptor;
import com.atlassian.jira.plugin.componentpanel.fragment.ComponentTabPanelFragment;

/**
 * Abstract child of {@link com.atlassian.jira.plugin.browsepanel.AbstractFragmentBasedTabPanel} that implements {@link
 * com.atlassian.jira.plugin.componentpanel.ComponentTabPanel}, so that all the concrete tab panels don't have to.
 *
 * @since v4.0
 */
public abstract class AbstractFragmentBasedComponentTabPanel
        extends AbstractFragmentBasedTabPanel<ComponentTabPanelModuleDescriptor, BrowseComponentContext, ComponentTabPanelFragment>
        implements ComponentTabPanel
{
}
