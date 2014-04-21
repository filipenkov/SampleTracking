package com.atlassian.upm.rest;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

import javax.ws.rs.core.UriBuilder;

import com.atlassian.plugins.domain.model.plugin.PluginVersion;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.upm.Sys;
import com.atlassian.upm.notification.NotificationType;
import com.atlassian.upm.notification.rest.resources.NotificationCollectionResource;
import com.atlassian.upm.notification.rest.resources.NotificationResource;
import com.atlassian.upm.osgi.Bundle;
import com.atlassian.upm.osgi.Package;
import com.atlassian.upm.osgi.Service;
import com.atlassian.upm.osgi.Version;
import com.atlassian.upm.osgi.rest.resources.BundleCollectionResource;
import com.atlassian.upm.osgi.rest.resources.BundleResource;
import com.atlassian.upm.osgi.rest.resources.PackageCollectionResource;
import com.atlassian.upm.osgi.rest.resources.PackageResource;
import com.atlassian.upm.osgi.rest.resources.ServiceCollectionResource;
import com.atlassian.upm.osgi.rest.resources.ServiceResource;
import com.atlassian.upm.rest.async.AsynchronousTaskResource;
import com.atlassian.upm.rest.resources.AuditLogSyndicationResource;
import com.atlassian.upm.rest.resources.AvailablePluginCollectionResource;
import com.atlassian.upm.rest.resources.AvailablePluginResource;
import com.atlassian.upm.rest.resources.ChangeRequiringRestartCollectionResource;
import com.atlassian.upm.rest.resources.ChangeRequiringRestartResource;
import com.atlassian.upm.rest.resources.FeaturedPluginCollectionResource;
import com.atlassian.upm.rest.resources.InstalledPluginCollectionResource;
import com.atlassian.upm.rest.resources.PacPluginDetailsResource;
import com.atlassian.upm.rest.resources.PacStatusResource;
import com.atlassian.upm.rest.resources.PluginMediaResource;
import com.atlassian.upm.rest.resources.PluginModuleResource;
import com.atlassian.upm.rest.resources.PluginResource;
import com.atlassian.upm.rest.resources.PopularPluginCollectionResource;
import com.atlassian.upm.rest.resources.ProductUpdatePluginCompatibilityResource;
import com.atlassian.upm.rest.resources.ProductUpdatesResource;
import com.atlassian.upm.rest.resources.ProductVersionResource;
import com.atlassian.upm.rest.resources.SafeModeResource;
import com.atlassian.upm.rest.resources.SupportedPluginCollectionResource;
import com.atlassian.upm.rest.resources.updateall.UpdateAllResource;
import com.atlassian.upm.test.rest.resources.BuildNumberResource;
import com.atlassian.upm.test.rest.resources.PacBaseUrlResource;
import com.atlassian.upm.test.rest.resources.PacModeResource;
import com.atlassian.upm.test.rest.resources.SysResource;

import static com.atlassian.upm.PluginManagerHandler.FRAGMENT_NAME;

import static com.atlassian.upm.rest.UpmUriEscaper.escape;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang.StringUtils.trim;

/**
 * Builds URIs to resources.
 */
public class UpmUriBuilder
{
    private final ApplicationProperties applicationProperties;

    public UpmUriBuilder(ApplicationProperties applicationProperties)
    {
        this.applicationProperties = checkNotNull(applicationProperties, "applicationProperties");
    }

    /**
     * @param pluginKey the key of the plugin
     * @return URI to the plugin resource
     */
    public final URI buildPluginUri(String pluginKey)
    {
        return newBaseUriBuilder().path(PluginResource.class).build(escape(pluginKey));
    }

    /**
     * @param pluginKey the key of the plugin
     * @return URI to the change requiring restart for that plugin
     */
    public final URI buildChangeRequiringRestart(String pluginKey)
    {
        return newBaseUriBuilder().path(ChangeRequiringRestartResource.class).build(escape(pluginKey));
    }

     /**
     * @param pluginKey the key of the plugin
     * @return URI to the plugin media resource icon
     */
    public final URI buildPluginIconLocationUri(String pluginKey)
    {
        return newBaseUriBuilder().path(PluginMediaResource.class).path("plugin-icon").build(escape(pluginKey));
    }

