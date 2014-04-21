package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.MutableComment;
import com.atlassian.jira.issue.fields.rest.json.beans.CommentJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.CommentsWithPaginationJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.rest.json.beans.VisibilityJsonBean;
import com.atlassian.jira.rest.NotFoundWebException;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.v2.issue.context.ContextI18n;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.util.SimpleErrorCollection;

import javax.ws.rs.core.Response;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * @since 4.2
 */
public class CommentResource
{
    private CommentService commentService;
    private JiraAuthenticationContext authContext;
    private ContextI18n i18n;
    private ProjectRoleManager projectRoleManager;
    private JiraBaseUrls jiraBaseUrls;
    private IssueFinder issueFinder;

    @SuppressWarnings ({ "UnusedDeclaration" })
    private CommentResource(IssueFinder issueFinder)
    {
        // this constructor used by tooling
        this.issueFinder = issueFinder;
    }

    @SuppressWarnings ({ "UnusedDeclaration" })
    public CommentResource(final CommentService commentService, final JiraAuthenticationContext authContext, ContextI18n i18n, final ProjectRoleManager projectRoleManager, JiraBaseUrls jiraBaseUrls, IssueFinder issueFinder)
    {
        this.authContext = authContext;
        this.commentService = commentService;
        this.i18n = i18n;
        this.projectRoleManager = projectRoleManager;
        this.jiraBaseUrls = jiraBaseUrls;
        this.issueFinder = issueFinder;
    }

