package com.atlassian.jira.plugin.issuetabpanel;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractIssueAction implements IssueAction
{
    protected final IssueTabPanelModuleDescriptor descriptor;

    public AbstractIssueAction(IssueTabPanelModuleDescriptor descriptor)
    {
        this.descriptor = descriptor;
    }

    public abstract Date getTimePerformed();

    public String getHtml()
    {
        Map params = new HashMap();
        populateVelocityParams(params);
        return descriptor.getHtml("view", params);
    }

    protected abstract void populateVelocityParams(Map params);

    public boolean isDisplayActionAllTab()
    {
        return true;
    }
}
