package com.atlassian.upm.pac;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.atlassian.plugins.PacException;
import com.atlassian.plugins.domain.model.plugin.PluginCompatibilityStatus;
import com.atlassian.plugins.domain.model.plugin.PluginVersion;
import com.atlassian.plugins.domain.model.product.Product;
import com.atlassian.plugins.domain.model.product.ProductCompatibility;
import com.atlassian.plugins.service.plugin.PluginVersionService;
import com.atlassian.plugins.service.product.ProductService;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.ProductUpdatePluginCompatibility;
import com.atlassian.upm.Sys;
import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.osgi.PackageAccessor;
import com.atlassian.upm.spi.Plugin;
import com.atlassian.upm.test.rest.resources.BuildNumberResource;
import com.atlassian.util.concurrent.LazyReference;
import com.atlassian.util.concurrent.ResettableLazyReference;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.upm.api.util.Option.none;
import static com.atlassian.upm.api.util.Option.option;
import static com.atlassian.upm.api.util.Option.some;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.getLast;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.transformValues;
import static com.google.common.collect.Maps.uniqueIndex;
import static java.lang.Math.min;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public final class PacClientImpl implements PacClient
{
    private final ApplicationProperties applicationProperties;
    private final PluginAccessorAndController manager;
    private final PacServiceFactory factory;
    private final PluginVersionComparator pluginVersionComparator;
    private final SpiPluginComparator pluginComparator;
    private final LazyReference<Option<Boolean>> development;
    private final ResettableLazyReference<Option<Boolean>> unknown;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public PacClientImpl(final ApplicationProperties applicationProperties,
                         final PluginAccessorAndController manager,
                         final PacServiceFactory factory,
                         final PackageAccessor packageAccessor)
    {
        this.applicationProperties = checkNotNull(applicationProperties, "applicationProperties");
        this.manager = checkNotNull(manager, "manager");
        this.factory = checkNotNull(factory, "factory");
        this.pluginVersionComparator = new PluginVersionComparator();
        this.pluginComparator = new SpiPluginComparator();

        development = new LazyReference<Option<Boolean>>()
        {
            protected Option<Boolean> create() throws Exception
            {
                String productPackage = "com.atlassian." + applicationProperties.getDisplayName().toLowerCase();
                Iterable<com.atlassian.upm.osgi.Package> packages = packageAccessor.getExportedPackages(0L, productPackage);
                if (Iterables.isEmpty(packages))
                {
                    //something went wrong. this should never happen
                    logger.warn("Could not find product package in the system bundle: " + productPackage);
                    return none(Boolean.class);
                }
                return some(isNotBlank(getLast(packages).getVersion().getQualifier()));
            }
        };
        unknown = new ResettableLazyReference<Option<Boolean>>()
        {
            protected Option<Boolean> create() throws Exception
            {
                try
                {
                    return some(getBuildNumber() > getLatestProductVersion());
                }
                catch (Exception e)
                {
                    return none(Boolean.class);
                }
            }
        };
    }

    public boolean isPacDisabled()
    {
        return Sys.isPacDisabled();
    }

    public Iterable<PluginVersion> getAvailable(String query, Integer max, Integer offset)
    {
        if (!isPacReachable())
        {
            return Collections.emptyList();
        }
        return paginate(filter(findCompatiblePlugins(query), not(installed)), max, offset);
    }

    public Iterable<PluginVersion> getPopular(Integer max, Integer offset)
    {
        if (!isPacReachable())
        {
            return Collections.emptyList();
        }
        return paginate(filter(findPopularPlugins(), not(installed)), max, offset);
    }

    public Iterable<PluginVersion> getSupported(Integer max, Integer offset)
    {
        if (!isPacReachable())
        {
            return Collections.emptyList();
        }
        return paginate(filter(findSupportedPlugins(), not(installed)), max, offset);
    }

    public Iterable<PluginVersion> getFeatured(Integer max, Integer offset)
    {
        if (!isPacReachable())
        {
            return Collections.emptyList();
        }
        return paginate(filter(findFeaturedPlugins(), not(installed)), max, offset);
    }

    public Iterable<PluginVersion> getUpdates()
    {
        if (!isPacReachable())
        {
            return Collections.emptyList();
        }
        PluginVersionService pluginVersions = factory.getPluginVersionService();
        Map<String, String> installedPluginVersions = getUpdatablePlugins();
        List<PluginVersion> updatablePluginsFromPac = pluginVersions.findUpdates(
            applicationProperties.getDisplayName().toLowerCase(),
            getQueryBuildNumber(),
            installedPluginVersions,
            /* showBeta */ null,
            /* max */ null,
            /* offset */ null,
            ImmutableList.of("plugin"));
        return sortVersions(updatablePluginsFromPac);
    }

    /**
     * Returns the build number with which to query PAC.
     *
     * UPM-1020: If the current version is known by PAC, return the current version.
     * If the current version is not known by PAC and this is an unknown release, return the current version.
     * If the current version is not known by PAC and this is a development version (e.g. snapshot or milestone),
     * return the latest product version known by PAC.
     *
     * @return the build number with which to query PAC.
     */
    private long getQueryBuildNumber()
    {
        return isUnknownProductVersion().getOrElse(false) && isDevelopmentProductVersion().getOrElse(false) ? getLatestProductVersion() : getBuildNumber();
    }

    private Map<String, String> getUpdatablePlugins()
    {
        return transformValues(uniqueIndex(filter(manager.getPlugins(), and(not(system), not(waitingForRestart))),
                                           toPluginKeys), toVersions);
    }

    private long getBuildNumber()
    {
        //UPM-871 to allow for blitz testing of newer products
        String buildNumber = BuildNumberResource.getBuildNumber() != null ? BuildNumberResource.getBuildNumber() : applicationProperties.getBuildNumber();
        // Fisheye dev build numbers start with "dev-", so we'll parse that out.
        if (buildNumber.startsWith("dev-"))
        {
            buildNumber = buildNumber.substring(4);
        }
        return Long.parseLong(buildNumber);
    }

    public Iterable<Product> getProductUpdates()
    {
        if (!isPacReachable())
        {
            return Collections.emptyList();
        }
        ProductService productService = factory.getProductService();
        return productService.getProductVersionsAfterVersion(applicationProperties.getDisplayName().toLowerCase(),
                                                             getBuildNumber());
    }

    public Option<Boolean> isUnknownProductVersion()
    {
        return isPacDisabled() ? none(Boolean.class) : unknown.get();
    }

    public Option<Boolean> isDevelopmentProductVersion()
    {
        return development.get();
    }

    public boolean isPacReachable()
    {
        // Disabled PAC implies unreachable PAC
        if (isPacDisabled())
        {
            return false;
        }

        try
        {
            return unknown.get().isDefined();
        }
        catch (Exception e)
        {
            logger.warn("Unable to determine whether PAC is reachable", e);
            return false;
        }
    }
    
    public void forgetPacReachableState()
    {
        unknown.reset();
    }

    /**
     * Returns the latest product version, according to PAC
     * @return the latest product version, according to PAC
     */
    private long getLatestProductVersion()
    {
        ProductService productService = factory.getProductService();
        Product latestProductVersion = productService.getLatestProductVersion(applicationProperties.getDisplayName().toLowerCase());
        return latestProductVersion.getBuildNumber();
    }

    public ProductUpdatePluginCompatibility getProductUpdatePluginCompatibility(Long updateBuildNumber)
    {
        if (!isPacReachable())
        {
            //PAC is disabled, no information is reachable
            return new ProductUpdatePluginCompatibility.Builder().build();
        }

        Long currentBuildNumber = getBuildNumber();
        Iterable<Plugin> currentlyInstalledPlugins = sort(filter(manager.getPlugins(), userInstalled));

        PluginVersionService pluginVersions = factory.getPluginVersionService();
        Iterable<PluginCompatibilityStatus> pluginCompatibilityStatuses;
        if (Iterables.isEmpty(currentlyInstalledPlugins))
        {
            // UPM-901 - PAC throws a 500 when you query it with no plugins PAC-594
            // No need to make the request
            pluginCompatibilityStatuses = ImmutableList.of();
        }
        else
        {
            pluginCompatibilityStatuses = pluginVersions.getCompatibilityStatus(applicationProperties.getDisplayName().toLowerCase(),
                                                                                currentBuildNumber,
                                                                                updateBuildNumber,
                                                                                transformValues(uniqueIndex(currentlyInstalledPlugins, toPluginKeys), toVersions),
                                                                                /* max */ null,
                                                                                /* offset */ null,
                                                                                ImmutableList.of("plugin", "productCompatibilities.productCompatibility.maxVersion", "productCompatibilities.productCompatibility.minVersion", "productCompatibilities.productCompatibility.product")
            );
        }
        return createProductUpdatePluginCompatibilityStatuses(pluginCompatibilityStatuses, currentlyInstalledPlugins);
    }

    private ProductUpdatePluginCompatibility createProductUpdatePluginCompatibilityStatuses(
            Iterable<PluginCompatibilityStatus> pluginCompatibilityStatuses, Iterable<Plugin> currentlyInstalledPlugins)
    {
        // build map from pluginCompatibilityStatuses to get access to statuses by plugin key
        Map<String, PluginCompatibilityStatus> pluginStatusMap = uniqueIndex(pluginCompatibilityStatuses, toPluginKeysFromStatus);

        ProductUpdatePluginCompatibility.Builder compatibilityBuilder = new ProductUpdatePluginCompatibility.Builder();

        // go through current plugins and determine which status list to put them in
        for (Plugin plugin : currentlyInstalledPlugins)
        {
            PluginCompatibilityStatus pluginCompatibilityStatus = pluginStatusMap.get(plugin.getKey());
            if (pluginCompatibilityStatus == null)
            {
                compatibilityBuilder.addUnknown(plugin);
            }
            else
            {
                if (pluginCompatibilityStatus.getCurrentPluginCompatibleWithSpecifiedVersionOfProduct())
                {
                    compatibilityBuilder.addCompatible(plugin);
                }
                else if (pluginCompatibilityStatus.getLatestCompatiblePluginVersion() != null)
                {
                    // determine if the latest plugin compatible with specified product version is also compatible with current product
                    if (isCompatibleWithProduct(pluginCompatibilityStatus.getLatestCompatiblePluginVersion()))
                    {
                        compatibilityBuilder.addUpdateRequired(plugin);
                    }
                    else
                    {
                        compatibilityBuilder.addUpdateRequiredAfterProductUpdate(plugin);
                    }
                }
                else
                {
                    compatibilityBuilder.addIncompatible(plugin);
                }
            }
        }

        return compatibilityBuilder.build();
    }

    private boolean isCompatibleWithProduct(PluginVersion pluginVersion)
    {
        Long productBuildNumber = getBuildNumber();
        for (ProductCompatibility productCompatibility : pluginVersion.getProductCompatibilities())
        {
            // The product compatibility must match the product key
            if (applicationProperties.getDisplayName().toLowerCase().equals(productCompatibility.getProduct().getKey()))
            {
                // The sort order is the build number
                // The version is compatible with target if the build number of min <= target && max >= target
                if (productCompatibility.getMinVersion().getSortOrder() <= productBuildNumber.intValue() && productCompatibility.getMaxVersion().getSortOrder() >= productBuildNumber.intValue())
                {
                    return true;
                }
            }
        }
        return false;
    }

    private Iterable<PluginVersion> findCompatiblePlugins(String query)
    {
        if (isEmpty(query))
        {
            query = "alias:Plugin";
        }
        PluginVersionService pluginVersions = factory.getPluginVersionService();
        // UPM-593 - Make sure we sort the plugins
        return pluginVersions.findCompatiblePluginVersions(
            applicationProperties.getDisplayName().toLowerCase(),
            getQueryBuildNumber(),
            query, // the query can't be null or else no results will be returned PAC-441
            /* showBeta */ null,
            /* max */ null,
            /* offset */ null,
            ImmutableList.of("plugin", "plugin.tinyicon")
        );
    }

    private Iterable<PluginVersion> findPopularPlugins()
    {
        String productName = applicationProperties.getDisplayName().toLowerCase();
        // fixes minor error that occurs with refapp and real-pac (UPM-501)
        if (productName.equals("refimpl") && !Sys.getPacBaseUrl().endsWith("fakepac"))
        {
            return new ArrayList<PluginVersion>();
        }
        else
        {
            PluginVersionService pluginVersionService = factory.getPluginVersionService();
            // Do not sort this as this should be handled by the server since there is a different algorithm for sorting based on popularity
            return pluginVersionService.findPopularPluginVersions(
                productName,
                getQueryBuildNumber(),
                /* max */ null,
                /* offset */ null,
                ImmutableList.of("plugin", "plugin.tinyicon")
            );
        }
    }

    private Iterable<PluginVersion> findSupportedPlugins()
    {
        PluginVersionService pluginVersions = factory.getPluginVersionService();
        // UPM-593 - Make sure we sort the plugins
        return pluginVersions.findSupportedPluginVersions(
            applicationProperties.getDisplayName().toLowerCase(),
            getQueryBuildNumber(),
            /* max */ null,
            /* offset */ null,
            ImmutableList.of("plugin", "plugin.tinyicon")
        );
    }

    private Iterable<PluginVersion> findFeaturedPlugins()
    {
        PluginVersionService pluginVersions = factory.getPluginVersionService();
        // UPM-593 - Make sure we sort the plugins
        return pluginVersions.findFeaturedPlugins(
            applicationProperties.getDisplayName().toLowerCase(),
            getQueryBuildNumber(),
            /* max */ null,
            /* offset */ null,
            ImmutableList.of("plugin", "plugin.tinyicon")
        );
    }

    private Iterable<PluginVersion> paginate(Iterable<PluginVersion> pluginVersions, Integer max, Integer offset)
    {
        if ((max == null || max == 0) && (offset == null || offset == 0))
        {
            return pluginVersions;
        }

        int size = size(pluginVersions);
        if (offset == null)
        {
            offset = 0;
        }
        if (max == null || max == 0)
        {
            max = size;
        }

        return newArrayList(pluginVersions).subList(offset, min(offset + max, size));
    }

    public PluginVersion getAvailablePlugin(String key)
    {
        return getSpecificAndLatestAvailablePluginVersions(key, null).getLatest().getOrElse((PluginVersion)null);
    }

    public PluginVersionPair getSpecificAndLatestAvailablePluginVersions(String key, String specificVersion)
    {
        if (!isPacReachable())
        {
            //PAC is disabled, no information is reachable
            return new PluginVersionPair(none(PluginVersion.class), none(PluginVersion.class));
        }
        try
        {
            PluginVersionService pluginVersions = factory.getPluginVersionService();
            List<PluginVersion> versions = pluginVersions.findAllCompatiblePluginVersionsByPluginKey(
                applicationProperties.getDisplayName().toLowerCase(),
                getQueryBuildNumber(),
                key,
                /* max */ null,
                /* offset */ null,
                ImmutableList.of("plugin.icon", "plugin.vendor", "license", "reviewSummary")
            );
            if (versions.isEmpty())
            {
                return new PluginVersionPair(none(PluginVersion.class), none(PluginVersion.class));
            }
            return findSpecificVersionAndLatest(versions, specificVersion);
        }
        // next two catches are to fix UPM-1058
        catch (PacException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new PacException(e);
        }
    }

    private PluginVersionPair findSpecificVersionAndLatest(List<PluginVersion> versions, String version)
    {
        PluginVersion latest = versions.get(0);
        PluginVersion specific = null;
        for (PluginVersion plugin : versions)
        {
            if (!isEmpty(version) && version.equals(plugin.getVersion()))
            {
                specific = plugin;
            }
            if (plugin.getBuildNumber() > latest.getBuildNumber())
            {
                latest = plugin;
            }
        }
        return new PluginVersionPair(option(specific), some(latest));
    }

    private final Predicate<PluginVersion> installed = new Predicate<PluginVersion>()
    {
        public boolean apply(PluginVersion plugin)
        {
            return manager.isPluginInstalled(plugin.getPlugin().getPluginKey());
        }
    };

    private final Predicate<Plugin> system = new Predicate<Plugin>()
    {
        public boolean apply(Plugin plugin)
        {
            return !manager.isUserInstalled(plugin) && !plugin.isBundledPlugin();
        }
    };

    private final Predicate<Plugin> userInstalled = new Predicate<Plugin>()
    {
        public boolean apply(Plugin plugin)
        {
            return manager.isUserInstalled(plugin);
        }
    };

    private final Predicate<Plugin> waitingForRestart = new Predicate<Plugin>()
    {
        public boolean apply(Plugin plugin)
        {
            return manager.requiresRestart(plugin);
        }
    };

    private static final Function<Plugin, String> toPluginKeys = new Function<Plugin, String>()
    {
        public String apply(Plugin plugin)
        {
            return plugin.getKey();
        }
    };

    private static final Function<Plugin, String> toVersions = new Function<Plugin, String>()
    {
        public String apply(Plugin plugin)
        {
            return plugin.getPluginInformation().getVersion();
        }
    };

    private static final Function<PluginCompatibilityStatus, String> toPluginKeysFromStatus = new Function<PluginCompatibilityStatus, String>()
    {
        public String apply(PluginCompatibilityStatus pluginCompatibilityStatus)
        {
            return pluginCompatibilityStatus.getPluginKey();
        }
    };

    private Iterable<PluginVersion> sortVersions(Iterable<PluginVersion> listToSort)
    {
        // Create a copy of the list to sort
        return Ordering.from(pluginVersionComparator).sortedCopy(listToSort);
    }

    private Iterable<Plugin> sort(Iterable<Plugin> listToSort)
    {
        return Ordering.from(pluginComparator).sortedCopy(listToSort);
    }

    private static int compareStringsNullSafe(final String s1, final String s2)
    {
        if (s1 == null || s2 == null)
        {
            if (s1 == null)
            {
                return (s2 == null) ? 0 : -1;
            }
            else
            {
                return 1;
            }
        }
        return s1.compareTo(s2);
    }

    private static final class PluginVersionComparator implements Comparator<PluginVersion>
    {
        public int compare(final PluginVersion o1, final PluginVersion o2)
        {
            final com.atlassian.plugins.domain.model.plugin.Plugin plugin1 = o1.getPlugin();
            final com.atlassian.plugins.domain.model.plugin.Plugin plugin2 = o2.getPlugin();

            // This should never happen, but can not hurt to be safe
            if (plugin1 == null || plugin2 == null)
            {
                if (plugin1 == null)
                {
                    return (plugin2 == null) ? 0 : -1;
                }
                else
                {
                    return 1;
                }
            }

            // Compare on the plugin name primarily
            int result = compareStringsNullSafe(plugin1.getName(), plugin2.getName());
            if (result != 0)
            {
                return result;
            }

            // Fallback to comparing on the plugin key
            return compareStringsNullSafe(plugin1.getPluginKey(), plugin2.getPluginKey());
        }
    }

    private static final class SpiPluginComparator implements Comparator<Plugin>
    {
        public int compare(final Plugin plugin1, final Plugin plugin2)
        {
            // This should never happen, but cannot hurt to be safe
            if (plugin1 == null || plugin2 == null)
            {
                if (plugin1 == null)
                {
                    return (plugin2 == null) ? 0 : -1;
                }
                else
                {
                    return 1;
                }
            }

            // Compare on the plugin name primarily
            int result = compareStringsNullSafe(plugin1.getName(), plugin2.getName());
            if (result != 0)
            {
                return result;
            }

            // Fallback to comparing on the plugin key
            return compareStringsNullSafe(plugin1.getKey(), plugin2.getKey());
        }
    }
}
