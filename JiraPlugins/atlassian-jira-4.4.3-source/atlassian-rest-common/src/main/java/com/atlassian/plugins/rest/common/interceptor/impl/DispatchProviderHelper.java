package com.atlassian.plugins.rest.common.interceptor.impl;

import com.atlassian.plugins.rest.common.interceptor.MethodInvocation;
import com.atlassian.plugins.rest.common.interceptor.ResourceInterceptor;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.server.impl.ResponseBuilderImpl;
import com.sun.jersey.server.impl.inject.InjectableValuesProvider;
import com.sun.jersey.server.impl.model.method.dispatch.ResourceJavaMethodDispatcher;
import com.sun.jersey.spi.dispatch.RequestDispatcher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;

/**
 * Helps invoke the appropriate method, wrapping the execution in an interceptor chain.
 *
 * This is a private class and used by the  {@link EntityParamDispatchProviderWrapper}
 * and {@link com.atlassian.plugins.rest.common.multipart.jersey.MultipartFormDispatchProvider}
 * which both will use this helper class to wrap calls to rest methods with interceptors.
 *
 * @since 2.0
 */
public class DispatchProviderHelper
{
    private final InterceptorChainBuilder interceptorChainBuilder;

    public DispatchProviderHelper(InterceptorChainBuilder interceptorChainBuilder)
    {
        this.interceptorChainBuilder = interceptorChainBuilder;
    }

    public RequestDispatcher create(AbstractResourceMethod abstractResourceMethod, InjectableValuesProvider pp)
    {
        if (pp == null)
        {
            return null;
        }

        final List<ResourceInterceptor> interceptors = interceptorChainBuilder.getResourceInterceptorsForMethod(abstractResourceMethod.getMethod());

        // TODO
        // Strictly speaking a GET request can contain an entity in the
        // message body, but this is likely to be not implemented by many
        // servers and clients, but should we support it?
        boolean requireReturnOfRepresentation =
                "GET".equals(abstractResourceMethod.getHttpMethod());

        Class<?> returnType = abstractResourceMethod.getMethod().getReturnType();
        if (Response.class.isAssignableFrom(returnType))
        {
            return new ResponseOutInvoker(abstractResourceMethod, pp, interceptors);
        }
        else if (returnType != void.class)
        {
            if (returnType == Object.class || GenericEntity.class.isAssignableFrom(returnType))
            {
                return new ObjectOutInvoker(abstractResourceMethod, pp, interceptors);
            }
            else
            {
                return new TypeOutInvoker(abstractResourceMethod, pp, interceptors);
            }
        }
        else if (requireReturnOfRepresentation)
        {
            return null;
        }
        else
        {
            return new VoidOutInvoker(abstractResourceMethod, pp, interceptors);
        }
    }

    private static void invokeMethodWithInterceptors(List<ResourceInterceptor> originalInterceptors,
                                                     AbstractResourceMethod method,
                                                     Object resource,
                                                     HttpContext httpContext,
                                                     Object[] params,
                                                     final MethodInvoker methodInvocation) throws IllegalAccessException, InvocationTargetException
    {
        ResourceInterceptor lastInterceptor = new ResourceInterceptor()
        {
            public void intercept(MethodInvocation invocation) throws IllegalAccessException, InvocationTargetException
            {
                methodInvocation.invoke();
            }
        };

        List<ResourceInterceptor> interceptors = new ArrayList<ResourceInterceptor>(originalInterceptors);
        interceptors.add(lastInterceptor);

        MethodInvocation inv = new DefaultMethodInvocation(resource, method, httpContext, interceptors, params);
        inv.invoke();
    }

    private static interface MethodInvoker
    {
        void invoke() throws IllegalAccessException, InvocationTargetException;
    }


    private static abstract class EntityParamInInvoker extends ResourceJavaMethodDispatcher
    {
        private final InjectableValuesProvider pp;
        final AbstractResourceMethod abstractResourceMethod;
        final List<ResourceInterceptor> interceptors;

