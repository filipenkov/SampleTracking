package com.atlassian.jira.quickedit.rest;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.quickedit.rest.api.UserPreferences;
import com.atlassian.jira.quickedit.user.UserPreferencesStore;
import com.atlassian.jira.rest.api.http.CacheControl;
import com.atlassian.jira.security.JiraAuthenticationContext;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST Resource responsible for storing & retrieving fields a user has selected to see on their quick edit/create
 * screens.
 * <p/>
 * Allows users to update the fields used for quick create/edit by posting JSON of the form:
 * <pre>
 *     {"fields": ["description","summary"]}
 * </pre>
 * to
 * <pre>http://localhost:2990/jira/rest/quickedit/1.0/userpreferences/</pre>
 *
 * @since v5.0
 */
@Produces ( { MediaType.APPLICATION_JSON })
@Consumes ( { MediaType.APPLICATION_JSON })
@Path ("userpreferences")
public class UserPreferenceResource
{
    private final JiraAuthenticationContext authenticationContext;
    private final UserPreferencesStore userPreferencesStore;

    public UserPreferenceResource(final JiraAuthenticationContext authenticationContext, final UserPreferencesStore userPreferencesStore)
    {
        this.authenticationContext = authenticationContext;
        this.userPreferencesStore = userPreferencesStore;
    }

    @GET
    @Path ("edit")
    public Response getEditPreferences()
    {
        final User loggedInUser = authenticationContext.getLoggedInUser();
        if (loggedInUser == null)
        {
            return Response.ok(Response.Status.NOT_FOUND).cacheControl(CacheControl.never()).build();
        }
        return Response.ok(userPreferencesStore.getEditUserPreferences(loggedInUser)).cacheControl(CacheControl.never()).build();

    }

    @POST
    @Path ("edit")
    public Response setEditPreferences(final UserPreferences prefs)
    {
        final User loggedInUser = authenticationContext.getLoggedInUser();
        if (loggedInUser == null || prefs == null)
        {
            return Response.ok(Response.Status.NOT_FOUND).cacheControl(CacheControl.never()).build();
        }
        userPreferencesStore.storeEditUserPreferences(loggedInUser, prefs);
        return Response.ok().cacheControl(CacheControl.never()).build();
    }

    @GET
    @Path ("create")
    public Response getCreatePreferences()
    {
        final User loggedInUser = authenticationContext.getLoggedInUser();
        if (loggedInUser == null)
        {
            return Response.ok(Response.Status.NOT_FOUND).cacheControl(CacheControl.never()).build();
        }
        return Response.ok(userPreferencesStore.getCreateUserPreferences(loggedInUser)).cacheControl(CacheControl.never()).build();

    }

    @POST
    @Path ("create")
    public Response setCreatePreferences(final UserPreferences prefs)
    {
        final User loggedInUser = authenticationContext.getLoggedInUser();
        if (loggedInUser == null || prefs == null)
        {
            return Response.ok(Response.Status.NOT_FOUND).cacheControl(CacheControl.never()).build();
        }
        userPreferencesStore.storeCreateUserPreferences(loggedInUser, prefs);
        return Response.ok().cacheControl(CacheControl.never()).build();
    }

}
