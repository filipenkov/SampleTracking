package com.atlassian.applinks.core.plugin;

import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.util.validation.ValidationPattern;
import com.atlassian.util.concurrent.NotNull;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

import static com.atlassian.plugin.util.validation.ValidationPattern.test;

public class AbstractAppLinksTypeModuleDescriptor<T> extends AbstractModuleDescriptor<T>
{
    private Iterable<String> interfaces;

    public AbstractAppLinksTypeModuleDescriptor(final ModuleFactory moduleFactory)
    {
        super(moduleFactory);
    }

    @Override
    protected void provideValidationRules(final ValidationPattern pattern)
    {
        super.provideValidationRules(pattern);
        pattern
                .rule(
                        test("@class")
                                .withError("No " + ApplicationType.class.getSimpleName() + "  class specified.")
                );
    }

    @Override
    public void init(@NotNull final Plugin plugin, @NotNull final Element element) throws PluginParseException
    {
        super.init(plugin, element);

        final List<String> interfaces = new ArrayList<String>();
        for (final Element child : (List<Element>) element.elements("interface"))
        {
            interfaces.add(child.getTextTrim());
        }
        if (element.attributeValue("interface") != null)
        {
            interfaces.add(element.attributeValue("interface"));
        }
        this.interfaces = interfaces;
    }

    public Iterable<String> getInterfaces()
    {
        return interfaces;
    }

    @Override
    public T getModule()
    {
        return moduleFactory.createModule(moduleClassName, this);
    }
}
