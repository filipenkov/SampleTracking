package com.atlassian.gadgets.oauth.serviceprovider.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Load help link urls from a property file.
 */
public final class HelpLinkResolver
{
    private final Properties properties;
    private final String baseUrl;
    
    public HelpLinkResolver()
    {
        this("/com/atlassian/gadgets/oauth/serviceprovider/internal/help-links.properties");
    }
    
    public HelpLinkResolver(String fileName)
    {
        this(loadProperties(fileName));
    }
    
    public HelpLinkResolver(InputStream is)
    {
        this(loadProperties(is));
    }
    
    private static Properties loadProperties(String fileName)
    {
        InputStream is = HelpLinkResolver.class.getResourceAsStream(fileName);
        try
        {
            return loadProperties(is);
        }
        finally
        {
            try
            {
                is.close();
            } catch (IOException ignored) {}
        }
    }
    
    private static Properties loadProperties(InputStream is)
    {
        Properties properties = new Properties();
        try
        {
            properties.load(is);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return properties;
    }
    
    public HelpLinkResolver(Properties properties)
    {
        this.properties = properties;
        baseUrl = properties.getProperty("base.url");
    }

    public String getLink(String name)
    {
        checkNotNull(name, "name");
        final String key = properties.containsKey(name) ? name : "default.page";
        return baseUrl + properties.getProperty(key);
    }
}
