package com.atlassian.upm.rest.resources.install;

import java.io.File;
import java.net.URI;

import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.upm.AccessDeniedException;
import com.atlassian.upm.PluginDownloadService;
import com.atlassian.upm.PluginDownloadService.Progress;
import com.atlassian.upm.PluginDownloadService.ProgressTracker;
import com.atlassian.upm.PluginInstaller;
import com.atlassian.upm.RelativeURIException;
import com.atlassian.upm.SelfUpdateController;
import com.atlassian.upm.UnsupportedProtocolException;
import com.atlassian.upm.log.AuditLogService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.upm.api.util.Option.option;
import static com.atlassian.upm.rest.resources.install.InstallStatus.downloading;
import static com.atlassian.upm.rest.resources.install.InstallStatus.err;
import static com.atlassian.upm.rest.resources.install.InstallStatus.installing;
import static com.google.common.base.Preconditions.checkNotNull;

public class InstallFromUriTask extends InstallTask
{
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private volatile URI uri;
    private final PluginDownloadService downloader;
    private final AuditLogService auditLogger;

    public InstallFromUriTask(URI uri, PluginDownloadService downloader, AuditLogService auditLogger, String username,
                              PluginInstaller pluginInstaller, SelfUpdateController selfUpdateController)
    {
        super(option(uri.toASCIIString()), username, pluginInstaller, selfUpdateController);
        this.uri = checkNotNull(uri, "uri");
        this.downloader = checkNotNull(downloader, "downloader");
        this.auditLogger = checkNotNull(auditLogger, "auditLogger");
    }

    public void accept()
    {
        status = downloading(uri);
    }

    protected URI executeTask() throws Exception
    {
        File downloaded = download();
        
        if (downloaded == null)
        {
            return null;
        }
        else
        {
            status = installing(uri);
            return installFromFile(downloaded);
        }
    }

    private File download() throws AccessDeniedException, ResponseException
    {
        try
        {
            File file = downloader.downloadPlugin(uri, null, null, newProgressTracker());
            if (file == null)
            {
                status = err("upm.pluginInstall.error.install.failed", getSource());
                logger.error("Failed to install plugin", getSource());
                auditLogger.logI18nMessage("upm.auditLog.install.plugin.failure", getSource());
            }
            return file;
        }
        catch (final AccessDeniedException ade)
        {
            status = err("upm.pluginInstall.error.access.denied", getSource());
            logger.error("Access denied while downloading plugin from " + getSource());
            auditLogger.logI18nMessage("upm.auditLog.install.plugin.failure", getSource());
        }
        catch (UnsupportedProtocolException e)
        {
            status = err("upm.pluginInstall.error.unsupported.protocol", getSource());
            logger.error("Error downloading plugin from " + getSource(), e);
            auditLogger.logI18nMessage("upm.auditLog.install.plugin.unsupported.protocol", getSource());
        }
        catch (RelativeURIException e)
        {
            status = err("upm.pluginInstall.error.invalid.relative.uri", getSource());
            logger.error("Error downloading plugin from " + getSource(), e);
            auditLogger.logI18nMessage("upm.auditLog.install.plugin.failure", getSource());
        }
        catch (ResponseException e)
        {
            status = err("upm.pluginInstall.error.response.exception", getSource());
            logger.error("Error downloading plugin from " + getSource());
            auditLogger.logI18nMessage("upm.auditLog.install.plugin.failure", getSource());
        }
        catch (IllegalStateException ise)
        {
            logUnexpectedError(ise);
        }
        catch (Exception e)
        {
            logUnexpectedError(e);
        }

        return null;
    }

    private void logUnexpectedError(Exception e)
    {
        status = err("upm.plugin.error.unexpected.error", getSource());
        logger.error("Failed to install plugin", e);
        auditLogger.logI18nMessage("upm.auditLog.install.plugin.failure", getSource());
    }

    private ProgressTracker newProgressTracker()
    {
        return new ProgressTracker()
        {
            public void notify(Progress progress)
            {
                status = downloading(uri, progress);
            }

            public void redirectedTo(URI newUri)
            {
                uri = newUri;
                status = downloading(uri);
            }
        };
    }
}
