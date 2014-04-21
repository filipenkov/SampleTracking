package com.atlassian.upm.impl;

import java.io.File;

import com.atlassian.plugin.PluginArtifact;
import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.PluginInstallHandler;
import com.atlassian.upm.rest.representations.PluginRepresentation;
import com.atlassian.upm.rest.representations.RepresentationFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An implementation of the {@code PluginInstallHandler} that handles plugin artifact installation.
 */
public abstract class PluginArtifactInstallHandler implements PluginInstallHandler
{
    private final PluginAccessorAndController pluginAccessorAndController;
    private final RepresentationFactory representationFactory;

    public PluginArtifactInstallHandler(PluginAccessorAndController pluginAccessorAndController,
        RepresentationFactory representationFactory)
    {
        this.pluginAccessorAndController = checkNotNull(pluginAccessorAndController, "pluginAccessorAndController");
        this.representationFactory = checkNotNull(representationFactory, "representationFactory");
    }

    public PluginRepresentation installPlugin(File plugin)
    {
        String pluginKey = pluginAccessorAndController.installPlugin(getPluginArtifact(plugin));
        return representationFactory.createPluginRepresentation(pluginAccessorAndController.getPlugin(pluginKey));
    }

    public abstract PluginArtifact getPluginArtifact(File plugin);
}
