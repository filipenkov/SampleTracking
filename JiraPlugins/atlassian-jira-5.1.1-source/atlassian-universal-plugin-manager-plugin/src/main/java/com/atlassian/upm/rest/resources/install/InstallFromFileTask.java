package com.atlassian.upm.rest.resources.install;

import java.io.File;
import java.net.URI;

import com.atlassian.upm.PluginInstaller;
import com.atlassian.upm.SelfUpdateController;
import com.atlassian.upm.api.util.Option;

import static com.atlassian.upm.rest.resources.install.InstallStatus.installing;
import static com.google.common.base.Preconditions.checkNotNull;

public class InstallFromFileTask extends InstallTask
{
    private final File plugin;

    public InstallFromFileTask(Option<String> fileName, File plugin, String username,
                               PluginInstaller pluginInstaller, SelfUpdateController selfUpdateController)
    {
        super(fileName, username, pluginInstaller, selfUpdateController);
        this.plugin = checkNotNull(plugin, "plugin");
    }

    public void accept()
    {
        status = installing(getSource());
    }

    protected URI executeTask() throws Exception
    {
        return installFromFile(plugin);
    }
}