package com.atlassian.plugins.rest.module.jersey;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;

import com.atlassian.plugin.AutowireCapablePlugin;
import com.atlassian.plugins.rest.module.OsgiComponentProviderFactory;
import com.atlassian.plugins.rest.module.ResourceConfigManager;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.header.InBoundHeaders;
import com.sun.jersey.core.header.OutBoundHeaders;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.core.spi.component.ProviderFactory;
import com.sun.jersey.core.spi.component.ProviderServices;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.core.spi.component.ioc.IoCProviderFactory;
import com.sun.jersey.core.spi.factory.ContextResolverFactory;
import com.sun.jersey.core.spi.factory.InjectableProviderFactory;
import com.sun.jersey.core.spi.factory.MessageBodyFactory;
import com.sun.jersey.core.util.FeaturesAndProperties;
import com.sun.jersey.spi.MessageBodyWorkers;
import com.sun.jersey.spi.inject.Errors;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import com.sun.jersey.spi.inject.Errors.Closure;
import com.sun.jersey.spi.inject.SingletonTypeInjectableProvider;

import org.osgi.framework.Bundle;

public class JerseyEntityHandler
{
    private final MessageBodyFactory messageBodyFactory;
    private final ResourceConfigManager resourceConfigManager;

    public JerseyEntityHandler(final AutowireCapablePlugin plugin, final Bundle bundle)
    {
        resourceConfigManager = new ResourceConfigManager(plugin, bundle);
        messageBodyFactory = Errors.processWithErrors(new Closure<MessageBodyFactory>()
        {
            public MessageBodyFactory f()
            {
                ResourceConfig config = resourceConfigManager.createResourceConfig(Collections.<String, Object>emptyMap(),
                        new String[0], Collections.<String>emptySet());

                IoCComponentProviderFactory provider = new OsgiComponentProviderFactory(config, plugin);
                final InjectableProviderFactory injectableFactory = new InjectableProviderFactory();

                ProviderFactory componentProviderFactory = new IoCProviderFactory(injectableFactory, provider);

                ProviderServices providerServices = new ProviderServices(
                        componentProviderFactory,
                        config.getClasses(),
                        config.getSingletons());

                // Allow injection of features and properties
                injectableFactory.add(new ContextInjectableProvider<FeaturesAndProperties>(FeaturesAndProperties.class, config));

                injectableFactory.add(new InjectableProvider<Context, Type>() {
                    public ComponentScope getScope() {
                        return ComponentScope.Singleton;
                    }

                    public Injectable<Injectable> getInjectable(ComponentContext ic, Context a, Type c) {
                        if (c instanceof ParameterizedType) {
                            ParameterizedType pt = (ParameterizedType)c;
                            if (pt.getRawType() == Injectable.class) {
                                if (pt.getActualTypeArguments().length == 1) {
                                    final Injectable<?> i = injectableFactory.getInjectable(
                                            a.annotationType(),
                                            ic,
                                            a,
                                            pt.getActualTypeArguments()[0],
                                            ComponentScope.PERREQUEST_UNDEFINED_SINGLETON);
                                    if (i == null)
                                        return null;
                                    return new Injectable<Injectable>() {
                                        public Injectable getValue() {
                                            return i;
                                        }
                                    };
                                }
                            }
                        }

                        return null;
                    }
                });

                injectableFactory.configure(providerServices);

                // Obtain all context resolvers
                final ContextResolverFactory crf = new ContextResolverFactory();
                crf.init(providerServices, injectableFactory);

                // Obtain all message body readers/writers
                final MessageBodyFactory messageBodyFactory = new MessageBodyFactory(providerServices, false);
                
                // Allow injection of message body context
                injectableFactory.add(new ContextInjectableProvider<MessageBodyWorkers>(
                        MessageBodyWorkers.class, messageBodyFactory));

                // Injection of Providers
                Providers providers = new Providers()
                {
                    public <T> MessageBodyReader<T> getMessageBodyReader(Class<T> c, Type t,
                                                                         Annotation[] as, MediaType m)
                    {
                        return messageBodyFactory.getMessageBodyReader(c, t, as, m);
                    }

                    public <T> MessageBodyWriter<T> getMessageBodyWriter(Class<T> c, Type t,
                                                                         Annotation[] as, MediaType m)
                    {
                        return messageBodyFactory.getMessageBodyWriter(c, t, as, m);
                    }

                    public <T extends Throwable> ExceptionMapper<T> getExceptionMapper(Class<T> c)
                    {
                        throw new IllegalArgumentException("This method is not supported on the client side");
                    }

                    public <T> ContextResolver<T> getContextResolver(Class<T> ct, MediaType m)
                    {
                        return crf.resolve(ct, m);
                    }
                };
                injectableFactory.add(
                        new ContextInjectableProvider<Providers>(
                                Providers.class, providers));

                // Initiate message body readers/writers
                messageBodyFactory.init();

                // Inject on all components
                Errors.setReportMissingDependentFieldOrMethod(true);
                componentProviderFactory.injectOnAllComponents();
                componentProviderFactory.injectOnProviderInstances(config.getSingletons());
                
                return messageBodyFactory;
            }
        });
    }
    
    /**
     * package protected constructor exposed for unit tests,
     * @see JerseyEntityHandler(AutowireCapablePlugin plugin, Bundle bundle)
     * @param msgBodyFactory
     * @param resourceConfigMgr
     */
    JerseyEntityHandler(MessageBodyFactory msgBodyFactory, ResourceConfigManager resourceConfigMgr)
    {
    	messageBodyFactory = msgBodyFactory;
    	resourceConfigManager = resourceConfigMgr;
    }

    public String marshall(Object entity, MediaType mediaType, Charset charset) throws IOException
    {
        Type entityType;
        if (entity instanceof GenericEntity)
        {
            final GenericEntity ge = (GenericEntity) entity;
            entityType = ge.getType();
            entity = ge.getEntity();
        }
        else
        {
            entityType = entity.getClass();
        }
        final Class entityClass = entity.getClass();

        MessageBodyWriter writer = messageBodyFactory.getMessageBodyWriter(entityClass, entityType, new Annotation[0], mediaType);
        if (writer == null)
        {
            throw new RuntimeException("Unable to find a message body writer for " + entityClass);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        writer.writeTo(entity, entityClass, entityType, new Annotation[0], mediaType, new OutBoundHeaders(), outputStream);

        try
        {
            return outputStream.toString(charset.name());
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException("Should never happen as we have already seen the " +
                    "Charset is supported as we have it as a parameter");
        }
    }

    public <T> T unmarshall(Class<T> entityClass, MediaType mediaType, InputStream entityStream,
                            Map<String, List<String>> responseHeaders) throws IOException
    {
        MessageBodyReader<T> reader = messageBodyFactory.getMessageBodyReader(entityClass, entityClass, new Annotation[0],
                mediaType);
        MultivaluedMap<String, String> headers = new InBoundHeaders();
        headers.putAll(responseHeaders);

        return reader.readFrom(entityClass, entityClass, new Annotation[0], mediaType, headers, entityStream);
    }

    public void destroy()
    {
        resourceConfigManager.destroy();
    }

    private static class ContextInjectableProvider<T> extends
            SingletonTypeInjectableProvider<Context, T>
    {

        ContextInjectableProvider(Type type, T instance)
        {
            super(type, instance);
        }
    }
}
