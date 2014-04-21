package com.atlassian.upm.rest.representations;

import java.net.URI;
import java.util.Map;

import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.Sys;
import com.atlassian.upm.permission.Permission;
import com.atlassian.upm.rest.UpmUriBuilder;
import com.atlassian.upm.rest.async.AsynchronousTaskManager;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;
import com.atlassian.upm.spi.Plugin;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap.Builder;

import static com.atlassian.upm.permission.Permission.GET_AUDIT_LOG;
import static com.atlassian.upm.permission.Permission.GET_AVAILABLE_PLUGINS;
import static com.atlassian.upm.permission.Permission.GET_NOTIFICATIONS;
import static com.atlassian.upm.permission.Permission.GET_OSGI_STATE;
import static com.atlassian.upm.permission.Permission.GET_PRODUCT_UPDATE_COMPATIBILITY;
import static com.atlassian.upm.permission.Permission.GET_SAFE_MODE;
import static com.atlassian.upm.permission.Permission.MANAGE_AUDIT_LOG;
import static com.atlassian.upm.permission.Permission.MANAGE_PLUGIN_INSTALL;
import static com.atlassian.upm.permission.Permission.MANAGE_SAFE_MODE;
import static com.atlassian.upm.rest.representations.RepresentationLinks.AVAILABLE_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.CHANGES_REQUIRING_RESTART_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.ENTER_SAFE_MODE_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.EXIT_SAFE_MODE_KEEP_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.EXIT_SAFE_MODE_RESTORE_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.FEATURED_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.INSTALLED_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.NOTIFICATIONS_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.OSGI_BUNDLES_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.OSGI_PACKAGES_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.OSGI_SERVICES_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.PENDING_TASKS_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.POPULAR_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.SELF_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.SUPPORTED_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.UPDATES_REL;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Maps.filterValues;

/**
 * Build links for the representations. Provides the ability to build links based on the user's permitted activity.
 */
public class LinkBuilder
{
    private final UpmUriBuilder uriBuilder;
    private final PluginAccessorAndController pluginAccessorAndController;
    private final AsynchronousTaskManager asynchronousTaskManager;
    private final PermissionEnforcer permissionEnforcer;

    public LinkBuilder(UpmUriBuilder uriBuilder, PluginAccessorAndController pluginAccessorAndController,
        AsynchronousTaskManager asynchronousTaskManager,
        PermissionEnforcer permissionEnforcer)
    {
        this.uriBuilder = checkNotNull(uriBuilder, "uriBuilder");
        this.pluginAccessorAndController = checkNotNull(pluginAccessorAndController, "pluginAccessorAndController");
        this.asynchronousTaskManager = checkNotNull(asynchronousTaskManager, "asynchronousTaskManager");
        this.permissionEnforcer = checkNotNull(permissionEnforcer, "permissionEnforcer");
    }

    /**
     * Builds a map of default links based on the current user's permissions.
     *
     * @return a map of default links based on the current user's permissions.
     */
    private Map<String, URI> generateDefaultLinksMap()
    {
        return builder()
            .put(INSTALLED_REL, uriBuilder.buildInstalledPluginCollectionUri())
            .putIfPermitted(GET_AVAILABLE_PLUGINS, AVAILABLE_REL, uriBuilder.buildAvailablePluginCollectionUri())
            .putIfPermitted(GET_AVAILABLE_PLUGINS, FEATURED_REL, uriBuilder.buildFeaturedPluginCollectionUri())
            .putIfPermitted(GET_AVAILABLE_PLUGINS, POPULAR_REL, uriBuilder.buildPopularPluginCollectionUri())
            .putIfPermitted(GET_AVAILABLE_PLUGINS, SUPPORTED_REL, uriBuilder.buildSupportedPluginCollectionUri())
            .putIfPermitted(GET_AVAILABLE_PLUGINS, UPDATES_REL, uriBuilder.buildInstalledPluginCollectionUri())
            .putIfPermitted(GET_NOTIFICATIONS, NOTIFICATIONS_REL, uriBuilder.buildNotificationCollectionUri())
            .build();
    }

    /**
     * Build a map of {@code String} and {@code URI} corresponding to the links for a representation {@code Class}.
     *
     * @param selfLink the {@code URI} of the calling representation
     * @return a builder for the links map built for the representation {@code Class}.
     */
    public LinksMapBuilder buildLinksFor(URI selfLink)
    {
        return buildLinksFor(selfLink, true);
    }

    /**
     * Build a map of {@code String} and {@code URI} starting with only the self link provided.
     *
     * @param selfLink the {@code URI} of the calling representation
     * @return a builder containing the self link provided.
     */
    public LinksMapBuilder buildLinkForSelf(URI selfLink)
    {
        return builder().put(SELF_REL, selfLink);
    }

