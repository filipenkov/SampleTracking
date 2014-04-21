package com.atlassian.upm.rest.representations;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import javax.annotation.Nullable;

import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.ProductUpdatePluginCompatibility;
import com.atlassian.upm.rest.UpmUriBuilder;
import com.atlassian.upm.spi.Plugin;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import static com.atlassian.upm.permission.Permission.GET_AVAILABLE_PLUGINS;
import static com.atlassian.upm.permission.Permission.GET_PRODUCT_UPDATE_COMPATIBILITY;
import static com.atlassian.upm.permission.Permission.MANAGE_PLUGIN_ENABLEMENT;
import static com.atlassian.upm.rest.representations.RepresentationLinks.AVAILABLE_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.INSTALLED_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.MODIFY_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.PLUGIN_ICON_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.PLUGIN_LOGO_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.PRODUCT_UPDATES_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.SELF_REL;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.ImmutableList.copyOf;

public class ProductUpdatePluginCompatibilityRepresentation
{
    @JsonProperty private final Map<String, URI> links;
    @JsonProperty private final Collection<PluginEntry> compatible;
    @JsonProperty private final Collection<PluginEntry> updateRequired;
    @JsonProperty private final Collection<PluginEntry> updateRequiredAfterProductUpdate;
    @JsonProperty private final Collection<PluginEntry> incompatible;
    @JsonProperty private final Collection<PluginEntry> unknown;

    @JsonCreator
    public ProductUpdatePluginCompatibilityRepresentation(@JsonProperty("links") Map<String, URI> links,
                                                          @JsonProperty("compatible") Collection<PluginEntry> compatible,
                                                          @JsonProperty("updateRequired") Collection<PluginEntry> updateRequired,
                                                          @JsonProperty("updateRequiredAfterProductUpdate") Collection<PluginEntry> updateRequiredAfterProductUpdate,
                                                          @JsonProperty("incompatible") Collection<PluginEntry> incompatible,
                                                          @JsonProperty("unknown") Collection<PluginEntry> unknown)
    {
        this.links = ImmutableMap.copyOf(links);
        this.compatible = copyOf(compatible);
        this.updateRequired = copyOf(updateRequired);
        this.updateRequiredAfterProductUpdate = copyOf(updateRequiredAfterProductUpdate);
        this.incompatible = copyOf(incompatible);
        this.unknown = copyOf(unknown);
    }

    public ProductUpdatePluginCompatibilityRepresentation(UpmUriBuilder uriBuilder, LinkBuilder linkBuilder, PluginAccessorAndController manager,
                                                          ProductUpdatePluginCompatibility pluginCompatibility, Long productUpdateBuildNumber)
    {
        Function<Plugin, PluginEntry> toEntries = new ToPluginEntriesFunction(uriBuilder, linkBuilder, manager);
        Function<Plugin, PluginEntry> toUpdatableEntries = new ToUpdatablePluginEntriesFunction(uriBuilder, linkBuilder, manager);

        this.links = linkBuilder.buildLinkForSelf(uriBuilder.buildProductUpdatePluginCompatibilityUri(productUpdateBuildNumber))
            .put(INSTALLED_REL, uriBuilder.buildInstalledPluginCollectionUri())
            .putIfPermitted(GET_PRODUCT_UPDATE_COMPATIBILITY, PRODUCT_UPDATES_REL, uriBuilder.buildProductUpdatesUri())
            .build();
        this.compatible = transform(copyOf(pluginCompatibility.getCompatible()), toEntries);
        this.updateRequired = transform(copyOf(pluginCompatibility.getUpdateRequired()), toUpdatableEntries);
        this.updateRequiredAfterProductUpdate = transform(copyOf(pluginCompatibility.getUpdateRequiredAfterProductUpdate()), toUpdatableEntries);
        this.incompatible = transform(copyOf(pluginCompatibility.getIncompatible()), toEntries);
        this.unknown = transform(copyOf(pluginCompatibility.getUnknown()), toEntries);
    }

    public Map<String, URI> getLinks()
    {
        return links;
    }

    public Collection<PluginEntry> getCompatible()
    {
        return compatible;
    }

    public Collection<PluginEntry> getUpdateRequired()
    {
        return updateRequired;
    }

    public Collection<PluginEntry> getUpdateRequiredAfterProductUpdate()
    {
        return updateRequiredAfterProductUpdate;
    }

    public Collection<PluginEntry> getIncompatible()
    {
        return incompatible;
    }

