package com.atlassian.jira.plugin.issuetabpanel;

public abstract class AbstractIssueTabPanel implements IssueTabPanel
{
    protected IssueTabPanelModuleDescriptor descriptor;

    public void init(IssueTabPanelModuleDescriptor descriptor)
    {
        this.descriptor = descriptor;
    }
}
