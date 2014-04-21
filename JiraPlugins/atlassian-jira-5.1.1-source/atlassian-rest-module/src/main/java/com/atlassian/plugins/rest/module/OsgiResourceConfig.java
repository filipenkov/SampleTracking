package com.atlassian.plugins.rest.module;

import com.atlassian.plugins.rest.common.error.jersey.NotFoundExceptionMapper;
import com.atlassian.plugins.rest.common.error.jersey.ThrowableExceptionMapper;
import com.atlassian.plugins.rest.common.json.JacksonJsonProviderFactory;
import com.atlassian.plugins.rest.common.security.jersey.SecurityExceptionMapper;
import com.atlassian.plugins.rest.common.security.jersey.SysadminOnlyResourceFilter;
import com.atlassian.plugins.rest.module.scanner.AnnotatedClassScanner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;
import org.osgi.framework.Bundle;

import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * An implementation of {@link ResourceConfig} that scans a given Osgi Bundle for Jersey resources ({#link Provider} and {@link Path}.
 */
class OsgiResourceConfig extends DefaultResourceConfig
{
    private final Bundle bundle;

    private final Set<Class<?>> classes = new HashSet<Class<?>>()
    {{
            add(NotFoundExceptionMapper.class);
            add(SecurityExceptionMapper.class);
            add(ThrowableExceptionMapper.class);
            add(SysadminOnlyResourceFilter.class);
        }};

    private final Set<Object> instances;

    private Set<Class<?>> scannedClasses;
    private final String[] packages;

    OsgiResourceConfig(Bundle bundle,
                       Set<String> packages,
                       Collection<? extends ContainerRequestFilter> containerRequestFilters,
                       Collection<? extends ContainerResponseFilter> containerResponseFilters,
                       Collection<? extends ResourceFilterFactory> resourceFilterFactories,
                       Collection<?> providers)
    {
        this.packages = packages.toArray(new String[packages.size()]);
        this.bundle = Preconditions.checkNotNull(bundle);

        // adds "filters" to Jersey
        getProperties().put(PROPERTY_CONTAINER_REQUEST_FILTERS, Lists.newLinkedList(containerRequestFilters));
        getProperties().put(PROPERTY_CONTAINER_RESPONSE_FILTERS, Lists.newLinkedList(containerResponseFilters));
        getProperties().put(PROPERTY_RESOURCE_FILTER_FACTORIES, Lists.newLinkedList(resourceFilterFactories));

        this.instances = Sets.newHashSet(Preconditions.checkNotNull(providers));
        this.instances.add(new JacksonJsonProviderFactory().create());

        addInstancesClassesToClasses();
    }

    private void addInstancesClassesToClasses()
    {
        for (Object o : instances)
        {
            classes.add(o.getClass());
        }
    }

    @Override
    public synchronized Set<Class<?>> getClasses()
    {
        if (scannedClasses == null)
        {
            scannedClasses = scanForAnnotatedClasses();
            classes.addAll(scannedClasses);
        }

        return classes;
    }

    private Set<Class<?>> scanForAnnotatedClasses()
    {
        return new AnnotatedClassScanner(bundle, Provider.class, Path.class).scan(packages);
    }

    public Set<?> getInstances()
    {
        return Collections.unmodifiableSet(instances);
    }
}
