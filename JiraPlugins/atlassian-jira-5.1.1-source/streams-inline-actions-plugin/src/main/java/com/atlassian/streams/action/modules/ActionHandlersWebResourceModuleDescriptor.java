package com.atlassian.streams.action.modules;

import java.util.List;

import com.atlassian.plugin.hostcontainer.DefaultHostContainer;
import com.atlassian.plugin.webresource.WebResourceModuleDescriptor;
import com.atlassian.streams.action.ActionHandlerAccessor;

import com.google.common.collect.ImmutableList;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.concat;

/**
 * {@code ModuleDescriptor} similar to {@code WebResourceModuleDescriptor} with extended functionality
 * to locate any action handlers (defined by {@code ActionHandlersModuleDescriptor}s) currently enabled in the system.
 */
public class ActionHandlersWebResourceModuleDescriptor extends WebResourceModuleDescriptor
{
    private final ActionHandlerAccessor actionHandlerAccessor;
    
    public ActionHandlersWebResourceModuleDescriptor(ActionHandlerAccessor actionHandlerAccessor)
    {
        super(checkNotNull(new DefaultHostContainer(), "hostContainer"));
        this.actionHandlerAccessor = checkNotNull(actionHandlerAccessor, "actionHandlerAccessor");
    }
    
    @Override
    public List<String> getDependencies()
    {
        return ImmutableList.copyOf(concat(super.getDependencies(), actionHandlerAccessor.getActionHandlerModuleKeys()));
    }
}
