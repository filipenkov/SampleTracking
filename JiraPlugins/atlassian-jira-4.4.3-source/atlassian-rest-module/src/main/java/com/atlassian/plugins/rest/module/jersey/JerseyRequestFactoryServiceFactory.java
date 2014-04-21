package com.atlassian.plugins.rest.module.jersey;

import com.atlassian.plugin.AutowireCapablePlugin;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.osgi.util.OsgiHeaderUtil;
import com.atlassian.sal.api.net.NonMarshallingRequestFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

public class JerseyRequestFactoryServiceFactory implements ServiceFactory
{
    private final PluginAccessor pluginAccessor;
    private final NonMarshallingRequestFactory requestFactory;

    public JerseyRequestFactoryServiceFactory(PluginAccessor pluginAccessor, NonMarshallingRequestFactory requestFactory)
    {
        this.pluginAccessor = pluginAccessor;
        this.requestFactory = requestFactory;
    }

    public Object getService(final Bundle bundle, final ServiceRegistration serviceRegistration)
    {
        final Plugin plugin = pluginAccessor.getPlugin(OsgiHeaderUtil.getPluginKey(bundle));

        if (!(plugin instanceof AutowireCapablePlugin))
        {
            throw new IllegalStateException("Can't create RequestFactory for non OsgiPlugin");
        }
        else
        {
            return new JerseyRequestFactory(requestFactory, plugin, bundle);
        }
    }

    public void ungetService(final Bundle bundle, final ServiceRegistration serviceRegistration, final Object o)
    {
        ((JerseyRequestFactory) o).destroy();
    }
}
