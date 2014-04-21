package com.atlassian.soy.renderer;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.module.ContainerManagedPlugin;
import com.atlassian.plugin.util.ClassLoaderUtils;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.dom4j.Element;
import com.atlassian.plugin.webresource.WebResourceModuleDescriptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SoyResourceModuleDescriptor extends WebResourceModuleDescriptor
{

    public static final String XML_ELEMENT_NAME = "soy-resource".intern(); // Prevent compiler replacement of constants for literals

    private Iterable<String> functionNames = Collections.emptyList();
    private Iterable<String> nativeFunctionNames = Collections.emptyList();
    private Iterable<Class<? extends SoyServerFunction<?>>> functions = Collections.emptyList();
    private Iterable<Class<?>> nativeFunctions = Collections.emptyList();
    private HostContainer hostContainer;

    public SoyResourceModuleDescriptor(final HostContainer hostContainer)
    {
        super(hostContainer);
        this.hostContainer = hostContainer;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void init(final Plugin plugin, final Element element) throws PluginParseException
    {
        super.init(plugin, element);

        functionNames = Collections.unmodifiableList(getContentsOfChildElements(element, "function"));
        nativeFunctionNames = Collections.unmodifiableList(getContentsOfChildElements(element, "soy-function"));
    }

    @Override
    public void enabled()
    {
        super.enabled();
        List<Class<? extends SoyServerFunction<?>>> funcs = Lists.newArrayList();
        for (String functionName : functionNames)
        {
            funcs.add(loadFunctionClass(getPlugin(), functionName));
        }
        functions = funcs;

        List<Class<?>> nativeFuncs = Lists.newArrayList();
        for (String nativeFunctionName : nativeFunctionNames)
        {
            nativeFuncs.add(loadClassByName(getPlugin(), nativeFunctionName));
        }
        nativeFunctions = nativeFuncs;
    }

    @SuppressWarnings("unchecked")
    private Class<? extends SoyServerFunction<?>> loadFunctionClass(Plugin plugin, String functionClass)
    {
        Class<?> clazz;
        clazz = loadClassByName(plugin, functionClass);
        if (!SoyServerFunction.class.isAssignableFrom(clazz)) {
            throw new IllegalStateException("Class " + functionClass + " does not implement " + SoyServerFunction.class.getName());
        }
        return (Class<? extends SoyServerFunction<?>>) clazz;
    }

    private Class<?> loadClassByName(Plugin plugin, String className)
    {
        Class<?> clazz;
        try {
            clazz = plugin.loadClass(className, SoyResourceModuleDescriptor.class);
        } catch (ClassNotFoundException e) {
            try {
                clazz = ClassLoaderUtils.loadClass(className, SoyResourceModuleDescriptor.class);
            } catch (ClassNotFoundException e1) {
                throw new IllegalStateException("Failed to load class '" + className + "' for plugin " + plugin.getName(), e);
            }
        }
        return clazz;
    }

    public Iterable<SoyServerFunction> getFunctions() {
        return Iterables.transform(functions, new Function<Class<? extends SoyServerFunction>, SoyServerFunction>()
        {
            public SoyServerFunction apply(Class<? extends SoyServerFunction> functionClass)
            {
                return createBean(functionClass);
            }
        });
    }

    public Iterable<Object> getNativeFunctions() {
        return Iterables.transform(nativeFunctions, new Function<Class<?>, Object>() {
            @Override
            public Object apply(Class<?> functionClass)
            {
                return createBean(functionClass);
            }
        });
    }

    private <T> T createBean(Class<? extends T> klass) {
        if (getPlugin() instanceof ContainerManagedPlugin) {
            return ((ContainerManagedPlugin) getPlugin()).getContainerAccessor().createBean(klass);
        }
        else
        {
            return hostContainer.create(klass);
        }
    }

    @SuppressWarnings({"unchecked"})
    private List<String> getContentsOfChildElements(Element element, String elementName)
    {
        final List<String> strings = Lists.newArrayList();
        for (Element e : (Iterable<Element>) element.elements(elementName))
        {
            strings.add(e.getTextTrim());
        }
        return strings;
    }
}
