package com.atlassian.jira.issue.comments;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import org.apache.commons.lang.StringUtils;

import static com.atlassian.jira.user.util.Users.isAnonymous;

/**
 * This permission manager is responsible to check and grant browse and edit
 * permissions to users over issue comments.
 */
public class DefaultCommentPermissionManager implements CommentPermissionManager
{
    private final ProjectRoleManager projectRoleManager;
    private final PermissionManager permissionManager;
    private final GroupManager groupManager;

    public DefaultCommentPermissionManager(ProjectRoleManager projectRoleManager, PermissionManager permissionManager, GroupManager groupManager)
    {
        this.projectRoleManager = projectRoleManager;
        this.permissionManager = permissionManager;
        this.groupManager = groupManager;
    }

    /**
     * Determines whether the user can see given comment.
     * <p/>
     * User can see the comment if comment does not have restricted visibility,
     * otherwise only if the user is in either comments group or project role
     * visibility level.
     *
     * @param user    user
     * @param comment comment
     * @return true if user can see the comment, false otherwise
     */
    public boolean hasBrowsePermission(User user, Comment comment)
    {
        // Retrieve both the group level and role level
        String groupLevel = comment.getGroupLevel();
        Long roleLevel = comment.getRoleLevelId();

        boolean roleProvided = (roleLevel != null);
        boolean groupProvided = StringUtils.isNotBlank(groupLevel);

        boolean userInRole = roleProvided && isUserInRole(roleLevel, user, comment.getIssue());
        boolean userInGroup = groupProvided && isUserInGroup(user, groupLevel);
        boolean noLevelsProvided = !groupProvided && !roleProvided;

        return (noLevelsProvided || userInRole || userInGroup);
    }

    /**
     * Determines whether the user can edit given comment.
     * <p/>
     * User can edit the given comment if he can edit all comment
     * of the associated issue or is the author of this comment
     * and has edit own comments permission granted.
     *
     * @param user    user
     * @param comment comment to edit
     * @return true if user can edit the given comment, false otherwise
     */
    public boolean hasEditPermission(User user, Comment comment)
    {
        return hasEditAllPermission(user, comment.getIssue()) ||
               (hasEditOwnPermission(user, comment.getIssue()) &&  isUserCommentAuthor(user, comment));
    }

    public boolean isUserCommentAuthor(User user, Comment comment)
    {
        String commentAuthor = comment.getAuthor();

        // if the author was anonymous, then no-one is the author
        if (commentAuthor == null)
        {
            return false;
        }

        // if the user is anonymous, they aren't the author
        if (isAnonymous(user))
        {
            return false;
        }

        // if the attachment author is the user, return true
        return commentAuthor.equals(user.getName());
    }

    public boolean hasEditAllPermission(final User user, final Issue issue)
    {
        return permissionManager.hasPermission(Permissions.COMMENT_EDIT_ALL, issue, user);
    }

    public boolean hasEditOwnPermission(final User user, final Issue issue)
    {
        return permissionManager.hasPermission(Permissions.COMMENT_EDIT_OWN, issue, user);
    }

    public boolean hasDeleteAllPermission(final User user, final Issue issue)
    {
        return permissionManager.hasPermission(Permissions.COMMENT_DELETE_ALL, issue, user);
    }

    public boolean hasDeleteOwnPermission(final User user, final Issue issue)
    {
        return permissionManager.hasPermission(Permissions.COMMENT_DELETE_OWN, issue, user);
    }

    private boolean isUserInGroup(User user, String groupname)
    {
        if (user == null)
        {
            return false;
        }
        Group group = groupManager.getGroup(groupname);
        return group != null && groupManager.isUserInGroup(user, group);
    }

    private boolean isUserInRole(Long roleLevel, User user, Issue issue)
    {
        boolean isUserInRole = false;
        ProjectRole projectRole = projectRoleManager.getProjectRole(roleLevel);
        if (projectRole != null)
        {
            isUserInRole = projectRoleManager.isUserInProjectRole(user, projectRole, issue.getProjectObject());
        }
        return isUserInRole;
    }

}
