package com.atlassian.upm.osgi.rest.representations;

import java.net.URI;
import java.util.Map;

import javax.annotation.Nullable;

import com.atlassian.upm.osgi.Bundle;
import com.atlassian.upm.osgi.Bundle.State;
import com.atlassian.upm.osgi.impl.Wrapper;
import com.atlassian.upm.rest.UpmUriBuilder;

import com.google.common.collect.ImmutableMap;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import static com.atlassian.upm.rest.representations.RepresentationLinks.SELF_REL;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A summary representation for OSGi bundle objects,
 * omitting headers and services
 */
public class BundleSummaryRepresentation
{
    @JsonProperty private final State state;
    @JsonProperty private final long id;
    @JsonProperty private final @Nullable URI location;
    @JsonProperty private final String symbolicName;
    @JsonProperty private final @Nullable String name;
    @JsonProperty private final String version;
    @JsonProperty private final Map<String, URI> links;

    @JsonCreator
    BundleSummaryRepresentation(@JsonProperty("state") State state,
        @JsonProperty("id") long id,
        @JsonProperty("location") @Nullable URI location,
        @JsonProperty("symbolicName") String symbolicName,
        @JsonProperty("name") @Nullable String name,
        @JsonProperty("version") String version,
        @JsonProperty("links") Map<String, URI> links)
    {
        this.state = checkNotNull(state, "state");
        this.id = id;
        this.location = location;
        this.symbolicName = checkNotNull(symbolicName, "symbolicName");
        this.name = name;
        this.version = checkNotNull(version, "version");
        this.links = ImmutableMap.copyOf(links);
    }

    BundleSummaryRepresentation(Bundle bundle, UpmUriBuilder uriBuilder)
    {
        this.state = bundle.getState();
        this.id = bundle.getId();
        this.location = bundle.getLocation();
        this.symbolicName = bundle.getSymbolicName();
        this.name = bundle.getName();
        this.version = bundle.getVersion().toString();
        this.links = ImmutableMap.of(SELF_REL, uriBuilder.buildOsgiBundleUri(bundle));
    }

    public State getState()
    {
        return state;
    }

    public long getId()
    {
        return id;
    }

    @Nullable
    public URI getLocation()
    {
        return location;
    }

    public String getSymbolicName()
    {
        return symbolicName;
    }

    @Nullable
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

    public static Wrapper<Bundle, BundleSummaryRepresentation> wrapSummary(final UpmUriBuilder uriBuilder)
    {
        return new Wrapper<Bundle, BundleSummaryRepresentation>("bundleSummaryRepresentation")
        {
            public BundleSummaryRepresentation wrap(Bundle bundle)
            {
                return new BundleSummaryRepresentation(bundle, uriBuilder);
            }
        };
    }

}
