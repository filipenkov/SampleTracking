package com.atlassian.upm.impl;

import java.io.File;

import com.atlassian.plugin.PluginArtifact;
import com.atlassian.plugin.XmlPluginArtifact;
import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.rest.representations.RepresentationFactory;

/**
 * An implementation of the {@code PluginArtifactInstallHandler} that handles XML plugin artifact installation.
 */
public class XmlPluginArtifactInstallHandler extends PluginArtifactInstallHandler
{
    public XmlPluginArtifactInstallHandler(PluginAccessorAndController pluginAccessorAndController,
        RepresentationFactory representationFactory)
    {
        super(pluginAccessorAndController, representationFactory);
    }

    @Override
    public PluginArtifact getPluginArtifact(File plugin)
    {
        return new XmlPluginArtifact(plugin);
    }
}
