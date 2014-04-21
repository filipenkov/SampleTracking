package com.atlassian.jira.plugin.versionpanel.impl;

import com.atlassian.jira.plugin.browsepanel.AbstractFragmentBasedTabPanel;
import com.atlassian.jira.plugin.projectpanel.fragment.MenuFragment;
import com.atlassian.jira.plugin.versionpanel.BrowseVersionContext;
import com.atlassian.jira.plugin.versionpanel.VersionTabPanel;
import com.atlassian.jira.plugin.versionpanel.VersionTabPanelFragment;
import com.atlassian.jira.plugin.versionpanel.VersionTabPanelModuleDescriptor;

import java.util.Collections;
import java.util.List;

/**
 * Abstract child of {@link com.atlassian.jira.plugin.browsepanel.AbstractFragmentBasedTabPanel} that implements {@link
 * com.atlassian.jira.plugin.versionpanel.VersionTabPanel}, so that all the concrete tab panels don't have to.
 *
 * @since v4.0
 */
public abstract class AbstractFragmentBasedVersionTabPanel
        extends AbstractFragmentBasedTabPanel<VersionTabPanelModuleDescriptor, BrowseVersionContext, VersionTabPanelFragment>
        implements VersionTabPanel
{
    protected List<MenuFragment> getMenuFragments()
    {
        return Collections.emptyList();
    }
}