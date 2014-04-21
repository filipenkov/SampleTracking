/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.project;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarImpl;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.avatar.TemporaryAvatar;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.config.CoreFeatures;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.AssigneeTypes;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.OnDemand;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericEntityException;
import webwork.action.ActionContext;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;

import static com.atlassian.jira.avatar.Avatar.Type.PROJECT;
import static com.atlassian.jira.web.SessionKeys.TEMP_AVATAR;

@WebSudoRequired
public class AddProject extends AbstractProjectAction
{
    private final ProjectService projectService;
    private final AvatarService avatarService;
    private final AvatarManager avatarManager;
    private final PermissionSchemeManager permissionSchemeManager;
    private final UserManager userManager;
    private final UserPickerSearchService userPickerSearchService;

    private User leadUserObj;
    private String leadError;

    /*
    * ************************************************ !!! NOTE !!! ***************************************************
    *
    * CHANGING THIS CONSTRUCTOR WILL BREAK ON DEMAND.
    *
    * Please consider if you really have to do that (unless you're trying to improve Studio integration).
    *
    * ************************************************ !!! NOTE !!! ***************************************************
    *
    *
    */
    @OnDemand ("ON DEMAND extends this action and thus changing this constructor will cause compilation errors")
    public AddProject(ProjectService projectService, AvatarService avatarService, AvatarManager avatarManager,
            PermissionSchemeManager permissionSchemeManager, UserManager userManager, UserPickerSearchService userPickerSearchService)
    {
        this.projectService = projectService;
        this.avatarService = avatarService;
        this.avatarManager = avatarManager;
        this.permissionSchemeManager = permissionSchemeManager;
        this.userManager = userManager;
        this.userPickerSearchService = userPickerSearchService;
    }

    @Override
    public String doDefault() throws Exception
    {
        ActionContext.getSession().remove(TEMP_AVATAR);
        setLead(getDefaultLead());
        setPermissionScheme(getDefaultPermissionSchemeId());
        setAssigneeType(getDefaultAssigneeType());

        return super.doDefault();
    }

    protected void doValidation()
    {
        final ProjectService.CreateProjectValidationResult result =
                projectService.validateCreateProject(getLoggedInUser(), getName(), getKey(), getDescription(), getLead(),
                        getUrl(), getAssigneeType());
        final ProjectService.UpdateProjectSchemesValidationResult schemesResult =
                projectService.validateUpdateProjectSchemes(getLoggedInUser(), getPermissionScheme(),
                        getNotificationScheme(), getIssueSecurityScheme());
        ErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addErrorCollection(result.getErrorCollection());
        errorCollection.addErrorCollection(schemesResult.getErrorCollection());

        if (errorCollection.hasAnyErrors())
        {
            //map keyed errors to JSP field names
            mapErrorCollection(errorCollection);
        }

        if (getLeadUserObj() == null)
        {
            setLeadError(getLead());
        }

        super.doValidation();
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        final ProjectService.CreateProjectValidationResult result =
                projectService.validateCreateProject(getLoggedInUser(), getName(), getKey(), getDescription(), getLead(),
                        getUrl(), getAssigneeType());

        final Project project = projectService.createProject(result);

        if (getAvatarId() == null)
        {
            Avatar avatar = createAvatarFromTemp(project);
            if (avatar != null)
            {
                setAvatarId(avatar.getId());
            }
        }

        final ProjectService.UpdateProjectSchemesValidationResult schemesResult =
                projectService.validateUpdateProjectSchemes(getLoggedInUser(), getPermissionScheme(),
                        getNotificationScheme(), getIssueSecurityScheme());
        projectService.updateProjectSchemes(schemesResult, project);

        if (getAvatarId() != null)
        {
            final ProjectService.UpdateProjectValidationResult updateProjectValidationResult =
                    projectService.validateUpdateProject(getLoggedInUser(), getName(), getKey(), getDescription(), getLead(),
                            getUrl(), getAssigneeType(), getAvatarId());

            projectService.updateProject(updateProjectValidationResult);
        }
        return returnCompleteWithInlineRedirect(getNextActionUrl(project));
    }

    /**
     * Creates an avatar for the given project using the temporary avatar as its whole input.
     * No scaling is performed. The region used is the square starting at the origin and extending by the size of the
     * large avatar. This is useful for add project because a TemporaryAvatar is scaled and used until the Project
     * object has been created.
     *
     * @param project
     * @return null on failure
     * @throws IOException
     */
    private Avatar createAvatarFromTemp(final Project project) throws IOException
    {
        // new avatar under creation at the same time, so get the temporary avatar and create the proper one
        TemporaryAvatar temporaryAvatar = (TemporaryAvatar) ActionContext.getSession().remove(TEMP_AVATAR);
        if (temporaryAvatar == null)
        {
            return null;
        }
        else
        {
            Avatar newAvatar = AvatarImpl.createCustomAvatar(temporaryAvatar.getOriginalFilename(), temporaryAvatar.getContentType(), PROJECT, project.getId().toString());
            return avatarManager.create(newAvatar, new FileInputStream(temporaryAvatar.getFile()), AvatarManager.ImageSize.LARGE.getOriginSelection());
        }
    }

    private Long getDefaultPermissionSchemeId()
    {
        try
        {
            return permissionSchemeManager.getDefaultScheme().getLong("id");
        }
        catch (GenericEntityException e)
        {
            return 0L;
        }
    }

    private Long getDefaultAssigneeType()
    {
        return AssigneeTypes.PROJECT_LEAD;
    }

    public boolean isShouldShowLead()
    {
        return userManager.getTotalUserCount() > 1 || getLoggedInUser() == null;
    }

    public User getLeadUserObj()
    {
        if (getLead() != null && leadUserObj == null)
        {
            leadUserObj = userManager.getUserObject(getLead());
        }
        return leadUserObj;
    }

    public URI getLeadUserAvatarUrl()
    {
        return avatarService.getAvatarURL(getLoggedInUser(), getLead(), Avatar.Size.SMALL);
    }

    public boolean userPickerDisabled()
    {
        return !userPickerSearchService.canPerformAjaxSearch(this.getJiraServiceContext());
    }

    private String getDefaultLead()
    {
        User user = getLoggedInUser();
        if (user != null)
        {
            return user.getName();
        }
        else
        {
            return null;
        }
    }

    protected String getNextActionUrl(Project project)
    {
        return "/plugins/servlet/project-config/" + project.getKey() + "/summary";
    }

    public String getLeadError()
    {
        return leadError;
    }

    public void setLeadError(String leadError)
    {
        this.leadError = leadError;
    }

    public boolean isShowImportHint()
    {
        // don't show the import hint in the JoD world!
        return !getComponentInstanceOfType(FeatureManager.class).isEnabled(CoreFeatures.ON_DEMAND);
    }

}
