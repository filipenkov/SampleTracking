package com.atlassian.upm.rest.representations;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import com.atlassian.plugin.PluginRestartState;
import com.atlassian.plugins.domain.model.plugin.PluginIcon;
import com.atlassian.plugins.domain.model.plugin.PluginVersion;
import com.atlassian.upm.PluginAccessorAndController;
import com.atlassian.upm.Sys;
import com.atlassian.upm.rest.UpmUriBuilder;
import com.atlassian.upm.spi.Plugin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import static com.atlassian.upm.permission.Permission.GET_AVAILABLE_PLUGINS;
import static com.atlassian.upm.permission.Permission.MANAGE_PLUGIN_INSTALL;
import static com.atlassian.upm.rest.representations.PluginPricingItemRepresentation.toPricingItem;
import static com.atlassian.upm.rest.representations.RepresentationLinks.AVAILABLE_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.BINARY_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.DETAILS_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.INSTALLED_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.PLUGIN_BANNER_REL;
import static com.atlassian.upm.rest.representations.RepresentationLinks.SELF_REL;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.transform;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.trim;

/**
 * A JSON representation of a plugin that is available for install.  The representation will be in the form
 * <p/>
 * <pre>
 * {
 *   links: {
 *     "self": "/available/plugin.key",
 *     "binary": "http://...",
 *     "available": "/available",
 *     "installed": "/"
 *   },
 *   key: "...",
 *   name: "...",
 *   logo: {                    // won't be present if there is no logo
 *     width: 270,
 *     height: 90,
 *     link: "http://..."
 *   },
 *   vendor: {                  // won't be present if there is no vendor name or link
 *     name: "...",
 *     link: "http://..."
 *   },
 *   version: "...",
 *   installedVersion: "...",
 *   license: "...",
 *   summary: "...",
 *   description: "...",
 *   releaseNotesUrl: "...",
 *   deployable: "...",
 *   pluginSystemVersion: "..."
 * }
 * </pre>
 */
public class AvailablePluginRepresentation
{
    @JsonProperty private final Map<String, URI> links;
    @JsonProperty private final String key;
    @JsonProperty private final String name;
    @JsonProperty private final Icon logo;
    @JsonProperty private final Vendor vendor;
    @JsonProperty private final String version;
    @JsonProperty private final String installedVersion;
    @JsonProperty private final String license;
    @JsonProperty private final String summary;
    @JsonProperty private final String description;
    @JsonProperty private final String releaseNotesUrl;
    @JsonProperty private final boolean deployable;
    @JsonProperty private final String pluginSystemVersion;
    @JsonProperty private final String restartState;
    @JsonProperty private final boolean soldOnMarketplace;
    @JsonProperty private final Collection<PluginPricingItemRepresentation> pricingItems;
    @JsonProperty private final Collection<String> marketingLabels;

    @JsonCreator
    public AvailablePluginRepresentation(@JsonProperty("links") Map<String, URI> links,
        @JsonProperty("key") String key,
        @JsonProperty("name") String name,
        @JsonProperty("logo") Icon logo,
        @JsonProperty("vendor") Vendor vendor,
        @JsonProperty("version") String version,
        @JsonProperty("installedVersion") String installedVersion,
        @JsonProperty("license") String license,
        @JsonProperty("summary") String summary,
        @JsonProperty("description") String description,
        @JsonProperty("releaseNotesUrl") String releaseNotesUrl,
        @JsonProperty("deployable") boolean deployable,
        @JsonProperty("pluginSystemVersion") String pluginSystemVersion,
        @JsonProperty("restartState") String restartState,
        @JsonProperty("soldOnMarketplace") boolean soldOnMarketplace,
        @JsonProperty("pricingItems") Collection<PluginPricingItemRepresentation> pricingItems,
        @JsonProperty("marketingLabels") Collection<String> marketingLabels)
    {
        this.links = ImmutableMap.copyOf(links);
        this.key = checkNotNull(key, "key");
        this.name = checkNotNull(name, "name");
        this.logo = logo;
        this.vendor = vendor;
        this.version = checkNotNull(version, "version");
        this.installedVersion = installedVersion;
        this.license = license;
        this.summary = summary;
        this.description = description;
        this.releaseNotesUrl = releaseNotesUrl;
        this.deployable = deployable;
        this.pluginSystemVersion = pluginSystemVersion;
        this.restartState = restartState;
        this.soldOnMarketplace = soldOnMarketplace;
        this.pricingItems = ImmutableList.copyOf(pricingItems);
        this.marketingLabels = ImmutableList.copyOf(marketingLabels);
    }

