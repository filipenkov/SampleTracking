package com.atlassian.plugins.rest.module;

import com.atlassian.plugin.AutowireCapablePlugin;
import com.atlassian.plugins.rest.common.expand.SelfExpandingExpander;
import com.atlassian.plugins.rest.common.expand.interceptor.ExpandInterceptor;
import com.atlassian.plugins.rest.common.expand.resolver.ChainingEntityExpanderResolver;
import com.atlassian.plugins.rest.common.expand.resolver.CollectionEntityExpanderResolver;
import com.atlassian.plugins.rest.common.expand.resolver.EntityExpanderResolver;
import com.atlassian.plugins.rest.common.expand.resolver.ExpandConstraintEntityExpanderResolver;
import com.atlassian.plugins.rest.common.expand.resolver.IdentityEntityExpanderResolver;
import com.atlassian.plugins.rest.common.expand.resolver.ListWrapperEntityExpanderResolver;
import com.atlassian.plugins.rest.common.filter.ExtensionJerseyFilter;
import com.atlassian.plugins.rest.common.interceptor.impl.InterceptorChainBuilderProvider;
import com.atlassian.plugins.rest.module.expand.resolver.PluginEntityExpanderResolver;
import com.atlassian.plugins.rest.module.filter.AcceptHeaderJerseyMvcFilter;
import com.atlassian.plugins.rest.module.json.JsonWithPaddingResponseFilter;
import com.google.common.collect.Lists;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.server.impl.model.method.dispatch.ResourceMethodDispatchProvider;
import com.sun.jersey.spi.container.ResourceFilterFactory;
import com.sun.jersey.spi.inject.InjectableProvider;
import com.sun.jersey.spi.template.TemplateProcessor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.ext.MessageBodyReader;

public class ResourceConfigManager
{
    private final OsgiServiceAccessor<ResourceFilterFactory> resourceFilterFactories;
    private final OsgiServiceAccessor<InjectableProvider> injectableProviders;
    private final OsgiServiceAccessor<TemplateProcessor> templateProcessors;
    private final OsgiServiceAccessor<MessageBodyReader> messageBodyReaders;
    private final OsgiServiceAccessor<ResourceMethodDispatchProvider> dispatchProviders;
    private final AutowireCapablePlugin plugin;
    private final Bundle bundle;

    public ResourceConfigManager(AutowireCapablePlugin plugin, Bundle bundle)
    {
        this.plugin = plugin;
        this.bundle = bundle;
        BundleContext bundleContext = bundle.getBundleContext();

        // looking up resource filters
        resourceFilterFactories = new OsgiServiceAccessor<ResourceFilterFactory>(ResourceFilterFactory.class, bundleContext, new OsgiFactory<ResourceFilterFactory>()
        {
            public ResourceFilterFactory getInstance(BundleContext bundleContext, ServiceReference serviceReference)
            {
                return new OsgiServiceReferenceResourceFilterFactory(bundleContext, serviceReference);
            }
        });

        // looking up injectable providers
        injectableProviders = new OsgiServiceAccessor<InjectableProvider>(InjectableProvider.class, bundleContext, new OsgiFactory<InjectableProvider>()
        {
            public InjectableProvider getInstance(BundleContext bundleContext, ServiceReference serviceReference)
            {
                return (InjectableProvider) bundleContext.getService(serviceReference);
            }
        });

        templateProcessors = new OsgiServiceAccessor<TemplateProcessor>(TemplateProcessor.class, bundleContext, new OsgiFactory<TemplateProcessor>()
        {
            public TemplateProcessor getInstance(BundleContext bundleContext, ServiceReference serviceReference)
            {
                return (TemplateProcessor) bundleContext.getService(serviceReference);
            }
        });

        messageBodyReaders = new OsgiServiceAccessor<MessageBodyReader>(MessageBodyReader.class, bundleContext, new OsgiFactory<MessageBodyReader>()
        {
            public MessageBodyReader getInstance(final BundleContext bundleContext, final ServiceReference serviceReference)
            {
                return (MessageBodyReader) bundleContext.getService(serviceReference);
            }
        });

        dispatchProviders = new OsgiServiceAccessor<ResourceMethodDispatchProvider>(ResourceMethodDispatchProvider.class, bundleContext, new OsgiFactory<ResourceMethodDispatchProvider>()
        {
            public ResourceMethodDispatchProvider getInstance(final BundleContext bundleContext, final ServiceReference serviceReference)
            {
                return (ResourceMethodDispatchProvider) bundleContext.getService(serviceReference);
            }
        });
    }

    public ResourceConfig createResourceConfig(Map<String, Object> props, String[] excludes, Set<String> packages)
    {
        // get the excludes parameter
        final Collection<String> excludesCollection = excludes != null ? Arrays.asList(excludes) : Collections.<String>emptyList();

        final EntityExpanderResolver expanderResolver = new ChainingEntityExpanderResolver(Lists.<EntityExpanderResolver>newArrayList(
                new PluginEntityExpanderResolver(plugin),
                new CollectionEntityExpanderResolver(),
                new ListWrapperEntityExpanderResolver(),
                new ExpandConstraintEntityExpanderResolver(),
                new SelfExpandingExpander.Resolver(),
                new IdentityEntityExpanderResolver()
        ));

        final Collection<Object> providers = Lists.newLinkedList();
        providers.addAll(injectableProviders.get());
        providers.addAll(templateProcessors.get());
        providers.addAll(messageBodyReaders.get());
        providers.addAll(dispatchProviders.get());

        providers.add(new InterceptorChainBuilderProvider(plugin, new ExpandInterceptor(expanderResolver)));

        return new OsgiResourceConfig(bundle, packages, 
                Lists.newArrayList(new ExtensionJerseyFilter(excludesCollection), new AcceptHeaderJerseyMvcFilter()), // request filters
                Lists.newArrayList(new JsonWithPaddingResponseFilter()), // response filters
                resourceFilterFactories.get(), // resource filters
                providers); // provider instances
    }

    public void destroy()
    {
        resourceFilterFactories.release();
        injectableProviders.release();
        templateProcessors.release();
        messageBodyReaders.release();
        dispatchProviders.release();
    }


}
