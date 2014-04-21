package com.atlassian.jira.plugin.projectpanel.fragment.impl;

import com.atlassian.jira.plugin.componentpanel.fragment.ComponentTabPanelFragment;
import com.atlassian.jira.plugin.versionpanel.VersionTabPanelFragment;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;

/**
 * Base class for all fragments which are multi-purpose i.e. can be used as {@link ComponentTabPanelFragment}s
 * or {@link VersionTabPanelFragment}s.
 *
 * @since v4.0
 */
public abstract class AbstractMultiFragment extends AbstractFragment implements ComponentTabPanelFragment, VersionTabPanelFragment
{
    protected AbstractMultiFragment(final VelocityTemplatingEngine templatingEngine,
            final JiraAuthenticationContext jiraAuthenticationContext)
    {
        super(templatingEngine, jiraAuthenticationContext);
    }
}
