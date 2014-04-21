package com.atlassian.jira.multitenant;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.ComponentManager;
import com.atlassian.plugin.osgi.hostcomponents.ComponentRegistrar;
import com.atlassian.plugin.osgi.hostcomponents.HostComponentProvider;
import com.atlassian.plugin.osgi.hostcomponents.impl.DefaultComponentRegistrar;

/**
 * 
 *
 * @since v4.3
 */
public class MultiTenantHostComponentProvider implements HostComponentProvider
{
    private final MultiTenantHostComponentProxier proxier;
    private final HostComponentProvider target;

    public MultiTenantHostComponentProvider(final HostComponentProvider target, final MultiTenantHostComponentProxier proxier)
    {
        this.target = target;
        this.proxier = proxier;
    }

    public void provide(final ComponentRegistrar registrar)
    {
        // First provide the event publisher for the plugin system, which is different but peered to the tenant specific
        // ones
        registrar.register(EventPublisher.class).forInstance(ComponentManager.getComponent(PluginsEventPublisher.class));
        DefaultComponentRegistrar tmpRegistrar = new DefaultComponentRegistrar();
        target.provide(tmpRegistrar);
        proxier.addToRegistry(tmpRegistrar.getRegistry(), registrar);
    }
}
