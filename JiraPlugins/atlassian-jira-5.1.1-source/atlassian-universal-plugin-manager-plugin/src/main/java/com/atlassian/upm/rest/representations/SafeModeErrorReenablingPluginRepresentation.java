package com.atlassian.upm.rest.representations;

import com.atlassian.upm.PluginConfiguration;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class SafeModeErrorReenablingPluginRepresentation extends ErrorRepresentation
{
    @JsonProperty private final String pluginKey;
    @JsonProperty private final String pluginName;

    @JsonCreator
    public SafeModeErrorReenablingPluginRepresentation(@JsonProperty("pluginKey") String pluginKey,
        @JsonProperty("pluginName") String pluginName)
    {
        super("System failed to restore from Safe Mode. Reenabling plugin '" + pluginName + "' failed while exiting safe mode.", "upm.safeMode.error.enabling.plugin.failed");
        this.pluginKey = pluginKey;
        this.pluginName = pluginName;
    }

    public SafeModeErrorReenablingPluginRepresentation(PluginConfiguration plugin)
    {
        this(plugin.getKey(), plugin.getName());
    }

    public String getPluginKey()
    {
        return pluginKey;
    }

    public String getPluginName()
    {
        return pluginName;
    }
}
