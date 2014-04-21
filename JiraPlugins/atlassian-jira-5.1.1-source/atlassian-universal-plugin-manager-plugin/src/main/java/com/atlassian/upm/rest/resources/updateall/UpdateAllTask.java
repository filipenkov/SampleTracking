package com.atlassian.upm.rest.resources.updateall;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import com.atlassian.plugins.PacException;
import com.atlassian.plugins.domain.model.plugin.PluginVersion;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.upm.AccessDeniedException;
import com.atlassian.upm.LegacyPluginsUnsupportedException;
import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.PluginDownloadService;
import com.atlassian.upm.PluginDownloadService.Progress;
import com.atlassian.upm.PluginDownloadService.ProgressTracker;
import com.atlassian.upm.PluginInstaller;
import com.atlassian.upm.RelativeURIException;
import com.atlassian.upm.SafeModeException;
import com.atlassian.upm.UnknownPluginTypeException;
import com.atlassian.upm.UnrecognisedPluginVersionException;
import com.atlassian.upm.UnsupportedProtocolException;
import com.atlassian.upm.XmlPluginsUnsupportedException;
import com.atlassian.upm.api.util.Either;
import com.atlassian.upm.license.internal.PluginLicenseRepository;
import com.atlassian.upm.log.AuditLogService;
import com.atlassian.upm.pac.PacClient;
import com.atlassian.upm.rest.UpmUriBuilder;
import com.atlassian.upm.rest.async.AsynchronousTask;
import com.atlassian.upm.rest.representations.PluginRepresentation;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.upm.pac.PluginVersions.isLicensedToBeUpdated;
import static com.atlassian.upm.rest.resources.updateall.UpdateStatus.err;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Iterables.filter;

public final class UpdateAllTask extends AsynchronousTask<UpdateStatus>
{
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final PacClient pacClient;
    private final PluginAccessorAndController pluginAccessorAndController;
    private final PluginInstaller pluginInstaller;
    private final PluginDownloadService pluginDownloadService;
    private final AuditLogService auditLogger;
    private final UpmUriBuilder uriBuilder;
    private final PluginLicenseRepository licenseRepository;

    public UpdateAllTask(PacClient pacClient,
                         PluginDownloadService pluginDownloadService,
                         PluginAccessorAndController pluginAccessorAndController,
                         PluginInstaller pluginInstaller,
                         AuditLogService auditLogger,
                         UpmUriBuilder uriBuilder,
                         PluginLicenseRepository licenseRepository,
                         String username)
    {
        super(Type.UPDATE_ALL, username);
        this.pluginDownloadService = pluginDownloadService;
        this.pluginAccessorAndController = pluginAccessorAndController;
        this.pluginInstaller = pluginInstaller;
        this.pacClient = pacClient;
        this.auditLogger = auditLogger;
        this.uriBuilder = uriBuilder;
        this.licenseRepository = licenseRepository;
    }

    public void accept()
    {
        status = findingUpdates();
    }

    public URI call()
    {
        try
        {
            status = update(download(findUpdates()));
        }
        catch (PacException pe)
        {
            status = err("err.finding.updates");
            logger.error("Failed to find available updates: " + pe);
        }
        catch (Throwable t)
        {
            status = err("unexpected.exception");
            logger.error("Failed to update all plugins", t);
        }
        return null;
    }

    UpdateAllResults getResults()
    {
        if (status.isDone())
        {
            return (UpdateAllResults) status;
        }
        return null;
    }

    static final UpdateStatus FINDING_UPDATES = new UpdateStatus(UpdateStatus.State.FINDING_UPDATES);

    static UpdateStatus findingUpdates()
    {
        return FINDING_UPDATES;
    }

    private Iterable<PluginVersion> findUpdates()
    {
        return filter(pacClient.getUpdates(), not(isUpmPluginVersion));
    }
    
    private Predicate<PluginVersion> isUpmPluginVersion = new Predicate<PluginVersion>()
    {
        public boolean apply(PluginVersion pv)
        {
            return pluginAccessorAndController.getUpmPluginKey().equals(pv.getPlugin().getPluginKey());
        }
    };

