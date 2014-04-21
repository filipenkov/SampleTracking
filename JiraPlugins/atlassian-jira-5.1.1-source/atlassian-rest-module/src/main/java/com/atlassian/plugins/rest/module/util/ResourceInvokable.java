package com.atlassian.plugins.rest.module.util;

import com.sun.jersey.core.reflection.AnnotatedMethod;
import net.sf.cglib.proxy.InvocationHandler;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.UriBuilder;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class used for creating dummy REST resource responses.
 */
public abstract class ResourceInvokable implements InvocationHandler
{
    protected Class<?> resourceClass;
    private final URI baseUri;

    public ResourceInvokable(final Class<?> resourceClass, final URI baseUri)
    {
        this.resourceClass = resourceClass;
        this.baseUri = baseUri;
    }

    protected Map<String, Object> buildParamMap(final AnnotatedMethod called, final Object[] args)
    {
        Map<String, Object> rv = new HashMap<String, Object>();

        Annotation[][] allParameterAnnotations = called.getParameterAnnotations();

        for (int i = 0; i < allParameterAnnotations.length; i++)
        {
            Annotation[] parameterAnnotations = allParameterAnnotations[i];

            for (Annotation annotation : parameterAnnotations)
            {
                if (annotation instanceof PathParam && args[i] != null)
                {
                    rv.put(((PathParam) annotation).value(), args[i]);
                }
            }
        }

        return rv;
    }

    protected URI getURI(final Method method, final Object[] args)
    {
        final UriBuilder builder = UriBuilder.fromUri(baseUri).path(resourceClass);
        if(new AnnotatedMethod(method).getAnnotation(Path.class) != null)
        {
            builder.path(method);
        }
        return builder.buildFromMap(buildParamMap(new AnnotatedMethod(method), args));
    }

}
