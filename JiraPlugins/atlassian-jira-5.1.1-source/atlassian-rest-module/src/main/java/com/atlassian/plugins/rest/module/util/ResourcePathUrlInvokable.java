package com.atlassian.plugins.rest.module.util;

import java.lang.reflect.Method;
import java.net.URI;

/**
 * Dummy response used to generate URIs for REST resources.
 */
class ResourcePathUrlInvokable extends ResourceInvokable
{
    ResourcePathUrlInvokable(final Class<?> resourceClass, final URI baseUri)
    {
        super(resourceClass, baseUri);
    }

    public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable
    {
        return new GeneratedURIResponse(getURI(method, args));
    }
}
