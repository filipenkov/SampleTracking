package com.atlassian.upm.rest.representations;

import java.util.Date;
import java.util.Locale;

import javax.annotation.Nullable;

import com.atlassian.extras.api.ProductLicense;
import com.atlassian.plugins.domain.model.plugin.PluginVersion;
import com.atlassian.plugins.domain.model.product.Product;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.upm.Change;
import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.PluginConfiguration;
import com.atlassian.upm.PluginModuleConfiguration;
import com.atlassian.upm.ProductUpdatePluginCompatibility;
import com.atlassian.upm.Strings;
import com.atlassian.upm.Sys;
import com.atlassian.upm.api.license.entity.Contact;
import com.atlassian.upm.api.license.entity.LicenseError;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.atlassian.upm.license.PluginLicenses;
import com.atlassian.upm.license.internal.HostLicenseProvider;
import com.atlassian.upm.license.internal.LicenseDateFormatter;
import com.atlassian.upm.license.internal.PluginLicenseRepository;
import com.atlassian.upm.osgi.Bundle;
import com.atlassian.upm.osgi.BundleAccessor;
import com.atlassian.upm.osgi.Package;
import com.atlassian.upm.osgi.PackageAccessor;
import com.atlassian.upm.osgi.Service;
import com.atlassian.upm.osgi.ServiceAccessor;
import com.atlassian.upm.osgi.rest.representations.BundleRepresentation;
import com.atlassian.upm.osgi.rest.representations.BundleSummaryRepresentation;
import com.atlassian.upm.osgi.rest.representations.CollectionRepresentation;
import com.atlassian.upm.osgi.rest.representations.PackageRepresentation;
import com.atlassian.upm.osgi.rest.representations.PackageSummaryRepresentation;
import com.atlassian.upm.osgi.rest.representations.ServiceRepresentation;
import com.atlassian.upm.osgi.rest.representations.ServiceSummaryRepresentation;
import com.atlassian.upm.pac.PacClient;
import com.atlassian.upm.pac.PluginVersionPair;
import com.atlassian.upm.rest.UpmUriBuilder;
import com.atlassian.upm.rest.resources.PacStatusResource;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;
import com.atlassian.upm.spi.Plugin;
import com.atlassian.upm.spi.Plugin.Module;
import com.atlassian.upm.test.rest.resources.BuildNumberResource.BuildNumberRepresentation;
import com.atlassian.upm.test.rest.resources.SysResource.IsOnDemandRepresentation;

import com.google.common.base.Function;

import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.transform;

/**
 * This is the implementation of RepresentationFactory. It creates and returns representations. It uses {@code UpmUriBuilder}
 * so that the URIs can be generated in just one place.
 */
public class RepresentationFactoryImpl implements RepresentationFactory
{
    private final PluginAccessorAndController pluginAccessorAndController;
    private final UpmUriBuilder uriBuilder;
    private final LinkBuilder linkBuilder;
    private final BundleAccessor bundleAccessor;
    private final ServiceAccessor serviceAccessor;
    private final PackageAccessor packageAccessor;
    private final PermissionEnforcer permissionEnforcer;
    private final ApplicationProperties applicationProperties;
    private final LicenseDateFormatter licenseDateFormatter;
    private final HostLicenseProvider hostLicenseProvider;
    private final PacClient pacClient;
    private final PluginLicenseRepository licenseRepository;

    public RepresentationFactoryImpl(PluginAccessorAndController pluginAccessorAndController,
        UpmUriBuilder uriBuilder,
        LinkBuilder linkBuilder,
        PacClient pacClient,
        BundleAccessor bundleAccessor,
        ServiceAccessor serviceAccessor,
        PackageAccessor packageAccessor,
        PermissionEnforcer permissionEnforcer,
        ApplicationProperties applicationProperties,
        LicenseDateFormatter licenseDateFormatter,
        HostLicenseProvider hostLicenseProvider,
        PluginLicenseRepository licenseRepository)
    {
        this.pluginAccessorAndController = checkNotNull(pluginAccessorAndController, "pluginAccessorAndController");
        this.uriBuilder = checkNotNull(uriBuilder, "uriBuilder");
        this.linkBuilder = checkNotNull(linkBuilder, "linkBuilder");
        this.bundleAccessor = checkNotNull(bundleAccessor, "bundleAccessor");
        this.serviceAccessor = checkNotNull(serviceAccessor, "serviceAccessor");
        this.packageAccessor = checkNotNull(packageAccessor, "packageAccessor");
        this.permissionEnforcer = checkNotNull(permissionEnforcer, "permissionEnforcer");
        this.applicationProperties = checkNotNull(applicationProperties, "applicationProperties");
        this.licenseDateFormatter = checkNotNull(licenseDateFormatter, "licenseDateFormatter");
        this.hostLicenseProvider = checkNotNull(hostLicenseProvider, "hostLicenseProvider");
        this.pacClient = checkNotNull(pacClient, "pacClient");
        this.licenseRepository = checkNotNull(licenseRepository, "licenseRepository");
    }

