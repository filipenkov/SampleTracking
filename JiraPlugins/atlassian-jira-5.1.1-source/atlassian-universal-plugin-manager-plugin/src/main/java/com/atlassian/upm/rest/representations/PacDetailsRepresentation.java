package com.atlassian.upm.rest.representations;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import com.atlassian.plugins.domain.model.plugin.PluginVersion;
import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.license.internal.PluginLicenseRepository;
import com.atlassian.upm.pac.PluginVersionPair;
import com.atlassian.upm.rest.UpmUriBuilder;
import com.atlassian.upm.rest.resources.permission.PermissionEnforcer;
import com.atlassian.upm.spi.Plugin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import static com.atlassian.upm.pac.PluginVersions.isLicensedToBeUpdated;
import static com.atlassian.upm.permission.Permission.GET_AVAILABLE_PLUGINS;
import static com.atlassian.upm.permission.Permission.MANAGE_PLUGIN_INSTALL;
import static com.atlassian.upm.rest.representations.PluginPricingItemRepresentation.toPricingItem;
import static com.atlassian.upm.rest.representations.RepresentationLinks.AVAILABLE_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.BINARY_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.DETAILS_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.INSTALLED_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.SELF_REL;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.transform;
import static org.apache.commons.lang.StringUtils.trim;

/**
 * Represents PAC's details of a given plugin. This contains at minimum a link to
 * the plugin's PAC details page, and at most, that plus information about
 * the latest available compatible update.
 */
public class PacDetailsRepresentation
{
    @JsonProperty private final Map<String, URI> links;
    @JsonProperty private final String installedVersion;
    @JsonProperty private final AvailablePluginUpdateRepresentation update;
    @JsonProperty private final Collection<PluginPricingItemRepresentation> pricingItems;

    @JsonCreator
    public PacDetailsRepresentation(@JsonProperty("links") Map<String, URI> links,
        @JsonProperty("installedVersion") String installedVersion,
        @JsonProperty("update") AvailablePluginUpdateRepresentation update,
        @JsonProperty("pricingItems") Collection<PluginPricingItemRepresentation> pricingItems)
    {
        this.links = ImmutableMap.copyOf(links);
        this.installedVersion = installedVersion;
        this.update = update;
        this.pricingItems = ImmutableList.copyOf(pricingItems);
    }

    PacDetailsRepresentation(PluginVersionPair pluginVersionPair, Plugin installedPlugin, UpmUriBuilder uriBuilder,
                             LinkBuilder linkBuilder, PluginAccessorAndController pluginAccessorAndController,
                             PermissionEnforcer permissionEnforcer, PluginLicenseRepository licenseRepository)
    {
        PluginVersion currentVersion = pluginVersionPair.getSpecific().getOrElse((PluginVersion)null);

        LinkBuilder.LinksMapBuilder links =
            linkBuilder.buildLinkForSelf(uriBuilder.buildPacPluginDetailsResourceUri(installedPlugin.getPlugin().getKey(), installedPlugin.getPluginInformation().getVersion()))
                .putIfPermitted(GET_AVAILABLE_PLUGINS, AVAILABLE_REL, uriBuilder.buildAvailablePluginCollectionUri())
                .put(INSTALLED_REL, uriBuilder.buildInstalledPluginCollectionUri());

        //UPM-1621 not all compatible versions are listed as compatible on PAC
        if (currentVersion != null)
        {
            links.put(DETAILS_REL, uriBuilder.buildPacPluginDetailsUri(currentVersion));
        }

        this.links = links.build();
        this.installedVersion = installedPlugin.getPluginInformation().getVersion();
        if (pluginVersionPair.getLatest().isDefined() && !pluginVersionPair.getLatest().get().equals(currentVersion) &&
            permissionEnforcer.hasPermission(MANAGE_PLUGIN_INSTALL, installedPlugin))
        {
            this.update = new AvailablePluginUpdateRepresentation(pluginVersionPair.getLatest().get(), installedPlugin, uriBuilder,
                                                                  linkBuilder, pluginAccessorAndController, licenseRepository);
        }
        else
        {
            this.update = null;
        }
        if (pluginVersionPair.getLatest().isDefined())
        {
            this.pricingItems = ImmutableList.copyOf(transform(pluginVersionPair.getLatest().get().getPricingInfo(), toPricingItem));
        }
        else
        {
            this.pricingItems = ImmutableList.of();
        }
    }

    public URI getSelfLink()
    {
        return links.get(SELF_REL);
    }

    public URI getInstalledLink()
    {
        return links.get(INSTALLED_REL);
    }

