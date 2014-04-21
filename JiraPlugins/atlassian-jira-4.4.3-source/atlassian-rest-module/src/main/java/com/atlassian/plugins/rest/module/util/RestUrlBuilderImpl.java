package com.atlassian.plugins.rest.module.util;

import com.atlassian.plugins.rest.common.util.RestUrlBuilder;
import org.springframework.util.Assert;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;
import java.net.URI;

/**
 * @since   2.2
 */
public class RestUrlBuilderImpl implements RestUrlBuilder
{
    public RestUrlBuilderImpl()
    {
        /**
         * IMPLEMENTATION NOTE:
         *
         * <p>
         * We're forcing jsr311 to initialize itself now that the context
         * classloader is still set to the rest bundle (which has access to
         * {@link com.sun.ws.rs.ext.RuntimeDelegateImpl}.
         * </p>
         * <p>
         * If we wait for it to lazily initialize itself, we can (and in fact,
         * we did) run into trouble when
         * {@link #getUrlFor(java.net.URI, Class)} is called by a different
         * plugin. In that scenario, by the time the caller then invokes a
         * method on the returned cglib-generated proxy instance which
         * triggers initialization of the jsr311 library, we get a
         * {@link ClassNotFoundException} because
         * {@link javax.ws.rs.ext.FactoryFinder#newInstance(String, ClassLoader)}
         * relies on the context classloader to have access to
         * {@link com.sun.ws.rs.ext.RuntimeDelegateImpl}, which it doesn't
         * because that invocation is performed inside the calling plugin.
         * </p>
         * <p>
         * It seems likely the above problem only applies to jsr311-1.0 (the
         * one atlassian.jersey-library ships with) and is fixed in 1.1.1, so
         * once we upgrade jersey, we should be able to get rid of this hack.
         */
        RuntimeDelegate.getInstance();
    }

    public URI getURI(Response resource)
    {
        if (resource instanceof GeneratedURIResponse)
        {
            return ((GeneratedURIResponse) resource).getURI();
        }
        else
        {
            throw new IllegalArgumentException("Supplied response is not a generated one");
        }
    }

    public <T> T getUrlFor(URI baseUri, Class<T> resourceClass)
    {
        Assert.notNull(resourceClass, "resourceClass cannot be null");
        Assert.notNull(baseUri, "baseUri cannot be null");
        return ProxyUtils.create(resourceClass, new ResourcePathUrlInvokable(resourceClass, baseUri));
    }
}
