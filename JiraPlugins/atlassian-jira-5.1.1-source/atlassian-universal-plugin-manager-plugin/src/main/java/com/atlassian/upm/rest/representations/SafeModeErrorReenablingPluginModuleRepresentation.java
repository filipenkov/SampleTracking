package com.atlassian.upm.rest.representations;

import com.atlassian.upm.PluginConfiguration;
import com.atlassian.upm.PluginModuleConfiguration;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class SafeModeErrorReenablingPluginModuleRepresentation extends ErrorRepresentation
{
    @JsonProperty private final String pluginKey;
    @JsonProperty private final String pluginName;
    @JsonProperty private final String moduleKey;
    @JsonProperty private final String moduleName;

    @JsonCreator
    public SafeModeErrorReenablingPluginModuleRepresentation(@JsonProperty("pluginKey") String pluginKey,
        @JsonProperty("pluginName") String pluginName, @JsonProperty("moduleKey") String moduleKey,
        @JsonProperty("moduleName") String moduleName)
    {
        super("System failed to restore from Safe Mode. Reenabling plugin module '" + moduleName + "' from '" +
            pluginName + "' failed while exiting safe mode.", "upm.safeMode.error.enabling.plugin.module.failed");
        this.pluginKey = pluginKey;
        this.pluginName = pluginName;
        this.moduleKey = moduleKey;
        this.moduleName = moduleName;
    }

    public SafeModeErrorReenablingPluginModuleRepresentation(PluginConfiguration plugin, PluginModuleConfiguration module)
    {
        this(plugin.getKey(), plugin.getName(), module.getCompleteKey(), module.getName());
    }

    public String getPluginKey()
    {
        return pluginKey;
    }

    public String getPluginName()
    {
        return pluginName;
    }


    public String getModuleKey()
    {
        return moduleKey;
    }

    public String getModuleName()
    {
        return moduleName;
    }
}
