package com.atlassian.upm.rest.representations;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import com.atlassian.plugins.domain.model.plugin.PluginVersion;
import com.atlassian.upm.rest.UpmUriBuilder;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Collection of plugins that can be installed in the application.
 */
public class AvailablePluginCollectionRepresentation extends AbstractInstallablePluginCollectionRepresentation
{
    @JsonCreator
    public AvailablePluginCollectionRepresentation(
        @JsonProperty("links") Map<String, URI> links,
        @JsonProperty("plugins") Collection<AvailablePluginEntry> plugins,
        @JsonProperty("hostStatus") HostStatusRepresentation hostStatus)
    {
        super(links, plugins, hostStatus);
    }

    public AvailablePluginCollectionRepresentation(Iterable<PluginVersion> pluginVersions, UpmUriBuilder uriBuilder,
        LinkBuilder linkBuilder, HostStatusRepresentation hostStatus)
    {
        super(linkBuilder.buildLinksFor(uriBuilder.buildAvailablePluginCollectionUri()).build(),
            pluginVersions, uriBuilder, hostStatus);
    }
}
