package com.atlassian.jira.bc.issue.comment;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.MutableComment;
import com.atlassian.jira.util.ErrorCollection;

import java.util.Date;
import java.util.List;

/**
 * This is the business layer component that must be used to access all {@link com.atlassian.jira.issue.comments.Comment} functionality.
 * This will perform validation before it hands off to the {@link com.atlassian.jira.issue.comments.CommentManager}.
 * Operations will not be performed if validation fails.
 */
public interface CommentService
{
    /**
     * Creates and persists a {@link Comment} on the given {@link Issue}.
     *
     * @param user            The {@link com.opensymphony.user.User} who will be performing the operation.
     * @param issue           The {@link Issue} you wish to associate the {@link Comment} with. This can not be null
     * @param body            The body of the comment
     * @param groupLevel      The group level visibility of the comment (null if roleLevelId specified)
     * @param roleLevelId     The role level visibility id of the comment (null if groupLevel specified)
     * @param created         The date of comment creation
     * @param dispatchEvent   whether or not you want to have an event dispatched on Comment creation
     * @param errorCollection holder for any errors that were thrown attempting to create a comment
     * @return the created Comment object, or null if no object created.
     */
    public Comment create(com.opensymphony.user.User user, Issue issue, String body, String groupLevel, Long roleLevelId, Date created, boolean dispatchEvent, ErrorCollection errorCollection);

    /**
     * Creates and persists a {@link Comment} on the given {@link Issue}.
     *
     * @param user            The {@link User} who will be performing the operation.
     * @param issue           The {@link Issue} you wish to associate the {@link Comment} with. This can not be null
     * @param body            The body of the comment
     * @param groupLevel      The group level visibility of the comment (null if roleLevelId specified)
     * @param roleLevelId     The role level visibility id of the comment (null if groupLevel specified)
     * @param created         The date of comment creation
     * @param dispatchEvent   whether or not you want to have an event dispatched on Comment creation
     * @param errorCollection holder for any errors that were thrown attempting to create a comment
     * @return the created Comment object, or null if no object created.
     */
    public Comment create(User user, Issue issue, String body, String groupLevel, Long roleLevelId, Date created, boolean dispatchEvent, ErrorCollection errorCollection);

    /**
     * Creates and persists a {@link Comment} on the given {@link Issue} set with current date and time.
     *
     * @param user            The {@link com.opensymphony.user.User} who will be performing the operation.
     * @param issue           The {@link Issue} you wish to associate the {@link Comment} with. This can not be null
     * @param body            The body of the comment
     * @param groupLevel      The group level visibility of the comment (null if roleLevelId specified)
     * @param roleLevelId     The role level visibility id of the comment (null if groupLevel specified)
     * @param dispatchEvent   whether or not you want to have an event dispatched on Comment creation
     * @param errorCollection holder for any errors that were thrown attempting to create a comment
     * @return the created Comment object, or null if no object created.
     */
    public Comment create(com.opensymphony.user.User user, Issue issue, String body, String groupLevel, Long roleLevelId, boolean dispatchEvent, ErrorCollection errorCollection);

    /**
     * Creates and persists a {@link Comment} on the given {@link Issue} set with current date and time.
     *
     * @param user            The {@link User} who will be performing the operation.
     * @param issue           The {@link Issue} you wish to associate the {@link Comment} with. This can not be null
     * @param body            The body of the comment
     * @param groupLevel      The group level visibility of the comment (null if roleLevelId specified)
     * @param roleLevelId     The role level visibility id of the comment (null if groupLevel specified)
     * @param dispatchEvent   whether or not you want to have an event dispatched on Comment creation
     * @param errorCollection holder for any errors that were thrown attempting to create a comment
     * @return the created Comment object, or null if no object created.
     */
    public Comment create(User user, Issue issue, String body, String groupLevel, Long roleLevelId, boolean dispatchEvent, ErrorCollection errorCollection);

    /**
     * Creates and persists a {@link Comment} on the given {@link Issue} set with current date and time, visible to all
     * - no group level or role level restriction.
     *
     * @param user            The {@link com.opensymphony.user.User} who will be performing the operation.
     * @param issue           The {@link Issue} you wish to associate the {@link Comment} with. This can not be null
     * @param body            The body of the comment
     * @param dispatchEvent   whether or not you want to have an event dispatched on Comment creation
     * @param errorCollection holder for any errors that were thrown attempting to create a comment
     * @return the created Comment object, or null if no object created.
     */
    public Comment create(com.opensymphony.user.User user, Issue issue, String body, boolean dispatchEvent, ErrorCollection errorCollection);

