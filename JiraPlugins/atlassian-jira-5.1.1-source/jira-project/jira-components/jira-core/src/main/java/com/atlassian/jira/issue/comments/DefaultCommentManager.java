package com.atlassian.jira.issue.comments;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.jira.event.issue.IssueEventDispatcher;
import com.atlassian.jira.event.issue.IssueEventSource;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.ActionConstants;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.IssueRelationConstants;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.TextFieldCharacterLengthValidator;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.jira.util.JiraDateUtils;
import com.atlassian.jira.util.ObjectUtils;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityFieldMap;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilDateTime;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultCommentManager implements CommentManager
{
    private final IssueManager issueManager;
    private final ProjectRoleManager projectRoleManager;
    private final CommentPermissionManager commentPermissionManager;
    private final OfBizDelegator delegator;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final TextFieldCharacterLengthValidator textFieldCharacterLengthValidator;
    private static final String COMMENT_ID = "id";
    public static final String COMMENT_ENTITY = "Action";

    public DefaultCommentManager(IssueManager issueManager, ProjectRoleManager projectRoleManager,
            CommentPermissionManager commentPermissionManager, OfBizDelegator delegator, JiraAuthenticationContext jiraAuthenticationContext,
            TextFieldCharacterLengthValidator textFieldCharacterLengthValidator)
    {
        this.issueManager = issueManager;
        this.projectRoleManager = projectRoleManager;
        this.commentPermissionManager = commentPermissionManager;
        this.delegator = delegator;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.textFieldCharacterLengthValidator = textFieldCharacterLengthValidator;
    }

    public ProjectRole getProjectRole(Long projectRoleId)
    {
        return projectRoleManager.getProjectRole(projectRoleId);
    }

    public Comment convertToComment(GenericValue gv)
    {
        return convertToComment(gv, issueManager.getIssueObject(gv.getLong("issue")));
    }

    public Comment getCommentById(Long commentId)
    {
        return getMutableComment(commentId);
    }

    public MutableComment getMutableComment(Long commentId)
    {
        if (commentId == null)
        {
            throw new IllegalArgumentException("The comment id must not be null.");
        }

        GenericValue gv = delegator.findById(COMMENT_ENTITY, commentId);
        if (gv != null)
        {
            return convertToComment(gv, issueManager.getIssueObject(gv.getLong("issue")));
        }
        return null;
    }

    public List<Comment> getCommentsForUser(Issue issue, User user)
    {
        List<Comment> visibleComments = new ArrayList<Comment>();

        try
        {
            // get a List<GenericValue> of comments
            List<GenericValue> allComments = issueManager.getEntitiesByIssueObject(IssueRelationConstants.COMMENTS, issue);

            for (final GenericValue commentGV : allComments)
            {
                Comment comment = convertToComment(commentGV, issue);

                if (commentPermissionManager.hasBrowsePermission(user, comment))
                {
                    visibleComments.add(comment);
                }
            }
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
        Collections.sort(visibleComments, CommentComparator.COMPARATOR);
        return visibleComments;
    }

    public List<Comment> getComments(Issue issue)
    {
        List<Comment> comments = new ArrayList<Comment>();

        try
        {
            // get a List<GenericValue> of comments
            List allComments = issueManager.getEntitiesByIssueObject(IssueRelationConstants.COMMENTS, issue);

            for (final Object allComment : allComments)
            {
                Comment comment = convertToComment((GenericValue) allComment, issue);
                comments.add(comment);
            }
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }

        Collections.sort(comments, CommentComparator.COMPARATOR);
        return comments;
    }

    public Comment create(Issue issue, String author, String body, boolean dispatchEvent)
    {
        return create(issue, author, body, null, null, dispatchEvent);
    }

    public Comment create(Issue issue, String author, String body, String groupLevel, Long roleLevelId, boolean dispatchEvent)
            throws DataAccessException
    {
        return create(issue, author, body, groupLevel, roleLevelId, new Date(), dispatchEvent);
    }

    public Comment create(Issue issue, String author, String body, String groupLevel, Long roleLevelId, Date created, boolean dispatchEvent)
            throws DataAccessException
    {
        return create(issue, author, author, body, groupLevel, roleLevelId, created, created, dispatchEvent);
    }

    public Comment create(Issue issue, String author, String updateAuthor, String body, String groupLevel, Long roleLevelId, Date created, Date updated, boolean dispatchEvent)
            throws DataAccessException
    {
        return create(issue, author, updateAuthor, body, groupLevel, roleLevelId, created, updated, dispatchEvent, true);
    }

    /**
     * @see com.atlassian.jira.issue.comments.CommentManager#create(com.atlassian.jira.issue.Issue,String,String,String,String,Long,java.util.Date,java.util.Date,boolean,boolean)
     */
    public Comment create(Issue issue, String author, String updateAuthor, String body, String groupLevel, Long roleLevelId, Date created, Date updated, boolean dispatchEvent, boolean tweakIssueUpdateDate)
            throws DataAccessException
    {
        if (textFieldCharacterLengthValidator.isTextTooLong(body))
        {
            final long maximumNumberOfCharacters = textFieldCharacterLengthValidator.getMaximumNumberOfCharacters();
            String errorMessage = getText("field.error.text.toolong", String.valueOf(maximumNumberOfCharacters));
            throw new IllegalArgumentException(errorMessage);
        }

        // create new instance of comment
        CommentImpl comment = new CommentImpl(this, author, updateAuthor, body, groupLevel, roleLevelId, created, updated, issue);

        // create persistable generic value
        Map<String, Object> fields = new HashMap<String, Object>();
        fields.put("issue", issue.getId());
        fields.put("type", ActionConstants.TYPE_COMMENT);
        fields.put("author", comment.getAuthor());
        fields.put("updateauthor", comment.getUpdateAuthor());
        fields.put("body", comment.getBody());
        fields.put("level", comment.getGroupLevel());
        fields.put("rolelevel", comment.getRoleLevelId());
        fields.put("created", new Timestamp(comment.getCreated().getTime()));
        fields.put("updated", new Timestamp(comment.getUpdated().getTime()));

        GenericValue commentGV = EntityUtils.createValue(COMMENT_ENTITY, fields);
        // set the ID on comment object
        comment.setId(commentGV.getLong(COMMENT_ID));

        // Update the issue object if require
        if (tweakIssueUpdateDate)
        {
            IssueFactory issueFactory = ComponentManager.getComponentInstanceOfType(IssueFactory.class);
            MutableIssue mutableIssue = issueFactory.getIssue(issue.getGenericValue());
            //JRA-15723: Use the comments updated time for the updated time of the issue.  This allows users to
            // import old comments (via Jelly for example) without setting the updated time on the issue to now, but to the date
            // of the old comments.
            mutableIssue.setUpdated(new Timestamp(comment.getUpdated().getTime()));
            issue.store();
        }

        // Dispatch an event if required
        if (dispatchEvent)
        {
            dispatchEvent(EventType.ISSUE_COMMENTED_ID, comment, EasyMap.build("eventsource", IssueEventSource.ACTION));
        }
        return comment;
    }

    public void update(Comment comment, boolean dispatchEvent)
    {
        if (comment == null)
        {
            throw new IllegalArgumentException("Comment must not be null");
        }
        if (comment.getId() == null)
        {
            throw new IllegalArgumentException("Comment ID must not be null");
        }

        // create persistable generic value
        GenericValue commentGV;

        // We need an in-memory copy of the old comment so we can pass it through in the fired event and to make sure
        // that some fields have changed.
        Comment originalComment = getCommentById(comment.getId());
        if (originalComment == null)
        {
            throw new IllegalArgumentException("Can not find a comment in the datastore with id: " + comment.getId());
        }

        // Make sure that either the comment body or visibility data has changed, otherwise do not update the datastore
        if (areCommentsEquivalent(originalComment, comment))
        {
            return;
        }

        try
        {
            commentGV = delegator.findById(COMMENT_ENTITY, comment.getId());
            populateGenericValueFromComment(comment, commentGV);
            commentGV.store();
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }

        // Update the issue object
        IssueFactory issueFactory = ComponentManager.getComponentInstanceOfType(IssueFactory.class);
        GenericValue issueGV = comment.getIssue().getGenericValue();
        MutableIssue mutableIssue = issueFactory.getIssue(issueGV);
        mutableIssue.setUpdated(UtilDateTime.nowTimestamp());
        mutableIssue.store();

        // Dispatch an event if required
        if (dispatchEvent)
        {
            dispatchEvent(EventType.ISSUE_COMMENT_EDITED_ID, comment,
                    EasyMap.build("eventsource", IssueEventSource.ACTION, EVENT_ORIGINAL_COMMENT_PARAMETER, originalComment));
        }
    }

    public ChangeItemBean delete(Comment comment)
    {
        ChangeItemBean changeItemBean = constructChangeItemBeanForCommentDelete(comment);
        // TODO: move this into the Store (when it gets created)
        delegator.removeByAnd("Action", EasyMap.build("id", comment.getId(), "type", ActionConstants.TYPE_COMMENT));
        return changeItemBean;
    }

    public boolean isUserCommentAuthor(User user, Comment comment)
    {
        return commentPermissionManager.isUserCommentAuthor(user, comment);
    }

    public int swapCommentGroupRestriction(String groupName, String swapGroup)
    {
        if (groupName == null)
        {
            throw new IllegalArgumentException("You must provide a non null group name.");
        }

        if (swapGroup == null)
        {
            throw new IllegalArgumentException("You must provide a non null swap group name.");
        }

        return delegator.bulkUpdateByAnd("Action", EasyMap.build("level", swapGroup), EasyMap.build("level", groupName, "type", ActionConstants.TYPE_COMMENT));
    }

    public long getCountForCommentsRestrictedByGroup(String groupName)
    {
        if (groupName == null)
        {
            throw new IllegalArgumentException("You must provide a non null group name.");
        }

        EntityCondition condition = new EntityFieldMap(EasyMap.build("level", groupName, "type", ActionConstants.TYPE_COMMENT), EntityOperator.AND);
        List commentCount = delegator.findByCondition("ActionCount", condition, EasyList.build("count"), Collections.EMPTY_LIST);
        if (commentCount != null && commentCount.size() == 1)
        {
            GenericValue commentCountGV = (GenericValue) commentCount.get(0);
            return commentCountGV.getLong("count").longValue();
        }
        else
        {
            throw new DataAccessException("Unable to access the count for the Action table");
        }
    }

    /**
     * Constructs an issue update bean for a comment delete. The comment text will be masked if the security levels are
     * set
     */
    ChangeItemBean constructChangeItemBeanForCommentDelete(Comment comment)
    {
        // Check the level of the comment, if the level is not null we need to override the comment
        // This is necessary as part of JRA-9394 to remove comment text from the change history for security (or lack thereof)
        String message;
        final String groupLevel = comment.getGroupLevel();
        final String roleLevel = (comment.getRoleLevel() == null) ? null : comment.getRoleLevel().getName();
        final String actionLevel = groupLevel == null ? roleLevel : groupLevel;
        if (actionLevel != null)
        {
            message = getText("comment.manager.deleted.comment.with.restricted.level", actionLevel);
        }
        else
        {
            message = comment.getBody();
        }

        return new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Comment", message, null);
    }

    // TODO: the event generation should live in the service
    // This is mostly here for testing purposes so we do not really need to dispatch the event to know it was called correctly
    void dispatchEvent(Long eventTypeId, Comment comment, Map parameters)
    {
        User authorUser = UserUtils.getUser(comment.getUpdateAuthor());
        IssueEventDispatcher.dispatchEvent(eventTypeId, comment.getIssue(), authorUser, comment, null, null, parameters);
    }

    private String getText(String key, String param)
    {
        return jiraAuthenticationContext.getI18nHelper().getText(key, param);
    }

    private void populateGenericValueFromComment(Comment updatedComment, GenericValue commentGV)
    {
        commentGV.setString("updateauthor", updatedComment.getUpdateAuthor());
        commentGV.setString("body", updatedComment.getBody());
        commentGV.setString("level", updatedComment.getGroupLevel());
        commentGV.set("rolelevel", updatedComment.getRoleLevelId());
        commentGV.set("updated", JiraDateUtils.copyOrCreateTimestampNullsafe(updatedComment.getUpdated()));
    }

    private MutableComment convertToComment(GenericValue gv, Issue issue)
    {
        Timestamp createdTS = gv.getTimestamp("created");
        Timestamp updatedTS = gv.getTimestamp("updated");
        CommentImpl comment = new CommentImpl(this,
                gv.getString("author"),
                gv.getString("updateauthor"),
                gv.getString("body"),
                gv.getString("level"),
                gv.getLong("rolelevel"),
                JiraDateUtils.copyDateNullsafe(createdTS),
                JiraDateUtils.copyDateNullsafe(updatedTS),
                issue);

        comment.setId(gv.getLong(COMMENT_ID));
        return comment;
    }

    /**
     * Returns true if both comments have equal bodies, group levels and role level ids, false otherwise.
     *
     * @param comment1 comment to compare
     * @param comment2 comment to compare
     * @return true if both comments have equal bodies, group levels and role level ids, false otherwise
     */
    private boolean areCommentsEquivalent(Comment comment1, Comment comment2)
    {
        return ObjectUtils.equalsNullSafe(comment1.getBody(), comment2.getBody())
                && ObjectUtils.equalsNullSafe(comment1.getGroupLevel(), comment2.getGroupLevel())
                && ObjectUtils.equalsNullSafe(comment1.getRoleLevelId(), comment2.getRoleLevelId());
    }

}
