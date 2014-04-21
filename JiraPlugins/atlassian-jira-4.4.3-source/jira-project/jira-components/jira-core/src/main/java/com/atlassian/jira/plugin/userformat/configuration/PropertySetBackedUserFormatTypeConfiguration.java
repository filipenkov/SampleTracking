package com.atlassian.jira.plugin.userformat.configuration;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import com.atlassian.util.concurrent.ResettableLazyReference;
import com.opensymphony.module.propertyset.PropertySet;

import java.util.Collection;

/**
 * Responsible for storing the configured user format modules for each user format type in a property set.
 *
 * The underlying property set stores the name of a user format type as a key an its value is the plugin module key
 * of the user format descriptor currently configured for that type.
 *
 * @since v3.13
 */
public class PropertySetBackedUserFormatTypeConfiguration implements UserFormatTypeConfiguration, Startable
{
    static final String USER_FORMAT_CONFIGURATION_PROPERTY_SET_KEY = "user.format.mapping";

    private final EventPublisher eventPublisher;
    private final ResettableLazyReference<PropertySet> mappingPSRef;

    public PropertySetBackedUserFormatTypeConfiguration(final JiraPropertySetFactory jiraPropertySetFactory,
            final EventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
        this.mappingPSRef = new ResettableLazyReference<PropertySet>()
        {
            @Override
            protected PropertySet create() throws Exception
            {
                return jiraPropertySetFactory.
                        buildCachingDefaultPropertySet(USER_FORMAT_CONFIGURATION_PROPERTY_SET_KEY, true);
            }
        };
    }

    @Override
    public void start()
    {
        eventPublisher.register(this);
    }

    @com.atlassian.event.api.EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        mappingPSRef.reset();
    }

    public void setUserFormatKeyForType(final String userFormatType, final String moduleKey)
    {
        mappingPSRef.get().setString(userFormatType, moduleKey);
    }

    @Override
    public String getUserFormatKeyForType(final String userFormatType)
    {
        return mappingPSRef.get().getString(userFormatType);
    }

    @Override
    public boolean containsType(final String userFormatType)
    {
        return mappingPSRef.get().getString(userFormatType) != null;
    }

    @Override
    public void remove(final String userFormatType)
    {
        mappingPSRef.get().remove(userFormatType);
    }

    Collection<String> getConfiguredTypes()
    {
        return mappingPSRef.get().getKeys();
    }
}