    public InstalledPluginCollectionRepresentation createInstalledPluginCollectionRepresentation(Locale locale, Iterable<Plugin> plugins, boolean pacUnreachable, String upmUpdateVersion)
    {
        return new InstalledPluginCollectionRepresentation(pluginAccessorAndController, uriBuilder, linkBuilder, permissionEnforcer,
                                                           locale, plugins, createHostStatusRepresentation(pacUnreachable), upmUpdateVersion);
    }

    public PluginRepresentation createPluginRepresentation(Plugin plugin)
    {
        return new PluginRepresentation(pluginAccessorAndController, checkNotNull(plugin, "plugin"), uriBuilder,
                                        linkBuilder, permissionEnforcer, this);
    }

    public PluginModuleRepresentation createPluginModuleRepresentation(Module module)
    {
        return new PluginModuleRepresentation(pluginAccessorAndController, checkNotNull(module, "module"), uriBuilder, linkBuilder);
    }

    public PacDetailsRepresentation createPacDetailsRepresentation(String pluginKey, PluginVersionPair pluginVersionPair)
    {
        return new PacDetailsRepresentation(pluginVersionPair,
                                            pluginAccessorAndController.getPlugin(pluginKey),
                                            uriBuilder, linkBuilder, pluginAccessorAndController, permissionEnforcer, licenseRepository);
    }

    /**
     * Creates a new error representation
     *
     * @param message the error message
     * @return A {@code ErrorRepresentation} with the error details
     */
    public ErrorRepresentation createErrorRepresentation(String message)
    {
        return new ErrorRepresentation(checkNotNull(message, "message"), null);
    }

    /**
     * Creates a new error representation
     *
     * @param message the error message
     * @return A {@code ErrorRepresentation} with the error details
     */
    public ErrorRepresentation createErrorRepresentation(String message, String subCode)
    {
        return new ErrorRepresentation(checkNotNull(message, "message"), checkNotNull(subCode, "subCode"));
    }

    /**
     * Creates a new error representation
     *
     * @param i18nKey the i18n key
     * @return A {@code ErrorRepresentation} with the error details
     */
    public ErrorRepresentation createI18nErrorRepresentation(String i18nKey)
    {
        return new ErrorRepresentation(null, checkNotNull(i18nKey, "i18nKey"));
    }

    public AvailablePluginCollectionRepresentation createInstallablePluginCollectionRepresentation(Iterable<PluginVersion> plugins, boolean pacUnavailable)
    {
        return new AvailablePluginCollectionRepresentation(plugins, uriBuilder, linkBuilder,
                                                           createHostStatusRepresentation(pacUnavailable));
    }

    public PopularPluginCollectionRepresentation createPopularPluginCollectionRepresentation(Iterable<PluginVersion> plugins, boolean pacUnavailable)
    {
        return new PopularPluginCollectionRepresentation(plugins, uriBuilder, linkBuilder,
                                                         createHostStatusRepresentation(pacUnavailable));
    }

    public SupportedPluginCollectionRepresentation createSupportedPluginCollectionRepresentation(Iterable<PluginVersion> plugins, boolean pacUnavailable)
    {
        return new SupportedPluginCollectionRepresentation(plugins, uriBuilder, linkBuilder,
                                                           createHostStatusRepresentation(pacUnavailable));
    }

    public FeaturedPluginCollectionRepresentation createFeaturedPluginCollectionRepresentation(Iterable<PluginVersion> plugins, boolean pacUnavailable)
    {
        return new FeaturedPluginCollectionRepresentation(plugins, uriBuilder, linkBuilder,
                                                          createHostStatusRepresentation(pacUnavailable));
    }