    public URI getAvailableLink()
    {
        return links.get(AVAILABLE_REL);
    }

    public URI getDetailsLink()
    {
        return links.get(DETAILS_REL);
    }

    public String getInstalledVersion()
    {
        return installedVersion;
    }

    public AvailablePluginUpdateRepresentation getUpdate()
    {
        return update;
    }

    public Collection<PluginPricingItemRepresentation> getPricingItems()
    {
        return pricingItems;
    }

    static class AvailablePluginUpdateRepresentation
    {
        @JsonProperty private final Map<String, URI> links;
        @JsonProperty private final String version;
        @JsonProperty private final String license;
        @JsonProperty private final String summary;
        @JsonProperty private final String description;
        @JsonProperty private final String releaseNotesUrl;
        @JsonProperty private final boolean deployable;
        @JsonProperty private final String pluginSystemVersion;
        @JsonProperty private final boolean licenseCompatible;

        @JsonCreator
        public AvailablePluginUpdateRepresentation(@JsonProperty("links") Map<String, URI> links,
            @JsonProperty("version") String version,
            @JsonProperty("license") String license,
            @JsonProperty("summary") String summary,
            @JsonProperty("description") String description,
            @JsonProperty("releaseNotesUrl") String releaseNotesUrl,
            @JsonProperty("deployable") boolean deployable,
            @JsonProperty("pluginSystemVersion") String pluginSystemVersion,
            @JsonProperty("licenseCompatible") boolean licenseCompatible)
        {
            this.links = ImmutableMap.copyOf(links);
            this.version = checkNotNull(version, "version");
            this.license = license;
            this.summary = summary;
            this.description = description;
            this.releaseNotesUrl = releaseNotesUrl;
            this.deployable = deployable;
            this.pluginSystemVersion = pluginSystemVersion;
            this.licenseCompatible = licenseCompatible;
        }

        AvailablePluginUpdateRepresentation(PluginVersion latestVersion, Plugin installedPlugin, UpmUriBuilder uriBuilder,
                                            LinkBuilder linkBuilder, PluginAccessorAndController pluginAccessorAndController,
                                            PluginLicenseRepository licenseRepository)
        {
            LinkBuilder.LinksMapBuilder links =
                linkBuilder.buildLinkForSelf(uriBuilder.buildPacPluginDetailsResourceUri(latestVersion.getPlugin().getPluginKey(), latestVersion.getVersion()))
                    .put(DETAILS_REL, uriBuilder.buildPacPluginDetailsUri(latestVersion));

            if (latestVersion.getBinaryUrl() != null)
            {
                links.putIfPermitted(MANAGE_PLUGIN_INSTALL, installedPlugin, "binary", URI.create(trim(latestVersion.getBinaryUrl())));
            }
            this.links = links.build();
            this.version = latestVersion.getVersion();
            this.license = latestVersion.getLicense().getName();
            this.summary = latestVersion.getSummary();
            this.description = latestVersion.getDescription();
            this.releaseNotesUrl = latestVersion.getReleaseNotesUrl();
            if (pluginAccessorAndController.getUpmPluginKey().equals(latestVersion.getPlugin().getPluginKey()))
            {
                // UPM-1200: PAC will say UPM is not deployable (since older UPMs can't self-update) but for our purposes it is deployable.
                this.deployable = true;
            }
            else
            {
                this.deployable = latestVersion.getPlugin().isDeployable();
            }
            this.pluginSystemVersion = latestVersion.getPluginSystemVersion().name();
            this.licenseCompatible = isLicensedToBeUpdated(latestVersion, licenseRepository);
        }

        public URI getSelfLink()
        {
            return links.get(SELF_REL);
        }

        public URI getBinaryLink()
        {
            return links.get(BINARY_REL);
        }

        public URI getInstalledLink()
        {
            return links.get(INSTALLED_REL);
        }

        public URI getAvailableLink()
        {
            return links.get(AVAILABLE_REL);
        }

        public URI getDetailsLink()
        {
            return links.get(DETAILS_REL);
        }

        public String getLicense()
        {
            return license;
        }

        public String getSummary()
        {
            return summary;
        }

        public String getDescription()
        {
            return description;
        }

        public String getReleaseNotesUrl()
        {
            return releaseNotesUrl;
        }

        public boolean isDeployable()
        {
            return deployable;
        }

        public boolean isLicenseCompatible()
        {
            return licenseCompatible;
        }

        public String getPluginSystemVersion()
        {
            return pluginSystemVersion;
        }
    }
}
