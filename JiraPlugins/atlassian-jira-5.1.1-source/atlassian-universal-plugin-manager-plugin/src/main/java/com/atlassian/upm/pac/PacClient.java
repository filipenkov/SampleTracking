package com.atlassian.upm.pac;

import com.atlassian.plugins.domain.model.plugin.PluginVersion;
import com.atlassian.plugins.domain.model.product.Product;
import com.atlassian.upm.ProductUpdatePluginCompatibility;
import com.atlassian.upm.api.util.Option;

/**
 * Provides ability to check http://plugins.atlassian.com for available plugins to install and update for the
 * application and version that the plugin manager is currently in.
 */
public interface PacClient
{
    /**
     * Fetches the plugins that are available to install and filters out any that are already installed.
     *
     * @param query specifies the query to be executed
     * @param max The maximum number of results to return, null if all results should be returned
     * @param offset The offset of the results to return, null to start at the beginning of the list
     * @return plugins that are available to install
     */
    Iterable<PluginVersion> getAvailable(String query, Integer max, Integer offset);

    /**
     * Fetches the plugin details for the specified plugin key or {@code null} if no plugin with that key exists.
     *
     * @param key key of the plugin to fetch the details of
     * @return plugin details for the specified plugin key or {@code null} if no plugin with that key exists
     */
    PluginVersion getAvailablePlugin(String key);

    /**
     * Get both the specified version and the latest version of the plugin with the given key, all in a single call to PAC.
     * @param key the plugin key
     * @param version the specified version
     * @return the specified and latest versions of the plugin
     */
    PluginVersionPair getSpecificAndLatestAvailablePluginVersions(String key, String version);

    /**
     * Fetches the popular plugins that are available to install and filters out any that are already installed.
     *
     * @param max The maximum number of results to return, null if all results should be returned
     * @param offset The offset of the results to return, null to start at the beginning of the list
     * @return plugins that are popular and available to install
     */
    Iterable<PluginVersion> getPopular(Integer max, Integer offset);

    /**
     * Fetches the supported plugins that are available to install and filters out any that are already installed.
     *
     * @param max The maximum number of results to return, null if all results should be returned
     * @param offset The offset of the results to return, null to start at the beginning of the list
     * @return plugins that are supported and available to install
     */
    Iterable<PluginVersion> getSupported(Integer max, Integer offset);

    /**
     * Fetches the featured plugins that are available to install and filters out any that are already installed.
     *
     * @param max The maximum number of results to return, null if all results should be returned
     * @param offset The offset of the results to return, null to start at the beginning of the list
     * @return plugins that are available to install
     */
    Iterable<PluginVersion> getFeatured(Integer max, Integer offset);

    /**
     * @return the product versions released after the current version
     */
    Iterable<Product> getProductUpdates();

    /**
     * Looks for updates for installed plugins that are compatible with the installed application.
     * This includes plugin versions which are incompatible with currently-installed plugin licenses
     * (e.g. a license's maintenance expiry date is prior to the plugin version's build date).
     *
     * @return plugins that can be updated
     */
    Iterable<PluginVersion> getUpdates();

    /**
     * Determines and returns the list of compatibility statuses of the currently installed plugins against the
     * specified product update version. Any installed plugins that are not found on PAC go in the unknown status.
     *
     * @param updateBuildNumber specifies the product version to check compatibility of plugins against
     * @return the compatibility statuses of the currently installed plugins
     */
    ProductUpdatePluginCompatibility getProductUpdatePluginCompatibility(Long updateBuildNumber);

    /**
     * Returns true if PAC does not know about the currently running Product version, false otherwise. Note that PAC
     * "knows" product versions not on its lists if they are in between known versions. The result is cached; the first
     * call may access PAC, but subsequent calls will not, since the value cannot change while the product is running.
     *
     * @return true if unknown, false if known, or none if either PAC was unreachable, or the product version couldn't be computed
     * @since 1.3.2
     */
    Option<Boolean> isUnknownProductVersion();

    /**
     * Returns true if the currently running Product version is a development version, false otherwise. The result is cached;
     * the first call may access PAC, but subsequent calls will not, since the value cannot change while the product is running.
     * @return true if a development version, false if it is not a development version, or none if the product version couldn't be computed
     * @since 1.6
     */
    Option<Boolean> isDevelopmentProductVersion();

    /**
     * Returns true if PAC is disabled. UPM can be set offline by setting the "upm.pac.disable" System Property to true
     * which is overridden by the PluginSettings property
     *
     * @return true if PAC is disabled; false if methods actually send a request to PAC each time they are called.
     */
    boolean isPacDisabled();

    /**
     * Returns true if the PAC server is reachable and not disabled; false otherwise.  This value is cached.
     * @return true if the PAC server is reachable and not disabled; false otherwise
     * @since 1.6
     */
    boolean isPacReachable();

    /**
     * Discards any previously cached PAC state so that the next call to {@link #isPacReachable()} will
     * check the current state. 
     */
    void forgetPacReachableState();
}