    public Collection<PluginEntry> getUnknown()
    {
        return unknown;
    }

    public static final class PluginEntry
    {
        @JsonProperty private final Map<String, URI> links;
        @JsonProperty private final String name;
        @JsonProperty private final String key;
        @JsonProperty private final boolean enabled;
        @JsonProperty private final String restartState;

        @JsonCreator
        public PluginEntry(@JsonProperty("links") Map<String, URI> links,
            @JsonProperty("name") String name,
            @JsonProperty("enabled") boolean enabled,
            @JsonProperty("key") String key,
            @JsonProperty("restartState") String restartState)
        {
            this.links = ImmutableMap.copyOf(links);
            this.name = checkNotNull(name, "name");
            this.key = checkNotNull(key, "key");
            this.enabled = enabled;
            this.restartState = restartState;
        }

        public URI getSelfLink()
        {
            return links.get(SELF_REL);
        }

        public URI getAvailableLink()
        {
            return links.get(AVAILABLE_REL);
        }

        public URI getModifyLink()
        {
            return links.get(MODIFY_REL);
        }

        public URI getPluginIconLink()
        {
            return links.get(PLUGIN_ICON_REL);
        }

        public URI getPluginLogoLink()
        {
            return links.get(PLUGIN_LOGO_REL);
        }

        public String getName()
        {
            return name;
        }

        public String getKey()
        {
            return key;
        }

        public String getRestartState()
        {
            return restartState;
        }

        @Override
        public String toString()
        {
            return "PluginEntry{" +
                "links=" + links +
                ", name='" + name + '\'' +
                ", enabled=" + enabled +
                ", restartState='" + restartState + '\'' +
                '}';
        }
    }

    private static class ToPluginEntriesFunction implements Function<Plugin, PluginEntry>
    {
        protected final UpmUriBuilder uriBuilder;
        protected final LinkBuilder linkBuilder;
        protected final PluginAccessorAndController manager;

        ToPluginEntriesFunction(UpmUriBuilder uriBuilder, LinkBuilder linkBuilder, PluginAccessorAndController manager)
        {
            this.uriBuilder = uriBuilder;
            this.linkBuilder = linkBuilder;
            this.manager = manager;
        }

        public PluginEntry apply(@Nullable Plugin plugin)
        {
            return new PluginEntry(getLinks(plugin), plugin.getName(), manager.isPluginEnabled(plugin.getKey()), plugin.getKey(), RestartState.toString(manager.getRestartState(plugin)));
        }

        protected Map<String, URI> getLinks(Plugin plugin)
        {
            return linkBuilder.buildLinkForSelf(uriBuilder.buildPluginUri(plugin.getKey()))
                .putIfPermitted(MANAGE_PLUGIN_ENABLEMENT, MODIFY_REL, uriBuilder.buildPluginUri(plugin.getKey()))
                .putIfPermitted(GET_AVAILABLE_PLUGINS, plugin, PLUGIN_ICON_REL, uriBuilder.buildPluginIconLocationUri(plugin.getKey()))
                .putIfPermitted(GET_AVAILABLE_PLUGINS, plugin, PLUGIN_LOGO_REL, uriBuilder.buildPluginLogoLocationUri(plugin.getKey()))
                .build();
        }
    }

    private static class ToUpdatablePluginEntriesFunction extends ToPluginEntriesFunction
    {
        ToUpdatablePluginEntriesFunction(UpmUriBuilder uriBuilder, LinkBuilder linkBuilder, PluginAccessorAndController manager)
        {
            super(uriBuilder, linkBuilder, manager);
        }

        @Override
        protected Map<String, URI> getLinks(Plugin plugin)
        {
            return linkBuilder.buildLinkForSelf(uriBuilder.buildPluginUri(plugin.getKey()))
                .putIfPermitted(GET_AVAILABLE_PLUGINS, AVAILABLE_REL, uriBuilder.buildAvailablePluginUri(plugin.getKey()))
                .putIfPermitted(MANAGE_PLUGIN_ENABLEMENT, MODIFY_REL, uriBuilder.buildPluginUri(plugin.getKey()))
                .putIfPermitted(GET_AVAILABLE_PLUGINS, plugin, PLUGIN_ICON_REL, uriBuilder.buildPluginIconLocationUri(plugin.getKey()))
                .putIfPermitted(GET_AVAILABLE_PLUGINS, plugin, PLUGIN_LOGO_REL, uriBuilder.buildPluginLogoLocationUri(plugin.getKey()))
                .build();
        }
    }

}
