package com.atlassian.upm.rest.resources.updateall;

import com.atlassian.plugins.domain.model.plugin.PluginVersion;

import org.codehaus.jackson.annotate.JsonProperty;

final class UpdatingPluginStatus extends UpdateStatus
{
    @JsonProperty private final String name;
    @JsonProperty private final String version;
    @JsonProperty private final int numberComplete;
    @JsonProperty private final int totalUpdates;

    UpdatingPluginStatus(PluginVersion pluginVersion, int numberComplete, int totalUpdates)
    {
        super(State.UPDATING);
        name = pluginVersion.getPlugin().getName();
        version = pluginVersion.getVersion();
        this.numberComplete = numberComplete;
        this.totalUpdates = totalUpdates;
    }

    public int getNumberComplete()
    {
        return numberComplete;
    }

    public int getTotalUpdates()
    {
        return totalUpdates;
    }

    public String getName()
    {
        return name;
    }

    public String getVersion()
    {
        return version;
    }
}