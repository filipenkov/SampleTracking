package com.atlassian.upm.osgi.rest.representations;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public final class CollectionRepresentation<T>
{
    @JsonProperty private final Collection<T> entries;
    @JsonProperty private final boolean safeMode;
    @JsonProperty private final Map<String, URI> links;

    @JsonCreator
    public CollectionRepresentation(@JsonProperty("entries") Collection<T> entries,
        @JsonProperty("safeMode") boolean safeMode,
        @JsonProperty("links") Map<String, URI> links)
    {
        this.entries = ImmutableList.copyOf(entries);
        this.safeMode = safeMode;
        this.links = ImmutableMap.copyOf(links);
    }

    public Collection<T> getEntries()
    {
        return entries;
    }

    public boolean isSafeMode()
    {
        return safeMode;
    }

    public Map<String, URI> getLinks()
    {
        return links;
    }
}
