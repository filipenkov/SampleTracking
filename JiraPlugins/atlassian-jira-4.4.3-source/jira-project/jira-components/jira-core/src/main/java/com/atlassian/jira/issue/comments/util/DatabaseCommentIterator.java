package com.atlassian.jira.issue.comments.util;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.ActionConstants;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.opensymphony.user.User;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * This iterator is not synchronized, and should not be accessed by multiple threads
 */
public abstract class DatabaseCommentIterator implements CommentIterator
{
    private final OfBizDelegator delegator;
    private final CommentManager commentManager;

    private final Collection<Long> issueIds;
    private final User user;
    private OfBizListIterator commentsIterator;
    private Comment nextComment;

    public DatabaseCommentIterator(final OfBizDelegator delegator, final CommentManager commentManager, final Collection<Long> issueIds, final User user)
    {
        this.delegator = delegator;
        this.commentManager = commentManager;
        this.issueIds = issueIds;
        this.user = user;
    }

    public boolean hasNext()
    {
        // As documented in org.ofbiz.core.entity.EntityListIterator.hasNext() the best way to find out
        // if there are any results left in the iterator is to iterate over it until null is returned
        // (i.e. not use hasNext() method)
        // The documentation mentions efficiency only - but the functionality is totally broken when using
        // hsqldb JDBC drivers (hasNext() always returns true).
        // So listen to the OfBiz folk and iterate until null is returned.
        populateNextCommentIfNull();
        return nextComment != null;
    }

    public Comment next()
    {
        return nextComment();
    }

    private void populateNextCommentIfNull()
    {
        if (nextComment == null)
        {
            pullNextComment();
        }
    }

    public void remove()
    {
        throw new UnsupportedOperationException("Cannot remove an issue from an Comment Iterator");
    }

    public Comment nextComment()
    {
        populateNextCommentIfNull();
        if (nextComment == null)
        {
            throw new NoSuchElementException();
        }

        final Comment comment = nextComment;
        nextComment = null;
        return comment;
    }

    public void close()
    {
        if (commentsIterator != null)
        {
            commentsIterator.close();
        }
    }

    public int size()
    {
        final EntityExpr commentExpr = new EntityExpr("type", EntityOperator.EQUALS, ActionConstants.TYPE_COMMENT);
        final EntityCondition issueIdsClause = new EntityExpr("issue", EntityOperator.IN, issueIds); // todo this could blow up with a too many clauses exception...
        final EntityCondition condition = new EntityExpr(commentExpr, EntityOperator.AND, issueIdsClause);

        final List<GenericValue> commentCount = delegator.findByCondition("ActionCount", condition, EasyList.build("count"), Collections.EMPTY_LIST);
        if ((commentCount != null) && (commentCount.size() == 1))
        {
            final GenericValue commentCountGV = commentCount.get(0);
            return (int) commentCountGV.getLong("count").longValue();
        }
        else
        {
            throw new DataAccessException("Unable to access the count for the Action table");
        }
    }

    public OfBizListIterator getCommentsIterator()
    {
        //todo - handle after being closed
        if (commentsIterator == null)
        {
            final EntityExpr commentExpr = new EntityExpr("type", EntityOperator.EQUALS, ActionConstants.TYPE_COMMENT);
            final EntityCondition issueIdsClause = new EntityExpr("issue", EntityOperator.IN, issueIds); // todo this could blow up with a too many clauses exception...
            final EntityCondition condition = new EntityExpr(commentExpr, EntityOperator.AND, issueIdsClause);
            commentsIterator = delegator.findListIteratorByCondition("Action", condition, null, null, EasyList.build("created DESC"), null);//todo - limit to certain issues
        }

        return commentsIterator;
    }

    private void pullNextComment()
    {

        do
        {
            final GenericValue commentGV = getCommentsIterator().next();
            if (commentGV == null)
            {
                return;
            }
            nextComment = commentManager.convertToComment(commentGV);
        }
        while (!hasReadPermissionForAction(nextComment, nextComment.getIssue(), user));
    }

    protected abstract boolean hasReadPermissionForAction(Comment comment, Issue issue, User user);

}
