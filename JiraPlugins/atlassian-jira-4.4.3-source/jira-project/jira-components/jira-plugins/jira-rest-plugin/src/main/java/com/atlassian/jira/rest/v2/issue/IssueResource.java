package com.atlassian.jira.rest.v2.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.bc.issue.vote.VoteService;
import com.atlassian.jira.bc.issue.watcher.WatcherService;
import com.atlassian.jira.bc.issue.watcher.WatchingDisabledException;
import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.IssueInputParametersImpl;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.impl.StringCFType;
import com.atlassian.jira.issue.fields.AbstractTextSystemField;
import com.atlassian.jira.issue.fields.AffectedVersionsSystemField;
import com.atlassian.jira.issue.fields.AssigneeSystemField;
import com.atlassian.jira.issue.fields.AttachmentSystemField;
import com.atlassian.jira.issue.fields.CommentSystemField;
import com.atlassian.jira.issue.fields.ComponentsSystemField;
import com.atlassian.jira.issue.fields.CreatedSystemField;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.DueDateSystemField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.FixVersionsSystemField;
import com.atlassian.jira.issue.fields.IssueTypeSystemField;
import com.atlassian.jira.issue.fields.LabelsSystemField;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.PrioritySystemField;
import com.atlassian.jira.issue.fields.ProjectSystemField;
import com.atlassian.jira.issue.fields.ReporterSystemField;
import com.atlassian.jira.issue.fields.ResolutionDateSystemField;
import com.atlassian.jira.issue.fields.ResolutionSystemField;
import com.atlassian.jira.issue.fields.SecurityLevelSystemField;
import com.atlassian.jira.issue.fields.StatusSystemField;
import com.atlassian.jira.issue.fields.TimeTrackingSystemField;
import com.atlassian.jira.issue.fields.UpdatedSystemField;
import com.atlassian.jira.issue.fields.UserField;
import com.atlassian.jira.issue.fields.VotesSystemField;
import com.atlassian.jira.issue.fields.WorklogSystemField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.renderer.RenderableField;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItemImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderTab;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.LinkCollection;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.jql.resolver.ResolverManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.rest.NotAuthorisedWebException;
import com.atlassian.jira.rest.NotFoundWebException;
import com.atlassian.jira.rest.api.Dates;
import com.atlassian.jira.rest.api.field.FieldBean;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.v2.issue.builder.BeanBuilderFactory;
import com.atlassian.jira.rest.v2.issue.component.ComponentBean;
import com.atlassian.jira.rest.v2.issue.context.ContextI18n;
import com.atlassian.jira.rest.v2.issue.context.ContextUriInfo;
import com.atlassian.jira.rest.v2.issue.customfield.CustomFieldOps;
import com.atlassian.jira.rest.v2.issue.project.ProjectBeanFactory;
import com.atlassian.jira.rest.v2.issue.version.VersionBeanFactory;
import com.atlassian.jira.rest.v2.issue.watcher.WatcherOps;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.util.collect.Transformed;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowFunctionUtils;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.opensymphony.workflow.Workflow;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * @since v4.2
 */
