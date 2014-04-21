package com.atlassian.jira.web.action.admin.user;

import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.portal.PortalPageService;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.user.UserService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DeleteUser extends ViewUser
{
    boolean confirm;
    long assignedIssues = -1;
    long reportedIssues = -1;
    private ArrayList<Project> projectsUserLeads;
    private ArrayList<ProjectComponent> componentsUserLeads;

    private final SearchRequestService searchRequestService;
    private final UserUtil userUtil;
    private final PortalPageService portalPageService;
    private UserService.DeleteUserValidationResult validationResult;
    private UserService userService;

    public DeleteUser(CrowdService crowdService, CrowdDirectoryService crowdDirectoryService, final UserPropertyManager userPropertyManager, final UserManager userManager)
    {
        super(crowdService, crowdDirectoryService, userPropertyManager, userManager);
        this.searchRequestService = ComponentManager.getComponentInstanceOfType(SearchRequestService.class);
        this.portalPageService = ComponentManager.getInstance().getPortalPageService();
        this.userService = ComponentManager.getComponentInstanceOfType(UserService.class);
        this.userUtil = ComponentAccessor.getUserUtil();
    }

    public DeleteUser(CrowdService crowdService, CrowdDirectoryService crowdDirectoryService, SearchRequestService searchRequestService, UserService userService, UserUtil userUtil,
            PortalPageService portalPageService, final UserPropertyManager userPropertyManager, UserManager userManager)
    {
        super(crowdService, crowdDirectoryService, userPropertyManager, userManager);
        this.searchRequestService = searchRequestService;
        this.userService = userService;
        this.userUtil = userUtil;
        this.portalPageService = portalPageService;
    }

    protected void doValidation()
    {
        validationResult = userService.validateDeleteUser(getRemoteUser(), getUser().getName());
        if (!validationResult.isValid())
        {
            addErrorCollection(validationResult.getErrorCollection());
        }
        super.doValidation();
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        try
        {
            if (confirm)
            {
                userService.removeUser(getRemoteUser(), validationResult);
            }
        }
        catch (Exception e)
        {
            addErrorMessage(getText("admin.errors.users.exception.trying.to.remove", e));
        }

        if (getHasErrorMessages())
        {
            return ERROR;
        }
        else
        {
            return getRedirect("UserBrowser.jspa");
        }
    }

    public boolean isDeleteable()
    {
        try
        {
            return (getNumberOfReportedIssues() == 0 && getNumberOfAssignedIssues() == 0 && getNumberOfProjectsUserLeads() == 0)
                    && !isNonSysAdminAttemptingToDeleteSysAdmin();
        }
        catch (Exception e)
        {
            log.error(e, e);
            return false;
        }
    }

    public long getNumberOfAssignedIssues() throws Exception
    {
        if (assignedIssues == -1)
        {
            assignedIssues = userUtil.getNumberOfAssignedIssuesIgnoreSecurity(getRemoteUser(), getUser());
        }
        return assignedIssues;
    }

    public long getNumberOfReportedIssues() throws Exception
    {
        if (reportedIssues == -1)
        {
            reportedIssues = userUtil.getNumberOfReportedIssuesIgnoreSecurity(getRemoteUser(), getUser());
        }
        return reportedIssues;
    }

    public boolean isNonSysAdminAttemptingToDeleteSysAdmin()
    {
        return userUtil.isNonSysAdminAttemptingToDeleteSysAdmin(getRemoteUser(), getUser());
    }

    public long getNumberOfFilters()
    {
        Collection requests = searchRequestService.getNonPrivateFilters(getUser());
        return requests.size();
    }

    public long getNumberOfOtherFavouritedFilters()
    {
        Collection requests = searchRequestService.getFiltersFavouritedByOthers(getUser());
        return requests.size();
    }

    public long getNumberOfNonPrivatePortalPages()
    {
        Collection requests = portalPageService.getNonPrivatePortalPages(getUser());
        return requests.size();
    }

    public long getNumberOfOtherFavouritedPortalPages()
    {
        Collection requests = portalPageService.getPortalPagesFavouritedByOthers(getUser());
        return requests.size();
    }

    public Collection<Project> getProjectsUserLeads()
    {
        return getProjectsUserLeads(-1);
    }

    public Collection<Project> getProjectsUserLeads(int limit)
    {
        if (projectsUserLeads == null)
        {
            projectsUserLeads = new ArrayList<Project>(userUtil.getProjectsLeadBy(getUser()));
        }
        if (limit == -1 || limit > projectsUserLeads.size())
        {
            return projectsUserLeads;
        }
        else
        {
            return projectsUserLeads.subList(0, limit);
        }
    }

    public long getNumberOfProjectsUserLeads()
    {
        return getProjectsUserLeads().size();
    }

    public Collection<ProjectComponent> getComponentsUserLeads()
    {
        if (componentsUserLeads == null)
        {
            componentsUserLeads = new ArrayList<ProjectComponent>(userUtil.getComponentsUserLeads(getUser()));
        }
        return componentsUserLeads;
    }

    public Collection<ProjectComponent> getComponentsUserLeads(int limit)
    {
        List<ProjectComponent> componentsUserLeads = new ArrayList<ProjectComponent>(getComponentsUserLeads());
        if (limit > componentsUserLeads.size())
        {
            return componentsUserLeads;
        }
        else
        {
            return componentsUserLeads.subList(0, limit);
        }
    }

    public String getProjectKey(ProjectComponent component)
    {
        final Project project = getProjectManager().getProjectObj(component.getProjectId());
        return project.getKey();

    }

    public long getNumberOfComponentsUserLeads()
    {
        return getComponentsUserLeads().size();
    }

    public boolean isConfirm()
    {
        return confirm;
    }

    public void setConfirm(boolean confirm)
    {
        this.confirm = confirm;
    }
}

