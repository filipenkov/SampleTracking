package com.atlassian.jira.plugin.issuenav.rest;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.issuenav.KickassRedirectFilter;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.opensymphony.util.TextUtils;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * Sends optoutfeedback to kickass hipchat room.
 *
 * @since v4.4
 */
@Path ("optoutfeedback")
public class OptOutFeedback
{
    private static final String API_BASE_URI = "https://api.hipchat.com";
    private static final String HIPCHAT_AUTH_TOKEN = "hipchat.auth.token";
    private static final Logger log = Logger.getLogger(OptOutFeedback.class);

    private final GroupManager groupManager;
    private final JiraAuthenticationContext authenticationContext;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final PluginSettingsFactory pluginSettingsFactory;
    private final HttpClient httpClient;

    public OptOutFeedback(GroupManager groupManager, JiraAuthenticationContext authenticationContext,
            VelocityRequestContextFactory velocityRequestContextFactory, final PluginSettingsFactory pluginSettingsFactory)
    {
        this.groupManager = groupManager;
        this.authenticationContext = authenticationContext;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.pluginSettingsFactory = pluginSettingsFactory;
        MultiThreadedHttpConnectionManager connectionManager =
                new MultiThreadedHttpConnectionManager();
        httpClient = new HttpClient(connectionManager);
    }

    @POST
    @Path ("authToken")
    public Response setAuthToken(@FormParam ("token") String token)
    {
        pluginSettingsFactory.createGlobalSettings().put(HIPCHAT_AUTH_TOKEN, StringUtils.trim(token));
        return Response.ok("Success").cacheControl(never()).build();
    }

    @POST
    public Response submitFeedback(Feedback feedback)
    {
        final User loggedInUser = authenticationContext.getLoggedInUser();
        final Group staff = groupManager.getGroup(KickassRedirectFilter.ATLASSIAN_STAFF);
        if (loggedInUser == null || staff == null || !groupManager.isUserInGroup(loggedInUser, staff))
        {
            return Response.serverError().cacheControl(never()).build();
        }

        notifyRoom("Kickass", feedback.getNote(), loggedInUser, velocityRequestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl());

        return Response.ok().cacheControl(never()).build();
    }


    private void notifyRoom(String room, String note, User user, String baseurl)
    {
        final String authToken = (String) pluginSettingsFactory.createGlobalSettings().get(HIPCHAT_AUTH_TOKEN);
        if (StringUtils.isBlank(authToken))
        {
            log.error("No hipchat authtoken was provided.");
            return;
        }

        String msg = new StringBuilder().
                append(TextUtils.htmlEncode(user.getDisplayName())).
                append(" just opted out of kickass on <a href=\"").
                append(baseurl).append("\">").
                append(baseurl).append("</a> because: \"").
                append(TextUtils.htmlEncode(note)).
                append("\"").toString();
        log.info(msg);
        PostMethod post = new PostMethod(API_BASE_URI + "/v1/rooms/message?auth_token=" + authToken);
        try
        {
            post.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
            post.addParameter("room_id", room);
            post.addParameter("from", "JIRA");
            post.addParameter("message", msg);
            post.addParameter("format", "json");


            int statusCode = httpClient.executeMethod(post);
            if (statusCode != HttpStatus.SC_OK)
            {
                log.error("Opting out of kickass failed (" + msg + "): " + post.getStatusLine());
            }
        }
        catch (IOException e)
        {
            log.error("IO Exception while posting to HipChat", e);
        }
        finally
        {
            post.releaseConnection();
        }
    }

    @XmlRootElement
    public static class Feedback
    {
        @XmlElement
        private String note;

        private Feedback() { }

        public Feedback(final String note)
        {
            this.note = note;
        }

        public String getNote()
        {
            return note;
        }
    }
}