    /**
     * @param pluginKey the key of the plugin
     * @return URI to the plugin media resource logo
     */
    public final URI buildPluginLogoLocationUri(String pluginKey)
    {
        return newBaseUriBuilder().path(PluginMediaResource.class).path("plugin-logo").build(escape(pluginKey));
    }

    /**
     * @param pluginKey the key of the plugin
     * @return URI to the plugin media resource banner
     */
    public final URI buildPluginBannerLocationUri(String pluginKey)
    {
        return newBaseUriBuilder().path(PluginMediaResource.class).path("plugin-banner").build(escape(pluginKey));
    }

    /**
     * @param pluginKey the key of the plugin
     * @return URI to the plugin media resource vendor icon
     */
    public final URI buildVendorIconLocationUri(String pluginKey)
    {
        return newBaseUriBuilder().path(PluginMediaResource.class).path("vendor-icon").build(escape(pluginKey));
    }

    /**
     * @param pluginKey the key of the plugin
     * @return URI to the plugin media resource vender logo
     */
    public final URI buildVendorLogoLocationUri(String pluginKey)
    {
        return newBaseUriBuilder().path(PluginMediaResource.class).path("vendor-logo").build(escape(pluginKey));
    }

    /**
     * @return URI to the safe mode resource
     */
    public final URI buildSafeModeUri()
    {
        return newBaseUriBuilder().path(SafeModeResource.class).build();
    }

    /**
     * @param keepState A flag used to indicate if the current state of the plugins system will be kept, or if the
     * saved configuration will be restored when exiting from safe mode.
     * @return URI to exit safe mode
     */
    public final URI buildExitSafeModeUri(boolean keepState)
    {
        return newBaseUriBuilder().path(SafeModeResource.class).queryParam("keepState", keepState).build();
    }

    /**
     * @return URI to the build number resource
     */
    public final URI buildBuildNumberUri()
    {
        return newBaseUriBuilder().path(BuildNumberResource.class).build();
    }

    /**
     * @return URI to the build number resource
     */
    public final URI buildIsOnDemandUri()
    {
        return newBaseUriBuilder().path(SysResource.class).build();
    }

    /**
     * @return URI to the PAC mode resource
     */
    public final URI buildPacModeUri()
    {
        return newBaseUriBuilder().path(PacModeResource.class).build();
    }

    /**
     * @return URI to PAC status
     */
    public final URI buildPacStatusUri()
    {
        return newBaseUriBuilder().path(PacStatusResource.class).build();
    }

    /**
     * @return URI to the PAC base URL resource
     */
    public final URI buildPacBaseUrlUri()
    {
        return newBaseUriBuilder().path(PacBaseUrlResource.class).build();
    }

    /**
     * @return URI to the audit log feed
     */
    public final URI buildAuditLogFeedUri()
    {
        return newBaseUriBuilder().path(AuditLogSyndicationResource.class).build();
    }

    public final URI buildAuditLogFeedUri(int maxResults, int startIndex)
    {
        return newBaseUriBuilder().path(AuditLogSyndicationResource.class)
            .queryParam("max-results", maxResults)
            .queryParam("start-index", startIndex)
            .build();
    }

    public final URI buildAuditLogMaxEntriesUri()
    {
        return newBaseUriBuilder().path(AuditLogSyndicationResource.class).path("max-entries").build();
    }

    public final URI buildAuditLogPurgeAfterUri()
    {
        return newBaseUriBuilder().path(AuditLogSyndicationResource.class).path("purge-after").build();
    }

    /**
     * @param pluginKey the key of the plugin that contains the plugin module
     * @param key the key of the plugin module
     * @return URI to the plugin module
     */
    public final URI buildPluginModuleUri(String pluginKey, String key)
    {
        return newBaseUriBuilder().path(PluginModuleResource.class).build(escape(pluginKey), escape(key));
    }

    /**
     * @return URI to all installed plugins
     */
    public final URI buildInstalledPluginCollectionUri()
    {
        return newBaseUriBuilder().path(InstalledPluginCollectionResource.class).build();
    }

