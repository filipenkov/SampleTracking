package com.atlassian.upm.osgi.rest.representations;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import com.atlassian.upm.osgi.Package;
import com.atlassian.upm.osgi.impl.Wrapper;
import com.atlassian.upm.rest.UpmUriBuilder;

import com.google.common.collect.ImmutableList;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import static com.google.common.base.Preconditions.checkNotNull;

public final class PackageRepresentation extends PackageSummaryRepresentation
{
    @JsonProperty private final BundleSummaryRepresentation exportingBundle;
    @JsonProperty private final Collection<BundleSummaryRepresentation> importingBundles;

    @JsonCreator
    PackageRepresentation(@JsonProperty("name") String name,
        @JsonProperty("exportingBundle") BundleSummaryRepresentation exportingBundle,
        @JsonProperty("importingBundles") Collection<BundleSummaryRepresentation> importingBundles,
        @JsonProperty("version") String version,
        @JsonProperty("links") Map<String, URI> links)
    {
        super(name, version, links);
        this.exportingBundle = checkNotNull(exportingBundle, "exportingBundle");
        this.importingBundles = ImmutableList.copyOf(importingBundles);
    }

    public PackageRepresentation(Package pkg, UpmUriBuilder uriBuilder)
    {
        super(pkg, uriBuilder);
        this.exportingBundle = BundleSummaryRepresentation.wrapSummary(uriBuilder).fromSingleton(pkg.getExportingBundle());
        this.importingBundles = BundleSummaryRepresentation.wrapSummary(uriBuilder).fromIterable(pkg.getImportingBundles());
    }

    public BundleSummaryRepresentation getExportingBundle()
    {
        return exportingBundle;
    }

    public Collection<BundleSummaryRepresentation> getImportingBundles()
    {
        return importingBundles;
    }

    public static Wrapper<Package, PackageRepresentation> wrap(final UpmUriBuilder uriBuilder)
    {
        return new Wrapper<Package, PackageRepresentation>("packageRepresentation")
        {
            public PackageRepresentation wrap(Package pkg)
            {
                return new PackageRepresentation(pkg, uriBuilder);
            }
        };
    }

}
