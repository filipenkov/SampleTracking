package com.atlassian.jira.plugins.mail;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentImpl;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.comments.MutableComment;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.security.roles.ProjectRole;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.TreeMultimap;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.lf5.util.StreamUtils;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * TODO: Document this class / interface here
 *
 * @since v5.0
 */
public class MockCommentManager implements CommentManager
{
    private final Multimap<Long, Comment> issueComments = LinkedHashMultimap.create();
    private final MockIssueManager issueManager;

    public MockCommentManager(MockIssueManager issueManager) {
        this.issueManager = issueManager;
    }

    @Override
    public List<Comment> getCommentsForUser(Issue issue, final User user)
    {
        return ImmutableList.copyOf(Iterables.filter(issueComments.get(issue.getId()), new Predicate<Comment>()
        {
            @Override
            public boolean apply(Comment input)
            {
                return StringUtils.equals(input.getAuthor(), user.getName());
            }
        }));

    }

    @Override
    public List<Comment> getComments(Issue issue)
    {
        return ImmutableList.copyOf(issueComments.get(issue.getId()));
    }

    @Override
    public Comment create(Issue issue, String author, String body, boolean dispatchEvent)
    {
        final GenericValue genericValue = new MockGenericValue(issueManager.getIssue(issue.getId()));
        genericValue.set(IssueFieldConstants.UPDATED, new Timestamp(111, 10, 14, 13, 13, 13, 0));
        issueManager.addIssue(genericValue);

        final CommentImpl comment = new CommentImpl(this, author, null, body, null, null, new Date(), new Date(), issue);
        issueComments.put(issue.getId(), comment);
        return comment;
    }

    @Override
    public Comment create(Issue issue, String author, String body, String groupLevel, Long roleLevelId, boolean dispatchEvent)
    {
        return create(issue, author, body, dispatchEvent);
    }

    @Override
    public Comment create(Issue issue, String author, String body, String groupLevel, Long roleLevelId, Date created, boolean dispatchEvent)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Comment create(Issue issue, String author, String updateAuthor, String body, String groupLevel, Long roleLevelId, Date created, Date updated, boolean dispatchEvent)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Comment create(Issue issue, String author, String updateAuthor, String body, String groupLevel, Long roleLevelId, Date created, Date updated, boolean dispatchEvent, boolean tweakIssueUpdateDate)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ProjectRole getProjectRole(Long projectRoleId)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Comment convertToComment(GenericValue commentGV)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Comment getCommentById(Long commentId)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public MutableComment getMutableComment(Long commentId)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void update(Comment comment, boolean dispatchEvent)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int swapCommentGroupRestriction(String groupName, String swapGroup)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getCountForCommentsRestrictedByGroup(String groupName)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChangeItemBean delete(Comment comment)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isUserCommentAuthor(User user, Comment comment)
    {
        throw new UnsupportedOperationException();
    }
}
