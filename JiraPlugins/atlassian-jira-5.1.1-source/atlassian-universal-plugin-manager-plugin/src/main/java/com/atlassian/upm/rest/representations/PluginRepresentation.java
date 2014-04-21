package com.atlassian.upm.rest.representations;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import javax.annotation.Nullable;

import com.atlassian.plugin.PluginRestartState;
import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.atlassian.upm.api.util.Option;
import com.atlassian.upm.rest.UpmUriBuilder;
import com.atlassian.upm.rest.representations.LinkBuilder.LinksMapBuilder;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;
import com.atlassian.upm.spi.Plugin;
import com.atlassian.upm.spi.Plugin.Module;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import static com.atlassian.upm.api.license.entity.LicenseError.EXPIRED;
import static com.atlassian.upm.api.license.entity.LicenseError.VERSION_MISMATCH;
import static com.atlassian.upm.license.PluginLicenses.isPluginBuyable;
import static com.atlassian.upm.license.PluginLicenses.isPluginRenewable;
import static com.atlassian.upm.license.PluginLicenses.isPluginTryable;
import static com.atlassian.upm.license.PluginLicenses.isPluginUpgradable;
import static com.atlassian.upm.permission.Permission.GET_AVAILABLE_PLUGINS;
import static com.atlassian.upm.permission.Permission.GET_PLUGIN_MODULES;
import static com.atlassian.upm.permission.Permission.MANAGE_PLUGIN_ENABLEMENT;
import static com.atlassian.upm.permission.Permission.MANAGE_PLUGIN_LICENSE;
import static com.atlassian.upm.permission.Permission.MANAGE_PLUGIN_MODULE_ENABLEMENT;
import static com.atlassian.upm.permission.Permission.MANAGE_PLUGIN_UNINSTALL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.CHANGE_REQUIRING_RESTART_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.DELETE_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.LICENSE_CALLBACK_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.MODIFY_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.NEW_LICENSE_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.PAC_DETAILS_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.PLUGIN_DETAILS_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.PLUGIN_ICON_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.PLUGIN_LOGO_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.RENEW_LICENSE_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.RENEW_LICENSE_REQUIRES_CONTACT_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.SELF_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.TRY_LICENSE_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.UPDATE_LICENSE_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.UPGRADE_LICENSE_REL;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Collections2.transform;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotBlank;


/**
 * Jackson representation of a particular plugin.
 */
public class PluginRepresentation
{
    @JsonProperty private final Map<String, URI> links;
    @JsonProperty private final String key;
    @JsonProperty private final boolean enabled;
    @JsonProperty private final boolean enabledByDefault;
    @JsonProperty private final String version;
    @JsonProperty private final String description;
    @JsonProperty private final Vendor vendor;
    @JsonProperty private final String name;
    @JsonProperty private final Collection<ModuleEntryRepresentation> modules;
    @JsonProperty private final boolean userInstalled;
    @JsonProperty private final boolean optional;
    @JsonProperty private final boolean unrecognisedModuleTypes;
    @JsonProperty private final String configureUrl;
    @JsonProperty private final String restartState;
    @JsonProperty private final LicenseDetailsRepresentation licenseDetails;
    @JsonProperty private final boolean licenseReadOnly;
    @JsonProperty private final URI licenseAdminUri;

