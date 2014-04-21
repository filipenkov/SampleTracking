package com.atlassian.jira.issue.comments;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.security.roles.ProjectRole;
import org.ofbiz.core.entity.GenericValue;

import java.util.Date;
import java.util.List;

/**
 * The CommentManager is used to retrieve and create comments in JIRA.
 * Comments are always associated with an issue.
 */
@PublicApi
public interface CommentManager
{

    public static final String EVENT_ORIGINAL_COMMENT_PARAMETER = "originalcomment";

    /**
     * Retrieves all {@link Comment}s on the
     * given Issue that the given {@link User} has permission to see.
     * If the passed in user is null, only comments with no permission level set will be returned.
     * <p/>
     * <strong>NOTE:</strong> keep in mind null user represents an anonymous i.e. non-logged in user.
     *
     * @param issue the comments are associated with.
     * @param user  the user whose permission level will be used to limit the comments returned.
     * @return Possibly empty List of {@link Comment}s
     */
    public List<Comment> getCommentsForUser(Issue issue, User user);

    /**
     * This will return all comments for a given issue.
     * Please note that this method does not perform any permission checks on the returned comments.
     * If you are returning comments to the UI or service for a given {@link User} please use
     * {@link #getCommentsForUser(com.atlassian.jira.issue.Issue, com.atlassian.crowd.embedded.api.User)}
     *
     * @param issue the comments are associated with.
     * @return a List of comments, will return an empty list if none found.
     */
    public List<Comment> getComments(Issue issue);

    /**
     * Creates a comment and associates it with the given issue. Using this method the comment will be created
     * with a createdDate of the current date/time and with no visibility restrictions.
     *
     * @param issue         the issue to associate the comment with.
     * @param author        the user who has created this comment.
     * @param body          the text of the comment.
     * @param dispatchEvent if true then an event of type {@link com.atlassian.jira.event.type.EventType#ISSUE_COMMENTED_ID}
     *                      will be dispatched and any notifications listening for that event will be triggered.
     *                      If false no event will be dispatched.
     * @return the object representation of the newly created comment.
     */
    public Comment create(Issue issue, String author, String body, boolean dispatchEvent);

    /**
     * Creates a comment and associates it with the given issue. Using this method the comment will be created
     * with a createdDate of the current time. If you have provided a groupLevel then the comment visibility will be restricted
     * to the provided group, it is assumed that validation to insure that the group actually exists has been performed
     * outside of this method. If you have provided a roleLevelId then the comment visibility will be restricted to the
     * provided role, it is assumed that validation to insure that the role actually exists has been performed outside
     * of this method.
     * <p/>
     * <strong>NOTE:</strong> A comment should not have both a group level and role level visibility restriction. This
     * method will not stop this, but it does not semantically make sense.
     *
     * @param issue         the issue to associate the comment with.
     * @param author        the user who has created this comment.
     * @param body          the text of the comment.
     * @param groupLevel    is the group name to limit comment visibility to, this must be a valid group name.
     * @param roleLevelId   is the id of the the {@link ProjectRole} to limit comment visibility to, this must reference a
     *                      valid project role.
     * @param dispatchEvent if true then an event of type {@link com.atlassian.jira.event.type.EventType#ISSUE_COMMENTED_ID}
     *                      will be dispatched and any notifications listening for that event will be triggered.
     *                      If false no event will be dispatched.
     * @return the object representation of the newly created comment.
     */
    public Comment create(Issue issue, String author, String body, String groupLevel, Long roleLevelId, boolean dispatchEvent);

