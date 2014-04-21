package com.atlassian.jira.plugin.link.applinks.rest.resource;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkRequestFactory;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.jira.plugin.link.applinks.AppLinkUtils;
import com.atlassian.jira.plugin.link.applinks.rest.bean.AppLinkInfoBean;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.sal.api.net.Request;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * A AppLinks-related REST resource for remote issue links.
 *
 * @since v5.0
 */
@AnonymousAllowed
@Path ("appLink")
public class AppLinksInfoResource
{
    private final static Logger LOG = LoggerFactory.getLogger(AppLinksInfoResource.class);

    private final ApplicationLinkService applicationLinkService;

    public AppLinksInfoResource(final ApplicationLinkService applicationLinkService)
    {
        this.applicationLinkService = applicationLinkService;
    }

    @GET
    @Path ("/info")
    @Produces ({ MediaType.APPLICATION_JSON })
    public Response getAppLinksInfo(@QueryParam ("type") final String type)
    {
        final Iterable<ApplicationLink> applicationLinks;

        if (type != null)
        {
            applicationLinks = getAppLinksByType(type);
        }
        else
        {
            applicationLinks = applicationLinkService.getApplicationLinks();
        }

        final Collection<AppLinkInfoBean> beans = convertToAppLinkInfoBeans(applicationLinks);
        return Response.ok(beans).cacheControl(never()).build();
    }

    private Iterable<ApplicationLink> getAppLinksByType(final String type)
    {
        try
        {
            final Class<ApplicationType> appTypeClass = AppLinkUtils.getApplicationTypeClass(type);
            return applicationLinkService.getApplicationLinks(appTypeClass);
        }
        catch (final IllegalArgumentException e)
        {
            LOG.error(e.getMessage(), e);
        }

        return Collections.emptyList();
    }

    private Collection<AppLinkInfoBean> convertToAppLinkInfoBeans(final Iterable<ApplicationLink> applicationLinks)
    {
        List<AppLinkInfoBean> appLinkInfoBeans = Lists.newArrayList();
        for (ApplicationLink applicationLink : applicationLinks)
        {
            final ApplicationLinkRequestFactory requestFactory = applicationLink.createAuthenticatedRequestFactory();

            boolean requireCredentials = appLinksRequiresAuthentication(requestFactory);

            final URI authorisationUri = requestFactory.getAuthorisationURI();
            final String authUrl = authorisationUri == null ? null : authorisationUri.toString();
            appLinkInfoBeans.add(new AppLinkInfoBean(applicationLink.getId().toString(), applicationLink.getDisplayUrl().toString(), applicationLink.getName(), applicationLink.isPrimary(), authUrl, requireCredentials));
        }
        return appLinkInfoBeans;
    }

    private boolean appLinksRequiresAuthentication(ApplicationLinkRequestFactory requestFactory)
    {
        try
        {
            requestFactory.createRequest(Request.MethodType.GET, "");
            return false;
        }
        catch (CredentialsRequiredException e)
        {
            return true;
        }
    }
}