    public LinksMapBuilder builder()
    {
        return new LinksMapBuilder(permissionEnforcer);
    }

    /**
     * Build a map of {@code String} and {@code URI} corresponding to the links for a representation {@code Class}.
     *
     * @param selfLink the {@code URI} of the calling representation
     * @param addConditionalLinks {@code true} if we'll add:
     * 'changes-requiring-restart' link if there are changes requiring restart
     * and 'pending-tasks' link if there are pending tasks respectively.
     * 'safe-mode' link will also be added if we are not in Safe Mode,
     * and 'exit-safe-mode-keep' and 'exit-safe-mode-restore' will be added if we are in Safe Mode.
     * 'osgi' link will also be added.
     * {@code false} otherwise.
     * @return the links map built for the representation {@code Class}.
     */
    public LinksMapBuilder buildLinksFor(URI selfLink, boolean addConditionalLinks)
    {
        // create a map from allLinks without the link of the class
        LinksMapBuilder builder = buildLinkForSelf(selfLink)
            .putAll(filterValues(generateDefaultLinksMap(), not(equalTo(selfLink))));

        if (addConditionalLinks)
        {
            addSafeModeLinks(builder);
            addPendingTaskLinkIfAble(builder);
            addChangesRequiringRestartLinkIfAble(builder);
            addOsgiLinks(builder);
        }
        return builder;
    }

    private LinksMapBuilder addChangesRequiringRestartLinkIfAble(LinksMapBuilder builder)
    {
        if (pluginAccessorAndController.hasChangesRequiringRestart())
        {
            builder.put(CHANGES_REQUIRING_RESTART_REL, uriBuilder.buildChangesRequiringRestartUri());
        }
        return builder;
    }

    private LinksMapBuilder addPendingTaskLinkIfAble(LinksMapBuilder builder)
    {
        if (asynchronousTaskManager.hasPendingTasks())
        {
            builder.put(PENDING_TASKS_REL, uriBuilder.buildPendingTasksUri());
        }
        return builder;
    }

    private LinksMapBuilder addSafeModeLinks(LinksMapBuilder builder)
    {
        if (pluginAccessorAndController.isSafeMode())
        {
            builder.putIfPermitted(MANAGE_SAFE_MODE, EXIT_SAFE_MODE_RESTORE_REL, uriBuilder.buildExitSafeModeUri(false))
                .putIfPermitted(MANAGE_SAFE_MODE, EXIT_SAFE_MODE_KEEP_REL, uriBuilder.buildExitSafeModeUri(true));
        }
        else
        {
            builder.putIfPermitted(MANAGE_SAFE_MODE, ENTER_SAFE_MODE_REL, uriBuilder.buildSafeModeUri());
        }
        return builder;
    }

    private LinksMapBuilder addOsgiLinks(LinksMapBuilder builder)
    {
        builder.putIfPermitted(GET_OSGI_STATE, OSGI_BUNDLES_REL, uriBuilder.buildOsgiBundleCollectionUri())
            .putIfPermitted(GET_OSGI_STATE, OSGI_SERVICES_REL, uriBuilder.buildOsgiServiceCollectionUri())
            .putIfPermitted(GET_OSGI_STATE, OSGI_PACKAGES_REL, uriBuilder.buildOsgiPackageCollectionUri());
        return builder;
    }

