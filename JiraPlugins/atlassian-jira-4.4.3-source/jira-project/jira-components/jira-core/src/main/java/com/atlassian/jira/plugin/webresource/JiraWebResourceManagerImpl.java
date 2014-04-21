package com.atlassian.jira.plugin.webresource;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.plugin.webresource.PluginResourceLocator;
import com.atlassian.plugin.webresource.ResourceBatchingConfiguration;
import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceIntegration;
import com.atlassian.plugin.webresource.WebResourceManagerImpl;
import org.apache.log4j.Logger;

/**
 * A simple subclass of WebResourceManagerImpl that allows us to override
 * some of its behaviour
 *
 * @since v4.4
 */
public class JiraWebResourceManagerImpl extends WebResourceManagerImpl
{
    private static final Logger log = Logger.getLogger(JiraWebResourceManagerImpl.class);

    private String staticBaseUrl;

    public JiraWebResourceManagerImpl(ApplicationProperties appProps,
            PluginResourceLocator pluginResourceLocator, WebResourceIntegration webResourceIntegration,
            ResourceBatchingConfiguration batchingConfiguration)
    {
        super(pluginResourceLocator, webResourceIntegration, batchingConfiguration);

        String cdn = appProps.getDefaultBackedString("jira.cdn.static.prefix");
        if (cdn != null) {
            if (cdn.endsWith("/")) {
                cdn = cdn.substring(0, cdn.length() - 1);
            }
            log.info("CDN static prefix in use, prefix=" + cdn);
        }
        staticBaseUrl = cdn;
    }

    @Override
    public String getStaticResourcePrefix(String resourceCounter, UrlMode urlMode)
    {
        if (staticBaseUrl != null) {
            // force urlMode to RELATIVE, we'll add our own prefix
            return staticBaseUrl + super.getStaticResourcePrefix(resourceCounter, UrlMode.RELATIVE);
        }
        return super.getStaticResourcePrefix(resourceCounter, urlMode);
    }

    @Override
    public String getStaticResourcePrefix(UrlMode urlMode)
    {
        if (staticBaseUrl != null) {
            // force urlMode to RELATIVE, we'll add our own prefix
            return staticBaseUrl + super.getStaticResourcePrefix(UrlMode.RELATIVE);
        }
        return super.getStaticResourcePrefix(urlMode);
    }
}
