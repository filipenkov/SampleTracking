package com.atlassian.applinks.core.rest;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.core.InternalTypeAccessor;
import com.atlassian.applinks.core.rest.model.ApplicationLinkEntity;
import com.atlassian.applinks.spi.link.MutatingApplicationLinkService;
import com.atlassian.plugins.rest.common.Link;
import com.atlassian.plugins.rest.common.util.RestUrlBuilder;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.RequestFactory;
import com.atlassian.sal.api.net.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * @since   3.0
 */
public abstract class AbstractResource {
    
    protected final RestUrlBuilder restUrlBuilder;
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
    protected final InternalTypeAccessor typeAccessor;
    protected final RequestFactory<Request<Request<?, Response>,Response>> requestFactory;
    protected final MutatingApplicationLinkService applicationLinkService;

    public AbstractResource(final RestUrlBuilder restUrlBuilder,
                            final InternalTypeAccessor typeAccessor,
                            final RequestFactory<Request<Request<?, Response>,Response>> requestFactory,
                            final MutatingApplicationLinkService applicationLinkService) {
        this.restUrlBuilder = restUrlBuilder;
        this.typeAccessor = typeAccessor;
        this.requestFactory = requestFactory;
        this.applicationLinkService = applicationLinkService;
    }

    protected final <T> T getUrlFor(final URI uri, final Class<T> tClass)
    {
        return restUrlBuilder.getUrlFor(uri, tClass);
    }

    protected ApplicationLinkEntity toApplicationLinkEntity(final ApplicationLink appLink)
    {
        return new ApplicationLinkEntity(appLink, createSelfLinkFor(appLink.getId()));
    }

    protected Link createSelfLinkFor(final ApplicationId appID) {
        return Link.self(applicationLinkService.createSelfLinkFor(appID));
    }

}
