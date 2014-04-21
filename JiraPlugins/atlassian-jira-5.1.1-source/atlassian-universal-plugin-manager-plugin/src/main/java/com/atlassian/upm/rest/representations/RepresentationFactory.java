package com.atlassian.upm.rest.representations;

import java.util.Locale;

import com.atlassian.plugins.domain.model.plugin.PluginVersion;
import com.atlassian.plugins.domain.model.product.Product;
import com.atlassian.upm.Change;
import com.atlassian.upm.PluginConfiguration;
import com.atlassian.upm.PluginModuleConfiguration;
import com.atlassian.upm.ProductUpdatePluginCompatibility;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.atlassian.upm.osgi.Bundle;
import com.atlassian.upm.osgi.Package;
import com.atlassian.upm.osgi.Service;
import com.atlassian.upm.osgi.rest.representations.BundleRepresentation;
import com.atlassian.upm.osgi.rest.representations.BundleSummaryRepresentation;
import com.atlassian.upm.osgi.rest.representations.CollectionRepresentation;
import com.atlassian.upm.osgi.rest.representations.PackageRepresentation;
import com.atlassian.upm.osgi.rest.representations.PackageSummaryRepresentation;
import com.atlassian.upm.osgi.rest.representations.ServiceRepresentation;
import com.atlassian.upm.osgi.rest.representations.ServiceSummaryRepresentation;
import com.atlassian.upm.pac.PluginVersionPair;
import com.atlassian.upm.rest.resources.PacStatusResource;
import com.atlassian.upm.spi.Plugin;
import com.atlassian.upm.spi.Plugin.Module;
import com.atlassian.upm.test.rest.resources.BuildNumberResource.BuildNumberRepresentation;
import com.atlassian.upm.test.rest.resources.SysResource.IsOnDemandRepresentation;

/**
 * Factory to construct new REST representations.
 */
public interface RepresentationFactory
{
    /**
     * Creates a new representation of the plugins installed in the current configuration.
     *
     * @param locale the current Locale, used to define sort order of plugins
     * @param plugins the collection of plugins to include in the representation
     * @param pacUnreachable  true if the most recent PAC request failed
     * @param upmUpdateVersion version string for the latest UPM, if a newer build is available
     * @return A {@code InstalledPluginCollectionRepresentation} containing some info ({@code PluginEntry}s) about the current plugins
     */
    InstalledPluginCollectionRepresentation createInstalledPluginCollectionRepresentation(Locale locale, Iterable<Plugin> plugins, boolean pacUnreachable, String upmUpdateVersion);

    /**
     * Given a {@code Plugin}, this creates a new representation of it.
     *
     * @param plugin the plugin to represent
     * @return A {@code PluginRepresentation} containing the details of the plugin
     */
    PluginRepresentation createPluginRepresentation(Plugin plugin);

    /**
     * Given a {@code ModuleDescriptor}, this creates a new representation of it.
     *
     * @param module the module to represent
     * @return A {@code PluginModuleRepresentation} containing the details of the module
     */
    PluginModuleRepresentation createPluginModuleRepresentation(Module module);

    /**
     * Creates a new {@code PacDetailsRepresentation} using the specified {@code PluginVersions}.
     *
     * @param pluginKey plugin key
     * @param pluginVersionPair plugin versions
     * @return a {@code PacDetailsRepresentation}
     */
    PacDetailsRepresentation createPacDetailsRepresentation(String pluginKey, PluginVersionPair pluginVersionPair);

    /**
     * Creates a new {@code ErrorRepresentation}, using the specified {@code message} as error message.
     *
     * @param message the error message
     * @return A {@code ErrorRepresentation} with the error details
     */
    ErrorRepresentation createErrorRepresentation(String message);

    /**
     * Creates a new {@code ErrorRepresentation}, using the specified {@code message} and {@code subCode}.
     *
     * @param message the error message
     * @param subCode the error sub code
     * @return A {@code ErrorRepresentation} with the error details
     */
    ErrorRepresentation createErrorRepresentation(String message, String subCode);