    /**
     * @return URI to the resource which provides plugin details
     */
    public final URI buildPacPluginDetailsResourceUri(String pluginKey, String pluginVersion)
    {
        return newBaseUriBuilder().path(PacPluginDetailsResource.class).build(pluginKey, pluginVersion);
    }

    public final URI buildOsgiBundleCollectionUri()
    {
        return buildOsgiBundleCollectionUri(null);
    }

    public final URI buildOsgiBundleCollectionUri(String term)
    {
        UriBuilder builder = newBaseUriBuilder().path(BundleCollectionResource.class);
        return term == null ?
            builder.build() :
            builder.queryParam("q", checkNotNull(term, "term")).build();
    }

    public final URI buildOsgiBundleUri(long id)
    {
        return newBaseUriBuilder().path(BundleResource.class).build(id);
    }

    public final URI buildOsgiBundleUri(Bundle bundle)
    {
        return buildOsgiBundleUri(checkNotNull(bundle, "bundle").getId());
    }

    public final URI buildOsgiServiceCollectionUri()
    {
        return newBaseUriBuilder().path(ServiceCollectionResource.class).build();
    }

    public final URI buildOsgiServiceUri(long id)
    {
        return newBaseUriBuilder().path(ServiceResource.class).build(id);
    }

    public final URI buildOsgiServiceUri(Service service)
    {
        return buildOsgiServiceUri(checkNotNull(service, "service").getId());
    }

    public final URI buildOsgiPackageCollectionUri()
    {
        return newBaseUriBuilder().path(PackageCollectionResource.class).build();
    }

    public final URI buildOsgiPackageUri(long bundleId, String name, Version version)
    {
        return newBaseUriBuilder().path(PackageResource.class)
            .build(bundleId, checkNotNull(name, "name"), checkNotNull(version, "version"));
    }

    public final URI buildOsgiPackageUri(Package pkg)
    {
        checkNotNull(pkg, "pkg");
        return buildOsgiPackageUri(pkg.getExportingBundle().getId(), pkg.getName(), pkg.getVersion());
    }

    /**
     * @return URI to the collection of available plugins to install
     */
    public final URI buildAvailablePluginCollectionUri()
    {
        return buildAvailablePluginCollectionUri(null, null, null);
    }

    /**
     * @param query specifies the query to be executed
     * @param max The maximum number of results to return, null if all results should be returned
     * @param offset The offset of the results to return, null to start at the beginning of the list
     * @return URI to the collection of available plugins to install
     */
    public final URI buildAvailablePluginCollectionUri(String query, Integer max, Integer offset)
    {
        return addSearchQuery(addOffset(addMaxResults(newBaseUriBuilder().path(AvailablePluginCollectionResource.class), max), offset), query).build();
    }

    /**
     * @return URI to the collection of featured plugins available to install
     */
    public final URI buildFeaturedPluginCollectionUri()
    {
        return buildFeaturedPluginCollectionUri(null, null);
    }

    /**
     * @param max The maximum number of results to return, null if all results should be returned
     * @param offset The offset of the results to return, null to start at the beginning of the list
     * @return URI to the collection of featured plugins available to install
     */
    public final URI buildFeaturedPluginCollectionUri(Integer max, Integer offset)
    {
        return addOffset(addMaxResults(newBaseUriBuilder().path(FeaturedPluginCollectionResource.class), max), offset).build();
    }

    /**
     * @return URI to the collection of popular plugins available to install
     */
    public final URI buildPopularPluginCollectionUri()
    {
        return buildPopularPluginCollectionUri(null, null);
    }

    /**
     * @param max The maximum number of results to return, null if all results should be returned
     * @param offset The offset of the results to return, null to start at the beginning of the list
     * @return URI to the collection of popular plugins available to install
     */
    public final URI buildPopularPluginCollectionUri(Integer max, Integer offset)
    {
        return addOffset(addMaxResults(newBaseUriBuilder().path(PopularPluginCollectionResource.class), max), offset).build();
    }