    AvailablePluginRepresentation(PluginVersion plugin, UpmUriBuilder uriBuilder, LinkBuilder linkBuilder, PluginAccessorAndController pluginAccessorAndController)
    {
        // This plugin might be null if it is not installed, but that is fine for the permission check
        final Plugin actualPlugin = pluginAccessorAndController.getPlugin(plugin.getPlugin().getPluginKey());
        LinkBuilder.LinksMapBuilder links =
            linkBuilder.buildLinkForSelf(uriBuilder.buildAvailablePluginUri(plugin.getPlugin().getPluginKey()))
                .putIfPermitted(GET_AVAILABLE_PLUGINS, AVAILABLE_REL, uriBuilder.buildAvailablePluginCollectionUri())
                .put(INSTALLED_REL, uriBuilder.buildInstalledPluginCollectionUri())
                .put(DETAILS_REL, uriBuilder.buildPacPluginDetailsUri(plugin));
        if (plugin.getBinaryUrl() != null)
        {
            links.putIfPermitted(MANAGE_PLUGIN_INSTALL, actualPlugin, BINARY_REL, URI.create(trim(plugin.getBinaryUrl())));
        }
        if (plugin.getBannerUrl() != null)
        {
            links.put(PLUGIN_BANNER_REL, URI.create(trim(plugin.getBannerUrl())));
        }
        this.links = links.build();
        this.key = plugin.getPlugin().getPluginKey();
        this.name = plugin.getPlugin().getName();
        this.logo = newIcon(plugin.getPlugin().getIcon());
        this.vendor = newVendor(plugin.getPlugin().getVendor());
        this.version = plugin.getVersion();
        this.installedVersion = getInstalledVersion(pluginAccessorAndController, this.key);
        this.license = plugin.getLicense().getName();
        this.summary = plugin.getSummary();
        this.description = plugin.getDescription();
        this.releaseNotesUrl = plugin.getReleaseNotesUrl();
        if (pluginAccessorAndController.getUpmPluginKey().equals(plugin.getPlugin().getPluginKey()))
        {
            // UPM-1200: PAC will say UPM is not deployable (since older UPMs can't self-update) but for our purposes it is deployable.
            this.deployable = true;
        }
        else
        {
            this.deployable = plugin.getPlugin().isDeployable();
        }
        this.pluginSystemVersion = plugin.getPluginSystemVersion().name();
        this.restartState = RestartState.toString(actualPlugin == null ? PluginRestartState.NONE : pluginAccessorAndController.getRestartState(actualPlugin));
        this.soldOnMarketplace = PluginVersion.MarketplaceType.MARKETPLACE.equals(plugin.getMarketplaceType()) && !Sys.isOnDemand();
        this.pricingItems = ImmutableList.copyOf(transform(plugin.getPricingInfo(), toPricingItem));
        this.marketingLabels = ImmutableList.copyOf(plugin.getMarketingLabel());
    }

    private String getInstalledVersion(PluginAccessorAndController pluginAccessorAndController, String pluginKey)
    {
        if (pluginAccessorAndController.isPluginInstalled(pluginKey))
        {
            return pluginAccessorAndController.getPlugin(pluginKey).getPluginInformation().getVersion();
        }
        return null;
    }

    private Vendor newVendor(com.atlassian.plugins.domain.model.vendor.Vendor vendor)
    {
        if (vendor == null || isEmpty(vendor.getName()))
        {
            return null;
        }
        return new Vendor(vendor.getName(), isEmpty(vendor.getUrl()) ? null : URI.create(vendor.getUrl()));
    }

    private Icon newIcon(PluginIcon icon)
    {
        if (icon == null || icon.getLocation() == null)
        {
            return null;
        }
        return new Icon(icon.getWidth(), icon.getHeight(), URI.create(icon.getLocation()));
    }

    public static final class Icon
    {
        @JsonProperty private final Integer width;
        @JsonProperty private final Integer height;
        @JsonProperty private final URI link;

        @JsonCreator
        public Icon(@JsonProperty("width") Integer width,
            @JsonProperty("height") Integer height,
            @JsonProperty("link") URI link)
        {
            this.width = width;
            this.height = height;
            this.link = checkNotNull(link, "link");
        }

        public Integer getWidth()
        {
            return width;
        }

        public Integer getHeight()
        {
            return height;
        }

        public URI getLink()
        {
            return link;
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

    public String getKey()
    {
        return key;
    }

    public String getName()
    {
        return name;
    }

    public Icon getLogo()
    {
        return logo;
    }

    public Vendor getVendor()
    {
        return vendor;
    }

    public String getVersion()
    {
        return version;
    }

    public String getInstalledVersion()
    {
        return installedVersion;
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

    public boolean isSoldOnMarketplace()
    {
        return soldOnMarketplace;
    }

    public Collection<PluginPricingItemRepresentation> getPricingItems()
    {
        return pricingItems;
    }

    public String getPluginSystemVersion()
    {
        return pluginSystemVersion;
    }

    public String getRestartState()
    {
        return restartState;
    }

    public Collection<String> getMarketingLabels()
    {
        return marketingLabels;
    }
}