    public AvailablePluginRepresentation createAvailablePluginRepresentation(PluginVersion plugin)
    {
        return new AvailablePluginRepresentation(checkNotNull(plugin, "plugin"), uriBuilder, linkBuilder, pluginAccessorAndController);
    }

    public ProductUpdatesRepresentation createProductUpdatesRepresentation(Iterable<Product> productVersions, boolean pacUnavailable)
    {
        return new ProductUpdatesRepresentation(uriBuilder, productVersions, linkBuilder,
                                                createHostStatusRepresentation(pacUnavailable));
    }

    public ProductVersionRepresentation createProductVersionRepresentation(boolean development, boolean unknown)
    {
        return new ProductVersionRepresentation(development, unknown);
    }

    public ProductUpdatePluginCompatibilityRepresentation createProductUpdatePluginCompatibilityRepresentation(
            ProductUpdatePluginCompatibility pluginCompatibility,
            Long productUpdateBuildNumber)
    {
        return new ProductUpdatePluginCompatibilityRepresentation(uriBuilder, linkBuilder, pluginAccessorAndController, pluginCompatibility, productUpdateBuildNumber);
    }

    public ChangesRequiringRestartRepresentation createChangesRequiringRestartRepresentation(Iterable<Change> restartChanges)
    {
        return new ChangesRequiringRestartRepresentation(restartChanges, uriBuilder, linkBuilder);
    }

    public CollectionRepresentation<BundleSummaryRepresentation> createOsgiBundleCollectionRepresentation()
    {
        return createOsgiBundleCollectionRepresentation(null);
    }

    public CollectionRepresentation<BundleSummaryRepresentation> createOsgiBundleCollectionRepresentation(@Nullable String term)
    {
        return new CollectionRepresentation<BundleSummaryRepresentation>(
            BundleSummaryRepresentation.wrapSummary(uriBuilder).fromIterable(bundleAccessor.getBundles(term)),
            pluginAccessorAndController.isSafeMode(),
            linkBuilder.buildLinksFor(uriBuilder.buildOsgiBundleCollectionUri(term)).build());
    }

    public BundleRepresentation createOsgiBundleRepresentation(Bundle bundle)
    {
        return new BundleRepresentation(checkNotNull(bundle, "bundle"), uriBuilder);
    }

    public CollectionRepresentation<ServiceSummaryRepresentation> createOsgiServiceCollectionRepresentation()
    {
        return new CollectionRepresentation<ServiceSummaryRepresentation>(
            ServiceSummaryRepresentation.wrapSummary(uriBuilder).fromIterable(serviceAccessor.getServices()),
            pluginAccessorAndController.isSafeMode(),
            linkBuilder.buildLinksFor(uriBuilder.buildOsgiServiceCollectionUri()).build());
    }

    public ServiceRepresentation createOsgiServiceRepresentation(Service service)
    {
        return new ServiceRepresentation(checkNotNull(service, "service"), uriBuilder);
    }

    public CollectionRepresentation<PackageSummaryRepresentation> createOsgiPackageCollectionRepresentation()
    {
        return new CollectionRepresentation<PackageSummaryRepresentation>(
            PackageSummaryRepresentation.wrapSummary(uriBuilder).fromIterable(packageAccessor.getPackages()),
            pluginAccessorAndController.isSafeMode(),
            linkBuilder.buildLinksFor(uriBuilder.buildOsgiPackageCollectionUri()).build());
    }

    public PackageRepresentation createOsgiPackageRepresentation(Package pkg)
    {
        return new PackageRepresentation(checkNotNull(pkg, "pkg"), uriBuilder);
    }

    public SafeModeErrorReenablingPluginRepresentation createSafeModeErrorReenablingPluginRepresentation(PluginConfiguration plugin)
    {
        return new SafeModeErrorReenablingPluginRepresentation(plugin);
    }

    public SafeModeErrorReenablingPluginModuleRepresentation createSafeModeErrorReenablingPluginModuleRepresentation(PluginConfiguration plugin, PluginModuleConfiguration module)
    {
        return new SafeModeErrorReenablingPluginModuleRepresentation(plugin, module);
    }

