package com.atlassian.jira.bc.issue.comment;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraException;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.util.VisibilityValidator;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.comments.CommentPermissionManager;
import com.atlassian.jira.issue.comments.MutableComment;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import com.atlassian.jira.issue.util.IssueUpdater;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Default implementation of comment service.
 */
public class DefaultCommentService implements CommentService
{
    private static final Logger log = Logger.getLogger(DefaultCommentService.class);

    private static final String ERROR_NO_PERMISSION = "comment.service.error.no.permission";
    private static final String ERROR_NO_PERMISSION_NO_USER = "comment.service.error.no.permission.no.user";
    private static final String ERROR_NO_EDIT_PERMISSION = "comment.service.error.no.edit.permission";
    private static final String ERROR_NO_EDIT_PERMISSION_NO_USER = "comment.service.error.no.edit.permission.no.user";
    private static final String ERROR_NO_COMMENT_VISIBILITY = "comment.service.error.no.comment.visibility";
    private static final String ERROR_NO_COMMENT_VISIBILITY_NO_USER = "comment.service.error.no.comment.visibility.no.user";
    private static final String ERROR_NULL_ISSUE = "comment.service.error.issue.null";
    private static final String ERROR_NULL_BODY = "comment.service.error.body.null";
    private static final String ERROR_USER_NOT_IN_ROLE = "comment.service.error.usernotinrole";
    private static final String ERROR_ROLE_DOES_NOT_EXIST = "comment.service.error.roledoesnotexist";
    private static final String ERROR_ROLE_ID_NOT_NUMBER = "comment.service.error.roleidnotnumber";
    private static final String ERROR_NO_COMMENT_FOR_ID = "comment.service.error.no.comment.for.id";
    private static final String ERROR_NO_ID_SPECIFIED = "comment.service.error.no.id.specified";
    public static final String ERROR_NULL_COMMENT_ID = "comment.service.error.update.null.comment.id";
    public static final String ERROR_NULL_COMMENT = "comment.service.error.update.null.comment";
    public static final String ERROR_NULL_COMMENT_DELETE = "comment.service.error.delete.null.comment";
    public static final String ERROR_NULL_COMMENT_ID_DELETE = "comment.service.error.delete.null.comment.id";
    public static final String ERROR_COMMENT_DELETE_ISSUE_UPDATE_FAILED = "comment.service.error.delete.issue.update.failed";
    public static final String ERROR_COMMENT_EDIT_NON_EDITABLE_ISSUE = "comment.service.error.edit.issue.non.editable";
    public static final String ERROR_COMMENT_DELETE_NON_EDITABLE_ISSUE = "comment.service.error.delete.issue.non.editable";
    public static final String ERROR_COMMENT_DELETE_NO_PERMISSION = "comment.service.error.delete.no.permission";

    private static final String COMMENT_I18N_PREFIX = "comment";

    private CommentManager commentManager;
    private PermissionManager permissionManager;
    private JiraAuthenticationContext jiraAuthenticationContext;
    private ProjectRoleManager projectRoleManager;
    private CommentPermissionManager commentPermissionManager;
    private final IssueUpdater issueUpdater;
    private final IssueManager issueManager;
    private final VisibilityValidator visibilityValidator;

    public DefaultCommentService(CommentManager commentManager, PermissionManager permissionManager,
                                 JiraAuthenticationContext jiraAuthenticationContext,
                                 ProjectRoleManager projectRoleManager,
                                 CommentPermissionManager commentPermissionManager, IssueUpdater issueUpdater,
                                 IssueManager issueManager, VisibilityValidator visibilityValidator)
    {
        this.commentManager = commentManager;
        this.permissionManager = permissionManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.projectRoleManager = projectRoleManager;
        this.commentPermissionManager = commentPermissionManager;
        this.issueUpdater = issueUpdater;
        this.issueManager = issueManager;
        this.visibilityValidator = visibilityValidator;
    }

    public Comment create(User user, Issue issue, String body, boolean dispatchEvent, ErrorCollection errorCollection)
    {
        return create(user, issue, body, null, null, null, dispatchEvent, errorCollection);
    }

    @Override
    public void validateCommentUpdate(com.opensymphony.user.User user, Long commentId, String body, String groupLevel, Long roleLevelId, ErrorCollection errorCollection)
    {
        validateCommentUpdate((User) user, commentId, body, groupLevel, roleLevelId, errorCollection);
    }

