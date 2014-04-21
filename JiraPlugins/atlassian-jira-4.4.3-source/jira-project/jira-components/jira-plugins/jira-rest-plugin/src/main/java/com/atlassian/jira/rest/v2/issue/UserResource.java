package com.atlassian.jira.rest.v2.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.rest.NotFoundWebException;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.v2.issue.context.ContextI18n;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;

import static com.atlassian.jira.rest.api.http.CacheControl.never;
import static com.atlassian.jira.rest.v2.issue.VelocityRequestContextFactories.getBaseURI;


/**
 * @since 4.2
 */
@Path ("user")
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class UserResource
{
    private UserUtil userUtil;
    private VelocityRequestContextFactory velocityRequestContextFactory;
    private ContextI18n i18n;
    private EmailFormatter emailFormatter;
    private JiraAuthenticationContext jiraAuthenticationContext;
    private TimeZoneManager timeZoneManager;

    private UserResource()
    {
        // this constructor used by tooling
    }

    public UserResource(final UserUtil userUtil, final VelocityRequestContextFactory velocityRequestContextFactory, ContextI18n i18n, EmailFormatter emailFormatter, JiraAuthenticationContext jiraAuthenticationContext, TimeZoneManager timeZoneManager)
    {
        this.userUtil = userUtil;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.i18n = i18n;
        this.emailFormatter = emailFormatter;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.timeZoneManager = timeZoneManager;
    }

    /**
     * Returns a user. This resource cannot be accessed anonymously.
     *
     * @param name the username
     *
     * @response.representation.200.qname
     *      user
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      Returns a full representation of a JIRA user in JSON format.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.issue.UserBean#DOC_EXAMPLE}
     *
     * @response.representation.404.doc
     *     Returned if the requested user is not found.
     *
     * @response.representation.401.doc
     *     Returned if the current user is not authenticated.
     */
    @GET
    public Response getUser(@QueryParam("username") final String name, @Context Request request, @Context UriInfo uriInfo)
    {
        if (name == null)
        {
            throw new RESTException(Response.Status.NOT_FOUND, ErrorCollection.of(i18n.getText("rest.user.error.no.username.param")));
        }

        final User user = userUtil.getUser(name);
        if (user == null)
        {
            throw new NotFoundWebException(ErrorCollection.of(i18n.getText("rest.user.error.not.found", name)));
        }

        UserBeanBuilder builder = new UserBeanBuilder().user(user).context(uriInfo);
        builder.baseURL(getBaseURI(velocityRequestContextFactory));
        builder.groups(new ArrayList(userUtil.getGroupNamesForUser(user.getName())));
        builder.loggedInUser(jiraAuthenticationContext.getUser());
        builder.emailFormatter(emailFormatter);
        builder.timeZone(timeZoneManager.getLoggedInUserTimeZone());
        return Response.ok(builder.buildFull()).cacheControl(never()).build();
    }
}
