package com.atlassian.jira.plugin.issuetabpanel;

import com.atlassian.jira.issue.Issue;
import com.opensymphony.user.User;

import java.util.List;

/**
 * A comment, work log, changelog etc on an issue.
 * This plugin type is <a href="http://confluence.atlassian.com/display/JIRA/Issue+Tab+Panel+Plugin+Module">documented online</a>.
 */
public interface IssueTabPanel
{
    void init(IssueTabPanelModuleDescriptor descriptor);

    /**
     * Return a list of issue actions in the order that you want them to be displayed.
     * <p>
     * Note that for the 'all' tab, they will be displayed in order according to the value returned
     * by {@link IssueAction#getTimePerformed()}
     * </p>
     * <p>
     * the user that is viewing the tab can affect which objects are shown, as well as which operations are available on each.
     *
     * @param issue The Issue that the objects belong to.
     * @param remoteUser The user viewing this tab.
     * @return A List of {@link IssueAction} objects.
     */
    List getActions(Issue issue, User remoteUser);

    /**
     * Whether or not to show this tab panel to the given User for the given Issue.
     *
     * @param issue The Issue.
     * @param remoteUser The viewing user.
     * @return <code>true</code> if we should show this tab panel to the given User for the given Issue.
     */
    boolean showPanel(Issue issue, User remoteUser);
}
