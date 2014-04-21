package com.atlassian.jira.config.properties;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.util.LocaleParser;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;

/**
 * A class to manage the interface with a single property set, used for application properties
 */
public class ApplicationPropertiesImpl implements ApplicationProperties, Startable
{
    private static final Logger log = Logger.getLogger(ApplicationPropertiesImpl.class);

    private final Locale defaultLocale = Locale.getDefault();
    private ApplicationPropertiesStore applicationPropertiesStore;

    public ApplicationPropertiesImpl(ApplicationPropertiesStore applicationPropertiesStore)
    {
        this.applicationPropertiesStore = applicationPropertiesStore;
    }

    public void start() throws Exception
    {
        ComponentManager.getComponent(EventPublisher.class).register(this);
    }

    @SuppressWarnings ({ "UnusedDeclaration" })
    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        refresh();
    }

    public String getText(final String name)
    {
        return this.applicationPropertiesStore.getTextFromDb(name);
    }

    public void setText(final String name, final String value)
    {
        applicationPropertiesStore.setText(name, value);
    }

    public String getString(final String name)
    {
        return applicationPropertiesStore.getStringFromDb(name);
    }

    /**
     * Get all the keys from the default properties
     */
    public Collection<String> getDefaultKeys()
    {
        return getDefaultProperties().keySet();
    }

    /**
     * Get the property from the application properties, but if not found, try to get from the default properties file.
     */
    public String getDefaultBackedString(final String name)
    {
        return applicationPropertiesStore.getString(name);
    }

    /**
     * Get the property from the application properties, but if not found, try to get from the default properties file.
     */
    public String getDefaultBackedText(final String name)
    {
        String value = null;
        try
        {
            value = getText(name);
        }
        catch (final Exception e)
        {
            log.warn("Exception getting property '" + name + "' from database. Using default");
        }
        if (value == null)
        {
            value = getDefaultString(name);
        }
        return value;
    }

    /**
     * Get the default property (if the property is not set)
     *
     * @param name the name of the property.
     */
    public String getDefaultString(final String name)
    {
        return getDefaultProperties().get(name);
    }

    public void setString(final String name, final String value)
    {
        applicationPropertiesStore.setString(name, value);
    }

    /**
     * Whether the specified key is present in the backing PropertySet. Typically called before {@link
     * #getOption(String)}
     */
    public final boolean exists(final String key)
    {
        return applicationPropertiesStore.existsInDb(key);
    }

    /**
     * Get the option from the application properties, but if not found, try to get from the default properties file.
     */
    public boolean getOption(final String key)
    {
        return applicationPropertiesStore.getOption(key);
    }

    public Collection<String> getKeys()
    {
        return applicationPropertiesStore.getKeysStoredInDb();
    }

    public Map<String, Object> asMap()
    {
        return applicationPropertiesStore.getPropertiesAsMap();
    }

    public void setOption(final String key, final boolean value)
    {
        applicationPropertiesStore.setOption(key, value);
    }

    /**
     * Convenience method to get the content type for an application
     */
    public String getEncoding()
    {
        String encoding = getString(APKeys.JIRA_WEBWORK_ENCODING);
        if (!TextUtils.stringSet(encoding))
        {
            encoding = "UTF-8";
            setString(APKeys.JIRA_WEBWORK_ENCODING, encoding);
        }
        return encoding;
    }

    /**
     * Convenience method to get the email encoding
     */
    public String getMailEncoding()
    {
        String encoding = getDefaultBackedString(APKeys.JIRA_MAIL_ENCODING);
        if (!TextUtils.stringSet(encoding))
        {
            encoding = getEncoding();

        }
        return encoding;
    }

    public String getContentType()
    {
        return "text/html; charset=" + getEncoding();
    }

    /**
     * Refresh application properties object by refreshing the PropertiesManager
     */
    public void refresh()
    {
        applicationPropertiesStore.refreshDbProperties();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).append("propertiesManager", applicationPropertiesStore).toString();
    }

    private Map<String, String> getDefaultProperties()
    {
        return applicationPropertiesStore.getDefaultsWithOverlays();
    }

    public Locale getDefaultLocale()
    {
        // The default locale almost never changes, but we must handle it correctly when it does.
        // We store the Locale for the defaultLocale string, which is expensive to create, in a very small cache (map).
        final String localeString = getDefaultBackedString(APKeys.JIRA_I18N_DEFAULT_LOCALE);
        if (localeString != null)
        {
            return LocaleParser.parseLocale(localeString);
        }
        return defaultLocale;
    }

    public Collection<String> getStringsWithPrefix(final String prefix)
    {
        return applicationPropertiesStore.getStringsWithPrefixFromDb(prefix);
    }
}