    /**
     * Creates and persists a {@link Comment} on the given {@link Issue} set with current date and time, visible to all
     * - no group level or role level restriction.
     *
     * @param user            The {@link User} who will be performing the operation.
     * @param issue           The {@link Issue} you wish to associate the {@link Comment} with. This can not be null
     * @param body            The body of the comment
     * @param dispatchEvent   whether or not you want to have an event dispatched on Comment creation
     * @param errorCollection holder for any errors that were thrown attempting to create a comment
     * @return the created Comment object, or null if no object created.
     */
    public Comment create(User user, Issue issue, String body, boolean dispatchEvent, ErrorCollection errorCollection);

    /**
     * Confirms the parameters to update a comment are valid and that the updating user has the permission to do so.
     * This method will validate the raw input parameters. This method only validates the parameters and will not
     * actually persist the changes, you must call {@link #update} to persist the changes. If an error is encountered
     * then the {@link ErrorCollection} will contain the specific error message.
     *
     * @param user            The {@link com.opensymphony.user.User} who will be performing the operation and who will be the updatedAuthor.
     * @param commentId       The id of the comment to be updated. Permissions will be checked to insure that the user
     *                        has the right to update this comment. If the comment does not exist an error will be reported.
     * @param body            The body of the comment to be updated.
     * @param groupLevel      The group level visibility of the comment (null if roleLevelId specified)
     * @param roleLevelId     The role level visibility id of the comment (null if groupLevel specified)
     * @param errorCollection holder for any errors that were thrown attempting to update a comment
     */
    public void validateCommentUpdate(com.opensymphony.user.User user, Long commentId, String body, String groupLevel, Long roleLevelId, ErrorCollection errorCollection);

    /**
     * Confirms the parameters to update a comment are valid and that the updating user has the permission to do so.
     * This method will validate the raw input parameters. This method only validates the parameters and will not
     * actually persist the changes, you must call {@link #update} to persist the changes. If an error is encountered
     * then the {@link ErrorCollection} will contain the specific error message.
     *
     * @param user            The {@link User} who will be performing the operation and who will be the updatedAuthor.
     * @param commentId       The id of the comment to be updated. Permissions will be checked to insure that the user
     *                        has the right to update this comment. If the comment does not exist an error will be reported.
     * @param body            The body of the comment to be updated.
     * @param groupLevel      The group level visibility of the comment (null if roleLevelId specified)
     * @param roleLevelId     The role level visibility id of the comment (null if groupLevel specified)
     * @param errorCollection holder for any errors that were thrown attempting to update a comment
     */
    public void validateCommentUpdate(User user, Long commentId, String body, String groupLevel, Long roleLevelId, ErrorCollection errorCollection);

    /**
     * Updates a {@link Comment} and sets the comments updated date to be now and the updatedAuthor to be the
     * passed in user.
     *
     * @param user            the user who must have permission to update this comment and who will be used as the udpateAuthor
     * @param comment         the object that contains the changes to the comment to persist.
     * @param dispatchEvent   whether or not you want to have an event dispatched on Comment update. If false then
     *                        the issue will not be reindexed.
     * @param errorCollection holder for any errors that were thrown attempting to update a comment
     * @throws IllegalArgumentException if comment or its id is null
     */
    public void update(com.opensymphony.user.User user, MutableComment comment, boolean dispatchEvent, ErrorCollection errorCollection);

    /**
     * Updates a {@link Comment} and sets the comments updated date to be now and the updatedAuthor to be the
     * passed in user.
     *
     * @param user            the user who must have permission to update this comment and who will be used as the udpateAuthor
     * @param comment         the object that contains the changes to the comment to persist.
     * @param dispatchEvent   whether or not you want to have an event dispatched on Comment update. If false then
     *                        the issue will not be reindexed.
     * @param errorCollection holder for any errors that were thrown attempting to update a comment
     * @throws IllegalArgumentException if comment or its id is null
     */
    public void update(User user, MutableComment comment, boolean dispatchEvent, ErrorCollection errorCollection);

    /**
     * Will return a list of {@link Comment}s for the given user
     *
     * @param currentUser     current user
     * @param issue           the issue with associated comments
     * @param errorCollection holder for any errors that were thrown attempting to update a comment
     * @return a List of comments
     */
    public List<Comment> getCommentsForUser(com.opensymphony.user.User currentUser, Issue issue, ErrorCollection errorCollection);

    /**
     * Will return a list of {@link Comment}s for the given user
     *
     * @param currentUser     current user
     * @param issue           the issue with associated comments
     * @param errorCollection holder for any errors that were thrown attempting to update a comment
     * @return a List of comments
     */
    public List<Comment> getCommentsForUser(User currentUser, Issue issue, ErrorCollection errorCollection);