    public void validateCommentUpdate(User user, Long commentId, String body, String groupLevel, Long roleLevelId, ErrorCollection errorCollection)
    {
        if (commentId == null)
        {
            errorCollection.addErrorMessage(getText(ERROR_NULL_COMMENT_ID));
            return;
        }

        // get the mutable comment
        MutableComment mutableComment = getMutableComment(user, commentId, errorCollection);
        if (mutableComment == null)
        {
            // no need to add any message as getMutableComment() did it already
            return;
        }

        hasPermissionToEdit(user, mutableComment, errorCollection);
        if (errorCollection.hasAnyErrors())
        {
            return;
        }

        isValidAllCommentData(user, mutableComment.getIssue(), body, groupLevel, roleLevelId == null ? null : roleLevelId.toString(), errorCollection);
    }

    @Override
    public void update(com.opensymphony.user.User user, MutableComment comment, boolean dispatchEvent, ErrorCollection errorCollection)
    {
        update((User) user, comment, dispatchEvent, errorCollection);
    }

    public void update(User user, MutableComment comment, boolean dispatchEvent, ErrorCollection errorCollection)
    {
        if (comment == null)
        {
            errorCollection.addErrorMessage(getText(ERROR_NULL_COMMENT));
            return;
        }
        if (comment.getId() == null)
        {
            errorCollection.addErrorMessage(getText(ERROR_NULL_COMMENT_ID));
            return;
        }

        hasPermissionToEdit(user, comment, errorCollection);
        if (!errorCollection.hasAnyErrors())
        {
            comment.setUpdateAuthor(user == null ? null : user.getName());
            comment.setUpdated(new Date(System.currentTimeMillis()));

            commentManager.update(comment, dispatchEvent);
        }
    }

    @Override
    public List<Comment> getCommentsForUser(com.opensymphony.user.User currentUser, Issue issue, ErrorCollection errorCollection)
    {
        return getCommentsForUser((User) currentUser, issue, errorCollection);
    }

    public Comment create(User user, Issue issue, String body, String groupLevel, Long roleLevelId, boolean dispatchEvent, ErrorCollection errorCollection)
    {
        return create(user, issue, body, groupLevel, roleLevelId, null, dispatchEvent, errorCollection);
    }

    @Override
    public Comment create(com.opensymphony.user.User user, Issue issue, String body, boolean dispatchEvent, ErrorCollection errorCollection)
    {
        return create((User) user, issue, body, dispatchEvent, errorCollection);
    }

    @Override
    public Comment create(com.opensymphony.user.User user, Issue issue, String body, String groupLevel, Long roleLevelId, Date created, boolean dispatchEvent, ErrorCollection errorCollection)
    {
        return create((User) user, issue, body, groupLevel, roleLevelId, created, dispatchEvent, errorCollection);
    }

    public Comment create(User user, Issue issue, String body, String groupLevel, Long roleLevelId, Date created, boolean dispatchEvent, ErrorCollection errorCollection)
    {
        Comment comment = null;
        if (hasPermissionToCreate(user, issue, errorCollection))
        {
            final String roleLevelIdString = roleLevelId == null ? null : roleLevelId.toString();
            if (isValidAllCommentData(user, issue, body, groupLevel, roleLevelIdString, errorCollection))
            {
                String author = user == null ? null : user.getName();
                comment = commentManager.create(issue, author, body, groupLevel, roleLevelId, created, dispatchEvent);
            }
        }
        return comment;
    }

    @Override
    public Comment create(com.opensymphony.user.User user, Issue issue, String body, String groupLevel, Long roleLevelId, boolean dispatchEvent, ErrorCollection errorCollection)
    {
        return create((User) user, issue, body, groupLevel, roleLevelId, dispatchEvent, errorCollection);
    }

    public List<Comment> getCommentsForUser(User currentUser, Issue issue, ErrorCollection errorCollection)
    {
        boolean internalError = false;

        List<Comment> comments = new ArrayList<Comment>();

        if (issue == null)
        {
            errorCollection.addErrorMessage(getText(ERROR_NULL_ISSUE));
            internalError = true;
        }

        if (!internalError)
        {
            comments = commentManager.getCommentsForUser(issue, currentUser);
        }

        return comments;
    }

