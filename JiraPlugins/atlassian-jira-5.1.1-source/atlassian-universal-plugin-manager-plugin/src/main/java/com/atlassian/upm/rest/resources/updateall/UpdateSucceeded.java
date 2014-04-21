package com.atlassian.upm.rest.resources.updateall;

import com.atlassian.plugins.domain.model.plugin.PluginVersion;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public final class UpdateSucceeded
{
    @JsonProperty private final String name;
    @JsonProperty private final String key;
    @JsonProperty private final String version;

    @JsonCreator
    public UpdateSucceeded(@JsonProperty("name") String name, @JsonProperty("key") String key, @JsonProperty("version") String version)
    {
        this.name = name;
        this.key = key;
        this.version = version;
    }

    UpdateSucceeded(PluginVersion update)
    {
        this(update.getPlugin().getName(), update.getPlugin().getPluginKey(), update.getVersion());
    }

    public String getName()
    {
        return name;
    }

    public String getKey()
    {
        return key;
    }
    
    public String getVersion()
    {
        return version;
    }
}