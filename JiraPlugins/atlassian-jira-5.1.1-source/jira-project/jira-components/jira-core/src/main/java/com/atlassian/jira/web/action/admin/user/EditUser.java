/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.user;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.ImmutableUser;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.user.UserService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.CoreFeatures;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.event.user.UserProfileUpdatedEvent;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.web.action.user.GenericEditProfile;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashSet;

@SuppressWarnings ({ "UnusedDeclaration" })
@WebSudoRequired
public class EditUser extends GenericEditProfile
{
    String editName;
    User oldUser;
    private final UserService userService;
    private final UserManager userManager;
    private final FeatureManager featureManager;
    private final EventPublisher eventPublisher;
    private UserService.UpdateUserValidationResult updateUserValidationResult;

    public EditUser(UserService userService, UserManager userManager, UserPropertyManager userPropertyManager, FeatureManager featureManager, EventPublisher eventPublisher)
    {
        super(userPropertyManager);
        this.userService = userService;
        this.featureManager = featureManager;
        this.userManager = userManager;
        this.eventPublisher = eventPublisher;
    }

    public void doValidation()
    {
        super.doValidation();
        final User newUser = buildNewUser();

        updateUserValidationResult = userService.validateUpdateUser(newUser);
        addErrorCollection(updateUserValidationResult.getErrorCollection());
    }

    public boolean showProjectsUserLeadsError()
    {
        if (!isActive())
        {
            final Collection<Project> projects = ComponentAccessor.getProjectManager().getProjectsLeadBy(getEditedUser());
            if (projects.size() > 0)
            {
                return true;
            }
        }
        return false;
    }

    public String projectsUserLeadsErrorMessage()
    {
        final Collection<Project> projects = ComponentAccessor.getProjectManager().getProjectsLeadBy(getEditedUser());
        String projectList = getDisplayableProjectList(projects, "/people");
        return getText("admin.errors.users.cannot.deactivate.due.to.project.lead", projectList);
    }

    public boolean showComponentsUserLeadsError()
    {
        if (!isActive())
        {
            final Collection<ProjectComponent> components = ComponentAccessor.getProjectComponentManager().findComponentsByLead(getEditName());
            if (components.size() > 0)
            {
                return true;
            }
        }
        return false;
    }

    public String componentsUserLeadsErrorMessage()
    {
        final Collection<ProjectComponent> components = ComponentAccessor.getProjectComponentManager().findComponentsByLead(getEditName());
        String projectList = getDisplayableProjectList(getProjectsFor(components), "/components");
        return getText("admin.errors.users.cannot.deactivate.due.to.component.lead", projectList);
    }

    private User buildNewUser()
    {
        ImmutableUser.Builder builder = ImmutableUser.newUser(getEditedUser());
        builder.displayName(getFullName());
        builder.emailAddress(getEmail());
        if (showActiveCheckbox())
        {
            builder.active(isActive());
        }
        return builder.toUser();
    }

    private Collection<Project> getProjectsFor(Collection<ProjectComponent> components)
    {
        ProjectManager projectManager = ComponentAccessor.getProjectManager();
        HashSet<Project> projects = new HashSet<Project>(components.size());
        for (ProjectComponent component : components)
        {
            projects.add(projectManager.getProjectObj(component.getProjectId()));
        }
        return projects;
    }

    private String getDisplayableProjectList(Collection<Project> projects, String projectConfigSection)
    {
        int count = 0;
        StringBuilder sb = new StringBuilder();
        for (Project project : projects)
        {
            if (count >= 5)
            {
                sb.append(", ...");
                break;
            }
            if (count > 0)
                sb.append(", ");

            sb.append("<a href=\"");
            sb.append(insertContextPath("/plugins/servlet/project-config/"));
            sb.append(project.getKey());
            sb.append(projectConfigSection);
            sb.append("\">");
            sb.append(project.getKey());
            sb.append("</a>");

            count++;
        }
        return sb.toString();
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        userService.updateUser(updateUserValidationResult);
        String result = getResult();

        if (SUCCESS.equals(result))
        {
            eventPublisher.publish(new UserProfileUpdatedEvent(updateUserValidationResult.getUser(), getLoggedInUser()));
            return getRedirect("ViewUser.jspa?name=" + URLEncoder.encode(editName,"UTF8"));
        }

        return result;
    }

    public String getEditName()
    {
        return editName;
    }

    public void setEditName(String editName)
    {
        this.editName = editName;
    }

    public boolean showActiveCheckbox()
    {
        // Hide this for JOD
        if (featureManager.isEnabled(CoreFeatures.ON_DEMAND))
        {
            return false;
        }
        else
        {
            // Hide for LDAP until we design JRA-24937
            final Directory directory = userManager.getDirectory(getEditedUser().getDirectoryId());
            return directory.getType() != DirectoryType.CONNECTOR;
        }
    }

    public User getEditedUser()
    {
        if (oldUser == null)
        {
            oldUser = crowdService.getUser(editName);
        }

        return oldUser;
    }
}
