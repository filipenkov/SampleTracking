package com.atlassian.upm.osgi.rest.representations;

import java.net.URI;
import java.util.Map;

import com.atlassian.upm.osgi.Package;
import com.atlassian.upm.osgi.impl.Wrapper;
import com.atlassian.upm.rest.UpmUriBuilder;

import com.google.common.collect.ImmutableMap;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import static com.atlassian.upm.rest.representations.RepresentationLinks.SELF_REL;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A summary representation for OSGi package objects,
 * omitting bundles
 */
public class PackageSummaryRepresentation
{
    @JsonProperty private final String name;
    @JsonProperty private final String version;
    @JsonProperty Map<String, URI> links;

    @JsonCreator
    public PackageSummaryRepresentation(@JsonProperty("name") String name,
        @JsonProperty("version") String version,
        @JsonProperty("links") Map<String, URI> links)
    {
        this.name = checkNotNull(name, "name");
        this.version = checkNotNull(version, "version");
        this.links = ImmutableMap.copyOf(links);
    }

    public PackageSummaryRepresentation(Package pkg, UpmUriBuilder uriBuilder)
    {
        this.name = pkg.getName();
        this.version = pkg.getVersion().toString();
        this.links = ImmutableMap.of(SELF_REL, uriBuilder.buildOsgiPackageUri(pkg));
    }

    public String getName()
    {
        return name;
    }

    public String getVersion()
    {
        return version;
    }

    public Map<String, URI> getLinks()
    {
        return links;
    }

    public static Wrapper<Package, PackageSummaryRepresentation> wrapSummary(final UpmUriBuilder uriBuilder)
    {
        return new Wrapper<Package, PackageSummaryRepresentation>("packageSummaryRepresentation")
        {
            public PackageSummaryRepresentation wrap(Package pkg)
            {
                return new PackageSummaryRepresentation(pkg, uriBuilder);
            }
        };
    }

}
