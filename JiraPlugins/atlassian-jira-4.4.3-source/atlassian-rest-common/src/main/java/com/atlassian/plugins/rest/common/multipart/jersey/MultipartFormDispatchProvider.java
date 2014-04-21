package com.atlassian.plugins.rest.common.multipart.jersey;

import com.atlassian.plugins.rest.common.interceptor.impl.DispatchProviderHelper;
import com.atlassian.plugins.rest.common.interceptor.impl.InterceptorChainBuilder;
import com.atlassian.plugins.rest.common.multipart.FilePart;
import com.atlassian.plugins.rest.common.multipart.MultipartForm;
import com.atlassian.plugins.rest.common.multipart.MultipartFormParam;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.server.impl.inject.InjectableValuesProvider;
import com.sun.jersey.server.impl.model.method.dispatch.AbstractResourceMethodDispatchProvider;
import com.sun.jersey.spi.dispatch.RequestDispatcher;
import com.sun.jersey.spi.inject.Injectable;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

/**
 * Dispatchs requests to methods with MultipartFormParam parameters
 *
 * @since 2.4
 */
@Provider
public class MultipartFormDispatchProvider extends AbstractResourceMethodDispatchProvider
{
    private static final String MULTIPART_FORM_PROPERTY = "com.atlassian.rest.multipart.form";

    private @Context InterceptorChainBuilder interceptorChainBuilder;

    @Override
    public RequestDispatcher create(AbstractResourceMethod abstractResourceMethod)
    {
        DispatchProviderHelper helper = new DispatchProviderHelper(interceptorChainBuilder);
        return helper.create(abstractResourceMethod, getInjectableValuesProvider(abstractResourceMethod));
    }

    @Override
    protected InjectableValuesProvider getInjectableValuesProvider(final AbstractResourceMethod method)
    {
        // We only return one if we find a MultipartFormParam annotated parameter
        for (Parameter param : method.getParameters())
        {
            for (Annotation annotation : param.getAnnotations())
            {
                if (annotation instanceof MultipartFormParam)
                {
                    // Here we return an object that will parse the form (done by MultipartFormMessageBodyReader when getEntity() is called)
                    return new InjectableValuesProvider(getInjectables(method))
                    {
                        @Override
                        public Object[] getInjectableValues(final HttpContext context)
                        {
                            if (!context.getProperties().containsKey(MULTIPART_FORM_PROPERTY))
                            {
                                context.getProperties().put(MULTIPART_FORM_PROPERTY, context.getRequest().getEntity(
                                        MultipartForm.class, MultipartForm.class, method.getAnnotations()));
                            }
                            return super.getInjectableValues(context);
                        }
                    };
                }
            }
        }
        return null;
    }

    private List<Injectable> getInjectables(AbstractResourceMethod method)
    {
        List<Injectable> is = new ArrayList<Injectable>(method.getParameters().size());
        for (int i = 0; i < method.getParameters().size(); i++)
        {
            Parameter parameter = method.getParameters().get(i);
            Injectable<?> injectable = null;
            // We don't support entities
            if (Parameter.Source.ENTITY == parameter.getSource())
            {
                return null;
            }
            for (Annotation annotation : parameter.getAnnotations())
            {
                if (annotation instanceof MultipartFormParam)
                {
                    // It's a multipart parameter, we get the injectable for it
                    injectable = getMultipartFormInjectable(parameter, (MultipartFormParam) annotation);
                }
            }
            if (injectable == null)
            {
                // This defaults back so that everything else, eg @HeaderParam, @Context etc can get injected
                injectable = getInjectableProviderContext().getInjectable(parameter, ComponentScope.PerRequest);
            }
            if (injectable == null)
            {
                return null;
            }
            is.add(injectable);
        }
        return is;
    }

    private Injectable<?> getMultipartFormInjectable(final Parameter parameter, final MultipartFormParam annotation)
    {
        // FilePart
        if (parameter.getParameterClass().equals(FilePart.class))
        {
            return new AbstractHttpContextInjectable<FilePart>()
            {
                public FilePart getValue(final HttpContext context)
                {
                    return ((MultipartForm) context.getProperties().get(MULTIPART_FORM_PROPERTY)).getFilePart(annotation.value());
                }
            };
        }
        // Collection of file parts
        if (Collection.class.isAssignableFrom(parameter.getParameterClass()))
        {
            return new AbstractHttpContextInjectable<Collection<FilePart>>()
            {
                public Collection<FilePart> getValue(final HttpContext context)
                {
                    Collection<FilePart> parts = ((MultipartForm) context.getProperties().get(MULTIPART_FORM_PROPERTY)).getFileParts(annotation.value());
                    if (parameter.getParameterClass().isAssignableFrom(Collection.class))
                    {
                        return parts;
                    }
                    else if (parameter.getParameterClass().isAssignableFrom(List.class))
                    {
                        return new ArrayList<FilePart>(parts);
                    }
                    else if (parameter.getParameterClass().isAssignableFrom(Set.class))
                    {
                        return new HashSet<FilePart>(parts);
                    }
                    return null;
                }
            };
        }
        return null;
    }
}