    @JsonCreator
    public PluginRepresentation(@JsonProperty("links") Map<String, URI> links,
                                @JsonProperty("key") String key,
                                @JsonProperty("enabled") boolean enabled,
                                @JsonProperty("enabledByDefault") boolean enabledByDefault,
                                @JsonProperty("version") String version,
                                @JsonProperty("description") String description,
                                @JsonProperty("vendor") Vendor vendor,
                                @JsonProperty("name") String name,
                                @JsonProperty("modules") Collection<ModuleEntryRepresentation> modules,
                                @JsonProperty("userInstalled") boolean userInstalled,
                                @JsonProperty("optional") boolean optional,
                                @JsonProperty("unrecognisedModuleTypes") boolean unrecognisedModuleTypes,
                                @JsonProperty("configureUrl") String configureUrl,
                                @JsonProperty("restartState") String restartState,
                                @JsonProperty("licenseDetails") LicenseDetailsRepresentation licenseDetails,
                                @JsonProperty("licenseReadOnly") boolean licenseReadOnly,
                                @JsonProperty("licenseAdminUri") URI licenseAdminUri)
    {
        this.links = ImmutableMap.copyOf(links);
        this.key = key;
        this.enabled = enabled;
        this.enabledByDefault = enabledByDefault;
        this.version = version;
        this.description = description;
        this.vendor = vendor;
        this.name = name;
        this.modules = ImmutableList.copyOf(modules);
        this.userInstalled = userInstalled;
        this.optional = optional;
        this.unrecognisedModuleTypes = unrecognisedModuleTypes;
        this.configureUrl = configureUrl;
        this.restartState = restartState;
        this.licenseDetails = licenseDetails;
        this.licenseReadOnly = licenseReadOnly;
        this.licenseAdminUri = licenseAdminUri;
    }

    PluginRepresentation(final PluginAccessorAndController pluginAccessorAndController, final Plugin plugin,
                         final UpmUriBuilder uriBuilder, final LinkBuilder linkBuilder, final PermissionEnforcer permissionEnforcer,
                         final RepresentationFactory factory)
    {
        this.key = checkNotNull(plugin, "plugin").getKey();
        this.enabled = checkNotNull(pluginAccessorAndController, "pluginAccessorAndController").isPluginEnabled(plugin.getKey());
        this.enabledByDefault = plugin.isEnabledByDefault();
        this.version = plugin.getPluginInformation().getVersion();
        this.description = plugin.getPluginInformation().getDescription();
        this.vendor = newVendor(plugin);
        this.optional = pluginAccessorAndController.isOptional(plugin);
        this.userInstalled = pluginAccessorAndController.isUserInstalled(plugin);
        this.unrecognisedModuleTypes = plugin.hasUnrecognisedModuleTypes();
        this.name = plugin.getName();

        boolean usesLicensing = usesLicensing(pluginAccessorAndController, permissionEnforcer).apply(plugin);
        if (usesLicensing && plugin.getLicense().isDefined())
        {
            this.licenseDetails = factory.createPluginLicenseRepresentation(plugin.getLicense().get());
        }
        else
        {
            this.licenseDetails = null;
        }
        this.licenseReadOnly = usesLicensing && pluginAccessorAndController.isLicenseReadOnly(plugin);
        if (this.licenseReadOnly)
        {
            this.licenseAdminUri = pluginAccessorAndController.getLicenseAdminUri(plugin).getOrElse((URI) null);
        }
        else
        {
            this.licenseAdminUri = null;
        }

        PluginRestartState restart = pluginAccessorAndController.getRestartState(plugin);
        this.restartState = RestartState.toString(restart);

        this.links = buildLinks(plugin, pluginAccessorAndController, uriBuilder, linkBuilder, usesLicensing, restart);

        if (permissionEnforcer.hasPermission(GET_PLUGIN_MODULES, plugin))
        {
            this.modules = transform(ImmutableList.copyOf(plugin.getModules()), new Function<Module, ModuleEntryRepresentation>()
            {
                public ModuleEntryRepresentation apply(@Nullable Module module)
                {
                    return new ModuleEntryRepresentation(module, pluginAccessorAndController, uriBuilder, linkBuilder);
                }
            });
        }
        else
        {
            this.modules = null;
        }

        this.configureUrl = getConfigureUrl(plugin);
    }