    /**
     * Returns all comments for an issue.
     *
     * @param issueIdOrKey to get comments for
     * @return all comments for the issue
     *
     */
    public Response getComments(final String issueIdOrKey)
    {
        final Issue issue = issueFinder.getIssueObject(issueIdOrKey);
        if (issue == null)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("rest.comment.error.issue.invalid", issueIdOrKey)));
        }

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        List<Comment> commentsForUser = commentService.getCommentsForUser(authContext.getLoggedInUser(), issue, errorCollection);
        if (!errorCollection.hasAnyErrors())
        {
            Collection<CommentJsonBean> commentJsonBeans = CommentJsonBean.shortBeans(commentsForUser, jiraBaseUrls, projectRoleManager);
            CommentsWithPaginationJsonBean commentsBean = new CommentsWithPaginationJsonBean();
            commentsBean.setComments(commentJsonBeans);
            commentsBean.setStartAt(0);
            commentsBean.setMaxResults(commentJsonBeans.size());
            commentsBean.setTotal(commentJsonBeans.size());

            return Response.ok(commentsBean).cacheControl(never()).build();
        }
        else
        {
            throw new NotFoundWebException(ErrorCollection.of(errorCollection));
        }
    }

    /**
     * Returns a single issue comment.
     *
     * @param commentId the ID of the comment to request
     * @param issueIdOrKey of the issue the comment belongs to
     * @return a Response containing a CommentJsonBean
     */
    public Response getComment(final String issueIdOrKey, final String commentId)
    {
        final Issue issue = issueFinder.getIssueObject(issueIdOrKey);
        if (issue == null)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("rest.comment.error.issue.invalid", issueIdOrKey)));
        }

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        try
        {
            final Comment comment = commentService.getCommentById(authContext.getLoggedInUser(), Long.parseLong(commentId), errorCollection);
            if (errorCollection.hasAnyErrors())
            {
                return Response.status(Response.Status.NOT_FOUND).entity(ErrorCollection.of(errorCollection)).cacheControl(never()).build();
            }

            if (!issue.equals(comment.getIssue()))
            {
                return Response.status(Response.Status.NOT_FOUND).entity(ErrorCollection.of(i18n.getText("rest.comment.error.invalidIssueForComment", issue.getKey()))).cacheControl(never()).build();
            }
            
            return Response.ok(CommentJsonBean.shortBean(comment, jiraBaseUrls, projectRoleManager)).cacheControl(never()).build();
        }
        catch (NumberFormatException e)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("comment.service.error.no.comment.for.id", commentId)));
        }
    }

    /**
     * Updates an existing comment using its JSON representation.
     *
     * @param issueIdOrKey a string containing the issue id or key the comment belongs to
     * @param commentId id of the comment to be updated
     * @param request json body of request converted to a {@link com.atlassian.jira.issue.fields.rest.json.beans.CommentJsonBean}
     * @return updated Comment
     */
    public Response updateComment(final String issueIdOrKey, final String commentId, CommentJsonBean request)
    {

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        if (request.getId() != null && !request.getId().equals(commentId))
        {
            throw new RESTException(Response.Status.BAD_REQUEST, i18n.getText("rest.comment.error.id.mismatch"));
        }

        final Issue issue = issueFinder.getIssueObject(issueIdOrKey);
        if (issue == null)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("rest.comment.error.issue.invalid", issueIdOrKey)));
        }

        try
        {
            MutableComment comment = commentService.getMutableComment(authContext.getLoggedInUser(), Long.parseLong(commentId), errorCollection);
            if (errorCollection.hasAnyErrors())
            {
                throw new NotFoundWebException(ErrorCollection.of(errorCollection));
            }

            if (comment == null)
            {
                throw new NotFoundWebException(ErrorCollection.of(i18n.getText("comment.service.error.no.comment.for.id", commentId)));
            }

            if (!issue.equals(comment.getIssue()))
            {
                throw new NotFoundWebException(ErrorCollection.of(i18n.getText("rest.comment.error.invalidIssueForComment", issue.getKey())));
            }

            // set the variables against the existing comment, in case the user didn't wany to update everything
            if (request.isBodySet())
            {
                comment.setBody(request.getBody());
            }
            if (request.isVisibilitySet())
            {
                CommentVisibility visibilityParams = getCommentVisibilityParams(request);
                comment.setGroupLevel(visibilityParams.group);
                comment.setRoleLevelId(visibilityParams.roleId);
            }

            commentService.validateCommentUpdate(authContext.getLoggedInUser(), comment.getId(), comment.getBody(), comment.getGroupLevel(), comment.getRoleLevelId(), errorCollection);
            if (!errorCollection.hasAnyErrors())
            {
                commentService.update(authContext.getLoggedInUser(), comment, true, errorCollection);
                if (!errorCollection.hasAnyErrors())
                {
                    CommentJsonBean bean = CommentJsonBean.shortBean(comment, jiraBaseUrls, projectRoleManager);
                    return Response.ok(bean).location(getUri(bean)).cacheControl(never()).build();
                }
            }

            return Response.status(Response.Status.BAD_REQUEST).entity(ErrorCollection.of(errorCollection)).cacheControl(never()).build();
        }
        catch (NumberFormatException e)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("comment.service.error.no.comment.for.id", commentId)));
        }
    }

    /**
     * Deletes an existing comment .
     *
     * @param issueIdOrKey a string containing the issue id or key the comment belongs to
     * @param commentId id of the comment to be deleted
     * @return No Content Response
     */
    public Response deleteComment(final String issueIdOrKey, final String commentId)
    {
        final Issue issue = issueFinder.getIssueObject(issueIdOrKey);
        if (issue == null)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("rest.comment.error.issue.invalid", issueIdOrKey)));
        }

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        try
        {
            final Comment comment = commentService.getCommentById(authContext.getLoggedInUser(), Long.parseLong(commentId), errorCollection);
            if (errorCollection.hasAnyErrors())
            {
                throw new NotFoundWebException(ErrorCollection.of(errorCollection));
            }

            if (!issue.equals(comment.getIssue()))
            {
                throw new NotFoundWebException(ErrorCollection.of(i18n.getText("rest.comment.error.invalidIssueForComment", issue.getKey())));
            }

            final JiraServiceContextImpl jiraServiceContext = new JiraServiceContextImpl(authContext.getLoggedInUser(), errorCollection);
            commentService.delete(jiraServiceContext, comment, true);
            if (errorCollection.hasAnyErrors())
            {
                return Response.status(Response.Status.BAD_REQUEST).entity(ErrorCollection.of(errorCollection)).cacheControl(never()).build();
            }

            return Response.noContent().cacheControl(never()).build();
        }
        catch (NumberFormatException e)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("comment.service.error.no.comment.for.id", commentId)));
        }
    }

    /**
     * Adds a new comment to an issue.
     *
     * @param issueIdOrKey a string containing the issue id or key the comment will be added to
     * @param request json body of request converted to a {@link com.atlassian.jira.issue.fields.rest.json.beans.CommentJsonBean}
     * @return the added comment
     */
    public Response addComment(final String issueIdOrKey, CommentJsonBean request)
    {
        final Issue issue = issueFinder.getIssueObject(issueIdOrKey);
        if (issue == null)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("rest.comment.error.issue.invalid", issueIdOrKey)));
        }

        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();

        CommentVisibility commentVisibility = getCommentVisibilityParams(request);
        commentService.isValidAllCommentData(authContext.getLoggedInUser(), issue, request.getBody(), commentVisibility.group, commentVisibility.roleId != null ? commentVisibility.roleId.toString() : null, errorCollection);
        if (!errorCollection.hasAnyErrors())
        {
            Comment comment = commentService.create(authContext.getLoggedInUser(), issue, request.getBody(), commentVisibility.group, commentVisibility.roleId, true, errorCollection);
            if (!errorCollection.hasAnyErrors())
            {
                CommentJsonBean entity = CommentJsonBean.shortBean(comment, jiraBaseUrls, projectRoleManager);
                return Response.status(Response.Status.CREATED).location(getUri(entity)).entity(entity).cacheControl(never()).build();
            }
        }
        return Response.status(Response.Status.BAD_REQUEST).entity(ErrorCollection.of(errorCollection)).cacheControl(never()).build();
    }

    private CommentVisibility getCommentVisibilityParams(CommentJsonBean request)
    {
        if (request.getVisibility() != null)
        {
            final VisibilityJsonBean visibility = request.getVisibility();
            Long role;
            String group;
            if (visibility.type == VisibilityJsonBean.VisibilityType.role)
            {
                role = projectRoleManager.getProjectRole(visibility.value).getId();
                return new CommentVisibility(VisibilityJsonBean.VisibilityType.role, null, role);
            }
            else
            {
                group = visibility.value;
                return new CommentVisibility(VisibilityJsonBean.VisibilityType.group, group, null);
            }
        }
        else
        {
            return new CommentVisibility(null, null, null);
        }
    }

    private class CommentVisibility
    {
        VisibilityJsonBean.VisibilityType type;
        String group;
        Long roleId;

        private CommentVisibility(VisibilityJsonBean.VisibilityType type, String group, Long roleId)
        {
            this.type = type;
            this.group = group;
            this.roleId = roleId;
        }
    }

    private URI getUri(CommentJsonBean comment)
    {
        try
        {
           return new URI(comment.getSelf());
        }
        catch (URISyntaxException e)
        {
            return null;
        }
    }
}