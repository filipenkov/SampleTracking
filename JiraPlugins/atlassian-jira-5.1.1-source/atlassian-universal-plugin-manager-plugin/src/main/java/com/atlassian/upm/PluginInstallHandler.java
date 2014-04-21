package com.atlassian.upm;

import java.io.File;

import com.atlassian.upm.rest.representations.PluginRepresentation;

/**
 * An interface that handles plugin artifact installation.
 */
public interface PluginInstallHandler
{
    /**
     * Installs the passed plugin {@code File}
     *
     * @param plugin the plugin {@code File} to install
     * @return the {@code PluginRepresentation} of the installed plugin
     * @throws PluginInstallException if any errors are encountered while installing the plugin
     */
    PluginRepresentation installPlugin(File plugin) throws PluginInstallException;
}
