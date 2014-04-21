/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.project;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.renderer.ProjectDescriptionRenderer;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.OnDemand;
import webwork.action.ActionContext;

public class EditProject extends ViewProject
{
    private final ProjectService projectService;
    private AvatarManager avatarManager;
    private final UserManager userManager;
    private final ProjectDescriptionRenderer projectDescriptionRenderer;

    /*
     * ************************************************ !!! NOTE !!! ***************************************************
     *
     * CHANGING THIS CONSTRUCTOR WILL BREAK ON DEMAND.
     *
     * Please consider if you really have to do that (unless you're trying to improve things).
     *
     * ************************************************ !!! NOTE !!! ***************************************************
     *
     *
     */
    @OnDemand ("ON DEMAND extends this action and thus changing this constructor will cause compilation errors")
    public EditProject(ProjectService projectService, AvatarManager avatarManager, UserManager userManager)
    {
        this.projectService = projectService;
        this.avatarManager = avatarManager;
        this.userManager = userManager;
        projectDescriptionRenderer = ComponentAccessor.getComponentOfType(ProjectDescriptionRenderer.class);
    }

    public String doDefault() throws Exception
    {
        // check if the project exists:
        if (getProject() == null)
        {
            return handleProjectDoesNotExist();
        }
        if (!(hasProjectAdminPermission() || hasAdminPermission()))
        {
            return "securitybreach";
        }
        setName(getProject().getString("name"));
        setLead(getProject().getString("lead"));
        setKey(getProject().getString("key"));
        setUrl(getProject().getString("url"));
        setDescription(getProject().getString("description"));
        setAssigneeType(getProject().getLong("assigneetype"));

        return INPUT;
    }

    private String handleProjectDoesNotExist() throws Exception
    {
        if (hasAdminPermission())
        {
            // User is admin - admit that the Project Doesn't exist because they have permission to see any project.
            // We will show the Edit Project Page, but without any values in the fields (and with an error message).
            // This is consistent with what happens if we start to edit a project, but it gets deleted before we save it.
            setName("???");
            addErrorMessage(getText("admin.errors.project.no.project.with.id"));

            return super.doDefault();
        }
        else
        {
            // User is not admin - show security breach because this isn't a Project they have permission to edit.
            return "securitybreach";
        }
    }

    protected void doValidation()
    {
        // First check that the Project still exists
        if (getProject() == null)
        {
            addErrorMessage(getText("admin.errors.project.no.project.with.id"));
            // Don't try to do any more validation.
            return;
        }

        final Project projectObject = getProjectObject();
        final ProjectService.UpdateProjectValidationResult result =
                projectService.validateUpdateProject(getLoggedInUser(), getName(), projectObject.getKey(),
                        getDescription(), projectObject.getLeadUserName(), getUrl(), projectObject.getAssigneeType());
        if (!result.isValid())
        {
            //map keyed errors to JSP field names
            mapErrorCollection(result.getErrorCollection());
        }


        // This validation seems to be redundant now - but leave it in case we add something special to ViewProject.doValidation()
        super.doValidation();
    }

    public String getAvatarUrl()
    {
        return ActionContext.getRequest().getContextPath() + "/secure/projectavatar?pid=" + getPid() + "&size=large&avatarId="
                + getProjectObject().getAvatar().getId();
    }
    
    public ProjectDescriptionRenderer getProjectDescriptionRenderer()
    {
        return projectDescriptionRenderer;
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (!(hasProjectAdminPermission() || hasAdminPermission()))
        {
            return "securitybreach";
        }


        final Project projectObject = getProjectObject();
        final ProjectService.UpdateProjectValidationResult result =
                projectService.validateUpdateProject(getLoggedInUser(), getName(), projectObject.getKey(),
                        getDescription(), projectObject.getLeadUserName(), getUrl(), projectObject.getAssigneeType());

        projectService.updateProject(result);

        if (isInlineDialogMode())
        {
            return returnComplete();
        }

        return getRedirect("/plugins/servlet/project-config/" + getProject().getString("key") + "/summary");
    }

    public boolean hasInvalidLead()
    {
        final Project projectObject = getProjectObject();

        if (projectObject == null)
        {
            return false;
        }
        else
        {
            final String leadUserName = projectObject.getLeadUserName();
            return userManager.getUserObject(leadUserName) == null;
        }
    }

    public Long getDefaultAvatar()
    {
        return avatarManager.getDefaultAvatarId(Avatar.Type.PROJECT);
    }
}
