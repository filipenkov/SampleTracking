package com.atlassian.upm.rest.representations;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import com.atlassian.plugins.domain.model.plugin.PluginIcon;
import com.atlassian.plugins.domain.model.plugin.PluginVersion;
import com.atlassian.upm.rest.UpmUriBuilder;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import static com.atlassian.upm.rest.representations.RepresentationLinks.PLUGIN_BANNER_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.PLUGIN_ICON_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.SELF_REL;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.transform;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.trim;

/**
 * Collection of plugins that can be installed in the application.
 */
public abstract class AbstractInstallablePluginCollectionRepresentation
{
    @JsonProperty private final Map<String, URI> links;
    @JsonProperty private final Collection<AvailablePluginEntry> plugins;
    @JsonProperty private final HostStatusRepresentation hostStatus;

    @JsonCreator
    public AbstractInstallablePluginCollectionRepresentation(
        @JsonProperty("links") Map<String, URI> links,
        @JsonProperty("plugins") Collection<AvailablePluginEntry> plugins,
        @JsonProperty("hostStatus") HostStatusRepresentation hostStatus)
    {
        this.plugins = ImmutableList.copyOf(plugins);
        this.links = ImmutableMap.copyOf(links);
        this.hostStatus = hostStatus;
    }

    public AbstractInstallablePluginCollectionRepresentation(Map<String, URI> links,
        Iterable<PluginVersion> plugins, UpmUriBuilder uriBuilder,
        HostStatusRepresentation hostStatus)
    {
        this.links = links;
        this.plugins = ImmutableList.copyOf(transform(plugins, toEntries(uriBuilder)));
        this.hostStatus = hostStatus;
    }

    public Map<String, URI> getLinks()
    {
        return links;
    }

    public Iterable<AvailablePluginEntry> getPlugins()
    {
        return plugins;
    }

    public HostStatusRepresentation getHostStatus()
    {
        return hostStatus;
    }

    private Function<PluginVersion, AvailablePluginEntry> toEntries(UpmUriBuilder uriBuilder)
    {
        return new ToEntryFunction(uriBuilder);
    }

    private final static class ToEntryFunction implements Function<PluginVersion, AvailablePluginEntry>
    {
        private final UpmUriBuilder uriBuilder;

        public ToEntryFunction(UpmUriBuilder uriBuilder)
        {
            this.uriBuilder = uriBuilder;
        }

        public AvailablePluginEntry apply(PluginVersion plugin)
        {
            return new AvailablePluginEntry(plugin, uriBuilder);
        }
    }

    public static final class AvailablePluginEntry
    {
        @JsonProperty private final Map<String, URI> links;
        @JsonProperty private final String name;
        @JsonProperty private final String key;
        @JsonProperty private final String summary;
        @JsonProperty private final Collection<String> marketingLabels;

        @JsonCreator
        public AvailablePluginEntry(@JsonProperty("links") Map<String, URI> links,
            @JsonProperty("name") String name,
            @JsonProperty("summary") String summary,
            @JsonProperty("key") String key,
            @JsonProperty("marketingLabels") Collection<String> marketingLabels)
        {
            this.links = ImmutableMap.copyOf(links);
            this.name = checkNotNull(name, "name");
            this.key = checkNotNull(key, "key");
            this.summary = checkNotNull(summary, "summary");
            this.marketingLabels = ImmutableList.copyOf(marketingLabels);
        }

        AvailablePluginEntry(PluginVersion plugin, UpmUriBuilder uriBuilder)
        {
            this.links = buildLinks(plugin, uriBuilder);
            this.name = plugin.getPlugin().getName();
            this.key = plugin.getPlugin().getPluginKey();
            this.summary = plugin.getSummary();
            this.marketingLabels = ImmutableList.copyOf(plugin.getMarketingLabel());
        }

        private Map<String, URI> buildLinks(PluginVersion plugin, UpmUriBuilder uriBuilder)
        {
            ImmutableMap.Builder<String, URI> builder = new ImmutableMap.Builder<String, URI>();
            builder.put(SELF_REL, uriBuilder.buildAvailablePluginUri(plugin.getPlugin().getPluginKey()));
            builder.put(PLUGIN_ICON_REL, getIconUri(plugin) != null ? getIconUri(plugin) : uriBuilder.buildPluginIconLocationUri(plugin.getPlugin().getPluginKey()));
            if (plugin.getBannerUrl() != null && !isEmpty(plugin.getBannerUrl()))
            {
                builder.put(PLUGIN_BANNER_REL, URI.create(trim(plugin.getBannerUrl())));
            }
            return builder.build();
        }

        private URI getIconUri(PluginVersion plugin)
        {
            PluginIcon pluginIcon = plugin.getPlugin().getTinyIcon();
            if (pluginIcon != null)
            {
                String location = pluginIcon.getLocation();
                if (location != null)
                {
                    return URI.create(location);
                }
            }
            return null;
        }

        public URI getSelf()
        {
            return links.get(SELF_REL);
        }

        public String getName()
        {
            return name;
        }

        public String getKey()
        {
            return key;
        }

        public String getSummary()
        {
            return summary;
        }

        public URI getPluginIconLink()
        {
            return links.get(PLUGIN_ICON_REL);
        }

        public Collection<String> getMarketingLabels()
        {
            return marketingLabels;
        }

        @Override
        public String toString()
        {
            return name;
        }
    }
}