@Path ("issue")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class IssueResource
{
    private static final Logger LOG = Logger.getLogger(IssueResource.class);

    private IssueService issueService;
    private IssueManager issueManager;
    private CommentService commentService;
    private UserManager userManager;
    private AttachmentManager attachmentManager;
    private PermissionManager permissionManager;
    private VersionBeanFactory versionBeanFactory;
    private ProjectBeanFactory projectBeanFactory;

    private FieldLayoutManager fieldLayoutManager;
    private JiraAuthenticationContext authContext;
    private WorkflowManager workflowManager;
    private FieldScreenRendererFactory fieldScreenRendererFactory;
    private FieldManager fieldManager;
    private ApplicationProperties applicationProperties;
    private IssueLinkManager issueLinkManager;
    private ResourceUriBuilder uriBuilder;
    private RendererManager rendererManager;
    private ProjectRoleManager projectRoleManager;
    private IssueSecurityLevelManager issueSecurityLevelManager;
    private WorklogService worklogService;
    private VoteService voteService;
    private CustomFieldOps customFieldOps;
    private ContextI18n i18n;

    private ResolverManager resolverManager;
    private WatcherOps watcherOps;
    private WatcherService watcherService;

    private BeanBuilderFactory beanBuilderFactory;
    private ContextUriInfo contextUriInfo;

    /**
     * This constructor needed by doclet.
     * @param issueManager the issue manager
     * @param permissionManager the permission manager
     */
    @SuppressWarnings ({ "UnusedDeclaration" })
    private IssueResource(final IssueManager issueManager, final PermissionManager permissionManager)
    {
        this.issueManager = issueManager;
        this.permissionManager = permissionManager;
    }

    public IssueResource(final IssueService issueService, final JiraAuthenticationContext authContext, final CommentService commentService, final FieldLayoutManager fieldLayoutManager, final WorkflowManager workflowManager, final FieldScreenRendererFactory fieldScreenRendererFactory, final UserManager userManager, final AttachmentManager attachmentManager, final FieldManager fieldManager, ApplicationProperties applicationProperties, IssueLinkManager issueLinkManager, ResourceUriBuilder uriBuilder, final RendererManager rendererManager, final ProjectRoleManager projectRoleManager, final IssueSecurityLevelManager issueSecurityLevelManager, final WorklogService worklogService, final ResolverManager resolverManager, final CustomFieldOps customFieldOps, final VoteService voteService, final ContextI18n i18n, WatcherOps watcherOps,
            WatcherService watcherService, BeanBuilderFactory beanBuilderFactory, ContextUriInfo contextUriInfo, final IssueManager issueManager, final PermissionManager permissionManager,
            final VersionBeanFactory versionBeanFactory, ProjectBeanFactory projectBeanFactory)
    {
        this.issueService = issueService;
        this.commentService = commentService;
        this.fieldLayoutManager = fieldLayoutManager;
        this.authContext = authContext;
        this.workflowManager = workflowManager;
        this.fieldScreenRendererFactory = fieldScreenRendererFactory;
        this.userManager = userManager;
        this.attachmentManager = attachmentManager;
        this.fieldManager = fieldManager;
        this.applicationProperties = applicationProperties;
        this.issueLinkManager = issueLinkManager;
        this.uriBuilder = uriBuilder;
        this.rendererManager = rendererManager;
        this.projectRoleManager = projectRoleManager;
        this.issueSecurityLevelManager = issueSecurityLevelManager;
        this.worklogService = worklogService;
        this.resolverManager = resolverManager;
        this.customFieldOps = customFieldOps;
        this.voteService = voteService;
        this.i18n = i18n;
        this.watcherOps = watcherOps;
        this.watcherService = watcherService;
        this.beanBuilderFactory = beanBuilderFactory;
        this.contextUriInfo = contextUriInfo;
        this.issueManager = issueManager;
        this.permissionManager = permissionManager;
        this.versionBeanFactory = versionBeanFactory;
        this.projectBeanFactory = projectBeanFactory;
    }

    /**
     * Get a list of the transitions possible for this issue by the current user, along with fields that are required and their types.
     * @param issueKey the issue whose transitions you want to view
     * @return a response containing a Map of TransitionFieldBeans for each transition possible by the current user.
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      Returns a full representation of the transitions possible for the specified issue and the fields required to perform the transition.
     *
     * @response.representation.200.example
     *      {@link TransitionsBean#DOC_MAP_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if the requested issue is not found or the user does not have permission to view it.
     */
    @GET
    @Path("/{issueKey}/transitions")
    public Response getTransitions(@PathParam ("issueKey") final String issueKey)
    {
        final Issue issue = getIssueObject(issueKey);

        final List<ActionDescriptor> actions = loadAvailableActions(authContext.getLoggedInUser(), issue);
        Collections.sort(actions, new Comparator<ActionDescriptor>(){
            public int compare(ActionDescriptor o1, ActionDescriptor o2)
            {
                return getSequenceFromAction(o1).compareTo(getSequenceFromAction(o2));
            }
        });

        Map<Integer, TransitionsBean> map = new HashMap<Integer, TransitionsBean>();

        for (ActionDescriptor action : actions)
        {
            final FieldScreenRenderer fieldScreenRenderer = fieldScreenRendererFactory.getFieldScreenRenderer(authContext.getUser(), issue, action);
            String status = getStatusFromStep(issue, action.getUnconditionalResult().getStep());
            final TransitionsBean transitions = new TransitionsBean(action.getName(), getRequiredFields(fieldScreenRenderer, issue), status);
            map.put(action.getId(), transitions);
        }

        return Response.ok(map).build();
    }

    /**
     * @param issue issue object to derive the worflow from
     * @param stepId the step id to get the status id for
     * @return the id of the status which stepId maps to in the associated workflow
     */
    private String getStatusFromStep(Issue issue, int stepId)
    {
        final WorkflowDescriptor wd = workflowManager.getWorkflow(issue).getDescriptor();
        return (String) wd.getStep(stepId).getMetaAttributes().get(JiraWorkflow.STEP_STATUS_KEY);
    }

    /**
     * Perform a transition on an issue.
     * The POST must contain a single JSON Object. It must have a "transition" string and a "fields" object.
     * A "comment" is optional. The comment can be either a simple string or an object with a "body" and "role" or "group".
     * @param issueKey the issue you want to transition
     * @param requestBody JSON representing the transition you want to perform along with the fields that will be updated.
     * @return only HTTP response codes
     * @throws JSONException the JSON you passed in is not valid
     *
     * @response.representation.404.doc
     *      The issue does not exist or the user does not have permission to view it
     *
     * @response.representation.400.doc
     *      The request body failed to validate.
     *
     * @response.representation.204.doc
     *      Nothing is returned on success.
     *
     * @request.representation.example
     * {
     *   "transition" : 2,
     *   "fields" : { "resolution" : "Duplicate" },
     *   "comment" : "We've already fixed this one."
     * }
     *
     * {
     *   "transition" : 3,
     *   "fields" : { "assignee" : "fred", "priority" : "Critical" },
     *   "comment" : { "body" : "This isn't fixed yet.", "group" : "jira-developers" }
     * }
     */
    @POST
    @Path("/{issueKey}/transitions")
    public Response doTransition(@PathParam("issueKey") final String issueKey, final String requestBody)
    {
        final Issue issue = getIssueObject(issueKey);

        // Now we need to begin parsing the request JSON that they sent in to us.
        final JSONObject json;
        try
        {
            json = new JSONObject(requestBody);
        }
        catch (JSONException e)
        {
            throw new RESTException(Response.Status.BAD_REQUEST, i18n.getText("rest.bad.json"), e.getLocalizedMessage());
        }

        // first check for a transition
        if (!json.has("transition"))
        {
            throw new RESTException(Response.Status.BAD_REQUEST, i18n.getText("rest.transition.error.no.transition"));
        }

        final int actionId;
        try
        {
            actionId = json.getInt("transition");
        }
        catch (JSONException e)
        {
            throw new RESTException(Response.Status.BAD_REQUEST, i18n.getText("rest.transition.error.id.not.integer"));
        }

        // then the fields
        final JSONObject fields = json.optJSONObject("fields");
        final IssueInputParameters issueInputParameters;
        try
        {
            issueInputParameters = (fields != null)
                    ? new IssueInputParametersImpl(jsonToIssueParams(fields))
                    : new IssueInputParametersImpl();
        }
        catch (JSONException e)
        {
            throw new RESTException(Response.Status.BAD_REQUEST, i18n.getText("rest.bad.json"), e.getLocalizedMessage());
        }

        // finally the (optional) comment
        try
        {
            final Object comment = json.opt("comment");
            fillCommentIssueInputParameter(comment, issueInputParameters);
        }
        catch (IllegalArgumentException e)
        {
            throw new RESTException(Response.Status.BAD_REQUEST, e.getMessage());
        }
        catch (JSONException e)
        {
            throw new RESTException(Response.Status.BAD_REQUEST, i18n.getText("rest.bad.json"), e.getLocalizedMessage());
        }

        final IssueService.TransitionValidationResult validationResult = issueService.validateTransition(authContext.getUser(), issue.getId(), actionId, issueInputParameters);

        if (!validationResult.isValid())
        {
            final ErrorCollection errorCollection = ErrorCollection.builder().addErrorCollection(validationResult.getErrorCollection()).build();
            throw new RESTException(Response.Status.BAD_REQUEST, errorCollection);
        }
        else
        {
            issueService.transition(authContext.getUser(), validationResult);
            return Response.noContent().build();
        }
    }

    private void fillCommentIssueInputParameter(final Object comment, final IssueInputParameters issueInputParameters)
            throws JSONException
    {
        if (comment != null)
        {
            if (comment instanceof String)
            {
                issueInputParameters.setComment((String) comment);
            }
            else if (comment instanceof JSONObject)
            {
                final JSONObject commentJson = (JSONObject) comment;
                final String body = commentJson.getString("body");
                if (commentJson.has("visibility"))
                {
                    final JSONObject visibility = commentJson.getJSONObject("visibility");
                    final String type = visibility.getString("type");
                    if (type.equalsIgnoreCase("group"))
                    {
                        issueInputParameters.setComment(body, visibility.getString("value"));
                    }
                    else if (type.equalsIgnoreCase("role"))
                    {
                        final String roleStr = visibility.getString("value");
                        final ProjectRole role = projectRoleManager.getProjectRole(roleStr);
                        if (role == null)
                        {
                            throw new IllegalArgumentException("Invalid role [" + roleStr + "]");
                        }

                        issueInputParameters.setComment(body, role.getId());
                    }
                    else
                    {
                        throw new IllegalArgumentException(String.format("Unknown visibility type: %s", visibility.getString("type")));
                    }
                }
                else
                {
                    issueInputParameters.setComment(body);
                }
            }
        }
    }

    private Map<String, String[]> jsonToIssueParams(final JSONObject json) throws JSONException
    {
        Map<String, String[]> map = new HashMap<String, String[]>();
        final String[] fields = JSONObject.getNames(json);
        if (fields == null)
        {
            return map;
        }

        for (String field : fields)
        {
            final Object value = json.get(field);
            // Numbers need special treatment because we need to localize the stringification
            if (value instanceof Integer || value instanceof Long || value instanceof Double)
            {
                final NumberFormat numberFormat = NumberFormat.getInstance(authContext.getLocale());
                map.put(field, new String[] { numberFormat.format(value) });
            }
            else if (value instanceof String)
            {
                // Wrap a single value in a String[]
                if (resolverManager.handles(field))
                {
                    final String resolvedId = resolverManager.getSingleIdFromName(value.toString(), field);
                    map.put(field, new String[] { resolvedId });
                }
                else
                {
                    map.put(field, new String[] { value.toString() });
                }
            }
            else if (value instanceof JSONArray)
            {
                // pull everything out of the JSONArray and put them in a regular Java String[] array
                final JSONArray valueArray = (JSONArray) value;
                final int size = valueArray.length();
                final String[] values = new String[size];
                for (int i = 0; i < size; i++)
                {
                    final String currentValue = valueArray.getString(i);
                    if (resolverManager.handles(field))
                    {
                        final String resolvedId = resolverManager.getSingleIdFromName(currentValue, field);
                        values[i] = resolvedId;
                    }
                    else
                    {
                        values[i] = currentValue;
                    }
                }

                map.put(field, values);
            }
        }

        return map;
    }

    private Issue getIssueObject(final String issueKey) throws NotFoundWebException
    {
        final User user = authContext.getUser();

        final MutableIssue issue = issueManager.getIssueObject(issueKey);
        if (issue == null)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("issue.does.not.exist.title")));
        }
        if (!permissionManager.hasPermission(Permissions.BROWSE, issue, user))
        {
            ErrorCollection.Builder errorBuilder = ErrorCollection.Builder.newBuilder().addErrorMessage(i18n.getText("admin.errors.issues.no.permission.to.see"));
            if (user == null)
            {
                errorBuilder.addErrorMessage(i18n.getText("login.required.title"));
                throw new RESTException(Response.Status.UNAUTHORIZED, errorBuilder.build());
            }
            else
            {
                throw new RESTException(Response.Status.FORBIDDEN, errorBuilder.build());
            }
        }
        
        return issue;
    }


    /**
     * Remove your vote from an issue. (i.e. "unvote")
     * @param issueKey the issue the current user is unvoting on
     * @return a Response containing either NO_CONTENT or an error message.
     *
     * @response.representation.204.doc
     *      Nothing is returned on success.
     *
     * @response.representation.404.doc
     *      Returned if the user cannot remove a vote for any reason. (The user did not vote on the issue,
     *      the user is the reporter, voting is disabled, the issue does not exist, etc.)
     *
     */
    @DELETE
    @Path("{issueKey}/votes")
    public Response removeVote(@PathParam("issueKey") final String issueKey)
    {
        final Issue issue = getIssueObject(issueKey);

        final VoteService.VoteValidationResult validationResult = voteService.validateRemoveVote(authContext.getUser(), authContext.getUser(), issue);
        if (!validationResult.isValid())
        {
            final ErrorCollection errors = ErrorCollection.builder()
                    .addErrorCollection(validationResult.getErrorCollection())
                    .build();

            throw new RESTException(Response.Status.NOT_FOUND, errors);
        }
        else
        {
            voteService.removeVote(authContext.getUser(), validationResult);
            return NO_CONTENT();
        }
    }

    /**
     * Cast your vote in favour of an issue.
     *
     * @param issueKey the issue to vote for
     * @return a Response containing NO_CONTENT or an error message
     *
     * @response.representation.204.doc
     *      Nothing is returned on success.
     *
     * @response.representation.404.doc
     *      Returned if the user cannot vote for any reason. (The user is the reporter, the user does
     *      not have permission to vote, voting is disabled in the instance, the issue does not exist, etc.)
     */
    @POST
    @Path("{issueKey}/votes")
    public Response addVote(@PathParam("issueKey") final String issueKey)
    {
        final Issue issue = getIssueObject(issueKey);

        final VoteService.VoteValidationResult validationResult = voteService.validateAddVote(authContext.getUser(), authContext.getUser(), issue);
        if (!validationResult.isValid())
        {
            final ErrorCollection errors = ErrorCollection.builder()
                    .addErrorCollection(validationResult.getErrorCollection())
                    .build();

            throw new RESTException(Response.Status.NOT_FOUND, errors);
        }
        else
        {
            voteService.addVote(authContext.getUser(), validationResult);
            return NO_CONTENT();
        }
    }

    /**
     * A REST sub-resource representing the voters on the issue. This sub-resource is also used for voting
     * and unvoting (via POST and DELETE).
     *
     * @param issueKey the issue to view voting information for
     * @return a Response containing a VoteBean
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      Information about voting on the current issue.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.VoteBean#DOC_EXAMPLE}
     * 
     * @response.representation.404.doc
     *      Returned if the user cannot view the issue in question or voting is disabled.
     */
    @GET
    @Path("{issueKey}/votes")
    public Response getVotes(@PathParam("issueKey") final String issueKey)
    {
        final Issue issue = getIssueObject(issueKey);
        final User user = authContext.getUser();

        if (voteService.isVotingEnabled())
        {
            List<UserBean> voters;

            final boolean hasVoted = voteService.hasVoted(issue, user);
            final ServiceOutcome<? extends Collection<User>> outcome = voteService.viewVoters(issue, user);
            if (outcome.isValid())
            {
                voters = new ArrayList<UserBean>(Transformed.collection(outcome.getReturnedValue(), new Function<User, UserBean>()
                {
                    public UserBean get(final User input)
                    {
                        return new UserBeanBuilder().user(input).context(contextUriInfo).buildShort();
                    }
                }));
            }
            else
            {
                voters = new ArrayList<UserBean>();
            }

            final URI selfUri = contextUriInfo.getBaseUriBuilder().path(IssueResource.class).path(issue.getKey()).path("votes").build();
            final VoteBean voteBean = new VoteBean(selfUri, hasVoted, issue.getVotes(), voters);
            return Response.ok(voteBean).cacheControl(never()).build();
        }
        else
        {
            final ErrorCollection error = ErrorCollection.builder()
                    .addErrorMessage(i18n.getText("issue.operations.voting.disabled")).build();
            throw new RESTException(Response.Status.NOT_FOUND, error);
        }
    }


    /**
     * Returns a full representation of the issue for the given issue key.
     *
     *  An issue JSON consists of the issue key, a collection of fields,
     *  a link to the workflow transition sub-resource, and (optionally) the HTML rendered values of any fields that support it
     *  (e.g. if wiki syntax is enabled for the description or comments).
     *  <p>
     *  The fields is the heart of the issue. Each field has the same basic shape:
     *  <p>
     *      <code>"field-id" : { "name" : "field-name", "type" : "field-type", "value" : "field-value" }</code>
     *  <p>
     *  For system fields, you'll notice that the field-id and the field-name are the same (e.g. "assignee" and "assignee"),
     *  while for custom fields they are different ("customfield_10000" and "Participants").
     *  <p>
     *  If a field has no value then the "value" key will not be present. For instance, if there is no assignee it would
     *  simply appear as:
     *  <p>
     *      <code>"assignee" : { "name" : "assignee", "type" : "com.opensymphony.user.User" }</code>
     *  <p>
     *  This indicates there is a field named assignee but it has no value.
     *
     * @param issueKey the issue key to request (i.e. JRA-1330)
     * @return a Response containing a IssueBean
     *
     * @response.representation.200.qname
     *      issue
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      Returns a full representation of a JIRA issue in JSON format.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.IssueBean#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *     Returned if the requested issue is not found, or the user does not have permission to view it.
     */
    @GET
    @Path ("/{issueKey}")
    public Response getIssue(@PathParam ("issueKey") final String issueKey)
    {
        final Issue issue = getIssueObject(issueKey);

        final IssueBean bean = createIssue(issue);
        return Response.ok(bean).cacheControl(never()).build();
    }

    /**
     * Returns the list of watchers for the issue with the given key.
     *
     * @param issueKey the issue key to request (i.e. JRA-1330)
     * @return a Response containing a WatchersBean
     *
     * @response.representation.200.qname
     *      watchers
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      Returns the list of watchers for an issue.
     *
     * @response.representation.200.example
     *      {@link WatchersBean#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *     Returned if the requested issue is not found, or the user does not have permission to view it.
     */
    @GET
    @Path("{issueKey}/watchers")
    public Response getIssueWatchers(@PathParam("issueKey") String issueKey)
    {
        Issue issue = getIssueObject(issueKey);
        WatchersBean watchers = watcherOps.getWatchers(issue, authContext.getUser());

        return Response.ok(watchers).cacheControl(never()).build();
    }

    /**
     * Adds a user to an issue's watcher list.
     *
     * @param issueKey a String containing an issue key
     * @param userName the name of the user to add to the watcher list. If no name is specified, the current user is added.
     * @return nothing
     *
     * @request.representation.example
     *      "fred"
     *
     * @response.representation.400.doc
     *      Returned if there is a problem with the received user representation.
     *
     * @response.representation.204.doc
     *      Returned if the watcher was added successfully.
     *
     * @response.representation.401.doc
     *      Returned if the calling user does not have permission to add the watcher to the issue's list of watchers.
     *
     * @response.representation.404.doc
     *      Returned if either the issue or the user does not exist.
     */
    @POST
    @Path("{issueKey}/watchers")
    public Response addWatcher(@PathParam("issueKey") String issueKey, String userName)
    {
        try
        {
            final User watchUser = getUserFromPost(userName);
            if (watchUser == null)
            {
                return BAD_REQUEST();
            }
            Issue issue = getIssueObject(issueKey);
            ServiceOutcome<List<User>> outcome = watcherService.addWatcher(issue, authContext.getUser(), watchUser);
            if (!outcome.isValid())
            {
                throw new NotAuthorisedWebException(ErrorCollection.of(outcome.getErrorCollection()));
            }

            return NO_CONTENT();
        }
        catch (WatchingDisabledException e)
        {
            throw new NotFoundWebException(e);
        }
    }

    private User getUserFromPost(final String body)
    {
        if (StringUtils.isEmpty(body))
        {
            return authContext.getUser();
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getJsonFactory();
        try
        {
            JsonParser jp = factory.createJsonParser(body);
            JsonNode obj = mapper.readTree(jp);
            if (obj.isTextual())
            {
                String userName = obj.getTextValue();
                if (StringUtils.isEmpty(userName))
                {
                    return authContext.getUser();
                }
                else
                {
                    return userManager.getUser(userName);
                }
            }
            else
            {
                throw new WebApplicationException(BAD_REQUEST());
            }
        }
        catch (JsonParseException e)
        {
            throw new WebApplicationException(e, BAD_REQUEST());
        }
        catch (JsonProcessingException e)
        {
            throw new WebApplicationException(e, BAD_REQUEST());
        }
        catch (IOException e)
        {
            throw new WebApplicationException(e, BAD_REQUEST());
        }
    }

    /**
     * Removes a user from an issue's watcher list.
     *
     * @param issueKey a String containing an issue key
     * @param userName a String containing the name of the user to remove from the watcher list
     * @return a 204 HTTP status if everything goes well
     *
     * @response.representation.204.doc
     *      Returned if the watcher was removed successfully.
     *
     * @response.representation.401.doc
     *      Returned if the calling user does not have permission to remove the watcher from the issue's list of
     *      watchers.
     *
     * @response.representation.404.doc
     *      Returned if either the issue or the user does not exist.
     */
    @DELETE
    @Path("{issueKey}/watchers")
    public Response removeWatcher(@PathParam("issueKey") String issueKey, @QueryParam ("username") String userName)
    {
        try
        {
            User unwatchUser = userManager.getUser(userName);
            if (unwatchUser == null)
            {
                throw new NotFoundWebException();
            }

            Issue issue = getIssueObject(issueKey);
            ServiceOutcome<List<User>> outcome = watcherService.removeWatcher(issue, authContext.getUser(), unwatchUser);
            if (!outcome.isValid())
            {
                throw new NotAuthorisedWebException(ErrorCollection.of(outcome.getErrorCollection()));
            }

            return NO_CONTENT();
        }
        catch (WatchingDisabledException e)
        {
            throw new NotFoundWebException();
        }
    }

    /**
     * Returns a Response with a status code of 400.
     *
     * @return a Response with a status code of 400.
     */
    protected Response BAD_REQUEST()
    {
        return Response.status(Response.Status.BAD_REQUEST).cacheControl(never()).build();
    }

    /**
     * Returns a Response with a status code of 204.
     *
     * @return a Response with a status code of 204
     */
    protected static Response NO_CONTENT()
    {
        return Response.noContent().cacheControl(never()).build();
    }

    IssueBean createIssue(final Issue issue)
    {
        final IssueBean bean = new IssueBean(issue.getKey(), uriBuilder.build(contextUriInfo, IssueResource.class, issue.getKey()));

        addFields(issue, bean);
        addIssueLinks(issue, bean);
        addParentSubtaskLinks(issue, bean);
        addCustomFields(issue, bean);
        addAttachments(issue, bean);
        bean.setTransitions(contextUriInfo.getBaseUriBuilder().path(IssueResource.class).path(issue.getKey()).path("transitions").build());
        addWatchers(issue, bean);

        return bean;
    }

    private void addWatchers(Issue issue, IssueBean bean)
    {
        WatchersBean watchers = watcherOps.getWatcherCount(issue, authContext.getUser());
        if (watchers != null)
        {
            bean.addField(IssueFieldConstants.WATCHERS, FieldBean.create(IssueFieldConstants.WATCHERS, IssueFieldConstants.WATCHERS, watchers));
        }
    }

    private void addAttachments(final Issue issue, final IssueBean bean)
    {
        if (attachmentManager.attachmentsEnabled())
        {
            final List<AttachmentBean> attachmentBeanList = CollectionUtil.transform(issue.getAttachments(), new Function<Attachment, AttachmentBean>()
            {
                public AttachmentBean get(Attachment attachment)
                {
                    return beanBuilderFactory.attachmentBean(attachment).context(contextUriInfo).build();
                }
            });
            bean.addField(IssueFieldConstants.ATTACHMENT, FieldBean.create(IssueFieldConstants.ATTACHMENT, JiraDataTypes.getType(IssueFieldConstants.ATTACHMENT), attachmentBeanList));
        }
    }

    public Collection<TransitionFieldBean> getRequiredFields(final FieldScreenRenderer fieldScreenRenderer, final Issue issue)
    {
        final Collection<TransitionFieldBean> fields = new ArrayList<TransitionFieldBean>();

        for (FieldScreenRenderTab fieldScreenRenderTab : fieldScreenRenderer.getFieldScreenRenderTabs())
        {
            for (FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem : fieldScreenRenderTab.getFieldScreenRenderLayoutItemsForProcessing())
            {
                if (fieldScreenRenderLayoutItem.isShow(issue))
                {
                    OrderableField orderableField = fieldScreenRenderLayoutItem.getOrderableField();

                    // JRA-16112 - This is a hack that is here because the resolution field is "special". You can not
                    // make the resolution field required and therefore by default the FieldLayoutItem for resolution
                    // returns false for the isRequired method. This is so that you can not make the resolution field
                    // required for issue creation. HOWEVER, whenever the resolution system field is shown it is
                    // required because the edit template does not provide a none option and indicates that it is
                    // required. THEREFORE, when the field is included on a transition screen we will do a special
                    // check to make the FieldLayoutItem claim it is required IF we run into the resolution field.
                    if (IssueFieldConstants.RESOLUTION.equals(orderableField.getId()))
                    {
                        fieldScreenRenderLayoutItem =
                                new FieldScreenRenderLayoutItemImpl(fieldScreenRenderLayoutItem.getFieldScreenLayoutItem(), fieldScreenRenderLayoutItem.getFieldLayoutItem())
                                {
                                    public boolean isRequired()
                                    {
                                        return true;
                                    }
                                };
                    }

                    String type;
                    if (orderableField instanceof CustomField)
                    {
                        type = ((CustomField)orderableField).getCustomFieldType().getKey();
                    }
                    else
                    {
                        type = JiraDataTypes.getType(orderableField);
                    }

                    final TransitionFieldBean bean = TransitionFieldBean.newBean()
                            .id(orderableField.getId())
                            .required(fieldScreenRenderLayoutItem.isRequired())
                            .type(type);
                    fields.add(bean);
                }
            }
        }
        return fields;
    }

    private Integer getSequenceFromAction(ActionDescriptor action)
    {
        if (action == null)
        {
            return Integer.MAX_VALUE;
        }

        final Map metaAttributes = action.getMetaAttributes();
        if (metaAttributes == null)
        {
            return Integer.MAX_VALUE;
        }

        final String value = (String) metaAttributes.get("opsbar-sequence");

        if (value == null || StringUtils.isBlank(value) || !StringUtils.isNumeric(value))
        {
            return Integer.MAX_VALUE;
        }

        return Integer.valueOf(value);
    }

    private List<ActionDescriptor> loadAvailableActions(User user, Issue issueObject)
    {
        final Project project = issueObject.getProjectObject();
        final List<ActionDescriptor> availableActions = new ArrayList<ActionDescriptor>();

        if (issueObject.getWorkflowId() == null)
        {
            LOG.warn("!!! Issue " + issueObject.getKey() + " has no workflow ID !!! ");
            return availableActions;
        }

        try
        {
            final Workflow wf = workflowManager.makeWorkflow(user != null ? user.getName() : null);
            final WorkflowDescriptor wd = workflowManager.getWorkflow(issueObject).getDescriptor();

            final HashMap<String, Object> inputs = new HashMap<String, Object>();
            inputs.put("pkey", project.getKey()); // Allows ${project.key} in condition args
            inputs.put("issue", issueObject);
            // The condition should examine the original issue object - put this in the transientvars
            // This is done here as AbstractWorkflow later changes this collection to be an unmodifiable map
            inputs.put(WorkflowFunctionUtils.ORIGINAL_ISSUE_KEY, issueObject);
            int[] actionIds = wf.getAvailableActions(issueObject.getWorkflowId(), inputs);

            for (int actionId : actionIds)
            {
                final ActionDescriptor action = wd.getAction(actionId);
                if (action == null)
                {
                    LOG.error("State of issue [" + issueObject + "] has an action [id=" + actionId +
                            "] which cannot be found in the workflow descriptor");
                }
                else
                {
                    availableActions.add(action);
                }
            }
        }
        catch (Exception e)
        {
            LOG.error("Exception thrown while getting available actions", e);
        }

        return availableActions;
    }


    // Even though comments are "expandable" we are still loading them all into memory here. This kind of sucks but
    // the CommentManager doesn't have a good enough API to do what we need. We want to get the number of comments without
    // loading them all. Then we would want to just get the comment ID (without the body or any other information) and
    // lazy-load the rest of the comment when the ListWrapperCallback gets invoked.
    private void addComments(final FieldLayoutItem fieldLayoutItem, final Issue issue, final IssueBean bean)
    {
        final Field field = fieldLayoutItem.getOrderableField();
        final Function<Comment, String> commentRenderer = new Function<Comment, String>()
        {
            public String get(final Comment comment)
            {
                return rendererManager.getRenderedContent(fieldLayoutItem.getRendererType(), comment.getBody(), issue.getIssueRenderContext());
            }
        };

        // Add in all comments for this issue.
        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        final List<Comment> comments = commentService.getCommentsForUser(authContext.getUser(), issue, errorCollection);
        if(!errorCollection.hasAnyErrors())
        {
            bean.addField(field.getId(), FieldBean.create(field.getId(), JiraDataTypes.getType(field), CommentBean.asBeans(comments, contextUriInfo)));
            bean.addHtml(field.getId(), Transformed.list(comments, commentRenderer));
        }
    }

    /**
     * Adds all necessary IssueLinkBean instances to the passed in IssueBean.
     *
     * @param issue the issue
     * @param bean the IssueBean instance that will be modified
     */
    protected void addIssueLinks(Issue issue, IssueBean bean)
    {
        if (!applicationProperties.getOption(APKeys.JIRA_OPTION_ISSUELINKING))
        {
            // issue linking disabled
            return;
        }

        // gather a list of all outward-linked issues
        final LinkCollection linksForIssue = issueLinkManager.getLinkCollection(issue, authContext.getLoggedInUser());
        final Map<Issue, IssueLinkType> outwardLinkedIssues = new HashMap<Issue, IssueLinkType>();
        final Map<Issue, IssueLinkType> inwardLinkedIssues = new HashMap<Issue, IssueLinkType>();
        if (linksForIssue.getLinkTypes() == null)
        {
            return;
        }

        for (IssueLinkType linkType : linksForIssue.getLinkTypes())
        {
            List<Issue> outwardIssueLinks = linksForIssue.getOutwardIssues(linkType.getName());
            if (outwardIssueLinks != null)
            {
                for (Issue linkedIssue : outwardIssueLinks)
                {
                    outwardLinkedIssues.put(linkedIssue, linkType);
                }
            }

            List<Issue> inwardIssueLinks = linksForIssue.getInwardIssues(linkType.getName());
            if (inwardIssueLinks != null)
            {
                for (Issue linkedIssue : inwardIssueLinks)
                {
                    inwardLinkedIssues.put(linkedIssue, linkType);
                }
        }
        }

        // now create an IssueLinkBean instance for each
        List<IssueLinkBean> links = new ArrayList<IssueLinkBean>();
        for (Map.Entry<Issue, IssueLinkType> linkToLinkType : outwardLinkedIssues.entrySet())
        {
            final LinkedIssueTypeBean linkedIssueType = LinkedIssueTypeBean.instance()
                    .name(linkToLinkType.getValue().getName())
                    .direction(LinkedIssueTypeBean.Direction.OUTBOUND)
                    .description(linkToLinkType.getValue().getOutward()).build();
            links.add(createIssueLink(linkToLinkType.getKey(), linkedIssueType));
        }
        for (Map.Entry<Issue, IssueLinkType> linkToLinkType : inwardLinkedIssues.entrySet())
        {
            final LinkedIssueTypeBean linkedIssueType = LinkedIssueTypeBean.instance()
                    .name(linkToLinkType.getValue().getName())
                    .direction(LinkedIssueTypeBean.Direction.INBOUND)
                    .description(linkToLinkType.getValue().getInward()).build();
            links.add(createIssueLink(linkToLinkType.getKey(), linkedIssueType));
        }

        bean.addField("links", FieldBean.create("links", IssueFieldConstants.ISSUE_LINKS, links));
    }

    protected void addParentSubtaskLinks(Issue issue, IssueBean bean)
    {
        Issue parent = issue.getParentObject();
        if (parent != null)
        {
            final LinkedIssueTypeBean linkedIssueType = LinkedIssueTypeBean.instance().name("Parent").direction(LinkedIssueTypeBean.Direction.INBOUND).build();
            bean.addField("parent", FieldBean.create("parent", IssueFieldConstants.ISSUE_LINKS, createIssueLink(parent, linkedIssueType)));
        }

        Collection<Issue> subtasks = issue.getSubTaskObjects();
        if (subtasks != null)
        {
            List<IssueLinkBean> subtaskLinks = new ArrayList<IssueLinkBean>(subtasks.size());
            for (Issue subtask : subtasks)
            {
                final LinkedIssueTypeBean linkedIssueType = LinkedIssueTypeBean.instance().name("Sub-Task").direction(LinkedIssueTypeBean.Direction.OUTBOUND).build();
                subtaskLinks.add(createIssueLink(subtask, linkedIssueType));
            }

            bean.addField("sub-tasks", FieldBean.create("sub-tasks", IssueFieldConstants.ISSUE_LINKS, subtaskLinks));
        }
    }

    private void addFields(final Issue issue, final IssueBean bean)
    {
        // First we try to add "NavigableFields". These aren't included in the Field Layout. This is a bit crap because
        // "getAvailableNavigableFields" doesn't take the issue into account. All it means is the field is not hidden
        // in at least one project the user has BROWSE permission on. This is far from what we need but isn't any worse
        // than what we had before. At least now the fieldId() isn't being hardcoded.....
        try
        {
            final Set<NavigableField> fields = fieldManager.getAvailableNavigableFields(authContext.getUser());
            for (NavigableField field : fields)
            {
                if (field instanceof ProjectSystemField)
                {
                    final Project project = issue.getProjectObject();
                    bean.addField(field.getId(), FieldBean.create(field.getId(), JiraDataTypes.getType(field), projectBeanFactory.shortProject(project)));
                }
                else if (field instanceof VotesSystemField)
                {
                    if (voteService.isVotingEnabled())
                    {
                        final URI selfUri = contextUriInfo.getBaseUriBuilder().path(IssueResource.class).path(issue.getKey()).path("votes").build();
                        bean.addField(field.getId(), FieldBean.create(field.getId(), JiraDataTypes.getType(field), new VoteBean(selfUri, voteService.hasVoted(issue, authContext.getUser()), issue.getVotes())));
                    }
                }
                else if (field instanceof StatusSystemField)
                {
                    Status status = issue.getStatusObject();
                    URI selfUri = uriBuilder.build(contextUriInfo, StatusResource.class, status.getId());
                    bean.addField(field.getId(), FieldBean.create(field.getId(), JiraDataTypes.getType(field), StatusBean.shortBean(status.getName(), selfUri)));
                }
                else if (field instanceof ResolutionDateSystemField)
                {
                    final Timestamp resolutionDate = issue.getResolutionDate();
                    if (resolutionDate != null)
                    {
                        bean.addField(field.getId(), FieldBean.create(field.getId(), JiraDataTypes.getType(field), Dates.asTimeString(resolutionDate)));
                    }
                }
                else if (field instanceof CreatedSystemField)
                {
                    bean.addField(field.getId(), FieldBean.create(field.getId(), JiraDataTypes.getType(field), Dates.asTimeString(issue.getCreated())));
                }
                else if (field instanceof UpdatedSystemField)
                {
                    bean.addField(field.getId(), FieldBean.create(field.getId(), JiraDataTypes.getType(field), Dates.asTimeString(issue.getUpdated())));
                }
            }
        }
        catch (FieldException e)
        {
            // ignored...display as much as we can.
        }

        // Now iterate over all the visible layout items from the field layout for this issue and attempt to add them
        // to the result
        final FieldLayout layout = fieldLayoutManager.getFieldLayout(issue);
        final List<FieldLayoutItem> fieldLayoutItems = layout.getVisibleLayoutItems(authContext.getUser(), issue.getProjectObject(), CollectionBuilder.list(issue.getIssueTypeObject().getId()));
        for (final FieldLayoutItem fieldLayoutItem : fieldLayoutItems)
        {
            final OrderableField field = fieldLayoutItem.getOrderableField();
            final String fieldName = field.getId();

            if (field instanceof CommentSystemField)
            {
                addComments(fieldLayoutItem, issue, bean);
            }
            else if (field instanceof AttachmentSystemField)
            {
                // Attachments shouldn't show up here (they should IMHO but they don't) but we'll add a case to catch (and ignore it) in case that happens.
                // They are handled separately above.
            }
            else if (field instanceof WorklogSystemField)
            {
                addWorklog(issue, bean, fieldLayoutItem);
            }
            else
            {
                final FieldBean fieldValue = getFieldValue(fieldLayoutItem, issue);

                if (fieldValue != null)
                {
                    bean.addField(fieldName, fieldValue);
                    if (field instanceof AbstractTextSystemField || field instanceof StringCFType)
                    {
                        if (((RenderableField) field).isRenderable())
                        {
                            final String content = rendererManager.getRenderedContent(fieldLayoutItem, issue);
                            bean.addHtml(fieldName, content);
                        }
                    }
                }
            }
        }
    }

    private void addWorklog(final Issue issue, final IssueBean bean, final FieldLayoutItem fieldLayoutItem)
    {
        final Field field = fieldLayoutItem.getOrderableField();
        final Function<Worklog, String> renderer = new Function<Worklog, String>()
        {
            public String get(final Worklog worklog)
            {
                return rendererManager.getRenderedContent(fieldLayoutItem.getRendererType(), worklog.getComment(), issue.getIssueRenderContext());
            }
        };

        final JiraServiceContextImpl serviceContext = new JiraServiceContextImpl(authContext.getUser());
        final List<Worklog> worklogs = worklogService.getByIssueVisibleToUser(serviceContext, issue);
        if (!serviceContext.getErrorCollection().hasAnyErrors())
        {
            bean.addField(field.getId(), FieldBean.create(field.getId(), JiraDataTypes.getType(field.getId()), WorklogBean.asBeans(worklogs, contextUriInfo, userManager)));
            bean.addHtml(field.getId(), Transformed.list(worklogs, renderer));
        }
    }

    protected void addCustomFields(Issue issue, IssueBean bean)
    {
        Map<String, FieldBean> customFields = customFieldOps.getCustomFields(issue);
        for (Map.Entry<String, FieldBean> customFieldEntry : customFields.entrySet())
        {
            bean.addField(customFieldEntry.getKey(), customFieldEntry.getValue());
        }
    }
    
    FieldBean getFieldValue(final FieldLayoutItem fieldLayoutItem, final Issue issue)
    {
        final OrderableField field = fieldLayoutItem.getOrderableField();
        final String id = field.getId();
        final String type = JiraDataTypes.getType(field);

        if (field instanceof AbstractTextSystemField)
        {
            final AbstractTextSystemField textField = (AbstractTextSystemField) field;
            return FieldBean.create(id, type, textField.getValueFromIssue(issue));
        }
        else if (field instanceof IssueTypeSystemField)
        {
            return FieldBean.create(id, type, new IssueTypeBeanBuilder().issueType(issue.getIssueTypeObject()).context(contextUriInfo).buildShort());
        }
        else if (field instanceof SecurityLevelSystemField)
        {
            return FieldBean.create(id, type, issueSecurityLevelManager.getIssueSecurityName(issue.getSecurityLevelId()));
        }
        else if (field instanceof PrioritySystemField)
        {
            final Priority priorityObject = issue.getPriorityObject();
            // priority may be technically null (e.g. when issue created through API or when priority was disabled
            // via field configuration and then re-enabled)
            final PriorityBean priorityBean = priorityObject != null
                    ? PriorityBean.shortBean(priorityObject, contextUriInfo) : null;
            return FieldBean.create(id, type, priorityBean);
        }
        else if (field instanceof UserField)
        {
            if (field instanceof ReporterSystemField)
            {
                return FieldBean.create(id, type, new UserBeanBuilder().user(issue.getReporter()).context(contextUriInfo).buildShort());
            }
            else if (field instanceof AssigneeSystemField)
            {
                return FieldBean.create(id, type, new UserBeanBuilder().user(issue.getAssignee()).context(contextUriInfo).buildShort());
            }
            else if (field instanceof CustomFieldType)
            {
                LOG.info(String.format("CustomField UserField %s not rendered in JSON", field.getId()));
                return null;
            }
            else
            {
                LOG.info(String.format("UserField %s not rendered in JSON", field.getId()));
                return null;
            }
        }
        else if (field instanceof ResolutionSystemField)
        {
            // if there is no resolution yet then we need to return an empty object
            final Resolution resolution = issue.getResolutionObject();
            if (resolution == null)
            {
                return null;
            }
            else
            {
                return FieldBean.create(id, type, ResolutionBean.shortBean(resolution, contextUriInfo));
            }
        }
        else if (field instanceof FixVersionsSystemField)
        {
            return FieldBean.create(id, type, versionBeanFactory.createVersionBeans(issue.getFixVersions()));
        }
        else if (field instanceof AffectedVersionsSystemField)
        {
            return FieldBean.create(id, type, versionBeanFactory.createVersionBeans(issue.getAffectedVersions()));
        }
        else if (field instanceof LabelsSystemField)
        {
            return FieldBean.create(id, type, LabelBean.asStrings(issue.getLabels()));
        }
        else if (field instanceof DueDateSystemField)
        {
            final Timestamp date = issue.getDueDate();
            final String s = date != null ? Dates.asDateString(date) : null;
            return FieldBean.create(id, type, s);
        }
        else if (field instanceof ComponentsSystemField)
        {
            return FieldBean.create(id, type, ComponentBean.asBeans(issue.getComponentObjects(), contextUriInfo));
        }
        else if (field instanceof TimeTrackingSystemField)
        {
            if (applicationProperties.getOption(APKeys.JIRA_OPTION_TIMETRACKING))
            {
                TimeTrackingBean timeTracking = new TimeTrackingBean(issue.getOriginalEstimate(), issue.getEstimate(), issue.getTimeSpent());

                return FieldBean.create(id, type, timeTracking.hasValues() ? timeTracking : null);
            }
            else
            {
                return null;
            }
        }
        else if (field instanceof CustomField)
        {
            // custom fields are handled by a separate mechanism
            return null;
        }
        else
        {
            LOG.info(String.format("OrderableField %s not rendered in JSON", field.getId()));
            return null;
        }
    }

    private IssueLinkBean createIssueLink(final Issue issue, final LinkedIssueTypeBean linkedIssueType)
    {
        return new IssueLinkBean(
                issue.getKey(),
                uriBuilder.build(contextUriInfo, IssueResource.class, issue.getKey()),
                linkedIssueType
        );
    }
}
