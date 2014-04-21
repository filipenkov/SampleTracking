package com.atlassian.upm.rest.resources.updateall;

import com.atlassian.plugins.domain.model.plugin.PluginVersion;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

public final class UpdateFailed
{
    @JsonProperty private final String name;
    @JsonProperty private final String key;
    @JsonProperty private final String version;
    @JsonProperty private final String subCode;
    @JsonProperty private final String message;
    @JsonProperty private final String source;
    @JsonIgnore private final UpdateFailed.Type type;

    @JsonCreator
    public UpdateFailed(@JsonProperty("type") String type,
                        @JsonProperty("name") String name,
                        @JsonProperty("key") String key,
                        @JsonProperty("version") String version,
                        @JsonProperty("subCode") String subCode,
                        @JsonProperty("message") String message,
                        @JsonProperty("source") String source)
    {
        this(Type.valueOf(type), name, key, version, subCode, message, source);
    }

    UpdateFailed(UpdateFailed.Type type, String subCode, PluginVersion update)
    {
        this(type, subCode, update, null);
    }

    UpdateFailed(UpdateFailed.Type type, String subCode, PluginVersion update, String message)
    {
        this(type, update.getPlugin().getName(), update.getPlugin().getPluginKey(), update.getVersion(), subCode, message, update.getBinaryUrl());
    }

    private UpdateFailed(UpdateFailed.Type type, String name, String key, String version, String subCode, String message, String source)
    {
        this.type = type;
        this.name = name;
        this.key = key;
        this.version = version;
        this.subCode = subCode;
        this.message = message;
        this.source = source;
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

    public String getSubCode()
    {
        return subCode;
    }

    public String getMessage()
    {
        return message;
    }

    public String getSource()
    {
        return source;
    }

    @JsonProperty
    public UpdateFailed.Type getType()
    {
        return type;
    }

    public enum Type
    {
        DOWNLOAD, INSTALL
    }
}