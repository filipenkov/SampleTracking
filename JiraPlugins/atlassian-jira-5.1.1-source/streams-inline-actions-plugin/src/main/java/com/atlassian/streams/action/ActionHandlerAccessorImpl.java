package com.atlassian.streams.action;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.streams.action.modules.ActionHandlersModuleDescriptor;

import com.google.common.base.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.transform;

public class ActionHandlerAccessorImpl implements ActionHandlerAccessor
{
    private final PluginAccessor pluginAccessor;
    
    public ActionHandlerAccessorImpl(PluginAccessor pluginAccessor)
    {
        this.pluginAccessor = checkNotNull(pluginAccessor, "pluginAccessor");
    }

    public Iterable<String> getActionHandlerModuleKeys()
    {
        return transform(getActionHandlerModuleDescriptors(), toCompleteModuleKey);
    }
    
    private Iterable<ActionHandlersModuleDescriptor> getActionHandlerModuleDescriptors()
    {
        return pluginAccessor.getEnabledModuleDescriptorsByClass(ActionHandlersModuleDescriptor.class);
    }
    
    private static final Function<ActionHandlersModuleDescriptor, String> toCompleteModuleKey = new Function<ActionHandlersModuleDescriptor, String>()
    {
        public String apply(ActionHandlersModuleDescriptor module)
        {
            return module.getCompleteKey();
        }
    };
}
