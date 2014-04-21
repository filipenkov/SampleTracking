package com.atlassian.jira.web.action.admin.roles;

import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.issue.comparator.UserBestNameComparator;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.roles.DefaultRoleActors;
import com.atlassian.jira.security.roles.ProjectRoleActor;
import com.atlassian.jira.security.roles.RoleActorFactory;
import com.atlassian.jira.security.roles.actor.UserRoleActorFactory;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.component.multiuserpicker.UserPickerLayoutBean;
import com.atlassian.jira.web.component.multiuserpicker.UserPickerWebComponent;
import com.atlassian.velocity.VelocityManager;
import webwork.action.ActionContext;

import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Action for creating and editing UserRoleActors
 *
 * @see com.atlassian.jira.security.roles.RoleActor
 */
public class UserRoleActorAction extends AbstractRoleActorAction
{

    private static final String REMOVE_USERS_PREFIX = "removeusers_";

    private String userNames;
    private final ProjectRoleService projectRoleService;
    private final VelocityManager velocityManager;
    private final UserPickerSearchService searchService;

    public UserRoleActorAction(ProjectRoleService projectRoleService, ProjectManager projectManager,
                               ProjectFactory projectFactory, RoleActorFactory roleActorFactory,
                               VelocityManager velocityManager, UserPickerSearchService searchService)
    {
        super(projectRoleService, projectManager, projectFactory, roleActorFactory);
        this.projectRoleService = projectRoleService;
        this.velocityManager = velocityManager;
        this.searchService = searchService;
    }

    public String doExecute()
    {
        if (!projectRoleService.hasProjectRolePermission(getRemoteUser(), getProject()))
        {
            return "securitybreach";
        }
        return SUCCESS;
    }

    public String getUserPickerHtml()
    {
        String removeUsersAction = "UserRoleActorAction!removeUsers.jspa?projectRoleId=" + getProjectRoleId() + ((getProject() != null) ? "&projectId=" + getProjectId() : "");
        String addUserAction = "UserRoleActorAction!addUsers.jspa?projectRoleId=" + getProjectRoleId() + ((getProject() != null) ? "&projectId=" + getProjectId() : "");
        UserPickerLayoutBean userPickerLayoutBean = new UserPickerLayoutBean("admin.user.role.actor.action", REMOVE_USERS_PREFIX, removeUsersAction, addUserAction);
        UserPickerWebComponent userPickerWebComponent = new UserPickerWebComponent(velocityManager, getApplicationProperties(), searchService);
        return userPickerWebComponent.getHtml(userPickerLayoutBean, getProjectRoleActorUsers(), true, getProjectRoleId());
    }

    /**
     * Provides the currently selected users.
     *
     * @return the users.
     */
    private Collection getProjectRoleActorUsers()
    {
        DefaultRoleActors defaultRoleActors;
        if (getProject() == null)
        {
            defaultRoleActors = projectRoleService.getDefaultRoleActors(getRemoteUser(), getProjectRole(), this);
        }
        else
        {
            defaultRoleActors = projectRoleService.getProjectRoleActors(getRemoteUser(), getProjectRole(), getProject(), this);
        }
        SortedSet usersByType = new TreeSet(new UserBestNameComparator(getLocale()));
        if (defaultRoleActors != null)
        {
            for (Iterator iterator = defaultRoleActors.getRoleActorsByType(UserRoleActorFactory.TYPE).iterator(); iterator.hasNext();)
            {
                ProjectRoleActor projectRoleActor = (ProjectRoleActor) iterator.next();
                usersByType.addAll(projectRoleActor.getUsers());
            }
        }
        return usersByType;
    }

    @RequiresXsrfCheck
    public String doRemoveUsers()
    {
        Collection userNamesToRemove = UserPickerWebComponent.getUserNamesToRemove(ActionContext.getParameters(), REMOVE_USERS_PREFIX);

        if (getProject() == null)
        {
            projectRoleService.removeDefaultActorsFromProjectRole(getRemoteUser(), userNamesToRemove, getProjectRole(), UserRoleActorFactory.TYPE, this);
        }
        else
        {
            projectRoleService.removeActorsFromProjectRole(getRemoteUser(), userNamesToRemove, getProjectRole(), getProject(), UserRoleActorFactory.TYPE, this);
        }

        if (hasAnyErrors())
        {
            return ERROR;
        }

        return SUCCESS;
    }

    @RequiresXsrfCheck
    public String doAddUsers()
    {
        Collection userNamesToAdd = UserPickerWebComponent.getUserNamesToAdd(getUserNames());

        if (getProject() == null)
        {
            projectRoleService.addDefaultActorsToProjectRole(getRemoteUser(), userNamesToAdd, getProjectRole(), UserRoleActorFactory.TYPE, this);
        }
        else
        {
            projectRoleService.addActorsToProjectRole(getRemoteUser(), userNamesToAdd, getProjectRole(), getProject(), UserRoleActorFactory.TYPE, this);
        }

        // do not continue if we have errors
        if (hasAnyErrors())
        {
            return ERROR;
        }

        return SUCCESS;
    }

    public String getUserNames()
    {
        return userNames;
    }

    public void setUserNames(String userNames)
    {
        this.userNames = userNames;
    }

}
