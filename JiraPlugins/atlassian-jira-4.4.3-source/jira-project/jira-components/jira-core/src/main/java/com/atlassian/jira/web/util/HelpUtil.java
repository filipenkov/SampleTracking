/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */

package com.atlassian.jira.web.util;

import com.atlassian.core.util.ClassLoaderUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Helps look up titles and URLs of help links.
 */
public class HelpUtil
{
    private final Logger log = Logger.getLogger(HelpUtil.class);

    static final String HELP_PATH_CONFIG_LOCATION = "help-paths.properties";
    static final String INTERNAL_HELP_PATH_CONFIG_LOCATION = "internal-help-paths.properties";
    
    private static HelpPath defaultHelpPath;
    private static volatile Map<String, HelpPath> helpPaths;

    private static volatile HelpUtil instance;

    public HelpUtil()
    {
        this(HELP_PATH_CONFIG_LOCATION, INTERNAL_HELP_PATH_CONFIG_LOCATION);
    }

    public HelpUtil(String externalPropertiesFileLocation, String internalPropertiesFileLocation)
    {
        init(externalPropertiesFileLocation, internalPropertiesFileLocation);
    }

    private void init(String externalPropertiesFileLocation, String internalPropertiesFileLocation)
    {
        // Need to synchronise here as we are modifying static variables.
        synchronized (HelpUtil.class)
        {
            if (helpPaths == null)
            {
                Properties properties = loadProperties(externalPropertiesFileLocation);
                loadExternalProperties(properties, true);

                properties = loadProperties(internalPropertiesFileLocation);
                loadInternalProperties(properties);
            }
        }
    }

    private Properties loadProperties(String propertiesFileLocation)
    {
        Properties properties = new Properties();
        try
        {
            final InputStream is = ClassLoaderUtils.getResourceAsStream(propertiesFileLocation, this.getClass());
            properties.load(is);
            is.close();
        }
        catch (IOException e)
        {
            log.error("Error loading helpfile " + propertiesFileLocation + ": " + e, e);
        }
        return properties;
    }


    public HelpUtil(Properties externalProperties)
    {
        loadExternalProperties(externalProperties, true);
    }

    /**
     * Returns the {@link com.atlassian.jira.web.util.HelpUtil.HelpPath} object for a given key
     *
     * @param helpPathKey the key in play
     *
     * @return the {@link com.atlassian.jira.web.util.HelpUtil.HelpPath} for tht key or defaultHelpPath if it cant be
     *         found.
     */
    public HelpPath getHelpPath(String helpPathKey)
    {
        HelpPath theHelpPath = helpPaths.get(helpPathKey);
        if (theHelpPath == null)
        {
            return defaultHelpPath;
        }
        else
        {
            return theHelpPath;
        }
    }

    /**
     * @return a set of all the keys that have {@link com.atlassian.jira.web.util.HelpUtil.HelpPath} values
     */
    public Set<String> keySet()
    {
        return new HashSet<String>(helpPaths.keySet());
    }

    private void loadExternalProperties(Properties properties, boolean reload)
    {
        if (helpPaths == null || reload)
        {
            final Map<String, HelpPath> newPaths = new HashMap<String, HelpPath>();

            String urlPrefix = properties.getProperty("url-prefix");
            String urlSuffix = properties.getProperty("url-suffix");

            defaultHelpPath = loadHelpPath("default", properties, urlPrefix, urlSuffix, null);

            Enumeration propertyNames = properties.propertyNames();
            while (propertyNames.hasMoreElements())
            {
                String property = (String) propertyNames.nextElement();

                //use '.url' as the key for whether it is a property or not
                int urlIndex = property.indexOf(".url");
                if (urlIndex != -1)
                {
                    String propertyName = property.substring(0, urlIndex);
                    HelpPath helpPath = loadHelpPath(propertyName, properties, urlPrefix, urlSuffix, defaultHelpPath);
                    newPaths.put(helpPath.getKey(), helpPath);
                }
            }
            helpPaths = newPaths;
        }
    }

