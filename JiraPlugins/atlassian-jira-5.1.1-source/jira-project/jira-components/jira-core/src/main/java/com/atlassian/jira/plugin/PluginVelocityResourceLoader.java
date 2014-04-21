package com.atlassian.jira.plugin;

import com.atlassian.jira.ComponentManager;
import com.atlassian.plugin.PluginAccessor;
import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;

import java.io.InputStream;

public class PluginVelocityResourceLoader extends ResourceLoader
{
    private PluginAccessor pluginAccessor;

    @Override
    public void init(final ExtendedProperties configuration)
    {
        rsvc.info("PluginVelocityResourceLoader : initialization starting.");
        rsvc.info("PluginVelocityResourceLoader : initialization complete.");
    }

    @Override
    public InputStream getResourceStream(String name) throws ResourceNotFoundException
    {
        while (name.startsWith("/") && (name.length() > 1))
        {
            name = name.substring(1);
        }

        if (pluginAccessor == null)
        {
            pluginAccessor = ComponentManager.getComponentInstanceOfType(PluginAccessor.class);
            if (pluginAccessor == null)
            {
                throw new ResourceNotFoundException("No plugin manager");
            }
        }

        return pluginAccessor.getDynamicResourceAsStream(name);
    }

    /**
     * @return always returns 0
     */
    @Override
    public long getLastModified(final Resource resource)
    {
        return 0;
    }

    /**
     * @return always returns true
     */
    @Override
    public boolean isSourceModified(final Resource resource)
    {
        return true;
    }
}