    static UpdateStatus downloading(final PluginVersion pluginVersion, int numberComplete, int totalUpdates) throws URISyntaxException
    {
        return new DownloadingPluginStatus(pluginVersion, numberComplete, totalUpdates);
    }

    static UpdateStatus downloading(final PluginVersion pluginVersion, Progress progress, int numberComplete, int totalUpdates) throws URISyntaxException
    {
        return new DownloadingPluginStatus(pluginVersion, progress, numberComplete, totalUpdates);
    }

    static UpdateStatus downloading(final PluginVersion pluginVersion, URI redirectedUri, int numberComplete, int totalUpdates)
    {
        return new DownloadingPluginStatus(pluginVersion, redirectedUri, numberComplete, totalUpdates);
    }

    private Map<PluginVersion, Either<File, UpdateFailed>> download(Iterable<PluginVersion> updates)
    {
        ImmutableMap.Builder<PluginVersion, Either<File, UpdateFailed>> updateFiles = ImmutableMap.builder();
        for (PluginVersion update : updates)
        {
            Either<File, UpdateFailed> result;
            try
            {
                if (update.getPlugin().getDeployable())
                {
                    int numberComplete = updateFiles.build().size();
                    int totalUpdates = Iterables.size(updates);
                    status = downloading(update, numberComplete, totalUpdates);
                    result = Either.left(
                        pluginDownloadService.downloadPlugin(new URI(update.getBinaryUrl().trim()), null, null, newProgressTracker(update, numberComplete, totalUpdates)));
                }
                else
                {
                    result = Either.right(new UpdateFailed(UpdateFailed.Type.INSTALL, "not.deployable", update));
                    auditLogger.logI18nMessage("upm.auditLog.update.plugin.failure", update.getPlugin().getName(), update.getBinaryUrl());
                }
            }
            catch (AccessDeniedException e)
            {
                result = Either.right(new UpdateFailed(UpdateFailed.Type.DOWNLOAD, "access.denied", update));
                logger.error("Access denied when downloading " + update.getBinaryUrl(), e);
                auditLogger.logI18nMessage("upm.auditLog.update.plugin.failure", update.getPlugin().getName(), update.getBinaryUrl());
            }
            catch (UnsupportedProtocolException e)
            {
                result = Either.right(new UpdateFailed(UpdateFailed.Type.DOWNLOAD, "unsupported.protocol", update, e.getMessage()));
                logger.error("Failed to download plugin " + update.getBinaryUrl(), e);
                auditLogger.logI18nMessage("upm.auditLog.update.plugin.failure", update.getPlugin().getName(), update.getBinaryUrl());
            }
            catch (RelativeURIException e)
            {
                result = Either.right(new UpdateFailed(UpdateFailed.Type.DOWNLOAD, "invalid.relative.uri", update, e.getMessage()));
                logger.error("Failed to download plugin " + update.getBinaryUrl(), e);
                auditLogger.logI18nMessage("upm.auditLog.update.plugin.failure", update.getPlugin().getName(), update.getBinaryUrl());
            }
            catch (ResponseException e)
            {
                result = Either.right(new UpdateFailed(UpdateFailed.Type.DOWNLOAD, "response.exception", update, e.getMessage()));
                logger.error("Failed to download " + update.getBinaryUrl(), e);
                auditLogger.logI18nMessage("upm.auditLog.update.plugin.failure", update.getPlugin().getName(), update.getBinaryUrl());
            }
            catch (URISyntaxException e)
            {
                result = Either.right(new UpdateFailed(UpdateFailed.Type.DOWNLOAD, "invalid.uri.syntax", update, e.getMessage()));
                logger.error("Invalid plugin binary URL " + update.getBinaryUrl(), e);
                auditLogger.logI18nMessage("upm.auditLog.update.plugin.failure", update.getPlugin().getName(), update.getBinaryUrl());
            }
            updateFiles.put(update, result);
        }
        return updateFiles.build();
    }

