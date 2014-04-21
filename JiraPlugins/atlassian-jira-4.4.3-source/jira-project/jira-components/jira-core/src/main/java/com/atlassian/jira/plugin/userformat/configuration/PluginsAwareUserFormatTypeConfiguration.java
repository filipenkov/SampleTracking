package com.atlassian.jira.plugin.userformat.configuration;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.plugin.userformat.UserFormatModuleDescriptor;
import com.atlassian.jira.propertyset.JiraPropertySetFactory;
import com.atlassian.plugin.event.PluginEventListener;
import com.atlassian.plugin.event.events.PluginUninstalledEvent;
import com.google.common.collect.Iterables;

/**
 * Reacts to plugin system events and updates the user format configuration accordingly.
 *
 * Delegates storage and retrieval to an instance of {@link PropertySetBackedUserFormatTypeConfiguration}
 *
 * @since v4.4
 */
public class PluginsAwareUserFormatTypeConfiguration implements UserFormatTypeConfiguration, Startable
{
    private final EventPublisher eventPublisher;
    private final PropertySetBackedUserFormatTypeConfiguration delegate;

    public PluginsAwareUserFormatTypeConfiguration(final EventPublisher eventPublisher,
            final JiraPropertySetFactory jiraPropertySetFactory)
    {
        this.eventPublisher = eventPublisher;
        this.delegate = new PropertySetBackedUserFormatTypeConfiguration(jiraPropertySetFactory, eventPublisher);
    }

    @Override
    public void start() throws Exception
    {
        eventPublisher.register(this);
    }

    @PluginEventListener
    public void onPluginUninstalled(final PluginUninstalledEvent event)
    {
        final Iterable<UserFormatModuleDescriptor> userFormatModuleDescriptors =
                Iterables.filter(event.getPlugin().getModuleDescriptors(), UserFormatModuleDescriptor.class);

        if (Iterables.size(userFormatModuleDescriptors) > 0)
        {
            for (final UserFormatModuleDescriptor descriptor : userFormatModuleDescriptors)
            {
                removeUserFormatFrom(descriptor);
            }
        }
    }

    private void removeUserFormatFrom(final UserFormatModuleDescriptor descriptor)
    {
        for (final String userFormatType : delegate.getConfiguredTypes())
        {
            if (getUserFormatKeyForType(userFormatType).equals(descriptor.getCompleteKey()))
            {
                remove(userFormatType);
                break;
            }
        }
    }

    @Override
    public boolean containsType(final String userFormatType)
    {
        return delegate.containsType(userFormatType);
    }

    @Override
    public void setUserFormatKeyForType(final String userFormatType, final String moduleKey)
    {
        delegate.setUserFormatKeyForType(userFormatType, moduleKey);
    }

    @Override
    public String getUserFormatKeyForType(final String userFormatType)
    {
        return delegate.getUserFormatKeyForType(userFormatType);
    }

    @Override
    public void remove(final String userFormatType)
    {
        delegate.remove(userFormatType);
    }
}
