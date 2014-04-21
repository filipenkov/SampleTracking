package com.atlassian.jira.web.action.user;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.portal.PortalPageService;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.portal.PortletConfigurationManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.UserHistoryManager;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.jira.util.GroupPermissionChecker;
import com.atlassian.jira.web.action.portal.AbstractSaveConfiguration;
import com.atlassian.jira.web.action.util.portal.PortalPageRetriever;
import com.opensymphony.user.User;

/**
 * Saves a portlet configuration to a property set. This action is used both to create a new portlet and save the
 * configuration of an existing portlet.
 *
 * @since ??
 */
public class SaveConfiguration extends AbstractSaveConfiguration implements UserProfileAction
{
    private final EmailFormatter emailFormatter;
    private final GroupPermissionChecker groupPermissionChecker;
    private final PortalPageRetriever portalPageRetriever;
    private boolean displayUserSummary = true;

    private String destination = null;

    public SaveConfiguration(ProjectManager projectManager, PermissionManager permissionManager, SearchRequestService searchRequestService,
            EmailFormatter emailFormatter, GroupPermissionChecker groupPermissionChecker, PortalPageService portalPageService, PortletConfigurationManager portletConfigurationManager,
            final UserHistoryManager userHistoryManager, final JiraAuthenticationContext authenticationContext)
    {
        super(projectManager, permissionManager, searchRequestService, portalPageService, portletConfigurationManager);
        this.emailFormatter = emailFormatter;
        this.groupPermissionChecker = groupPermissionChecker;
        this.portalPageRetriever = new PortalPageRetriever(portalPageService, userHistoryManager, authenticationContext);
    }

    protected PortalPage loadPortalPage()
    {
        if (getPortalPageId() != null)
        {
            JiraServiceContext serviceContext = getJiraServiceContext();
            return  getPortalPageService().getPortalPage(serviceContext, getPortalPageId());
        }
        return null;
    }

    protected String getHomeRedirect()
    {
        return getRedirect("Dashboard.jspa?selectPageId=" + getPortalPageId());
    }

    public String getDestination()
    {
        return destination;
    }

    public void setDestination(String destination)
    {
        this.destination = destination;
    }

    public String getCancelUrl()
    {
        return "Dashboard.jspa?selectPageId=" + getPortalPageId();
    }

    public boolean isHasViewGroupPermission(String group, User user)
    {
        return groupPermissionChecker.hasViewGroupPermission(group, user);
    }

    public String getDisplayEmail(String email)
    {
        return emailFormatter.formatEmailAsLink(email, getRemoteUser());
    }

    public Long getPortalPageId()
    {
        return portalPageRetriever.getPageId();
    }

    public void setPortalPageId(final Long portalPageId)
    {
        portalPageRetriever.setRequestedPageId(portalPageId);
    }

    public boolean isDisplayUserSummary()
    {
        return displayUserSummary;
    }

    public void setDisplayUserSummary(final boolean displayUserSummary)
    {
        this.displayUserSummary = displayUserSummary;
    }
}
