package com.atlassian.upm.osgi.rest.representations;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import javax.annotation.Nullable;

import com.atlassian.upm.osgi.Bundle;
import com.atlassian.upm.osgi.Bundle.HeaderClause;
import com.atlassian.upm.osgi.Bundle.State;
import com.atlassian.upm.osgi.impl.Wrapper;
import com.atlassian.upm.osgi.impl.Wrapper2;
import com.atlassian.upm.rest.UpmUriBuilder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A representation for OSGi bundle objects
 */
public final class BundleRepresentation extends BundleSummaryRepresentation
{
    public static final class HeaderClauseRepresentation
    {
        @JsonProperty private final String path;
        @JsonProperty private final Map<String, String> parameters;
        @JsonProperty private final @Nullable
        PackageRepresentation referencedPackage;

        @JsonCreator
        HeaderClauseRepresentation(@JsonProperty("path") String path,
            @JsonProperty("parameters") Map<String, String> parameters,
            @JsonProperty("packages") @Nullable PackageRepresentation referencedPackage)
        {
            this.path = checkNotNull(path, "path");
            this.parameters = ImmutableMap.copyOf(parameters);
            this.referencedPackage = referencedPackage;
        }

        HeaderClauseRepresentation(HeaderClause headerClause, UpmUriBuilder uriBuilder)
        {
            this.path = checkNotNull(headerClause.getPath());
            this.parameters = ImmutableMap.copyOf(headerClause.getParameters());
            this.referencedPackage = PackageRepresentation.wrap(uriBuilder).fromSingleton(headerClause.getReferencedPackage());
        }

        public String getPath()
        {
            return path;
        }

        public Map<String, String> getParameters()
        {
            return parameters;
        }

        @Nullable
        public PackageRepresentation getReferencedPackage()
        {
            return referencedPackage;
        }

        static Wrapper2<String, HeaderClause, HeaderClauseRepresentation> wrap(final UpmUriBuilder uriBuilder)
        {
            return new Wrapper2<String, HeaderClause, HeaderClauseRepresentation>("headerClauseRepresentation")
            {
                protected HeaderClauseRepresentation wrap(String headerName, HeaderClause headerClause)
                {
                    return new HeaderClauseRepresentation(headerClause, uriBuilder);
                }
            };
        }
    }

    @JsonProperty private final Map<String, String> unparsedHeaders;
    @JsonProperty private final Map<String, Collection<HeaderClauseRepresentation>> parsedHeaders;
    @JsonProperty private final Collection<ServiceRepresentation> registeredServices;
    @JsonProperty private final Collection<ServiceRepresentation> servicesInUse;

    @JsonCreator
    BundleRepresentation(@JsonProperty("state") State state,
        @JsonProperty("unparsedHeaders") Map<String, String> unparsedHeaders,
        @JsonProperty("parsedHeaders") Map<String, Collection<HeaderClauseRepresentation>> parsedHeaders,
        @JsonProperty("id") long id,
        @JsonProperty("location") @Nullable URI location,
        @JsonProperty("registeredServices") Collection<ServiceRepresentation> registeredServices,
        @JsonProperty("servicesInUse") Collection<ServiceRepresentation> servicesInUse,
        @JsonProperty("symbolicName") String symbolicName,
        @JsonProperty("name") @Nullable String name,
        @JsonProperty("version") String version,
        @JsonProperty("links") Map<String, URI> links)
    {
        super(state, id, location, symbolicName, name, version, links);
        this.unparsedHeaders = ImmutableMap.copyOf(unparsedHeaders);
        this.parsedHeaders = ImmutableMap.copyOf(parsedHeaders);
        this.registeredServices = ImmutableList.copyOf(registeredServices);
        this.servicesInUse = ImmutableList.copyOf(servicesInUse);
    }

    public BundleRepresentation(Bundle bundle, UpmUriBuilder uriBuilder)
    {
        super(bundle, uriBuilder);
        this.unparsedHeaders = ImmutableMap.copyOf(bundle.getUnparsedHeaders());
        this.parsedHeaders = HeaderClauseRepresentation.wrap(uriBuilder).fromIterableValuedMap(bundle.getParsedHeaders());
        this.registeredServices = ServiceRepresentation.wrap(uriBuilder).fromIterable(bundle.getRegisteredServices());
        this.servicesInUse = ServiceRepresentation.wrap(uriBuilder).fromIterable(bundle.getServicesInUse());
    }

    public Map<String, String> getUnparsedHeaders()
    {
        return unparsedHeaders;
    }

    public Map<String, Collection<HeaderClauseRepresentation>> getParsedHeaders()
    {
        return parsedHeaders;
    }

    public Collection<ServiceRepresentation> getRegisteredServices()
    {
        return registeredServices;
    }

    public Collection<ServiceRepresentation> getServicesInUse()
    {
        return servicesInUse;
    }

    public static Wrapper<Bundle, BundleRepresentation> wrap(final UpmUriBuilder uriBuilder)
    {
        return new Wrapper<Bundle, BundleRepresentation>("bundleRepresentation")
        {
            public BundleRepresentation wrap(Bundle bundle)
            {
                return new BundleRepresentation(bundle, uriBuilder);
            }
        };
    }
}