    /**
     * Has the correct permission to create a comment for the given issue.
     *
     * @param user            The {@link com.opensymphony.user.User} who will be performing the operation.
     * @param issue           The {@link Issue} you wish to associate the {@link Comment} with. This can not be null.
     * @param errorCollection holder for any errors that were thrown attempting permission checks
     * @return true if permission check passes.
     */
    public boolean hasPermissionToCreate(com.opensymphony.user.User user, Issue issue, ErrorCollection errorCollection);

    /**
     * Has the correct permission to create a comment for the given issue.
     *
     * @param user            The {@link User} who will be performing the operation.
     * @param issue           The {@link Issue} you wish to associate the {@link Comment} with. This can not be null.
     * @param errorCollection holder for any errors that were thrown attempting permission checks
     * @return true if permission check passes.
     */
    public boolean hasPermissionToCreate(User user, Issue issue, ErrorCollection errorCollection);

    /**
     * Determine whether the current user has the permission to edit the
     * comment. In case of errors, add error messages to the error collection.
     * <p/>
     * Passing in null comment or a comment with null ID will return false and
     * an error message will be added to the error collection.
     * <p/>
     * Passing in null error collection will throw NPE.
     *
     * @param user            The {@link com.opensymphony.user.User} who will be performing the operation.
     * @param comment         The {@link Comment} you wish to edit.
     * @param errorCollection holder for any errors that were thrown attempting permission checks
     * @return true if the user has edit permission, false otherwise
     */
    public boolean hasPermissionToEdit(com.opensymphony.user.User user, Comment comment, ErrorCollection errorCollection);

    /**
     * Determine whether the current user has the permission to edit the
     * comment. In case of errors, add error messages to the error collection.
     * <p/>
     * Passing in null comment or a comment with null ID will return false and
     * an error message will be added to the error collection.
     * <p/>
     * Passing in null error collection will throw NPE.
     *
     * @param user            The {@link User} who will be performing the operation.
     * @param comment         The {@link Comment} you wish to edit.
     * @param errorCollection holder for any errors that were thrown attempting permission checks
     * @return true if the user has edit permission, false otherwise
     */
    public boolean hasPermissionToEdit(User user, Comment comment, ErrorCollection errorCollection);

    /**
     * Validates that the body is a valid string, if not the appropriate error
     * is added to the <code>errorCollection</code>. This method was added so
     * the CommentSystemField can validate the body and set the appropriate error message.
     *
     * @param body            comment body to validate
     * @param errorCollection holder for any errors that can occur in process of validation
     * @return true is the body is valid.
     */
    public boolean isValidCommentBody(String body, ErrorCollection errorCollection);

    /**
     * This method validates if the comment has the correct role and group
     * levels set. If there is an error during validation the passed in
     * <code>errorCollection</code> will contain the errors.
     *
     * @param user            The {@link com.opensymphony.user.User} who will be performing the operation.
     * @param issue           issue to associate the comment with
     * @param groupLevel      comment group visibility level
     * @param roleLevelId     comment project role visibility level id
     * @param errorCollection holder for any errors that can occur in process of validation
     * @return true if the role and group level information has been set correctly for a comment
     */
    public boolean isValidCommentData(com.opensymphony.user.User user, Issue issue, String groupLevel, String roleLevelId, ErrorCollection errorCollection);

    /**
     * This method validates if the comment has the correct role and group
     * levels set. If there is an error during validation the passed in
     * <code>errorCollection</code> will contain the errors.
     *
     * @param user            The {@link User} who will be performing the operation.
     * @param issue           issue to associate the comment with
     * @param groupLevel      comment group visibility level
     * @param roleLevelId     comment project role visibility level id
     * @param errorCollection holder for any errors that can occur in process of validation
     * @return true if the role and group level information has been set correctly for a comment
     */
    public boolean isValidCommentData(User user, Issue issue, String groupLevel, String roleLevelId, ErrorCollection errorCollection);

    /**
     * Will call all other validate methods setting the appropriate errors
     * in the <code>errorCollection</code> if any errors occur.
     *
     * @param user            The {@link com.opensymphony.user.User} who will be performing the operation.
     * @param issue           issue to associate the comment with
     * @param body            comment body
     * @param groupLevel      comment group visibility level
     * @param roleLevelId     comment project role visibility level id
     * @param errorCollection holder for any errors that can occur in process of validarion
     * @return true if validation passes
     */
    public boolean isValidAllCommentData(com.opensymphony.user.User user, Issue issue, String body, String groupLevel, String roleLevelId, ErrorCollection errorCollection);

    /**
     * Will call all other validate methods setting the appropriate errors
     * in the <code>errorCollection</code> if any errors occur.
     *
     * @param user            The {@link User} who will be performing the operation.
     * @param issue           issue to associate the comment with
     * @param body            comment body
     * @param groupLevel      comment group visibility level
     * @param roleLevelId     comment project role visibility level id
     * @param errorCollection holder for any errors that can occur in process of validarion
     * @return true if validation passes
     */
    public boolean isValidAllCommentData(User user, Issue issue, String body, String groupLevel, String roleLevelId, ErrorCollection errorCollection);

