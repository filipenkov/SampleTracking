package com.atlassian.upm.rest.representations;

import java.net.URI;
import java.util.Map;

import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.rest.UpmUriBuilder;
import com.atlassian.upm.spi.Plugin.Module;

import com.google.common.collect.ImmutableMap;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import static com.atlassian.upm.permission.Permission.MANAGE_PLUGIN_MODULE_ENABLEMENT;
import static com.atlassian.upm.rest.representations.RepresentationLinks.MODIFY_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.PLUGIN_REL;
/**
 * Jackson representation of a particular module in a particular plugin.
 */
public class PluginModuleRepresentation
{
    @JsonProperty private final Map<String, URI> links;
    @JsonProperty private final boolean enabled;
    @JsonProperty private final String name;
    @JsonProperty private final String description;


    @JsonCreator
    public PluginModuleRepresentation(@JsonProperty("links") Map<String, URI> links,
        @JsonProperty("enabled") boolean enabled,
        @JsonProperty("name") String name,
        @JsonProperty("description") String description)
    {
        this.links = ImmutableMap.copyOf(links);
        this.enabled = enabled;
        this.name = name;
        this.description = description;
    }

    public PluginModuleRepresentation(PluginAccessorAndController pluginAccessorAndController, Module module, UpmUriBuilder uriBuilder, LinkBuilder linkBuilder)
    {
        this.name = module.getName();
        this.description = module.getDescription();
        this.enabled = pluginAccessorAndController.isPluginModuleEnabled(module.getCompleteKey());
        this.links = linkBuilder.buildLinkForSelf(uriBuilder.buildPluginModuleUri(module.getPluginKey(), module.getKey()))
            .put(PLUGIN_REL, uriBuilder.buildPluginUri(module.getPluginKey()))
            .putIfPermitted(MANAGE_PLUGIN_MODULE_ENABLEMENT, module, MODIFY_REL, uriBuilder.buildPluginModuleUri(module.getPluginKey(), module.getKey()))
            .build();
    }

    public Map<String, URI> getLinks()
    {
        return links;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

}
