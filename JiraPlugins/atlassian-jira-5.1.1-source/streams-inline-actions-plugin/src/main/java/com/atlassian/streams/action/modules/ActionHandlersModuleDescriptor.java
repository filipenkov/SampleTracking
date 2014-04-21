package com.atlassian.streams.action.modules;

import com.atlassian.plugin.hostcontainer.DefaultHostContainer;
import com.atlassian.plugin.webresource.WebResourceModuleDescriptor;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A marker class for action handling web resources.
 */
public class ActionHandlersModuleDescriptor extends WebResourceModuleDescriptor
{
    public ActionHandlersModuleDescriptor()
    {
        super(checkNotNull(new DefaultHostContainer(), "hostContainer"));
    }
}