    private Map<String, URI> buildLinks(final Plugin plugin, final PluginAccessorAndController pluginAccessorAndController, final UpmUriBuilder uriBuilder,
                                        final LinkBuilder linkBuilder, boolean usesLicensing, PluginRestartState restart)
    {
        LinksMapBuilder builder = linkBuilder.buildLinkForSelf(uriBuilder.buildPluginUri(plugin.getKey()))
            .putIfPermitted(MANAGE_PLUGIN_ENABLEMENT, plugin, MODIFY_REL, uriBuilder.buildPluginUri(plugin.getKey()))
            .putIfPermitted(GET_AVAILABLE_PLUGINS, plugin, PLUGIN_ICON_REL, uriBuilder.buildPluginIconLocationUri(plugin.getKey()))
            .putIfPermitted(GET_AVAILABLE_PLUGINS, plugin, PLUGIN_LOGO_REL, uriBuilder.buildPluginLogoLocationUri(plugin.getKey()))
            .putIfPermittedAndConditioned(MANAGE_PLUGIN_UNINSTALL, plugin, isUninstallable(pluginAccessorAndController), DELETE_REL, uriBuilder.buildPluginUri(plugin.getKey()))
            .put(PAC_DETAILS_REL, uriBuilder.buildPacPluginDetailsResourceUri(plugin.getKey(), plugin.getVersion()))
            .put(PLUGIN_DETAILS_REL, uriBuilder.buildUpmTabPluginUri("manage", plugin.getKey()));

        if (!PluginRestartState.NONE.equals(restart))
        {
            builder.put(CHANGE_REQUIRING_RESTART_REL, uriBuilder.buildChangeRequiringRestart(plugin.getKey()));
        }

        if (usesLicensing)
        {
            builder
                .putIfPermitted(MANAGE_PLUGIN_LICENSE, plugin, UPDATE_LICENSE_REL, uriBuilder.buildPluginUri(plugin.getKey()))
                .putIfPermitted(MANAGE_PLUGIN_LICENSE, plugin, LICENSE_CALLBACK_REL, uriBuilder.buildLicenseReceiptUri(plugin.getKey()));

            Option<PluginLicense> possiblePluginLicense = plugin.getLicense();

            //the following if statement logic is intentional:
            //for example, licenses may be both tryable and buyable, but will NEVER be both buyable and renewable.

            if (isPluginTryable(possiblePluginLicense))
            {
                builder.putIfPermitted(MANAGE_PLUGIN_LICENSE, plugin, TRY_LICENSE_REL, uriBuilder.buildMacPluginLicenseUri(plugin.getKey(), "try"));
            }
            if (isPluginBuyable(possiblePluginLicense))
            {
                builder.putIfPermitted(MANAGE_PLUGIN_LICENSE, plugin, NEW_LICENSE_REL, uriBuilder.buildMacPluginLicenseUri(plugin.getKey(), "new"));
            }
            else if (isPluginUpgradable(possiblePluginLicense))
            {
                builder.putIfPermitted(MANAGE_PLUGIN_LICENSE, plugin, UPGRADE_LICENSE_REL, uriBuilder.buildMacPluginLicenseUri(plugin.getKey(), "upgrade"));
            }
            //is the plugin renewable directly on MAC?
            else if (isPluginRenewable(possiblePluginLicense))
            {
                builder.putIfPermitted(MANAGE_PLUGIN_LICENSE, plugin, RENEW_LICENSE_REL, uriBuilder.buildMacPluginLicenseUri(plugin.getKey(), "renew"));
            }
            //is the plugin renewable and the vendor has contact information?
            else if ((licenseDetails.isNearlyExpired() || ImmutableSet.of(EXPIRED, VERSION_MISMATCH).contains(licenseDetails.getError())))
            {
                Vendor vendor = newVendor(plugin);

                if (vendor != null && vendor.getLink() != null)
                {
                    builder.putIfPermitted(MANAGE_PLUGIN_LICENSE, plugin, RENEW_LICENSE_REQUIRES_CONTACT_REL, vendor.getLink());
                }
            }
        }

        return builder.build();
    }

    private static final Predicate<Plugin> usesLicensing(PluginAccessorAndController pluginAccessorAndController, PermissionEnforcer permissionEnforcer)
    {
        return new UsesLicensing(pluginAccessorAndController, permissionEnforcer);
    }

    private static class UsesLicensing implements Predicate<Plugin>
    {
        private final PluginAccessorAndController pluginAccessorAndController;
        private final PermissionEnforcer permissionEnforcer;

        UsesLicensing(PluginAccessorAndController pluginAccessorAndController, PermissionEnforcer permissionEnforcer)
        {
            this.pluginAccessorAndController = checkNotNull(pluginAccessorAndController, "pluginAccessorAndController");
            this.permissionEnforcer = checkNotNull(permissionEnforcer, "permissionEnforcer");
        }

