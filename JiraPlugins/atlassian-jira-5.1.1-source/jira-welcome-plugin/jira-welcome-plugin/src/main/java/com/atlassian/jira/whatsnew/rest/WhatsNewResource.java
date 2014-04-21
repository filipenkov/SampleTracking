package com.atlassian.jira.whatsnew.rest;

import com.atlassian.jira.rest.api.http.CacheControl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.whatsnew.WhatsNewManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * REST Resource to set if a user should see the what's new dialog.
 *
 * @since v1.0
 */
@AnonymousAllowed
@Path ("show")
public class WhatsNewResource
{
    private final JiraAuthenticationContext authenticationContext;
    private final WhatsNewManager whatsNewManager;

    public WhatsNewResource(final JiraAuthenticationContext authenticationContext, final WhatsNewManager whatsNewManager)
    {
        this.authenticationContext = authenticationContext;
        this.whatsNewManager = whatsNewManager;
    }

    @GET
    public Response shouldShowForUser()
    {
        return Response.ok(new WhatsNewSetting(whatsNewManager.isShownForUser(authenticationContext.getLoggedInUser(), false))).
                cacheControl(CacheControl.never()).build();
    }

    @POST
    public Response setShownForUser()
    {
        whatsNewManager.setShownForUser(authenticationContext.getLoggedInUser(), true);
        return Response.ok().cacheControl(CacheControl.never()).build();
    }

    @DELETE
    public Response dontShownForUser()
    {
        whatsNewManager.setShownForUser(authenticationContext.getLoggedInUser(), false);
        return Response.ok().cacheControl(CacheControl.never()).build();
    }

    @XmlRootElement
    static class WhatsNewSetting
    {
        @XmlElement
        private boolean isShownForUser;

        private WhatsNewSetting() {}

        WhatsNewSetting(final boolean shownForUser)
        {
            isShownForUser = shownForUser;
        }

    }
}
