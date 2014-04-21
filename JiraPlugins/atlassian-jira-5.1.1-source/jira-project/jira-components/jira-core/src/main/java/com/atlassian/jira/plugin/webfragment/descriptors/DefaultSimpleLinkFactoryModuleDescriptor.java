package com.atlassian.jira.plugin.webfragment.descriptors;

import com.atlassian.jira.plugin.util.ModuleDescriptors;
import com.atlassian.jira.plugin.webfragment.SimpleLinkFactory;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.plugin.AutowireCapablePlugin;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import org.dom4j.Element;

/**
 * Plugin in Module descriptor that defines a {@link com.atlassian.jira.plugin.webfragment.SimpleLinkFactory}.  This is
 * used by the {@link com.atlassian.jira.plugin.webfragment.SimpleLinkManager} to display a list of simple links. The
 * special attributes for this are: lazy - whether the section should be loaded lazily, weight - the position to include
 * the generated list, and section - the section that the generated links go into.
 *
 * @since v4.0
 */
public class DefaultSimpleLinkFactoryModuleDescriptor extends AbstractModuleDescriptor<SimpleLinkFactory>
        implements SimpleLinkFactoryModuleDescriptor
{
    private int weight;
    private boolean shouldBeLazy = false;
    private String section;
    private SimpleLinkFactory module;

    @Override
    public void init(Plugin plugin, Element element) throws PluginParseException
    {
        super.init(plugin, element);

        weight = 1000;
        try
        {
            weight = Integer.parseInt(element.attributeValue("weight"));
        }
        catch (final NumberFormatException e)
        {
            throw new PluginParseException(String.format("The plugin module: %s specified a weight attribute that is not an integer.", getCompleteKey()), e);
        }

        shouldBeLazy = Boolean.valueOf(element.attributeValue("lazy"));
        section = element.attributeValue("section");
    }

    public boolean shouldBeLazy()
    {
        return shouldBeLazy;
    }

    public int getWeight()
    {
        return weight;
    }

    public String getSection()
    {
        return section;
    }

    @Override
    public SimpleLinkFactory getModule()
    {
        if (module == null)
        {
            final Plugin plugin = getPlugin();
            if (plugin instanceof AutowireCapablePlugin)
            {
                module = ((AutowireCapablePlugin) plugin).autowire(getModuleClass());
            }
            else
            {
                module = JiraUtils.loadComponent(getModuleClass());
            }
            if (module != null)
            {
                module.init(this);
            }
        }
        return module;
    }

    @Override
    public boolean equals(Object obj)
    {
        return new ModuleDescriptors.EqualsBuilder(this).isEqualsTo(obj);
    }

    @Override
    public int hashCode()
    {
        return new ModuleDescriptors.HashCodeBuilder(this).toHashCode();
    }
}

