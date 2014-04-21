package com.atlassian.upm.rest.representations;

import java.net.URI;
import java.text.Collator;
import java.util.Locale;
import java.util.Map;

import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.Sys;
import com.atlassian.upm.api.license.entity.LicenseError;
import com.atlassian.upm.rest.UpmUriBuilder;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;
import com.atlassian.upm.spi.Plugin;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Ordering;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import static com.atlassian.upm.permission.Permission.GET_AVAILABLE_PLUGINS;
import static com.atlassian.upm.permission.Permission.MANAGE_PLUGIN_ENABLEMENT;
import static com.atlassian.upm.permission.Permission.MANAGE_PLUGIN_LICENSE;
import static com.atlassian.upm.permission.Permission.MANAGE_PLUGIN_UNINSTALL;
import static com.atlassian.upm.rest.representations.PluginRepresentation.isUninstallable;
import static com.atlassian.upm.rest.representations.RepresentationLinks.DELETE_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.MODIFY_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.PLUGIN_DETAILS_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.PLUGIN_ICON_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.PLUGIN_LOGO_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.SELF_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.UPDATE_DETAILS_REL;

/**
 * An installed plugin entry.
 */
public class InstalledPluginEntry
{
    @JsonProperty private final boolean enabled;
    @JsonProperty private final Map<String, URI> links;
    @JsonProperty private final String name;
    @JsonProperty private final String key;
    @JsonProperty private final boolean userInstalled;
    @JsonProperty("static") private final boolean staticPlugin;
    @JsonProperty private final String restartState;
    @JsonProperty private final String description;
    @JsonProperty private final boolean usesLicensing;
    @JsonProperty private final LicenseError licenseError;

    @JsonCreator
    public InstalledPluginEntry(@JsonProperty("enabled") boolean enabled,
                       @JsonProperty("links") Map<String, URI> links,
                       @JsonProperty("name") String name,
                       @JsonProperty("userInstalled") boolean userInstalled,
                       @JsonProperty("static") boolean staticPlugin,
                       @JsonProperty("restartState") String restartState,
                       @JsonProperty("description") String description,
                       @JsonProperty("key") String key,
                       @JsonProperty("usesLicensing") boolean usesLicensing,
                       @JsonProperty("licenseError") LicenseError licenseError)
    {
        this.enabled = enabled;
        this.links = ImmutableMap.copyOf(links);
        this.name = name;
        this.userInstalled = userInstalled;
        this.staticPlugin = staticPlugin;
        this.restartState = restartState;
        this.description = description;
        this.key = key;
        this.usesLicensing = usesLicensing;
        this.licenseError = licenseError;
    }

    InstalledPluginEntry(Plugin plugin,
                PluginAccessorAndController pluginAccessorAndController,
                UpmUriBuilder uriBuilder,
                LinkBuilder linkBuilder,
                PermissionEnforcer permissionEnforcer)
    {
        this.enabled = pluginAccessorAndController.isPluginEnabled(plugin.getKey());
        this.links = linkBuilder.buildLinkForSelf(uriBuilder.buildPluginUri(plugin.getKey()))
            .putIfPermitted(MANAGE_PLUGIN_ENABLEMENT, plugin, MODIFY_REL, uriBuilder.buildPluginUri(plugin.getKey()))
            .putIfPermittedAndConditioned(MANAGE_PLUGIN_UNINSTALL, plugin, isUninstallable(pluginAccessorAndController), DELETE_REL, uriBuilder.buildPluginUri(plugin.getKey()))
            .putIfPermittedAndConditioned(GET_AVAILABLE_PLUGINS, plugin, isUpdateAvailable, UPDATE_DETAILS_REL, uriBuilder.buildAvailablePluginUri(plugin.getKey()))
            .putIfPermitted(GET_AVAILABLE_PLUGINS, plugin, PLUGIN_ICON_REL, uriBuilder.buildPluginIconLocationUri(plugin.getKey()))
            .putIfPermitted(GET_AVAILABLE_PLUGINS, plugin, PLUGIN_LOGO_REL, uriBuilder.buildPluginLogoLocationUri(plugin.getKey()))
            .putIfPermitted(GET_AVAILABLE_PLUGINS, plugin, PLUGIN_DETAILS_REL, uriBuilder.buildUpmTabPluginUri("manage", plugin.getKey()))
            .build();
        this.userInstalled = pluginAccessorAndController.isUserInstalled(plugin);
        this.staticPlugin = plugin.isStaticPlugin();
        this.restartState = RestartState.toString(pluginAccessorAndController.getRestartState(plugin));
        this.description = plugin.getPluginInformation().getDescription();
        this.name = plugin.getName();
        this.key = plugin.getKey();
        this.usesLicensing = pluginAccessorAndController.usesLicensing(plugin) && permissionEnforcer.hasPermission(MANAGE_PLUGIN_LICENSE, plugin);
        if (usesLicensing)
        {
            this.licenseError = plugin.getLicense().isDefined() ? plugin.getLicense().get().getError().getOrElse((LicenseError) null) : null;
        }
        else
        {
            this.licenseError = null;
        }
    }

