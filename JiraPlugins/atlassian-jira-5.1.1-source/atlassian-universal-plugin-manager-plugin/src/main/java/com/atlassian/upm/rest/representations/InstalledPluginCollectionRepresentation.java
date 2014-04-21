package com.atlassian.upm.rest.representations;

import java.net.URI;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.rest.UpmUriBuilder;
import com.atlassian.upm.rest.representations.InstalledPluginEntry.PluginOrdering;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;
import com.atlassian.upm.spi.Plugin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import static com.atlassian.upm.permission.Permission.GET_AUDIT_LOG;
import static com.atlassian.upm.permission.Permission.GET_PRODUCT_UPDATE_COMPATIBILITY;
import static com.atlassian.upm.permission.Permission.MANAGE_AUDIT_LOG;
import static com.atlassian.upm.permission.Permission.MANAGE_PLUGIN_INSTALL;
import static com.atlassian.upm.rest.representations.InstalledPluginEntry.toEntry;
import static com.atlassian.upm.rest.representations.RepresentationLinks.AUDIT_LOG_MAX_ENTRIES_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.AUDIT_LOG_PURGE_AFTER_MANAGE_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.AUDIT_LOG_PURGE_AFTER_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.AUDIT_LOG_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.INSTALL_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.PRODUCT_UPDATES_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.UPDATE_ALL_REL;
import static com.google.common.collect.Iterables.transform;

/**
 * Jackson representation of the plugins installed in the current configuration.
 */
public class InstalledPluginCollectionRepresentation
{
    @JsonProperty final Collection<InstalledPluginEntry> plugins;
    @JsonProperty final Map<String, URI> links;
    @JsonProperty private final String upmUpdateVersion;
    @JsonProperty private final HostStatusRepresentation hostStatus;

    @JsonCreator
    public InstalledPluginCollectionRepresentation(@JsonProperty("plugins") Collection<InstalledPluginEntry> plugins,
                                                   @JsonProperty("links") Map<String, URI> links,
                                                   @JsonProperty("hostStatus") HostStatusRepresentation hostStatus,
                                                   @JsonProperty("upmUpdateVersion") String upmUpdateVersion)
    {
        this.plugins = ImmutableList.copyOf(plugins);
        this.links = ImmutableMap.copyOf(links);
        this.hostStatus = hostStatus;
        this.upmUpdateVersion = upmUpdateVersion;
    }

    InstalledPluginCollectionRepresentation(PluginAccessorAndController pluginAccessorAndController, UpmUriBuilder uriBuilder,
                                            LinkBuilder linkBuilder, PermissionEnforcer permissionEnforcer,
                                            Locale locale, Iterable<Plugin> plugins, HostStatusRepresentation hostStatus,
                                            String upmUpdateVersion)
    {
        links = linkBuilder.buildLinksFor(uriBuilder.buildInstalledPluginCollectionUri())
            .putIfPermitted(GET_AUDIT_LOG, AUDIT_LOG_REL, uriBuilder.buildAuditLogFeedUri())
            .putIfPermitted(GET_AUDIT_LOG, AUDIT_LOG_MAX_ENTRIES_REL, uriBuilder.buildAuditLogMaxEntriesUri())
            .putIfPermitted(GET_AUDIT_LOG, AUDIT_LOG_PURGE_AFTER_REL, uriBuilder.buildAuditLogPurgeAfterUri())
            .putIfPermitted(MANAGE_AUDIT_LOG, AUDIT_LOG_PURGE_AFTER_MANAGE_REL, uriBuilder.buildAuditLogPurgeAfterUri())
            .putIfPermitted(GET_PRODUCT_UPDATE_COMPATIBILITY, PRODUCT_UPDATES_REL, uriBuilder.buildProductUpdatesUri())
            .putIfPermitted(MANAGE_PLUGIN_INSTALL, INSTALL_REL, uriBuilder.buildInstalledPluginCollectionUri())
            .putIfPermitted(MANAGE_PLUGIN_INSTALL, UPDATE_ALL_REL, uriBuilder.buildUpdateAllUri())
            .build();
        
        this.plugins = ImmutableList.copyOf(transform(new PluginOrdering(locale).sortedCopy(plugins),
                                                      toEntry(pluginAccessorAndController, uriBuilder, linkBuilder, permissionEnforcer)));
        this.hostStatus = hostStatus;
        this.upmUpdateVersion = upmUpdateVersion;
    }

    public Iterable<InstalledPluginEntry> getPlugins()
    {
        return this.plugins;
    }

    public Map<String, URI> getLinks()
    {
        return links;
    }
    
    public HostStatusRepresentation getHostStatus()
    {
        return hostStatus;
    }
}