    @Override
    public boolean hasPermissionToCreate(com.opensymphony.user.User user, Issue issue, ErrorCollection errorCollection)
    {
        return hasPermissionToCreate((User) user, issue, errorCollection);
    }

    public boolean isValidCommentData(User currentUser, Issue issue, String groupLevel, String roleLevelId, ErrorCollection errorCollection)
    {
        return visibilityValidator.isValidVisibilityData(new JiraServiceContextImpl(currentUser, errorCollection), "comment", issue, groupLevel, roleLevelId);
    }

    @Override
    public boolean isValidAllCommentData(com.opensymphony.user.User user, Issue issue, String body, String groupLevel, String roleLevelId, ErrorCollection errorCollection)
    {
        return isValidAllCommentData((User) user, issue, body, groupLevel, roleLevelId, errorCollection);
    }

    public boolean isValidCommentBody(String body, ErrorCollection errorCollection)
    {
        boolean valid = true;
        if (StringUtils.isBlank(body))
        {
            valid = false;
            errorCollection.addError("comment", getText(ERROR_NULL_BODY));
        }
        return valid;
    }

    @Override
    public boolean isValidCommentData(com.opensymphony.user.User user, Issue issue, String groupLevel, String roleLevelId, ErrorCollection errorCollection)
    {
        return isValidCommentData((User) user, issue, groupLevel, roleLevelId, errorCollection);
    }

    public boolean isValidAllCommentData(User currentUser, Issue issue, String body, String groupLevel, String roleLevelId, ErrorCollection errorCollection)
    {
        boolean validCommentBody = isValidCommentBody(body, errorCollection);
        boolean validCommentData = isValidCommentData(currentUser, issue, groupLevel, roleLevelId, errorCollection);
        return validCommentBody && validCommentData;
    }

    public boolean isGroupVisiblityEnabled()
    {
        return visibilityValidator.isGroupVisiblityEnabled();
    }

    public boolean isProjectRoleVisiblityEnabled()
    {
        return visibilityValidator.isProjectRoleVisiblityEnabled();
    }

    @Override
    public Comment getCommentById(com.opensymphony.user.User user, Long commentId, ErrorCollection errorCollection)
    {
        return getCommentById((User) user, commentId, errorCollection);
    }

    public Comment getCommentById(User user, Long commentId, ErrorCollection errorCollection)
    {
        return getMutableComment(user, commentId, errorCollection);
    }

    @Override
    public MutableComment getMutableComment(com.opensymphony.user.User user, Long commentId, ErrorCollection errorCollection)
    {
        return getMutableComment((User) user, commentId, errorCollection);
    }

    public MutableComment getMutableComment(User user, Long commentId, ErrorCollection errorCollection)
    {
        if (commentId == null)
        {
            errorCollection.addErrorMessage(getText(ERROR_NO_ID_SPECIFIED));
            return null;
        }
        MutableComment comment = commentManager.getMutableComment(commentId);

        // Check that the comment exists
        if (comment == null)
        {
            errorCollection.addErrorMessage(getText(ERROR_NO_COMMENT_FOR_ID, commentId.toString()));
            return null;
        }

        if (commentPermissionManager.hasBrowsePermission(user, comment))
        {
            return comment;
        }
        else
        {
            if (user == null)
            {
                errorCollection.addErrorMessage(getText(ERROR_NO_PERMISSION_NO_USER));
            }
            else
            {
                errorCollection.addErrorMessage(getText(ERROR_NO_PERMISSION, user.getDisplayName()));
            }
        }
        return null;
    }