        EntityParamInInvoker(AbstractResourceMethod abstractResourceMethod,
                             InjectableValuesProvider pp,
                             List<ResourceInterceptor> interceptors)
        {
            super(abstractResourceMethod);
            this.pp = pp;
            this.abstractResourceMethod = abstractResourceMethod;
            this.interceptors = interceptors;
        }

        final Object[] getParams(HttpContext context)
        {
            return pp.getInjectableValues(context);
        }

    }

    private static final class VoidOutInvoker extends EntityParamInInvoker
    {


        VoidOutInvoker(AbstractResourceMethod abstractResourceMethod,
                       InjectableValuesProvider pp,
                       List<ResourceInterceptor> interceptors)
        {
            super(abstractResourceMethod, pp, interceptors);
        }

        public void _dispatch(final Object resource, HttpContext context)
                throws IllegalAccessException, InvocationTargetException
        {
            final Object[] params = getParams(context);
            invokeMethodWithInterceptors(interceptors, abstractResourceMethod, resource, context, params, new MethodInvoker()
            {
                public void invoke() throws IllegalAccessException, InvocationTargetException
                {
                    method.invoke(resource, params);
                }
            });
        }
    }

    private static final class TypeOutInvoker extends EntityParamInInvoker
    {
        private final Type t;

        TypeOutInvoker(AbstractResourceMethod abstractResourceMethod,
                       InjectableValuesProvider pp,
                       List<ResourceInterceptor> interceptors)
        {
            super(abstractResourceMethod, pp, interceptors);
            this.t = abstractResourceMethod.getMethod().getGenericReturnType();
        }

        public void _dispatch(final Object resource, final HttpContext context)
                throws IllegalAccessException, InvocationTargetException
        {
            final Object[] params = getParams(context);

            invokeMethodWithInterceptors(interceptors, abstractResourceMethod, resource, context, params, new MethodInvoker()
            {
                public void invoke() throws IllegalAccessException, InvocationTargetException
                {
                    Object o = method.invoke(resource, params);
                    if (o != null)
                    {
                        Response r = new ResponseBuilderImpl().
                                entityWithType(o, t).status(200).build();
                        context.getResponse().setResponse(r);
                    }
                }
            });
        }
    }

    private static final class ResponseOutInvoker extends EntityParamInInvoker
    {
        ResponseOutInvoker(AbstractResourceMethod abstractResourceMethod,
                           InjectableValuesProvider pp,
                           List<ResourceInterceptor> interceptors)
        {
            super(abstractResourceMethod, pp, interceptors);
        }

        public void _dispatch(final Object resource, final HttpContext context)
                throws IllegalAccessException, InvocationTargetException
        {
            final Object[] params = getParams(context);

            invokeMethodWithInterceptors(interceptors, abstractResourceMethod, resource, context, params, new MethodInvoker()
            {
                public void invoke() throws IllegalAccessException, InvocationTargetException
                {
                    Response r = (Response) method.invoke(resource, params);
                    if (r != null)
                    {
                        context.getResponse().setResponse(r);
                    }
                }
            });

        }
    }

    private static final class ObjectOutInvoker extends EntityParamInInvoker
    {
        ObjectOutInvoker(AbstractResourceMethod abstractResourceMethod,
                         InjectableValuesProvider pp,
                         List<ResourceInterceptor> interceptors)
        {
            super(abstractResourceMethod, pp, interceptors);
        }

        public void _dispatch(final Object resource, final HttpContext context)
                throws IllegalAccessException, InvocationTargetException
        {
            final Object[] params = getParams(context);

            invokeMethodWithInterceptors(interceptors, abstractResourceMethod, resource, context, params, new MethodInvoker()
            {
                public void invoke() throws IllegalAccessException, InvocationTargetException
                {
                    Object o = method.invoke(resource, params);

                    if (o instanceof Response)
                    {
                        Response r = (Response) o;
                        context.getResponse().setResponse(r);
                    }
                    else if (o != null)
                    {
                        Response r = new ResponseBuilderImpl().status(200).entity(o).build();
                        context.getResponse().setResponse(r);
                    }
                }
            });


        }
    }
}
