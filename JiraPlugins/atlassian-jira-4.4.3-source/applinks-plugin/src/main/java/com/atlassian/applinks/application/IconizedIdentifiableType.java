package com.atlassian.applinks.application;

import com.atlassian.applinks.core.AppLinkPluginUtil;
import com.atlassian.applinks.spi.application.IdentifiableType;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Base class that generates a local icon URL based on the {@link com.atlassian.applinks.spi.application.TypeId}
 * name of the sub class.
 *
 * @since   3.1
 */
public abstract class IconizedIdentifiableType implements IdentifiableType
{
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    protected final WebResourceManager webResourceManager;
    protected final AppLinkPluginUtil pluginUtil;

    public IconizedIdentifiableType(final AppLinkPluginUtil pluginUtil,
                                    final WebResourceManager webResourceManager)
    {
        this.pluginUtil = pluginUtil;
        this.webResourceManager = webResourceManager;
    }

    public final URI getIconUrl()
    {
        try
        {
            return new URI(webResourceManager.getStaticPluginResource(pluginUtil.getPluginKey() +
                    ":applinks-images", "images", UrlMode.ABSOLUTE) + "/types/16" + getId().get() + ".png");
        }
        catch (URISyntaxException e)
        {
            LOG.warn("Unable to find the icon for this application type.", e);
            return null;
        }
    }
}
