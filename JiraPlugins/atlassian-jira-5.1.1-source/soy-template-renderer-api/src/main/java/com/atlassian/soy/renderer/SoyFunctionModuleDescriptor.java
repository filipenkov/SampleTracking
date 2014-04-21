package com.atlassian.soy.renderer;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.module.ContainerManagedPlugin;
import com.atlassian.plugin.module.ModuleFactory;

public class SoyFunctionModuleDescriptor extends AbstractModuleDescriptor {
    
    public static final String XML_ELEMENT_NAME = "soy-function".intern(); // Prevent compiler replacement of constants for literals

    private final HostContainer hostContainer;

    public SoyFunctionModuleDescriptor(final ModuleFactory factory, final HostContainer hostContainer) {
        super(factory);
        this.hostContainer = hostContainer;
    }

    @Override
    protected void loadClass(Plugin plugin, String clazz) throws PluginParseException {
        try
        {
            this.moduleClass = plugin.loadClass(clazz, null);
        }
        catch (ClassNotFoundException e)
        {
            throw new PluginParseException("cannot load soy-function class", e);
        }
    }

    @Override
    public Object getModule() {
        return createBean(getModuleClass());
    }

    private <T> T createBean(Class<? extends T> klass) {
        if (getPlugin() instanceof ContainerManagedPlugin) {
            return ((ContainerManagedPlugin) getPlugin()).getContainerAccessor().createBean(klass);
        } else {
            return hostContainer.create(klass);
        }
    }
    
}
