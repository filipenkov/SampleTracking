package com.atlassian.jira.portal.portlets;

import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.portal.PortletImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.bean.FieldVisibilityBean;
import com.atlassian.jira.web.util.IssueTableBean;
import com.atlassian.query.order.SearchSort;
import com.opensymphony.user.User;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class AbstractVotesAndWatchesPortlet extends PortletImpl  //todo - need to add this to AbstractSearchResultsPortlet when votes and watches are indexed
{
    private static final Logger log = Logger.getLogger(AbstractVotesAndWatchesPortlet.class);

    SearchSort sortOrder = new SearchSort(NavigableField.ORDER_DESCENDING, IssueFieldConstants.PRIORITY);
    SearchSort sortOrder2 = new SearchSort(NavigableField.ORDER_ASCENDING, IssueFieldConstants.CREATED);

    protected final ConstantsManager constantsManager;
    protected final IssueManager issueManager;

    public AbstractVotesAndWatchesPortlet(JiraAuthenticationContext authenticationContext, PermissionManager permissionManager, ConstantsManager constantsManager,
                                          ApplicationProperties applicationProperties, IssueManager issueManager)
    {
        super(authenticationContext, permissionManager, applicationProperties);
        this.constantsManager = constantsManager;
        this.issueManager = issueManager;
    }


    protected Map getVelocityParams(final PortletConfiguration portletConfiguration)
    {
        Map params = super.getVelocityParams(portletConfiguration);
        List issues = getIssues(authenticationContext.getUser(), portletConfiguration);
        int issuesCount = issues == null ? 0 : issues.size();
        int maxEntryCount = getMaxEntryCount(portletConfiguration);
        if (issuesCount != 0)
        {
            //Make sure maxEntryCount is positive for subList() JRA-12107
            params.put("issues", maxEntryCount < issuesCount ? issues.subList(0, maxEntryCount < 0 ? 0 : maxEntryCount) : issues);
        }
        params.put("displayedIssueCount", new Integer(Math.min(issuesCount, maxEntryCount)));
        params.put("showTotals", Boolean.valueOf(isShowTotals(portletConfiguration)));
        params.put("linkToSearch", getLinkToSearch());
        params.put("removeIssueText", getRemoveIssueText());
        params.put("removeIssueLink", getRemoveIssueLink());
        params.put("noissues", getNoIssuesText());
        params.put("searchName", getSearchName());
        params.put("viewAssociationLink", getLinkToViewAssociations());
        params.put("fullIssueCount", new Integer(issuesCount));
        params.put("constantsManager", constantsManager);
        params.put("issueBean", new IssueTableBean());
        params.put("fieldVisibility", new FieldVisibilityBean());
        params.put("user", authenticationContext.getUser());
        return params;
    }

    private int getMaxEntryCount(PortletConfiguration portletConfiguration)
    {
        int maxEntryCount = 0;
        try
        {
            final Long numofentries = portletConfiguration.getLongProperty("numofentries");
            if (numofentries != null)
            {
                maxEntryCount = numofentries.intValue();
            }
            else
            {
                maxEntryCount = 10;
            }
        }
        catch (ObjectConfigurationException e)
        {
            log.error(e, e);
        }
        return maxEntryCount;
    }

    protected boolean isShowTotals(PortletConfiguration portletConfiguration)
    {
        return isPropertySetAndTrue(portletConfiguration, "showTotals");
    }

    protected boolean isShowResolved(PortletConfiguration portletConfiguration)
    {
        return isPropertySetAndTrue(portletConfiguration, "showResolved");
    }

    private boolean isPropertySetAndTrue(PortletConfiguration portletConfiguration, String propertyKey)
    {
        try
        {
            return (Boolean.valueOf(portletConfiguration.getProperty(propertyKey)).booleanValue());
        }
        catch (ObjectConfigurationException e)
        {
            log.error(e, e);
        }
        return false;
    }

    public Boolean hasViewPermission(Issue issue)
    {
        return Boolean.valueOf(permissionManager.hasPermission(Permissions.VIEW_VOTERS_AND_WATCHERS, issue.getGenericValue(), authenticationContext.getUser()));
    }

    protected abstract List getIssues(User remoteUser, PortletConfiguration portletConfiguration);

    protected void removeResolvedIssues(Collection issues)
    {
        for (Iterator iterator = issues.iterator(); iterator.hasNext();)
        {
            Issue issue = (Issue) iterator.next();
            if (issue.getResolution() != null) //only remove issues that has a resolution
            {
                iterator.remove();
            }
        }
    }

    public abstract boolean canRemoveAssociation(Issue issue);

    protected abstract String getLinkToViewAssociations();

    public abstract Long getTotalAssociations(Issue issue);

    public abstract String getToolTipText(String issueKey, long size);

    protected abstract String getRemoveIssueText();

    protected abstract String getRemoveIssueLink();

    protected abstract String getNoIssuesText();

    protected abstract String getLinkToSearch();

    protected abstract String getSearchName();
}
