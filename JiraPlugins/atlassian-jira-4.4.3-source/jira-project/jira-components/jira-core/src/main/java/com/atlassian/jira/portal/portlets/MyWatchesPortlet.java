package com.atlassian.jira.portal.portlets;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.watchers.WatcherManager;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.opensymphony.user.User;
import org.apache.log4j.Logger;

import java.util.List;

public class MyWatchesPortlet extends AbstractVotesAndWatchesPortlet
{
    private static final Logger log = Logger.getLogger(com.atlassian.jira.portal.portlets.MyWatchesPortlet.class);
    private WatcherManager watcherManager;

    public MyWatchesPortlet(JiraAuthenticationContext authenticationContext, PermissionManager permissionManager, ConstantsManager constantsManager,
                            ApplicationProperties applicationProperties, IssueManager issueManager, WatcherManager watcherManager)
    {
        super(authenticationContext, permissionManager, constantsManager, applicationProperties, issueManager);
        this.watcherManager = watcherManager;
    }


    public List getIssues(User remoteUser, PortletConfiguration portletConfiguration)
    {
        if (remoteUser == null) return null;

        try
        {
            return issueManager.getWatchedIssues(remoteUser);
        }
        catch (Exception e)
        {
            log.error("Could not get issues", e);
            return null;
        }
    }

    public boolean canRemoveAssociation(Issue issue)
    {
        return true;
    }

    protected String getLinkToViewAssociations()
    {
        return "/secure/ManageWatchers!default.jspa";
    }

    public Long getTotalAssociations(Issue issue)
    {
        return new Long(watcherManager.getCurrentWatcherUsernames(issue).size());
    }

    public String getToolTipText(String issueKey, long size)
    {
        return authenticationContext.getI18nHelper().getText("portlet.watches.icon.showtotal.span", issueKey, Long.toString(size));
    }

    protected String getRemoveIssueText()
    {
        return authenticationContext.getI18nHelper().getText("portlet.watches.unwatch");
    }

    protected String getRemoveIssueLink()
    {
        return "/secure/UserWatches.jspa?delWatch=";
    }

    protected String getNoIssuesText()
    {
        return authenticationContext.getI18nHelper().getText("portlet.watches.nowatches");
    }

    protected String getLinkToSearch()
    {
        return "secure/UserWatches!default.jspa";
    }

    protected String getSearchName()
    {
        return authenticationContext.getI18nHelper().getText("portlet.watches.display.name");
    }
}
