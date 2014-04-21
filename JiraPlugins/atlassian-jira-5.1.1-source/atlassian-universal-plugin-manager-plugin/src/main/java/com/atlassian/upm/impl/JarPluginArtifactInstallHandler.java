package com.atlassian.upm.impl;

import java.io.File;

import com.atlassian.plugin.JarPluginArtifact;
import com.atlassian.plugin.PluginArtifact;
import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.rest.representations.RepresentationFactory;

/**
 * An implementation of the {@code PluginArtifactInstallHandler} that handles JAR plugin artifact installation.
 */
public final class JarPluginArtifactInstallHandler extends PluginArtifactInstallHandler
{
    public JarPluginArtifactInstallHandler(PluginAccessorAndController pluginAccessorAndController,
        RepresentationFactory representationFactory)
    {
        super(pluginAccessorAndController, representationFactory);
    }

    @Override
    public PluginArtifact getPluginArtifact(File plugin)
    {
        return new JarPluginArtifact(plugin);
    }
}
