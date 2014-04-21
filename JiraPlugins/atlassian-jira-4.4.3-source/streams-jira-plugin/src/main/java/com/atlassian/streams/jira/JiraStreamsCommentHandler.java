package com.atlassian.streams.jira;

import java.net.URI;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.streams.api.StreamsException;
import com.atlassian.streams.api.common.Either;
import com.atlassian.streams.spi.StreamsCommentHandler;

import static com.atlassian.streams.spi.StreamsCommentHandler.PostReplyError.Type.DELETED_OR_PERMISSION_DENIED;
import static com.atlassian.streams.spi.StreamsCommentHandler.PostReplyError.Type.UNKNOWN_ERROR;
import static com.google.common.collect.Iterables.getOnlyElement;

public class JiraStreamsCommentHandler implements StreamsCommentHandler
{
    private final JiraAuthenticationContext authenticationContext;
    private final IssueManager issueManager;
    private final PermissionManager permissionManager;
    private final CommentService commentService;
    private final ApplicationProperties applicationProperties;

    public JiraStreamsCommentHandler(JiraAuthenticationContext authenticationContext, IssueManager issueManager,
        PermissionManager permissionManager, CommentService commentService, ApplicationProperties applicationProperties)
    {
        this.authenticationContext = authenticationContext;
        this.issueManager = issueManager;
        this.permissionManager = permissionManager;
        this.commentService = commentService;
        this.applicationProperties = applicationProperties;
    }

    public Either<PostReplyError, URI> postReply(Iterable<String> itemPath, String comment) throws StreamsException
    {
        User user = authenticationContext.getLoggedInUser();
        String issueKey = getOnlyElement(itemPath);
        Issue issue = issueManager.getIssueObject(issueKey);
        if (issue == null || !permissionManager.hasPermission(Permissions.COMMENT_ISSUE, issue, user))
        {
            return Either.left(new PostReplyError(DELETED_OR_PERMISSION_DENIED));
        }
        ErrorCollection errorCollection = new SimpleErrorCollection();
        Comment commentObject = commentService.create(user, issue, comment, true, errorCollection);
        if (errorCollection.hasAnyErrors())
        {
            return Either.left(new PostReplyError(UNKNOWN_ERROR));
        }
        return Either.right(URI.create(
            applicationProperties.getBaseUrl() + "/browse/" + issueKey + "#action_" + commentObject.getId()));
    }
}
