package com.atlassian.jira.web.action.admin;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.user.User;
import org.apache.commons.lang.StringUtils;
import webwork.action.ActionContext;

import static com.atlassian.jira.avatar.Avatar.Type.PROJECT;
import static com.atlassian.jira.avatar.Avatar.Type.USER;

/**
 * Action for deleting Project Avatars.
 *
 * @since v4.0
 */
public class DeleteAvatar extends JiraWebActionSupport
{
    public static final String SECURITY_BREACH = "securitybeach";

    private boolean confirm = false;

    private Long avatarId;
    private String ownerId;
    private String avatarType;
    private ProjectManager projectManager;
    private AvatarManager avatarManager;
    private final ApplicationProperties applicationProperties;
    private final UserUtil userUtil;

    public DeleteAvatar(ProjectManager projectManager, AvatarManager avatarManager,
            ApplicationProperties applicationProperties, UserUtil userUtil)
    {
        this.projectManager = projectManager;
        this.avatarManager = avatarManager;
        this.applicationProperties = applicationProperties;
        this.userUtil = userUtil;
    }

    protected String doExecute() throws Exception
    {
        if (avatarId == null || StringUtils.isBlank(ownerId))
        {
            addErrorMessage(getText("admin.project.avatar.delete.specify.ids"));
            return ERROR;
        }
        Avatar avatar = avatarManager.getById(avatarId);
        if (avatar == null)
        {
            addErrorMessage(getText("admin.project.avatar.delete.unknown"));
            return ERROR;
        }
        if (avatar.isSystemAvatar())
        {
            addErrorMessage(getText("admin.project.avatar.delete.cannot.system"));
            return ERROR;
        }
        if (!avatarManager.hasPermissionToEdit(getRemoteUser(), avatar.getAvatarType(), avatar.getOwner()))
        {
            return SECURITY_BREACH;
        }

        if(PROJECT.equals(getRealAvatarType()))
        {
            Project project;
            try
            {
                project = projectManager.getProjectObj(Long.parseLong(avatar.getOwner()));
            }
            catch (NumberFormatException e)
            {
                addErrorMessage(getText("admin.project.avatar.delete.unknown"));
                return ERROR;
            }

            if (project.getAvatar().getId().equals(avatarId))
            {
                setDefaultAvatar(project);
            }
        }
        else if(USER.equals(getRealAvatarType()))
        {
            final User user = userUtil.getUser(avatar.getOwner());
            final PropertySet userProperties = user.getPropertySet();
            if(avatarId.equals(userProperties.getLong(AvatarManager.USER_AVATAR_ID_KEY)))
            {
                userProperties.setLong(AvatarManager.USER_AVATAR_ID_KEY, Long.parseLong(applicationProperties.getString(APKeys.JIRA_DEFAULT_USER_AVATAR_ID)));
            }
        }
        avatarManager.delete(avatarId);

        return getRedirect("/secure/project/AvatarPicker!default.jspa?ownerId=" + getOwnerId() + "&avatarType=" + getAvatarType());
    }

    private void setDefaultAvatar(final Project project)
    {
        final Long defaultAvatarId = avatarManager.getDefaultAvatarId(getRealAvatarType());
        projectManager.updateProject(project, project.getName(), project.getDescription(), project.getLeadUserName(), project.getUrl(), project.getAssigneeType(), defaultAvatarId);
    }

    public String getAvatarUrl()
    {
        String servlet = "projectavatar?pid=";
        if(USER.equals(getRealAvatarType()))
        {
            servlet = "useravatar?ownerId=";
        }
        return ActionContext.getRequest().getContextPath() + "/secure/" + servlet + getOwnerId() + "&avatarId=" + avatarId;
    }

    public Long getAvatarId()
    {
        return avatarId;
    }

    public void setAvatarId(final Long avatarId)
    {
        this.avatarId = avatarId;
    }

    public boolean isConfirm()
    {
        return confirm;
    }

    public void setConfirm(final boolean confirm)
    {
        this.confirm = confirm;
    }

    public Avatar.Type getRealAvatarType()
    {
        return Avatar.Type.getByName(getAvatarType());
    }

    public String getAvatarType()
    {
        return avatarType;
    }

    public void setAvatarType(final String avatarType)
    {
        this.avatarType = avatarType;
    }

    public String getOwnerId()
    {
        return ownerId;
    }

    public void setOwnerId(final String ownerId)
    {
        this.ownerId = ownerId;
    }
}