    /**
     * Creates a comment and associates it with the given issue. Using this method the comment will be created
     * with a createdDate of the specified date. This method should be used if you are trying to preserve existing
     * information and it is important to retain the original created date. If you have provided a groupLevel then the
     * comment visibility will be restricted to the provided group, it is assumed that validation to insure that the
     * group actually exists has been performed outside of this method. If you have provided a roleLevelId then the
     * comment visibility will be restricted to the provided role, it is assumed that validation to insure that the
     * role actually exists has been performed outside of this method.
     * <p/>
     * <strong>NOTE:</strong> A comment should not have both a group level and role level visibility restriction. This
     * method will not stop this, but it does not semantically make sense.
     *
     * @param issue         the issue to associate the comment with.
     * @param author        the user who has created this comment.
     * @param body          the text of the comment.
     * @param groupLevel    is the group name to limit comment visibility to, this must be a valid group name.
     * @param roleLevelId   is the id of the the {@link ProjectRole} to limit comment visibility to, this must reference a
     *                      valid project role.
     * @param created       is the date that will be used as the comments creation date.
     * @param dispatchEvent if true then an event of type {@link com.atlassian.jira.event.type.EventType#ISSUE_COMMENTED_ID}
     *                      will be dispatched and any notifications listening for that event will be triggered.
     *                      If false no event will be dispatched.
     * @return the object representation of the newly created comment.
     */
    public Comment create(Issue issue, String author, String body, String groupLevel, Long roleLevelId, Date created, boolean dispatchEvent);

    /**
     * Creates a comment and associates it with the given issue. Using this method the comment will be created
     * with a createdDate of the specified date. This method should be used if you are trying to preserve existing
     * information and it is important to retain the original created date. If you have provided a groupLevel then the
     * comment visibility will be restricted to the provided group, it is assumed that validation to insure that the
     * group actually exists has been performed outside of this method. If you have provided a roleLevelId then the
     * comment visibility will be restricted to the provided role, it is assumed that validation to insure that the
     * role actually exists has been performed outside of this method.
     * <p/>
     * <strong>NOTE:</strong> A comment should not have both a group level and role level visibility restriction. This
     * method will not stop this, but it does not semantically make sense.
     *
     * @param issue         the issue to associate the comment with.
     * @param author        the user who has created this comment.
     * @param updateAuthor  the user who has updated this comment last
     * @param body          the text of the comment.
     * @param groupLevel    is the group name to limit comment visibility to, this must be a valid group name.
     * @param roleLevelId   is the id of the the {@link ProjectRole} to limit comment visibility to, this must reference a
     *                      valid project role.
     * @param created       is the date that will be used as the comments creation date.
     * @param updated       is the date that will be used as the comments updated date.
     * @param dispatchEvent if true then an event of type {@link com.atlassian.jira.event.type.EventType#ISSUE_COMMENTED_ID}
     *                      will be dispatched and any notifications listening for that event will be triggered.
     *                      If false no event will be dispatched.
     * @return the object representation of the newly created comment.
     */
    public Comment create(Issue issue, String author, String updateAuthor, String body, String groupLevel, Long roleLevelId, Date created, Date updated, boolean dispatchEvent);

    /**
     * Creates a comment and associates it with the given issue. Using this method the comment will be created
     * with a createdDate of the specified date. This method should be used if you are trying to preserve existing
     * information and it is important to retain the original created date. If you have provided a groupLevel then the
     * comment visibility will be restricted to the provided group, it is assumed that validation to insure that the
     * group actually exists has been performed outside of this method. If you have provided a roleLevelId then the
     * comment visibility will be restricted to the provided role, it is assumed that validation to insure that the
     * role actually exists has been performed outside of this method.
     * <p/>
     * <strong>NOTE:</strong> A comment should not have both a group level and role level visibility restriction. This
     * method will not stop this, but it does not semantically make sense.
     *
     * @param issue                the issue to associate the comment with.
     * @param author               the user who has created this comment.
     * @param updateAuthor         the user who has updated this comment last
     * @param body                 the text of the comment.
     * @param groupLevel           is the group name to limit comment visibility to, this must be a valid group name.
     * @param roleLevelId          is the id of the the {@link ProjectRole} to limit comment visibility to, this must reference a
     *                             valid project role.
     * @param created              is the date that will be used as the comments creation date.
     * @param updated              is the date that will be used as the comments updated date.
     * @param dispatchEvent        if true then an event of type {@link com.atlassian.jira.event.type.EventType#ISSUE_COMMENTED_ID}
     *                             will be dispatched and any notifications listening for that event will be triggered.
     *                             If false no event will be dispatched.
     * @param tweakIssueUpdateDate if true the issues 'updated' date will be set to now.
     * @return the object representation of the newly created comment.
     */
    public Comment create(Issue issue, String author, String updateAuthor, String body, String groupLevel, Long roleLevelId, Date created, Date updated, boolean dispatchEvent, boolean tweakIssueUpdateDate);


