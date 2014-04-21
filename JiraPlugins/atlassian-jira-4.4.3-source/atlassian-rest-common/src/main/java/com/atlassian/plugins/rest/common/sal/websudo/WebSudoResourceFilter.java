package com.atlassian.plugins.rest.common.sal.websudo;

import static com.google.common.base.Preconditions.checkNotNull;

import com.atlassian.sal.api.websudo.WebSudoNotRequired;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;

import java.lang.reflect.Method;

/**
 * This is a Jersey resource filter that checks whether the resource requires or opts out of WebSudo protection.
 * The check is based on annotations ({@link com.atlassian.sal.api.websudo.WebSudoRequired} and
 * {@link com.atlassian.sal.api.websudo.WebSudoNotRequired}) that can be applied on the method, class and package level.
 * <p/>
 * Annotations on more specific elements override annotations applied to more general elements (in ascending order from specific to general):
 * <ul>
 * <li>Method level</li>
 * <li>Class level</li>
 * <li>Package level</li>
 * </li>
 */
final class WebSudoResourceFilter implements ResourceFilter, ContainerRequestFilter
{
    private final AbstractMethod abstractMethod;
    private final WebSudoResourceContext webSudoResourceContext;

    public WebSudoResourceFilter(final AbstractMethod abstractMethod, final WebSudoResourceContext webSudoResourceContext)
    {
        this.abstractMethod = checkNotNull(abstractMethod);
        this.webSudoResourceContext = checkNotNull(webSudoResourceContext);
    }

    public ContainerRequestFilter getRequestFilter()
    {
        return this;
    }

    public ContainerResponseFilter getResponseFilter()
    {
        return null;
    }

    public ContainerRequest filter(final ContainerRequest request)
    {
        if (requiresWebSudo() && webSudoResourceContext.shouldEnforceWebSudoProtection())
        {
            throw new WebSudoRequiredException("This resource requires WebSudo.");
        }
        return request;
    }

    private boolean requiresWebSudo()
    {
        final Method m = abstractMethod.getMethod();
        if (null != m && m.getAnnotation(WebSudoRequired.class) != null)
        {
            return true;
        }
        if (null != m && m.getAnnotation(WebSudoNotRequired.class) != null)
        {
            return false;
        }

        final AbstractResource resource = abstractMethod.getResource();
        if (resource.isAnnotationPresent(WebSudoRequired.class))
        {
            return true;
        }
        if (resource.isAnnotationPresent(WebSudoNotRequired.class))
        {
            return false;
        }

        final Package p = abstractMethod.getResource().getResourceClass().getPackage();
        if (p.getAnnotation(WebSudoRequired.class) != null)
        {
            return true;
        }
        if (p.getAnnotation(WebSudoNotRequired.class) != null)
        {
            return false;
        }
        return false;
    }
}