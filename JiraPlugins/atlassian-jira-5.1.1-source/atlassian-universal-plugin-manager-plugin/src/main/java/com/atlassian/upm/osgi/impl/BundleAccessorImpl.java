package com.atlassian.upm.osgi.impl;

import javax.annotation.Nullable;

import com.atlassian.upm.osgi.Bundle;
import com.atlassian.upm.osgi.Bundle.HeaderClause;
import com.atlassian.upm.osgi.BundleAccessor;
import com.atlassian.upm.osgi.PackageAccessor;
import com.atlassian.upm.osgi.Service;

import com.google.common.base.Predicate;

import org.osgi.framework.BundleContext;
import org.springframework.osgi.context.BundleContextAware;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.or;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.filter;

public final class BundleAccessorImpl implements BundleAccessor, BundleContextAware
{
    private final PackageAccessor packageAccessor;
    private BundleContext bundleContext;

    public BundleAccessorImpl(PackageAccessor packageAccessor)
    {
        this.packageAccessor = checkNotNull(packageAccessor, "packageAccessor");
    }

    public void setBundleContext(BundleContext bundleContext)
    {
        this.bundleContext = checkNotNull(bundleContext, "bundleContext");
    }

    public Iterable<Bundle> getBundles()
    {
        return BundleImpl.wrap(packageAccessor).fromArray(bundleContext.getBundles());
    }

    public Iterable<Bundle> getBundles(@Nullable String term)
    {
        return term == null ?
            getBundles() :
            copyOf(filter(getBundles(), bundleContains(term)));
    }

    @Nullable
    public Bundle getBundle(long bundleId)
    {
        return BundleImpl.wrap(packageAccessor).fromSingleton(bundleContext.getBundle(bundleId));
    }

    private static final Predicate<Bundle> bundleContains(final String term)
    {
        final Predicate<String> stringContains = new Predicate<String>()
        {
            private final String lowerCaseTerm = term.toLowerCase();
            public boolean apply(@Nullable String s)
            {
                return s.toLowerCase().contains(lowerCaseTerm);
            }
        };

        final Predicate<Bundle> unparsedHeadersContain = new Predicate<Bundle>()
        {
            public boolean apply(@Nullable Bundle bundle)
            {
                return any(bundle.getUnparsedHeaders().values(), stringContains);
            }
        };

        final Predicate<Bundle> parsedHeadersContain = new Predicate<Bundle>()
        {
            public boolean apply(@Nullable Bundle bundle)
            {
                return any(bundle.getParsedHeaders().values(), parsedHeaderContains);
            }

            private final Predicate<Iterable<HeaderClause>> parsedHeaderContains = new Predicate<Iterable<HeaderClause>>()
            {
                public boolean apply(@Nullable Iterable<HeaderClause> headers)
                {
                    return any(headers, headerClauseContains);
                }

                private final Predicate<HeaderClause> headerClauseContains = new Predicate<HeaderClause>()
                {
                    public boolean apply(@Nullable HeaderClause headerClause)
                    {
                        return stringContains.apply(headerClause.getPath());
                    }
                };
            };
        };

        final Predicate<Bundle> servicesContain = new Predicate<Bundle>()
        {
            public boolean apply(@Nullable Bundle bundle)
            {
                return any(concat(bundle.getRegisteredServices(), bundle.getServicesInUse()), serviceContains);
            }

            private final Predicate<Service> serviceContains = new Predicate<Service>()
            {
                public boolean apply(@Nullable Service service)
                {
                    return any(service.getObjectClasses(), stringContains);
                }
            };
        };

        return or(unparsedHeadersContain, parsedHeadersContain, servicesContain);
    }
}
