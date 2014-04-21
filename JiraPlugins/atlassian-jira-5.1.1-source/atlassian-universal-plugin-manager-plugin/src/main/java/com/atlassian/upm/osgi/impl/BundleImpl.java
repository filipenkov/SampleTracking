package com.atlassian.upm.osgi.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;

import javax.annotation.Nullable;

import com.atlassian.upm.Functions.Function2;
import com.atlassian.upm.osgi.Bundle;
import com.atlassian.upm.osgi.Package;
import com.atlassian.upm.osgi.PackageAccessor;
import com.atlassian.upm.osgi.Service;
import com.atlassian.upm.osgi.Version;
import com.atlassian.upm.osgi.VersionRange;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.apache.sling.commons.osgi.ManifestHeader;
import org.apache.sling.commons.osgi.ManifestHeader.Entry;

import static com.atlassian.upm.Functions.curry;
import static com.atlassian.upm.osgi.Bundle.State.ACTIVE;
import static com.atlassian.upm.osgi.Bundle.State.INSTALLED;
import static com.atlassian.upm.osgi.Bundle.State.RESOLVED;
import static com.atlassian.upm.osgi.Bundle.State.STARTING;
import static com.atlassian.upm.osgi.Bundle.State.STOPPING;
import static com.atlassian.upm.osgi.Bundle.State.UNINSTALLED;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.in;
import static com.google.common.base.Predicates.not;
import static com.google.common.base.Suppliers.memoize;
import static com.google.common.collect.Maps.filterKeys;
import static java.lang.String.format;
import static org.apache.sling.commons.osgi.ManifestHeader.parse;
import static org.osgi.framework.Constants.BUNDLE_CLASSPATH;
import static org.osgi.framework.Constants.BUNDLE_NAME;
import static org.osgi.framework.Constants.BUNDLE_NATIVECODE;
import static org.osgi.framework.Constants.BUNDLE_REQUIREDEXECUTIONENVIRONMENT;
import static org.osgi.framework.Constants.DYNAMICIMPORT_PACKAGE;
import static org.osgi.framework.Constants.EXPORT_PACKAGE;
import static org.osgi.framework.Constants.FRAGMENT_HOST;
import static org.osgi.framework.Constants.IMPORT_PACKAGE;
import static org.osgi.framework.Constants.REQUIRE_BUNDLE;
import static org.osgi.framework.Constants.VERSION_ATTRIBUTE;

/**
 * A wrapper class around OSGi bundles
 */
public final class BundleImpl implements Bundle
{
    public static class HeaderClauseImpl implements HeaderClause
    {
        private final String path;
        private final Map<String, String> parameters;
        private final Function<HeaderClause, Package> getPackageFn;

        HeaderClauseImpl(Function<BundleImpl.HeaderClause, Package> getPackageFn, Entry entry)
        {
            ImmutableMap.Builder<String, String> propertiesBuilder = ImmutableMap.builder();
            for (ManifestHeader.NameValuePair attribute : entry.getAttributes())
            {
                propertiesBuilder.put(attribute.getName(), attribute.getValue());
            }
            for (ManifestHeader.NameValuePair directive : entry.getDirectives())
            {
                propertiesBuilder.put(directive.getName(), directive.getValue());
            }
            this.getPackageFn = getPackageFn;
            this.path = checkNotNull(entry.getValue());
            this.parameters = propertiesBuilder.build();
        }

        public String getPath()
        {
            return path;
        }

        public Map<String, String> getParameters()
        {
            return parameters;
        }

        public Package getReferencedPackage()
        {
            return getPackageFn.apply(this);
        }
    }

    private final org.osgi.framework.Bundle bundle;
    private final PackageAccessor packageAccessor;
    private final Wrapper2<Function<HeaderClause, Package>, Entry, HeaderClause> wrapHeaderClause;
    private final Wrapper2<String, String, Iterable<HeaderClause>> parseHeader;
    private static final Map<Integer, State> states =
        ImmutableMap.<Integer, State>builder()
            .put(org.osgi.framework.Bundle.UNINSTALLED, UNINSTALLED)
            .put(org.osgi.framework.Bundle.INSTALLED, INSTALLED)
            .put(org.osgi.framework.Bundle.RESOLVED, RESOLVED)
            .put(org.osgi.framework.Bundle.STARTING, STARTING)
            .put(org.osgi.framework.Bundle.STOPPING, STOPPING)
            .put(org.osgi.framework.Bundle.ACTIVE, ACTIVE)
            .build();

    private final Supplier<Map<String, String>> unparsedHeaders =
        memoize(new Supplier<Map<String, String>>()
        {
            public Map<String, String> get()
            {
                return ImmutableMap.copyOf(filterKeys(getHeaders(), not(parseable)));
            }
        });