    public BuildNumberRepresentation createBuildNumberRepresentation(String buildNumber)
    {
        if (buildNumber == null)
        {
            buildNumber = applicationProperties.getBuildNumber();
        }

        return new BuildNumberRepresentation(buildNumber);
    }

    public IsOnDemandRepresentation createIsOnDemandRepresentation(Boolean isOnDemand)
    {
        if (isOnDemand == null)
        {
            isOnDemand = Sys.isOnDemand();
        }

        return new IsOnDemandRepresentation(isOnDemand);
    }
    
    public PacStatusResource.PacStatusRepresentation createPacStatusRepresentation(boolean disabled, boolean reached)
    {
        return new PacStatusResource.PacStatusRepresentation(disabled, reached, uriBuilder, linkBuilder);
    }
    
    public LicenseDetailsRepresentation createPluginLicenseRepresentation(PluginLicense pluginLicense)
    {
        return new LicenseDetailsRepresentation(pluginLicense.isValid(),
                                                pluginLicense.getError().getOrElse((LicenseError) null),
                                                pluginLicense.isEvaluation(),
                                                PluginLicenses.isNearlyExpired().apply(pluginLicense),
                                                pluginLicense.getMaximumNumberOfUsers().getOrElse((Integer)null),
                                                pluginLicense.getMaintenanceExpiryDate().map(toDate).getOrElse((Date)null),
                                                pluginLicense.getLicenseType(),
                                                pluginLicense.isEvaluation() ? pluginLicense.getExpiryDate().map(toDate).getOrElse((Date)null) : null,
                                                pluginLicense.getRawLicense(),
                                                pluginLicense.getMaintenanceExpiryDate().map(format(licenseDateFormatter)).getOrElse((String) null),
                                                pluginLicense.getSupportEntitlementNumber().getOrElse((String)null),
                                                pluginLicense.getOrganization().getName(),
                                                getContactsEmail(pluginLicense.getContacts()));
    }
    
    public HostStatusRepresentation createHostStatusRepresentation(boolean pacUnavailable)
    {
        return new HostStatusRepresentation(pluginAccessorAndController.isSafeMode(),
                                            pacClient.isPacDisabled(),
                                            pacUnavailable,
                                            createHostLicenseRepresentation());
    }
    
    public LicenseDetailsRepresentation createHostLicenseRepresentation()
    {
        for (ProductLicense productLicense : hostLicenseProvider.getHostApplicationLicense())
        {
            // We're omitting some of the properties that would be provided for a plugin license, just
            // because they're somewhat annoying to compute and they're not used by the front end.
            return new LicenseDetailsRepresentation(!productLicense.isExpired(),
                                                    null,
                                                    productLicense.isEvaluation(),
                                                    false,
                                                    productLicense.getMaximumNumberOfUsers(),
                                                    null,
                                                    null,
                                                    productLicense.getExpiryDate(),
                                                    null,
                                                    null,
                                                    productLicense.getSupportEntitlementNumber(),
                                                    productLicense.getOrganisation().getName(),
                                                    getHostContactsEmail(productLicense.getContacts()));
        }
        return null;
    }

    private String getContactsEmail(Iterable<Contact> contacts)
    {
        Iterable<String> emails = transform(contacts, new Function<Contact, String>()
        {
            @Override
            public String apply(Contact contact)
            {
                return contact.getEmail();
            }
        });

        return Strings.getFirstNonEmpty(emails).getOrElse((String)null);
    }

    private String getHostContactsEmail(Iterable<com.atlassian.extras.api.Contact> contacts)
    {
        Iterable<String> emails = transform(contacts, new Function<com.atlassian.extras.api.Contact, String>()
        {
            @Override
            public String apply(com.atlassian.extras.api.Contact contact)
            {
                return contact.getEmail();
            }
        });

        return Strings.getFirstNonEmpty(emails).getOrElse((String)null);
    }
    
    private static final Function<DateTime, Date> toDate = new Function<DateTime, Date>()
    {
        @Override
        public Date apply(DateTime dateTime)
        {
            return dateTime.toDate();
        }
    };


    private Function<DateTime, String> format(final LicenseDateFormatter dateFormatter)
    {
        return new Function<DateTime, String>()
        {
            @Override
            public String apply(DateTime dateTime)
            {
                return dateFormatter.format(dateTime);
            }
        };
    }
}