    public boolean hasPermissionToDelete(JiraServiceContext jiraServiceContext, Long commentId)
    {
        ErrorCollection errorCollection = new SimpleErrorCollection();
        User user = jiraServiceContext.getUser();

        // This will do the checks against the commentId and comment object existing
        Comment comment = getCommentById(user, commentId, errorCollection);

        if (errorCollection.hasAnyErrors())
        {
            jiraServiceContext.getErrorCollection().addErrorCollection(errorCollection);
            return false;
        }

        // Do a check to make sure that the user is a member of the role or group if the comment is protected by
        // any of these visibility levels
        if (!hasVisibility(jiraServiceContext, comment))
        {
            return false;
        }

        Issue issue = comment.getIssue();
        if (!isIssueInEditableWorkflowState(issue))
        {
            errorCollection.addErrorMessage(getText(ERROR_COMMENT_DELETE_NON_EDITABLE_ISSUE));
            jiraServiceContext.getErrorCollection().addErrorCollection(errorCollection);
            return false;
        }

        if (userHasCommentDeleteAllPermission(issue, user)
                || (userHasCommentDeleteOwnPermission(issue, user) && commentManager.isUserCommentAuthor(user, comment)))
        {
            return true;
        }

        // Add an error about not having permission
        errorCollection.addErrorMessage(getText(ERROR_COMMENT_DELETE_NO_PERMISSION, String.valueOf(comment.getId())));
        jiraServiceContext.getErrorCollection().addErrorCollection(errorCollection);
        return false;
    }

    protected boolean hasVisibility(JiraServiceContext jiraServiceContext, Comment comment)
    {
        final ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();
        final User user = jiraServiceContext.getUser();

        final Issue issue = comment.getIssue();

        // Do a check to make sure that the user is a member of the role or group if the worklog is protected by
        // any of these visibility levels
        boolean visible = visibilityValidator.isValidVisibilityData(
                new JiraServiceContextImpl(user, errorCollection),
                COMMENT_I18N_PREFIX,
                issue,
                comment.getGroupLevel(),
                comment.getRoleLevelId() == null ? null : comment.getRoleLevelId().toString());

        if (!visible)
        {
            if (user != null)
            {
                errorCollection.addErrorMessage(getText(ERROR_NO_COMMENT_VISIBILITY, user.getDisplayName()));
            }
            else
            {
                errorCollection.addErrorMessage(getText(ERROR_NO_COMMENT_VISIBILITY_NO_USER));
            }
        }
        return visible;
    }

    public void delete(JiraServiceContext jiraServiceContext, Comment comment, boolean dispatchEvent)
    {
        ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();

        // Check that the comment exists
        if (comment == null)
        {
            errorCollection.addErrorMessage(getText(ERROR_NULL_COMMENT_DELETE, null));
            return;
        }

        if (comment.getId() == null)
        {
            errorCollection.addErrorMessage(getText(ERROR_NULL_COMMENT_ID_DELETE));
            return;
        }

        // Re-do the permission check
        if (hasPermissionToDelete(jiraServiceContext, comment.getId()))
        {
            // Do the actual delete and get the change item caused by the delete
            ChangeItemBean changeItem = commentManager.delete(comment);

            // Persist the changeItem and update the issues update date
            try
            {
                doUpdateWithChangelog(EventType.ISSUE_UPDATED_ID, EasyList.build(changeItem), comment.getIssue(), jiraServiceContext.getUser(), dispatchEvent);
            }
            catch (JiraException e)
            {
                log.error("Unable to update the issue with information about deleting a comment.", e);
                errorCollection.addErrorMessage(getText(ERROR_COMMENT_DELETE_ISSUE_UPDATE_FAILED));
            }
        }

    }

    public boolean hasPermissionToCreate(User user, Issue issue, ErrorCollection errorCollection)
    {
        boolean hasPerm = (issue.getProjectObject() == null)
                ? permissionManager.hasPermission(Permissions.COMMENT_ISSUE, issue, user)
                : permissionManager.hasPermission(Permissions.COMMENT_ISSUE, issue.getProjectObject(), user);

        if (!hasPerm)
        {
            //JRA-11539 User may be null if the session has timed out or the user has logged out while entering the comment
            if (user != null)
            {
                errorCollection.addErrorMessage(getText(ERROR_NO_PERMISSION, user.getDisplayName()));
            }
            else
            {
                errorCollection.addErrorMessage(getText(ERROR_NO_PERMISSION_NO_USER));
            }
        }
        return hasPerm;
    }

    @Override
    public boolean hasPermissionToEdit(com.opensymphony.user.User user, Comment comment, ErrorCollection errorCollection)
    {
        return hasPermissionToEdit((User) user, comment, errorCollection);
    }