        public boolean apply(Plugin plugin)
        {
            return pluginAccessorAndController.usesLicensing(plugin) && permissionEnforcer.hasPermission(MANAGE_PLUGIN_LICENSE, plugin);
        }
    }

    private String getConfigureUrl(final Plugin plugin)
    {
        Map<String, String> map = plugin.getPluginInformation().getParameters();

        if (isNotBlank(map.get("configure.url")))
        {
            return map.get("configure.url");
        }
        else
        {
            return "";
        }
    }

    private Vendor newVendor(Plugin plugin)
    {
        String name = plugin.getPluginInformation().getVendorName();
        String url = plugin.getPluginInformation().getVendorUrl();
        
        if (isEmpty(name))
        {
            return null;
        }

        if (isEmpty(url))
        {
            return new Vendor(name, null);
        }

        try
        {
            URI vendorUri = URI.create(url);
            return new Vendor(name, vendorUri);
        }
        catch (IllegalArgumentException iae)
        {
            // ignore it since it's not valid anyway.
            return new Vendor(name, null);
        }
    }

    /**
     * Get the plugin key.
     * @return the plugin key
     */
    public String getKey()
    {
        return key;
    }

    /**
     * Get the URI to this representation.
     *
     * @return the representation's URI
     */
    public URI getSelfLink()
    {
        return links.get(SELF_REL);
    }

    /**
     * Get the enablement status of the plugin represented.
     *
     * @return the true if the plugin is enabled, false otherwise
     */
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * Get the enablement (by default) status of the plugin represented.
     *
     * @return the true if the plugin is enabled by default, false otherwise
     */
    public boolean isEnabledByDefault()
    {
        return enabledByDefault;
    }

    /**
     * Returns {@code true} if plugin is user-installed, {@code false} otherwise
     *
     * @return {@code true} if plugin is user-installed, {@code false} otherwise
     */
    public boolean isUserInstalled()
    {
        return userInstalled;
    }

    /**
     * @return {@code true} if plugin is considered to not be required by the host application parameter, {@code false} otherwise
     */
    public boolean isOptional()
    {
        return optional;
    }

    /**
     * Returns {@code true} if one or more of this plugin's module desciptors is of an unrecognised module type. false if not.
     *
     * @return {@code true} if one or more of this plugin's module desciptors is of an unrecognised module type. false if not.
     */
    public boolean hasUnrecognisedModuleTypes()
    {
        return unrecognisedModuleTypes;
    }

    /**
     * Get the plugin version
     *
     * @return the plugin version
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * Returns {@code true} if plugin has a configure link, {@code false} otherwise
     *
     * @return {@code true} if plugin has a configure link, {@code false} otherwise
     */

    @JsonIgnore
    public boolean isConfigurable()
    {
        return StringUtils.isNotBlank(configureUrl);
    }

    public String getDescription()
    {
        return description;
    }

    public Collection<ModuleEntryRepresentation> getModules()
    {
        return modules;
    }

    public String getName()
    {
        return name;
    }

    public Vendor getVendor()
    {
        return vendor;
    }

    public String getRestartState()
    {
        return restartState;
    }

    public URI getChangeRequiringRestartLink()
    {
        return links.get(CHANGE_REQUIRING_RESTART_REL);
    }

    public boolean getUsesLicensing()
    {
        return links.containsKey(UPDATE_LICENSE_REL);
    }

    public LicenseDetailsRepresentation getLicenseDetails()
    {
        return licenseDetails;
    }

    public boolean isLicenseReadOnly()
    {
        return licenseReadOnly;
    }

    public URI getLicenseAdminUri()
    {
        return licenseAdminUri;
    }

    public URI getPluginIconLink()
    {
        return links.get(PLUGIN_ICON_REL);
    }

    public URI getPluginLogoLink()
    {
        return links.get(PLUGIN_LOGO_REL);
    }

    public URI getPluginDetailsLink()
    {
        return links.get(PLUGIN_DETAILS_REL);
    }

    public URI getPacDetailsLink()
    {
        return links.get(PAC_DETAILS_REL);
    }

