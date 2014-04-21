package com.atlassian.jira.dev.reference.plugin.tabpanels;

import com.atlassian.jira.plugin.projectpanel.impl.GenericProjectTabPanel;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.FieldVisibilityManager;

/**
 * Represents a simple reference project tab panel.
 *
 * @since v4.3
 */
public class ReferenceProjectTabPanel extends GenericProjectTabPanel
{
    public ReferenceProjectTabPanel(final JiraAuthenticationContext jiraAuthenticationContext,
            final FieldVisibilityManager fieldVisibilityManager)
    {
        super(jiraAuthenticationContext, fieldVisibilityManager);
    }

    @Override
    public boolean showPanel(BrowseContext ctx)
    {
        return true;
    }
}