    public boolean hasPermissionToEdit(JiraServiceContext jiraServiceContext, Long commentId)
    {
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final User user = jiraServiceContext.getUser();

        // This will do the checks against the commentId and comment object existing
        final Comment comment = getCommentById(user, commentId, errorCollection);

        if (errorCollection.hasAnyErrors())
        {
            jiraServiceContext.getErrorCollection().addErrorCollection(errorCollection);
            return false;
        }

        return hasPermissionToEdit(user, comment, jiraServiceContext.getErrorCollection());
    }

    public boolean hasPermissionToEdit(User user, Comment comment, ErrorCollection errorCollection)
    {
        if (comment == null)
        {
            errorCollection.addErrorMessage(getText(ERROR_NULL_COMMENT));
            return false;
        }
        if (comment.getId() == null)
        {
            errorCollection.addErrorMessage(getText(ERROR_NULL_COMMENT_ID));
            return false;
        }

        if (!isIssueInEditableWorkflowState(comment.getIssue()))
        {
            errorCollection.addErrorMessage(getText(ERROR_COMMENT_EDIT_NON_EDITABLE_ISSUE));
            return false;
        }

        // Do a check to make sure that the user is a member of the role or group if the comment is protected by
        // any of these visibility levels
        if (!hasVisibility(new JiraServiceContextImpl(user, errorCollection), comment))
        {
            return false;
        }

        boolean hasPerm = commentPermissionManager.hasEditPermission(user, comment);
        if (!hasPerm)
        {
            //JRA-11539 User may be null if the session has timed out or the user has logged out while entering the comment
            if (user != null)
            {
                errorCollection.addErrorMessage(getText(ERROR_NO_EDIT_PERMISSION, user.getDisplayName()));
            }
            else
            {
                errorCollection.addErrorMessage(getText(ERROR_NO_EDIT_PERMISSION_NO_USER));
            }
        }
        return hasPerm;
    }

    /**
     * This method 'completes' the update of an issue entity.
     * <p/>
     * It sets the update timestamp, stores the issue, updated the cache if needed,
     * creates the changelog and dispatches the event (if desired).
     * <p/>
     * This method will ALWAYS generate an update - see also doUpdateIfNeeded.
     *
     * @param eventTypeId   event type id
     * @param changeItems   list of change items
     * @param issue         issue to update
     * @param user          user performing this operation
     * @param dispatchEvent dispatch event flag
     * @throws JiraException if update fails
     */
    protected void doUpdateWithChangelog(Long eventTypeId, List changeItems, Issue issue, User user, boolean dispatchEvent)
            throws JiraException
    {
        IssueUpdateBean issueUpdateBean = new IssueUpdateBean(issue.getGenericValue(), issue.getGenericValue(), eventTypeId, user);
        issueUpdateBean.setChangeItems(changeItems);
        if (dispatchEvent)
        {
            issueUpdateBean.setDispatchEvent(true);
        }

        issueUpdater.doUpdate(issueUpdateBean, false);
    }

    boolean isIssueInEditableWorkflowState(Issue issue)
    {
        return issueManager.isEditable(issue);
    }

    boolean userHasCommentDeleteAllPermission(Issue issue, User user)
    {
        return permissionManager.hasPermission(Permissions.COMMENT_DELETE_ALL, issue, user);
    }

    boolean userHasCommentDeleteOwnPermission(Issue issue, User user)
    {
        return permissionManager.hasPermission(Permissions.COMMENT_DELETE_OWN, issue, user);
    }

    private boolean isRoleLevelValid(String roleLevelId, User currentUser, Issue issue, ErrorCollection errorCollection)
    {
        boolean valid = true;
        // If we have a roleLevel specified, ensure that the roleLevel exists and the user is in it.
        try
        {
            ProjectRole projectRole = projectRoleManager.getProjectRole(new Long(roleLevelId));
            if (projectRole != null)
            {
                if (!projectRoleManager.isUserInProjectRole(currentUser, projectRole, issue.getProjectObject()))
                {
                    errorCollection.addError("commentLevel", getText(ERROR_USER_NOT_IN_ROLE, projectRole.getName()));
                    valid = false;
                }
            }
            else
            {
                errorCollection.addError("commentLevel", getText(ERROR_ROLE_DOES_NOT_EXIST, roleLevelId));
                valid = false;
            }
        }
        catch (NumberFormatException e)
        {
            errorCollection.addError("commentLevel", getText(ERROR_ROLE_ID_NOT_NUMBER));
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