    /**
     * @return URI to the collection of supported plugins available to install
     */
    public final URI buildSupportedPluginCollectionUri()
    {
        return buildSupportedPluginCollectionUri(null, null);
    }

    /**
     * @param max The maximum number of results to return, null if all results should be returned
     * @param offset The offset of the results to return, null to start at the beginning of the list
     * @return URI to the collection of supported plugins available to install
     */
    public final URI buildSupportedPluginCollectionUri(Integer max, Integer offset)
    {
        return addOffset(addMaxResults(newBaseUriBuilder().path(SupportedPluginCollectionResource.class), max), offset).build();
    }

    /**
     * @param pluginKey key of the plugin available for install
     * @return URI of the plugin available for install
     */
    public final URI buildAvailablePluginUri(String pluginKey)
    {
        return newBaseUriBuilder().path(AvailablePluginResource.class).build(escape(pluginKey));
    }

    public final URI buildPacPluginDetailsUri(PluginVersion plugin)
    {
        checkNotNull(plugin, "plugin");
        StringBuilder pluginDetailsUri = new StringBuilder(System.getProperty("pac.website", "https://plugins.atlassian.com"));
        pluginDetailsUri.append("/plugin/details/");
        pluginDetailsUri.append(plugin.getPlugin().getId());
        pluginDetailsUri.append("?versionId=");
        pluginDetailsUri.append(plugin.getId());
        return URI.create(trim(pluginDetailsUri.toString()));
    }

    public final URI buildNotificationCollectionUri()
    {
        return newBaseUriBuilder().path(NotificationCollectionResource.class).build();
    }

    public final URI buildNotificationCollectionUri(String username)
    {
        return newBaseUriBuilder().path(NotificationCollectionResource.class).path(username).build();
    }

    public final URI buildNotificationCollectionUri(String username, NotificationType type)
    {
        return newBaseUriBuilder().path(NotificationCollectionResource.class).path(username).path(type.getKey()).build();
    }

    public final URI buildNotificationUri(String username, NotificationType type, String pluginKey)
    {
        return newBaseUriBuilder().path(NotificationResource.class).build(username, type.getKey(), pluginKey);
    }

    public final URI buildPendingTasksUri()
    {
        return newBaseUriBuilder().path(AsynchronousTaskResource.class).build();
    }

    public final URI buildPendingTaskUri(String taskId)
    {
        return newBaseUriBuilder().path(AsynchronousTaskResource.class).path(taskId).build();
    }

    public final URI buildAbsolutePendingTaskUri(String taskId)
    {
        return makeAbsolute(newBaseUriBuilder().path(AsynchronousTaskResource.class).path(taskId).build());
    }

    /**
     * Creates and returns the absolute URI to the user profile page corresponding to the passed in {@code UserProfile}.
     *
     * @param userProfile the user profile to create the profile link for
     * @return the absolute URI to the userProfile user's profile page
     */
    public final URI buildAbsoluteProfileUri(UserProfile userProfile)
    {
        if (userProfile == null)
        {
            return null;
        }
        URI salProfileUri = userProfile.getProfilePageUri();
        if (salProfileUri.isAbsolute())
        {
            return salProfileUri;
        }
        return makeAbsolute(newApplicationBaseUriBuilder()
            .path(salProfileUri.getPath())
            .replaceQuery(salProfileUri.getQuery())
            .build());
    }

    /**
     * Returns the absolute URI to the main UPM page.
     */
    public final URI buildUpmUri()
    {
        return makeAbsolute(newApplicationBaseUriBuilder().path("/plugins/servlet/upm").build());
    }

