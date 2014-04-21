package com.atlassian.administration.quicksearch.rest;

import com.atlassian.administration.quicksearch.spi.UserContext;
import com.atlassian.applinks.core.AppLinksManager;
import com.atlassian.applinks.core.Application;
import com.atlassian.applinks.core.ApplicationInstance;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.RequestFactory;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/**
 * Forwards requests for admin links to remote applications.
 *
 * @since 1.0
 */
public class DefaultRemoteAdminLinksProvider implements RemoteAdminLinksProvider {

    private static final Logger log = LoggerFactory.getLogger(DefaultRemoteAdminLinksProvider.class);

    // JRADEV-11739 disabling remote links until Bamboo and Conf plugins are fixed
    private static final Application[] SUPPORTED_APPS = { /*Application.JIRA, Application.CONFLUENCE, Application.BAMBOO*/ };

    private final RequestFactory<?> requestFactory;
    private final AppLinksManager appLinksManager;

    public DefaultRemoteAdminLinksProvider(RequestFactory<?> requestFactory, AppLinksManager appLinksManager)
    {
        this.requestFactory = requestFactory;
        this.appLinksManager = appLinksManager;
    }

    @Override
    public LocationBean getDefaultRemoteAdminLinks(UserContext context)
    {
        // assumed default location
        return getRemoteAdminLinksFor("default", context);
    }

    @Override
    public LocationBean getRemoteAdminLinksFor(String location, UserContext userContext)
    {
        List<LocationBean> remoteLinks = Lists.newArrayList();
        for (ApplicationInstance instance: getApps())
        {
            remoteLinks.add(getRemoteLinks(instance, location, userContext));
        }
        return LocationBeanUtils.merge(remoteLinks);
    }

    private LocationBean getRemoteLinks(ApplicationInstance instance, String location, UserContext userContext)
    {
        try
        {
            final Request<?,?> request = requestFactory.createRequest(Request.MethodType.GET, instance.getUrl()
                    + "/rest/adminquicksearch/latest/local/links/" + location);
            request.setConnectionTimeout(5000);
            request.setSoTimeout(5000);
            request.addTrustedTokenAuthentication(userContext.getUsername());
            return makeAbsoluteLinks(instance, LocationBeanUtils.deserialize(request.execute()));
        }
        catch (Exception e)
        {
            log.warn("Exception while getting admin links from application " + instance.getUrl() + ": " + e);
            log.trace("Exception", e);
            return LocationBeanUtils.newEmptyBean();
        }
    }

    private Iterable<ApplicationInstance> getApps()
    {
        List<ApplicationInstance> instances = Lists.newArrayList();
        for (Application application : SUPPORTED_APPS)
        {
            instances.addAll(appLinksManager.getApplicationInstances(application).getAll().values());
        }
        return instances;
    }

    private LocationBean makeAbsoluteLinks(ApplicationInstance application, LocationBean locationBean)
    {
        for (LinkBean link : locationBean.links())
        {
            makeAbsolute(application, link);
        }
        for (SectionBean section : locationBean.sections())
        {
            makeAbsoluteLinks(application, section);
        }
        return locationBean;
    }

    private void makeAbsolute(ApplicationInstance application, LinkBean link)
    {
        if (isRelative(link.linkUrl))
        {
            link.linkUrl = addBaseUrl(application, link.linkUrl);
        }
    }

    private String addBaseUrl(ApplicationInstance application, String linkUrl)
    {
        final String appBaseUrl = application.getUrl();
        if (linkUrl.startsWith("/"))
        {
            final int indexOfSecondSeparator = linkUrl.indexOf("/", 1);
            if (indexOfSecondSeparator > 0 && appBaseUrl.endsWith(linkUrl.substring(0, indexOfSecondSeparator)))
            {
                return appBaseUrl + linkUrl.substring(indexOfSecondSeparator);
            }
            else
            {
                return appBaseUrl + linkUrl;
            }
        }
        else
        {
            return appBaseUrl + "/" + linkUrl;
        }
    }

    private boolean isRelative(String linkUrl)
    {
        return !linkUrl.startsWith("http://") && !linkUrl.startsWith("https://");
    }
}
