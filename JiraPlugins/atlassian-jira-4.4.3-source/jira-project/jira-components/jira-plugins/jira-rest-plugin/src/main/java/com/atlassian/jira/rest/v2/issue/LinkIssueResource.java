package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.rest.NotAuthorisedWebException;
import com.atlassian.jira.rest.NotFoundWebException;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.v2.issue.context.ContextI18n;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import org.apache.log4j.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * The Link Issue Resource provides functionality to manage issue links.
 *
 * @since v4.3
 */
@Path ("issueLink")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class LinkIssueResource
{
    private final ApplicationProperties applicationProperties;
    private ContextI18n i18n;
    private IssueLinkTypeManager issueLinkTypeManager;
    private IssueService issueService;
    private JiraAuthenticationContext authContext;
    private PermissionManager permissionManager;
    private IssueLinkManager issueLinkManager;
    private CommentService commentService;
    private ProjectRoleManager projectRoleManager;
    private static final Logger LOG = Logger.getLogger(LinkIssueResource.class);

    public LinkIssueResource(ApplicationProperties applicationProperties, ContextI18n i18n, IssueLinkTypeManager issueLinkTypeManager, IssueService issueService, JiraAuthenticationContext authContext, PermissionManager permissionManager, IssueLinkManager issueLinkManager, CommentService commentService, ProjectRoleManager projectRoleManager)
    {
        this.applicationProperties = applicationProperties;
        this.i18n = i18n;
        this.issueLinkTypeManager = issueLinkTypeManager;
        this.issueService = issueService;
        this.authContext = authContext;
        this.permissionManager = permissionManager;
        this.issueLinkManager = issueLinkManager;
        this.commentService = commentService;
        this.projectRoleManager = projectRoleManager;
    }

    /**
     * Creates an issue link between two issues.
     * The user requires the link issue permission for the issue which will be linked to another issue.
     * The specified link type in the request is used to create the link and will create a link from the first issue
     * to the second issue using the outward description. It also create a link from the second issue to the first issue using the
     * inward description of the issue link type.
     * It will add the supplied comment to the first issue. The comment can have a restriction who can view it.
     * If group is specified, only users of this group can view this comment, if roleLevel is specified only users who have the specified role can view this comment.
     * The user who creates the issue link needs to belong to the specified group or have the specified role.
     *
     * @param linkIssueRequestBean contains all information about the link relationship. Which issues to link, which issue link type to use and
     *        and an optional comment that will be added to the first issue.
     *
     * @return a response indicating if the issue link was created successfully.
     *
     *
     * @request.representation.example
     *      {@link LinkIssueRequestBean#DOC_EXAMPLE}
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      if the issue link was created successfully.
     *
     * @response.representation.400.doc
     *      if it can't create the supplied comment.
     *      The response will contain an error message indicating why it failed to create the comment.
     *      No issue link will be created if it failed to create the comment.
     *
     * @response.representation.404.doc
     *      If issue linking is disabled or
     *      it failed to find one of the issues (issue might exist, but it is not visible for this user) or
     *      it failed to find the specified issue link type.
     *
     * @response.representation.401.doc
     *      if the user does not have the link issue permission for the issue, which will be linked to another issue.
     *
     * @response.representation.500.doc
     *      if an error occurred when creating the issue link or the comment.
     */
    @POST
    public Response linkIssues(final LinkIssueRequestBean linkIssueRequestBean)
    {
        if (!applicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING))
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("admin.issuelinking.status", i18n.getText("admin.common.words.disabled"))));
        }

        final IssueService.IssueResult fromIssueResult = issueService.getIssue(authContext.getLoggedInUser(), linkIssueRequestBean.getFromIssueKey());
        if (!fromIssueResult.isValid())
        {
            throw new NotFoundWebException(ErrorCollection.of(fromIssueResult.getErrorCollection()));
        }
        final MutableIssue fromIssue = fromIssueResult.getIssue();

        final IssueService.IssueResult toIssueResult = issueService.getIssue(authContext.getLoggedInUser(), linkIssueRequestBean.getToIssueKey());
        if (!toIssueResult.isValid())
        {
            throw new NotFoundWebException(ErrorCollection.of(toIssueResult.getErrorCollection()));
        }
        final MutableIssue toIssue = toIssueResult.getIssue();

        if (!permissionManager.hasPermission(Permissions.LINK_ISSUE, fromIssue, authContext.getLoggedInUser()))
        {
            throw new NotAuthorisedWebException(ErrorCollection.of(i18n.getText("rest.issue.link.error.link.no.link.permission", fromIssue.getKey())));
        }

        final IssueLinkType linkType;
        final Collection<IssueLinkType> issueLinkTypesByName = issueLinkTypeManager.getIssueLinkTypesByName(linkIssueRequestBean.getLinkType());
        if (issueLinkTypesByName.isEmpty())
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("rest.issue.link.type.not.found", String.valueOf(linkIssueRequestBean.getLinkType()))));
        }
        if (issueLinkTypesByName.size() > 1)
        {
            LOG.warn("Found " + issueLinkTypesByName.size() + " issue link types with name '" + linkIssueRequestBean.getLinkType()  + "'! Using first one returned.");
        }
        linkType = issueLinkTypesByName.iterator().next();

        final CommentBean commentBean = linkIssueRequestBean.getComment();
        if (commentBean != null)
        {
            VisibilityBean visibility = commentBean.getVisibility();
            if (visibility != null)
            {
                validateVisibilityRole(visibility);
                validateVisibilityGroup(fromIssue, commentBean, visibility);
            }
            validateCommentBody(commentBean);
        }

        try
        {
            issueLinkManager.createIssueLink(fromIssue.getId(), toIssue.getId(), linkType.getId(), null, authContext.getLoggedInUser());
        }
        catch (CreateException e)
        {
             //Exception will be logged by the ExceptionInterceptor.
             throw new RESTException(Response.Status.INTERNAL_SERVER_ERROR, e);
        }

        if (commentBean != null && commentBean.getBody() != null)
        {
            createComment(fromIssue, toIssue, commentBean);
        }
        return Response.status(Response.Status.OK).cacheControl(never()).build();
    }

    private void createComment(MutableIssue fromIssue, MutableIssue toIssue, CommentBean commentBean)
    {
        Comment comment;
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        if (commentBean.getVisibility() != null)
        {
            final VisibilityBean visibility = commentBean.getVisibility();
            Long role;
            String group;
            if (visibility.type == VisibilityType.ROLE)
            {
                role = projectRoleManager.getProjectRole(visibility.value).getId();
                group = null;
            }
            else
            {
                group = visibility.value;
                role = null;
            }
            comment = commentService.create(authContext.getLoggedInUser(), fromIssue, commentBean.getBody(), group, role, false, errorCollection);
        }
        else
        {
            comment = commentService.create(authContext.getLoggedInUser(), fromIssue, commentBean.getBody(), false, errorCollection);
        }
        if (errorCollection.hasAnyErrors() || comment == null)
        {
            errorCollection.addErrorMessage(i18n.getText("rest.issue.link.error.comment", toIssue.getKey()));
            throw new RESTException(Response.Status.BAD_REQUEST, ErrorCollection.of(errorCollection));
        }
    }

    private void validateCommentBody(CommentBean commentBean)
    {
        if (commentBean.getBody() != null)
        {
            SimpleErrorCollection errorCollection = new SimpleErrorCollection();
            commentService.isValidCommentBody(commentBean.getBody(), errorCollection);
            if (errorCollection.hasAnyErrors())
            {
                throw new RESTException(Response.Status.BAD_REQUEST, ErrorCollection.of(errorCollection));
            }
        }
    }

    private void validateVisibilityGroup(MutableIssue fromIssue, CommentBean commentBean, VisibilityBean visibility)
    {
        if (visibility.type == VisibilityType.GROUP)
        {
            if (visibility.value == null)
            {
                throw new NotFoundWebException(ErrorCollection.of(i18n.getText("rest.comment.visibility.no.value")));
            }

            SimpleErrorCollection errorCollection = new SimpleErrorCollection();
            commentService.isValidAllCommentData(authContext.getLoggedInUser(), fromIssue, commentBean.getBody(), visibility.value, null /* must be null if we specify a group */, errorCollection);
            if (errorCollection.hasAnyErrors())
            {
                throw new RESTException(Response.Status.BAD_REQUEST, ErrorCollection.of(errorCollection));
            }
        }
    }

    private void validateVisibilityRole(VisibilityBean visibility)
    {
        if (visibility.type == VisibilityType.ROLE)
        {
            if (visibility.value == null)
            {
                throw new NotFoundWebException(ErrorCollection.of(i18n.getText("rest.comment.visibility.no.value")));
            }

            final Long roleId = projectRoleManager.getProjectRole(visibility.value).getId();

            if (roleId == null)
            {
                throw new NotFoundWebException(ErrorCollection.of(i18n.getText("rest.issue.link.error.project.role.not.found", visibility.value)));
            }
        }
    }
}
