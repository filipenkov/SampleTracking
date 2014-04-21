package com.atlassian.upm.rest.resources.install;

import java.io.File;
import java.net.URI;

import com.atlassian.upm.*;
import com.atlassian.upm.api.util.Either;
import com.atlassian.upm.SelfUpdateController;
import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.rest.async.AsynchronousTask;
import com.atlassian.upm.rest.representations.PluginRepresentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.upm.rest.resources.install.InstallStatus.complete;
import static com.atlassian.upm.rest.resources.install.InstallStatus.err;
import static com.google.common.base.Preconditions.checkNotNull;

public abstract class InstallTask extends AsynchronousTask<InstallStatus>
{
    private static final Logger logger = LoggerFactory.getLogger(InstallTask.class);

    private final Option<String> source;
    private final PluginInstaller installer;
    private final SelfUpdateController selfUpdateController;

    public InstallTask(Option<String> source, String username, PluginInstaller installer, SelfUpdateController selfUpdateController)
    {
        super(Type.PLUGIN_INSTALL, username);
        this.source = source;
        this.installer = checkNotNull(installer, "installer");
        this.selfUpdateController = checkNotNull(selfUpdateController, "selfUpdateController");
    }

    public URI call() throws Exception
    {
        try
        {
            return executeTask();
        }
        catch (SafeModeException sme)
        {
            status = err("upm.pluginInstall.error.safe.mode", getSource());
        }
        catch (PluginInstallException pie)
        {
            status = err("upm.pluginInstall.error.install.failed", getSource());
        }
        catch (UnrecognisedPluginVersionException upve)
        {
            status = err("upm.pluginInstall.error.unrecognised.plugin.version", getSource());
        }        
        catch (UnknownPluginTypeException upte)
        {
            status = err("upm.pluginInstall.error.unknown.plugin.type", getSource());
        }
        catch (LegacyPluginsUnsupportedException lpue)
        {
            status = err("upm.pluginInstall.error.legacy.plugins.unsupported", getSource());
        }
        catch (XmlPluginsUnsupportedException xpue)
        {
            status = err("upm.pluginInstall.error.xml.plugins.unsupported", getSource());
        }
        catch (RuntimeException re)
        {
            logger.warn("Unexpected error in install task", re);
            status = err("upm.plugin.error.unexpected.error", getSource());
        }
        return null;
    }

    protected String getSource()
    {
        return source.getOrElse("");
    }
    
    protected abstract URI executeTask() throws Exception;

    public abstract void accept();

    protected URI installFromFile(File plugin)
    {
        if (selfUpdateController.isUpmPlugin(plugin))
        {
            Either<String, URI> completionUriOrError = selfUpdateController.prepareSelfUpdate(plugin);
            for (String error : completionUriOrError.left())
            {
                status = err(error, getSource());
                return null;
            }

            URI completionUri = completionUriOrError.right().get();
            status = InstallStatus.nextTaskPostRequired(getSource(), completionUri);
            return completionUri;
        }
        else
        {
            PluginRepresentation pluginRepresentation = installer.install(plugin, getSource());
            status = complete(getSource());
            return pluginRepresentation.getSelfLink();
        }
    }
}