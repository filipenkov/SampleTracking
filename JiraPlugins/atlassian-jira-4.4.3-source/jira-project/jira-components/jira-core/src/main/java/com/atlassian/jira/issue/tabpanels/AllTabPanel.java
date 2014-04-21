package com.atlassian.jira.issue.tabpanels;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.action.IssueActionComparator;
import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueTabPanel;
import com.atlassian.jira.plugin.issuetabpanel.IssueAction;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanel;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.util.profiling.UtilTimerStack;
import com.opensymphony.user.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class AllTabPanel extends AbstractIssueTabPanel
{
    private PluginAccessor pluginAccessor;

    public AllTabPanel(PluginAccessor pluginAccessor)
    {
        this.pluginAccessor = pluginAccessor;
    }

    public List getActions(Issue issue, User remoteUser)
    {
        List allActions = new ArrayList();

        final List<IssueTabPanelModuleDescriptor> tabPanelDescriptors =
                pluginAccessor.getEnabledModuleDescriptorsByClass(IssueTabPanelModuleDescriptor.class);

        for (IssueTabPanelModuleDescriptor tabPanelDescriptor : tabPanelDescriptors)
        {
            IssueTabPanel issueTabPanel = tabPanelDescriptor.getModule();

            final String name = "Getting actions for " + tabPanelDescriptor.getCompleteKey();
            UtilTimerStack.push(name);
            try
            {
                if (!(issueTabPanel instanceof AllTabPanel) && issueTabPanel.showPanel(issue, remoteUser))
                {
                    allActions.addAll(issueTabPanel.getActions(issue, remoteUser));
                }
            }
            finally
            {
                UtilTimerStack.pop(name);
            }
        }

        // See if the returned actions should be actually shown on the all tab
        for (Iterator iterator = allActions.iterator(); iterator.hasNext();)
        {
            IssueAction issueAction = (IssueAction) iterator.next();
            if (!issueAction.isDisplayActionAllTab())
                iterator.remove();
        }

        // This is a bit of a hack to indicate that there are nothing to display in the all tab panel
        if (allActions.isEmpty())
        {
            GenericMessageAction action = new GenericMessageAction(descriptor.getI18nBean().getText("viewissue.noactions"));
            return EasyList.build(action);
        }

        Collections.sort(allActions, IssueActionComparator.COMPARATOR);
        return allActions;
    }

    public boolean showPanel(Issue issue, User remoteUser)
    {
        return true;
    }
}