    /**
     * Creates a new I18n {@code ErrorRepresentation}, passing the specified {@code i18nKey} error key
     *
     * @param i18nKey the i18n key
     * @return A {@code ErrorRepresentation} with the error details
     */
    ErrorRepresentation createI18nErrorRepresentation(String i18nKey);

    /**
     * Creates a new {@code AvailablePluginCollectionRepresentation} from a collection of installable plugins
     *
     * @param plugins the collection of plugins to include in the representation
     * @param pacUnreachable  true if the most recent PAC request failed
     * @return an {@code AvailablePluginCollectionRepresentation} built from the passed-in plugins
     */
    AvailablePluginCollectionRepresentation createInstallablePluginCollectionRepresentation(Iterable<PluginVersion> plugins, boolean pacUnreachable);

    /**
     * Creates a new {@code PopularPluginCollectionRepresentation} from a collection of popular plugins
     *
     * @param plugins the collection of plugins to include in the representation
     * @param pacUnreachable  true if the most recent PAC request failed
     * @return a {@code PopularPluginCollectionRepresentation} built from the passed-in plugins
     */
    PopularPluginCollectionRepresentation createPopularPluginCollectionRepresentation(Iterable<PluginVersion> plugins, boolean pacUnreachable);

    /**
     * Creates a new {@code SupportedPluginCollectionRepresentation} from a collection of supported plugins
     *
     * @param plugins the collection of plugins to include in the representation
     * @param pacUnreachable  true if the most recent PAC request failed
     * @return a {@code SupportedPluginCollectionRepresentation} built from the passed-in plugins
     */
    SupportedPluginCollectionRepresentation createSupportedPluginCollectionRepresentation(Iterable<PluginVersion> plugins, boolean pacUnreachable);

    /**
     * Creates a new {@code FeaturedPluginCollectionRepresentation} from a collection of featured plugins
     *
     * @param plugins the collection of plugins to include in the representation
     * @param pacUnreachable  true if the most recent PAC request failed
     * @return a {@code FeaturedPluginCollectionRepresentation} built from the passed-in plugins
     */
    FeaturedPluginCollectionRepresentation createFeaturedPluginCollectionRepresentation(Iterable<PluginVersion> plugins, boolean pacUnreachable);

    AvailablePluginRepresentation createAvailablePluginRepresentation(PluginVersion plugin);

    /**
     * Creates a new {@code ProductUpdatesRepresentation} from a collection of product versions
     *
     * @param productVersions the collection of versions to include in the representation
     * @param pacUnreachable  true if the most recent PAC request failed
     * @return a {@code ProductUpdatesRepresentation} built from the passed-in versions
     */
    ProductUpdatesRepresentation createProductUpdatesRepresentation(Iterable<Product> productVersions, boolean pacUnreachable);

    /**
     * Creates a new {@code ProductVersionRepresentation} representing the current product version state.
     *
     * @param development true if the current product version is a development version (e.g. milestone or snapshot)
     * @param unknown true if the current product version is an unknown version (e.g. not registered in PAC)
     * @return a {@code ProductVersionRepresentation} from the current product version state
     */
    ProductVersionRepresentation createProductVersionRepresentation(boolean development, boolean unknown);

    /**
     * Creates a new {@code ProductUpdatePluginCompatibilityRepresentation} from a collection of plugin compatibility
     * statuses
     *
     * @param pluginCompatibility statuses to use in constructing the representation
     * @param productUpdateBuildNumber build number of the product update that the plugin statuses are against
     * @return the {@code ProductUpdatePluginCompatibilityRepresentation}
     */
    ProductUpdatePluginCompatibilityRepresentation createProductUpdatePluginCompatibilityRepresentation(
            ProductUpdatePluginCompatibility pluginCompatibility,
            Long productUpdateBuildNumber);

