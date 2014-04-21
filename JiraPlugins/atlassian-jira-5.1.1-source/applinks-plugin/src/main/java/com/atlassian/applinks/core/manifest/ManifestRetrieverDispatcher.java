package com.atlassian.applinks.core.manifest;

import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.core.plugin.ApplicationTypeModuleDescriptor;
import com.atlassian.applinks.spi.Manifest;
import com.atlassian.applinks.spi.manifest.ApplicationStatus;
import com.atlassian.applinks.spi.manifest.ManifestNotFoundException;
import com.atlassian.applinks.spi.manifest.ManifestProducer;
import com.atlassian.applinks.spi.manifest.ManifestRetriever;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.predicate.ModuleDescriptorPredicate;
import com.google.common.collect.Iterables;

import java.net.URI;
import java.util.Collection;

/**
 * Simple component for retrieving manifests from remote applications.
 *
 * @since   3.0
 */
public class ManifestRetrieverDispatcher implements ManifestRetriever
{
    private final AppLinksManifestDownloader downloader;
    private final PluginAccessor pluginAccessor;

    public ManifestRetrieverDispatcher(final AppLinksManifestDownloader downloader,
                             final PluginAccessor pluginAccessor)
    {
        this.downloader = downloader;
        this.pluginAccessor = pluginAccessor;
    }

    /**
     * Useful only for peers that ship with AppLinks (and serve a manifest at
     * the standardised rest endpoint).
     *
     * @param uri
     * @return
     * @throws ManifestNotFoundException
     */
    public Manifest getManifest(final URI uri) throws ManifestNotFoundException
    {
        return downloader.download(uri);
    }

    public Manifest getManifest(final URI uri, final ApplicationType type) throws ManifestNotFoundException
    {
        return getRequiredManifestProducer(type).getManifest(uri);
    }

    public ApplicationStatus getApplicationStatus(final URI uri, final ApplicationType type)
    {
        return getRequiredManifestProducer(type).getStatus(uri);
    }

    /**
     * @throws IllegalStateException    when a {@code com.atlassian.applinks.spi.manifest.ManifestProducer}
     * could not be loaded.
     */
    private ManifestProducer getRequiredManifestProducer(final ApplicationType type)
            throws IllegalStateException
    {
        final Collection<ModuleDescriptor<ApplicationType>> descriptors =
                pluginAccessor.getModuleDescriptors(new ModuleDescriptorPredicate<ApplicationType>()
                {
                    public boolean matches(ModuleDescriptor<? extends ApplicationType> moduleDescriptor)
                    {
                        return moduleDescriptor instanceof ApplicationTypeModuleDescriptor &&
                                type.getClass().isAssignableFrom(moduleDescriptor.getModule().getClass());
                    }
                });
        if (!descriptors.isEmpty())
        {
            // TODO: what if we have more than one? This could happen with hierarchical AppTypes (which we don't have atm)
            return ((ApplicationTypeModuleDescriptor) Iterables.get(descriptors, 0))
                    .getManifestProducer();
        }
        else
        {
            throw new IllegalStateException("Cannot query application status for unknown application type \"" +
                    type.getClass().getName() + "\"");
        }
    }
}