    private ProgressTracker newProgressTracker(final PluginVersion pluginVersion, final int numberComplete, final int totalUpdates)
    {
        return new ProgressTracker()
        {
            public void notify(Progress progress)
            {
                try
                {
                    status = downloading(pluginVersion, progress, numberComplete, totalUpdates);
                }
                catch (URISyntaxException e)
                {
                    // Won't ever happen.  If the URI was bad, it would have failed long before now.  
                }
            }

            public void redirectedTo(URI newUri)
            {
                status = downloading(pluginVersion, newUri, numberComplete, totalUpdates);
            }
        };
    }

    static UpdateStatus updating(final PluginVersion pluginVersion, int numberComplete, int totalUpdates)
    {
        return new UpdatingPluginStatus(pluginVersion, numberComplete, totalUpdates);
    }

    private UpdateStatus update(Map<PluginVersion, Either<File, UpdateFailed>> updates)
    {
        boolean updateRequiresRestart = false;
        final ImmutableList.Builder<UpdateSucceeded> successes = ImmutableList.builder();
        final ImmutableList.Builder<UpdateFailed> failures = ImmutableList.builder();
        for (Map.Entry<PluginVersion, Either<File, UpdateFailed>> update : updates.entrySet())
        {
            PluginVersion pluginVersion = update.getKey();
            if (!isLicensedToBeUpdated(licenseRepository).apply(pluginVersion))
            {
                //don't install updates which aren't compatible with current licenses.
                failures.add(new UpdateFailed(UpdateFailed.Type.INSTALL, "not.licensed.to.be.updated", pluginVersion));
                auditLogger.logI18nMessage("upm.auditLog.update.plugin.failure", pluginVersion.getPlugin().getName());
            }
            else if (update.getValue().isLeft())
            {
                try
                {
                    int numberComplete = successes.build().size() + failures.build().size();
                    int totalUpdates = updates.size();
                    status = updating(pluginVersion, numberComplete, totalUpdates);
                    final PluginRepresentation pluginRepresentation = pluginInstaller.update(update.getValue().left().get(), pluginVersion.getPlugin().getName());
                    if (pluginRepresentation.getChangeRequiringRestartLink() != null)
                    {
                        updateRequiresRestart = true;
                    }
                    successes.add(new UpdateSucceeded(pluginVersion));
                }
                catch (LegacyPluginsUnsupportedException lpue)
                {
                    failures.add(new UpdateFailed(UpdateFailed.Type.INSTALL, "legacy.plugins.unsupported", pluginVersion));
                }
                catch (XmlPluginsUnsupportedException lpue)
                {
                    failures.add(new UpdateFailed(UpdateFailed.Type.INSTALL, "xml.plugins.unsupported", pluginVersion));
                }
                catch (UnrecognisedPluginVersionException upve) 
                {
                    failures.add(new UpdateFailed(UpdateFailed.Type.INSTALL, "unrecognised.plugin.version", pluginVersion));
                }
                catch (UnknownPluginTypeException upte)
                {
                    failures.add(new UpdateFailed(UpdateFailed.Type.INSTALL, "unknown.plugin.type", pluginVersion));
                }
                catch (SafeModeException sme)
                {
                    failures.add(new UpdateFailed(UpdateFailed.Type.INSTALL, "safe.mode", pluginVersion));
                }
                catch (RuntimeException re)
                {
                    failures.add(new UpdateFailed(UpdateFailed.Type.INSTALL, "install.failed", pluginVersion, re.getMessage()));
                }
            }
            else
            {
                failures.add(update.getValue().right().get());
            }
        }

        // UPM-884 - need to get UPM to check for requires restart for the updated plugins
        final Map<String, URI> links;
        if (updateRequiresRestart)
        {
            links = of("changes-requiring-restart", uriBuilder.buildChangesRequiringRestartUri());
        }
        else
        {
            links = of();
        }

        return new UpdateAllResults(successes.build(), failures.build(), links);
    }
}
