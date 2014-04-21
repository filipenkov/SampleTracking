package com.atlassian.jira.mock.plugin;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginException;
import com.atlassian.plugin.PluginInformation;
import com.atlassian.plugin.PluginState;
import com.atlassian.plugin.Resourced;
import com.atlassian.plugin.Resources;
import com.atlassian.plugin.elements.ResourceDescriptor;
import com.atlassian.plugin.elements.ResourceLocation;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Iterables.filter;

public class MockPlugin implements Plugin
{
    private int pluginsVersion;
    private String name;
    private String key;
    private boolean enabledByDefault;
    private PluginInformation pluginInformation;
    private boolean enabled;
    private boolean systemPlugin;
    private PluginState pluginState;
    private final Map<String,String> resources = Maps.newHashMap();

    private ClassLoader classLoader = getClass().getClassLoader();

    private final Map<String,ModuleDescriptor<?>> moduleDescriptors = new HashMap<String,ModuleDescriptor<?>>();
    private final List<ResourceDescriptor> resourceDescriptors = new ArrayList<ResourceDescriptor>(); 

    public MockPlugin(String name, String key, PluginInformation pluginInformation, PluginState pluginState)
    {
        this.name = name;
        this.key = key;
        this.pluginInformation = pluginInformation;
        this.pluginState = pluginState;
    }


    public MockPlugin(String name, String key, PluginInformation pluginInformation)
    {
        this.name = name;
        this.key = key;
        this.pluginInformation = pluginInformation;
    }


    @Override
    public int getPluginsVersion()
    {
        return pluginsVersion;
    }

    @Override
    public void setPluginsVersion(int version)
    {
        pluginsVersion = version;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public String getI18nNameKey()
    {
        return null;
    }

    @Override
    public void setI18nNameKey(String i18nNameKey)
    {
    }

    @Override
    public String getKey()
    {
        return key;
    }

    @Override
    public void setKey(String aPackage)
    {
        this.key = aPackage;
    }

    @Override
    public void addModuleDescriptor(ModuleDescriptor<?> moduleDescriptor)
    {
        this.moduleDescriptors.put(moduleDescriptor.getKey(), moduleDescriptor);
    }

    @Override
    public Collection<ModuleDescriptor<?>> getModuleDescriptors()
    {
        return ImmutableList.copyOf(moduleDescriptors.values());
    }

    @Override
    public ModuleDescriptor<?> getModuleDescriptor(String key)
    {
        return moduleDescriptors.get(key);
    }

    @Override
    public <M> List<ModuleDescriptor<M>> getModuleDescriptorsByModuleClass(final Class<M> moduleClass)
    {
        @SuppressWarnings("unchecked") Iterable<ModuleDescriptor<M>> result = (Iterable)Iterables.filter(getModuleDescriptors(), new Predicate<ModuleDescriptor<?>>()
        {
            @Override
            public boolean apply(@Nullable ModuleDescriptor<?> input)
            {
                return input.getModuleClass().equals(moduleClass);
            }
        });
        return ImmutableList.copyOf(result);
    }

    @Override
    public boolean isEnabledByDefault()
    {
        return enabledByDefault;
    }

    @Override
    public void setEnabledByDefault(boolean enabledByDefault)
    {
        this.enabledByDefault = enabledByDefault;
    }

    @Override
    public PluginInformation getPluginInformation()
    {
        return pluginInformation;
    }

    @Override
    public void setPluginInformation(PluginInformation pluginInformation)
    {
        this.pluginInformation = pluginInformation;
    }

    @Override
    public void setResources(Resourced resources)
    {
    }

    @Override
    public PluginState getPluginState()
    {
        return pluginState;
    }

    @Override
    public boolean isEnabled()
    {
        return enabled;
    }

    @Override
    public boolean isSystemPlugin()
    {
        return systemPlugin;
    }

    @Override
    public void setSystemPlugin(boolean system)
    {
        this.systemPlugin = system;
    }

    @Override
    public boolean containsSystemModule()
    {
        return false;
    }

    @Override
    public boolean isBundledPlugin()
    {
        return false;
    }

    @Override
    public Date getDateLoaded()
    {
        return null;
    }

    @Override
    public boolean isUninstallable()
    {
        return false;
    }

    @Override
    public boolean isDeleteable()
    {
        return false;
    }

    @Override
    public boolean isDynamicallyLoaded()
    {
        return false;
    }

    @Override
    public <T> Class<T> loadClass(String clazz, Class<?> callingClass) throws ClassNotFoundException
    {
        return null;
    }

    public MockPlugin classLoader(ClassLoader loader)
    {
        this.classLoader = loader;
        return this;
    }

    @Override
    public ClassLoader getClassLoader()
    {
        return classLoader;
    }

    @Override
    public URL getResource(String path)
    {
        return null;
    }

    @Override
    public InputStream getResourceAsStream(String name)
    {
        final String resource = resources.get(name);
        if (resource != null)
        {
            return IOUtils.toInputStream(resource);
        }
        return null;
    }

    public MockPlugin resource(String name, String value)
    {
        resources.put(name, value);
        return this;
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    @Override
    public void close()
    {
    }

    @Override
    public void install() throws PluginException
    {
    }

    @Override
    public void uninstall() throws PluginException
    {
    }

    @Override
    public void enable() throws PluginException
    {
    }

    @Override
    public void disable() throws PluginException
    {
    }

    @Override
    public Set<String> getRequiredPlugins()
    {
        return null;
    }

    @Override
    public int compareTo(Plugin o)
    {
        return 0;
    }

    public MockPlugin addResourceDescriptor(ResourceDescriptor rd)
    {
        resourceDescriptors.add(rd);
        return this;
    }

    /**
     * Add resource descriptor and corresponding contents that will be returned by
     * {@link #getResourceAsStream(String)}.
     *
     * @param resourceDescriptor resource descriptor to add
     * @param contents corresponding contents
     * @return this mock plugin
     */
    public MockPlugin addResourceDescriptor(ResourceDescriptor resourceDescriptor, String contents)
    {
        resourceDescriptors.add(resourceDescriptor);
        resource(resourceDescriptor.getLocation(), contents);
        return this;
    }

    @Override
    public List<ResourceDescriptor> getResourceDescriptors()
    {
        return ImmutableList.copyOf(resourceDescriptors);
    }

    @Override
    public List<ResourceDescriptor> getResourceDescriptors(String type)
    {
        return ImmutableList.copyOf(filter(resourceDescriptors, new Resources.TypeFilter(type)));
    }

    @Override
    public ResourceDescriptor getResourceDescriptor(String type, String name)
    {
        // TODO
        return null;
    }

    @Override
    public ResourceLocation getResourceLocation(String type, String name)
    {
        // TODO
        return null;
    }
}
