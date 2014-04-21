package com.atlassian.jira.portal.portlets;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.vote.VoteManager;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.opensymphony.user.User;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

public class MyVotesPortlet extends AbstractVotesAndWatchesPortlet
{
    private static final Logger log = Logger.getLogger(MyVotesPortlet.class);
    protected final VoteManager voteManager;

    public MyVotesPortlet(JiraAuthenticationContext authenticationContext, PermissionManager permissionManager, ConstantsManager constantsManager,
                          ApplicationProperties applicationProperties, IssueManager issueManager, VoteManager voteManager)
    {
        super(authenticationContext, permissionManager, constantsManager, applicationProperties, issueManager);
        this.voteManager = voteManager;
    }

    protected Map getVelocityParams(final PortletConfiguration portletConfiguration)
    {
        Map params = super.getVelocityParams(portletConfiguration);
        params.put("voteManager", voteManager);
        return params;
    }

    public List getIssues(User remoteUser, PortletConfiguration portletConfiguration)
    {
        if (remoteUser == null) return null;
        try
        {
            List votedIssues = issueManager.getVotedIssues(remoteUser);
            //if show Resolved issues is NOT enabled, remove resolved issues JRA-7515
            if (!isShowResolved(portletConfiguration))
                removeResolvedIssues(votedIssues);

            return votedIssues;
        }
        catch (Exception e)
        {
            log.error("Could not get issues", e);
            return null;
        }
    }

    public boolean canRemoveAssociation(Issue issue)
    {
        return issue != null && issue.getResolution() == null;
    }

    protected String getLinkToViewAssociations()
    {
        return "/secure/ViewVoters!default.jspa";
    }

    public Long getTotalAssociations(Issue issue)
    {
        return issue.getVotes();
    }

    public String getToolTipText(String issueKey, long size)
    {
        return authenticationContext.getI18nHelper().getText("portlet.votes.icon.showtotal.span", issueKey, Long.toString(size));
    }

    public String getRemoveIssueText()
    {
        return authenticationContext.getI18nHelper().getText("portlet.votes.unvote");
    }

    public String getRemoveIssueLink()
    {
        return "/secure/UserVotes.jspa?delVote=";
    }

    public String getNoIssuesText()
    {
        return authenticationContext.getI18nHelper().getText("portlet.votes.novotes");
    }

    public String getLinkToSearch()
    {
        return "secure/UserVotes!default.jspa";
    }

    protected String getSearchName()
    {
        return authenticationContext.getI18nHelper().getText("portlet.votes.display.name");
    }
}
