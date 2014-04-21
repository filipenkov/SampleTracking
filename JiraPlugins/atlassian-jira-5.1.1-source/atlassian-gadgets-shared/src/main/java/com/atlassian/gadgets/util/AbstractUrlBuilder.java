package com.atlassian.gadgets.util;

import com.atlassian.plugin.webresource.UrlMode;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.ApplicationProperties;

public abstract class AbstractUrlBuilder implements UrlBuilder
{
    private static final String PLUGINS_URI_COMPONENTS = "/plugins/servlet/gadgets"; 

    protected final WebResourceManager webResourceManager;
    protected final ApplicationProperties applicationProperties;
    private final String pluginModuleKey;
    
    public AbstractUrlBuilder(ApplicationProperties applicationProperties,
            WebResourceManager webResourceManager,
            String pluginModuleKey)
    {
        this.applicationProperties = applicationProperties;
        this.webResourceManager = webResourceManager;
        this.pluginModuleKey = pluginModuleKey;
    }

    public String buildImageUrl(String path)
    {
        if (!path.startsWith("/"))
        {
            path = "/" + path;
        }
        return getBaseImageUrl() + path; 
    }

    public String buildRpcJsUrl()
    {
        return getBaseUrl() + "/js/rpc.js?c=1&debug=1";
    }

    protected String getBaseUrl()
    {
        return applicationProperties.getBaseUrl() + PLUGINS_URI_COMPONENTS;
    }

    private String getBaseImageUrl()
    {
        return webResourceManager.getStaticPluginResource(pluginModuleKey, "images/", UrlMode.AUTO);
    }
}
