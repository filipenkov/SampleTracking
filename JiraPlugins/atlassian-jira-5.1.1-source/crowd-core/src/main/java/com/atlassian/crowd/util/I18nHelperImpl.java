package com.atlassian.crowd.util;

import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;

public class I18nHelperImpl implements I18nHelper
{
    private static final Logger LOG = Logger.getLogger(I18nHelperImpl.class);

    private final Locale locale;
    private final List<String> i18nLocations;

    public I18nHelperImpl(I18nHelperConfiguration configuration)
    {
        checkNotNull(configuration);
        this.locale = checkNotNull(configuration.getLocale());
        this.i18nLocations = checkNotNull(configuration.getBundleLocations());
    }

    public String getText(String key)
    {
        return getText(key, new Object[0]);
    }

    public String getText(String key, String value1)
    {
        return getText(key, asList(value1));
    }

    public String getText(String key, String value1, String value2)
    {
        return getText(key, asList(value1, value2));
    }

    public String getText(String key, Object parameters)
    {
        final Object[] params;
        if (parameters instanceof List)
        {
            params = ((List<?>) parameters).toArray();
        }
        else if (parameters instanceof Object[])
        {
            params = (Object[]) parameters;
        }
        else
        {
            params = new Object[]{parameters};
        }
        return new MessageFormat(getUnescapedText(key)).format(params);
    }

    /**
     * Get the raw property value, complete with {0}'s.
     * @param key Non-null key to look up
     * @return Unescaped property value for the key, or the key itself if no property with the specified key is found
     */
    public String getUnescapedText(String key)
    {
        for (String i18nLocation : i18nLocations)
        {
            try
            {
                ResourceBundle rb = getResourceBundle(i18nLocation);
                if (key.startsWith("'") && key.endsWith("'"))
                {
                    key = key.substring(1, key.length() - 1);
                }

                try
                {
                    return rb.getString(key);
                }
                catch (MissingResourceException e)
                {
                    LOG.debug("Key not present<" + key + "> in bundle <" + i18nLocation + ">");
                }
            }
            catch (MissingResourceException e)
            {
                LOG.error("Cannot load resource bundle with location '" + i18nLocation + "'.", e);
            }
        }
        return key;
    }

    private ResourceBundle getResourceBundle(String i18nLocation)
    {
        return ResourceBundle.getBundle(i18nLocation, locale);
    }
}
