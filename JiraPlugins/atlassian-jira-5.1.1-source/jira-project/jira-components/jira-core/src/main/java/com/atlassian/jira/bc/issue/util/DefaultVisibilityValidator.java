package com.atlassian.jira.bc.issue.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ErrorCollection.Reason;
import org.apache.commons.lang.StringUtils;

import static com.atlassian.jira.user.util.Users.isAnonymous;

/**
 * Used to validate things like {@link com.atlassian.jira.issue.comments.Comment}'s and
 * {@link com.atlassian.jira.issue.worklog.Worklog}'s group or project role visiblity restrictions.
 */
public class DefaultVisibilityValidator implements VisibilityValidator
{
    private final ApplicationProperties applicationProperties;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final ProjectRoleManager projectRoleManager;
    private final GroupManager groupManager;


    public DefaultVisibilityValidator(ApplicationProperties applicationProperties, JiraAuthenticationContext jiraAuthenticationContext, ProjectRoleManager projectRoleManager,
        GroupManager groupManager)
    {
        this.applicationProperties = applicationProperties;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.projectRoleManager = projectRoleManager;
        this.groupManager = groupManager;
    }

    public boolean isValidVisibilityData(JiraServiceContext jiraServiceContext, String i18nPrefix, Issue issue, String groupLevel, String roleLevelId)
    {
        boolean valid = true;
        ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();
        User currentUser = jiraServiceContext.getLoggedInUser();

        if (issue == null)
        {
            errorCollection.addErrorMessage(getText(i18nPrefix + ".service.error.issue.null"), Reason.VALIDATION_FAILED);
            valid = false;
        }

        final boolean roleLevelIdNotBlank = StringUtils.isNotBlank(roleLevelId);
        final boolean groupLevelNotBlank = StringUtils.isNotBlank(groupLevel);

        if ((groupLevelNotBlank || roleLevelIdNotBlank) && isAnonymous(currentUser))
        {
            errorCollection.addErrorMessage(getText(i18nPrefix + ".service.error.visibility.anonymous"), Reason.VALIDATION_FAILED);
            return false;
        }

        if (groupLevelNotBlank && roleLevelIdNotBlank)
        {
            valid = false;
            errorCollection.addError("commentLevel", getText(i18nPrefix + ".service.error.visibility"), Reason.VALIDATION_FAILED);
        }
        else if (groupLevelNotBlank)
        {
            if (!isGroupVisiblityEnabled())
            {
                valid = false;
                errorCollection.addError("commentLevel", getText(i18nPrefix + ".service.error.visibility.group"), Reason.VALIDATION_FAILED);
            }
            else
            {
                if (!groupManager.groupExists(groupLevel))
                {
                    valid = false;
                    errorCollection.addError("commentLevel", getText(i18nPrefix + ".service.error.groupdoesnotexist", groupLevel), Reason.VALIDATION_FAILED);
                }
                else if (!groupManager.isUserInGroup(currentUser.getName(), groupLevel))
                {
                    valid = false;
                    errorCollection.addError("commentLevel", getText(i18nPrefix + ".service.error.usernotingroup", groupLevel), Reason.VALIDATION_FAILED);
                }
            }
        }
        else if (roleLevelIdNotBlank)
        {
            if (!isProjectRoleVisiblityEnabled())
            {
                valid = false;
                errorCollection.addError("commentLevel", getText(i18nPrefix + ".service.error.visibility.role"), Reason.VALIDATION_FAILED);
            }
            else
            {
                valid = (valid && isRoleLevelValid(jiraServiceContext, i18nPrefix, roleLevelId, issue));
            }
        }

        return valid;
    }

    public boolean isGroupVisiblityEnabled()
    {
        return applicationProperties.getOption(APKeys.COMMENT_LEVEL_VISIBILITY_GROUPS);
    }

    public boolean isProjectRoleVisiblityEnabled()
    {
        // This will always be true since we want our users to move towards Project Roles
        return true;
    }

    boolean isRoleLevelValid(JiraServiceContext jiraServiceContext, String i18nPrefix, String roleLevelId, Issue issue)
    {
        ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();
        User currentUser = jiraServiceContext.getLoggedInUser();
        boolean valid = true;

        // If we have a roleLevel specified, ensure that the roleLevel exists and the user is in it.
        try
        {
            ProjectRole projectRole = projectRoleManager.getProjectRole(new Long(roleLevelId));
            if (projectRole != null)
            {
                if (!projectRoleManager.isUserInProjectRole(currentUser, projectRole, issue.getProjectObject()))
                {
                    errorCollection.addError("commentLevel", getText(i18nPrefix + ".service.error.usernotinrole", projectRole.getName()));
                    valid = false;
                }
            }
            else
            {
                errorCollection.addError("commentLevel", getText(i18nPrefix + ".service.error.roledoesnotexist", roleLevelId));
                valid = false;
            }
        }
        catch (NumberFormatException e)
        {
            errorCollection.addError("commentLevel", getText(i18nPrefix + ".service.error.roleidnotnumber"));
            valid = false;
        }
        return valid;
    }

    private String getText(String key)
    {
        return jiraAuthenticationContext.getI18nHelper().getText(key);
    }

    private String getText(String key, String param)
    {
        return jiraAuthenticationContext.getI18nHelper().getText(key, param);
    }
}
