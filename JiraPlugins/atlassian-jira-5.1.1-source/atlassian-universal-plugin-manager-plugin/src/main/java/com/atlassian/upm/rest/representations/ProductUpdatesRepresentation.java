package com.atlassian.upm.rest.representations;

import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import com.atlassian.plugins.domain.model.product.Product;
import com.atlassian.upm.rest.UpmUriBuilder;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.joda.time.DateTime;

import static com.atlassian.upm.rest.representations.RepresentationLinks.SELF_REL;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.transform;

public class ProductUpdatesRepresentation
{
    @JsonProperty private final Map<String, URI> links;
    @JsonProperty private final Collection<ProductUpdateEntry> versions;
    @JsonProperty private final HostStatusRepresentation hostStatus;

    @JsonCreator
    public ProductUpdatesRepresentation(@JsonProperty("links") Map<String, URI> links,
                                        @JsonProperty("versions") Collection<ProductUpdateEntry> versions,
                                        @JsonProperty("safeMode") HostStatusRepresentation hostStatus)
    {
        this.links = links;
        this.versions = versions;
        this.hostStatus = hostStatus;
    }

    public ProductUpdatesRepresentation(UpmUriBuilder uriBuilder, Iterable<Product> products,
                                        LinkBuilder linkBuilder, HostStatusRepresentation hostStatus)
    {
        links = linkBuilder.buildLinksFor(uriBuilder.buildProductUpdatesUri()).build();
        versions = ImmutableList.copyOf(transform(products, toEntries(uriBuilder)));
        this.hostStatus = hostStatus;
    }

    public Collection<ProductUpdateEntry> getVersions()
    {
        return versions;
    }

    public Map<String, URI> getLinks()
    {
        return links;
    }

    public HostStatusRepresentation getHostStatus()
    {
        return hostStatus;
    }

    private Function<Product, ProductUpdateEntry> toEntries(UpmUriBuilder uriBuilder)
    {
        return new ToEntryFunction(uriBuilder);
    }

    private final static class ToEntryFunction implements Function<Product, ProductUpdateEntry>
    {
        private final UpmUriBuilder uriBuilder;

        public ToEntryFunction(UpmUriBuilder uriBuilder)
        {
            this.uriBuilder = uriBuilder;
        }

        public ProductUpdateEntry apply(Product product)
        {
            return new ProductUpdateEntry(product, uriBuilder);
        }
    }

    public static final class ProductUpdateEntry
    {
        @JsonProperty private final String version;
        @JsonProperty private final boolean recent;
        @JsonProperty private final Map<String, URI> links;

        @JsonCreator
        public ProductUpdateEntry(@JsonProperty("version") String version,
                                  @JsonProperty("recent") boolean recent,
                                  @JsonProperty("links") Map<String, URI> links)
        {
            this.version = checkNotNull(version, "version");
            this.recent = recent;
            this.links = ImmutableMap.copyOf(links);
        }

        public ProductUpdateEntry(Product product, UpmUriBuilder uriBuilder)
        {
            this.version = product.getVersionNumber();
            this.recent = isRecent(product);
            this.links = ImmutableMap.of(SELF_REL, uriBuilder.buildProductUpdatePluginCompatibilityUri(product.getBuildNumber()));
        }

        public URI getSelf()
        {
            return links.get(SELF_REL);
        }

        public String getVersion()
        {
            return version;
        }

        public boolean isRecent()
        {
            return recent;
        }

        private static boolean isRecent(Product product)
        {
            Date releaseDate = product.getReleaseDate();
            return releaseDate == null ? false : new DateTime(releaseDate).isAfter(new DateTime().minusWeeks(2));
        }
    }
}
