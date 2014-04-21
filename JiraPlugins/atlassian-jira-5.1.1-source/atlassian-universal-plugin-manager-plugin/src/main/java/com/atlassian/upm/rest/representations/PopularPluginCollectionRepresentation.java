package com.atlassian.upm.rest.representations;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import com.atlassian.plugins.domain.model.plugin.PluginVersion;
import com.atlassian.upm.rest.UpmUriBuilder;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Collection of popular plugins that can be installed in the application.
 */
public class PopularPluginCollectionRepresentation extends AbstractInstallablePluginCollectionRepresentation
{
    @JsonCreator
    public PopularPluginCollectionRepresentation(
        @JsonProperty("links") Map<String, URI> links,
        @JsonProperty("plugins") Collection<AvailablePluginEntry> plugins,
        @JsonProperty("hostStatus") HostStatusRepresentation hostStatus)
    {
        super(links, plugins, hostStatus);
    }

    public PopularPluginCollectionRepresentation(Iterable<PluginVersion> pluginVersions, UpmUriBuilder uriBuilder,
        LinkBuilder linkBuilder, HostStatusRepresentation hostStatus)
    {
        super(linkBuilder.buildLinksFor(uriBuilder.buildPopularPluginCollectionUri()).build(),
            pluginVersions, uriBuilder, hostStatus);
    }
}