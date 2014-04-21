package com.atlassian.plugins.rest.module.expand.resolver;

import com.atlassian.plugin.AutowireCapablePlugin;
import com.atlassian.plugins.rest.common.expand.EntityExpander;
import com.atlassian.plugins.rest.common.expand.Expander;
import com.atlassian.plugins.rest.common.expand.resolver.AbstractAnnotationEntityExpanderResolver;

import static com.google.common.base.Preconditions.checkNotNull;

public class PluginEntityExpanderResolver extends AbstractAnnotationEntityExpanderResolver
{
    private final AutowireCapablePlugin plugin;

    public PluginEntityExpanderResolver(AutowireCapablePlugin plugin)
    {
        this.plugin = checkNotNull(plugin);
    }

    protected final EntityExpander<?> getEntityExpander(Expander expander)
    {
        return plugin.autowire(expander.value());
    }
}