    public URI getSelfLink()
    {
        return links.get(SELF_REL);
    }

    public boolean isUpdateAvailable()
    {
        return links.get(UPDATE_DETAILS_REL) != null;
    }

    public boolean isUserInstalled()
    {
        return this.userInstalled;
    }

    public boolean isEnabled()
    {
        return this.enabled;
    }

    public boolean isStatic()
    {
        return this.staticPlugin;
    }

    public String getName()
    {
        return name;
    }

    public String getRestartState()
    {
        return restartState;
    }

    public String getDescription()
    {
        return description;
    }

    public String getKey()
    {
        return key;
    }

    public boolean usesLicensing()
    {
        return usesLicensing;
    }

    public LicenseError getLicenseError()
    {
        return licenseError;
    }

    public URI getPluginIconLink()
    {
        return links.get(PLUGIN_ICON_REL);
    }

    public URI getPluginLogoLink()
    {
        return links.get(PLUGIN_LOGO_REL);
    }

    public static final class PluginOrdering extends Ordering<Plugin>
    {
        private final Collator collator;

        public PluginOrdering(Locale locale)
        {
            collator = Collator.getInstance(locale);
        }

        public int compare(Plugin p1, Plugin p2)
        {
            int result = collator.compare(getNameOrKey(p1), getNameOrKey(p2));
            return result != 0 ? result : collator.compare(p1.getKey(), p2.getKey());
        }

        private static String getNameOrKey(Plugin p)
        {
            String name = p.getName();
            return name != null ? name : p.getKey();
        }
    }

    public static Function<Plugin, InstalledPluginEntry> toEntry(PluginAccessorAndController pluginAccessorAndController,
                                                                 UpmUriBuilder uriBuilder, LinkBuilder linkBuilder, PermissionEnforcer permissionEnforcer)
    {
        return new PluginToEntryFunction(pluginAccessorAndController, uriBuilder, linkBuilder, permissionEnforcer);
    }

    private static final class PluginToEntryFunction implements Function<Plugin, InstalledPluginEntry>
    {
        private final PluginAccessorAndController pluginAccessorAndController;
        private final UpmUriBuilder uriBuilder;
        private final LinkBuilder linkBuilder;
        private final PermissionEnforcer permissionEnforcer;

        PluginToEntryFunction(PluginAccessorAndController pluginAccessorAndController, UpmUriBuilder uriBuilder,
                              LinkBuilder linkBuilder, PermissionEnforcer permissionEnforcer)
        {
            this.pluginAccessorAndController = pluginAccessorAndController;
            this.uriBuilder = uriBuilder;
            this.linkBuilder = linkBuilder;
            this.permissionEnforcer = permissionEnforcer;
        }

        public InstalledPluginEntry apply(Plugin plugin)
        {
            return new InstalledPluginEntry(plugin, pluginAccessorAndController, uriBuilder, linkBuilder, permissionEnforcer);
        }
    }

    private static final Predicate<Plugin> isUpdateAvailable = new Predicate<Plugin>()
    {
        @Override
        public boolean apply(Plugin plugin)
        {
            for (Boolean updateAvailable : plugin.isUpdateAvailable())
            {
                return updateAvailable && !Sys.isPacDisabled();
            }
            return false;
        }
    };

}