package com.atlassian.jira.config.component;

import com.atlassian.jira.config.properties.ApplicationProperties;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public class AppPropertiesComponentAdaptor extends AbstractSwitchingInvocationAdaptor
{
    private final String appPropertiesKey;
    private InvocationSwitcher invocationSwitcher;

    public AppPropertiesComponentAdaptor(Class interfaceClass, Class enabledClass, Class disabledClass, String appPropertiesKey)
    {
        super(interfaceClass, enabledClass, disabledClass);
        this.appPropertiesKey = appPropertiesKey;
    }

    protected InvocationSwitcher getInvocationSwitcher()
    {
        // Lazy load the switcher so that ApplicationProperties are also lazy loaded.
        if (invocationSwitcher == null)
        {
            invocationSwitcher = new AppPropertiesInvocationSwitcherImpl(getProperties(), appPropertiesKey);
        }

        return invocationSwitcher;
    }

    private ApplicationProperties getProperties()
    {
        // Lazy load the application properties
        return (ApplicationProperties) getContainer().getComponentInstanceOfType(ApplicationProperties.class);
    }
}