    private void loadInternalProperties(Properties properties)
    {
        Iterator iter = properties.keySet().iterator();
        while (iter.hasNext())
        {
            String keyStr = (String) iter.next();
            if (keyStr.endsWith(".path"))
            {
                String key = keyStr.substring(0, keyStr.length() - 5);
                HelpPath helpPath = new HelpPath();
                helpPath.setKey(key);
                helpPath.setUrl((String) properties.get(keyStr));
                helpPath.setTitle((String) properties.get(key + ".title"));
                helpPath.setLocal(Boolean.TRUE);
                helpPaths.put(key, helpPath);
            }
        }

    }

    private HelpPath loadHelpPath(String name, Properties props, String prefix, String suffix, HelpPath defaultHelpPath)
    {
        HelpPath helpPath;
        if (defaultHelpPath == null)
        {
            helpPath = new HelpPath();
        }
        else
        {
            helpPath = (HelpPath) defaultHelpPath.clone();
        }

        //key cannot be null
        helpPath.setKey(name);

        String url = props.getProperty(name + ".url");
        if (url != null)
        {
            String builtURL = buildValidUrl(prefix, url, suffix);
            if (isExternalLink(url))
            {
                builtURL = buildValidUrl("", url, "");
            }
            helpPath.setUrl(builtURL);
        }

        String alt = props.getProperty(name + ".alt");
        if (alt != null)
        {
            helpPath.setAlt(alt);
        }

        String title = props.getProperty(name + ".title");
        if (title != null)
        {
            helpPath.setTitle(title);
        }

        return helpPath;
    }

    /**
     * Some URLS are in the form a/b#someanchor.  If we simply append the suffix then then it may produce an invalid
     * URL.  So we make sure we are more careful
     *
     * @param prefix the prefix to append
     * @param url    the url to use
     * @param suffix the suffix to append
     *
     * @return the smooshed string
     */
    private String buildValidUrl(final String prefix, final String url, final String suffix)
    {
        String targetUrl = url;
        String targetSuffix = suffix;
        int hashIndex = url.indexOf("#");
        if (hashIndex != -1 && (suffix.contains("?") || suffix.contains("&")))
        {
            targetUrl = url.substring(0, hashIndex);
            targetSuffix = suffix + url.substring(hashIndex);
        }

        return new StringBuilder().append(prefix).append(targetUrl).append(targetSuffix).toString();
    }

    private boolean isExternalLink(String url)
    {
        String urlLowerCase = url.toLowerCase();
        return urlLowerCase.startsWith("http://") || urlLowerCase.startsWith("https://");
    }

    public class HelpPath implements Cloneable
    {
        String url;
        String alt;
        String title;
        String key;
        Boolean local;

        public String getUrl()
        {
            return url;
        }

        public String getAlt()
        {
            return alt;
        }

        public String getTitle()
        {
            return title;
        }

        public String getKey()
        {
            return key;
        }

        public void setUrl(String url)
        {
            this.url = url;
        }

        public void setAlt(String alt)
        {
            this.alt = alt;
        }

        public void setTitle(String title)
        {
            this.title = title;
        }

        public void setKey(String key)
        {
            this.key = key;
        }

        public Boolean isLocal()
        {
            return local;
        }

        /* Velocity doesn't understand $helpPath.local unless we have this. */
        public Boolean getLocal()
        {
            return local;
        }

        public void setLocal(Boolean local)
        {
            this.local = local;
        }

        public Object clone()
        {
            try
            {
                return super.clone();
            }
            catch (CloneNotSupportedException e)
            {
                log.error(e, e);
                return null;
            }
        }

        public String toString()
        {
            return new ToStringBuilder(this).append("url", url).append("title", title).append("key", key).append("alt", alt).toString();
        }
    }

    public static HelpUtil getInstance()
    {
        if (instance == null)
        {
            instance = new HelpUtil();
        }

        return instance;
    }
}