    ChangesRequiringRestartRepresentation createChangesRequiringRestartRepresentation(Iterable<Change> restartChanges);

    /**
     * Create a new {@code CollectionRepresentation<BundleSummaryRepresentation>}
     *
     * @return a representation of a collection of all OSGi bundles
     */
    CollectionRepresentation<BundleSummaryRepresentation> createOsgiBundleCollectionRepresentation();

    /**
     * Create a new {@code CollectionRepresentation<BundleSummaryRepresentation>}
     *
     * @param term the term that must be matched by all bundles in the collection
     * @return a representation of a collection of all OSGi bundles matching the term
     */
    CollectionRepresentation<BundleSummaryRepresentation> createOsgiBundleCollectionRepresentation(String term);

    /**
     * Creates a new {@code BundleRepresentation}
     *
     * @param bundle the bundle to represent
     * @return the representation of the bundle
     */
    BundleRepresentation createOsgiBundleRepresentation(Bundle bundle);

    /**
     * Create a new {@code CollectionRepresentation<ServiceSummaryRepresentation>}
     *
     * @return a representation of a collection of all OSGi services
     */
    CollectionRepresentation<ServiceSummaryRepresentation> createOsgiServiceCollectionRepresentation();

    /**
     * Creates a new {@code ServiceRepresentation}
     *
     * @param service the service to represent
     * @return the representation of the service
     */
    ServiceRepresentation createOsgiServiceRepresentation(Service service);

    /**
     * Create a new {@code CollectionRepresentation<PackageSummaryRepresentation>}
     *
     * @return a representation of a collection of all OSGi packages
     */
    CollectionRepresentation<PackageSummaryRepresentation> createOsgiPackageCollectionRepresentation();

    /**
     * Creates a new {@code PackageRepresentation}
     *
     * @param pkg the package to represent
     * @return the representation of the package
     */
    PackageRepresentation createOsgiPackageRepresentation(Package pkg);

    SafeModeErrorReenablingPluginRepresentation createSafeModeErrorReenablingPluginRepresentation(PluginConfiguration plugin);

    SafeModeErrorReenablingPluginModuleRepresentation createSafeModeErrorReenablingPluginModuleRepresentation(PluginConfiguration plugin, PluginModuleConfiguration module);

    /**
     * Creates a new {@code BuildNumberRepresentation}
     *
     * @param buildNumber the build number to represent
     * @return the representation of the build number
     */
    BuildNumberRepresentation createBuildNumberRepresentation(String buildNumber);

    /**
     * Creates a new {@code IsOnDemandRepresentation}
     *
     * @param isOnDemand if this is an on demand system
     * @return the representation of the on demand status
     */
    IsOnDemandRepresentation createIsOnDemandRepresentation(Boolean isOnDemand);

    /**
     * Creates a new {@code PacStatusResource.PacStatusRepresentation}
     *
     * @param disabled true if PAC is disabled (UPM works in offline mode)
     * @param reached true if PAC is could be reached (the value if undefined if PAC is disabled)
     * @return the representation of the status of PAC
     */
    PacStatusResource.PacStatusRepresentation createPacStatusRepresentation(boolean disabled, boolean reached);

    /**
     * Creates a {@link LicenseDetailsRepresentation} from a plugin license.
     * 
     * @param license  the plugin license
     * @return the representation of the plugin license
     */
    LicenseDetailsRepresentation createPluginLicenseRepresentation(PluginLicense license);
    
    /**
     * Creates a {@link HostStatusRepresentation} containing information about the host application and PAC states.
     * @param pacUnreachable  true if the most recent PAC request failed
     */
    HostStatusRepresentation createHostStatusRepresentation(boolean pacUnreachable);
    
    /**
     * Creates a {@link LicenseDetailsRepresentation} containing properties of the host product license.
     * @return the representation of the product license
     */
    LicenseDetailsRepresentation createHostLicenseRepresentation();
}
