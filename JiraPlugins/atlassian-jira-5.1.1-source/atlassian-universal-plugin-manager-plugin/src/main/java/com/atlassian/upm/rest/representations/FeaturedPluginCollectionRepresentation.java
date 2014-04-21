package com.atlassian.upm.rest.representations;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import com.atlassian.plugins.domain.model.plugin.PluginVersion;
import com.atlassian.upm.rest.UpmUriBuilder;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Collection of featured plugins that can be installed in the application.
 */
public class FeaturedPluginCollectionRepresentation extends AbstractInstallablePluginCollectionRepresentation
{
    @JsonCreator
    public FeaturedPluginCollectionRepresentation(
        @JsonProperty("links") Map<String, URI> links,
        @JsonProperty("plugins") Collection<AvailablePluginEntry> plugins,
        @JsonProperty("hostStatus") HostStatusRepresentation hostStatus)
    {
        super(links, plugins, hostStatus);
    }

    public FeaturedPluginCollectionRepresentation(Iterable<PluginVersion> pluginVersions, UpmUriBuilder uriBuilder,
        LinkBuilder linkBuilder, HostStatusRepresentation hostStatus)
    {
        super(linkBuilder.buildLinksFor(uriBuilder.buildFeaturedPluginCollectionUri()).build(),
            pluginVersions, uriBuilder, hostStatus);
    }
}