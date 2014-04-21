package com.atlassian.upm.pac;

import com.atlassian.plugins.domain.model.plugin.PluginVersion;
import com.atlassian.upm.api.util.Option;

/**
 * Holds multiple versions for a given plugin: the current (or any specific) version + the latest.
 */
public class PluginVersionPair
{
    private final Option<PluginVersion> specific;
    private final Option<PluginVersion> latest;

    PluginVersionPair(Option<PluginVersion> specific, Option<PluginVersion> latest)
    {
        this.specific = specific;
        this.latest = latest;
    }

    public Option<PluginVersion> getSpecific()
    {
        return specific;
    }

    public Option<PluginVersion> getLatest()
    {
        return latest;
    }
}
