/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.config.component;

/**
 * The SwitchingInvocationAdapter returns a proxy that allows for dynamic determination of
 * which class implementation to be called on when invoking a specified method.
 */
public class SwitchingInvocationAdapter extends AbstractSwitchingInvocationAdaptor
{
    private final InvocationSwitcher invocationSwitcher;

    public SwitchingInvocationAdapter(Class interfaceClass, Class enabledClass, Class disabledClass, InvocationSwitcher invocationSwitcher)
    {
        super(interfaceClass, enabledClass, disabledClass);
        this.invocationSwitcher = invocationSwitcher;
    }


    protected InvocationSwitcher getInvocationSwitcher()
    {
        return invocationSwitcher;
    }
}