    /**
     * This is a convenience method to allow us to easily get a ProjectRole. This is being used by the CommentImpl
     * to get a {@link ProjectRole}.
     * <p/>
     * <strong>NOTE:</strong> If you are trying to retrieve a {@link ProjectRole} then you should be using the
     * {@link com.atlassian.jira.security.roles.ProjectRoleManager}.
     *
     * @param projectRoleId the id to the {@link ProjectRole} object you would like returned.
     * @return will return a ProjectRole based on the passed in projectRoleId.
     */
    public ProjectRole getProjectRole(Long projectRoleId);

    /**
     * This is a convenience method that can be used to convert a GenericValue representation of a comment to a comment
     * object.
     *
     * @param commentGV is the GenericValue representation of a comment.
     * @return Comment object.
     */
    public Comment convertToComment(GenericValue commentGV);

    /**
     * Will return a comment for the passed in commentId.
     *
     * @param commentId the id representing the {@link Comment} you would like to retrieve.
     * @return a {@link Comment} or null  (if the user cannot browse the comment).
     * @throws IllegalArgumentException if its id is null
     */
    public Comment getCommentById(Long commentId);

    /**
     * Retrieves comment by given id and returns it as mutable object.
     *
     * @param commentId comment id
     * @return mutable comment or null if comment with given id not found
     * @throws IllegalArgumentException if its id is null
     */
    public MutableComment getMutableComment(Long commentId);

    /**
     * Persists the updated comment.
     *
     * @param comment       to update
     * @param dispatchEvent if true then an event of type {@link com.atlassian.jira.event.type.EventType#ISSUE_COMMENT_EDITED_ID}
     *                      will be dispatched and any notifications listening for that event will be triggered.
     *                      If false no event will be dispatched.
     * @throws IllegalArgumentException if comment passed in is null or its id is null
     */
    public void update(Comment comment, boolean dispatchEvent);

    /**
     * Updates {@link Comment}'s such that comments that have a visibility
     * restriction of the provided groupName will be changed to have a visibility restriction of the
     * provided swapGroup.
     *
     * Note: There is no validation performed by this method to determine if the provided swapGroup is a valid
     * group with JIRA. This validation must be done by the caller.
     *
     * @param groupName identifies the group the comments are restricted by, this must not be null.
     * @param swapGroup identifies the group the comments will be changed to be restricted by, this must not be null.
     * @return tbe number of comments affected by the update.
     *
     * @since v3.12
     */
    public int swapCommentGroupRestriction(String groupName, String swapGroup);

    /**
     * Returns the count of all {@link Comment}'s that have their visibility restricted by the named group.
     *
     * @param groupName identifies the group the comments are restricted by, this must not be null.
     * @return a count of {@link Comment}'s who's visibility are restricted by the passed in group name.
     *
     * @since v3.12
     */
    public long getCountForCommentsRestrictedByGroup(String groupName);

    /**
     * Deletes the specified comment.
     *
     * @param comment to delete
     * @return a change item that represents the change of deleting the comment
     * @throws IllegalArgumentException if comment passed in is null or its id is null
     */
    public ChangeItemBean delete(Comment comment);

    /**
     * Returns true if the given user is the author of the given comment.
     * <p>
     * If the given Comment has a null author, this represents that it was created anonymously, and no-one is the author (returns false).
     * If the given User is null, then this represents that the current user is anonymous, and the method will always return false.
     * </p>
     *
     * @param user The User. Can be null to represent current user is not logged in (anonymous).
     * @param comment The Comment. Cannot be null.
     * @return true if the given user is the author of the given comment.
     *
     * @since v4.3
     */
    boolean isUserCommentAuthor(User user, Comment comment);
}