    /**
     * Returns the absolute URI to the main UPM page with a specific tab preselected.
     * @param tabName  name of the tab to open
     */
    public final URI buildUpmTabUri(String tabName)
    {
        try
        {
            return URI.create(buildUpmUri().toASCIIString() + "?" + FRAGMENT_NAME + "=" + URLEncoder.encode(tabName, "UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            //UTF-8 shouldn't ever be unsupported....
            return URI.create(buildUpmUri().toASCIIString() + "?" + FRAGMENT_NAME + "=" + tabName);
        }
    }
    
    /**
     * Returns the absolute URI to the main UPM page with a specific tab preselected and the
     * details for a specific plugin pre-expanded.
     * @param tabName  name of the tab to open
     * @param pluginKey  name of the plugin to expand
     */
    public final URI buildUpmTabPluginUri(String tabName, String pluginKey)
    {
        return buildUpmTabUri(tabName + "/" + pluginKey);
    }

    /**
     * Returns the absolute URI to the main UPM page with a specific tab preselected, the
     * details for a specific plugin pre-expanded, and a message displayed in the detail area.
     * @param tabName  name of the tab to open
     * @param pluginKey  name of the plugin to expand
     * @param messageCode  key of the message to display; this must be the name by which the message is
     *   known in the front end (a property name within AJS.params), which may not be the same as the i18n key.
     */
    public final URI buildUpmTabPluginUri(String tabName, String pluginKey, String messageCode)
    {
        return buildUpmTabUri(tabName + "/" + pluginKey + "/" + messageCode);
    }

    /**
     * Returns the absolute URI to the servlet for receiving a new license from MAC for a specific plugin.
     */
    public final URI buildLicenseReceiptUri(String pluginKey)
    {
        return makeAbsolute(newApplicationBaseUriBuilder().path("plugins/servlet/upm/license/" + pluginKey).build());
    }
    
    /**
     * Returns an absolute {@code URI}. 
     * If the parameter is a relative URI, prepends the base url.
     * If the parameter is already absolute, does nothing.
     * 
     * Note that this will NOT add the context path (i.e. "upm" in our case), given a relative URI.
     * 
     * @param uri the uri to make absolute
     * @return the absolute uri
     */
    public final URI makeAbsolute(URI uri)
    {
        if (uri.isAbsolute())
        {
            return uri;
        }
        return URI.create(applicationProperties.getBaseUrl()).resolve(uri).normalize();
    }

    /**
     * @return URI to the product version resource
     */
    public final URI buildProductVersionUri()
    {
        return newBaseUriBuilder().path(ProductVersionResource.class).build();
    }

    /**
     * @return URI to the update all controller resource
     */
    public final URI buildUpdateAllUri()
    {
        return newBaseUriBuilder().path(UpdateAllResource.class).build();
    }

    protected UriBuilder newBaseUriBuilder()
    {
        return newApplicationBaseUriBuilder().path("/rest/plugins/1.0");
    }

    private UriBuilder newApplicationBaseUriBuilder()
    {
        URI base = URI.create(applicationProperties.getBaseUrl()).normalize();
        return UriBuilder.fromPath(base.getPath());
    }

    public URI buildProductUpdatesUri()
    {
        return newBaseUriBuilder().path(ProductUpdatesResource.class).build();
    }

    public URI buildMacPluginLicenseUri(String pluginKey, String type)
    {
        return URI.create(Sys.getMacBaseUrl() + "/addon/" + type + "/" + pluginKey);
    }

    public URI buildProductUpdatePluginCompatibilityUri(Long productUpdateBuildNumber)
    {
        return newBaseUriBuilder().path(ProductUpdatePluginCompatibilityResource.class).build(productUpdateBuildNumber);
    }

    public URI buildChangesRequiringRestartUri()
    {
        return newBaseUriBuilder().path(ChangeRequiringRestartCollectionResource.class).build();
    }

    private UriBuilder addMaxResults(UriBuilder uriBuilder, Integer max)
    {
        return addQueryParamIfNotNull(uriBuilder, "max-results", max);
    }

    private UriBuilder addOffset(UriBuilder uriBuilder, Integer offset)
    {
        return addQueryParamIfNotNull(uriBuilder, "start-index", offset);
    }

    private UriBuilder addSearchQuery(UriBuilder uriBuilder, String query)
    {
        return addQueryParamIfNotNull(uriBuilder, "q", query);
    }

    private UriBuilder addQueryParamIfNotNull(UriBuilder uriBuilder, String name, Object value)
    {
        if (value != null)
        {
            return uriBuilder.queryParam(name, value);
        }
        else
        {
            return uriBuilder;
        }
    }
}