    public URI getUninstallLink()
    {
        return links.get(DELETE_REL);
    }

    public static class ModuleEntryRepresentation
    {
        @JsonProperty private final String key;
        @JsonProperty private final String completeKey;
        @JsonProperty private final Map<String, URI> links;
        @JsonProperty private final boolean enabled;
        @JsonProperty private final boolean optional;
        @JsonProperty private final String name;
        @JsonProperty private final String description;
        @JsonProperty private final boolean recognisableType;

        @JsonCreator
        public ModuleEntryRepresentation(@JsonProperty("key") String key,
            @JsonProperty("completeKey") String completeKey,
            @JsonProperty("links") Map<String, URI> links,
            @JsonProperty("enabled") boolean enabled,
            @JsonProperty("optional") boolean optional,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("recognisableType") boolean recognisableType)
        {
            this.key = key;
            this.completeKey = completeKey;
            this.links = ImmutableMap.copyOf(links);
            this.name = name;
            this.enabled = enabled;
            this.description = description;
            this.optional = optional;
            this.recognisableType = recognisableType;
        }

        public ModuleEntryRepresentation(Module module,
            PluginAccessorAndController pluginAccessorAndController,
            UpmUriBuilder uriBuilder, LinkBuilder linkBuilder)
        {
            this.key = module.getKey();
            this.completeKey = module.getCompleteKey();
            this.links = linkBuilder.builder()
                .putIfPermitted(MANAGE_PLUGIN_MODULE_ENABLEMENT, module, SELF_REL, uriBuilder.buildPluginModuleUri(module.getPlugin().getKey(), key))
                .build();
            this.enabled = pluginAccessorAndController.isPluginModuleEnabled(module.getCompleteKey());
            this.optional = pluginAccessorAndController.isOptional(module);
            this.recognisableType = module.hasRecognisableType();

            // added to fix UPM-925: plugins waiting to be installed on restart aren't really all there yet, and thus
            // can have problems getting their module names and descriptions, for example if they are i18n'd
            if (pluginAccessorAndController.getRestartRequiredChange(module.getPlugin()) == null
                || !"install".equals(pluginAccessorAndController.getRestartRequiredChange(module.getPluginKey()).getAction()))
            {
                this.name = module.getName();
                this.description = module.getDescription();
            }
            else
            {
                this.name = null;
                this.description = null;
            }
        }

        public String getKey()
        {
            return key;
        }

        public String getCompleteKey()
        {
            return completeKey;
        }

        public URI getSelfLink()
        {
            return links.get(SELF_REL);
        }

        public boolean isEnabled()
        {
            return enabled;
        }

        public boolean hasRecognisableType()
        {
            return recognisableType;
        }

        public String getName()
        {
            return name;
        }

        public String getDescription()
        {
            return description;
        }

        public boolean isOptional()
        {
            return optional;
        }
    }

    public static final class Vendor
    {
        @JsonProperty private final String name;
        @JsonProperty private final URI link;

        @JsonCreator
        public Vendor(@JsonProperty("name") String name, @JsonProperty("link") URI link)
        {
            this.name = checkNotNull(name, "name");
            this.link = link;
        }

        public String getName()
        {
            return name;
        }

        public URI getLink()
        {
            return link;
        }
    }

    static final Predicate<Plugin> isUninstallable(PluginAccessorAndController pluginAccessorAndController)
    {
        return new IsUninstallable(pluginAccessorAndController);
    }

    static class IsUninstallable implements Predicate<Plugin>
    {
        private final PluginAccessorAndController pluginAccessorAndController;

        public IsUninstallable(PluginAccessorAndController pluginAccessorAndController)
        {
            this.pluginAccessorAndController = pluginAccessorAndController;
        }

        @Override
        public boolean apply(Plugin plugin)
        {
            //PluginResource.uninstallPlugin() restricts these attributes from allowing uninstallation.
            return !plugin.isStaticPlugin()
                   && pluginAccessorAndController.isUserInstalled(plugin)
                   && plugin.isUninstallable()
                   && !plugin.getKey().equals(pluginAccessorAndController.getUpmPluginKey());
        }
    };
}
