package com.atlassian.gadgets.dashboard.internal.util;

import com.atlassian.sal.api.message.HelpPath;
import com.atlassian.sal.api.message.HelpPathResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.io.IOUtils.closeQuietly;

/**
 * Load help link urls from a property file.
 */
public final class HelpLinkResolver
{
    private final Properties prop;
    private final String baseUrl;
    private final HelpPathResolver helpPathResolver;

    public HelpLinkResolver(final HelpPathResolver helpPathResolver)
    {
        this.helpPathResolver = helpPathResolver;
        this.prop = new Properties();
        InputStream is = getClass().getResourceAsStream("/gadget-help-paths.properties");
        try
        {
            this.prop.load(is);
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            closeQuietly(is);
        }

        baseUrl = prop.getProperty("base.url");
    }

    public String getLink(String name)
    {
        checkNotNull(name, "name");
        String url = null;
        if (prop.containsKey(name))
        {
            url = baseUrl + prop.getProperty(name);
        }
        else if (helpPathResolver != null)
        {
            HelpPath helpPath = helpPathResolver.getHelpPath(name);
            if (helpPath != null)
            {
                url = helpPath.getUrl();
            }
        }
        if (url == null)
        {
            url = baseUrl + prop.getProperty("default.page");
        }
        return url;
    }
}