    /**
     * Build URIs for velocity template
     */
    public Map<String, URI> buildPermissionedUris()
    {
        LinksMapBuilder linksMapBuilder = new LinksMapBuilder(permissionEnforcer);
        linksMapBuilder.put("upmUriRoot", uriBuilder.buildInstalledPluginCollectionUri()) //is always allowed
            .putIfPermitted(MANAGE_PLUGIN_INSTALL, "upmUriInstall", uriBuilder.buildInstalledPluginCollectionUri())
            .putIfPermitted(GET_AVAILABLE_PLUGINS, "upmUriUpdates", uriBuilder.buildInstalledPluginCollectionUri())
            .putIfPermitted(GET_AVAILABLE_PLUGINS, "upmUriFeatured", uriBuilder.buildFeaturedPluginCollectionUri())
            .putIfPermitted(GET_AVAILABLE_PLUGINS, "upmUriPopular", uriBuilder.buildPopularPluginCollectionUri())
            .putIfPermitted(GET_AVAILABLE_PLUGINS, "upmUriSupported", uriBuilder.buildSupportedPluginCollectionUri())
            .putIfPermitted(GET_AVAILABLE_PLUGINS, "upmUriAvailable", uriBuilder.buildAvailablePluginCollectionUri())
            .putIfPermitted(GET_AUDIT_LOG, "upmUriAuditLog", uriBuilder.buildAuditLogFeedUri())
            .putIfPermitted(GET_PRODUCT_UPDATE_COMPATIBILITY, "upmUriProductUpdates", uriBuilder.buildProductUpdatesUri())
            .putIfPermitted(GET_SAFE_MODE, "upmUriSafeMode", uriBuilder.buildSafeModeUri())
            .putIfPermitted(GET_AUDIT_LOG, "upmUriPurgeAfter", uriBuilder.buildAuditLogPurgeAfterUri())
            .putIfPermitted(MANAGE_AUDIT_LOG, "upmUriManagePurgeAfter", uriBuilder.buildAuditLogPurgeAfterUri())
            .putIfPermitted(GET_OSGI_STATE, "upmUriOsgiBundles", uriBuilder.buildOsgiBundleCollectionUri())
            .putIfPermitted(GET_OSGI_STATE, "upmUriOsgiServices", uriBuilder.buildOsgiServiceCollectionUri())
            .putIfPermitted(GET_OSGI_STATE, "upmUriOsgiPackages", uriBuilder.buildOsgiPackageCollectionUri())
            .putIfPermitted(GET_AVAILABLE_PLUGINS, "upmUriProductVersion", uriBuilder.buildProductVersionUri())
            .put("upmUriPendingTasks", uriBuilder.buildPendingTasksUri());

        if (!Sys.isPacDisabled())
        {
            linksMapBuilder.put("upmUriPacStatus", uriBuilder.buildPacStatusUri());
        }

        return linksMapBuilder.build();
    }

    /**
     * Produces an {@code ImmutableMap} based on standard additions and
     * permission-enforced additions.
     */
    public static class LinksMapBuilder extends Builder<String, URI>
    {
        private PermissionEnforcer permissionEnforcer;

        public LinksMapBuilder(PermissionEnforcer permissionEnforcer)
        {
            this.permissionEnforcer = permissionEnforcer;
        }

        /**
         * If the {@code permission} is allowed, execute a standard {@code put}. If not allowed, do nothing.
         *
         * @param permission to check
         * @param rel the uri key
         * @param uri link to include if permission check passes
         * @return this builder
         */
        public LinksMapBuilder putIfPermitted(Permission permission, String rel, URI uri)
        {
            if (permissionEnforcer.hasPermission(permission))
            {
                put(rel, uri);
            }
            return this;
        }

        /**
         * If the {@code permission} is allowed, execute a standard {@code put}. If not allowed, do nothing.
         *
         * @param permission to check
         * @param plugin the plugin in context for this permission check, can be null
         * @param rel the uri key
         * @param uri link to include if permission check passes
         * @return this builder
         */
        public LinksMapBuilder putIfPermitted(Permission permission, Plugin plugin, String rel, URI uri)
        {
            if (permissionEnforcer.hasPermission(permission, plugin))
            {
                put(rel, uri);
            }
            return this;
        }

        /**
         * If the {@code permission} is allowed and {@code Predicate} is met, execute a standard {@code put}. If not allowed, do nothing.
         * The {@code Predicate} is executed first.
         *
         * @param permission to check
         * @param plugin the plugin in context for this permission check, can be null
         * @param condition the {@link Predicate} to check
         * @param rel the uri key
         * @param uri link to include if permission and condition checks pass
         * @return this builder
         */
        public LinksMapBuilder putIfPermittedAndConditioned(Permission permission, Plugin plugin, Predicate<Plugin> condition, String rel, URI uri)
        {
            if (condition.apply(plugin) && permissionEnforcer.hasPermission(permission, plugin))
            {
                put(rel, uri);
            }
            return this;
        }

        /**
         * If the {@code permission} is allowed, execute a standard {@code put}. If not allowed, do nothing.
         *
         * @param permission to check
         * @param module the module in context for this permission check, can be null
         * @param rel the uri key
         * @param uri link to include if permission check passes
         * @return this builder
         */
        public LinksMapBuilder putIfPermitted(Permission permission, Plugin.Module module, String rel, URI uri)
        {
            if (permissionEnforcer.hasPermission(permission, module))
            {
                put(rel, uri);
            }
            return this;
        }

        @Override
        public LinksMapBuilder putAll(Map<? extends String, ? extends URI> map)
        {
            super.putAll(map);
            return this;
        }

        @Override
        public LinksMapBuilder put(String key, URI value)
        {
            super.put(key, value);
            return this;
        }
    }

}