    private final Supplier<Map<String, Iterable<HeaderClause>>> parsedHeaders =
        memoize(new Supplier<Map<String, Iterable<HeaderClause>>>()
        {
            public Map<String, Iterable<HeaderClause>> get()
            {
                return parseHeader.fromSingletonValuedMap(filterKeys(getHeaders(), parseable));
            }
        });

    BundleImpl(org.osgi.framework.Bundle bundle, PackageAccessor packageAccessor)
    {
        this.bundle = bundle;
        this.packageAccessor = packageAccessor;
        this.wrapHeaderClause = new Wrapper2<Function<HeaderClause, com.atlassian.upm.osgi.Package>, Entry, HeaderClause>(format("bundle-%d.headerClause", getId()))
            {
                protected HeaderClause wrap(Function<HeaderClause, Package> getPackageFn, Entry headerEntry)
                {
                    return new HeaderClauseImpl(getPackageFn, headerEntry);
                }
            };
        this.parseHeader = new Wrapper2<String, String, Iterable<HeaderClause>>(format("bundle-%d.header", getId()))
            {
                protected Iterable<HeaderClause> wrap(@Nullable String headerName, @Nullable String headerEntries)
                {
                    return wrapHeaderClause.fromArray(
                        curry(getPackageFn).apply(headerName),
                        parse(headerEntries).getEntries());
                }
            };
    }

    public State getState()
    {
        return checkNotNull(states.get(bundle.getState()), "state");
    }

    public Map<String, String> getUnparsedHeaders()
    {
        return unparsedHeaders.get();
    }

    public Map<String, Iterable<HeaderClause>> getParsedHeaders()
    {
        return parsedHeaders.get();
    }

    public long getId()
    {
        return bundle.getBundleId();
    }

    @Nullable
    public URI getLocation()
    {
        try
        {
            return new URI(bundle.getLocation());
        }
        catch (URISyntaxException e)
        {
            return null;
        }
    }

    public Iterable<Service> getRegisteredServices()
    {
        return ServiceImpl.wrap(packageAccessor).fromArray(bundle.getRegisteredServices());
    }

    public Iterable<Service> getServicesInUse()
    {
        return ServiceImpl.wrap(packageAccessor).fromArray(bundle.getServicesInUse());
    }

    public String getSymbolicName()
    {
        return bundle.getSymbolicName();
    }

    @Nullable
    public String getName()
    {
        return getUnparsedHeaders().get(BUNDLE_NAME);
    }

    public Version getVersion()
    {
        return Versions.wrap.fromSingleton(bundle.getVersion());
    }

    static Wrapper<org.osgi.framework.Bundle, Bundle> wrap(final PackageAccessor packageAccessor)
    {
        return new Wrapper<org.osgi.framework.Bundle, Bundle>("bundle")
        {
            protected Bundle wrap(org.osgi.framework.Bundle bundle)
            {
                return new BundleImpl(bundle, packageAccessor);
            }
        };
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getHeaders()
    {
        Dictionary<String, String> headers = bundle.getHeaders();
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        for (Enumeration<String> keys = headers.keys(); keys.hasMoreElements();)
        {
            String key = keys.nextElement();
            String value = headers.get(key);
            builder.put(key, value);
        }
        return builder.build();
    }

    /**
     * Non-standard OSGi headers are missing constants from {@code org.osgi.framework.Constant}.
     * See http://www.osgi.org/Specifications/ReferenceHeaders for complete list of standard headers.
     */
    private static final Predicate<String> parseable = in(ImmutableSet.of(
        BUNDLE_CLASSPATH,
        BUNDLE_NATIVECODE,
        BUNDLE_REQUIREDEXECUTIONENVIRONMENT,
        DYNAMICIMPORT_PACKAGE,
        EXPORT_PACKAGE,
        FRAGMENT_HOST,
        "Ignore-Package",
        IMPORT_PACKAGE,
        "Private-Package",
        REQUIRE_BUNDLE
    ));

    private final Function2<String, HeaderClause, Package> getPackageFn =
        new Function2<String, HeaderClause, Package>()
        {
            public Package apply(@Nullable String headerName, @Nullable HeaderClause headerClause)
            {
                if (IMPORT_PACKAGE.equals(headerName) || DYNAMICIMPORT_PACKAGE.equals(headerName))
                {
                    String versionRange = headerClause.getParameters().get(VERSION_ATTRIBUTE);
                    return packageAccessor.getImportedPackage(getId(), headerClause.getPath(),
                        VersionRange.fromString(versionRange == null ? "0" : versionRange));
                }

                if (EXPORT_PACKAGE.equals(headerName))
                {
                    String version = headerClause.getParameters().get(VERSION_ATTRIBUTE);
                    return packageAccessor.getExportedPackage(getId(), headerClause.getPath(),
                        Versions.fromString(version == null ? "0" : version));
                }

                return null;
            }
        };
}