    /**
     * Returns the flag that indicates whether group visiblity is enabled
     *
     * @return true if enabled, false otherwise
     */
    boolean isGroupVisiblityEnabled();

    /**
     * Returns the flag that indicates whether project role visibility is enabled
     *
     * @return true if enabled, false otherwise
     */
    boolean isProjectRoleVisiblityEnabled();

    /**
     * Will return a comment for the passed in commentId. This will return null
     * if the user does not have permission to view the comment
     *
     * @param user            who is looking up the comment
     * @param commentId       the id representing the {@link Comment} you would like to retrieve.
     * @param errorCollection holder for any errors that can occur in process of validation
     * @return a {@link Comment} or null  (if the user cannot browse the comment).
     */
    public Comment getCommentById(com.opensymphony.user.User user, Long commentId, ErrorCollection errorCollection);

    /**
     * Will return a comment for the passed in commentId. This will return null
     * if the user does not have permission to view the comment
     *
     * @param user            who is looking up the comment
     * @param commentId       the id representing the {@link Comment} you would like to retrieve.
     * @param errorCollection holder for any errors that can occur in process of validation
     * @return a {@link Comment} or null  (if the user cannot browse the comment).
     */
    public Comment getCommentById(User user, Long commentId, ErrorCollection errorCollection);

    /**
     * Will return a {@link MutableComment} for the passed in commentId. This
     * will return null if the user does not have permission to view the
     * comment. The difference between this method and
     * {@link #getCommentById(com.opensymphony.user.User,Long,ErrorCollection)} is that this method
     * returns a version of the {@link Comment} that we can set values on.
     *
     * @param user            the current user.
     * @param commentId       the id that we use to find the comment object.
     * @param errorCollection holder for any errors that can occur in process of validation
     * @return the comment that is identified by the commentId.
     */
    public MutableComment getMutableComment(com.opensymphony.user.User user, Long commentId, ErrorCollection errorCollection);

    /**
     * Will return a {@link MutableComment} for the passed in commentId. This
     * will return null if the user does not have permission to view the
     * comment. The difference between this method and
     * {@link #getCommentById(User,Long,ErrorCollection)} is that this method
     * returns a version of the {@link Comment} that we can set values on.
     *
     * @param user            the current user.
     * @param commentId       the id that we use to find the comment object.
     * @param errorCollection holder for any errors that can occur in process of validation
     * @return the comment that is identified by the commentId.
     */
    public MutableComment getMutableComment(User user, Long commentId, ErrorCollection errorCollection);

    /**
     * Determines whether the user can delete a comment. Will return true when the following are satisfied:
     * <ul>
     * <li>The user has the DELETE_COMMENT_ALL permission, or the user has the DELETE_COMMENT_OWN permission and is
     * attempting to delete a comment they authored</li>
     * <li>The issue is in an editable workflow state</li>
     * </ul>
     *
     * @param jiraServiceContext jiraServiceContext containing the user who wishes to delete a comment and the
     *                           errorCollection that will contain any errors encountered when calling the method
     * @param commentId          the id of the target comment (cannot be null)
     * @return true if the user has permission to delete the target comment, false otherwise
     */
    public boolean hasPermissionToDelete(JiraServiceContext jiraServiceContext, Long commentId);

    /**
     * Determines whether the user can edit a comment. Will return true when the following are satisfied:
     * <ul>
     * <li>The user has the comment edit all or comment edit own permission</li>
     * <li>The issue is in an editable workflow state</li>
     * </ul>
     *
     * @param jiraServiceContext JIRA service context containing the user who wishes to edit a comment and the
     *                           errorCollection that will contain any errors encountered when calling the method
     * @param commentId          the id of the target comment (cannot be null)
     * @return true if the user has permission to edit the comment, false otherwise
     */
    public boolean hasPermissionToEdit(JiraServiceContext jiraServiceContext, Long commentId);

    /**
     * Deletes a comment and updates the issue's change history and updated date. Expects that
     * {@link #hasPermissionToDelete(com.atlassian.jira.bc.JiraServiceContext,Long)} is successfully called first.
     *
     * @param jiraServiceContext containing the user who wishes to delete a comment and the errorCollection
     *                           that will contain any errors encountered when calling the method
     * @param comment            the comment to delete (cannot be null)
     * @param dispatchEvent      a flag indicating whether to dispatch an issue updated event. If this flag is false then
     *                           the issue will not be reindexed.
     */
    public void delete(JiraServiceContext jiraServiceContext, Comment comment, boolean dispatchEvent);

}
